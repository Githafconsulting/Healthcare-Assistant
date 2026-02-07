# AI Guidance Presentation — Design Specification

Design for how clinical guidance is shown to the CHW. **Goals:** Never feel authoritative; always explain “why”; require human confirmation. Aligns with [clinical decision support](CLINICAL_DECISION_SUPPORT.md), [design system](CHW_WEBAPP_DESIGN_SYSTEM.md), and [community care network](COMMUNITY_CARE_NETWORK.md).

**Visual tone:** Calm, supportive, non-alarming unless critical.

---

## 1. Core Principles

| Principle | Implementation |
|-----------|----------------|
| **Never authoritative** | Wording: “Consider…”, “We suggest…”, “Refer because…”. Never “You must” or “Diagnosis: X”. The CHW decides. |
| **Always explain why** | Every card has a visible “Why” line and optional “Based on” (inputs that triggered the suggestion). |
| **Require human confirmation** | No action is recorded until the CHW taps Accept or Skip. Critical referrals get explicit confirmation; completion blocked or warned until handled. |
| **Calm unless critical** | Critical (danger/refer) uses reserved danger color and clear urgency; all other levels use supportive, low-alarm visuals. |

---

## 2. Guidance Card Design

### 2.1 Card structure (single recommendation)

Each suggestion is one **card**. Content order is fixed so the CHW always finds “what to do” and “why” in the same place.

```
┌─────────────────────────────────────────────────────────────┐
│ [Risk badge]                                    [Source ref] │  ← Top: level + guideline
├─────────────────────────────────────────────────────────────┤
│  Consider ORS + zinc for 10–14 days                          │  ← Title (action, not diagnosis)
│                                                              │
│  What to do: Give ORS Plan B; amount per age. Give zinc      │  ← One–two sentences
│  for 10–14 days.                                             │
│                                                              │
│  Why: Child has diarrhea with some dehydration.               │  ← Always present
│                                                              │
│  Based on: Diarrhea · Sunken eyes · Skin pinch slow  [⋮]     │  ← Inputs; expand for more
│                                                              │
│  From: WHO IMCI Plan B                                       │  ← Source (e.g. WHO)
├─────────────────────────────────────────────────────────────┤
│  [  I did this  ]   [  Skip  ]                               │  ← Confirmation actions only
└─────────────────────────────────────────────────────────────┘
```

- **Risk badge:** Right or left of card header; icon + short label (see §3). Color supports meaning but is redundant with label.
- **Source ref:** Short, e.g. “WHO IMCI”, “WHO malaria”, “[National]”. Tappable for optional “About this guideline” (one short sentence or link to local doc).
- **Title:** Action-oriented, not diagnostic. Examples: “Consider treating for malaria”, “Urgent referral”, “Counsel on cough and when to return”.
- **What to do:** Plain language; 1–2 sentences. No jargon without explanation.
- **Why:** One sentence. “Because: [reason].” or “Why: [reason].”
- **Based on:** Bullet or chip list of inputs (e.g. “Fever, RDT positive, no danger signs”). Optional expand (⋮) for full rule detail if needed.
- **From:** Always show guideline/source. No suggestion without a source.

### 2.2 Card variants

| Variant | When | Visual difference |
|---------|------|-------------------|
| **Standard** | Treatment or counsel (Medium/Low risk) | Neutral card border; risk badge only in header. |
| **Referral** | Any referral suggestion | Same structure; title starts with “Refer” or “Urgent referral”; optional small “Referral” label. |
| **Critical** | Critical risk (danger sign, severe classification) | Left border or full header in danger color; “Urgent” in badge; no decorative imagery, just clear typography. |

All variants use the same confirmation block: **I did this** (primary) and **Skip** (secondary).

### 2.3 No long text blocks

- “What to do”: max 2–3 sentences; break into bullets if needed.
- “Why”: one sentence.
- “Based on”: 3–5 items visible; “More” or expand for rest.
- Guideline name: short string only; detail on tap.

### 2.4 Order of cards on screen

1. **Critical** first (urgent referrals, danger signs).
2. Then **High**, then **Medium**, then **Low**.
3. Within same level: referral before treatment before counsel.

One primary CTA per screen when possible: e.g. “Continue to treatment” after reviewing all cards, but each **card** has its own “I did this” / “Skip” so confirmation is per suggestion.

---

## 3. Color Usage for Risk Levels

Use color **only for meaning**; every level has an **icon + label** so meaning is clear in grayscale and for color-blind users (per design system).

### 3.1 Risk level mapping

| Level | Label (short) | Color (hex) | Icon | Use |
|-------|----------------|-------------|------|-----|
| **Critical** | Urgent | `#C44536` (Danger) | Alert circle / exclamation | Refer immediately; danger sign; severe classification. Use sparingly. |
| **High** | Attention | `#B86B1A` (Warning) | Alert triangle | Needs facility or close follow-up; referral or first dose + refer. |
| **Medium** | Routine | `#0D6B5C` (Primary) or muted | Info or document | Treatment at community level (e.g. ORS, ACT, paracetamol). |
| **Low** | OK | `#2D8A5E` (Success) | Check or tick | Counsel only; schedule next visit. |

- **Critical** is the only level that should feel “alarming” — reserved for immediate referral/danger. Red left border or header strip; badge “Urgent” with danger color.
- **High** is “please pay attention” but not panic: warning amber, “Attention” label.
- **Medium** and **Low** are calm: primary (teal) or success (green); “Routine” and “OK” so the CHW feels supported, not judged.

### 3.2 Badge style

- Small pill or tag (e.g. 20–24 px height); **icon + 2–4 word label** (“Urgent”, “Attention”, “Routine”, “OK”).
- Same position on every card (e.g. top-right of card header).
- No color-only meaning: label (or icon) always visible.

### 3.3 Non-alarming default

- Most cards will be Medium or Low. Page background stays neutral (`#F8F6F3`); cards white/surface. Only the **risk badge** and optional **critical card border** use strong color.
- Avoid large red areas or flashing. Critical = clear, sober emphasis (border + badge + “Urgent”), not drama.

---

## 4. Referral Suggestions

### 4.1 Referral card content

Referral suggestions use the same card layout with:

- **Title:** “Urgent referral” or “Refer to facility”.
- **What to do:** “Refer to [nearest health facility / clinic] immediately.” (Or from community care network: “Consider: [Clinic name] — 2 km, open today.”)
- **Why:** “Refer because: [trigger condition].” (e.g. “Refer because: general danger sign (unable to drink).”)
- **Based on:** The danger sign(s) or criteria that triggered the suggestion.
- **From:** “WHO IMCI General Danger Signs” or “[NATIONAL_ANC]”.

### 4.2 Where to refer (optional block)

If the app has a **community care network** (clinics, hospitals):

- Below the main referral card, a short section: **“Suggested places”** with 1–3 options (name, distance, “Open today” / “Call first”).
- Each option is a small tappable row; CHW can pick one and then “I referred here” or “I will refer here”.
- No automatic selection; CHW confirms the facility.

### 4.3 Urgency wording

- **Critical:** “Refer immediately” / “Urgent referral”.
- **High (urgent but not emergency):** “Refer soon” / “Refer to facility”.
- Avoid “You must refer” — use “We suggest urgent referral because …”.

---

## 5. Source References (e.g. WHO)

### 5.1 Where sources appear

- **On every card:** “From: [Guideline name]” at bottom of card (e.g. “From: WHO IMCI Plan B”, “From: WHO malaria”, “From: National Malaria Guidelines”).
- **Optional:** Small “i” or “About” next to the source; tap shows one sentence: “This recommendation follows the WHO Integrated Management of Childhood Illness guideline for diarrhea with some dehydration.”

### 5.2 Format

- Short, consistent names: “WHO IMCI”, “WHO malaria”, “WHO ANC”, “WHO PNC”, “[National name]”.
- No long URLs in the main UI; links only in an optional “Guidelines” or “Sources” screen if needed.
- National placeholders (e.g. [NATIONAL_MALARIA]) are replaced with real guideline names in deployment.

### 5.3 Tone

- Sources are for **trust and transparency**, not for intimidating the CHW. “From: WHO IMCI” = “this is where this advice comes from,” not “you must obey.”

---

## 6. Confirmation Interaction Pattern

### 6.1 Per-suggestion confirmation

- Every guidance card has **two actions only:** **I did this** (primary) and **Skip** (secondary).
- **I did this:** Records that the CHW accepted and (in their practice) performed or will perform the action. No “Are you sure?” for accept; the tap is confirmation.
- **Skip:** Opens a **reason** step: fixed list (e.g. “Already given”, “Not available”, “Patient refused”, “Not needed”, “Other”) or short free text. Required before closing the card or proceeding. No blame; “Skip” is neutral.

### 6.2 Critical referral

- **Stricter option:** Visit cannot be marked “Complete” until the critical referral is either “I referred” / “I will refer” or “Skip” with reason.
- **Softer option:** Allow completion but show a clear line: “You have an urgent referral suggestion. Refer or record why not.” with a link back to the referral card.
- No silent dismissal of critical suggestions; audit log records Accept or Skip + reason.

### 6.3 Summary before complete visit

- Before “Complete visit,” show a **summary screen**: list of **accepted** actions (what the CHW did) and **skipped** ones with reasons.
- One primary button: “Complete visit”. CHW confirms the summary, then taps to complete.
- Short line at bottom: “You are responsible for the final decision. This tool only suggests actions based on guidelines.”

### 6.4 Override and responsibility

- CHW can **always** skip any suggestion.
- Skip always requires a reason (fixed list or short text).
- No suggestion is ever shown as “Diagnosis: X”; only “Consider …” or “Refer because …”.
- Responsibility statement visible on summary or in app footer: “You are responsible for the final decision. This tool only suggests actions based on guidelines.”

---

## 7. Visual Tone Summary

| Aspect | Implementation |
|--------|----------------|
| **Calm** | Neutral background; white cards; strong color only in risk badge and critical border. No flashing or large red blocks. |
| **Supportive** | “Consider…”, “We suggest…”, “Here’s what we recommend.” Success/primary for routine and OK levels. |
| **Non-alarming unless critical** | Critical = danger color + “Urgent” + clear “Refer immediately”. All other levels = teal/green/amber in small doses; labels “Routine”, “OK”, “Attention”. |
| **Explain why** | Every card has “Why” and “Based on”; source “From: …” on every card. |
| **Human in charge** | “I did this” / “Skip”; summary before complete; responsibility statement. |

---

## 8. Consistency with Design System

- **One primary action per card:** “I did this”. “Skip” is secondary (outline or muted).
- **Min 48 px touch targets** for “I did this”, “Skip”, and any “Based on” expand.
- **Icon + label** for risk (badge with icon + “Urgent”/“Attention”/“Routine”/“OK”).
- **Color for meaning only:** Critical = danger; High = warning; Medium/Low = primary/success. No decorative color.
- **Progressive disclosure:** “Based on” can expand; “About this guideline” on tap.
- **No long blocks:** What to do (2–3 sentences), Why (1), Based on (3–5 items visible).

This specification defines **guidance card design**, **risk level colors and labels**, **referral presentation**, **source references**, and the **confirmation pattern** so the UI never feels authoritative, always explains why, and requires explicit human confirmation.
