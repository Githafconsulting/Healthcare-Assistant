# Mobile-First, Offline-First Architecture

## Overview

Android app for Community Health Workers that works fully offline for days, syncs when connectivity returns, and keeps patient data encrypted and private.

---

## Architecture Diagram (Text)

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           ANDROID DEVICE (Offline-First)                         │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐ │
│  │   Voice     │  │  Guidelines  │  │  Decision    │  │   Audit Logger      │ │
│  │   Capture   │  │  (local      │  │  Support     │  │   (immutable log)   │ │
│  │   (Vosk)    │  │   bundle +   │  │  (rule-based │  │                      │ │
│  │             │  │   synced)    │  │   on-device) │  │                      │ │
│  └──────┬──────┘  └──────┬───────┘  └──────┬───────┘  └──────────┬───────────┘ │
│         │                │                 │                      │             │
│         └────────────────┴─────────────────┴──────────────────────┘             │
│                                          │                                        │
│  ┌───────────────────────────────────────▼─────────────────────────────────────┐│
│  │              ENCRYPTED LOCAL STORAGE (SQLite + SQLCipher)                     ││
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐             ││
│  │  │  Patients   │ │   Visits    │ │  SyncQueue  │ │  AuditLogs  │             ││
│  │  │  Guidelines │ │             │ │  (pending   │ │  Guidelines │             ││
│  │  │             │ │             │ │   uploads)  │ │  (versions) │             ││
│  │  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘             ││
│  └───────────────────────────────────────┬───────────────────────────────────────┘│
│                                          │                                        │
│  ┌───────────────────────────────────────▼─────────────────────────────────────┐│
│  │                    SYNC ENGINE (WorkManager + Connectivity)                   ││
│  │   When online: drain SyncQueue (priority: danger/referral → visits → audit)   ││
│  │   Conflict resolution: safety-first merge or last-write-wins by entity type    ││
│  └───────────────────────────────────────┬───────────────────────────────────────┘│
│                                          │                                        │
└──────────────────────────────────────────┼────────────────────────────────────────┘
                                           │ HTTPS (when connected)
                    ┌──────────────────────▼──────────────────────┐
                    │                  CLOUD                       │
                    │  ┌────────────┐  ┌────────────┐              │
                    │  │  Sync API  │  │ Guidelines│              │
                    │  │  (receive  │  │  CDN /    │              │
                    │  │   uploads) │  │  versioned│              │
                    │  └────────────┘  └────────────┘              │
                    │  No PHI in analytics; sync uses tokens only  │
                    └─────────────────────────────────────────────┘

  ON-DEVICE AI (always)          CLOUD AI (never for this app)
  ─────────────────────         ─────────────────────────────
  • Voice → text (Vosk)          • (Optional) future: aggregate
  • Symptom extraction             analytics only, no PHI
  • Danger sign checks
  • Treatment suggestions
  • All guidance rule-based,
    guideline-referenced
```

---

## 1. Local Data Models

All entities are stored in Room with SQLCipher. Primary keys are UUIDs generated on-device so there are no ID collisions when offline.

### Core entities

| Entity | Purpose | Key fields |
|--------|---------|------------|
| **Patient** | One per person under care | id (UUID), name, dateOfBirth, sex, village, phone?, caregiverName?, createdAt, updatedAt, synced |
| **Visit** | One per encounter | id, patientId, chwId, startTime, endTime?, symptoms (JSON), vitals (JSON), dangerSigns (JSON), assessment?, treatment?, referral (JSON)?, followUpScheduled?, synced |
| **AuditLog** | Immutable trail of actions | id, timestamp, chwId, action, entityType, entityId, details?, synced |
| **SyncQueueItem** | Pending upload | id, entityType, entityId, operation (CREATE/UPDATE), payload (JSON), priority, attempts, lastError? |
| **GuidelineVersion** | Which guidelines are on device | id, category, version, contentHash, downloadedAt |

### Design choices

- **JSON for variable content** (symptoms, vitals, referral) so schema stays stable and sync payloads are simple.
- **No server-generated IDs for core data** — device creates UUIDs; server can store same id (no merge-by-id from server for patients/visits).
- **Version or updatedAt** on Patient/Visit for conflict detection (see Sync).

---

## 2. Encrypted Local Storage

- **SQLCipher** (or Android Encrypted SharedPreferences for small config) for the main Room database.
- **Key** comes from Android Keystore (device-bound), derived with a user PIN/password or biometric so that:
  - Unlock is required after app launch (or after period of inactivity).
  - Raw DB file is useless without the key.
- **Room** points at the SQLCipher-backed DB; all DAOs read/write encrypted data.
- **No PHI in filenames or logs**; temp files (e.g. voice) are cleared after use.

---

## 3. Sync Strategy (Eventual Consistency)

### Principles

- **Offline-first**: App never blocks on network. All writes go to local DB and into SyncQueue.
- **Eventual consistency**: When the device sees network, it uploads queued items; server is the secondary store.
- **No mandatory “sync before use”**: CHW can work for days without connectivity.

### Sync flow

1. **Connectivity**  
   - ConnectivityMonitor (network callback) sets “online” when there is validated internet.

2. **Drain queue**  
   - SyncEngine runs (e.g. via WorkManager when online).  
   - Reads SyncQueue ordered by **priority** (e.g. danger/referral first, then visits, then audit).  
   - For each item: POST to Sync API with entity payload + version/updatedAt.

3. **Success**  
   - Mark entity `synced = true`; remove or mark SyncQueueItem complete.

4. **Failure**  
   - Increment attempts; exponential backoff; re-queue.  
   - After N failures, keep in queue and show “X pending” in UI; data stays on device.

5. **Optional pull**  
   - If the backend supports it: when online, GET “updates since last sync timestamp” for guidelines only (no PHI).  
   - Patients/visits can stay upload-only from the device to simplify conflict handling and privacy.

### What gets synced

| Data | Direction | When |
|------|-----------|------|
| Patients | Device → Cloud | After create/update |
| Visits | Device → Cloud | After visit complete |
| Audit logs | Device → Cloud | Batched when online |
| Guidelines | Cloud → Device | Periodic or on app launch when online |

---

## 4. Conflict Resolution

- **Single writer per record in practice**: One CHW, one device per patient record at a time; conflicts are rare (e.g. same patient edited on two devices before sync).

### Strategy by entity

| Entity | Strategy | Rationale |
|--------|----------|-----------|
| **Patient** | **Last-write-wins (LWW)** by `updatedAt` | Demographics; if two devices edit, newer wins. Optional: server returns 409 with server version so client can show “updated on server” and re-fetch. |
| **Visit** | **Merge by rule** | Visits are append-only in practice. If same visitId synced twice: server keeps one; if different devices created different visits for same patient, both are kept (different ids). No merge of symptom lists. |
| **AuditLog** | **Append-only** | No conflict; every log line is unique. Server deduplicates by id. |
| **Guidelines** | **Server wins** | Guidelines are versioned on server; device replaces local copy when it downloads a newer version. |

### Safety-first rule

- Any **danger sign or referral** in a visit is never overwritten by a merge. If a merge were to combine two versions of a visit, the result must include all danger signs and referrals from both.

---

## 5. Works With No Network for Days

- **All core flows use only local storage and on-device AI**: find/create patient, capture visit (voice + taps), suggestions, treatment plan, follow-up. No API calls required.
- **SyncQueue** grows unbounded (within device storage); when back online, sync drains the queue.
- **Guidelines**: App ships with a bundled baseline; optional updates when online. If never online, baseline guidelines still work.
- **Time** is device time; server can normalize to UTC on ingest if needed.

---

## 6. Voice Capture and AI Assistance

### Voice (on-device only)

- **Vosk** (or similar) for offline speech-to-text in the language(s) supported by the model.
- Audio is processed in memory or short-lived temp file; **not** stored or sent to the cloud.
- Transcript is used to **extract symptoms** (keyword/rules) and to **pre-fill or suggest** fields; CHW confirms or edits.

### AI assistance (on-device only)

- **Rule-based only**: Guideline-driven (e.g. WHO IMCI). No cloud inference, no sending PHI off-device.
- **Responsibilities**:
  - Map symptoms + vitals to **danger signs** and **suggested treatments**.
  - Expose **reason** and **guideline reference** for every suggestion.
  - CHW **accepts or overrides**; overrides are logged in AuditLog.
- **Cloud AI**: Not used for this app. No patient data sent for ML. (Optional future: anonymized aggregates only, no PHI.)

---

## 7. Patient Data Privacy

| Measure | Implementation |
|---------|----------------|
| **Data at rest** | DB encrypted with SQLCipher; key in Keystore, tied to user auth (PIN/biometric). |
| **Data in transit** | HTTPS only; certificate pinning optional for Sync API. |
| **Minimal PHI** | Only fields needed for care: name, DOB, sex, village, phone/caregiver; no national ID unless required by policy. |
| **On-device only** | Voice and AI run locally; no PHI sent to cloud for inference. |
| **Sync** | Sync sends only to authenticated Sync API; tokens are session/device-bound; server must be trusted and compliant. |
| **Access control** | App unlock required; single CHW login per device (or profile) so only that CHW sees their local data. |
| **Audit** | All access and actions logged (AuditLog) and synced for accountability. |

---

## 8. Clinical Guidelines Updates

- **Baseline**: App ships with a **bundled** set of guidelines (e.g. WHO IMCI subset) so the app works offline from first install.
- **Updates** (when online):
  - **Versioned** on server (e.g. by category + version number or content hash).
  - App periodically (or on launch) calls a **guidelines endpoint** (no PHI): “what’s latest for categories X, Y, Z?”.
  - If newer version exists, app **downloads** and **replaces** local guideline for that category.
  - **Verification**: Optional signature or hash check so only trusted guideline packages are applied.
- **No conflict with visits**: Guidelines are read-only reference data; visits reference “guideline version at time of visit” if needed for audit.

---

## 9. Summary Table

| Concern | Approach |
|---------|----------|
| **Local data models** | Patient, Visit, AuditLog, SyncQueueItem, GuidelineVersion; UUIDs, JSON for flexible fields. |
| **Encrypted storage** | SQLCipher + Keystore; optional Encrypted SharedPreferences for config. |
| **Sync** | Eventual consistency; queue by priority; upload when online; optional guideline pull. |
| **Conflict resolution** | LWW for Patient; append/merge-by-rule for Visit (safety-first); append-only for AuditLog; server wins for Guidelines. |
| **Offline for days** | No dependency on network for core workflow; queue and drain when back. |
| **Voice** | On-device only (e.g. Vosk); no storage/upload of raw audio. |
| **AI** | On-device, rule-based, guideline-driven; no cloud AI; no PHI off device for ML. |
| **Privacy** | Encrypt at rest; HTTPS; minimal PHI; sync to trusted backend only; audit trail. |
| **Guidelines** | Bundled baseline + versioned updates when online; optional integrity check. |

This gives you a **mobile-first, offline-first** architecture that meets the requirements and keeps the design consistent with the existing workflow and Android codebase.
