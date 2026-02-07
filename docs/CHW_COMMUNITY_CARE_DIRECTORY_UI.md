# Community Care Directory UI — Design Specification

Design for the community care directory: **Traditional healers**, **Pharmacies**, **Clinics** (and optionally **Referral hospitals**). Aligns with [community care network](COMMUNITY_CARE_NETWORK.md) and [design system](CHW_WEBAPP_DESIGN_SYSTEM.md).

**Design requirements:** Neutral presentation; no hierarchy implying superiority; clear “appropriate for” indicators; distance and availability.

**AI role:** Suggest, not instruct.

---

## 1. Core Principles

| Principle | Implementation |
|-----------|----------------|
| **Neutral presentation** | All provider types use the same card and list treatment. No type is visually “above” or “below” another (e.g. no “Tier 1 / Tier 2” or stacked hierarchy). Order is by relevance (e.g. suggested first, then distance), not by type rank. |
| **No superiority implied** | Labels are factual: “Traditional healer”, “Pharmacy”, “Clinic”, “Hospital”. No “primary” vs “alternative” or “formal” vs “informal” in the UI. Each type has an icon and short description of what they offer. |
| **Clear “appropriate for”** | Each provider (or type) shows when it **is** appropriate and when it **is not**. Plain language: “Good for: …” and “Not for: …” (or “Use for: …” / “Do not use for: …”). |
| **Distance and availability** | Every card shows distance (e.g. “2 km”) and/or travel time (“~30 min walk”) and availability summary (“Open Mon–Sat 8–5” or “Call first”). |
| **Suggest, not instruct** | When shown in referral context, list is “Suggested places” or “You might consider”; CHW selects. No “Send patient here” or “You must refer to…”. |

---

## 2. List and Map Layout

### 2.1 Entry points

- **Standalone directory:** Nav item “Care directory” or “Places” → full list/map of all providers (filter by type, area, or search by name).
- **In-context (referral):** After CDS suggests “Refer to facility”, a step “Where to refer?” shows **suggested** providers only (filtered by condition); CHW can “Show all” to open full directory.

### 2.2 List view (default)

- **Layout:** Single column list. Each item is a **provider card** (compact in list, expandable or tap-through to detail).
- **Order (in-context):** Suggested providers first (by relevance: condition match, then distance). Then “More options” (all others in area) if CHW taps “Show all”.
- **Order (standalone):** By distance (nearest first) or by type (group by type: Healers, Pharmacies, Clinics, Hospitals). If grouped, **order of groups is configurable or alphabetical** — not fixed as “Clinics first, then pharmacies, then healers” (to avoid implying order of preference).
- **Filter:** Tabs or chips: “All” | “Traditional healers” | “Pharmacies” | “Clinics” | “Hospitals”. Same visual weight for each tab; no default “best” tab.
- **Search:** Optional search by name or village. Placeholder: “Name or village”.

```
┌──────────────────────────────────────────────────────────────┐
│  [←]  Care directory                        [List] [Map]     │
├──────────────────────────────────────────────────────────────┤
│  [ All ] [ Healers ] [ Pharmacies ] [ Clinics ] [ Hospitals ] │  ← Equal-weight filter
│  [ Search: Name or village                              ]    │
├──────────────────────────────────────────────────────────────┤
│  ┌────────────────────────────────────────────────────────┐  │
│  │  [icon]  Kijiji Health Centre              · 2 km       │  │  ← Provider card (compact)
│  │          Clinic · Open Mon–Sat 8–5                      │  │
│  │          Good for: Fever, diarrhea, cough, ANC         │  │
│  └────────────────────────────────────────────────────────┘  │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  [icon]  Mwana Pharmacy                   · 3 km        │  │
│  │          Pharmacy · Open daily 9–6                       │  │
│  │          Good for: Medicines after CHW/clinic advice    │  │
│  └────────────────────────────────────────────────────────┘  │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  [icon]  Mzee Bakari (village elder)      · 1 km        │  │
│  │          Traditional healer · By visit                  │  │
│  │          Good for: Support, blessings (not for illness) │  │
│  └────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────┘
```

### 2.3 Map view (optional)

- **Toggle:** “List” | “Map” in header or tabs. Same data; map shows pins.
- **Pins:** One icon per provider type (distinct shape or icon: healer, pharmacy, clinic, hospital). **Same size** for all types so no type looks “more important”. Color can differentiate type (muted, not hierarchy: e.g. one tint per type) or one neutral color for all.
- **Tap pin:** Inline popover or bottom sheet with **same provider card** content (name, type, distance, availability, “Good for”). “Open” or “Select” → detail or referral flow.
- **No hierarchy on map:** Pins are not larger or more prominent by type; clustering by distance is fine.

### 2.4 In-context “Where to refer?” layout

- **Heading:** “Suggested places” or “You might consider these places.” Subtext: “Choose one. You decide.”
- **List:** Only providers that match the referral condition (from CDS). Each card shows why it’s suggested: “Good for: [condition in plain language].”
- **Safety:** If the condition is danger sign / urgent, only clinic and hospital appear (no pharmacy or traditional healer as sole option). Optional short line: “For this situation we suggest a clinic or hospital.”
- **Primary action:** CHW taps a card → “Use this for referral” or “Select” → confirm referral; no automatic selection.

---

## 3. Provider Card Design

### 3.1 Card structure (list row or detail)

Each provider is presented in a **card** (or row) with the same structure for every type. No extra “badge” or “recommended” that would imply one type is better.

**Compact (list):**

| Element | Content | Notes |
|---------|---------|--------|
| **Icon** | Type icon (same style for all) | Healer, pharmacy, clinic, hospital — equal size (e.g. 24 px). |
| **Name** | Provider name (+ nameLocal if different) | One line; bold or medium weight. |
| **Type** | “Traditional healer”, “Pharmacy”, “Clinic”, “Referral hospital” | One short label; neutral. |
| **Distance** | “2 km” or “~30 min walk” | Right-aligned or under name. |
| **Availability** | “Open Mon–Sat 8–5” or “Call first” or “24/7” | One line; muted. |
| **Appropriate for** | “Good for: Fever, diarrhea, ANC” (short) | 1 line; expand for full list. |

**Detail (tap-through or expand):**

| Section | Content |
|---------|--------|
| **Header** | Name, nameLocal, type (icon + label), distance, travel time. |
| **Contact** | Phone / WhatsApp if present; “Call” / “WhatsApp” actions. |
| **Availability** | Days and hours; “Best to call first” if in notes. |
| **Good for (appropriate)** | Bullet or chips: conditions from `conditionsAppropriate` in **plain language** (e.g. “Fever with test”, “Diarrhea without danger”, “Refill after clinic said OK”, “Support and blessings after facility care”). |
| **Not for (not appropriate)** | Short list in plain language (e.g. “Danger signs”, “First time fever in child”, “Need for injectables”). |
| **Notes** | Optional “Closed Wed PM”, “Ask for Maria”, etc. |
| **Action** | “Use for referral” (in context) or “Call” / “Directions” (standalone). |

### 3.2 Plain-language “appropriate for” / “not for”

- **Source:** From `conditionsAppropriate` and `conditionsNotAppropriate` (condition codes from CDS). Map codes to short, non-technical phrases.
- **Examples:**
  - Clinic: Good for — “Fever (with test)”, “Diarrhea and cough”, “ANC/PNC”, “Wounds”. Not for — “Danger signs (go to hospital)”.
  - Pharmacy: Good for — “Buy ORS/medicines after CHW or clinic said OK”, “Refill known prescription”. Not for — “First time sick child”, “Danger signs”.
  - Traditional healer: Good for — “Support and blessings”, “Family support after facility care”. Not for — “Fever, diarrhea, or any illness that needs clinic”.
- **Tone:** Factual and neutral. “Not for” is not judgmental; it clarifies scope so the CHW can choose safely.

### 3.3 Visual parity across types

- **Same card layout** for healer, pharmacy, clinic, hospital: icon + name + type + distance + availability + “Good for”.
- **Icon set:** One icon per type (e.g. leaf/person for healer, pill for pharmacy, building for clinic, hospital cross for hospital). Same size and style (filled or outline, consistent).
- **No “star” or “recommended”** on certain types. In referral context, “Suggested” applies to the **list** (“Suggested places”) not to a single type.

---

## 4. Safety Warning Presentation

### 4.1 When to show safety warnings

- **In-context referral:** When the referral reason is **urgent** or **danger sign**, a short safety line may appear so the CHW understands why only some provider types are shown.
- **Provider detail:** When viewing a **traditional healer** or **pharmacy**, a short reminder when the **current** referral is urgent (e.g. “For danger signs, use a clinic or hospital.”). Not a warning on every healer/pharmacy card in the directory when there is no referral context.
- **Cultural note:** When suggesting a traditional healer **in addition to** facility: “Family may also wish to visit [name] for support — after going to clinic or hospital.”

### 4.2 Warning placement and tone

- **Placement:** One line at **top of list** (in-context) or at **top of provider detail** (when opening a healer/pharmacy and referral is urgent). Not on every card in the list.
- **Tone:** Calm, factual. Not alarming; informative.
- **Wording examples:**
  - “For this situation we suggest a clinic or hospital.”
  - “Danger signs need a clinic or hospital. Pharmacies and traditional healers are not for this.”
  - “This place is for support and blessings, not for treating fever or diarrhea.”
- **Visual:** Muted background (e.g. light amber or grey), small icon (info or alert), one to two lines. Icon + text; not a big red banner unless truly critical (e.g. “Do not refer danger sign to pharmacy only”). Prefer **info** style over **warning** when the list is already filtered correctly.

### 4.3 Safety block (provider detail — traditional healer / pharmacy)

On the **detail** screen for a traditional healer or pharmacy, show a short **“When to use”** block:

- **Traditional healer:** “Use for: cultural support, blessings, family support. Not for: treating fever, diarrhea, cough, or any condition that needs clinic or hospital.”
- **Pharmacy:** “Use for: buying medicines after a CHW or clinic has already said what to take. Not for: first assessment of a sick child or danger signs.”

This is **always visible** on detail (not only in referral context) so the CHW has the rule in mind when choosing. Same neutral, factual tone; no hierarchy wording.

### 4.4 No diagnosis

- Warnings refer to **situations** (e.g. “danger signs”, “first time sick child”, “need for clinic”) not to **diagnoses** (e.g. “malaria”, “pneumonia”). Condition codes from CDS are translated to plain language as above.

---

## 5. AI Role: Suggest, Not Instruct

### 5.1 Wording in the UI

| Avoid | Use |
|-------|-----|
| “Refer patient here” | “You might consider this place” / “Suggested” |
| “Send to clinic” | “Suggested places: [list]. Choose one.” |
| “You must use a clinic” | “For this situation we suggest a clinic or hospital.” |
| “Do not use traditional healer” (as sole line) | “Traditional healer is for support, not for this illness. Use a clinic for this.” |

### 5.2 Selection and confirmation

- **CHW selects** the provider. No pre-selection or “best” radio default. Optional: sort suggested list by distance so nearest is first, but CHW still taps to choose.
- **Confirmation:** After tap: “Use [Provider name] for this referral?” with “Yes” / “Choose another”. CHW can add free-text instructions before confirming.
- **Override:** CHW can ignore suggestions and refer elsewhere (e.g. free text “Refer to [name]” or pick a provider not in the suggested list if “Show all” is used). System records referral; it does not block.

### 5.3 Summary line

- At bottom of referral flow or in directory: “You decide where to refer. This list is only a suggestion.”

---

## 6. Consistency with Design System

- **One primary action per context:** In list = tap card to open detail or select; in “Where to refer?” = “Use for referral” after selection.
- **Min 48 px touch targets** for cards, filter tabs, and buttons.
- **Icon + label** for provider type and for “Good for” / “Not for”.
- **Color for meaning only:** No color hierarchy for provider types; muted tint per type on map is optional and equal-weight. Safety line uses info/warning style (muted background + icon).
- **Progressive disclosure:** Compact card in list; full “Good for” / “Not for” and contact in detail.
- **Short copy:** “Good for” and “Not for” in one-line or bullet form; no paragraphs.

---

## 7. Summary

| Output | Content |
|--------|---------|
| **List layout** | Single column; filter tabs (All / Healers / Pharmacies / Clinics / Hospitals) with equal weight; optional search; order by distance or by type (no rank order); “Suggested places” in referral context with “You decide” subtext. |
| **Map layout** | Optional List/Map toggle; pins same size per type; tap pin → same card content; no hierarchy by pin size or color emphasis. |
| **Provider card** | Same structure for all types: icon + name + type + distance + availability + “Good for” (plain language); detail: contact, full “Good for” / “Not for”, notes, “Use for referral” or “Call” / “Directions”. Visual parity; no “recommended” badge by type. |
| **Safety warning** | In-context: one line at top when referral is urgent (“We suggest a clinic or hospital”); on healer/pharmacy detail: “When to use” block (appropriate / not appropriate). Calm, factual tone; icon + text; no diagnosis. |
| **AI role** | Suggest only: “Suggested places”, “You might consider”, “Choose one”, “You decide”. CHW selects and confirms; can override; no instruct language. |

This gives a single specification for the **community care directory UI** (list and map, provider card, safety warning) with **neutral presentation**, **no hierarchy implying superiority**, **clear “appropriate for” indicators**, **distance and availability**, and **suggest-not-instruct** AI role.
