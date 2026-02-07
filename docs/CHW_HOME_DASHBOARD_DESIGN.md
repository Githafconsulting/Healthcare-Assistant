# CHW Home Dashboard — Design Specification

Dashboard design for the CHW web app home screen. **Goals:** Reduce anxiety, show priorities instantly, surface only what matters today. **Requirements:** Card-based layout, clear visual hierarchy, no dense tables, color used only for meaning. Aligns with [design system](CHW_WEBAPP_DESIGN_SYSTEM.md) and [information architecture](CHW_WEBAPP_INFORMATION_ARCHITECTURE.md).

---

## 1. Wireframe Description

### 1.1 Zones (top to bottom)

```
┌──────────────────────────────────────────────────────────┐
│  HEADER                                                   │
│  Afya Assistant                    [●] Synced / Offline  │
├──────────────────────────────────────────────────────────┤
│                                                          │
│  QUICK ACTION (single primary CTA)                       │
│  ┌────────────────────────────────────────────────────┐  │
│  │  ＋  New visit                                      │  │
│  └────────────────────────────────────────────────────┘  │
│                                                          │
│  STATUS BANNER (sync/offline — compact)                  │
│  ┌────────────────────────────────────────────────────┐  │
│  │  ○ Offline · 3 visits pending sync                  │  │
│  └────────────────────────────────────────────────────┘  │
│  (Only when offline or pending; else omit or show "Synced")
│                                                          │
│  HIGH-RISK (only if any; card with left accent)           │
│  ┌────────────────────────────────────────────────────┐  │
│  │ ▌ Amina Juma    Referral not yet completed          │  │
│  │   Due 2 days ago · Tap to follow up                 │  │
│  └────────────────────────────────────────────────────┘  │
│  (Max 3; "See all" if more)                              │
│                                                          │
│  FOLLOW-UPS DUE TODAY                                     │
│  ┌────────────────────────────────────────────────────┐  │
│  │  Joseph Mwangi     Recheck fever                    │  │
│  │  Due today                                         │  │
│  ├────────────────────────────────────────────────────┤  │
│  │  Fatima Hassan     Complete ORS                    │  │
│  │  Due today                                         │  │
│  └────────────────────────────────────────────────────┘  │
│  [ See all follow-ups ]                                  │
│                                                          │
│  TODAY'S PATIENTS (visited or planned)                   │
│  ┌────────────────────────────────────────────────────┐  │
│  │  Maria Okonkwo    Visit 2h ago · Fever, ORS given   │  │
│  ├────────────────────────────────────────────────────┤  │
│  │  Peter Kipchoge   Visit this morning                │  │
│  └────────────────────────────────────────────────────┘  │
│  [ See all visits ]                                      │
│                                                          │
└──────────────────────────────────────────────────────────┘
│  [Home] [Visits] [Follow-ups] [Patients] [Sync]           │
└──────────────────────────────────────────────────────────┘
```

### 1.2 Zone specs

| Zone | Content | Card? | Max items | Tap target |
|------|---------|-------|-----------|------------|
| **Header** | App name (left); Sync/offline status (right). | No | — | Status tappable → Sync tab. |
| **Quick action** | One button: “New visit” + icon. Full width, min 48 px height. | Yes (single CTA card or no card, just button) | 1 | Whole button. |
| **Status banner** | One line: “Offline · N pending” or “Synced”. Shown only when offline or pending. | Yes (subtle: border + soft bg) | 1 | Whole banner → Sync tab. |
| **High-risk** | Patients with e.g. referral not completed, danger sign, overdue follow-up. Left accent strip (danger). Name + short reason + “Due X ago” / “Tap to follow up”. | Yes, one card per patient or one card with list | 3 | Whole row/card. |
| **Follow-ups due today** | Section label “Follow-ups due today”. Cards: patient name, reason (e.g. “Recheck fever”), “Due today”. | Yes, one card per patient | 3–5 | Whole card. “See all” = secondary link. |
| **Today’s patients** | Section label “Today’s patients”. Cards: patient name, last visit time + one-line summary (e.g. “Fever, ORS given”). | Yes, one card per patient | 3–5 | Whole card. “See all” = secondary link. |

### 1.3 No dense tables

- **No table grid.** Every row is a **card** (or a single list item with padding and separation).
- One concept per card: one patient, one follow-up, one status line.
- No columns of numbers or multi-column headers. If counts are shown (e.g. “3 follow-ups”), they are **supporting text** next to the section title or inside the first card, not a separate table cell.
- Lists are **vertical only**; no horizontal scrolling.

### 1.4 Empty and loading states

- **No high-risk:** Section is hidden. No “No high-risk patients” block (reduces anxiety).
- **No follow-ups today:** One card: “No follow-ups due today.” Muted text; no illustration required.
- **No today’s patients:** One card: “No visits yet today. Start with New visit.” Secondary CTA or plain text.
- **Loading:** Skeleton cards (same shape, grey blocks) or single “Loading…” in section; no spinner per card.

---

## 2. Visual Hierarchy Explanation

### 2.1 Hierarchy levels (top = highest priority)

| Level | What | How it’s shown |
|-------|------|-----------------|
| **1. Primary action** | “New visit” | Single full-width button; primary colour; only solid-filled CTA on the screen. |
| **2. Urgent attention** | Sync/offline when relevant; High-risk patients | Status banner (when not synced); High-risk block with danger accent. Both use colour + icon + short label. |
| **3. Today’s priorities** | Follow-ups due today | Section title (overline or H2) + cards. No accent colour; neutral cards so “due today” stands out by position and label. |
| **4. Context / recent** | Today’s patients | Section title + cards. Same card style as follow-ups but content is “visited” summary. Slightly lower than follow-ups so “what I need to do” is above “what I did.” |
| **5. Navigation** | Bottom nav | Always visible; same weight for all 5 items; current = Home highlighted. |

### 2.2 Reading order and scanning

- **First glance:** Header (where I am) → Status (am I synced?) → “New visit” (main action).
- **Second:** High-risk (if present) — “who needs attention first.”
- **Third:** Follow-ups due today — “who to see today.”
- **Fourth:** Today’s patients — “who I’ve seen or have on my list.”
- **Last:** “See all” links for deeper lists.

Section titles use **consistent style** (e.g. 13 px uppercase, muted) so the eye can skip to “Follow-ups due today” and “Today’s patients” quickly. Card content: **name** bold/large, **detail** one line, muted.

### 2.3 Reducing anxiety

- **One primary action** so there’s no “what do I press?” moment.
- **High-risk only when it exists** — no empty red block.
- **Bounded lists** (3–5 items) so the day feels manageable; “See all” for full list.
- **Calm wording:** “Follow-ups due today” not “Overdue”; “Tap to follow up” not “Action required.”
- **Offline** stated plainly (“Offline · 3 pending”) without alarm; colour muted, not danger red.

---

## 3. Color Usage Rationale

**Rule:** Color is used **only for meaning**, not decoration. Every colour has a semantic role and is paired with icon or text (colour-blind safe, works in grayscale).

### 3.1 By element

| Element | Color | Meaning | Alternative (grayscale) |
|---------|--------|---------|--------------------------|
| **Header background** | Primary (#0D6B5C) | App identity; consistent with rest of app | Dark bar; “Afya” text white. |
| **New visit button** | Primary (#0D6B5C) | Main action; “do this” | Solid dark button; “＋ New visit” label. |
| **Synced** (header/banner) | Success (#2D8A5E) | Data is synced; no action needed | Check icon + “Synced” text. |
| **Offline** (header/banner) | Muted (#5C6B64) | No connection; data saved locally | Cloud-off icon + “Offline” text. |
| **Pending sync** (banner) | Warning (#B86B1A) | Data waiting to upload; not an error | “3 pending” + cloud icon. |
| **High-risk card accent** | Danger (#C44536) | Needs attention; referral or overdue | Left vertical bar + “Referral not completed” (icon + text). |
| **Follow-up / Today’s patient cards** | Surface (#FFF), border (#E4E6E5), text neutral/muted | Neutral; no status colour | Card shape + name + one line. |
| **Section titles** | Muted (#5C6B64) | Label only; not interactive | Uppercase/label style. |
| **“See all” links** | Muted or primary (text link) | Secondary navigation | Underline or label “See all.” |
| **Background** | Soft (#F8F6F3) | Page surface; not white glare | Light grey. |

### 3.2 What has no colour

- **Card backgrounds** for follow-ups and today’s patients: white/surface. No alternating row colour, no status colour per card unless high-risk.
- **Dividers** between cards: neutral border only.
- **Body text** (names, reasons): neutral; **metadata** (due date, time): muted.
- **Icons** in neutral cards: same muted or neutral; no coloured icons except in status or high-risk.

### 3.3 High-risk only use of danger

- **Danger** (#C44536) is used **only** for the high-risk block: left accent strip and/or icon + “Referral not completed” / “Overdue” type label.
- Ensures “red” always means “needs attention” and is not diluted by decorative use.

---

## 4. Quick Action Buttons (Summary)

| Action | Placement | Style |
|--------|-----------|--------|
| **New visit** | Below header; full width | Primary button; icon ＋ ; label “New visit”; min 48 px height. |
| **See all follow-ups** | Below follow-ups card list | Secondary (text or outline); goes to Follow-ups tab. |
| **See all visits** | Below today’s patients list | Secondary; goes to Visits tab. |
| **Sync status** | Header right + optional banner | Not a button; tappable area → Sync tab. “Sync now” lives on Sync tab when online. |

Only **one** primary button on the dashboard: **New visit**. All other actions are secondary or navigation.

---

## 5. Responsive and Accessibility Notes

- **Max content width:** 480 px; centre on larger viewports (per design system).
- **Touch targets:** Every card and button min 48×48 px; spacing between cards ≥ 8 px.
- **Focus order:** Header → New visit → Status banner (if present) → High-risk (if present) → Follow-ups section → Today’s patients → See all links → Bottom nav.
- **Screen reader:** Section titles as headings (e.g. H2); card content as one announcement per card (name, then detail); “New visit” as button with accessible name.

This specification gives a single, implementable definition of the CHW home dashboard: wireframe, hierarchy, and color use, with no dense tables and colour only for meaning.
