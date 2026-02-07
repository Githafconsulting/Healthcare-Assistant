# Voice Capture & Visit Note Review Interface — Design Specification

Design for the CHW voice capture and structured-note review flow. **Goals:** CHW speaks more than they type; AI feels like a **helper**, not a judge; errors are **easy to fix**. Aligns with [voice-to-structured-notes](VOICE_TO_STRUCTURED_NOTES.md) and [design system](CHW_WEBAPP_DESIGN_SYSTEM.md).

---

## 1. Recording UI Description

### 1.1 Layout (single column, minimal)

```
┌──────────────────────────────────────────────────────────┐
│  [←]  Capture notes                          [Offline ●] │
├──────────────────────────────────────────────────────────┤
│                                                          │
│  Patient: Maria Okonkwo · 2 years                        │
│  ─────────────────────────────────────────────────────   │
│                                                          │
│           ┌─────────────────────┐                       │
│           │                     │                       │
│           │    [  🎤  ]         │  ← Main control        │
│           │                     │     (min 80×80 px)    │
│           └─────────────────────┘                       │
│                                                          │
│  [  Listening…  ]  or  [  Tap to speak  ]                │  ← State label (icon + text)
│                                                          │
│  ┌─ What we heard (optional, short) ───────────────────┐│
│  │  "Fever three days, cough…"                          ││  ← 1–2 lines max; no long block
│  └────────────────────────────────────────────────────┘ │
│                                                          │
│  Or tap a symptom:                                       │
│  [Fever] [Cough] [Diarrhea] [Vomiting] [Not eating]     │  ← Chips; add without speaking
│                                                          │
│  [  Continue  ]  ← Primary (enabled when something       │
│                   captured or at least one chip)         │
└──────────────────────────────────────────────────────────┘
```

### 1.2 Clear “recording” state

| State | Visual | Label | Behaviour |
|-------|--------|-------|-----------|
| **Idle** | Mic icon, primary or neutral fill; **no motion** | “Tap to speak” | Tap → start listening. |
| **Listening** | Same mic icon; **pulsing ring** (CSS animation, 1.5 s loop) or soft glow; optional red/danger tint on button to signal “on” | “Listening…” | Tap → stop. Transcript updates live (short preview). |
| **Processing** (optional) | Mic icon; **spinner** or static icon; button disabled | “Understanding…” | Brief; then go to review or show preview. |

- **Never ambiguous:** Only two main states for the CHW: “Tap to speak” vs “Listening…”. Label is always visible (icon + text); colour supports (e.g. listening = subtle pulse or tint) but meaning is clear from **label** and **animation** (pulse = on).
- **One control:** One large button (min 80×80 px, prefer 96 px) for start/stop. No separate “pause” in MVP; tap again to stop.

### 1.3 Visual feedback during capture

- **Live transcript (short):** Show 1–2 lines of “what we heard” below the button, updating as the CHW speaks. **No long scrollable block.** Truncate with ellipsis; full transcript available after stop (or on review screen). Purpose: confirm the device is hearing, not to edit during speech.
- **Pulsing ring:** When listening, a subtle expanding/contracting ring around the mic button (or soft background pulse) so the CHW sees “on” at a glance. Animation is **calm** (e.g. 1.5 s cycle); not fast or distracting.
- **Optional: sound level** — Very simple bars or single bar (no numbers) to show input level; reinforces “we’re listening.” Only if it doesn’t clutter; otherwise omit.
- **Offline:** Small “Offline” or “Saved on device” near header so CHW knows capture works without network.

### 1.4 No long text blocks

- **During capture:** “What we heard” is 1–2 lines max; no paragraph. If transcript is long, show last portion with “…” or “(more on next screen).”
- **After capture:** Full transcript is available on the **review** screen in a single, editable block only if needed; default focus is **structured preview** (short fields), not raw text.

### 1.5 Works offline

- **No “connecting” or “uploading”** during capture. All copy is local: “Tap to speak”, “Listening…”, “Saved on device.”
- **No disabled state** due to network. Mic and Continue are enabled based on content (something captured or chips selected), not connectivity.
- If ASR runs on device (e.g. Vosk), no network indicator on this screen beyond the global header “Offline.”

---

## 2. Review & Correction Flow

### 2.1 Screen order

1. **Capture** (above) → CHW taps Continue.
2. **Review** — Structured note preview with confidence; one-tap corrections.
3. **Confirm** — Single primary action: “Use these notes” or “Continue to suggestions.”

No extra step between capture and review; one transition.

### 2.2 Review screen layout (structured note preview)

```
┌──────────────────────────────────────────────────────────┐
│  [←]  Check what we understood                           │
├──────────────────────────────────────────────────────────┤
│                                                          │
│  We wrote this from what you said. Fix anything wrong.   │  ← Helper tone; one line
│                                                          │
│  ┌─ Chief complaint ────────────────────────────────────┐│
│  │  Fever and cough for 3 days              [High ✓]   ││  ← Value + confidence badge
│  │  [ Tap to edit ]                                    ││  ← One-tap correction entry
│  └────────────────────────────────────────────────────┘│
│                                                          │
│  ┌─ Symptoms ──────────────────────────────────────────┐│
│  │  Fever · 3 days                    [High]  [Edit]   ││
│  │  Cough                             [Med]   [Edit]   ││
│  │  [ + Add symptom ]                                 ││
│  └────────────────────────────────────────────────────┘│
│                                                          │
│  ┌─ Red flags ─────────────────────────────────────────┐│
│  │  Not drinking well                   [High]  [Edit] ││  (Only if any)
│  └────────────────────────────────────────────────────┘│
│                                                          │
│  ┌─ Notes ─────────────────────────────────────────────┐│
│  │  "Fever three days, cough, mother says…"  [Edit]   ││  ← 1–2 lines; expand to edit
│  └────────────────────────────────────────────────────┘│
│                                                          │
│  [  Continue to suggestions  ]  ← Primary                │
└──────────────────────────────────────────────────────────┘
```

### 2.3 One-tap corrections

- **Per field:** Each structured field (chief complaint, each symptom, red flag, notes) has an **explicit “Edit” or “Tap to edit”** control. Tap target min 44 px; whole row can be tappable for “edit this.”
- **Tap behaviour:** Inline edit preferred: field becomes an input (or short dropdown for symptoms); “Done” or tap outside saves. **No modal** for simple text fix. For symptom list: tap “Edit” on one row → inline edit or small popover (symptom picker + duration); “Add symptom” → same.
- **Confirmation:** No “Are you sure?” for edits. Saving the edit is the confirmation. Only “Continue to suggestions” is the main confirmation action for the whole screen.
- **Clear actions:** Buttons are “Edit” or “Tap to edit” (never “Modify” or “Change”). Primary CTA is one only: “Continue to suggestions.”

### 2.4 Helper tone (AI not judge)

- **Opening line:** One short sentence: “We wrote this from what you said. Fix anything wrong.” Or: “Here’s what we understood. Tap to fix if something’s wrong.”
- **No blame:** Avoid “Correct the following” or “Errors below.” Use “Check” or “Fix if wrong.”
- **Empty or low-confidence:** “We didn’t catch much. You can type chief complaint below or add symptoms.” Option to type short chief complaint + chips.
- **Labels:** “Chief complaint”, “Symptoms”, “Red flags”, “Notes” — neutral. Confidence is shown with badges (see §3), not with words like “Uncertain” in the main label.

---

## 3. Visual Cues for AI Uncertainty

### 3.1 Confidence indicators (per field)

- **Three levels:** High, Medium, Low. Each level has **icon + short label + color** (redundant; grayscale-safe).
- **Placement:** Same place for every field: right-aligned next to the value (or below on small screens). Always visible so CHW can scan “what to double-check.”

| Level | Label | Color | Icon | Use |
|-------|--------|------|------|-----|
| **High** | “High” or ✓ | Success (#2D8A5E) | Check or dot | We’re confident; quick review enough. |
| **Medium** | “Check” or “Med” | Warning (#B86B1A) | Triangle or dot | Please glance; easy to edit if wrong. |
| **Low** | “Uncertain” or “Low” | Muted or warning | Question or dot | We may have missed or misheard; please fix. |

- **Badge style:** Small pill (e.g. 20–24 px height); icon + 2–4 letter label (“High”, “Med”, “Low”). Not a long sentence.
- **No color only:** Label or icon is always present so meaning is clear in grayscale and for color-blind users.

### 3.2 When to show “check” or “uncertain”

- **Medium:** Chief complaint or symptom came from unclear phrase; duration ambiguous; possible red flag inferred (not clearly said).
- **Low:** Very short or noisy transcript; no chief complaint extracted; symptom guessed from one word.
- **Processing warning (section):** If the system has a global warning (e.g. “Transcript was short — please add anything we missed”), show **one** short line above or below the fields, with optional info icon. Muted background; not alarming.

### 3.3 Reducing anxiety around uncertainty

- **Framing:** “Check” and “Uncertain” mean “we’re not sure — you decide,” not “you made an error.” Copy: “Tap to edit” not “Correct this.”
- **Low-confidence fields are easy to fix:** Same one-tap edit as high-confidence; no extra steps. CHW can leave as is or fix quickly.
- **No count of “errors”:** Don’t show “3 items need attention.” Show confidence per field and let the CHW choose what to edit.

### 3.4 Summary: confidence in the UI

| Where | What |
|-------|------|
| **Per field** | Badge right of value: High (green/check), Medium (amber/“Check”), Low (muted or amber/“Uncertain”). |
| **Global** | Optional one-line message if transcript was short or noisy: “We didn’t hear much — add anything below.” |
| **Tone** | Helper; “fix if wrong”; no blame; one-tap edit for every field. |

---

## 4. Consistency with Design System

- **One primary action:** On capture = “Continue”; on review = “Continue to suggestions.” No competing CTAs.
- **Min 48 px touch targets** for mic button, Continue, and each “Edit” / row.
- **Icon + label** for recording state (“Listening…”) and confidence (badge with icon + “High”/“Med”/“Low”).
- **Color for meaning only:** Listening = pulse/tint; High = success; Medium/Low = warning/muted. No decorative color.
- **No long blocks:** Transcript preview 1–2 lines on capture; on review, notes field can expand but default is short.
- **Works offline:** No network-dependent states or copy on these screens.

This gives a single specification for the **recording UI**, **review and correction flow**, and **visual cues for AI uncertainty** so the CHW can speak first, see what was understood, and fix anything with minimal effort.
