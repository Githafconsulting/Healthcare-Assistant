# CHW Web App — Information Architecture

Information architecture for the Community Health Worker web app. **Primary user:** CHW doing 20–40 patient visits per day. **Constraints:** One-handed use, fast task switching, offline awareness. Aligns with the [6-step visit flow](CHW_WORKFLOW.md) and [design system](CHW_WEBAPP_DESIGN_SYSTEM.md).

---

## 1. Top-Level Navigation (Max 5 Items)

| # | Item | Icon | Purpose | Opens |
|---|------|------|---------|--------|
| **1** | **Home** | House | Dashboard: today’s workload, quick start visit | Home screen (see §2) |
| **2** | **Visits** | Clipboard / list | Active and recent visits; resume or review | List: today’s visits, recent; tap → visit detail or continue |
| **3** | **Follow-ups** | Calendar / bell | Who is due today / overdue; plan the day | List: Today / Overdue / Upcoming; tap → patient or mark done |
| **4** | **Patients** | Person / people | Find or register patient (no visit yet) | Search + “Add patient”; tap patient → profile or “Start visit” |
| **5** | **Sync** | Cloud / refresh | Pending uploads; offline status; manual sync | Status screen: pending count, last sync, “Sync now” (when online) |

**Rationale for 5:**

- **Home** = primary landing and “what do I do next?” — supports fast task switching and daily review.
- **Visits** = in-progress and just-done visits — resume or quick review without going via patient search.
- **Follow-ups** = daily workload clarity (who to see today); high value for 20–40 visits/day.
- **Patients** = find or register when starting from “I need to see Maria” rather than “I’m in a visit.”
- **Sync** = one place for offline awareness and pending data; keeps header simple while making status available.

**What is not top-level:** Settings (inside Sync or profile), Treatment plans (lived inside a visit), AI guidance (shown in-context during visit). This keeps top-level to **5 items** and avoids clutter.

---

## 2. Home / Dashboard Layout

### 2.1 Structure (single column, top to bottom)

```
┌─────────────────────────────────────────┐
│ [Header: Afya Assistant]  [Offline ●]   │  ← Offline indicator (see §5)
├─────────────────────────────────────────┤
│                                         │
│  [  ＋ New visit  ]  ← Primary CTA      │  One main action: start visit flow
│     (full width, 48px+ height)          │
│                                         │
│  TODAY’S FOLLOW-UPS                     │
│  ┌─────────────────────────────────┐   │
│  │ Amina Juma      Due today       │   │  Up to 3–5; tap → patient/visit
│  │ Joseph Mwangi   Overdue         │   │
│  └─────────────────────────────────┘   │
│  [See all follow-ups]  (secondary)     │
│                                         │
│  RECENT VISITS                          │
│  ┌─────────────────────────────────┐   │
│  │ Fatima Hassan   Fever, 2h ago    │   │  Last 3–5; tap → summary or patient
│  │ Maria Okonkwo   Diarrhea, yesterday│   │
│  └─────────────────────────────────┘   │
│  [See all visits]  (secondary)         │
│                                         │
└─────────────────────────────────────────┘
│ [Home] [Visits] [Follow-ups] [Patients] [Sync] │  ← Bottom nav (§4)
└─────────────────────────────────────────┘
```

### 2.2 Content rules

- **One primary action:** “New visit” only. No competing CTAs (e.g. no “New visit” and “Add patient” both primary on Home).
- **Today’s follow-ups:** Show due today + overdue; max 5 items; “See all” goes to Follow-ups tab.
- **Recent visits:** Last 3–5 completed or in-progress; “See all” goes to Visits tab.
- **Empty states:** If no follow-ups today: “No follow-ups due today.” If no recent visits: “No visits yet. Start with New visit.”
- **No scroll required** for primary action and first list; secondary lists can scroll.

### 2.3 Task mapping from Home

| CHW intent | Path from Home |
|------------|----------------|
| Start a new visit | Tap “New visit” → Find/Create patient → … (6-step flow) |
| See who I need to see today | “Today’s follow-ups” or tab **Follow-ups** |
| Resume or check a recent visit | “Recent visits” or tab **Visits** |
| Find a patient (no visit yet) | Tab **Patients** |
| Check offline / pending sync | Tab **Sync** or header indicator |

---

## 3. Primary vs Secondary Actions

### 3.1 Definitions

- **Primary:** The one action that advances the main task of the current screen. One per screen (design system). High emphasis (solid fill, 48 px+ height, icon + label).
- **Secondary:** Alternative or supporting actions (back, skip, see all, cancel). Medium emphasis (outline or text; same touch target size).

### 3.2 By screen / context

| Screen / context | Primary action | Secondary actions |
|------------------|----------------|-------------------|
| **Home** | New visit | See all follow-ups, See all visits |
| **Patients (list)** | Add patient (if empty) or first in list for “start visit” | Search, tap patient → profile / Start visit |
| **Find patient (in flow)** | Select patient (tap card) | Create new patient |
| **Create patient** | Save & start visit | Back |
| **Capture (visit)** | Continue (to notes/suggestions) | Back, Voice, tap symptom chips |
| **Review notes** | Continue to suggestions | Back, edit fields |
| **Suggestions** | Accept / Skip per suggestion; then “Continue to summary” when done | Back |
| **Summary** | Complete visit | Back, change follow-up |
| **Follow-ups tab** | Mark done (per row) or tap row | Filter: Today / Overdue / Upcoming |
| **Visits tab** | Tap visit → detail or resume | Filter by date (optional) |
| **Sync** | Sync now (when online) | — |

### 3.3 Rules

- **One primary CTA per screen** in the main content area (excluding bottom nav).
- **Destructive or urgent** (e.g. “Refer”, “Skip with reason”) can be primary in modals or dedicated steps; danger styling per design system.
- **Bottom nav** is navigation, not “action” — so it doesn’t count as a second primary action on Home.

---

## 4. Navigation Patterns

### 4.1 Recommended: Bottom navigation (fixed)

- **Pattern:** Fixed bar at bottom with 5 items: Home, Visits, Follow-ups, Patients, Sync.
- **Rationale:**
  - **One-handed use:** Thumb can reach bottom nav on phones; no stretch to top.
  - **Fast task switching:** One tap to Follow-ups or Visits without going back through Home.
  - **Consistency:** Same five destinations everywhere; CHW always knows where to go.
- **Spec:** Min 48 px height; icon + label per item; current section highlighted (e.g. primary colour or bold). Active state clearly distinct.

### 4.2 Within a flow (visit flow)

- **Pattern:** Linear flow with **back** in header (top-left). No bottom nav inside the 6-step visit (Find patient → … → Complete visit).
- **Rationale:** Visit is a single task; back = previous step; “Home” or “Exit” can be in header as secondary (e.g. “Cancel” or “Leave” with confirm if in progress).
- **After completion:** Redirect to Home or Visits tab; show short success state (“Visit saved”) then land on Home.

### 4.3 Lists (Visits, Follow-ups, Patients)

- **Pattern:** List screen with optional filter/segment (e.g. Today / Overdue). Tap row → detail or next action (e.g. patient → Start visit; follow-up → Mark done).
- **Back:** Header back to previous screen or to tab root. No breadcrumbs; keep depth shallow (list → detail, max two levels before action).

### 4.4 What to avoid

- **Hamburger menu** for main destinations: hides navigation and adds a tap; bottom nav is always visible.
- **Tabs at top** on mobile: harder to reach one-handed; bottom preferred.
- **Nested tabs** (e.g. tabs inside a tab): use segments or a single list with filters instead.
- **More than 5 top-level items:** collapse “Settings” or “More” into Sync/profile if needed.

---

## 5. How Offline Status Is Surfaced Visually

### 5.1 Principle

- **Always visible** so the CHW knows data is stored locally and will sync later.
- **Calm and informative**, not alarming. Offline is normal in rural use.
- **Redundant:** Icon + short label; not colour-only (colour-blind safe, grayscale).

### 5.2 Placement

| Location | What is shown |
|----------|----------------|
| **Header (every screen)** | Small status: **Online** (green dot + “Synced” or “Online”) or **Offline** (grey dot + “Offline” or “No connection”). Optional: “Pending: 3” when offline or sync queued. |
| **Sync tab** | Full status: “You’re offline. Visits and patients are saved on this device.” “Pending: N visits, M patients.” “When you have connection, data will sync.” Button: “Sync now” (enabled only when online). “Last synced: [date/time]” when online. |

### 5.3 Visual spec

| State | Icon | Label | Colour | Notes |
|-------|------|-------|--------|--------|
| **Online, synced** | Check or cloud-done | “Synced” or “Online” | Success (#2D8A5E) | No animation |
| **Online, pending** | Cloud + number | “Pending: 3” or “3 to sync” | Warning (#B86B1A) | Optional subtle pulse |
| **Offline** | Cloud-off or signal-off | “Offline” | Muted (#5C6B64) | Not danger red; offline is expected |
| **Sync in progress** | Spinner or cloud-sync | “Syncing…” | Primary | Disabled “Sync now” |

- **Header:** Single line: icon + text; no long sentence. Tap → can open Sync tab or a small tooltip (“Data saves on device. Syncs when online.”).
- **No blocking overlay** for offline: CHW can use the app fully; banner or header is enough.
- **After reconnect:** Briefly show “Synced” or “Sync complete” (toast or header update); then return to normal “Synced” state.

### 5.4 Edge cases

- **Sync failed (e.g. server error):** Show “Sync failed. Will retry.” in Sync tab; optional warning colour; “Sync now” to retry.
- **Conflict (future):** Handled in Sync tab or a dedicated message; not in header.
- **First load offline:** App works; header shows Offline from first screen. No “you must be online” block.

---

## 6. Summary: IA at a Glance

| Area | Decision |
|------|----------|
| **Top-level (max 5)** | Home, Visits, Follow-ups, Patients, Sync |
| **Home** | One primary CTA (New visit); Today’s follow-ups; Recent visits; empty states |
| **Primary vs secondary** | One primary per screen; secondary = Back, See all, Skip, etc.; table by screen |
| **Navigation** | Bottom nav (fixed, 5 items); linear flow + header back in visit; list → detail |
| **Offline** | Header: icon + “Offline”/“Synced”/“Pending: N”; Sync tab: full message + “Sync now”; calm, non-alarming |

This IA supports **one-handed use**, **fast switching** between daily workload, visits, and follow-ups, and **clear offline awareness** without blocking the CHW.
