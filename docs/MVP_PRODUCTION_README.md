# Production-Ready MVP — Afya Healthcare Assistant

This document describes the **production-ready MVP** for the Android Healthcare Assistant: scope, tech stack, architecture, key classes, security, and deployment. It is intended for pilot deployment with Ministry of Health or NGO partners.

---

## 1. Scope (MVP)

| Capability | Description |
|------------|-------------|
| **Android app** | Single APK; minSdk 26; offline-first; Jetpack Compose UI. |
| **Offline patient records** | Patients and visits stored in Room; sync when online; no PHI in logs. |
| **Voice note capture** | Vosk-based offline speech-to-text; transcript linked to visit; optional audio file reference. |
| **Malaria + maternal health guidance** | Rule-based suggestions: malaria (fever, RDT, ACT, refer); maternal (ANC/PNC danger signs, counsel). No diagnosis. |
| **SMS reminders** | Follow-up reminders scheduled locally; sent via gateway when online; consent and opt-out enforced. |

---

## 2. Tech Stack Choices

| Layer | Choice | Rationale |
|-------|--------|-----------|
| **Language** | Kotlin 1.9+ | Null safety, coroutines, preferred for Android. |
| **UI** | Jetpack Compose + Material 3 | Single toolkit, good for list/detail and forms. |
| **Local DB** | Room 2.6+ | Offline-first; migrations; type-safe DAOs. |
| **DI** | Hilt | Scoped dependencies; testability; standard for Android. |
| **Network** | Retrofit + Gson | Sync uploads; simple JSON. |
| **Voice** | Vosk (offline) | No cloud dependency; model shipped with app or downloaded once. |
| **Background** | WorkManager | Deferred sync and reminder scheduling; survives reboot. |
| **Security** | EncryptedSharedPreferences (optional), no PHI in logs | Keys/tokens; audit logs contain IDs only, not names. |
| **Min SDK** | 26 | Broad device coverage; acceptable for CHW programmes. |

---

## 3. Folder Structure (Clean Modular Architecture)

```
app/src/main/java/com/afya/assistant/
├── AfyaApplication.kt              # Application class; Hilt; init Vosk/DB
├── di/
│   └── AppModule.kt                # Provides DB, API, services
│
├── domain/                         # Business logic and models (no Android)
│   ├── models/
│   │   ├── Patient.kt
│   │   ├── Visit.kt
│   │   ├── Suggestion.kt
│   │   ├── AuditEntry.kt
│   │   └── FollowUp.kt             # For reminders
│   └── VisitWorkflow.kt            # Orchestrates 6-step flow
│
├── data/                           # Data layer
│   └── local/
│       └── AppDatabase.kt          # Room DB: entities + DAOs (patients, visits, audit, voice_notes, follow_ups)
│
├── guidelines/                     # Clinical rules (read-only; no diagnosis)
│   ├── DangerSigns.kt             # WHO IMCI danger signs
│   ├── Treatments.kt              # ORS, Zinc, Paracetamol
│   ├── MalariaGuidance.kt          # Malaria: RDT, ACT, refer
│   └── MaternalGuidance.kt         # ANC/PNC danger signs and counsel
│
├── ai/                             # Decision support (rule-based)
│   └── DecisionSupport.kt         # Evaluates visit → suggestions; uses guidelines
│
├── voice/                          # Voice capture
│   └── VoiceCapture.kt             # Vosk STT; optional audio file ref
│
├── messaging/                      # SMS reminders
│   ├── SmsReminderService.kt      # Schedule + send via gateway
│   └── ReminderGateway.kt          # Interface for SMS API (inject implementation)
│
├── sync/                           # Offline sync
│   ├── SyncManager.kt
│   ├── SyncApi.kt
│   └── ConnectivityMonitor.kt
│
├── audit/                          # Audit trail
│   └── AuditLogger.kt              # Append-only; no PHI in message
│
├── security/                       # Security-first helpers (optional in MVP)
│   └── SecurePrefs.kt             # CHW id / token storage (encrypted if available)
│
└── ui/                             # Compose screens
    ├── MainActivity.kt
    ├── nav/                        # Navigation graph
    ├── screen/
    │   ├── home/
    │   ├── patient/
    │   ├── capture/
    │   ├── suggestions/
    │   └── summary/
    └── theme/
```

**Separation of concerns:**
- **domain:** Pure Kotlin; visit workflow, models; no Android APIs.
- **data:** Room only; entities and DAOs; mapping to/from domain in workflow or use-case.
- **guidelines:** Static rules and lists; no DB.
- **ai:** DecisionSupport depends on domain + guidelines; returns suggestions.
- **voice, messaging, sync, audit:** Single-responsibility services; injected where needed.
- **ui:** Compose; observes ViewModel or StateFlow from VisitWorkflow / repositories.

---

## 4. Key Classes and Services

### 4.1 Domain

- **Patient** — id, name, dateOfBirth, sex, village, phone, caregiverName; `ageInMonths()`, `isUnderFive()`.
- **Visit** — id, patientId, chwId, symptoms, vitals, dangerSigns, assessment, treatment, referral, notes; synced.
- **Suggestion** — type (DANGER_SIGN, REFERRAL, TREATMENT, etc.), title, description, reason, guidelineRef, confidence, isUrgent; accepted/skipReason.
- **FollowUp** — visitId, patientId, dueDate, reason, reminderSentAt, smsConsent.

### 4.2 Data (Room)

- **PatientEntity, VisitEntity, AuditEntity** — existing.
- **VoiceNoteEntity** — id, visitId, filePathOrNull, durationMs, transcript, createdAt; links voice capture to visit.
- **FollowUpEntity** — id, visitId, patientId, dueDateEpoch, reason, reminderSentAt, smsConsent, synced.

### 4.3 Guidelines

- **DangerSigns** — WHO IMCI general danger signs; `checkVitals()`, `checkSymptoms()`.
- **Treatments** — ORS, Zinc, Paracetamol; `forSymptoms()`.
- **MalariaGuidance** — Fever + RDT positive → ACT; danger signs → refer; no diagnosis.
- **MaternalGuidance** — ANC/PNC danger signs; counsel points; refer triggers.

### 4.4 AI

- **DecisionSupport** — `evaluate(patient, visit): List<Suggestion>`. Uses DangerSigns, Treatments, MalariaGuidance, MaternalGuidance; orders by urgency; no diagnosis.

### 4.5 Voice

- **VoiceCapture** — Vosk init, start/stop, transcript StateFlow; optional: save audio path to VoiceNoteEntity for visit.

### 4.6 Messaging (SMS)

- **SmsReminderService** — `scheduleReminder(followUp)` (WorkManager or AlarmManager); when due, check consent then call **ReminderGateway**.
- **ReminderGateway** — Interface: `sendSms(phone, message)`. Implementation uses partner SMS API (e.g. Africa's Talking, Twilio); configured via build/config.

### 4.7 Sync

- **SyncManager** — When online, upload unsynced patients, visits, audit in priority order; mark synced; do not upload raw voice or full PHI in audit payload.

### 4.8 Audit

- **AuditLogger** — `log(chwId, action, entityType, entityId, details)`. Details must not contain patient name or free-text notes; use IDs only. Append-only.

---

## 5. Security-First Design

| Measure | Implementation |
|---------|----------------|
| **Data at rest** | Room DB in app-private storage; optional: enable Room encryption (SQLCipher) for pilot if required. |
| **No PHI in logs** | Audit: entityId only (e.g. visitId), no names; Android log: no patient data; ProGuard in release. |
| **Network** | HTTPS only; certificate pinning optional for pilot. |
| **Secrets** | API keys / SMS gateway credentials in build config or secure backend; never in source. |
| **Auth** | CHW identity (chwId) from login or device profile; no patient-facing auth in MVP. |
| **Consent for SMS** | Store consent per contact; SmsReminderService sends only if consent and not opted out. |

---

## 6. Deployment Considerations

### 6.1 Build variants

- **debug** — Logging; test backend URL; no obfuscation.
- **release** — Minify + ProGuard; production API URL; versionCode/versionName from CI.

### 6.2 Configuration

- **Base URL** for sync API: BuildConfig or `local.properties` / env.
- **SMS gateway** (API key, sender ID): BuildConfig or secure config; not in repo.
- **Vosk model**: Bundled in assets (small model) or first-run download; document in pilot playbook.

### 6.3 Pilot checklist

- [ ] MoH or partner sign-off on scope (no diagnosis, human-in-the-loop).
- [ ] Backend sync API deployed and health-check passing.
- [ ] SMS gateway account and credentials; test numbers.
- [ ] Consent flow for SMS documented and implemented.
- [ ] Audit log retention and access agreed (e.g. 2 years).
- [ ] CHW training on: voice capture, reviewing suggestions, when to refer, SMS consent.
- [ ] Device requirements: Android 8+, ~200 MB free, microphone.

### 6.4 Release

- **Channel:** Internal testing → closed pilot (e.g. Internal testing track) → production track.
- **Versioning:** versionCode increments every release; versionName semantic (e.g. 1.0.0-pilot).
- **Ota / Play:** If using Play, upload AAB; restrict to testers or target country as needed.

### 6.5 Build requirements

- **Launcher icons:** Add `res/mipmap-*` with `ic_launcher` and `ic_launcher_round` (Android Studio: File → New → Image Asset), or set `android:icon` to a drawable for testing.
- **Vosk model:** Place small Vosk model in `files/vosk-model` or implement first-run download; see [VoiceCapture](app/src/main/java/com/afya/assistant/voice/VoiceCapture.kt).

---

## 7. Commented Code and Maintainability

- **Public APIs** (workflow, DecisionSupport, SmsReminderService, AuditLogger): KDoc with purpose, parameters, and “no diagnosis” where relevant.
- **Guidelines**: Short comment per rule (e.g. “WHO IMCI General Danger Signs”).
- **Security-sensitive paths**: Comment that no PHI is logged and consent is checked before SMS.

---

## 8. Alignment with Existing Docs

| Doc | MVP usage |
|-----|-----------|
| [CLINICAL_DECISION_SUPPORT](CLINICAL_DECISION_SUPPORT.md) | Malaria + maternal pathways; suggestion format; no diagnosis. |
| [TREATMENT_PLAN_GENERATOR](TREATMENT_PLAN_GENERATOR.md) | Follow-up and reminder logic; templates for SMS. |
| [PATIENT_COMMUNICATION_SYSTEM](PATIENT_COMMUNICATION_SYSTEM.md) | Consent and opt-out before SMS; templates only. |
| [AI_GOVERNANCE_FRAMEWORK](AI_GOVERNANCE_FRAMEWORK.md) | Boundaries, human-in-the-loop, audit, incident response. |

This MVP is **ready for pilot deployment** once the backend, SMS gateway, and consent flows are in place and CHW training is done.
