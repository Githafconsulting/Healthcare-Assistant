# Impact Metrics, Pilot Study Design & Evidence Narrative for Funders

This document translates the Afya Healthcare Assistant into **measurable CHW productivity gains**, **health outcome improvements**, and **cost savings for health systems**, and provides a **pilot study design** and **evidence narrative** for funders (MoH, NGOs, donors).

---

## 1. Measurable CHW Productivity Gains

### 1.1 How the product drives productivity

| Product feature | Productivity mechanism | Measurable outcome |
|-----------------|------------------------|--------------------|
| **Voice capture + structured notes** | Less time writing; auto-extraction of symptoms and chief complaint | **Reduced time per visit** (minutes) |
| **6-step workflow (5–10 min target)** | Standardised flow; minimal taps; no long forms | **More visits per CHW per day** |
| **Guideline-based suggestions** | CHW sees treatment/referral options with reason and dosing; less mental load | **Faster decision step**; **higher guideline adherence** |
| **Offline-first** | No waiting for network; sync when back | **No visit time lost to connectivity** |
| **Follow-up reminders (SMS)** | System prompts patient; CHW sees “due today” list | **Fewer missed follow-ups**; **less CHW time chasing defaulters** |

### 1.2 Impact metrics: CHW productivity

| Metric | Definition | Baseline (typical without tool) | Target (with Afya) | How measured |
|--------|-------------|----------------------------------|--------------------|--------------|
| **Time per completed visit** | From visit start to “Complete visit” (minutes) | 15–25 min (paper/mixed) | **≤ 10 min** | App timestamp: `startTime` to `endTime` |
| **Visits per CHW per day** | Completed visits per working day | 15–25 | **25–35** | Count completed visits per CHW per day |
| **Documentation completeness** | % visits with symptoms + at least one outcome (treatment/referral/counsel) | 40–60% (paper) | **≥ 90%** | % visits with non-empty symptoms and assessment/treatment/referral |
| **Guideline adherence (malaria)** | % fever cases with RDT considered or ACT/paracetamol suggested per guideline | Variable (30–50%) | **≥ 80%** | % fever visits where suggestion log shows RDT/ACT/paracetamol path |
| **Guideline adherence (danger signs)** | % visits with any danger sign where referral was suggested and CHW acknowledged | Variable | **100%** (suggestion present); CHW accept/skip tracked | Audit: suggestion type REFERRAL/DANGER_SIGN + CHW response |
| **Follow-up scheduling rate** | % completed visits with follow-up date set | Low (often not recorded) | **≥ 70%** (where clinically indicated) | % visits with followUpDays or due date |
| **CHW time on admin per day** | Hours spent on reporting/paperwork (if collected) | 1–2 h | **&lt; 30 min** (reporting from app) | Survey or time-motion (pilot) |

### 1.3 Summary: productivity gains for funders

- **Fewer minutes per visit** → same CHW can see **more patients per day** (estimated **+30–50%** in visits per day if time per visit drops from ~20 min to ~10 min).
- **More complete documentation** → better continuity, fewer repeated questions, and **auditable trail** for supervision and M&E.
- **Higher guideline adherence** → more consistent quality of care and **fewer missed referrals or wrong treatments**.

---

## 2. Health Outcome Improvements

### 2.1 Causal pathway (theory of change)

```
Tool use → Faster, more standardised visits
         → Danger signs surfaced and referral suggested
         → Malaria/ORS/maternal pathways followed
         → Follow-up reminders sent and due list visible
         → More referrals completed, more treatments given correctly, more follow-ups attended
         → Better outcomes (morbidity, mortality, continuity)
```

The tool does **not** diagnose; it **supports** the CHW to follow guidelines and refer when needed. Outcomes depend on CHW and system (facility capacity, transport, caregiver behaviour).

### 2.2 Impact metrics: health outcomes

| Outcome | Definition | Baseline (typical) | Target (with Afya) | How measured |
|---------|------------|--------------------|--------------------|--------------|
| **Danger sign → referral suggested** | % of visits where a danger sign was recorded and a referral suggestion was shown | Often missed | **100%** (when danger sign in pathway) | Audit: danger sign present → REFERRAL suggestion in log |
| **Referral completion (patient attended)** | % of referrals where patient attended facility (within 7 days) | 30–50% | **≥ 50%** (pilot); **≥ 60%** (at scale with reminders) | Referral outcome field; facility or CHW follow-up |
| **Malaria: appropriate treatment** | % fever cases with RDT+ receiving ACT (or RDT− managed per guideline) | Variable | **≥ 75%** | Visit + suggestion log: fever, RDT result, ACT given/suggested |
| **Diarrhea: ORS/zinc given** | % diarrhea cases with ORS (and zinc where available) given or suggested and accepted | 40–60% | **≥ 80%** | Visit symptoms + treatment log |
| **Follow-up attendance** | % of scheduled follow-ups where patient was seen (by CHW or facility) within 2 days of due date | 20–40% | **≥ 45%** (pilot); **≥ 55%** with SMS | FollowUp entity: due date vs actual visit/completion |
| **Maternal danger sign → referral** | % ANC/PNC visits with maternal danger sign where referral suggested and acknowledged | Variable | **100%** (suggestion); CHW response logged | Audit + maternal pathway |
| **Under-5 mortality (if measurable)** | Deaths per 1,000 live births in catchment (programme data or survey) | Local rate | **No increase**; trend **down** in pilot area vs comparison (if powered) | Vital registration or household survey (longer-term) |

### 2.3 Evidence link to WHO and literature

- **IMCI:** WHO evidence that correct classification and referral reduce child mortality; tool reinforces IMCI steps and danger signs.
- **Malaria:** RDT + ACT and referral for severe cases align with WHO and national policy; consistent application expected to improve appropriate treatment and referral.
- **Maternal:** ANC/PNC danger signs and referral align with WHO recommendations; tool ensures danger signs trigger a referral suggestion and CHW acknowledgment.
- **Follow-up:** Reminders and due lists are associated with better adherence in other settings; SMS consent-based reminders support attendance.

---

## 3. Cost Savings for Health Systems

### 3.1 Cost mechanisms

| Mechanism | How Afya contributes | Cost impact |
|-----------|----------------------|-------------|
| **Fewer missed referrals** | Danger signs and referral suggestions with CHW acknowledgment | Fewer late presentations and expensive emergency care; lower mortality and disability costs |
| **Less duplication and repeat visits** | Structured records, follow-up list, patient search | Fewer “lost” patients and repeated assessments |
| **Faster visits** | Voice + workflow; less paper | Same number of CHWs can cover more households (or same coverage with less overtime) |
| **Better first-line treatment** | Guideline-based ORS, zinc, ACT, paracetamol | More cases resolved at community level; fewer unnecessary facility visits |
| **Targeted facility use** | Referral only when pathway says so; reminders for follow-up | Facilities see appropriate cases; less congestion from non-urgent or defaulted follow-ups |
| **Reporting from app** | Sync and audit; no separate paper aggregation | Less supervisor/CHW time on manual reporting |

### 3.2 Impact metrics: cost

| Metric | Definition | Direction | How estimated |
|--------|-------------|-----------|----------------|
| **Cost per visit (CHW time)** | CHW cost per completed visit (salary/benefit prorated per visit) | **Decrease** (more visits per day) | (Monthly CHW cost / working days) / visits per day |
| **Referral cost per appropriate referral** | Facility cost per referral that was indicated by guideline | **Stable or better value** (same or more appropriate referrals, fewer inappropriate) | Facility cost / number of referrals; quality via outcome |
| **Cost of missed referral (avoided)** | Estimated cost of a missed danger sign (emergency care, death, disability) | **Reduction** if missed referrals fall | Literature or local estimate × (baseline missed − pilot missed) |
| **Reporting/admin cost per CHW** | Time and cost of aggregation and reporting | **Decrease** (digital sync vs paper) | Time-motion or supervisor survey |
| **Incremental cost of Afya** | Device, data, SMS, training, support per CHW per year | **Transparent** for ROI | Sum: device amortisation, data, SMS, training, support |

### 3.3 ROI narrative for funders

- **Productivity:** If visits per CHW per day rise by 30–50%, the **cost per visit** drops proportionally for the same CHW salary.
- **Quality:** Fewer missed danger signs and better guideline adherence can **reduce costly complications** (e.g. severe malaria, maternal emergencies) and **improve appropriate use** of facilities.
- **Pilot:** Collect cost per visit (with vs without tool), referral outcomes, and follow-up attendance to model **incremental cost per additional visit** and **cost per additional appropriate referral or completed follow-up**.

---

## 4. Pilot Study Design

### 4.1 Objective

To estimate the **effect of Afya Assistant** on CHW productivity (time per visit, visits per day, documentation completeness, guideline adherence), **health-related behaviours** (referral completion, follow-up attendance, treatment given), and **cost per visit** in a defined catchment over a fixed period.

### 4.2 Design

- **Type:** Cluster randomised or quasi-experimental (matched clusters).
  - **Option A:** Clusters = health posts or CHW zones; randomise to **Afya** vs **usual care** (paper or existing tool).
  - **Option B:** Before–after in same clusters (Afya introduced after baseline period); weaker for causation but simpler.
- **Duration:** **6–12 months** (minimum 3 months post-implementation for outcome stability).
- **Sample:**
  - **Clusters:** e.g. 20–30 per arm (power for visit-level and cluster-level outcomes).
  - **CHWs:** All CHWs in selected clusters (e.g. 40–80 total).
  - **Visits:** All visits in study period (or random sample per cluster per month) for productivity and process indicators.

### 4.3 Arms

| Arm | Description |
|-----|-------------|
| **Intervention** | CHWs use Afya Assistant (offline patient records, voice capture, malaria + maternal guidance, SMS reminders with consent). Training and devices provided. |
| **Control** | Usual care (paper registers and/or existing digital tool). Same supervision and facility access. |

### 4.4 Primary outcomes

| # | Outcome | Measure | Analysis |
|---|---------|---------|----------|
| 1 | **Time per completed visit (minutes)** | App (intervention) vs time-motion or sample (control) | Mean per cluster; mixed model (cluster random effect) |
| 2 | **Visits per CHW per day** | Count per CHW per day over study period | Mean per cluster; mixed model |
| 3 | **Documentation completeness (%)** | % visits with symptoms + outcome | Proportion per cluster; GEE or mixed model |

### 4.5 Secondary outcomes

| # | Outcome | Measure |
|---|---------|---------|
| 4 | Guideline adherence (danger sign → referral suggested) | % visits with danger sign and referral suggestion |
| 5 | Guideline adherence (malaria: RDT/ACT path) | % fever visits with appropriate suggestion and CHW accept |
| 6 | Referral completion (patient attended within 7 days) | Referral outcome from CHW/facility |
| 7 | Follow-up attendance (within 2 days of due) | FollowUp due date vs actual visit |
| 8 | Cost per visit (CHW time) | (CHW cost / visits per day) by arm |
| 9 | CHW satisfaction / acceptability | Survey (e.g. SUS, acceptability scale) |

### 4.6 Data collection

- **Routine:** App data (intervention): visit timestamps, symptoms, suggestions, CHW responses, follow-ups, reminders sent. Sync to secure server; de-identified for analysis.
- **Control:** Visit count and time per visit from register or time-motion sample; sample of forms for documentation completeness and guideline adherence.
- **Referral/follow-up outcomes:** From CHW or facility record; or short follow-up call/survey (consent).
- **Costs:** CHW salary, device, data, SMS, training (incremental cost of Afya in intervention arm).

### 4.7 Ethics and governance

- **Ethics:** IRB/ethics approval; consent for use of routine data and (if any) follow-up contact.
- **MoH:** Agreement and, where required, co-authorship and approval for publication.
- **Data:** Stored and analysed per national and funder requirements; no PHI in publications.

### 4.8 Sample size (illustrative)

- For **time per visit:** Assume SD ≈ 5 min, detectable difference 3 min, 80% power, ICC 0.05 → ~20 clusters per arm, ~15 visits per cluster (or 300 visits per arm).
- For **visits per CHW per day:** Assume baseline 20, target 28, SD 5 → similar cluster count.
- Adjust with a statistician and local ICC estimates.

---

## 5. Evidence Narrative for Funders

### 5.1 The problem

- **CHWs are overloaded** and spend long periods on paper documentation and recall, limiting the number of quality visits per day.
- **Guidelines are underused** in the field: danger signs are missed, malaria and diarrhea management are inconsistent, and maternal danger signs do not always trigger referral.
- **Follow-up is weak:** Many patients default; CHWs have no simple way to see who is due or to remind caregivers.
- **Health systems bear avoidable costs** from missed referrals (late presentations, emergencies), duplication, and low productivity of the CHW workforce.

### 5.2 The solution

- **Afya Assistant** is an offline-first, Android decision-support tool for CHWs. It provides:
  - **Structured, voice-supported visits** (5–10 min target) with offline patient records.
  - **Guideline-based suggestions** (WHO IMCI, malaria, maternal) with clear reasons and referral triggers; **no diagnosis**—CHW decides.
  - **Follow-up scheduling and SMS reminders** (with consent) to improve attendance.
- **Human-in-the-loop:** Every suggestion is accepted or skipped by the CHW; all actions are logged for audit and M&E.

### 5.3 Theory of change

- The tool **reduces time per visit** and **increases visits per day** (productivity).
- It **surfaces danger signs and referral** and **standardises malaria/maternal pathways** (quality).
- It **schedules and reminds** for follow-up (continuity).
- Together, this leads to **more appropriate referrals**, **better treatment adherence**, and **higher follow-up attendance**, contributing to **better health outcomes** and **more efficient use of the health system** (cost savings and value).

### 5.4 Evidence base

- **WHO IMCI and malaria/maternal guidelines** are the clinical foundation; the tool encodes these and prompts the CHW.
- **Digital CHW tools** in similar settings have shown gains in documentation, guideline adherence, and time savings; **SMS reminders** have shown improved attendance in other programmes.
- **This pilot** will generate **local evidence** on productivity, process, and cost to inform scale-up and further funding.

### 5.5 Scalability and sustainability

- **Offline-first** allows use in low-connectivity areas; sync when online supports supervision and reporting.
- **Configurable** guidelines and templates allow national adaptation (malaria, maternal, language).
- **Cost structure** is predictable: devices, data, SMS, training; ROI improves as visits per CHW increase and outcomes improve.
- **MoH alignment:** Designed for integration with national guidelines and reporting; can support national CHW programmes.

### 5.6 Asks for funders

- **Pilot funding** for devices, data, SMS gateway, training, and **independent M&E** (data collection, analysis, report).
- **Partnership** with MoH and implementing NGO for approval, rollout, and use of data.
- **Commitment** to use pilot results for **scale-up decisions** and to share **evidence** (reports, summaries) with the sector.

### 5.7 One-page summary (for proposals)

| Section | Points |
|---------|--------|
| **Problem** | CHW productivity and guideline adherence are limited by paper and lack of decision support; follow-up is weak; health systems bear avoidable costs. |
| **Solution** | Afya Assistant: offline Android app with voice capture, IMCI/malaria/maternal guidance, and SMS reminders; human-in-the-loop, no diagnosis. |
| **Productivity** | Target: ≤10 min per visit, 25–35 visits/CHW/day, ≥90% documentation completeness, ≥80% guideline adherence (malaria/danger signs). |
| **Outcomes** | Target: 100% danger sign → referral suggested; ≥50% referral completion; ≥45% follow-up attendance (pilot); better malaria/ORS/maternal process indicators. |
| **Cost** | Lower cost per visit (more visits/day); fewer missed referrals; incremental cost of tool (device, data, SMS) transparent for ROI. |
| **Pilot** | 6–12 months; cluster design; primary: time per visit, visits/day, documentation; secondary: adherence, referral completion, follow-up, cost. |
| **Evidence** | WHO-aligned; pilot will produce local productivity, outcome, and cost data for scale-up and further investment. |

---

This document can be used in **funding proposals**, **MoH briefs**, and **partner presentations** to articulate measurable impact, pilot design, and the evidence narrative for Afya Assistant.
