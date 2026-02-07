# Afya Assistant

Offline-first healthcare decision support for Community Health Workers in Africa.

## The 6-Step Visit Workflow

```
1. FIND PATIENT  →  2. START VISIT  →  3. CAPTURE (voice)
       ↓                                      ↓
6. COMPLETE     ←  5. TREAT        ←  4. REVIEW SUGGESTIONS
```

| Step | CHW Does | AI Does | Time |
|------|----------|---------|------|
| 1. Find Patient | Search or create | Voice-to-text name | 30s |
| 2. Start Visit | Tap "Start" | Load history, prep checklist | 10s |
| 3. Capture | Speak symptoms | Extract symptoms, flag danger signs | 2-3m |
| 4. Review | Read suggestions | Show treatments with reasons | 30s |
| 5. Treat | Accept/skip each | Record decisions | 1-2m |
| 6. Complete | Set follow-up | Save, queue sync | 30s |

**Total: 5-10 minutes per visit**

**Architecture (mobile-first, offline-first):** [docs/MOBILE_ARCHITECTURE.md](docs/MOBILE_ARCHITECTURE.md) — local data models, sync, conflict resolution, on-device vs cloud AI, privacy, guidelines.

**Voice-to-structured-notes:** [docs/VOICE_TO_STRUCTURED_NOTES.md](docs/VOICE_TO_STRUCTURED_NOTES.md) — schema, offline pipeline, example input→output, error handling, confidence, CHW correction, audio reference.

**Clinical decision support (no diagnosis):** [docs/CLINICAL_DECISION_SUPPORT.md](docs/CLINICAL_DECISION_SUPPORT.md) — pathways (Malaria, Maternal ANC/PNC, Diarrhea, Respiratory), IMCI + national placeholders, risk levels, referral triggers, explanations, CHW confirmation.

**Treatment plan generator:** [docs/TREATMENT_PLAN_GENERATOR.md](docs/TREATMENT_PLAN_GENERATOR.md) — actionable steps, medication schedules, follow-up reminders, SMS/WhatsApp templates, escalation when patients don’t respond, CHW alerts without overload, low-literacy adaptation.

**Community care network:** [docs/COMMUNITY_CARE_NETWORK.md](docs/COMMUNITY_CARE_NETWORK.md) — traditional healers, pharmacies, clinics, referral hospitals; services and conditions appropriate/not appropriate; contextual suggestions, cultural preferences, outcome-based escalation; safety and ethics.

**Patient communication (SMS/WhatsApp):** [docs/PATIENT_COMMUNICATION_SYSTEM.md](docs/PATIENT_COMMUNICATION_SYSTEM.md) — consent and opt-in/opt-out, low literacy and multi-language templates, shared-phone safety, AI personalisation and response interpretation, risk mitigation, logging and audit.

**AI governance:** [docs/AI_GOVERNANCE_FRAMEWORK.md](docs/AI_GOVERNANCE_FRAMEWORK.md) — AI boundaries and disclaimers, human-in-the-loop, audit logs, model update governance, bias and harm monitoring, incident response; alignment with MoH, NGO ethics, and donor requirements.

**Production MVP (Android):** [docs/MVP_PRODUCTION_README.md](docs/MVP_PRODUCTION_README.md) — tech stack, folder structure, key classes, offline records, voice capture, malaria + maternal guidance, SMS reminders, security, deployment.

**Impact & funder evidence:** [docs/IMPACT_AND_FUNDER_EVIDENCE.md](docs/IMPACT_AND_FUNDER_EVIDENCE.md) — CHW productivity metrics, health outcome improvements, cost savings, pilot study design, evidence narrative for funders.

**CHW web app design system:** [docs/CHW_WEBAPP_DESIGN_SYSTEM.md](docs/CHW_WEBAPP_DESIGN_SYSTEM.md) — Product character: calm, human, reliable, unflashy. Visual & UX: design principles, color palette (WCAG AA, color-blind safe), typography, iconography, spacing, component tone (buttons, alerts, cards).

**CHW web app information architecture:** [docs/CHW_WEBAPP_INFORMATION_ARCHITECTURE.md](docs/CHW_WEBAPP_INFORMATION_ARCHITECTURE.md) — Top-level nav (5 items), Home/dashboard layout, primary vs secondary actions, bottom nav, offline status.

**CHW home dashboard design:** [docs/CHW_HOME_DASHBOARD_DESIGN.md](docs/CHW_HOME_DASHBOARD_DESIGN.md) — Wireframe, visual hierarchy, color usage (meaning only), today’s patients, follow-ups due, high-risk, sync status, quick actions; card-based, no dense tables.

**CHW patient profile design:** [docs/CHW_PATIENT_PROFILE_DESIGN.md](docs/CHW_PATIENT_PROFILE_DESIGN.md) — Layout (identifiers, risk, active plan, timeline), timeline visual style, risk indicator design, expand/collapse, past vs current.

**CHW voice capture & review interface:** [docs/CHW_VOICE_AND_REVIEW_INTERFACE.md](docs/CHW_VOICE_AND_REVIEW_INTERFACE.md) — Recording UI, review and correction flow, confidence indicators, one-tap corrections; helper tone, works offline.

**CHW AI guidance presentation:** [docs/CHW_AI_GUIDANCE_PRESENTATION.md](docs/CHW_AI_GUIDANCE_PRESENTATION.md) — Guidance cards, risk levels and colors, referral suggestions, source references (e.g. WHO), confirmation pattern; calm, supportive, non-alarming unless critical.

**CHW treatment plan & follow-up screen:** [docs/CHW_TREATMENT_PLAN_AND_FOLLOWUP_DESIGN.md](docs/CHW_TREATMENT_PLAN_AND_FOLLOWUP_DESIGN.md) — Treatment plan layout (steps, medications, follow-up), reminder status indicators, patient-friendly printable/shareable view, visual confirmation of completion; plain language, icon-led.

**CHW community care directory UI:** [docs/CHW_COMMUNITY_CARE_DIRECTORY_UI.md](docs/CHW_COMMUNITY_CARE_DIRECTORY_UI.md) — List and map layout, provider card design (traditional healers, pharmacies, clinics), “appropriate for” indicators, distance and availability, safety warning presentation; neutral, no hierarchy; suggest not instruct.

**CHW offline and sync indicators:** [docs/CHW_OFFLINE_AND_SYNC_INDICATORS.md](docs/CHW_OFFLINE_AND_SYNC_INDICATORS.md) — System-wide offline/sync states, indicator designs, placement rules, data freshness, copy examples; passive, always visible, non-technical; build trust, reduce fear of data loss.

**CHW visit visual walkthrough:** [docs/CHW_VISIT_VISUAL_WALKTHROUGH.md](docs/CHW_VISIT_VISUAL_WALKTHROUGH.md) — Full visit scenario (new patient, suspected malaria, offline, follow-up): dashboard → patient creation → voice capture → AI guidance → treatment plan → follow-up → return to dashboard; screen-by-screen descriptions and design intent.

## Key Files

```
app/src/main/java/com/afya/assistant/
├── domain/
│   ├── VisitWorkflow.kt      # Orchestrates the 6 steps
│   └── models/               # Patient, Visit, Suggestion
├── ai/
│   └── DecisionSupport.kt    # Rule-based suggestions
├── guidelines/
│   ├── DangerSigns.kt        # WHO IMCI danger signs
│   └── Treatments.kt         # ORS, Zinc, Paracetamol
├── data/local/
│   └── AppDatabase.kt        # Room DB (single file)
├── voice/
│   └── VoiceCapture.kt       # Vosk offline STT
├── sync/
│   └── SyncManager.kt        # Upload when online
└── audit/
    └── AuditLogger.kt        # Action logging
```

## What AI Does (and Doesn't Do)

### AI Does:
- Convert voice to text
- Extract symptoms from speech ("fever for 3 days" → Symptom)
- Flag potential danger signs
- Suggest treatments with dosing
- Explain WHY each suggestion is made

### AI Does NOT:
- Make diagnoses
- Take any action without CHW confirmation
- Work without network (everything runs locally)
- Hide its reasoning

## Safety Features

1. **Danger signs checked first** - Every symptom entry
2. **CHW must confirm** - "Can child drink?" prompt
3. **All suggestions explained** - Reason + WHO guideline reference
4. **Overrides logged** - If CHW skips suggestion, reason recorded

## Data Flow

```
Voice → Local SQLite → Sync when online
          ↓
    DecisionSupport
      (offline)
```

| Data | Stored | Synced |
|------|--------|--------|
| Patients | Local | Yes |
| Visits | Local | Yes (danger signs first) |
| Audit logs | Local | Yes (batched) |
| Voice audio | Never saved | Never |

## Running

**Web app (easiest):**
```bash
cd webapp
python -m http.server 8081
```
Open http://localhost:8081 (if blocked, try port 5500)

**Android:**
```bash
cd app && ./gradlew assembleDebug
```

**Backend:**  
```bash
cd cloud && pip install -r requirements.txt && uvicorn api.main:app
```

## Design Principles

- **KISS**: 5 screens, 1 database file, 1 workflow class
- **YAGNI**: No dashboards, no analytics, no reports
- **Offline-first**: 100% functional without network
- **Voice-first**: Minimize typing on entry-level phones
