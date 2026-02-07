# Full CHW Visit — Visual Walkthrough

A screen-by-screen walkthrough of one complete visit in the Afya Assistant app. **Scenario:** New patient; suspected malaria; offline environment; follow-up required. Aligns with the [6-step workflow](CHW_WORKFLOW.md), [design system](CHW_WEBAPP_DESIGN_SYSTEM.md), and all CHW screen design specs.

---

## Scenario Summary

| Factor | Detail |
|--------|--------|
| **Patient** | New (not in system); child, ~2 years |
| **Presentation** | Fever 3 days; no danger signs; RDT available (or presumptive per national) → malaria pathway |
| **Environment** | Offline (no network during visit) |
| **Outcome** | Treatment at community level (ACT + paracetamol); 2-day follow-up for recheck |

**Path:** Dashboard → Create patient → Start visit → Voice capture → Review notes → AI guidance → Accept treatments → Treatment plan & follow-up → Complete → Dashboard.

---

## 1. Dashboard (Home)

### Screen

```
┌──────────────────────────────────────────────────────────────┐
│  Afya Assistant                    [●] Saved on device       │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌────────────────────────────────────────────────────────┐│
│  │  ＋  New visit                                           ││
│  └────────────────────────────────────────────────────────┘│
│                                                              │
│  ┌────────────────────────────────────────────────────────┐│
│  │  Saved on device · 0 visits to sync                      ││
│  └────────────────────────────────────────────────────────┘│
│                                                              │
│  FOLLOW-UPS DUE TODAY                                        │
│  ┌────────────────────────────────────────────────────────┐│
│  │  No follow-ups due today.                               ││
│  └────────────────────────────────────────────────────────┘│
│                                                              │
│  TODAY'S PATIENTS                                            │
│  ┌────────────────────────────────────────────────────────┐│
│  │  No visits yet today. Start with New visit.             ││
│  └────────────────────────────────────────────────────────┘│
│                                                              │
└──────────────────────────────────────────────────────────────┘
│  [Home] [Visits] [Follow-ups] [Patients] [Sync]              │
└──────────────────────────────────────────────────────────────┘
```

### What the CHW sees

- **Header:** App name; status **“Saved on device”** with muted icon (offline). No red or alarm.
- **Primary CTA:** Single full-width **“New visit”** button (only solid primary action on screen).
- **Status bar:** One line: “Saved on device · 0 visits to sync” — calm, informative; tap → Sync tab.
- **Sections:** Follow-ups due today (empty state); Today’s patients (empty: “No visits yet today”).

### Design intent

- **Trust:** Offline is stated plainly; “Saved on device” and “0 visits to sync” show nothing is lost.
- **Clarity:** One obvious next step — “New visit.” No competing buttons.
- **Calm:** Empty states are short and positive; no “You must sync” or blocking message.

---

## 2. Find or Create Patient (New Patient)

### Screen (after tapping “New visit”)

```
┌──────────────────────────────────────────────────────────────┐
│  [←]  New visit                    [●] Saved on device     │
├──────────────────────────────────────────────────────────────┤
│  Find patient                                                 │
│  [ Search by name or village                          ]  🎤  │
│                                                              │
│  Or say the name: tap mic and speak.                         │
│                                                              │
│  No matches yet. Add new patient.                             │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  ＋  Add new patient                                    │  │
│  └────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────┘
```

**Alternative:** CHW taps mic and says “Maria Okonkwo” → search runs (local); no match → create flow with name pre-filled.

### Screen (Add new patient — form)

```
┌──────────────────────────────────────────────────────────────┐
│  [←]  New patient                   [●] Saved on device     │
├──────────────────────────────────────────────────────────────┤
│  Name *          [ Maria Okonkwo                      ]      │
│  Date of birth * [ Pick date  ]   e.g. 15 Mar 2022           │
│  Sex *           [  Girl  ] [  Boy  ]                        │
│  Village / area  [ Kijiji        ▼ ]                         │
│  (Optional)      [ Caregiver phone                    ]      │
│                                                              │
│  [  Save patient  ]                                          │
└──────────────────────────────────────────────────────────────┘
```

### What the CHW does

- Taps “New visit” → lands on Find patient.
- For new patient: taps “Add new patient” (or voice “Maria Okonkwo” and then “Add new” when no match).
- Fills name, DOB, sex, village; taps “Save patient.” Record stored locally.

### Design intent

- **Minimal input:** Only essential fields; large tap targets; date picker and two buttons for sex.
- **Offline:** All data saved on device; no “connecting” or “saving…” that implies network.
- **Voice option:** Search and name entry can be voice-first; fallback to type.

---

## 3. Patient Selected — Start Visit

### Screen (after saving new patient or selecting existing)

```
┌──────────────────────────────────────────────────────────────┐
│  [←]  Maria Okonkwo                 [●] Saved on device     │
├──────────────────────────────────────────────────────────────┤
│  Maria Okonkwo · 2 years                                      │
│  Village: Kijiji                                              │
│  (New patient — no previous visits)                            │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  Start visit                                            │  │
│  └────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────┘
```

### What the CHW does

- Taps **“Start visit”**. Visit record created; timer starts (subtle); app navigates to Capture.

### Design intent

- **Confirmation of who:** Patient name and age prominent so the CHW never doubts who the visit is for.
- **Single next action:** One primary button; no extra steps before capture.

---

## 4. Voice Capture (Capture Notes)

### Screen (idle)

```
┌──────────────────────────────────────────────────────────────┐
│  [←]  Capture notes                 [●] Saved on device     │
├──────────────────────────────────────────────────────────────┤
│  Patient: Maria Okonkwo · 2 years                            │
│  ─────────────────────────────────────────────────────────   │
│                                                              │
│           ┌─────────────────────┐                             │
│           │       [  🎤  ]     │                             │
│           └─────────────────────┘                             │
│                                                              │
│  Tap to speak                                                │
│                                                              │
│  Or tap a symptom:                                           │
│  [Fever] [Cough] [Diarrhea] [Vomiting] [Not eating]          │
│                                                              │
│  [  Continue  ]   (enabled when something captured)          │
└──────────────────────────────────────────────────────────────┘
```

### Screen (while speaking — listening)

```
│  … same header and patient line …                             │
│           ┌─────────────────────┐                             │
│           │   [  🎤  ]  ⟳      │   ← Pulsing ring (calm)    │
│           └─────────────────────┘                             │
│  Listening…                                                   │
│  ┌──────────────────────────────────────────────────────────┐│
│  │  "Fever three days, mother says not drinking well…"     ││  ← 1–2 lines
│  └──────────────────────────────────────────────────────────┘│
│  [Fever] [Cough] [Diarrhea] …                                 │
│  [  Continue  ]                                               │
```

### What the CHW does

- Taps mic; says: *“Child has fever for three days. Mother says she is not drinking well.”*
- Sees “Listening…” and short live transcript. Taps mic again to stop (or Continue when done).
- If “not drinking well” is detected, app may prompt: “Possible danger sign — Can the child drink or breastfeed?” [Yes] [No]. CHW answers (in this scenario: Yes, so no danger sign).
- Taps **“Continue”** to go to review.

### Design intent

- **Voice-first:** One large mic button; state always clear (Tap to speak / Listening…).
- **No data loss fear:** “Saved on device” in header; no “upload” or “connecting.”
- **Short preview only:** 1–2 lines of “what we heard”; no long scrollable block.
- **Fallback:** Symptom chips if voice isn’t used or to add more.

---

## 5. Review Notes (Check What We Understood)

### Screen

```
┌──────────────────────────────────────────────────────────────┐
│  [←]  Check what we understood      [●] Saved on device     │
├──────────────────────────────────────────────────────────────┤
│  We wrote this from what you said. Fix anything wrong.      │
│                                                              │
│  Chief complaint                                              │
│  Fever for 3 days, not drinking well            [High ✓]     │
│  [ Tap to edit ]                                             │
│                                                              │
│  Symptoms                                                     │
│  Fever · 3 days                                 [High] [Edit] │
│  Not drinking well                              [Med]  [Edit] │
│  [ + Add symptom ]                                           │
│                                                              │
│  Red flags                                                    │
│  (None) or e.g. Not drinking well               [Edit]        │
│                                                              │
│  Notes                                                        │
│  "Fever three days, mother says not drinking…"   [Edit]      │
│                                                              │
│  [  Continue to suggestions  ]                               │
└──────────────────────────────────────────────────────────────┘
```

### What the CHW sees and does

- **Helper tone:** One line: “We wrote this from what you said. Fix anything wrong.”
- **Structured preview:** Chief complaint, symptoms (with duration), red flags, notes; each with confidence (High/Med) and **one-tap edit**.
- CHW can tap Edit to fix anything, then taps **“Continue to suggestions.”**

### Design intent

- **AI as helper:** “Fix anything wrong” — not “Correct errors.” Confidence badges (High/Med) show what to double-check without blame.
- **Easy correction:** Every field has Edit; no modals for simple fixes.
- **Single next step:** One primary button to move to AI guidance.

---

## 6. AI Guidance (Review Suggestions)

### Screen (malaria pathway — no danger signs; fever, possible malaria)

Suggestions ordered by risk: routine treatment first (no critical referral in this scenario).

```
┌──────────────────────────────────────────────────────────────┐
│  [←]  Suggestions                   [●] Saved on device     │
├──────────────────────────────────────────────────────────────┤
│  Based on what you told us. You decide.                      │
│                                                              │
│  ┌─ [Routine] ───────────────────────── From: WHO malaria ─┐│
│  │  Consider treating for malaria                           ││
│  │  What to do: Do RDT if available. If positive, give ACT  ││
│  │  per weight. Give paracetamol for fever.                  ││
│  │  Why: Child has fever; no danger signs.                  ││
│  │  Based on: Fever · 3 days · No danger signs        [⋮]   ││
│  │  [  I did this  ]   [  Skip  ]                            ││
│  └──────────────────────────────────────────────────────────┘│
│                                                              │
│  ┌─ [OK] ───────────────────────────── From: WHO IMCI ──────┐│
│  │  Counsel on when to return                                ││
│  │  What to do: Tell caregiver danger signs; return if worse. ││
│  │  Why: Part of fever care.                                 ││
│  │  [  I did this  ]   [  Skip  ]                            ││
│  └──────────────────────────────────────────────────────────┘│
│                                                              │
│  [  Continue to treatment plan  ]                            │
└──────────────────────────────────────────────────────────────┘
```

### What the CHW does

- Reads first card (malaria: RDT, ACT, paracetamol); taps **“I did this”** (e.g. gave ACT and paracetamol; RDT done or skipped per national protocol).
- Reads second card (counsel); taps **“I did this”** or **“Skip”**.
- Taps **“Continue to treatment plan.”**

### Design intent

- **Suggest, not instruct:** “Consider treating”; “You decide.” No “Diagnosis: malaria.”
- **Explain why:** Every card has “Why” and “Based on”; source “From: WHO malaria.”
- **Confirmation required:** No action recorded until “I did this” or “Skip.” Risk badges (Routine, OK) are calm; no red unless critical referral.
- **One primary action per card:** “I did this” primary; “Skip” secondary.

---

## 7. Treatment Plan & Follow-up Scheduling

### Screen (treatment plan generated from accepted suggestions)

```
┌──────────────────────────────────────────────────────────────┐
│  [←]  Treatment plan               [●] Saved on device     │
├──────────────────────────────────────────────────────────────┤
│  Maria Okonkwo · 2 years                                      │
│  Started today · Active                                       │
│                                                              │
│  WHAT TO DO                                                  │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  1  [✓]  Give ACT today                    Done         │  │
│  │      Per weight. First dose given.                     │  │
│  ├────────────────────────────────────────────────────────┤  │
│  │  2  [ ]  [💊]  Paracetamol for fever                    │  │
│  │      As needed for fever. Max 4 times a day.            │  │
│  ├────────────────────────────────────────────────────────┤  │
│  │  3  [ ]  [💬]  Counsel when to return                   │  │
│  └────────────────────────────────────────────────────────┘  │
│                                                              │
│  MEDICATIONS                                                 │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  [💊] ACT · For malaria                                 │  │
│  │  Given per weight. Day 1 of 3.                         │  │
│  ├────────────────────────────────────────────────────────┤  │
│  │  [💊] Paracetamol · For fever                           │  │
│  │  As needed; max 4 times a day.                         │  │
│  └────────────────────────────────────────────────────────┘  │
│                                                              │
│  FOLLOW-UP                                                   │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  [📅]  When should they come back?                     │  │
│  │  [  In 2 days  ] [  In 5 days  ] [  No follow-up  ]    │  │
│  │  (Recheck fever)                                       │  │
│  └────────────────────────────────────────────────────────┘  │
│                                                              │
│  [  Complete visit  ]                                        │
└──────────────────────────────────────────────────────────────┘
```

### What the CHW does

- Reviews steps and medications; marks “Give ACT today” as done if already given.
- In **Follow-up**, taps **“In 2 days”** (recheck fever). Due date set (e.g. Wed 5 Feb).
- Taps **“Complete visit.”**

### Design intent

- **Plain language, icon-led:** Each step has icon + short title; medications with simple dosing.
- **Visual completion:** Checkmarks for done steps; success color; “Done” label.
- **Follow-up explicit:** Clear choices (2 days / 5 days / None) with reason (Recheck fever); no free-text required.
- **Single primary action:** “Complete visit” after setting follow-up.

---

## 8. Summary Before Complete (Optional Step)

### Screen (if product shows summary before closing visit)

```
┌──────────────────────────────────────────────────────────────┐
│  [←]  Complete visit                [●] Saved on device     │
├──────────────────────────────────────────────────────────────┤
│  Summary                                                      │
│  You did: ACT, paracetamol, counsel.                         │
│  Follow-up: In 2 days (recheck fever).                        │
│                                                              │
│  You are responsible for the final decision. This tool only   │
│  suggests actions based on guidelines.                        │
│                                                              │
│  [  Complete visit  ]                                         │
└──────────────────────────────────────────────────────────────┘
```

### Design intent

- **Reassurance:** Short summary of what was done and when to follow up.
- **Responsibility statement:** One line so the CHW knows the tool supports, not replaces, their judgment.

---

## 9. Visit Complete — Return to Dashboard

### Screen (Home, after completing visit)

```
┌──────────────────────────────────────────────────────────────┐
│  Afya Assistant                    [●] Saved on device       │
├──────────────────────────────────────────────────────────────┤
│  ┌────────────────────────────────────────────────────────┐  │
│  │  ＋  New visit                                         │  │
│  └────────────────────────────────────────────────────────┘  │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  Saved on device · 1 visit to sync                     │  │  ← Pending count
│  └────────────────────────────────────────────────────────┘  │
│                                                              │
│  FOLLOW-UPS DUE TODAY                                        │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  No follow-ups due today.                              │  │
│  └────────────────────────────────────────────────────────┘  │
│                                                              │
│  TODAY'S PATIENTS                                             │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  Maria Okonkwo    Visit just now · Fever, ACT given    │  │
│  └────────────────────────────────────────────────────────┘  │
│  [ See all visits ]                                          │
│                                                              │
└──────────────────────────────────────────────────────────────┘
│  [Home] [Visits] [Follow-ups] [Patients] [Sync]              │
└──────────────────────────────────────────────────────────────┘
```

### What the CHW sees

- **Status:** Still “Saved on device”; now **“1 visit to sync”** — passive, not alarming; tap opens Sync tab.
- **Today’s patients:** Maria Okonkwo appears with “Visit just now · Fever, ACT given.”
- **Next:** CHW can start another “New visit” or tap Maria for profile/treatment plan.

### Design intent

- **Trust:** “1 visit to sync” confirms data is stored and will sync when online; no fear of loss.
- **Continuity:** Today’s list gives quick recall of who was seen; tap to open plan or follow-up.
- **No alert fatigue:** No pop-up “Visit saved!”; status in header and list is enough.

---

## 10. Follow-up Later (Same Flow, Different Entry)

When the CHW returns for the 2-day follow-up:

- **Home** or **Follow-ups** tab shows: “Maria Okonkwo — Recheck fever — Due today.”
- CHW taps → can open **treatment plan** and tap **“Mark follow-up done”** after seeing the child, or start a new visit for the same patient and document the recheck.
- Reminder status (if reminders are sent) appears on the plan: “Reminder sent” / “Patient replied” (per treatment plan and offline/sync indicator specs).

---

## Flow Summary (Screen-by-Screen)

| # | Screen | Design intent in one line |
|---|--------|----------------------------|
| 1 | **Dashboard** | One primary action (New visit); offline status calm and visible; build trust. |
| 2 | **Find/Create patient** | Minimal form; voice or type; save locally; no network needed. |
| 3 | **Patient — Start visit** | Confirm who; one tap to start; visit created locally. |
| 4 | **Voice capture** | Voice-first; clear Listening state; short transcript; “Saved on device”; chips fallback. |
| 5 | **Review notes** | Helper tone; structured preview; confidence + one-tap edit; no blame. |
| 6 | **AI guidance** | Suggest not instruct; explain why; I did this / Skip; source refs; one CTA per card. |
| 7 | **Treatment plan & follow-up** | Steps + meds in plain language; 2/5 days follow-up; complete visit. |
| 8 | **Summary (optional)** | Short recap + responsibility statement before complete. |
| 9 | **Dashboard (return)** | “1 visit to sync”; Maria in Today’s patients; no noisy alerts. |

---

## Cross-Cutting Design Intent

| Theme | How it shows in this walkthrough |
|-------|----------------------------------|
| **Offline** | “Saved on device” on every screen; no blocking; capture and save work without network. |
| **Trust / no data loss fear** | Status bar and header state clearly that data is stored and will sync; “1 visit to sync” after complete. |
| **Passive, not noisy** | No pop-ups for “Visit saved”; status always in header; optional one-time “Synced” later when online. |
| **Voice-first** | Capture is mic-led; optional voice for patient search; chips as fallback. |
| **AI as helper** | “We wrote this from what you said”; “Consider…”; “You decide”; I did this / Skip; no diagnosis. |
| **One primary action per screen** | New visit → Add patient / Start visit → Continue → Continue to suggestions → I did this → In 2 days → Complete visit. |
| **Follow-up explicit** | 2 days / 5 days / None with reason; due date on plan; later visible in Follow-ups and Today. |

This walkthrough ties together **dashboard → patient creation → voice capture → AI guidance → treatment plan → follow-up scheduling → return to dashboard** with screen-by-screen descriptions and design intent at each step.
