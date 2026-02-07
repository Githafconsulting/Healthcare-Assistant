# CHW Patient Profile Screen — Design Specification

Design for the patient profile screen: **low-literacy review**, **fast recall of history**, **trust and accuracy**. Includes patient identifiers, visit timeline, active treatment plan, medication adherence, and risk indicators. Aligns with [design system](CHW_WEBAPP_DESIGN_SYSTEM.md).

---

## 1. Layout Structure

### 1.1 Zones (top to bottom, single column)

```
┌──────────────────────────────────────────────────────────┐
│  [←]  Patient profile                         [Sync ●]   │
├──────────────────────────────────────────────────────────┤
│                                                          │
│  ┌─ PATIENT CARD (always visible) ─────────────────────┐ │
│  │  [👤]  Maria Okonkwo                                 │ │
│  │        Girl · 2 years · Kijiji                      │ │
│  │        ID: 1234  (optional)                          │ │
│  │  [  Start visit  ]  ← primary CTA                   │ │
│  └────────────────────────────────────────────────────┘ │
│                                                          │
│  ┌─ RISK INDICATORS (only if any) ──────────────────────┐ │
│  │  [⚠] Referral not completed  · Due 2 days ago       │ │
│  │  or: [△] Follow-up overdue                         │ │
│  └────────────────────────────────────────────────────┘ │
│                                                          │
│  ┌─ ACTIVE NOW (expand/collapse, default expanded) ────┐ │
│  │  ▼ Active treatment & follow-up                    │ │
│  │     [📋] ORS · Give after each stool · 3 days left  │ │
│  │     [📅] Follow-up due Wed 5 Feb · Recheck fever    │ │
│  │     [💊] Adherence: On track (or "Missed 2 doses")  │ │
│  └────────────────────────────────────────────────────┘ │
│                                                          │
│  ┌─ VISIT TIMELINE (expand/collapse, default collapsed)┐ │
│  │  ▶ Past visits (6)                                 │ │
│  └────────────────────────────────────────────────────┘ │
│     (When expanded:)                                    │
│  ┌─ VISIT TIMELINE (expanded) ──────────────────────────┐ │
│  │  Past visits                                        │ │
│  │  ───●─── 3 Feb  Fever · ORS, paracetamol   [Done ✓] │ │
│  │       ●── 28 Jan  Diarrhea · ORS, zinc      [Done ✓] │ │
│  │         ● 20 Jan  Cough · Counsel only     [Done ✓] │ │
│  │  [ Show fewer ]                                     │ │
│  └────────────────────────────────────────────────────┘ │
│                                                          │
└──────────────────────────────────────────────────────────┘
│  [Home] [Visits] [Follow-ups] [Patients] [Sync]          │
└──────────────────────────────────────────────────────────┘
```

### 1.2 Section order and purpose

| Section | Purpose | Default state |
|---------|---------|----------------|
| **Patient card** | Identity at a glance; one place to confirm “right patient.” | Always visible |
| **Risk indicators** | Urgent items (referral, overdue) so CHW sees them first. | Visible only if any |
| **Active now** | Current plan and follow-up; what to do next. | **Expanded** |
| **Visit timeline** | History for recall; past visits in time order. | **Collapsed** |

### 1.3 Expand/collapse rules

- **Active now:** Section title + chevron (▼ expanded, ▶ collapsed). Tap header toggles. Default **expanded** so “what’s active” is visible without a tap.
- **Visit timeline:** Section title “Past visits (N)” + chevron. Default **collapsed** to avoid long scroll; tap to expand. Optional “Show fewer” after 5–6 items to collapse again or cap visible items.
- **No accordion nesting:** Only these two sections collapse; content inside them does not have sub-accordions.
- **Min tap target:** Entire section header row min 48 px height for expand/collapse.

### 1.4 Past vs current separation

- **Current:** Everything in **Active now** — active treatment plan, next follow-up date, adherence for current meds. Visually one block with clear heading “Active treatment & follow-up.”
- **Past:** Everything in **Visit timeline** — past visits only, ordered newest first. Label “Past visits”; timeline line and nodes (see §2) reinforce “this is history.”
- **No mixing:** No past visit inside Active now; no active plan inside timeline. One divider (section title + optional horizontal rule) between Active now and Visit timeline.

---

## 2. Timeline Visual Style

### 2.1 Structure

- **Orientation:** Vertical. Newest at top, older below.
- **Line:** One vertical line (2 px, muted #E4E6E5) running down the left side of the visit list. Line starts below the section title and runs to the last visit (or “Show fewer”).
- **Nodes:** One node per visit on the line. Node = small circle (8–10 px) filled with surface or primary when “today”; otherwise neutral. Node aligns with the visit row.
- **Row per visit:** One row (or card) per visit. Left: node on the line. Right: date, one-line summary (symptoms/treatment), and status (e.g. “Done ✓”).

### 2.2 Wireframe (expanded timeline)

```
  Past visits
  ─────────────────────────────────────
       ●  3 Feb   Fever · ORS, paracetamol    ✓
  │    │
  │    ●  28 Jan  Diarrhea · ORS, zinc         ✓
  │    │
  │    ●  20 Jan  Cough · Counsel only         ✓
  │
```

### 2.3 Specs

| Element | Spec |
|---------|------|
| **Vertical line** | 2 px width; color border/muted (#E4E6E5); from first node to last node. |
| **Node (circle)** | 8–10 px diameter; fill: neutral (#E4E6E5) for past; primary (#0D6B5C) if visit was today. Stroke optional (1 px) for clarity. |
| **Date** | 14 px, muted; format “3 Feb” or “Today”; left-aligned after node. |
| **Summary** | One line, 16 px, neutral; e.g. “Fever · ORS, paracetamol.” Truncate with ellipsis if long. |
| **Status** | Icon + short label: ✓ “Done” (success) or “Incomplete” (muted). Right-aligned. |
| **Spacing** | 12–16 px vertical between rows. 8–12 px gap between node and text. |

### 2.4 Low-literacy and fast recall

- **Icons over text where possible:** Each visit row can start with a small icon (e.g. clipboard or calendar) next to the node; summary uses words + optional pill/visit icon. Same icon set as rest of app.
- **One line per visit:** No paragraph; “Date · Symptoms · Treatments” in one line so CHW can scan quickly.
- **Newest first:** Most recent visit at top so “what happened last time” is found first.
- **Tap row:** Row tappable → visit detail (expand inline or new screen) for full notes if needed; default view stays minimal.

---

## 3. Risk Indicator Design

### 3.1 When to show

- **Referral not completed** (patient referred but no outcome or follow-up recorded).
- **Follow-up overdue** (due date passed, not marked done).
- **Danger sign in last visit** (e.g. referral suggested, CHW accepted; optional per programme).
- **Medication adherence alert** (e.g. multiple doses missed; optional).

Show a **single risk block** only if at least one of these is true. If none, section is hidden (no “No risks” message).

### 3.2 Layout

- **One card** for all risk indicators on this patient.
- **Left accent:** 4 px vertical strip in **danger** (#C44536) so the card is recognisable even in grayscale.
- **Content:** One row per risk type. Each row: **icon** (alert/danger) + **short label** (e.g. “Referral not completed”) + **context** (e.g. “Due 2 days ago”). No long sentences.

### 3.3 Visual spec

| Element | Spec |
|---------|------|
| **Card** | Background surface (#FFF); border 2 px; left border or 4 px left accent in danger (#C44536). Padding 16 px. |
| **Icon** | 24 px; danger color or neutral with danger accent. Icon = alert circle or exclamation (not skull). |
| **Label** | 16 px, bold or 600; neutral text (#1F2924). E.g. “Referral not completed.” |
| **Context** | 14 px, muted; after label. E.g. “Due 2 days ago” or “Follow-up Wed 5 Feb.” |
| **Row** | Min 44 px height; icon left, label + context right. Multiple risks stacked vertically with 8 px between rows. |

### 3.4 Color and meaning (color-blind safe)

- **Danger** (#C44536) used only for the **accent strip** (and optionally icon). Meaning is never by color alone.
- **Redundant coding:** Each risk has **icon** (alert) + **label** (“Referral not completed”) + **context** (“Due 2 days ago”). In grayscale, left stripe (darker) + icon + text still convey “needs attention.”
- **One risk card per patient:** Do not use different colors for different risk types (e.g. red vs orange); one danger accent for “something needs attention.” Risk type is distinguished by **label text** and optional icon variant (e.g. referral vs calendar).

### 3.5 Example rows

| Risk type | Label | Context |
|-----------|-------|---------|
| Referral not completed | Referral not completed | Due 2 days ago |
| Follow-up overdue | Follow-up overdue | Was due 3 Feb |
| Danger sign last visit | Urgent referral from last visit | Tap to review |

---

## 4. Patient Identifiers (Patient Card)

### 4.1 Content (low-literacy review)

| Field | How shown | Icon |
|-------|-----------|------|
| **Name** | Large, 17–18 px, bold. Single line; truncate with ellipsis if long. | Person (👤 or icon) left of name. |
| **Age** | “X years” or “X months” for under 2. 14 px, muted. | Optional calendar or none. |
| **Sex** | “Boy” / “Girl” or “Male” / “Female” per programme. 14 px, muted. | Optional. |
| **Village / area** | One line. 14 px, muted. | Optional location pin. |
| **ID** | If used: “ID: 1234” or “No. 1234”. 13 px, muted. Small so it doesn’t dominate. | Optional. |

### 4.2 Layout

- **Icon** (person) left, 32–40 px.
- **Name** next to icon; below it one line of **Age · Sex · Village** (or two lines if needed).
- **ID** below or at bottom of card, subtle.
- **Primary CTA:** “Start visit” full-width button below identifiers (min 48 px height). One primary action per screen: start a visit.

### 4.3 Trust and accuracy

- **Single source:** All identifiers from one patient record; no mixed sources.
- **No editing on this screen:** Profile edit (if any) is a separate flow so this screen is “review only” and reduces accidental changes.
- **ID visible** when programme uses it so CHW can confirm with paper or facility list.

---

## 5. Active Treatment Plan & Adherence

### 5.1 Active treatment (in “Active now”)

- **One row per active item:** e.g. “ORS · Give after each stool · 3 days left.”
- **Icon** (e.g. pill or clipboard) + **medicine/plan name** (bold) + **short instruction** (muted) + **remaining** (e.g. “3 days left” or “Until 8 Feb”).
- **No dosing paragraph here:** Dosing on tap/expand or in visit detail; this block is for quick recall only.

### 5.2 Follow-up (in “Active now”)

- **One row:** Icon (calendar) + “Follow-up due [date]” + reason (e.g. “Recheck fever”).
- **Date:** Short format “Wed 5 Feb”; if overdue, add “Overdue” in muted or warning (and ensure risk block shows if used).

### 5.3 Medication adherence

- **One line** in Active now: e.g. “Adherence: On track” (success icon) or “Missed 2 doses” (warning icon).
- **Icon + short label only;** no chart on profile. Detail (which days missed) on tap/expand or separate screen if needed.
- **Color:** On track = success (#2D8A5E) with check icon; missed = warning (#B86B1A) with alert icon. Label always present (color-blind safe).

### 5.4 Empty state

- If no active treatment and no follow-up: “No active plan. Start a visit to add one.” Muted text; no large illustration.

---

## 6. Summary: Layout, Timeline, Risk

| Area | Decision |
|------|----------|
| **Layout** | Single column: Patient card (fixed) → Risk indicators (if any) → Active now (expand/collapse, default on) → Visit timeline (expand/collapse, default off). One primary CTA: “Start visit.” |
| **Timeline** | Vertical; newest top; 2 px line + 8–10 px nodes; one row per visit (date · summary · status); icons where helpful; tap row for detail. |
| **Risk** | One card with danger left accent; icon + label + context per risk; no color-only meaning; section hidden when no risks. |

This keeps the profile **timeline-based**, **icon-supported**, **expand/collapse** for current vs past, and **clearly separated** so the CHW can review quickly and trust what they see.
