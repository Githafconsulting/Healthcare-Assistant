# Clinical Decision Support Module — Tropical Diseases

**Principle:** This system does **not** diagnose. It only guides the CHW along recognised clinical pathways. All actions are recommendations; the CHW must confirm or override every step.

**Sources:** WHO Integrated Management of Childhood Illness (IMCI), WHO malaria guidelines, WHO maternal health (ANC/PNC), plus **national guideline placeholders** for local adaptation.

---

## 1. Decision Pathway Structure

Pathways are **trees**, not single flows. Each pathway has:

- **Entry:** Who the pathway applies to (e.g. child 2 months–5 years, pregnant woman, postpartum woman).
- **Nodes:** Steps that ask for input or apply rules (danger signs → referral; symptoms → classification → action).
- **Branches:** Based on inputs (e.g. “fever + no danger signs” → malaria vs other fever; “diarrhea + dehydration” → Plan A/B/C).
- **Leaves:** Recommended actions (counsel only, give treatment at community level, refer).

### 1.1 Pathway Overview

| Pathway | Entry criteria | Main branches | Output types |
|--------|----------------|---------------|--------------|
| **IMCI General (child)** | Child 2 months–5 years, sick | Danger signs → Refer; then symptom-specific (cough/fever/diarrhea/etc.) | Referral, treatment, counsel |
| **Malaria** | Fever (or history of fever) in endemic area | Danger signs → Refer; then RDT+/RDT−/no test → treat or refer or other fever | Referral, ACT, paracetamol, counsel |
| **Diarrheal disease** | Diarrhea (loose/watery stools) | Dehydration (none/some/severe) → Plan A / Plan B / Plan C | ORS, zinc, referral |
| **Respiratory (ARI/pneumonia)** | Cough or difficult breathing | Danger signs → Refer; fast breathing / chest indrawing → classify | Referral, antibiotic (where allowed), counsel |
| **Maternal — Antenatal (ANC)** | Pregnant woman, any gestation | Risk factors, danger signs, gestational age | Referral, counsel, schedule next visit |
| **Maternal — Postnatal (PNC)** | Woman within 42 days after delivery | Maternal danger signs; newborn danger signs; breastfeeding | Referral, counsel, schedule |

### 1.2 Pathway Flow (Logical Order)

```
For every sick child (IMCI):
  1. General danger signs? → YES → REFER (urgent); stop.
  2. Classify COUGH/DIFficult BREATHING → severe → REFER; pneumonia → [per national protocol]; no pneumonia → counsel.
  3. Classify DIARRHOEA → severe dehydration / some + danger → REFER; some dehydration → Plan B; no dehydration → Plan A (+ zinc).
  4. Classify FEVER → severe (e.g. stiff neck, etc.) → REFER; malaria (RDT+ or presumptive per national) → ACT + paracetamol; other fever → paracetamol + counsel.
  5. Check MALNUTRITION (MUAC) → severe → REFER; moderate → counsel + follow-up.
  6. Check other local priorities (e.g. eye, ear) per national placeholder.
```

Maternal pathways run in parallel to child IMCI: separate entry (pregnant/postpartum) and their own danger signs and actions.

### 1.3 National Guideline Placeholders

- **`NATIONAL_MALARIA`** — RDT use (routine vs stock-out), age/weight bands for ACT, when to refer.
- **`NATIONAL_ANC`** — Number of ANC contacts, danger signs list, referral facility levels.
- **`NATIONAL_PNC`** — Postnatal contact schedule, maternal and newborn danger signs.
- **`NATIONAL_ARI`** — Whether CHW can give first dose antibiotic; antibiotic choice; referral criteria.
- **`NATIONAL_DIARRHEA`** — Plan A/B/C thresholds; zinc regimen; referral list.

Implementation: each placeholder is a **reference string** and (where needed) a **small set of parameters** (e.g. thresholds) loaded from config; no diagnosis, only “if inputs match pathway step then suggest action.”

---

## 2. Input Data Required

### 2.1 Per-Visit Context (Always)

| Input | Type | Required for | Notes |
|-------|------|----------------|-------|
| Patient type | Enum | All | `CHILD_2M_5Y`, `CHILD_UNDER_2M`, `PREGNANT`, `POSTPARTUM_MOTHER`, `NEWBORN`, `OTHER` |
| Age | Months or weeks | IMCI, malaria, diarrhea, ARI, maternal | DOB + visit date |
| Sex | M/F | Some pathways | e.g. maternal = F |
| Pregnancy / postpartum | Gestation or days since delivery | ANC, PNC | If applicable |

### 2.2 Symptoms and Signs (By Pathway)

| Input | Type | Used in | Example values |
|-------|------|---------|-----------------|
| General danger signs | Multi-select (confirmed by CHW) | All child pathways | Unable to drink, vomits everything, convulsions, lethargic/unconscious |
| Cough / difficult breathing | Present + duration | ARI, IMCI | Yes/No; “3 days” |
| Fast breathing | Count (breaths/min) or “yes/no” | ARI | Age-based thresholds (IMCI) |
| Chest indrawing | Yes/No | ARI | Severe pneumonia |
| Diarrhea | Present + duration + blood? | Diarrhea | Yes/No; “2 days”; blood in stool |
| Dehydration signs | Per IMCI | Diarrhea | Restless/irritable; sunken eyes; skin pinch; drink (eagerly/drinks poorly/unable) |
| Fever | Present + duration; temp if measured | Malaria, IMCI fever | Yes/No; “2 days”; 38.5°C |
| RDT result | If done | Malaria | Positive, Negative, Not done |
| Vomiting | Present | General, malaria | Yes/No |
| MUAC | mm | Malnutrition (IMCI) | e.g. 115 |
| Weight | kg | Dosing, malnutrition | Optional |
| Maternal danger signs | Multi-select | ANC, PNC | Severe headache, blurred vision, bleeding, fits, etc. |
| Newborn danger signs | Multi-select | PNC | Not feeding, convulsions, fast breathing, etc. |

### 2.3 Data Model (Minimal, Aligned to Pathways)

- **Patient:** id, type (or derived), DOB, sex, pregnancy status / EDD / delivery date as needed.
- **Visit:** id, patientId, date; **symptoms** (name, present, duration, severity); **vitals** (temperature, respiratory rate, MUAC, weight); **danger signs** (list of confirmed); **assessments** (e.g. “dehydration: some”); **RDT result** if applicable.
- **Structured notes** (from voice): chief complaint, symptoms with duration/severity, red flags, free text — map into the above inputs for pathway logic.

---

## 3. AI Reasoning Logic (Plain Language)

The “AI” here is **rule-based logic** only. No disease labels are asserted; only pathway steps and recommended actions.

### 3.1 General Principle

- **If** [inputs match a pathway rule] **then** [suggest one or more actions with reason and guideline].
- Wording is always: “Consider …”, “Classify as …”, “Refer because …”, “Give … per guideline.” Never “Patient has malaria” or “Diagnosis: pneumonia.”

### 3.2 Malaria Pathway (Plain Language)

1. **Danger signs present (any)**  
   → Suggest: “Urgent referral. Reason: General danger sign(s). Guideline: WHO IMCI / [NATIONAL_MALARIA].”

2. **Fever (or history of fever) + no danger signs**  
   - If **RDT positive**  
     → Suggest: “Consider treating for malaria. Give ACT per weight/age. Guideline: WHO malaria / [NATIONAL_MALARIA].”  
     → Suggest paracetamol if fever present.
   - If **RDT negative** (and test done)  
     → Suggest: “Malaria unlikely. Consider other causes of fever. Guideline: [NATIONAL_MALARIA].”  
     → Paracetamol for fever if appropriate.
   - If **RDT not done**  
     → Suggest: “Consider doing RDT if available. If not, follow national policy (e.g. treat as malaria or refer). Guideline: [NATIONAL_MALARIA].”

3. **Severe malaria criteria (e.g. unable to drink, repeated vomiting, convulsions, etc.)**  
   → Suggest: “Urgent referral. Reason: Severe malaria criteria. Guideline: WHO malaria.”

### 3.3 Diarrheal Disease Pathway (Plain Language)

1. **Danger signs present**  
   → Refer (as in general IMCI).

2. **Diarrhea present, no danger signs**  
   - **Severe dehydration** (e.g. two or more: lethargic, sunken eyes, skin pinch >2 s, drinks poorly or unable)  
     → Suggest: “Urgent referral. Reason: Severe dehydration. Guideline: WHO IMCI Plan C / [NATIONAL_DIARRHEA].”
   - **Some dehydration** (e.g. restless, sunken eyes, skin pinch <2 s, drinks eagerly)  
     → Suggest: “Give ORS Plan B (amount per age/weight). Give zinc. Guideline: WHO IMCI Plan B.”
   - **No dehydration**  
     → Suggest: “Counsel on ORS Plan A (more fluids, continue feeding). Give zinc. Guideline: WHO IMCI Plan A.”

3. **Blood in stool**  
   → Suggest: “Refer for assessment (dysentery). Guideline: WHO IMCI / [NATIONAL_DIARRHEA].”

### 3.4 Respiratory (ARI / Pneumonia) Pathway (Plain Language)

1. **Danger signs or chest indrawing**  
   → Suggest: “Urgent referral. Reason: Severe pneumonia or very severe disease. Guideline: WHO IMCI / [NATIONAL_ARI].”

2. **No danger signs, no chest indrawing**  
   - **Fast breathing** (age-based cutoff, e.g. ≥50/min 2–12 months, ≥40/min 12–59 months)  
     → Suggest: “Classify as pneumonia. [If national protocol allows: Give first dose antibiotic and refer.] Otherwise: Refer for antibiotic. Guideline: WHO IMCI / [NATIONAL_ARI].”
   - **No fast breathing**  
     → Suggest: “No pneumonia. Counsel on cough/cold, when to return. Guideline: WHO IMCI.”

### 3.5 Maternal — Antenatal (Plain Language)

1. **Any maternal danger sign** (e.g. severe headache, blurred vision, fits, bleeding, fever, severe abdominal pain)  
   → Suggest: “Urgent referral. Reason: [Danger sign]. Guideline: WHO ANC / [NATIONAL_ANC].”

2. **No danger signs**  
   → Suggest: “Continue ANC. Counsel on danger signs, nutrition, rest. Schedule next contact per [NATIONAL_ANC].”

3. **Risk factors** (e.g. first pregnancy, previous stillbirth, short stature)  
   → Suggest: “Consider referral or extra visits per [NATIONAL_ANC].” (No diagnosis; only pathway-based guidance.)

### 3.6 Maternal — Postnatal (Plain Language)

1. **Maternal or newborn danger signs**  
   → Suggest: “Urgent referral. Reason: [Danger sign]. Guideline: WHO PNC / [NATIONAL_PNC].”

2. **No danger signs**  
   → Suggest: “Counsel on breastfeeding, hygiene, family planning, when to return. Schedule next PNC per [NATIONAL_PNC].”

---

## 4. Risk Stratification Levels

Used only to **order and prioritise** suggestions and to show urgency. Not diagnostic categories.

| Level | Name | Meaning | Example use |
|-------|------|--------|-------------|
| **1** | **Critical** | Pathway says refer urgently; danger sign or severe classification | Red; show first; “Refer immediately” |
| **2** | **High** | Needs facility care or close follow-up; referral or give first dose + refer | Orange; after critical |
| **3** | **Medium** | Treatment at community level (e.g. ORS, ACT, paracetamol) | Yellow; routine treatment |
| **4** | **Low** | Counsel only, or schedule next visit | Green; last |

Rules map to levels, e.g.:

- General danger sign → **Critical**  
- Severe dehydration, severe pneumonia, severe malaria → **Critical**  
- Some dehydration, pneumonia (fast breathing), malaria RDT+ → **High** or **Medium** depending on action (refer vs treat)  
- No dehydration Plan A, no pneumonia → **Low**

---

## 5. Referral Triggers

Each trigger is a **condition on inputs** that leads to a **referral suggestion** with reason and urgency. No diagnosis is stated; only “refer because [condition].”

### 5.1 Child (IMCI)

| Trigger | Condition | Urgency | Guideline ref |
|---------|-----------|--------|----------------|
| General danger sign | Any of: unable to drink/breastfeed, vomits everything, convulsions, lethargic/unconscious | Emergency | WHO IMCI |
| Severe pneumonia | Chest indrawing or danger signs with cough/difficult breathing | Emergency | WHO IMCI / NATIONAL_ARI |
| Severe dehydration | Two or more: lethargic, sunken eyes, skin pinch >2 s, drinks poorly/unable | Emergency | WHO IMCI Plan C |
| Severe malaria | Criteria per WHO/national (e.g. unable to drink, repeated vomiting, convulsions) | Emergency | WHO malaria / NATIONAL_MALARIA |
| Dysentery | Blood in stool | Urgent | WHO IMCI |
| Very high fever | e.g. ≥39.5°C (configurable) | Urgent | WHO IMCI |
| Severe acute malnutrition | MUAC &lt; 115 mm (or per national) | Urgent | WHO IMCI |
| Young infant sick | Child &lt; 2 months with any illness | Urgent | WHO IMCI (young infant) |

### 5.2 Maternal

| Trigger | Condition | Urgency | Guideline ref |
|---------|-----------|--------|----------------|
| ANC danger sign | Severe headache, blurred vision, fits, bleeding, fever, severe abdominal pain, etc. | Emergency/Urgent | WHO ANC / NATIONAL_ANC |
| PNC danger sign (mother or newborn) | Per national list (e.g. heavy bleeding, fever, fits; newborn not feeding, convulsions, fast breathing) | Emergency/Urgent | WHO PNC / NATIONAL_PNC |

### 5.3 Referral Suggestion Format

- **Title:** e.g. “Urgent referral”
- **Reason:** “Refer because: [trigger condition].”
- **Guideline:** e.g. “WHO IMCI General Danger Signs” or “[NATIONAL_MALARIA]”
- **Action text:** “Refer to [nearest health facility / per national] immediately.”

---

## 6. How Explanations Are Presented to CHWs

Every suggestion must be **explainable** in the same way.

### 6.1 Per-Suggestion Block

For each recommendation the CHW sees:

1. **Title** — Short action (e.g. “Urgent referral”, “Consider ORS + zinc”).
2. **What to do** — One or two sentences (e.g. “Give ORS Plan B; amount per age. Give zinc for 10–14 days.”).
3. **Why** — “Because: [reason in one sentence].” (e.g. “Child has diarrhea with some dehydration.”)
4. **Based on** — Bullet list of inputs that triggered the rule (e.g. “Diarrhea, sunken eyes, skin pinch returns slowly”).
5. **Guideline** — “From: WHO IMCI Plan B” or “From: [NATIONAL_MALARIA]”.
6. **Risk level** — Critical / High / Medium / Low (and colour if UI supports it).

Wording must never imply diagnosis: use “Consider …”, “Classify as …”, “Refer because …”.

### 6.2 Order of Presentation

- **Critical** first (referrals and danger).
- Then **High**, then **Medium**, then **Low**.
- Within same level: referral before treatment before counsel.

### 6.3 Optional Short Rationale (Tooltip or Expand)

- One line: “This suggestion comes from the [Malaria/Diarrhea/ARI/ANC/PNC] pathway because [condition].”

---

## 7. How CHW Confirmation Is Enforced

### 7.1 No Automatic Actions

- The system **never** records a treatment, referral, or classification as done without an explicit CHW action.
- Suggestions are **recommendations only**; the CHW must **Accept** or **Skip** (with reason) for each.

### 7.2 Required Explicit Actions

- **Referral:** CHW must tap “Accept referral” (or equivalent). Optionally: “I have referred” or “I will refer” and facility name. No way to “complete visit” with an unresolved critical referral without at least acknowledging (e.g. “Skipped” with reason).
- **Treatment:** CHW must tap “Give treatment” or “Skip”. If skip: short reason (e.g. “Already given”, “Not available”, “Patient refused”, “Not needed”).
- **Counsel only:** CHW can “Acknowledge” or “Skip”; no treatment given.

### 7.3 Blocking / Soft Enforcement (Configurable)

- **Critical referral:**  
  - Option A (stricter): Visit cannot be “completed” until the referral suggestion is either accepted or explicitly skipped with reason.  
  - Option B (softer): Show warning “You have an urgent referral suggestion. Please refer or record why not,” but allow completion.
- **Audit:** Every suggestion and CHW response (accept/skip + reason) is logged with timestamp and user for audit and quality, not for overriding the CHW.

### 7.4 Override and Responsibility

- CHW can **always** skip any suggestion.
- When skipping, a **reason** is required (fixed list or free text).
- UI text: “You are responsible for the final decision. This tool only suggests actions based on guidelines.”
- No suggestion is ever shown as “Diagnosis: X”; only “Consider …” or “Refer because …”.

### 7.5 Summary Screen Before Complete

- Before “Complete visit,” show: list of **accepted** actions (referrals, treatments) and **skipped** ones with reasons.
- CHW confirms summary, then taps “Complete visit.”

---

## Implementation Checklist (High Level)

- [ ] Define pathway trees (malaria, diarrhea, ARI, ANC, PNC) as data or code (rules only, no diagnosis).
- [ ] Map inputs (visit + structured notes) into the data model required for each pathway.
- [ ] Implement rule engine: for each pathway, evaluate in order (danger → referral → classify → treat/counsel); output list of suggestions with reason, basedOn, guidelineRef, risk level.
- [ ] Add national placeholders (strings + optional parameters); no change to “no diagnosis” principle.
- [ ] UI: show suggestions in risk order; each with What / Why / Based on / Guideline; Accept / Skip with reason; block or warn on unhandled critical referral; summary before complete.
- [ ] Audit log: suggestion id, type, title, CHW response (accept/skip), reason, timestamp.

---

## References (Placeholders)

- WHO IMCI chart (generic and/or country-adapted).
- WHO guidelines for malaria, diarrhoea, ARI.
- WHO recommendations on antenatal and postnatal care.
- **National guidelines:** [NATIONAL_MALARIA], [NATIONAL_ANC], [NATIONAL_PNC], [NATIONAL_ARI], [NATIONAL_DIARRHEA] — to be replaced with actual ministry documents per deployment country.
