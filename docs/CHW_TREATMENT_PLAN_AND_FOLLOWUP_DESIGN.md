# Treatment Plan & Follow-up Screen — Design Specification

Design for the treatment plan and follow-up screen. **Users:** CHW (creator and manager); Patient / caregiver (indirect recipient via printable or shareable view). Aligns with [treatment plan generator](TREATMENT_PLAN_GENERATOR.md) and [design system](CHW_WEBAPP_DESIGN_SYSTEM.md).

**Design rules:** Plain language; icon-led instructions; printable / shareable view; visual confirmation of completion.

---

## 1. Treatment Plan Layout

### 1.1 Screen purpose and entry

- **CHW view:** One screen per active treatment plan, reached from “Complete visit” (after accepting suggestions) or from patient profile (“Active plan”).
- **Content:** Simple step-by-step plan, medication schedule(s), follow-up date, reminder status. One primary action per context: “Mark complete”, “Print / Share for patient”, “Set follow-up”.

### 1.2 Layout (CHW — single column)

```
┌──────────────────────────────────────────────────────────────┐
│  [←]  Treatment plan                         [Offline ●]     │
├──────────────────────────────────────────────────────────────┤
│  Maria Okonkwo · 2 years                                      │
│  Started 2 Feb 2025 · Active                                  │
├──────────────────────────────────────────────────────────────┤
│  WHAT TO DO (steps)                                           │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  1  [✓]  Give ORS today                    Done        │  │  ← Step + completion
│  │      Give 50–100 ml after each loose stool.            │  │
│  ├────────────────────────────────────────────────────────┤  │
│  │  2  [ ]  [💊]  Give Zinc for 14 days                    │  │  ← Icon + title
│  │      One tablet each morning. Started 2 Feb.           │  │
│  ├────────────────────────────────────────────────────────┤  │
│  │  3  [ ]  [🏥]  Refer to clinic                          │  │
│  │      Refer to nearest clinic (danger sign).            │  │
│  └────────────────────────────────────────────────────────┘  │
├──────────────────────────────────────────────────────────────┤
│  MEDICATIONS                                                  │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  [💊] ORS · For diarrhea                                │  │
│  │  Give 50–100 ml after each loose stool.  No fixed times.│  │
│  │  Started 2 Feb ·  [  Mark day 1 done  ]                 │  │  ← Optional daily tick
│  ├────────────────────────────────────────────────────────┤  │
│  │  [💊] Zinc · 10–14 days                                 │  │
│  │  One tablet each morning.  Day 2 of 14.                 │  │
│  │  [■■□□□□□□□□□□□□]  Progress (optional)                  │  │
│  └────────────────────────────────────────────────────────┘  │
├──────────────────────────────────────────────────────────────┤
│  FOLLOW-UP                                                    │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  [📅]  Due Wed 5 Feb  ·  Recheck fever                  │  │
│  │  Reminder: Sent 4 Feb  [✓]   Patient replied OK  [✓]   │  │  ← Reminder status
│  │  [  Mark follow-up done  ]  or  [  Reschedule  ]       │  │
│  └────────────────────────────────────────────────────────┘  │
├──────────────────────────────────────────────────────────────┤
│  [  Print / Share for patient  ]   ← Printable, patient view  │
└──────────────────────────────────────────────────────────────┘
```

- **Header:** Back, title “Treatment plan”, offline indicator. Patient name + age; plan start date; status (Active / Completed / Cancelled / Escalated).
- **Sections (in order):** What to do (steps) → Medications → Follow-up. Each section is a card or clearly separated block.
- **One primary CTA** per section when relevant (e.g. “Mark follow-up done”). Screen-level primary: “Print / Share for patient”.

### 1.3 Step-by-step plan (actionable steps)

- **List:** Numbered steps (1, 2, 3…). Each step shows:
  - **Icon** (optional): pill for medication, building for referral, chat for counsel.
  - **Title:** Short, plain language (e.g. “Give ORS today”, “Give Zinc for 14 days”, “Refer to clinic”).
  - **Description:** One line under the title; fuller instructions on tap/expand.
  - **Completion:** Checkbox or “Done” badge. When done: checkmark (✓), muted text, optional “Done” label and date. CHW taps to mark step done (visual confirmation).
- **Order:** Same as `ActionableStep.order`. No reordering in MVP.
- **Visual confirmation:** Completed steps use success color (#2D8A5E) for icon/check; unchecked steps use neutral or primary. Always icon + label (“Done” or check).

### 1.4 Medication schedule block

- **Per medication:** One row or card: medication name (e.g. “ORS”, “Zinc”), indication (“For diarrhea”), then dosing in plain language.
- **Dosing:** Prefer one short line: “Give 50–100 ml after each loose stool” or “One tablet each morning.” If multiple times: “Morning and evening” or “Every 6 hours (max 4 times a day).” Use `plainLanguage` from the treatment plan schema.
- **Duration:** “10–14 days”, “Day 2 of 14”, “Until fever is gone.”
- **Progress (optional):** For fixed-length courses, a simple progress indicator (e.g. “Day 2 of 14” or a row of blocks ■■□□…). Not required for event-based (e.g. ORS “after each stool”).
- **Actions:** “Mark day X done” or “Mark course complete” where useful; tap to expand full dose instructions (from `DoseInstruction`).

### 1.5 Follow-up block

- **Due date:** Prominent: “Due Wed 5 Feb” or “Due in 2 days” (relative). Icon (calendar).
- **Reason:** One short line: “Recheck fever”, “Complete ORS course”, “After referral.”
- **Reminder status:** See §2 (indicators).
- **Actions:** “Mark follow-up done” (primary) or “Reschedule”. When done: show “Completed 5 Feb” with checkmark.

### 1.6 No long text blocks

- Step descriptions: one line default; expand for more.
- Medication instructions: one to two lines; “More” for full dosing table if needed.
- Counsel points (for CHW only): list of short bullets or expandable cards; not on patient-facing print.

---

## 2. Reminder Status Indicators

### 2.1 What to show

Reminder status is shown in the **Follow-up** block (and optionally in a list of “Follow-ups due” on Home). States derive from `FollowUpPlan`: reminder sent, patient responded, due date passed, escalation.

### 2.2 Indicator set

| Status | Label (short) | Icon | Color | When |
|--------|----------------|------|--------|------|
| **Not sent** | “Reminder not sent” | Clock or bell | Muted | Due date in future; reminder not yet sent. |
| **Sent** | “Reminder sent” | Check or sent | Success (#2D8A5E) | `reminderSentAt` set; awaiting response/visit. |
| **Replied** | “Patient replied” | Check or message | Success | Patient/caregiver replied (e.g. OK); optional. |
| **Due today** | “Due today” | Calendar | Warning (#B86B1A) | Due date is today; not yet completed. |
| **Overdue** | “Overdue” | Alert triangle | Warning | Due date passed; follow-up not marked done. |
| **Escalated** | “Escalated” | Alert circle | Danger (#C44536) | L2/L3; CHW or supervisor alerted. |
| **Done** | “Done” | Check | Success | Follow-up marked completed. |

- **Icon + label** for every state (color-blind safe). No color-only meaning.
- Placement: under the follow-up due date, one line: “Reminder: Sent 4 Feb ✓” or “Reminder: Not sent” or “Due today” or “Overdue · 1 day”.

### 2.3 Inline display (follow-up card)

```
  Reminder:  [Sent 4 Feb]  [✓]     Patient: [Replied OK]  [✓]
  or
  Reminder:  [Due today]
  or
  Reminder:  [Overdue · 2 days]  [  Mark done  ]  [  Reschedule  ]
  or
  Reminder:  [Escalated]  —  CHW alerted
```

- Keep to one line when possible; two lines on small screens is acceptable.
- Tapping “Reminder” or an info icon can show one sentence: “We sent a reminder to the caregiver on 4 Feb” or “No reminder sent yet; will send on due date.”

### 2.4 List view (e.g. Home — “Follow-ups due”)

- Each row: Patient name, due date, “Due today” / “Overdue” / “Sent” badge, tap → plan or follow-up screen.
- Sort: overdue first, then due today, then upcoming. Escalated can be a separate filter or top section.

---

## 3. Patient-Friendly Visual Mode (Printable / Shareable)

### 3.1 Purpose

A **patient-facing** view of the same plan: simple, plain language, icon-led. The CHW can **print** or **share** (e.g. screenshot, PDF, or share sheet) so the caregiver has a clear, at-home reference. No login required for the recipient; content is read-only.

### 3.2 Layout (patient view)

- **Title:** “What to do for [Patient name]” or “Your care plan” — one short line.
- **Date:** “From visit on [date].”
- **Steps:** Same order as CHW view but:
  - **Icon first,** large enough to scan (e.g. 32–40 px).
  - **One sentence per step:** “Give ORS after each runny stool.” “Give one Zinc tablet every morning for 14 days.” “Go to the clinic as advised.”
  - No “Mark done” or internal labels; only instructions.
- **Medications:** One block per medicine:
  - Icon (pill).
  - Name: “ORS”, “Zinc”.
  - When: “After each runny stool” / “Every morning”.
  - How long: “For 10–14 days” / “14 days”.
- **Follow-up:** “Come back on [date]” or “Come back in 2 days.” Reason: “To recheck fever.” Optional: “We will send you a reminder.”
- **Footer:** “If you have questions, ask your health worker.” Optional: CHW name or facility.

### 3.3 Plain language and icon-led instructions

- **Words:** Short sentences; avoid medical jargon. Use “runny stool” instead of “loose stool” if preferred for locale; “fever” not “pyrexia”; “tablet” or “pill” consistently.
- **Icons:** Every step and every medication has an icon (pill, calendar, building for referral, person for “talk to health worker”). Icon above or left of text; same icon set as CHW app.
- **Numbers:** Use digits: “14 days”, “2 times a day”, “50–100 ml.”
- **No internal state:** No “Reminder sent”, “Mark done”, or status badges; only what the patient must do and when to return.

### 3.4 Printable / shareable behaviour

- **Print:** “Print / Share for patient” opens the patient view in a print-friendly layout (white background, no nav, large text, page break after plan so it fits one page when possible).
- **Share:** Same content as shareable image or PDF (e.g. “Save as PDF” then share, or in-app “Share” that generates image/PDF). No editable fields in shared content.
- **Offline:** Patient view is generated from local plan data; no network required to print or save.

### 3.5 Wireframe (patient view)

```
┌──────────────────────────────────────────────────────────────┐
│  What to do for Maria                                         │
│  From visit on 2 Feb 2025                                     │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│    [💊]  Give ORS after each runny stool.                     │
│                                                              │
│    [💊]  Give one Zinc tablet every morning for 14 days.     │
│                                                              │
│    [📅]  Come back on Wed 5 Feb to recheck fever.            │
│          We will send you a reminder.                        │
│                                                              │
├──────────────────────────────────────────────────────────────┤
│  Questions? Ask your health worker.                           │
└──────────────────────────────────────────────────────────────┘
```

---

## 4. Visual Confirmation of Completion

### 4.1 Principles

- Every completable item has a **clear before/after** state.
- **Completion is explicit:** CHW (or caregiver, if tracked) performs an action to mark done; the UI reflects it immediately.
- Use **icon + color + label** (design system): done = check + success color + “Done” or “Completed”.

### 4.2 Step completion

- **Before:** Empty circle or unchecked box; step title and description in normal weight; no “Done” label.
- **After:** Checkmark (✓) in success color (#2D8A5E); optional “Done” or “Completed [date]” in muted text; step body slightly muted (e.g. opacity 0.9) so completed steps don’t compete with pending ones.
- **Action:** Tap step row or “Mark done” → step is marked complete; optional short toast “Step 1 marked done.” No confirmation dialog for marking done.

### 4.3 Medication progress

- **Event-based (e.g. ORS):** “Mark day 1 done” or “Started” once; then show “Started on [date]” with check.
- **Course (e.g. Zinc 14 days):** Show “Day X of 14” and optional progress bar or block strip (■■□□…). When course complete: “Completed [date]” with check. If the system supports daily tick: “Mark day 3 done” per day.
- **Visual:** Completed courses use same success check + muted style as steps.

### 4.4 Follow-up completion

- **Before:** “Mark follow-up done” primary button; reminder status as in §2.
- **After:** “Completed on [date]” with checkmark; “Mark follow-up done” hidden or replaced by “Completed”. Reminder indicators can remain for audit (e.g. “Reminder sent 4 Feb”) but “Done” is prominent.
- **Action:** CHW taps “Mark follow-up done” → one confirmation: “Mark follow-up for Maria as done?” with “Yes” / “Cancel” (optional; can be tap = done). Then show success state.

### 4.5 Plan-level status

- When all steps and follow-up are done (or no follow-up), plan can move to **Completed**.
- Option: “Complete plan” button when all items done; or auto-transition to Completed and show “Plan completed” banner with checkmark.
- Cancelled / Escalated: show status in header with appropriate icon and label (e.g. “Escalated”, “Cancelled”).

---

## 5. Consistency with Design System

- **One primary action per section:** e.g. “Mark follow-up done” in follow-up block; “Print / Share for patient” at screen level.
- **Min 48 px touch targets** for “Mark done”, “Reschedule”, “Print / Share”, and step rows.
- **Icon + label** for all states (reminder status, step done, medication).
- **Color for meaning only:** Success for done/sent/replied; warning for due today/overdue; danger for escalated.
- **Progressive disclosure:** Full dosing and counsel points on expand/tap.
- **Short copy:** Step titles and descriptions in plain language; no long paragraphs.

---

## 6. Summary

| Output | Content |
|--------|---------|
| **Treatment plan layout** | Single column: patient + plan status → What to do (numbered steps with icons and completion) → Medications (name, plain-language dosing, duration, optional progress) → Follow-up (due date, reason, reminder status, Mark done / Reschedule). Primary: Print / Share for patient. |
| **Reminder status indicators** | Not sent / Sent / Replied / Due today / Overdue / Escalated / Done; each with icon + short label and semantic color; shown in follow-up block and in follow-ups-due lists. |
| **Patient-friendly visual mode** | Separate view: “What to do for [name]”, icon-led steps and medications in plain language, follow-up date and reason, no internal status; printable and shareable (PDF/image), works offline. |
| **Visual confirmation of completion** | Steps and follow-up: checkmark + success color + “Done”/“Completed”; medication: “Day X of Y” or “Completed”; one-tap “Mark done” with immediate UI update; no color-only meaning. |

This gives a single specification for the **treatment plan and follow-up screen** for both the CHW (creator) and the patient (indirect recipient via printable/shareable view), with **plain language**, **icon-led instructions**, and **visual confirmation of completion**.
