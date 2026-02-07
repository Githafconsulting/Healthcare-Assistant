# CHW Visit Workflow

## Context (Design Inputs)

| Factor | Implication |
|--------|-------------|
| **Rural African setting** | Intermittent connectivity → offline-first; sync when back in range |
| **20–40 patients/day** | Visit must be 5–10 min; minimal taps; no long forms |
| **Low literacy patients** | Caregiver may not read; CHW speaks and interprets; voice + simple words |
| **CHW: local language + basic English** | Voice capture in local language or English; UI labels simple; future: multi-language |
| **Entry-level Android phone** | Large tap targets; minimal animations; small payloads; voice optional fallback to tap |

These drive: offline-first, voice-first, minimal manual input, large buttons, and sync-only-when-online.

---

## Requirements Checklist

| Requirement | Where in workflow | Implemented |
|-------------|-------------------|-------------|
| Offline patient identification or creation | Step 1: Find/Create Patient | Search + create; all local |
| Voice-based visit capture | Step 3: Capture | Voice → transcript → symptom extraction |
| Minimal manual input | Steps 1–6 | Voice primary; tap chips fallback; 5 screens only |
| AI-assisted symptom clarification | Step 3 + 4 | Extract from voice; danger sign prompt; suggest treatments |
| Safe clinical guidance (not diagnosis) | Step 4–5 | Suggestions with “Consider”, guideline ref, CHW must accept/skip |
| Treatment plan creation | Step 5 | Accept/skip per suggestion; summary before complete |
| Follow-up scheduling | Step 6 | 2 days / 5 days / No follow-up |
| Sync when connectivity returns | After Step 6 | Pending count in header; auto sync when online |

---

## Overview

**Goal**: Complete a patient visit in 5–10 minutes with minimal typing.

**Constraints**:
- Works 100% offline
- One-hand operation possible
- Large tap targets (entry-level phone)
- Voice-first data capture
- CHW confirms everything

---

## The 6-Step Visit Flow

```
┌─────────────────────────────────────────────────────────────┐
│  1. FIND/CREATE PATIENT  →  2. START VISIT  →  3. CAPTURE  │
│                                                             │
│  4. REVIEW SUGGESTIONS  →  5. DECIDE & TREAT  →  6. CLOSE  │
└─────────────────────────────────────────────────────────────┘
```

---

## Step 1: Find or Create Patient (30 seconds)

### What CHW Does
- Opens app → sees "New Visit" button (large, green)
- Taps → search screen appears
- **Option A**: Types first few letters of name OR
- **Option B**: Taps microphone, says "Maria Okonkwo"

### What AI Does
- Voice → converts to text, shows matches
- Shows recent patients first (likely repeat visits)
- If no match: pre-fills "Create New" form with spoken name

### What CHW Confirms
- Taps correct patient from list, OR
- For new patient: confirms name, enters DOB (date picker), sex (2 buttons), village (dropdown of known villages)

### Stored Locally
```
Patient {
  id: auto-generated
  name: "Maria Okonkwo"
  dateOfBirth: 2022-03-15
  sex: FEMALE
  village: "Kijiji"
  synced: false  ← will sync later
}
```

### Failure Modes
| Problem | Fallback |
|---------|----------|
| Voice not working | Type name manually |
| Duplicate patient | Show "Similar patients" warning |
| Wrong patient selected | Back button, search again |

---

## Step 2: Start Visit (10 seconds)

### What CHW Does
- Taps "Start Visit" on patient screen
- Visit timer begins (shown subtly)

### What AI Does
- Creates visit record
- Loads patient history (shows last visit summary if exists)
- Pre-loads danger sign checklist

### What CHW Sees
```
┌─────────────────────────────────┐
│ Maria Okonkwo, 2 years          │
│ Last visit: 2 weeks ago         │
│ Previous: Diarrhea, given ORS   │
├─────────────────────────────────┤
│                                 │
│   🎤 TAP TO DESCRIBE PROBLEM    │
│       (large button)            │
│                                 │
│   or tap symptoms below:        │
│   [Fever] [Cough] [Diarrhea]    │
│   [Vomiting] [Not eating]       │
│                                 │
└─────────────────────────────────┘
```

### Stored Locally
```
Visit {
  id: auto-generated
  patientId: "..."
  chwId: "CHW-123"
  startTime: now
  synced: false
}

AuditEntry { action: "visit_started", ... }
```

---

## Step 3: Capture Symptoms (2-3 minutes)

### What CHW Does

**Primary: Voice capture**
- Taps big microphone button
- Speaks naturally: "The child has had fever for 3 days and diarrhea since yesterday. Mother says she is not drinking well."
- Sees text appear on screen
- Taps "Done" when finished

**Secondary: Tap symptoms**
- Taps symptom chips to add/confirm
- Each symptom shows optional duration picker

### What AI Does

1. **Transcribes speech** (Vosk, offline)
2. **Extracts symptoms** from text:
   ```
   "fever for 3 days" → Symptom(fever, duration: "3 days")
   "diarrhea since yesterday" → Symptom(diarrhea, duration: "1 day")
   "not drinking well" → Symptom(poor_intake, severity: concerning)
   ```
3. **Highlights for confirmation**:
   ```
   Found:
   ✓ Fever - 3 days
   ✓ Diarrhea - 1 day  
   ⚠️ NOT DRINKING WELL ← danger sign flag
   ```

4. **IMMEDIATELY checks danger signs** when "not drinking" detected:
   ```
   ┌─────────────────────────────────────┐
   │ ⚠️ POSSIBLE DANGER SIGN             │
   │                                     │
   │ "Not drinking well" detected.       │
   │                                     │
   │ Please check:                       │
   │ Can the child drink or breastfeed?  │
   │                                     │
   │ [YES, can drink]  [NO, cannot]      │
   └─────────────────────────────────────┘
   ```

### What CHW Confirms
- Reviews extracted symptoms
- Taps to edit/remove incorrect ones
- **Must respond** to danger sign question
- Adds vitals if taken (temperature, MUAC)

### Stored Locally
```
Visit.symptoms: [
  {name: "fever", present: true, duration: "3 days"},
  {name: "diarrhea", present: true, duration: "1 day"},
  {name: "poor intake", present: true}
]
Visit.dangerSigns: []  // or ["unable_to_drink"] if confirmed
```

### Failure Modes
| Problem | Fallback |
|---------|----------|
| Voice unclear | Show "Didn't catch that, try again" + manual entry |
| Wrong symptom extracted | CHW taps X to remove |
| CHW skips danger sign check | Cannot proceed until answered |

---

## Step 4: Review AI Suggestions (30 seconds)

### What AI Does

Runs `DecisionSupport.evaluate()`:
1. Checks all danger signs
2. Matches symptoms to treatments
3. Returns prioritized suggestions

### What CHW Sees

```
┌─────────────────────────────────────┐
│ SUGGESTIONS                         │
├─────────────────────────────────────┤
│ 🔴 HIGH PRIORITY                    │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ Consider: ORS for Diarrhea      │ │
│ │                                 │ │
│ │ WHY: Child has diarrhea (1 day) │ │
│ │ GUIDELINE: WHO IMCI Plan A      │ │
│ │                                 │ │
│ │ Dose (age 2): 100ml after each  │ │
│ │ loose stool                     │ │
│ │                                 │ │
│ │ [✓ GIVE ORS]  [✗ Skip - why?]   │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ Consider: Zinc with ORS         │ │
│ │ ...                             │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ Consider: Paracetamol for Fever │ │
│ │ ...                             │ │
│ └─────────────────────────────────┘ │
└─────────────────────────────────────┘
```

### If Danger Sign Confirmed

```
┌─────────────────────────────────────┐
│ 🚨 DANGER SIGN DETECTED             │
│                                     │
│ Child CANNOT drink/breastfeed       │
│                                     │
│ ACTION REQUIRED:                    │
│ Refer URGENTLY to health facility   │
│                                     │
│ Before referral:                    │
│ • Give first dose ORS if possible   │
│ • Keep child warm                   │
│ • Arrange transport                 │
│                                     │
│ Nearest facility: Kijiji Health Ctr │
│ Distance: 5 km                      │
│                                     │
│ [CREATE REFERRAL]                   │
└─────────────────────────────────────┘
```

### What CHW Confirms
- For each suggestion: Accept or Skip (with reason)
- If skipping, must tap reason: "Already given", "Not available", "Patient refused", "Other"

---

## Step 5: Decide & Treat (1-2 minutes)

### What CHW Does

**For accepted treatments:**
- Gives medication/ORS
- App shows dosing reminder
- Taps "Given" to confirm

**For referral:**
- Taps "Create Referral"
- Selects facility (dropdown)
- Selects urgency (Emergency/Urgent/Routine)
- App generates referral summary

### What AI Does
- Records CHW decisions
- Logs any overrides (for quality review)
- Does NOT prevent CHW from any action

### What CHW Sees After Decisions
```
┌─────────────────────────────────────┐
│ TREATMENT PLAN                      │
├─────────────────────────────────────┤
│ ✓ ORS - 100ml after loose stool    │
│ ✓ Zinc - 20mg daily for 10 days    │
│ ✓ Paracetamol - 125mg if fever     │
│                                     │
│ ✗ Referral - not needed            │
├─────────────────────────────────────┤
│ TELL CAREGIVER:                     │
│ • Give ORS after each loose stool   │
│ • Continue breastfeeding            │
│ • Return if: not drinking, blood    │
│   in stool, fever continues 3 days  │
└─────────────────────────────────────┘
```

### Stored Locally
```
Visit.treatment: "ORS 100ml per stool, Zinc 20mg x10d, Paracetamol PRN"
Visit.referral: null  // or Referral object if created

AuditEntry { action: "suggestion_accepted", entityId: "suggestion-1" }
AuditEntry { action: "suggestion_accepted", entityId: "suggestion-2" }
```

---

## Step 6: Close Visit (30 seconds)

### What CHW Does
- Reviews summary
- Sets follow-up (optional): "Return in 2 days" / "Return in 5 days" / "No follow-up"
- Taps "Complete Visit"

### What AI Does
- Validates visit is complete (has at least: 1 symptom, 1 decision)
- Saves final visit
- Queues for sync
- Returns to home screen

### What CHW Sees
```
┌─────────────────────────────────────┐
│ ✓ VISIT COMPLETE                    │
│                                     │
│ Maria Okonkwo                       │
│ Duration: 4 minutes                 │
│                                     │
│ Follow-up: Return in 2 days         │
│                                     │
│ ⏳ Will sync when online            │
│                                     │
│ [NEW VISIT]  [VIEW PATIENT]         │
└─────────────────────────────────────┘
```

### Stored Locally
```
Visit {
  ...
  endTime: now
  assessment: "Diarrhea, no dehydration. Fever."
  treatment: "ORS, Zinc, Paracetamol"
  followUp: now + 2 days
  synced: false
}

AuditEntry { action: "visit_completed", ... }
```

---

## Sync Behavior

### When Offline (Normal State)
- All data stored in local SQLite
- Sync icon shows: `⏳ 3 pending`
- CHW workflow unchanged

### When Connection Detected
```
SyncManager runs automatically:
1. Upload patients (oldest first)
2. Upload visits (with danger signs first)
3. Upload audit logs (batch)
4. Update synced flags
```

### What Gets Synced

| Data | Priority | Why |
|------|----------|-----|
| Visits with danger signs | HIGH | Safety monitoring |
| Visits with referrals | HIGH | Facility needs heads-up |
| Regular visits | NORMAL | Completeness |
| Patient records | NORMAL | Master data |
| Audit logs | LOW | Batch OK |

### Sync UI
```
┌─────────────────────────────────────┐
│ 📶 Connection detected              │
│                                     │
│ Syncing... 3/5                      │
│ ████████░░░░░░░░                    │
│                                     │
│ (continues in background)           │
└─────────────────────────────────────┘
```

### Sync Failure
- Retry automatically (exponential backoff)
- Data never lost (stays local)
- CHW sees: `⚠️ Sync pending (retry in 5 min)`

---

## Failure Modes Summary

| Failure | Impact | Fallback |
|---------|--------|----------|
| No network | None | Works offline |
| Voice fails | Minor | Type manually |
| Battery dies | Data safe | SQLite persists |
| App crashes | Minimal | Visit recoverable from draft |
| Wrong patient | Fixable | Can edit before sync |
| Wrong symptom | Fixable | CHW reviews all extractions |
| CHW overrides AI | Logged | Audit trail for review |

---

## Data Flow Summary

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   CHW       │────▶│   LOCAL     │────▶│   SERVER    │
│   Input     │     │   SQLite    │     │   (when     │
│             │     │             │     │   online)   │
└─────────────┘     └─────────────┘     └─────────────┘
                          │
                          ▼
                    ┌─────────────┐
                    │  Decision   │
                    │  Support    │
                    │  (offline)  │
                    └─────────────┘
```

### Local Only (Never Synced)
- Voice audio files (deleted after transcription)
- Draft visits (before completion)

### Synced
- Completed visits
- Patients
- Audit logs

---

## Screen Count: 5

1. **Home** - New Visit button, pending sync count
2. **Patient Search/Create** - Find or add patient
3. **Capture** - Voice + symptom chips + vitals
4. **Suggestions** - AI recommendations + decisions
5. **Summary** - Review + follow-up + complete

That's it. No settings screens, no dashboards, no reports (for CHW).

---

## 1. Step-by-Step CHW Workflow (Summary)

| Step | CHW action | Time |
|------|------------|------|
| 1 | Open app → New Visit → search name or speak name → tap patient or Create New (name, DOB, sex, village) | ~30 s |
| 2 | Tap Start Visit | ~10 s |
| 3 | Tap mic, describe problem in own words; or tap symptom chips. Confirm danger sign if asked. Optionally add temp/MUAC | 2–3 min |
| 4 | Read each suggestion (reason + guideline). For each: Accept or Skip (choose reason) | ~30 s |
| 5 | Give accepted treatments; if referral, create referral (facility, urgency) | 1–2 min |
| 6 | Set follow-up (2 days / 5 days / none) → Complete Visit | ~30 s |

**Total: 5–10 minutes per visit.**

---

## 2. What the AI Does at Each Step

| Step | AI / system action |
|------|--------------------|
| 1 | Converts voice to text; shows patient matches; pre-fills name if new |
| 2 | Creates visit; loads last visit summary if any |
| 3 | Transcribes voice; extracts symptoms from text; flags “not drinking” etc.; asks “Can child drink?”; suggests vitals to enter |
| 4 | Evaluates symptoms + vitals; lists danger signs first; suggests treatments with dose + guideline; does **not** diagnose |
| 5 | Records accept/skip per suggestion; logs overrides; does **not** block any CHW action |
| 6 | Validates visit; saves; queues for sync; shows “Will sync when online” |

---

## 3. What the CHW Confirms or Overrides

| Point | CHW must confirm / can override |
|-------|---------------------------------|
| Patient | Correct patient from list, or confirm new patient details |
| Symptoms | Review extracted list; remove wrong; add missed; answer danger-sign question |
| Each suggestion | Accept or Skip; if Skip, choose reason (Already given, Not available, Patient refused, Not needed) |
| Referral | Create referral and set urgency; can choose not to refer despite suggestion |
| Follow-up | Choose 2 days, 5 days, or No follow-up |
| Complete | Tap Complete Visit only after review |

**AI never acts alone; every clinical action is CHW-confirmed.**

---

## 4. What Is Stored Locally vs Synced

| Data | Stored locally | Synced when online |
|------|----------------|--------------------|
| Patients | Yes (SQLite / web: localStorage) | Yes |
| Visits (complete) | Yes | Yes (danger/referral first) |
| Audit logs | Yes | Yes (batched) |
| Voice audio | No (discarded after transcript) | No |
| Draft visit (in progress) | In memory only | No |

**Offline:** All workflow works; data stays on device. **Online:** Sync runs in background; “pending” count decreases.

---

## 5. Failure Modes and Fallbacks

| Failure | Impact | Fallback |
|---------|--------|----------|
| No network | None | Full workflow offline; sync when back |
| Voice not available / unclear | Slower capture | Tap symptom chips; type name |
| Wrong symptom extracted | Possible wrong suggestions | CHW removes wrong symptom before Continue |
| CHW skips danger sign question | Unsafe to proceed | App does not allow Continue until answered (or “No danger”) |
| Battery dies mid-visit | Current visit may be lost | Draft not saved; completed visits already saved |
| Wrong patient selected | Wrong record | Back, search again before completing |
| CHW disagrees with AI | Correct | CHW skips with reason; logged for review |
| Sync fails | Data still on device | Retry later; “pending” shown |

---

## Web App Alignment

The `webapp/` (single-page app) implements this workflow for demo/testing:

| Workflow element | Web app implementation |
|------------------|-------------------------|
| Offline | Uses localStorage; no server required to run |
| Step 1 | Search box + Create New (name, DOB, sex, village) |
| Step 2 | Selecting patient goes straight to Capture |
| Step 3 | Voice (browser Web Speech API if available); symptom chips; danger sign prompt for “Not eating” |
| Step 4 | Suggestion cards with Accept / Skip (reason modal) |
| Step 5 | Accepted treatments shown in summary |
| Step 6 | Follow-up buttons (2 / 5 days / none); Complete Visit |
| Sync | “Pending” count in header; real sync requires backend (Android app has SyncManager) |
| Local vs synced | All in localStorage; “sync” is simulated (count only) |

**Differences from full Android design:** Web uses Web Speech API (online in Chrome) not Vosk; localStorage not SQLite; no real sync to server. Same workflow and UX intent.
