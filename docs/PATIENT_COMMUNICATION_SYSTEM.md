# Patient Communication System (SMS and WhatsApp)

**Purpose:** Send and receive messages to/from patients and caregivers via **SMS** and **WhatsApp** for reminders, follow-up, and simple responses, under **consent**, with support for **low literacy**, **multiple local languages**, and **shared phones**. The system uses **templates** for outbound content; **AI** assists with personalisation, timing, language selection, and response interpretation. All flows are **logged** and **auditable**, with **risk-of-harm mitigation** built in.

**Principles:** Consent is required before sending; no clinical diagnosis or advice in messages; shared phones require no sensitive content in message body; AI supports—not replaces—human decisions.

---

## 1. Constraints and Implications

| Constraint | Implication |
|------------|-------------|
| **Low literacy** | Short messages; simple words; numbers not long sentences; optional pictograms; no medical jargon. |
| **Multiple local languages** | Templates per language; language selection per contact; AI or CHW sets preferred language. |
| **Shared phones** | No patient name or clinical detail in message body when possible; use "Your appointment" or codes; identify program only ("Afya" / local name). |
| **Consent required** | No marketing or non-essential messages without opt-in; clear opt-out in every message or first message; record consent and opt-out in audit. |

---

## 2. Opt-In / Opt-Out Flows

### 2.1 Consent model

- **Explicit opt-in** before any programmatic SMS/WhatsApp (except one-time opt-out reply to an already-sent message).
- Consent is **per channel** (SMS and/or WhatsApp) and **per purpose** (e.g. reminders, health tips) if the program supports multiple purposes.
- Consent is stored with **contact** (phone number + patient/caregiver link), **timestamp**, **who obtained it** (CHW id or "self"), and **method** (in-person, paper, app).

### 2.2 Data schema (consent)

```kotlin
data class ContactConsent(
    val id: String,
    val patientId: String,
    val contactPhone: String,          // E.164 or national format
    val contactRole: ContactRole,      // PATIENT, CAREGIVER, OTHER
    val channel: Channel,             // SMS, WHATSAPP
    val purpose: String,               // "reminders", "health_tips"
    val consentedAt: Long,
    val consentedBy: String,           // chwId or "patient_self"
    val consentMethod: ConsentMethod,  // IN_PERSON, PAPER, APP
    val preferredLanguage: String?,    // "en", "sw", "lg"
    val lowLiteracyPreferred: Boolean,
    val optedOutAt: Long?,
    val optOutReason: String?,         // optional from reply or CHW
    val lastMessageAt: Long?
)

enum class ContactRole { PATIENT, CAREGIVER, OTHER }
enum class ConsentMethod { IN_PERSON, PAPER, APP }
```

### 2.3 Opt-in flow

1. **CHW (or enrollment)** asks: "Can we send you reminders by [SMS/WhatsApp]? In which language?"
2. CHW records phone number and language (and low-literacy preference if offered).
3. System creates **ContactConsent** with `consentedAt`, `consentedBy`, `consentMethod`.
4. **Optional:** Send one confirmation message: "You are now signed up for Afya reminders. Reply STOP to opt out." (Template; language and shared-phone safe.)
5. Log: `consent_recorded` (contactId, channel, purpose, chwId).

### 2.4 Opt-out flow

- **In every message (or first message):** Include short opt-out instruction: "Reply STOP to stop messages."
- **When system receives STOP (or local equivalent):**
  1. Match phone number to ContactConsent; set `optedOutAt = now`, `optOutReason = "REPLY_STOP"`.
  2. Cease all further outbound to that number for that purpose/channel until new opt-in.
  3. Log: `opt_out` (contactId, channel, source: "reply", content).
- **CHW-initiated opt-out:** CHW marks "Do not send messages" in app; system sets `optedOutAt`, `optOutReason = "CHW_REQUEST"`; log same.
- **Re-opt-in:** New consent record (new `consentedAt`); old opt-out remains in history for audit.

### 2.5 Shared-phone handling in consent

- At consent, CHW can mark "Shared phone" (optional field).
- When true: system prefers templates that **do not** include patient name or clinical reason in body (e.g. "Reminder: visit on {dueDate}. Reply OK or STOP."); language and date only.
- If name is required for clarity, use first name only and avoid condition (e.g. "Amina – visit Wed 5 Feb. Reply STOP to stop.").

---

## 3. Message Templates

Templates are the **only** source of outbound message body content (no free-form AI-generated clinical text). Align with [Treatment plan generator](TREATMENT_PLAN_GENERATOR.md) templates where applicable.

### 3.1 Template schema (extended)

```kotlin
data class MessageTemplate(
    val id: String,
    val purpose: String,               // "follow_up_reminder", "medication_reminder", "opt_out_confirm"
    val channel: Channel,
    val languageCode: String,
    val lowLiteracyVariant: Boolean,
    val sharedPhoneSafe: Boolean,     // true = no name, no clinical reason in body
    val body: String,
    val placeholders: List<String>,
    val optOutClause: String?,        // e.g. " Reply STOP to stop." — appended if not in body
    val maxLength: Int?                // SMS: 160 for single segment
)
```

### 3.2 Placeholders (common)

| Placeholder | Use | Shared-phone safe? |
|-------------|-----|--------------------|
| `{patientName}` | First name only if sharedPhoneSafe=false | No |
| `{dueDate}` | "Wed 5 Feb" or "5/2" | Yes |
| `{reason}` | Short reason ("Recheck") | No – omit in shared-phone variant |
| `{medicationName}` | "ORS" | Yes |
| `{plainInstruction}` | One line | Yes |
| `{chwName}` | CHW first name | Optional |
| `{programName}` | "Afya" | Yes |

### 3.3 Example templates (low literacy, multi-language)

**Follow-up reminder (SMS, English, low literacy, shared-phone safe)**  
`Reminder: visit {dueDate}. Reply OK or STOP.`

**Follow-up reminder (SMS, Swahili, low literacy)**  
`Ukumbusho: njoo {dueDate}. Jibu OK au STOP.`

**Opt-out confirmation (any language)**  
`You have been unsubscribed. We will not send more messages. Reply START to sign up again.`

**Medication reminder (SMS, low literacy)**  
`Today: give {medicationName}. {plainInstruction}. Reply STOP to stop.`

All outbound messages **append or include** the opt-out clause so consent can be withdrawn at any time.

---

## 4. AI Responsibilities

AI **supports** decisions; it does not send messages without going through templates and consent checks. No AI-generated free-form clinical content to patients.

### 4.1 Message personalisation

- **Inputs:** Contact (language, low-literacy flag, shared-phone flag), template purpose, plan/visit context.
- **Behaviour:** Choose **template variant** (language, low-literacy, shared-phone safe) and fill **placeholders** (name, date, reason, medication, plain instruction) from structured data only.
- **Limits:** No generative text; no diagnosis or new clinical advice; personalisation = which template + which placeholder values.

### 4.2 Timing optimisation

- **Inputs:** Contact timezone or area, historical open/response rates by hour/day (if available), quiet hours (e.g. 07:00–20:00 local).
- **Behaviour:** Choose **send window** (e.g. 08:00–10:00 local) for reminders to maximise likelihood of read and reply while respecting quiet hours.
- **Fallback:** Default to fixed time (e.g. 08:00) if no data; no sending during quiet hours.
- **Log:** Scheduled time and actual send time for audit.

### 4.3 Language selection

- **Inputs:** Contact’s `preferredLanguage` from consent; if missing, patient or area default from registration.
- **Behaviour:** Select template with matching `languageCode`; fallback to English or program default if no match.
- **Log:** Language used per message so we can improve template coverage.

### 4.4 Response interpretation

- **Inputs:** Inbound message (SMS/WhatsApp) from a known number: body text, timestamp.
- **Behaviour:**
  - **Opt-out:** Detect STOP, UNSUBSCRIBE, and local equivalents (e.g. "ondoa", "stop"); trigger opt-out flow; no clinical interpretation.
  - **Simple acknowledgment:** Detect OK, YES, "ndiyo", "sawa" → mark "patient responded" for follow-up/reminder; optional: link to plan or visit for CHW view.
  - **Unclear or other:** Do **not** infer clinical meaning; store raw message, flag for CHW review; optionally reply with template: "Thanks. If you need to talk to your CHW, call [number]. Reply STOP to stop."
- **Limits:** No diagnosis or clinical advice based on free text; escalation to human only (e.g. "needs call-back" or "see in person").

### 4.5 No AI-generated clinical content

- Outbound body text is **always** from a **template** plus placeholder substitution.
- AI does not write new sentences about conditions, treatments, or results; it only picks template and values.

---

## 5. Risk of Harm Mitigation

| Risk | Mitigation |
|------|------------|
| **Wrong person reads (shared phone)** | Shared-phone-safe templates (no name/condition); optional "shared phone" flag at consent; first name only if needed. |
| **Patient misunderstands and acts wrongly** | No clinical advice in messages; only reminders and simple instructions already given in person; use plain-language templates; "Contact your CHW" in reply template. |
| **Harassment or over-messaging** | Consent required; opt-out in every message; cap messages per contact per day (e.g. 2); quiet hours; no marketing. |
| **Breach of confidentiality** | No diagnosis or sensitive detail in body; logs and DB access restricted; audit trail for who saw what. |
| **Opt-out ignored** | On STOP (or equivalent), immediate opt-out and no further sends until re-opt-in; log and alert if send attempted after opt-out. |
| **Wrong language** | Language stored at consent; template selected by that; fallback to default; log language used. |
| **Danger sign or emergency in reply** | Do not interpret as diagnosis; store reply, flag for CHW/supervisor; optional auto-reply: "If this is an emergency, go to [facility] or call [number]. Reply STOP to stop." |

### 5.1 Escalation from inbound messages

- Keywords or free text suggesting **emergency** (e.g. "can't breathe", "unconscious") can trigger **flag only**: notify CHW/supervisor to call or visit; **no** automated clinical response.
- No bot-delivered clinical advice; only templated signposting to facility or CHW.

---

## 6. Logging and Audit Trails

### 6.1 Events to log

| Event | Data to log |
|-------|-------------|
| **Consent** | contactId, channel, purpose, consentedAt, consentedBy, consentMethod, preferredLanguage. |
| **Opt-out** | contactId, channel, optedOutAt, source (reply/chw), raw reply if from message. |
| **Message sent** | contactId, channel, templateId, purpose, placeholder values (sanitised: no full name in log if shared-phone), scheduledTime, sentAt, messageId from provider. |
| **Message delivery** | messageId, status (delivered/failed), provider code, at. |
| **Inbound received** | phone, body (truncated or hashed if policy requires), receivedAt, matched contactId. |
| **Response interpreted** | contactId, raw body, interpretation (OPT_OUT, ACK, UNKNOWN), action taken. |
| **Send attempted after opt-out** | contactId, blockedAt, who triggered (system/chw). |

### 6.2 Audit schema (minimal)

```kotlin
data class CommunicationAuditEntry(
    val id: String,
    val at: Long,
    val eventType: String,     // "consent", "opt_out", "message_sent", "message_received", "response_interpreted"
    val contactId: String?,
    val channel: Channel?,
    val payload: String,      // JSON or key-value; no sensitive free text if policy restricts
    val actor: String?        // chwId, "system", "patient"
)
```

### 6.3 Retention and access

- Retention: per program policy (e.g. 2 years for consent/opt-out; 90 days for message body if stored).
- Access: only authorised roles (e.g. supervisor, audit) for full payloads; CHWs see only their contacts and summary.
- Opt-out and consent history kept for dispute resolution and compliance.

---

## 7. End-to-End Flows (Summary)

### 7.1 Sending a reminder

1. **Eligibility:** Plan has follow-up; due date in send window; contact has consent for channel and purpose; not opted out.
2. **Template selection:** AI (or rules): language + low-literacy + shared-phone safe → pick template.
3. **Personalisation:** Fill placeholders from plan/visit (no free text).
4. **Timing:** AI or default: choose time in allowed window; schedule send.
5. **Send:** Via SMS/WhatsApp gateway; log `message_sent` with templateId, time, contactId.
6. **Opt-out clause:** Ensure present in body (in template or appended).

### 7.2 Receiving a reply

1. **Inbound:** Gateway receives message; match phone to ContactConsent.
2. **Interpret:** AI/rules: opt-out (STOP etc.) → opt-out flow and log; OK/YES etc. → mark response for plan, log; other → log and flag for CHW.
3. **No clinical advice:** No automated clinical response; only templated reply if needed (e.g. "Thanks. Reply STOP to stop." or "Contact your CHW.").
4. **Audit:** Log `message_received` and `response_interpreted`.

### 7.3 Opt-out

1. **Trigger:** Reply STOP (or equivalent) or CHW marks opt-out.
2. **Update:** Set `optedOutAt`, `optOutReason` on ContactConsent; stop all sends to that number for that purpose/channel.
3. **Optional confirm:** One templated reply: "You have been unsubscribed."
4. **Audit:** Log `opt_out`.

---

## 8. Implementation Checklist

- [ ] **ContactConsent** table and UI for CHW to record consent (channel, language, low-literacy, shared phone).
- [ ] **MessageTemplate** store with purpose, channel, language, low-literacy, sharedPhoneSafe, body, placeholders, optOutClause.
- [ ] **Outbound:** Eligibility (consent, not opted out, purpose match); template selection (language, low-literacy, shared-phone); placeholder fill; schedule; send; log.
- [ ] **Inbound:** Match phone → contact; interpret STOP/OK/other; opt-out or mark response; optional templated reply; log.
- [ ] **AI/rules:** Personalisation (template + placeholders only); timing window; language selection; response classification (opt-out, ack, unknown).
- [ ] **Opt-out:** In every template or append; STOP handling; CHW opt-out; re-opt-in = new consent.
- [ ] **Risk controls:** Shared-phone-safe variants; no clinical content; caps and quiet hours; no send after opt-out.
- [ ] **Audit:** Consent, opt-out, sent, received, interpreted; retention and access policy.

This design keeps **consent** central, **templates** as the only outbound content, **AI** within safe bounds (personalisation, timing, language, response interpretation only), and **logging** for safety and accountability.
