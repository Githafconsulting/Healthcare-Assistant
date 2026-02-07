# CHW Web App — Visual & UX Design System

Design specification for the Community Health Worker healthcare web app in rural Africa. **Trust and clarity over aesthetics.** Mobile-first, low bandwidth, bright outdoor use, low digital literacy, long days, cognitive fatigue.

---

## 0. Product Character — How It Should Feel

The product should feel: **Calm. Human. Reliable. Unflashy.** Every screen and interaction should reinforce this.

| Feeling | What it means in the product |
|--------|------------------------------|
| **Calm** | No alarm unless truly critical. Muted colours for normal states; reserved use of red. No flashing, no aggressive motion. Status (offline, sync, reminders) is visible but quiet. Copy is reassuring (“Saved on device”, “Will sync when you have connection”) not urgent. |
| **Human** | Feels like a helpful colleague, not a system. Short, plain-language copy. “We wrote this from what you said” not “Data extracted.” AI suggests; the CHW decides. Warm neutrals, soft shapes, readable type. No cold or clinical jargon. |
| **Reliable** | The CHW can trust that data is safe, actions are confirmed, and the next step is always clear. One primary action per screen. Explicit “Done” and “Saved” states. Offline works; sync is transparent. No surprising behaviour or hidden steps. |
| **Unflashy** | No decorative animation, no marketing-style gradients, no “wow” effects. Solid colours, simple icons, clear hierarchy. Content and task completion matter; the UI stays out of the way. Familiar patterns (buttons, lists, cards) over novel ones. |

**In one line:** The app should feel like a dependable, low-stress tool that supports the CHW without demanding attention or causing anxiety.

---

## 1. Design Principles (Max 6)

| # | Principle | Rationale |
|---|-----------|-----------|
| **1** | **One primary action per screen** | Reduces cognitive load and mistakes. CHW always knows the next step. No competing buttons or links. |
| **2** | **High contrast, large touch targets (min 48×48 dp)** | Works in bright sun and with tired eyes. Thumbs and varying accuracy; no mis-taps. WCAG AA and low-literacy friendly. |
| **3** | **Progressive disclosure — show only what’s needed now** | Never overwhelm. Details (dosing, guidelines) on tap/expand. List → one item → actions. |
| **4** | **Icon + color + short label together** | Low literacy: meaning from shape and color first, text confirms. No icon-only or text-only critical actions. |
| **5** | **Calm, human, non-clinical** | Matches product character (§0): calm, human, reliable, unflashy. Reduces anxiety; feels like a helpful tool, not a hospital form. Soft shapes, warm neutrals, clear hierarchy. |
| **6** | **Works in grayscale and is color-blind safe** | Don’t rely on color alone. Use pattern, position, icon, and label so meaning is clear without color. |

---

## 2. Color Palette & Usage Rules

### 2.1 Palette (color-blind safe, high contrast)

| Role | Hex | Use | Do not |
|------|-----|-----|--------|
| **Primary** | `#0D6B5C` | Main actions (Start visit, Save, Continue), header, key CTAs | Large decorative blocks |
| **Primary dark** | `#084A41` | Hover/active primary, gradient end | Text on light |
| **Success / safe** | `#2D8A5E` | Completed, accepted, “yes”, follow-up done | Warnings or errors |
| **Warning / attention** | `#B86B1A` | Caution, review needed, optional step | Critical danger |
| **Danger / urgent** | `#C44536` | Danger signs, referral, stop, skip confirm | Success or neutral |
| **Neutral text** | `#1F2924` | Body and headings | On dark backgrounds without contrast check |
| **Muted text** | `#5C6B64` | Hints, labels, secondary info | Primary actions |
| **Surface** | `#FFFFFF` | Cards, inputs, modals | Only with border or shadow for separation |
| **Background** | `#F8F6F3` | Page background | Text (too low contrast) |
| **Border** | `#E4E6E5` | Dividers, input borders | Primary actions |

### 2.2 Usage rules

- **Contrast:** Body text on background or surface ≥ 4.5:1; large text ≥ 3:1. Primary and danger on white ≥ 4.5:1.
- **Color is redundant:** Every state has an icon or label (e.g. danger = icon + “Urgent” + red).
- **Grayscale test:** All meaning (primary action, danger, success) must be clear when viewed in grayscale (pattern/position/icon).
- **Bright outdoor:** Prefer dark text on light background; avoid large pure white (#FFF) areas that glare; soft off-white (#F8F6F3) for background.
- **Bandwidth:** No decorative images or gradients in critical path; solid colors and CSS only for core UI.

### 2.3 Semantic mapping

| Meaning | Color | Icon (example) | Label (example) |
|---------|-------|------------------|-----------------|
| Do / continue | Primary | Arrow, check | “Continue”, “Save” |
| Done / safe | Success | Check | “Done”, “Accepted” |
| Be careful | Warning | Alert triangle | “Check”, “Review” |
| Urgent / danger | Danger | Alert circle, stop | “Refer”, “Danger” |
| Informational | Muted | Info (optional) | “Tip”, “Note” |

---

## 3. Typography — Choices & Hierarchy

### 3.1 Font choice

- **Primary:** System font stack for **zero extra bandwidth** and familiarity:  
  `-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif`
- **Alternative (if one web font is allowed):** A single **sans-serif** (e.g. **DM Sans** or **Source Sans 3**) for a calmer, human tone; load once, subset Latin + target script. Fallback to system.

### 3.2 Scale (mobile-first, large by default)

| Level | Size | Weight | Line height | Use |
|-------|------|--------|-------------|-----|
| **H1 / Screen title** | 22–24 px | 700 | 1.2 | One per screen, e.g. “New Visit” |
| **H2 / Section** | 17–18 px | 700 | 1.3 | Section headings |
| **Body** | 16 px | 400 | 1.5 | Paragraphs, list content |
| **Body large** | 17 px | 500 | 1.5 | Emphasised body (e.g. patient name) |
| **Small / caption** | 14 px | 400 | 1.4 | Hints, labels, metadata |
| **Overline / label** | 13 px | 600 | 1.3 | Uppercase labels, e.g. “RECENT PATIENTS” |

- **Minimum body:** 16 px to support accessibility and reduce pinch-zoom.
- **No long lines:** Max ~45–50 characters per line on mobile; use readable width (e.g. max-width 480 px).

### 3.3 Hierarchy rules

- One **H1** per screen; it answers “Where am I?”
- **H2** for sections (e.g. “Recent patients”, “Suggestions”).
- **Short sentences;** avoid blocks of text. Bullets over paragraphs where possible.
- **No bold for long stretches;** use for one-word or short phrase emphasis only.

---

## 4. Iconography — Style Guidelines

### 4.1 Style

- **Simple, filled or high-weight outline.** Same stroke weight across a set (e.g. 2 px).
- **Recognition over realism.** Universal metaphors: plus = add, check = done, arrow = next, house = home, person = patient.
- **Size:** Min 24 px for tappable; 20–24 px next to labels. Critical actions: 28–32 px when icon is primary.
- **Consistent set:** Use one icon set (e.g. Material Icons, Phosphor, or custom) so style is uniform.

### 4.2 Usage

- **Icon + label** for all primary actions (no icon-only for “Continue”, “Save”, “Refer”).
- **Redundant coding:** Danger = red + alert icon + word “Urgent” or “Danger”.
- **Avoid:** Medical or clinical symbols that scare (e.g. skull, syringe); use “alert” or “warning” shapes instead.
- **Positive framing:** Prefer “Continue”, “Done”, “Next” over “Submit”, “Confirm” where it fits.

### 4.3 Key icons (suggested)

| Context | Icon | Meaning |
|---------|------|---------|
| New / add | Plus | New visit, new patient |
| Next / continue | Arrow right | Go to next step |
| Back | Arrow left / chevron | Previous screen |
| Voice | Mic | Start/stop voice capture |
| Patient | Person | Patient list, profile |
| Success | Check in circle | Completed, accepted |
| Danger | Alert circle / exclamation | Urgent, referral |
| Warning | Triangle | Review, caution |
| Sync | Cloud / refresh | Pending sync, sync now |
| Home | House | Home / dashboard |

---

## 5. Spacing & Layout Rules

### 5.1 Spacing scale (multiples of 4)

- **4 px** — Inline gaps (icon–text, tag spacing).
- **8 px** — Tight grouping (label to input).
- **12 px** — Within-card padding, list item padding.
- **16 px** — Default padding (screen edge, card internal).
- **24 px** — Section spacing (between blocks).
- **32 px** — Screen edge padding (mobile); before first content block.
- **48 px** — Major section separation; space before primary CTA.

Use the scale consistently so the layout feels predictable and calm.

### 5.2 Layout

- **Max width:** 480 px for content; centre on larger screens. Prevents long lines and keeps focus.
- **Single column** on mobile; no side-by-side forms or competing columns.
- **Sticky header only** (and optional sticky primary CTA at bottom). No multiple sticky bars.
- **Touch targets:** Min **48×48 px** (dp); prefer 52–56 px for primary actions. Space between tappable elements min 8 px.
- **Cards / lists:** One card per concept (e.g. one patient per card). Clear separation (border or shadow), left accent stripe for status optional (e.g. primary = active).

### 5.3 Progressive disclosure

- **Lists:** Show title + one line of detail; tap opens detail or next step.
- **Forms:** Group in sections; one section visible at a time for long flows, or accordion “Optional” sections.
- **Alerts:** One at a time; single primary action (e.g. “Refer” or “OK”). No stacked modals.
- **Dosing / guidelines:** Short line in list; “Show details” expands full text so screen isn’t crowded by default.

---

## 6. Component Tone — Buttons, Alerts, Cards

### 6.1 Buttons

| Type | Visual | Use |
|------|--------|-----|
| **Primary** | Solid primary colour; 48 px+ height; bold label; icon optional (e.g. arrow) | One main action per screen: “Continue”, “Save visit”, “New visit” |
| **Secondary** | Outline (2 px) primary or neutral; same height | Alternative: “Back”, “Skip”, “Cancel” |
| **Danger** | Solid danger colour | Destructive or urgent: “Refer urgently”, “Mark as danger” |
| **Ghost / tertiary** | No border; muted text | Low emphasis: “Change”, “Edit” |

- **Tone:** Action-oriented labels: “Continue”, “Save”, “Start visit”. Avoid “Submit”, “OK” where a clearer verb fits.
- **State:** Visible pressed/active (darker or scale 0.98); disabled = muted colour + reduced opacity; loading = spinner or disabled state, no double-tap.

### 6.2 Alerts

| Severity | Background | Border | Icon | Tone |
|----------|------------|--------|------|------|
| **Danger** | Light red (#FEF0EE) | 2 px danger | Alert circle | Short sentence + one primary action (“Refer”, “Confirm”) |
| **Warning** | Light amber (#FEF6E8) | 2 px warning | Triangle | “Check this” or “Review”; action optional |
| **Success** | Light green (#E8F5EE) | 2 px success | Check | “Done” or “Saved”; no action or “Continue” |
| **Info** | Light neutral (#F5F5F5) | 2 px border | Info (optional) | Tip or hint; dismiss or “Got it” |

- **One alert at a time.** No stacking. Auto-dismiss only for success/info if non-critical.
- **Copy:** Short (one line if possible). No jargon. Example: “Child may need urgent referral. Confirm?” not “Danger sign detected: possible severe classification.”

### 6.3 Cards

- **Purpose:** One idea per card (one patient, one suggestion, one visit).
- **Structure:** Optional left accent (4 px) for status; padding 16 px; title bold, detail muted.
- **Tap target:** Whole card tappable where the whole row is one action; else one clear button inside.
- **Tone:** Neutral surface (#FFF) on soft background (#F8F6F3); subtle shadow or border so cards don’t float. Not “flashy” — calm and scannable.

### 6.4 Inputs

- **Height:** Min 48 px. Large tap target for focus and thumb.
- **Label:** Above field, 14 px, muted; required marked (e.g. asterisk) if needed.
- **Border:** 2 px; focus = primary colour + optional ring. Error = danger border + short message below.
- **Placeholder:** Hint only; never the only label. Same or lighter muted colour.

### 6.5 Lists (e.g. patients, suggestions)

- **One row per item;** no nested tables. Icon or avatar left, title + one line detail, optional chevron.
- **Order:** Clear (e.g. recent first, or urgent first for suggestions).
- **Empty state:** One short line + one action, e.g. “No patients yet. Add first patient.”

---

## 7. Quick Reference Checklist

- [ ] One primary action per screen
- [ ] Touch targets ≥ 48×48 px; spacing between ≥ 8 px
- [ ] Text contrast ≥ 4.5:1 (body), 3:1 (large)
- [ ] Meaning clear in grayscale (icon + label + position)
- [ ] Body text ≥ 16 px; one H1 per screen
- [ ] No critical action that is icon-only or colour-only
- [ ] Alerts: one at a time; short copy; one primary action
- [ ] Background off-white (#F8F6F3) to reduce glare; dark text on light
- [ ] Max content width 480 px; single column
- [ ] Progressive disclosure: show only what’s needed; expand for details

This design system keeps the CHW web app **calm, clear, and trustworthy** while meeting **WCAG AA**, **low bandwidth**, **small screens**, and **bright outdoor** use in low-resource settings.
