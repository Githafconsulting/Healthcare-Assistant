# AI-Assisted Treatment Plan Generator

**Purpose:** Turn accepted clinical guidance (from the decision-support module) into **actionable steps**, **medication schedules**, **follow-up reminders**, and **patient-facing messages** adapted for low-literacy and mixed-language contexts. CHWs are alerted when needed without being overloaded.

**Principles:** The system does not diagnose. It converts **CHW-confirmed** treatments and follow-up into structured plans and reminders. All outbound messages are template-based; no free-form AI-generated clinical text to patients.

---

## 1. Treatment Plan Schema

### 1.1 Plan (root)

```kotlin
data class TreatmentPlan(
    val id: String,
    val visitId: String,
    val patientId: String,
    val createdAt: Long,
    val effectiveFrom: Long,           // when plan starts (usually visit date)
    val status: PlanStatus,            // ACTIVE, COMPLETED, CANCELLED, ESCALATED
    val languagePreference: String?,  // "en", "sw", etc. for patient messages
    val lowLiteracyMode: Boolean,      // true → shorter messages, more pictures/codes

    // Content (generated from accepted suggestions)
    val actionableSteps: List<ActionableStep>,
    val medicationSchedules: List<MedicationSchedule>,
    val followUp: FollowUpPlan?,
    val counselPoints: List<CounselPoint>,  // what to tell caregiver (for CHW use)

    // Audit
    val generatedFrom: List<String>,  // suggestion IDs that were accepted
    val chwId: String,
    val lastUpdatedAt: Long
)

enum class PlanStatus { ACTIVE, COMPLETED, CANCELLED, ESCALATED }
```

### 1.2 Actionable steps (what to do, in order)

Convert each accepted treatment/referral into a concrete step the CHW or caregiver can follow.

```kotlin
data class ActionableStep(
    val id: String,
    val order: Int,                    // 1, 2, 3...
    val type: StepType,                // GIVE_MEDICATION, REFERRAL, COUNSEL, FEEDBACK_QUESTION
    val title: String,                 // short, e.g. "Give ORS today"
    val titleForPatient: String?,      // low-literacy / local language version
    val description: String,           // fuller instructions for CHW
    val forCaregiver: Boolean,         // true if caregiver does this at home
    val dueDate: Long?,                // optional single due date
    val dueWindowStart: Long?,        // optional window
    val dueWindowEnd: Long?,
    val completedAt: Long?,
    val completedBy: String?,          // "chw" | "caregiver" | "system"
    val guidelineRef: String?,
    val linkedSuggestionId: String?
)

enum class StepType { GIVE_MEDICATION, REFERRAL, COUNSEL, FEEDBACK_QUESTION }
```

### 1.3 Medication schedule (from accepted treatment suggestions)

```kotlin
data class MedicationSchedule(
    val id: String,
    val medicationName: String,        // e.g. "ORS", "Zinc", "Paracetamol"
    val medicationCode: String,        // standardised, e.g. "ORS", "ZINC_10MG"
    val indication: String,            // e.g. "For diarrhea"
    val dosingInstructions: List<DoseInstruction>,
    val totalDurationDays: Int?,
    val startedAt: Long?,
    val completedAt: Long?,
    val forCaregiver: Boolean,         // true if given at home
    val guidelineRef: String?
)

data class DoseInstruction(
    val doseNumber: Int,               // 1, 2, 3... for "dose 1 of 3"
    val amount: String,                // "50-100ml", "half tablet", "10mg"
    val whenToGive: String,            // "After each loose stool", "Every 6 hours"
    val timeOfDay: List<String>?,     // ["morning", "evening"] or null if event-based
    val maxPerDay: Int?,               // e.g. 4 for paracetamol
    val plainLanguage: String         // for low literacy: "Give after each runny stool"
)
```

### 1.4 Follow-up plan

```kotlin
data class FollowUpPlan(
    val id: String,
    val visitId: String,
    val patientId: String,
    val followUpType: FollowUpType,    // DAYS_2, DAYS_5, DATE_SPECIFIC, REFERRAL_RETURN
    val dueDate: Long,
    val dueWindowDays: Int?,           // e.g. 2 = remind 2 days before and on day
    val reason: String,                // "Recheck fever", "Complete ORS course"
    val reasonForPatient: String?,    // short, low-literacy
    val reminderSentAt: Long?,
    val patientResponded: Boolean?,
    val actualVisitDate: Long?,
    val escalatedAt: Long?,
    val escalationReason: String?
)

enum class FollowUpType { DAYS_2, DAYS_5, DATE_SPECIFIC, REFERRAL_RETURN }
```

### 1.5 Counsel points (for CHW to say, not sent as SMS)

```kotlin
data class CounselPoint(
    val id: String,
    val topic: String,                 // "Feeding during diarrhea", "When to return"
    val shortText: String,             // 1–2 sentences for CHW
    val guidelineRef: String?
)
```

### 1.6 Inputs to the generator

- **Visit** (patient, symptoms, vitals, danger signs).
- **Accepted suggestions** (list of `Suggestion` with `accepted == true`), e.g. ORS, Zinc, Paracetamol, Referral.
- **Follow-up choice** (2 days / 5 days / none) from visit close.
- **Patient** (id, name, language preference, low-literacy flag if available).
- **Guideline/treatment catalog** (dosing by age/weight, instructions, plain-language strings).

Output: one `TreatmentPlan` with `actionableSteps`, `medicationSchedules`, `followUp`, `counselPoints` populated.

---

## 2. Reminder Logic

### 2.1 What gets reminded

| Entity | Reminder type | When |
|--------|----------------|------|
| **Caregiver / patient** | Medication reminder | Per medication schedule (e.g. "Give ORS after each stool today") — optional, configurable by program |
| **Caregiver / patient** | Follow-up reminder | X days before and on due date ("Come for check-up in 2 days" / "Today: check-up") |
| **CHW** | Follow-up due | Day before and on due date: "Follow-up due: [Patient], [date]" |
| **CHW** | Escalation | When patient has not responded after N reminders (see Escalation) |

### 2.2 Timing rules (configurable)

```text
Follow-up due date = visitDate + followUpDays (e.g. visit Mon → 2-day follow-up = Wed).

Patient reminder:
  - Option A: Send 1 day before and on due date.
  - Option B: Send on due date only.
  - Time: e.g. 08:00 local (configurable).

CHW reminder:
  - Send day before (e.g. 18:00): "Tomorrow: 3 follow-ups (Amina, Joseph, Fatima)."
  - Send on due date (e.g. 08:00): "Today: follow-up due for [list]."
  - Only include plans that are still ACTIVE and not yet marked completed.
```

### 2.3 Medication reminder (if enabled)

- For each `MedicationSchedule` with `forCaregiver == true` and a daily pattern (e.g. paracetamol every 6 hours), program can send:
  - Once per day: "Remember: give [medication] as shown. [plainLanguage]."
- Or no automatic medication SMS (CHW explains in person only); configurable.

### 2.4 Idempotence and channels

- Each reminder has a **type + planId + date** key. Send at most once per (type, plan, date).
- Store `reminderSentAt` (or a small `ReminderLog` table) so we don’t double-send if job runs twice.
- Channel (SMS vs WhatsApp) is a deployment setting; templates are shared, content adapted by channel (length, link usage).

---

## 3. SMS / WhatsApp Message Templates

All patient/caregiver messages are from **templates**. Placeholders are filled by the system; no free-form AI. Templates support **low-literacy** (short, simple words, numbers) and **multi-language** (one template per language).

### 3.1 Template schema

```kotlin
data class MessageTemplate(
    val id: String,
    val purpose: String,               // "follow_up_reminder", "medication_reminder", "escalation"
    val channel: Channel,              // SMS, WHATSAPP
    val languageCode: String,          // "en", "sw"
    val lowLiteracyVariant: Boolean,   // true = shorter, simpler
    val body: String,                  // "Follow-up: {patientName}. Come on {dueDate}. {reason}. Reply OK if coming."
    val placeholders: List<String>     // ["patientName", "dueDate", "reason"]
)

enum class Channel { SMS, WHATSAPP }
```

### 3.2 Placeholders (common set)

| Placeholder | Meaning | Example |
|-------------|---------|--------|
| `{patientName}` | Patient or child name | "Amina" |
| `{dueDate}` | Follow-up date (simple format) | "Wed 5 Feb" or "5/2" |
| `{reason}` | Short reason for follow-up | "Recheck fever" |
| `{medicationName}` | Medication | "ORS" |
| `{plainInstruction}` | One-line instruction | "Give after each runny stool" |
| `{chwName}` | CHW name (optional) | "Maria" |
| `{clinicOrVillage}` | Where to come | "Kijiji" |

### 3.3 Example templates (English)

**Follow-up reminder (SMS, standard)**  
`Afya: Follow-up for {patientName} on {dueDate}. Reason: {reason}. Reply OK if you will come.`

**Follow-up reminder (SMS, low literacy)**  
`Reminder: {patientName} – come {dueDate}. Reply OK.`

**Follow-up reminder (WhatsApp, standard)**  
`*Follow-up reminder*\nPatient: {patientName}\nDate: {dueDate}\nReason: {reason}\nPlease reply OK if you will come.`

**Medication reminder (SMS, low literacy)**  
`Remember: give {medicationName} to {patientName} today. {plainInstruction}.`

**Escalation to caregiver (SMS)**  
`We did not see you for follow-up for {patientName}. Please come to {clinicOrVillage} or contact your CHW {chwName}.`

### 3.4 Local language (placeholder)

- Same structure; `body` in Swahili (or other). Example (Swahili, low literacy):  
  `Ukumbusho: {patientName} – njoo {dueDate}. Jibu OK.`
- Placeholders stay the same; values are filled in the same way (names, dates); date format can be locale-specific.

### 3.5 Constraints

- **SMS:** Keep under 160 characters for single segment where possible; use abbreviations in low-literacy variant.
- **WhatsApp:** Can be slightly longer; avoid sensitive data in plain text; links only if program supports.
- **No diagnosis:** Templates say "follow-up", "recheck", "complete treatment" — never "you have X".

---

## 4. Escalation Rules (When Patients Do Not Respond)

### 4.1 Definition of "no response"

- **Option A:** Patient/caregiver did not reply to the reminder (e.g. "Reply OK").
- **Option B:** Follow-up due date passed and CHW has not marked the follow-up as done (visit completed or patient seen).
- **Option C:** Both: no reply **and** no visit marked.

Escalation logic uses **Option B** as the primary trigger (follow-up overdue and not completed); optional use of "Reply OK" to mark engagement.

### 4.2 Escalation levels (configurable)

| Level | Trigger | Action |
|-------|--------|--------|
| **L1** | Due date + 1 day; follow-up not completed | Notify CHW: "Follow-up overdue: [Patient]. Please visit or call." |
| **L2** | Due date + 2 days; still not completed | Second CHW alert; optionally send caregiver escalation SMS (e.g. "We did not see you…"). |
| **L3** | Due date + 3–5 days (configurable); still not completed | Mark plan as ESCALATED; CHW supervisor or facility gets a digest (if configured). No auto-diagnosis or auto-referral; human decides. |

### 4.3 Escalation actions (no overload — see section 5)

- **CHW:** Single in-app (or push) alert per plan that crosses a level; batched digest if many (e.g. "3 follow-ups overdue" with list).
- **Caregiver:** At L2 (or L3), send one escalation SMS from templates above.
- **Supervisor:** Optional daily digest of ESCALATED plans (count + list); no real-time ping per patient.

### 4.4 Data model for escalation

```kotlin
// On FollowUpPlan
val escalatedAt: Long?
val escalationReason: String?   // "L1_OVERDUE", "L2_OVERDUE", "L3_OVERDUE"
val escalationNotifiedAt: Long? // when CHW was last notified

// Optional: EscalationLog table
// planId, level, at, channel (in_app, sms, digest), recipient (chw, supervisor)
```

---

## 5. How CHWs Are Alerted Without Overload

### 5.1 Principles

- **Batch** where possible (one "Today you have 5 follow-ups" instead of 5 separate alerts).
- **Cap** daily reminder volume per CHW (e.g. at most one "follow-up due" summary per day; escalation digests once per day).
- **Prioritise:** Escalation (L2/L3) > follow-up due today > follow-up due tomorrow.
- **In-app first:** Prefer in-app list/badge over push; use push only for high priority or once-per-day digest.

### 5.2 Alert types and frequency

| Alert type | Frequency | Form | Content |
|------------|----------|------|--------|
| **Follow-up due tomorrow** | Once per day (e.g. 18:00) | One batched notification | "Tomorrow: N follow-ups. [List names or] Open app for list." |
| **Follow-up due today** | Once per day (e.g. 08:00) | One batched notification | "Today: N follow-ups. [List or] Open app." |
| **Overdue (L1)** | Per plan, once when it becomes L1 | In-app; optional push if CHW has &lt; 5 today | "Follow-up overdue: [Patient]. Please visit or call." |
| **Overdue (L2/L3)** | Per plan when it hits L2/L3; then no repeat for same level | In-app + optional 1 escalation SMS to caregiver | Same; add "Escalation" badge in app. |
| **Supervisor digest** | Once per day if any ESCALATED | Email or in-app list (no push) | "N plans escalated: [list]. Review when possible." |

### 5.3 In-app CHW experience

- **Home or "Tasks" screen:** Single list "Follow-ups" with sections, e.g.:
  - **Today** (due date = today)
  - **Overdue** (due date &lt; today, not completed)
  - **Tomorrow**
- Each row: Patient name, due date, reason, [Mark done] [Call] [Escalate to supervisor].
- **Badge count:** Total "Today + Overdue" (or Today + Overdue + Tomorrow); no separate badge per patient.
- **No pop-up per patient:** Only batched reminders (one per day for "today", one for "tomorrow") and one-time escalation when a plan first becomes L1/L2/L3.

### 5.4 Configurable caps (example)

- Max **push notifications** per CHW per day: e.g. 3 (today summary, tomorrow summary, one escalation batch).
- Max **escalation SMS** to same caregiver per plan: 1 (at L2 or L3).
- **Quiet hours:** No patient-facing SMS/WhatsApp outside e.g. 07:00–20:00 local.

---

## 6. Low-Literacy Adaptation

### 6.1 Plan presentation to CHW

- **Actionable steps:** Short titles; "Give ORS today" not "Administer oral rehydration solution".
- **Medication schedule:** Include `plainLanguage` per dose ("Give after each runny stool") for CHW to read aloud or show.

### 6.2 Patient/caregiver messages

- **Low-literacy template variant:** Shorter, fewer words, same placeholders.
- **Dates:** Use simple format (e.g. "Wed 5 Feb" or "5/2"); avoid long prose.
- **Numbers:** Use digits (2, 5) not words when possible.
- **No medical jargon** in templates; "medicine for fever" not "antipyretic".

### 6.3 Optional: visual aids

- Future: link to a **visual dose schedule** (e.g. sun/moon icons for morning/evening) generated per plan; stored as image or PDF; CHW can show on phone or print. Not required for v1.

---

## 7. End-to-End Flow (Summary)

1. **Visit closed** with accepted suggestions and follow-up choice (2/5 days or none).
2. **Generator** runs (on device or after sync): builds `TreatmentPlan` from accepted suggestions + follow-up; creates `FollowUpPlan`; computes due date.
3. **Reminders:** Per config, send patient reminder (SMS/WhatsApp) and CHW batched reminder (tomorrow / today).
4. **On due date:** CHW sees list in app; marks done or records "patient not reached".
5. **If not done:** Escalation rules run (L1 → CHW alert; L2 → CHW + optional caregiver SMS; L3 → mark ESCALATED, optional supervisor digest).
6. **Templates** used for all outbound messages; placeholders filled from plan + patient; low-literacy and language variants selected by preference.

---

## 8. Implementation Checklist

- [ ] Persist `TreatmentPlan`, `ActionableStep`, `MedicationSchedule`, `FollowUpPlan`, `CounselPoint` (local DB).
- [ ] Generator function: visit + accepted suggestions + follow-up choice → one `TreatmentPlan` with steps and schedules.
- [ ] Reminder job (cron or background): daily, compute "due today", "due tomorrow", "overdue"; send batched CHW alerts; send patient SMS/WhatsApp from templates.
- [ ] Escalation job: daily, find plans past due; set L1/L2/L3; trigger CHW alert and optional caregiver escalation SMS (once per plan per level).
- [ ] Template store (local or sync): key by purpose, channel, language, lowLiteracy; render with placeholders.
- [ ] CHW in-app: Follow-ups list (Today / Overdue / Tomorrow), mark done, badge count, no per-patient push.
- [ ] Config: reminder times, escalation day thresholds, caps (push per day, SMS per plan), quiet hours.

This design keeps the system **guidance-based** (no diagnosis), **template-based** for safety, and **CHW-friendly** (batched alerts, clear escalation, low-literacy support).
