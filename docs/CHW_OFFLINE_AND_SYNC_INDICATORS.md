# System-Wide Offline and Sync Indicators — Design Specification

Design for **offline** and **sync** status across the CHW app. **Goals:** Build trust; reduce fear of data loss; avoid constant alerts. Aligns with [design system](CHW_WEBAPP_DESIGN_SYSTEM.md) and [information architecture](CHW_WEBAPP_INFORMATION_ARCHITECTURE.md).

**Design rules:** Passive, not noisy; always visible; non-technical language.

---

## 1. Principles

| Principle | Implementation |
|-----------|----------------|
| **Build trust** | State clearly that data is “saved on this device” when offline. After sync: “Your visits are saved” or “Up to date.” No jargon (“upload”, “queue”, “API”). |
| **Reduce fear of data loss** | Reassure that nothing is lost: “Saved on device” when offline; “Will sync when you have connection.” Avoid “Failed” without “Will retry” or “Try again.” |
| **Avoid constant alerts** | No pop-ups or blocking overlays for normal offline. No repeated toasts. Status is **always visible** in one place (header/bar) so the CHW can glance; only **failure** may use a one-time, dismissible message. |
| **Passive, not noisy** | Indicators are small and calm. No animation except a subtle sync-in-progress spinner. No sound or vibration for sync. |
| **Always visible** | At least one indicator (e.g. in header) on every screen so the CHW always knows connection and sync state without searching. |
| **Non-technical language** | Use “Saved on device”, “Synced”, “Syncing…”, “Couldn’t sync – will try again.” Avoid “Offline mode”, “Upload queue”, “Sync failed (403)”. |

---

## 2. States to Show

| State | Meaning | User message |
|-------|---------|---------------|
| **Offline** | No network; app works; data stored locally. | “Saved on device” / “Offline” |
| **Sync in progress** | Upload/download in progress. | “Syncing…” |
| **Sync success** | Last sync completed; nothing pending. | “Synced” / “Up to date” |
| **Sync failure** | Last attempt failed; will retry (or user can retry). | “Couldn’t sync – will try again” |
| **Pending** | Online but data not yet synced (e.g. queue not empty). | “3 visits to sync” / “Saved, will sync” |
| **Data freshness** | When data was last synced (or “Never” if first use). | “Last synced: today 2pm” / “Saved on device” |

---

## 3. Indicator Designs

### 3.1 Global status strip (header)

**Placement:** Same place on every screen: **top-right of the header** (or top bar), next to the screen title. One compact unit: **icon + short label**. Tappable → opens Sync screen (or tooltip).

**Visual:**

| State | Icon | Label (short) | Color | Animation |
|-------|------|----------------|--------|-----------|
| **Synced** | Cloud with check, or check | “Synced” | Success (#2D8A5E) | None |
| **Offline** | Cloud-off, or signal-off | “Saved on device” or “Offline” | Muted (#5C6B64) | None |
| **Pending** | Cloud, or cloud + number | “3 to sync” or “Saved, will sync” | Warning (#B86B1A) | None (or very subtle pulse, 2 s) |
| **Syncing** | Spinner, or cloud-arrows | “Syncing…” | Primary (#0D6B5C) | Subtle spinner only |
| **Sync failed** | Cloud with alert, or alert | “Couldn’t sync” | Warning (#B86B1A) | None |

- **Size:** Icon ~20–24 px; label 1–3 words. Total block fits in header (e.g. max ~120 px width).
- **Icon + label** always together (no icon-only) so meaning is clear in grayscale and for low literacy.
- **Offline is not danger:** Use muted grey, not red. Offline is normal in the field.

### 3.2 Optional: thin status bar (below header)

When **pending** or **offline**, an optional **one-line bar** below the header (same width as screen). Not a full banner; minimal height (~32–40 px).

- **Content:** Same message as header but can be slightly longer: “Saved on device. Will sync when you have connection.” or “3 visits to sync.”
- **Style:** Muted background (#F8F6F3 or #E4E6E5 border); no bright warning strip. Tappable → Sync screen.
- **When to show:** Only when offline or pending (or after a failed sync). When synced, this bar can **collapse** so the header indicator is enough. Reduces noise when everything is fine.

### 3.3 Sync screen (dedicated)

Full status and actions live on the **Sync** tab/screen.

- **When offline:** “You’re offline. Visits and patients are saved on this device. When you have connection, they will sync.”
- **When pending:** “3 visits and 1 patient waiting to sync.” “Sync now” button (enabled when online).
- **When syncing:** “Syncing…” with spinner; “Sync now” disabled.
- **When synced:** “Up to date. Last synced: today at 2:30 pm.” “Sync now” (optional manual refresh).
- **When failed:** “Couldn’t sync. Will try again when you have connection.” “Sync now” to retry. No alarming wording; “Will try again” reassures.
- **Data freshness:** Always show “Last synced: [date/time]” when online and at least one sync has happened; “Never synced” only if applicable (first use).

### 3.4 Data freshness indicators

- **Where:** Header (short): only when relevant — e.g. “Synced” implies “just now” or “recent”; no need for “2 min ago” in the header. On **Sync screen**: explicit “Last synced: [date/time]”.
- **Format (non-technical):** “Today at 2:30 pm”, “Yesterday”, “5 Feb” — not “2025-02-05T14:30:00Z”.
- **When never synced:** “Saved on device. Not synced yet.” (Sync screen). Header can show “Offline” or “Saved on device”.
- **No “stale” warning by default:** Avoid “Data is 3 days old” unless the product explicitly supports “data expiry” rules. For most CHW use, “Last synced: [date]” is enough; CHW can infer.

---

## 4. Placement Rules

### 4.1 Always visible

- **Header (every screen):** One status unit (icon + label) in the **same position** (e.g. top-right). So on every screen the CHW can see at a glance: am I offline? is something syncing? is something waiting?
- **No hiding:** Don’t hide the indicator on “main” screens and show only on Sync. Consistency builds trust.

### 4.2 Single place for “full” status

- **Sync tab/screen:** The only place with “Sync now”, “Last synced” detail, pending count breakdown (e.g. “3 visits, 1 patient”), and failure message with retry. Header stays minimal.

### 4.3 No blocking overlay

- **Offline:** Never block the app or show a full-screen “You are offline”. The app works; header (and optional bar) say “Saved on device”. CHW can continue working.
- **Sync failed:** Don’t block. Show failure in header (“Couldn’t sync”) and on Sync screen with “Sync now”. Optional: one **dismissible** inline message on Sync screen, not a modal.

### 4.4 After reconnect or sync success

- **Option A (minimal):** Header simply switches to “Synced”; no toast.
- **Option B (reassurance):** Short toast or header flash: “Synced” or “Your visits are saved.” Auto-dismiss in 2–3 s. Use at most **once** per sync cycle so it’s not noisy.

---

## 5. Copy Examples (Non-Technical)

### 5.1 Header (short)

| State | Copy (pick one per row) |
|-------|--------------------------|
| Synced | “Synced” / “Up to date” |
| Offline | “Saved on device” / “Offline” |
| Pending | “3 to sync” / “Saved, will sync” |
| Syncing | “Syncing…” |
| Sync failed | “Couldn’t sync” / “Sync didn’t work” |

### 5.2 Sync screen

| State | Copy |
|-------|------|
| Offline | “You’re offline. Visits and patients are saved on this device. When you have connection, they will sync.” |
| Pending | “3 visits and 1 patient waiting to sync.” “Sync now” (button). |
| Syncing | “Syncing…” “Your data is being saved.” |
| Synced | “Up to date.” “Last synced: today at 2:30 pm.” |
| Failed | “Couldn’t sync. Will try again when you have connection.” “Sync now” (button). “Your visits are still saved on this device.” |

### 5.3 Data freshness

| Context | Copy |
|--------|------|
| Last sync time | “Last synced: today at 2:30 pm” / “Last synced: yesterday” / “Last synced: 5 Feb” |
| Never synced | “Not synced yet. Your data is saved on this device.” |
| Reassurance | “Your visits are saved on this device.” “Will sync when you have connection.” |

### 5.4 What to avoid

| Avoid | Use instead |
|-------|--------------|
| “Upload queue” | “3 visits to sync” |
| “Sync failed (403)” | “Couldn’t sync – will try again” |
| “Offline mode enabled” | “Saved on device” / “Offline” |
| “Network unreachable” | “You’re offline” |
| “Data sync completed” | “Synced” / “Up to date” |
| “Retry sync?” (as only message) | “Couldn’t sync. Sync now?” or “Will try again.” |

---

## 6. Behaviour Summary

| Scenario | Header | Optional bar | Sync screen | Pop-up / toast |
|----------|--------|--------------|-------------|----------------|
| **Online, synced** | “Synced” (green) | Hide | “Up to date” + last synced time | None |
| **Online, pending** | “3 to sync” (amber) | “3 visits to sync” (optional) | Pending count + “Sync now” | None |
| **Offline** | “Saved on device” (muted) | “Saved on device. Will sync when you have connection.” (optional) | Full offline message | None |
| **Syncing** | “Syncing…” (spinner) | Hide or “Syncing…” | Spinner + “Syncing…” | None |
| **Sync just finished** | Switch to “Synced” | Hide | Update to “Up to date” | Optional: “Synced” toast once |
| **Sync failed** | “Couldn’t sync” (amber) | Optional: “Couldn’t sync – will try again” | Failure message + “Sync now” | No blocking modal |

---

## 7. Consistency with Design System

- **Icon + label** for every state (no icon-only in header).
- **Color for meaning only:** Success = synced; Muted = offline (not danger); Warning = pending or failed.
- **Min touch target:** Header status area ≥ 48 px tap target; opens Sync or tooltip.
- **One place for actions:** “Sync now” and retry live on Sync screen; header is status only.
- **Calm:** No red for offline; no repeated alerts; optional bar is subtle.

This gives **indicator designs**, **placement rules**, and **copy examples** for system-wide offline and sync so the app builds trust, reduces fear of data loss, and avoids constant alerts while staying passive, always visible, and non-technical.
