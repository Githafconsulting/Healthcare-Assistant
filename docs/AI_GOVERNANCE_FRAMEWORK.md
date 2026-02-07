# AI Governance Framework

**Purpose:** Define how AI is governed across the Afya Assistant application so that it remains **safe**, **accountable**, and **aligned** with Ministry of Health (MoH) expectations, NGO ethics reviews, and donor requirements. This document sets **AI boundaries**, **human-in-the-loop** rules, **audit** and **model governance**, **bias and harm monitoring**, and **incident response**, and explains how the design meets common regulatory and funder expectations.

**Scope:** All AI-assisted features: voice-to-text, symptom extraction, clinical decision support (suggestions), voice-to-structured-notes, treatment plan generation, community care suggestions, and patient communication (personalisation, timing, response interpretation).

---

## 1. AI Boundaries and Disclaimers

### 1.1 What the system does and does not do

| AI does | AI does not |
|--------|--------------|
| Converts voice to text (ASR) | Diagnose any condition |
| Extracts symptoms/keywords from transcript | Prescribe or dispense without CHW |
| Suggests treatments and referrals from rules (pathway-based) | Send clinical advice to patients in free text |
| Fills structured fields with confidence | Override or hide CHW decisions |
| Suggests care locations and message timing | Make referral or treatment decisions |
| Interprets simple replies (OK, STOP) | Interpret clinical meaning from patient messages |

These boundaries are **enforced by design**: no code path allows diagnosis, auto-prescription, or unconfirmed action (see §2).

### 1.2 Disclaimers (where shown)

- **In-app (CHW-facing):**  
  On first use or on the suggestions screen:  
  *"This tool suggests actions based on guidelines. You are responsible for the final decision. It does not diagnose."*

- **In training / onboarding:**  
  *"Afya Assistant supports your work. It does not replace your judgment. Always confirm or correct suggestions before acting."*

- **In technical and donor documentation:**  
  *"The system provides decision support only. No diagnosis or automated clinical action. All patient-facing content is template-based; no AI-generated clinical text to patients."*

### 1.3 Boundary violations (prevented by design)

- **No diagnosis:** Clinical logic is pathway/rule-based; outputs are "suggest referral", "consider ORS", not "patient has malaria".
- **No unconfirmed action:** Treatments, referrals, and messages require CHW accept/skip or explicit consent; no auto-send of clinical advice.
- **No free-form AI to patients:** Patient communication uses templates only; AI selects template and fills placeholders ([PATIENT_COMMUNICATION_SYSTEM](PATIENT_COMMUNICATION_SYSTEM.md)).
- **No override of opt-out:** Once a contact opts out, no further messages; checks before every send ([PATIENT_COMMUNICATION_SYSTEM](PATIENT_COMMUNICATION_SYSTEM.md)).

---

## 2. Human-in-the-Loop Enforcement

### 2.1 Points where a human must be in the loop

| Step | What AI does | What human must do |
|------|--------------|--------------------|
| **Suggestions (CDS)** | Produce list of suggestions with reason and guideline | CHW must **Accept** or **Skip** (with reason) each; visit cannot be completed with critical referral unacknowledged ([CLINICAL_DECISION_SUPPORT](CLINICAL_DECISION_SUPPORT.md)). |
| **Structured notes** | Extract chief complaint, symptoms, red flags, etc. | CHW **reviews and corrects** before proceeding; corrections stored ([VOICE_TO_STRUCTURED_NOTES](VOICE_TO_STRUCTURED_NOTES.md)). |
| **Treatment plan** | Generate steps and reminders from accepted suggestions | Plan is created only from **CHW-accepted** suggestions; CHW confirms summary before "Complete visit". |
| **Referral destination** | Suggest care locations by condition and distance | CHW **selects** provider and confirms referral; system does not send referral without selection ([COMMUNITY_CARE_NETWORK](COMMUNITY_CARE_NETWORK.md)). |
| **Patient messages** | Choose template, timing, language; interpret STOP/OK | **Consent** obtained by CHW (or program) before any send; no clinical content beyond templates. |
| **Escalation** | Flag overdue follow-up, non-attendance, poor outcome | **Humans** (CHW, supervisor) decide next action; system only alerts ([TREATMENT_PLAN_GENERATOR](TREATMENT_PLAN_GENERATOR.md), [COMMUNITY_CARE_NETWORK](COMMUNITY_CARE_NETWORK.md)). |

### 2.2 Technical enforcement

- **Suggestions:** No API or flow that "applies" a suggestion without a recorded CHW response (accept/skip). UI does not allow "Complete visit" with an unacknowledged critical referral unless policy allows with explicit "Skipped" + reason.
- **Structured notes:** Next step (e.g. suggestions) uses the **edited** values from the review screen, not raw AI output only.
- **Referrals:** ReferralRecord is created only when CHW selects a provider and confirms (no auto-refer).
- **Messages:** Send path checks consent and opt-out; no send without consent and no send after opt-out.
- **Audit:** Every accept/skip, correction, referral creation, and message send is logged so that "human in the loop" can be verified (see §3).

### 2.3 Responsibility statement

- **CHW:** Responsible for clinical and referral decisions; the system assists only.
- **Supervisor / program:** Responsible for training, oversight, and responding to escalations and incidents.
- **System owner:** Responsible for safe design, audit, model governance, and incident response as described in this framework.

---

## 3. Audit Logs

### 3.1 What is logged

Audit events are **append-only** and synced for central review. No deletion of audit records except per retention policy (e.g. after 7 years if required by MoH).

| Category | Events | Key data (no PHI in log body where avoidable) |
|----------|--------|------------------------------------------------|
| **Clinical** | suggestion_shown, suggestion_accepted, suggestion_skipped, structured_notes_corrected, visit_completed | suggestionId, visitId, chwId, response (accept/skip), skipReason, timestamp |
| **Referral** | referral_created, referral_outcome_recorded, referral_escalated | referralId, providerId, outcome, escalationReason, chwId, timestamp |
| **Treatment plan** | plan_created, follow_up_marked_done, follow_up_escalated | planId, visitId, chwId, timestamp |
| **Communication** | consent_recorded, opt_out, message_sent, message_received, response_interpreted | contactId, channel, templateId, purpose, interpretation (e.g. OPT_OUT), timestamp |
| **Voice** | voice_capture_started, voice_capture_completed, transcript_corrected, structured_notes_generated, audio_stored, audio_deleted | voiceCaptureId, notesId, warnings, timestamp |
| **Access / security** | login, logout, sync_completed, error_captured | userId, deviceId, errorCode, timestamp |

### 3.2 Audit log schema (conceptual)

```kotlin
data class AuditEntry(
    val id: String,
    val at: Long,
    val eventType: String,
    val actor: String?,          // chwId, "system"
    val entityType: String?,     // "visit", "suggestion", "referral", "contact"
    val entityId: String?,
    val payload: String,         // JSON; minimal, no full PHI in plain text where possible
    val deviceId: String?,
    val synced: Boolean
)
```

### 3.3 Retention and access

- **Retention:** Minimum 2 years for clinical and referral events; align with MoH and donor contracts (e.g. 5–7 years for research or M&E).
- **Access:** Role-based: CHWs see only their own actions in app; supervisors see team; auditors see full logs. Export only for authorised compliance or incident review.
- **Integrity:** Logs are append-only; checksum or signing if required by donor/MoH.

---

## 4. Model Update Governance

### 4.1 Types of "models" in this application

- **Rule sets:** Clinical pathways, danger signs, conditions appropriate/not appropriate, referral triggers (no trainable ML).
- **Keyword/symbol lists:** Symptoms, severity, duration, language (local + English) for extraction and templates.
- **ASR model:** Offline speech-to-text (e.g. Vosk); updated only by replacing the model file.
- **Config:** Thresholds, reminder times, escalation days, template IDs; loaded from config or DB.

### 4.2 Update process

| Asset | Who can change | Process | Rollback |
|-------|----------------|---------|----------|
| **Clinical rules / pathways** | Technical lead + clinical advisor (or MoH-designated) | Versioned change; test against known cases; release notes; deploy with version tag | Revert to previous rule version; visits already recorded keep snapshot of "guideline at time of visit" |
| **Keyword / synonym lists** | Same | Same as rules; regression test extraction on sample transcripts | Revert list version |
| **ASR model** | Technical team | Replace model file; test WER on sample set; deploy; log model version in app | Revert to previous model version |
| **Config (timing, escalation)** | Program/admin | Change in config; no code release; log config version | Revert config |
| **Templates (SMS/WhatsApp)** | Content owner + review | Edit template; approve for language and safety; deploy; template version in log | Revert template version |

### 4.3 Versioning and traceability

- Every **suggestion** and **visit** can record the **guideline/rule version** (or config version) at time of use so that later review or research knows which logic was applied.
- **ASR:** App or sync reports model version (e.g. Vosk model name/date) for traceability.
- **Templates:** Template id and version (or hash) stored with every message_sent so that content can be reproduced for audits.

### 4.4 No silent updates

- Rule/config/model updates go through the process above; no automatic pull of new clinical rules or models without a release step and, where required, MoH or ethics notification (see §7).

---

## 5. Bias and Harm Monitoring

### 5.1 What we monitor

- **Clinical:** Over- or under-suggestion of referral/treatment by subgroup (e.g. by age band, sex, village) if data available; outlier CHWs (e.g. very high skip rate for critical suggestions) for supervision, not punishment.
- **Communication:** Opt-out rates by language or area; delivery failure rates; no clinical outcome inferred from messages (we don’t diagnose from replies).
- **Referrals:** Uptake and outcome by provider type and area; escalation rates.
- **Voice/extraction:** Extraction confidence and correction rates by language or segment length; no differential by demographic in the product (we don’t store demographic for bias analysis unless required by program with consent).

### 5.2 How we monitor

- **Aggregate metrics:** Counts and rates (suggestions accepted/skipped, referrals by type, follow-up completed, opt-outs, delivery success) per time window, optionally per area or CHW; dashboards for program/supervisor.
- **Alerts:** Sudden change in skip rate for critical suggestions; spike in opt-outs or delivery failures; spike in escalations.
- **Review:** Periodic (e.g. quarterly) review of metrics and any flagged CHWs or areas; no automated penalty; findings feed into training and product improvements.

### 5.3 Bias mitigation (design)

- **Pathways:** Same rules for all patients meeting the same clinical inputs (age, symptoms, vitals); no input based on ethnicity or non-clinical attributes.
- **Language:** Support multiple languages and low-literacy variants so that no group is disadvantaged by language-only content.
- **Transparency:** Suggestions show reason and guideline so that CHWs can spot misapplication; corrections and overrides are logged.

### 5.4 Harm monitoring

- **Incidents:** Any reported harm (e.g. missed referral, wrong message, patient complaint) is logged and handled per incident response (§6).
- **Outcomes:** Referral outcome (attended, improved, not improved, died) and follow-up completion are recorded so that patterns (e.g. high non-attendance for one facility) can be reviewed by humans.

---

## 6. Incident Response Procedures

### 6.1 Definition of incident

- **Clinical:** Reported missed danger sign, wrong suggestion, or patient harm linked to use of the app.
- **Technical:** Data breach, prolonged outage, corruption of visit/referral data.
- **Communication:** Message sent to wrong person, message after opt-out, or complaint about content.
- **Governance:** Unauthorised change to rules/model, or audit log tampering.

### 6.2 Response steps

| Phase | Actions |
|-------|--------|
| **1. Detect & log** | Report received (CHW, patient, supervisor); create incident record with date, reporter, type, brief description; assign owner. |
| **2. Triage** | Severity: critical (potential harm/death, breach), high (wrong message, wrong suggestion acted on), medium (e.g. confusion, no harm), low. Critical/high: escalate to incident lead and, if required, MoH/donor contact. |
| **3. Contain** | Stop further harm: e.g. disable affected feature, revert config/model, or pause messages for a contact. Preserve evidence (logs, screenshots, DB snapshot). |
| **4. Investigate** | Gather audit trail (who, what, when); reproduce if possible; identify root cause (rule bug, misconfiguration, human error, external). Document findings. |
| **5. Remedy** | Fix cause (patch rule, revert model, correct consent record); notify affected people if required (e.g. wrong message recipient); offer support per program policy. |
| **6. Communicate** | Internal: incident report to leadership and, as needed, MoH/donor. External: only if required by law or contract; no speculative clinical claims. |
| **7. Learn** | Update runbooks; add checks or guardrails; train CHWs if human error; revise governance if process failed. |

### 6.3 Roles

- **Incident owner:** Program or technical lead; drives response.
- **Clinical advisor:** Input on clinical incidents and communications.
- **MoH / donor liaison:** Informed per agreement; may require formal notification within N days for critical incidents.

### 6.4 Documentation

- **Incident register:** Id, date, type, severity, summary, root cause, remedy, closed date.
- **Retention:** Align with MoH and donor; typically 5+ years for critical/high.

---

## 7. Alignment with External Expectations

### 7.1 Ministry of Health (MoH) expectations

Typical MoH concerns: **safety**, **alignment with national guidelines**, **accountability**, and **data control**.

| Expectation | How this framework addresses it |
|-------------|----------------------------------|
| **No diagnosis by software** | AI boundaries (§1) and design prevent diagnosis; only pathway-based suggestions; disclaimers in app and docs. |
| **Guidelines alignment** | Clinical logic follows WHO IMCI and national placeholders ([CLINICAL_DECISION_SUPPORT](CLINICAL_DECISION_SUPPORT.md)); rule updates involve clinical input and versioning (§4). |
| **Human responsibility** | Human-in-the-loop (§2) and audit (§3) ensure every clinical action is traceable to a CHW decision. |
| **Audit and oversight** | Audit logs (§3) and retention; MoH or designated authority can be granted read-only access to audit data under agreement. |
| **Incident reporting** | Incident response (§6) includes triage, containment, and notification; process can require MoH notification within agreed timeframe for serious incidents. |
| **Data locality and security** | Architecture is offline-first with sync to controlled backend; PHI handling and retention as per national policy; governance does not assume data leaves country without agreement. |

**Recommendation:** Formalise a short **MoH alignment document** (1–2 pages) that states: no diagnosis, guideline-based support, human-in-the-loop, audit and incident process, and data handling; sign-off or acknowledgment from MoH where required.

---

### 7.2 NGO ethics reviews

Ethics committees typically look for: **beneficence and non-maleficence**, **consent**, **transparency**, **equity**, and **accountability**.

| Expectation | How this framework addresses it |
|-------------|----------------------------------|
| **Beneficence / non-maleficence** | AI boundaries and human-in-the-loop reduce risk of harm; no diagnosis; escalation and incident response for when harm occurs. |
| **Consent** | Patient communication requires opt-in; opt-out in every message; consent and opt-out logged ([PATIENT_COMMUNICATION_SYSTEM](PATIENT_COMMUNICATION_SYSTEM.md)). Visit/recording consent as per program (e.g. verbal consent for voice). |
| **Transparency** | Disclaimers and training state that the tool assists and does not diagnose; suggestions show reason and guideline; corrections and overrides are logged. |
| **Equity** | Bias and harm monitoring (§5); same clinical rules for same inputs; multi-language and low-literacy support. |
| **Accountability** | Audit logs and retention; incident response; clear roles (CHW, supervisor, system owner). |
| **Research / secondary use** | If audit or outcome data are used for research, separate ethics approval and consent (or waiver) as required; governance doc can state that such use is out of scope of “routine” AI governance unless agreed. |

**Recommendation:** Submit this **AI Governance Framework** (or a short ethics-facing summary) as part of IRB/ethics applications; highlight boundaries, human-in-the-loop, consent for messaging, audit, and incident procedures.

---

### 7.3 Donor requirements

Donors often require: **safeguarding**, **transparency**, **no unintended harm**, **value for money**, and **reporting**.

| Expectation | How this framework addresses it |
|-------------|----------------------------------|
| **Safeguarding** | Incident response (§6) covers harm and breach; escalation to humans; no automated clinical action. |
| **Transparency** | Documented boundaries and disclaimers; audit trail; versioning of rules and models. |
| **Risk management** | Bias and harm monitoring (§5); model update governance (§4); no silent changes. |
| **Reporting** | Audit and metrics can feed donor reports (aggregate only); incident register can be summarised in periodic reports. |
| **Compliance** | Governance doc is the single place for AI-related commitments; can be attached to contract or MOU. |

**Recommendation:** In donor proposals or contracts, reference this **AI Governance Framework** and confirm: no diagnosis, human-in-the-loop, audit and incident process, and alignment with MoH and ethics. Offer a short **donor summary** (1 page) if needed.

---

## 8. Summary Table

| Area | Commitment |
|------|------------|
| **AI boundaries** | No diagnosis; no unconfirmed action; no free-form AI to patients; disclaimers in app and docs. |
| **Human-in-the-loop** | CHW must accept/skip suggestions, correct notes, select referral, consent for messages; enforced in code and audit. |
| **Audit** | Append-only logs for clinical, referral, communication, voice, access; retention per policy; role-based access. |
| **Model updates** | Versioned rules/lists/models/config; change process with clinical input; traceability for visits and messages. |
| **Bias and harm** | Monitoring of aggregates and outliers; alerts; periodic review; no automated penalty; design for equity. |
| **Incidents** | Detect, triage, contain, investigate, remedy, communicate, learn; incident register; MoH/donor notification as agreed. |
| **MoH** | No diagnosis; guideline alignment; human responsibility; audit and incident process; data and access under agreement. |
| **Ethics** | Consent, transparency, equity, accountability; framework suitable for IRB submission. |
| **Donors** | Safeguarding, transparency, risk management, reporting; framework as contractual reference. |

This framework aligns the application’s existing design (no diagnosis, CHW confirmation, audit, consent, templates, escalation) with a single governance document that can be shared with MoH, ethics committees, and donors.
