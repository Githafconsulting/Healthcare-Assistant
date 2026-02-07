# Community Care Network Module

**Purpose:** Support CHWs in **choosing where to refer or send** patients by maintaining a local map of **traditional healers, pharmacies, clinics, and referral hospitals**, with clear rules on what each provider type is **appropriate** and **not appropriate** for. The system **suggests** care locations contextually, respects **cultural preferences**, and triggers **escalation when outcomes are poor**. The CHW (or facility) always makes the final referral decision.

**Principles:** The system does **not** diagnose. It does **not** direct patients to a provider without CHW confirmation. It **suggests** options based on clinical pathway outcome (e.g. "referral needed"), patient context, distance, availability, and cultural preference. All referrals are logged for safety and outcome follow-up.

---

## 1. Provider Types and Attributes

### 1.1 Provider type enum

```kotlin
enum class ProviderType {
    TRADITIONAL_HEALER,
    PHARMACY,
    CLINIC,              // health centre, dispensary, primary care
    REFERRAL_HOSPITAL    // hospital, district/regional
}
```

### 1.2 Core provider schema

For **each provider** the network stores:

```kotlin
data class CareProvider(
    val id: String,
    val type: ProviderType,
    val name: String,
    val nameLocal: String?,           // name in local language
    val location: GeoLocation?,
    val villageOrArea: String,
    val district: String?,
    val contactPhone: String?,
    val contactWhatsApp: String?,

    // --- Services offered ---
    val servicesOffered: List<ServiceCode>,  // see below
    val servicesDescription: String?,         // short free text for CHW

    // --- Referral appropriateness (condition codes from clinical pathways) ---
    val conditionsAppropriate: List<String>, // e.g. "FEVER_NO_DANGER", "DIARRHEA_PLAN_A", "REFILL_CHRONIC"
    val conditionsNotAppropriate: List<String>, // e.g. "DANGER_SIGN", "SEVERE_PNEUMONIA"

    // --- Distance and availability ---
    val distanceKm: Float?,                  // from CHW/village centre; or computed at query time
    val estimatedTravelMinutes: Int?,        // walking / typical transport
    val availability: AvailabilitySchedule?, // opening hours, days
    val is24_7: Boolean,                     // for hospitals
    val notes: String?                       // "Closed Wed PM", "Best to call first"
)

data class GeoLocation(val latitude: Double, val longitude: Double)

data class AvailabilitySchedule(
    val days: List<DayOfWeek>,
    val openTime: String?,   // "08:00"
    val closeTime: String?,  // "17:00"
    val byAppointmentOnly: Boolean
)
```

### 1.3 Service codes (examples)

Standardised list so conditions can be matched to services. Configurable per deployment.

| Code | Description | Typical provider types |
|------|-------------|------------------------|
| BASIC_MEDICINES | Paracetamol, ORS, simple analgesics | Pharmacy, Clinic |
| ANTIMALARIALS | ACT, RDT | Clinic, Pharmacy (if allowed) |
| ORAL_ANTIBIOTICS | First-line antibiotics | Clinic |
| INJECTABLES | Injections, IV capability | Clinic, Referral hospital |
| MATERNAL_ANC | Antenatal care | Clinic, Referral hospital |
| MATERNAL_DELIVERY | Delivery | Clinic, Referral hospital |
| PEDIATRIC | Under-5 care, IMCI | Clinic, Referral hospital |
| NUTRITION | MUAC, supplementary feeding | Clinic, Referral hospital |
| MENTAL_SUPPORT | Counselling, traditional support | Traditional healer, Clinic |
| CULTURAL_RITUALS | Blessings, rituals (non-clinical) | Traditional healer |
| REFILL_CHRONIC | Refill of known chronic meds | Pharmacy, Clinic |
| LAB_RDT | Malaria RDT, basic lab | Clinic, some Pharmacies |
| EMERGENCY | Emergency care, resuscitation | Referral hospital |

---

## 2. Conditions: Appropriate vs Not Appropriate by Provider Type

These are **guidance rules** for the module. They align with WHO IMCI and national referral pathways. The system uses them to **filter and rank** suggestions; it does not diagnose the patient.

### 2.1 Traditional healers

| | |
|---|--|
| **Services offered** | Cultural rituals, mental/social support, comfort care, mediation with family; **no** dispensing of prescription medicines or clinical procedures. |
| **Conditions APPROPRIATE for referral** | Psychosocial distress, family conflict, cultural/bereavement support, **after** biomedical treatment is in place (e.g. “continue ORS at home; family may also wish to see elder for blessing”). Not for acute clinical treatment. |
| **Conditions NOT appropriate** | Any acute danger sign; fever in child; diarrhea with dehydration; cough/difficult breathing; need for medicines (ORS, ACT, antibiotics); maternal danger signs; any condition requiring clinical assessment or prescription. |
| **Distance / availability** | Often village-based; distance short. Availability by custom (e.g. certain days, by visit). |

**Safety rule:** The system must **never** suggest a traditional healer as the **only** option when the clinical pathway says "refer urgently" or "danger sign". It may suggest "in addition to" facility care when culturally relevant and after facility referral is in plan.

### 2.2 Pharmacies

| | |
|---|--|
| **Services offered** | Sale of OTC and (where allowed) schedule medicines; refill of known prescriptions; basic advice. No diagnosis, no assessment of severity. |
| **Conditions APPROPRIATE** | Refill of chronic medication (known prescription); purchase of ORS/zinc/paracetamol when **already advised by CHW or facility** (e.g. "CHW said give ORS – go to pharmacy to buy"); simple OTC for minor symptoms when no danger signs. |
| **Conditions NOT appropriate** | First presentation of fever, cough, diarrhea, or any sick child without prior clinical assessment; danger signs; need for RDT, injectables, or prescription-only antibiotics; maternal/child emergency. |
| **Distance / availability** | Distance and opening hours stored; some may be part-time. |

**Safety rule:** Do **not** suggest pharmacy as sole destination when pathway says "refer to facility" or "danger sign". Pharmacy can be suggested for **medicines only** after a facility/CHW has already decided the treatment.

### 2.3 Clinics (health centre, dispensary, primary care)

| | |
|---|--|
| **Services offered** | IMCI, basic maternal/child care, RDT, ACT, ORS, zinc, first-line antibiotics, basic lab, dressings, some injectables; referral to hospital when needed. |
| **Conditions APPROPRIATE** | Fever (with RDT); diarrhea Plan A/B; cough/cold; uncomplicated pneumonia (per national protocol); mild/moderate malnutrition; ANC/PNC; minor wounds; follow-up after hospital discharge. |
| **Conditions NOT appropriate (refer to hospital)** | General danger signs; severe pneumonia; severe dehydration; severe malaria; severe acute malnutrition; maternal/newborn danger signs; need for IV, surgery, or inpatient care. |
| **Distance / availability** | Distance, travel time, opening hours; some 24/7 or on-call. |

### 2.4 Referral hospitals

| | |
|---|--|
| **Services offered** | Inpatient care, IV, surgery, emergency, maternity, paediatric admission, higher-level lab. |
| **Conditions APPROPRIATE** | All urgent referrals (danger signs, severe dehydration, severe pneumonia, severe malaria, SAM, maternal/newborn emergency); any condition clinic has referred. |
| **Conditions NOT appropriate** | None for clinical severity; may be "not appropriate" only for non-urgent, distance-sensitive cases (e.g. simple refill – prefer pharmacy/clinic). |
| **Distance / availability** | Often farther; 24/7 for emergency; may have specific hours for OPD. |

### 2.5 Data: conditions lists per provider

Each **CareProvider** has:

- **conditionsAppropriate:** list of condition/classification codes (e.g. from clinical decision support) for which this provider is a valid option.
- **conditionsNotAppropriate:** codes for which this provider must **not** be suggested (or must be deprioritised).

Examples of codes (aligned with CDS):  
`DANGER_SIGN`, `SEVERE_PNEUMONIA`, `SEVERE_DEHYDRATION`, `SEVERE_MALARIA`, `FEVER_NO_DANGER`, `DIARRHEA_PLAN_A`, `DIARRHEA_PLAN_B`, `COUGH_NO_PNEUMONIA`, `PNEUMONIA`, `MATERNAL_DANGER`, `REFILL_ONLY`, etc.

---

## 3. Contextual Suggestion Logic (AI / Rules)

The system **suggests** care locations; it does not assign. Inputs come from the **visit and clinical pathway outcome** (e.g. "referral needed", "give ORS at community level"), not from a diagnosis label.

### 3.1 Inputs

- **Referral context:** From decision support: referral suggested? urgency (emergency vs urgent vs routine)? condition/classification code(s) (e.g. `DANGER_SIGN`, `SEVERE_DEHYDRATION`).
- **Patient context:** Village/area, preferred language, **cultural preference** (e.g. "family accepts traditional healer in addition to facility"; "prefer clinic in village X").
- **CHW context:** CHW location or assigned area (for distance).
- **Provider data:** All CareProviders with services, conditions appropriate/not appropriate, distance, availability.

### 3.2 Rules (plain language)

1. **Filter by condition**
   - Exclude any provider whose **conditionsNotAppropriate** contains the current referral condition.
   - Include only providers whose **conditionsAppropriate** contains the condition (or "any" for referral hospital for urgent).

2. **Filter by urgency**
   - **Emergency/urgent:** Only suggest **clinic** or **referral hospital** (and optionally traditional healer "in addition" if cultural preference set; never as sole).
   - **Routine:** Include clinic, pharmacy (if condition matches), and traditional healer only when appropriate (e.g. psychosocial).

3. **Distance and availability**
   - Sort by **distance** or **estimatedTravelMinutes** (closer first).
   - Optionally filter out providers that are currently **closed** (if availability is known); or show "May be closed – call first".

4. **Cultural preferences**
   - If patient/family has **preference for traditional healer in addition to facility:** add a short note: "Family may also wish to visit [name] for support/blessing after going to facility."
   - If **preferred area/village** is set: boost providers in that area in ranking.
   - Never use cultural preference to **replace** facility referral when pathway requires it.

5. **Output**
   - Ordered list of **suggested providers** with: name, type, distance, availability summary, reason ("Appropriate for severe dehydration"), and optional cultural note.
   - CHW sees list and selects; CHW can override or add free-text referral instructions.

### 3.3 No diagnosis

The "condition" used for filtering is the **pathway output** (e.g. "refer – severe dehydration"), not a diagnostic label. The module only uses codes that the clinical decision support module already outputs when the CHW has gone through the pathway.

---

## 4. Escalation When Outcomes Are Poor

"Outcomes poor" means: referral was made but **patient did not improve** or **did not attend** follow-up, or **adverse outcome** was reported (e.g. death, deterioration). The module triggers **escalation** so the system or supervisor can surface these cases; it does **not** auto-refer or change the patient’s plan by itself.

### 4.1 Outcome signals (inputs)

- **Follow-up not done:** From treatment plan generator – follow-up due date passed, not completed, escalation level L2/L3.
- **Patient/caregiver reported:** CHW or facility marks "Patient did not go to referral" or "Went but got worse".
- **Explicit outcome:** When implemented: "Patient attended referral" / "Improved" / "Not improved" / "Died" (from CHW or facility).

### 4.2 Escalation triggers

| Signal | Escalation action |
|--------|-------------------|
| Referral made but follow-up overdue (L2/L3) | Already in treatment plan generator (CHW/supervisor alert). Link to referral: "Referral to [facility] – patient not seen at follow-up." |
| "Patient did not go to referral" | Flag referral record; add to CHW task list: "Follow up: encourage visit or document reason." Optional supervisor digest. |
| "Went but got worse" / "Not improved" | Flag referral and outcome; suggest "Consider re-referral or different facility." CHW/supervisor review. |
| Death or serious adverse outcome | Immediate flag; audit log; supervisor/facility notification; no automatic change to plan. |

### 4.3 Data for outcome and escalation

```kotlin
data class ReferralRecord(
    val id: String,
    val visitId: String,
    val patientId: String,
    val careProviderId: String,
    val referredAt: Long,
    val urgency: Urgency,
    val conditionCode: String?,
    val reasonText: String?,
    val chwId: String,

    val outcome: ReferralOutcome?,   // NOT_ATTENDED, ATTENDED_IMPROVED, ATTENDED_NOT_IMPROVED, DIED, UNKNOWN
    val outcomeRecordedAt: Long?,
    val outcomeNotes: String?,

    val escalatedAt: Long?,
    val escalationReason: String?   // "FOLLOW_UP_OVERDUE", "NOT_ATTENDED", "NOT_IMPROVED", "DEATH"
)

enum class ReferralOutcome { NOT_ATTENDED, ATTENDED_IMPROVED, ATTENDED_NOT_IMPROVED, DIED, UNKNOWN }
```

Escalation **does not** auto-send the patient elsewhere; it **alerts** CHW/supervisor to review and decide next steps.

---

## 5. Safety and Ethical Considerations

### 5.1 Safety

| Risk | Mitigation |
|------|------------|
| **Danger sign sent to pharmacy or traditional healer only** | Strict filter: when condition = danger sign or urgent referral, only clinic and referral hospital are suggested. Traditional healer only "in addition" with explicit note. |
| **Wrong condition code** | Condition codes come from the same clinical pathway module (no free-text diagnosis). CHW confirms referral reason before sending. |
| **Outdated provider list** | Regular review; mark providers as inactive; do not delete history. |
| **Distance wrong** | Show "Approximate"; encourage CHW to confirm with patient. |
| **Over-reliance on AI suggestion** | UI always shows "Suggested – you decide"; CHW must select provider and can add free text. |

### 5.2 Ethics

| Principle | Application |
|-----------|-------------|
| **No replacement of clinical judgment** | System suggests options; CHW or facility decides where to refer. No automatic referral. |
| **Respect for cultural preference** | Cultural preference (e.g. traditional healer in addition) is supported without replacing biomedical care when pathway requires facility referral. |
| **Transparency** | Reason for suggestion is shown ("Appropriate for severe dehydration"); conditions not appropriate are documented per provider type. |
| **Equity** | Distance and availability are used so that feasible options are ranked; avoid suggesting only far or always-closed facilities when alternatives exist. |
| **Accountability** | All referrals and outcomes (attended, improved, not improved, died) are logged; escalation goes to humans for review. |
| **No harm** | Conditions-not-appropriate lists and safety rules ensure that dangerous mismatches (e.g. severe malaria → pharmacy only) cannot be suggested. |

### 5.3 Traditional healers – specific ethics

- **Role is complementary,** not substitute for acute clinical care when pathway says refer.
- **Services** described honestly (support, rituals, counselling) – no implication that they treat severe dehydration or severe malaria.
- **Suggest** traditional healer only for appropriate situations (e.g. psychosocial, cultural support after facility care in plan); never as sole suggestion when referral to facility is indicated.
- **Local norms:** Configurable per deployment (some areas may not list traditional healers at all; others may list with clear "in addition to" wording).

### 5.4 Data and consent

- Provider list: no personal health data; only names, locations, services, and appropriateness rules.
- Referral records: contain patient/visit identifiers; store and transmit according to existing app privacy and security policy; consent for referral and follow-up as per program.

---

## 6. Data Schema Summary

### 6.1 Entities

- **CareProvider** – type, name, location, village, contact, **servicesOffered**, **conditionsAppropriate**, **conditionsNotAppropriate**, **distanceKm**, **estimatedTravelMinutes**, **availability**, notes.
- **ReferralRecord** – visit, patient, careProvider, urgency, conditionCode, reason, outcome, escalation.
- **Patient cultural preference** (optional) – e.g. "Include traditional healer in addition when relevant"; "Preferred area: X".

### 6.2 Suggestion output (no new entity; used in UI)

- List of **SuggestedProvider** (provider id, name, type, distance, availability summary, reason match, cultural note if any).
- CHW selects one (or none) and optionally adds free text; system creates **ReferralRecord** and links to treatment plan / follow-up.

---

## 7. Implementation Checklist

- [ ] **CareProvider** table and seed data (traditional healers, pharmacies, clinics, referral hospitals) with services and conditions appropriate/not appropriate.
- [ ] **ReferralRecord** table and link to visit/patient; outcome and escalation fields.
- [ ] Suggestion function: input (referral context, condition code, urgency, patient location, cultural preference) → filter and rank providers → return ordered list.
- [ ] UI: after referral suggestion from CDS, show "Where to refer?" with suggested list; CHW selects and confirms; create ReferralRecord.
- [ ] Outcome recording: CHW or facility can set outcome on ReferralRecord; trigger escalation when outcome = NOT_ATTENDED, NOT_IMPROVED, or DIED.
- [ ] Escalation: hook to existing treatment-plan escalation (follow-up overdue) and add referral-outcome escalation (not attended, not improved, death); supervisor digest.
- [ ] Config: condition codes aligned with CDS; safety rules (no traditional healer/pharmacy as sole for danger sign); cultural-preference options.
- [ ] Audit: log referral created, outcome updated, escalation triggered.

This design keeps the **community care network** as a **support** for CHWs, ensures **safety** (appropriate/not appropriate per provider type), **respects culture** without replacing necessary care, and **escalates** when outcomes are poor, with clear safety and ethics guardrails.
