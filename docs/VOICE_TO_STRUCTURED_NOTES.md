# Voice-to-Structured-Notes System for CHWs

## Overview

Takes free-form spoken visit notes and produces structured fields for the visit record. **Offline-first**, exposes **confidence** per field, allows **easy CHW correction**, and logs **original audio reference** when audio is stored.

---

## Input → Output (Summary)

| | |
|---|--|
| **Input** | Free-form spoken visit notes; **mixed language** (local + English); **noisy environment** (field conditions, background speech, poor mic). |
| **Output (structured fields)** | **Chief complaint** • **Symptoms** (standardised codes) • **Duration** • **Severity** • **Red flags** • **CHW observations** • **Free text notes** |
| **Constraints** | Works **offline** initially; shows **confidence levels** per field; CHW can **correct easily**; **logs audio reference** when stored. |

---

## 1. Data Schema

### Input (raw capture)

```kotlin
// What we capture from the CHW
data class VoiceCaptureInput(
    val id: String,                    // UUID
    val visitId: String,
    val capturedAt: Long,              // epoch ms
    val languageHint: String?,         // "en", "sw", "mixed"
    val audioFileId: String?,          // null if not stored; otherwise reference to encrypted blob
    val audioDurationMs: Int?,         // if we have audio
    val transcriptRaw: String,         // full ASR output, uncorrected
    val transcriptConfidence: Float?, // 0–1, from ASR if available
    val wasCorrectedByChw: Boolean     // true if CHW edited transcript before processing
)
```

### Output (structured notes)

```kotlin
data class StructuredVisitNotes(
    val id: String,
    val voiceCaptureId: String,        // link back to VoiceCaptureInput
    val visitId: String,
    val generatedAt: Long,

    // --- Structured fields (all with confidence) ---

    val chiefComplaint: StructuredField<ChiefComplaintValue>,
    val symptoms: List<StructuredField<SymptomValue>>,
    val duration: StructuredField<DurationValue>,
    val severity: StructuredField<SeverityValue>,
    val redFlags: List<StructuredField<RedFlagValue>>,
    val chwObservations: List<StructuredField<ObservationValue>>,
    val freeTextNotes: StructuredField<String>,

    // --- Correction & audit ---
    val chwCorrections: List<FieldCorrection>,  // which field, original value, corrected value
    val processingWarnings: List<ProcessingWarning>  // low confidence, ambiguous, etc.
)

// Single field with confidence and source snippet
data class StructuredField<T>(
    val value: T,
    val confidence: ConfidenceLevel,   // HIGH, MEDIUM, LOW
    val confidenceScore: Float,         // 0–1
    val sourceSnippet: String?,         // span of transcript this came from
    val sourceSpan: IntRange?,          // character range in transcript
    val chwAccepted: Boolean?,          // null = not yet shown; true/false after CHW review
    val chwCorrectedValue: T?           // if CHW overrode
)

// --- Value types for structured fields ---

data class ChiefComplaintValue(
    val text: String,                   // e.g. "Child has fever and cough"
    val standardisedCode: String?       // e.g. "FEVER_COUGH" from a small ontology
)

data class SymptomValue(
    val name: String,                   // display name
    val code: String,                   // standardised code (e.g. SNOMED or local list)
    val duration: String?,              // "3 days", "1 week"
    val severity: SeverityLevel?,       // MILD, MODERATE, SEVERE
    val isNegated: Boolean = false      // "no vomiting"
)

data class DurationValue(
    val value: Int,
    val unit: DurationUnit,             // DAYS, WEEKS, HOURS
    val rawText: String                 // "about 3 days"
)

enum class SeverityLevel { MILD, MODERATE, SEVERE }

data class RedFlagValue(
    val code: String,                   // e.g. "UNABLE_TO_DRINK"
    val name: String,
    val mentionedInTranscript: Boolean, // true if explicitly said
    val inferredFromContext: Boolean   // true if derived from other symptoms
)

data class ObservationValue(
    val category: String,               // "general", "skin", "breathing", etc.
    val text: String,
    val standardisedCode: String?
)

// When CHW corrects a field
data class FieldCorrection(
    val fieldName: String,
    val originalValue: String,          // JSON or display string
    val correctedValue: String,
    val correctedAt: Long,
    val chwId: String
)

// When processing is uncertain
data class ProcessingWarning(
    val code: String,                   // "LOW_CONFIDENCE", "AMBIGUOUS_DURATION", "POSSIBLE_RED_FLAG"
    val message: String,
    val fieldName: String?,
    val suggestion: String?
)

enum class ConfidenceLevel { HIGH, MEDIUM, LOW }
```

### Audio reference (when stored)

```kotlin
// Only if policy allows storing audio; otherwise audioFileId remains null
data class AudioReference(
    val id: String,
    val voiceCaptureId: String,
    val filePath: String,               // encrypted, app-private storage
    val durationMs: Int,
    val format: String,                 // "opus", "aac"
    val createdAt: Long,
    val retentionUntil: Long,           // delete after this (e.g. 7 days)
    val loggedInAudit: Boolean          // audit log has "audio_stored" event with id
)
```

### Persistence (Room entities)

- **VoiceCaptureInput** → table `voice_captures`
- **StructuredVisitNotes** → table `structured_notes` (JSON for nested lists or separate tables for symptoms/redFlags/observations)
- **FieldCorrection** → either embedded in StructuredVisitNotes or table `note_corrections`
- **AudioReference** → table `audio_references` (only if storing audio)

---

## 2. AI Processing Steps (Offline Pipeline)

All steps run on-device. No PHI sent to cloud.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  INPUT: transcriptRaw ( + optional audioReference )                          │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│  STEP 1: Normalise & segment                                                 │
│  - Lowercase/trim; detect language chunks (local vs English) if needed        │
│  - Split into sentences or phrases for attribution                            │
│  - Output: normalised transcript + segments[]                                 │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│  STEP 2: Chief complaint extraction                                          │
│  - Match first sentence or “main problem” patterns                            │
│  - Map to standardisedCode from small ontology (e.g. FEVER, DIARRHEA, COUGH)  │
│  - confidence = f(match strength, keyword clarity)                            │
│  - sourceSnippet = segment used                                              │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│  STEP 3: Symptom + duration + severity extraction                            │
│  - Keyword/list-based matcher for symptom codes (multilingual list)           │
│  - Duration: regex + number (“3 days”, “siku 3”, “one week”)                  │
│  - Severity: keywords (“mild”, “sana”, “severe”, “kali”)                       │
│  - Negation: “no X”, “hakuna X” → isNegated = true                            │
│  - Each extraction: confidence, sourceSnippet                                │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│  STEP 4: Red-flag detection                                                  │
│  - Rule list: phrases/codes for danger signs (e.g. not drinking, convulsions) │
│  - mentionedInTranscript = explicit phrase matched                           │
│  - inferredFromContext = e.g. “not drinking” + “vomiting” → possible severe   │
│  - confidence typically MEDIUM for inferred, HIGH for explicit                │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│  STEP 5: CHW observations extraction                                         │
│  - Patterns like “I saw…”, “child looks…”, “mother said…”                     │
│  - Assign category (general, skin, breathing) from keywords                   │
│  - Keep original text; optional standardisedCode if match                     │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│  STEP 6: Free-text notes                                                     │
│  - Full transcript (or cleaned) as fallback free text                         │
│  - confidence = LOW if used as catch-all; HIGH if little else extracted        │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│  STEP 7: Confidence & warnings                                               │
│  - Set confidence per field from extraction quality                          │
│  - Add ProcessingWarning for: low confidence, ambiguous duration,             │
│    possible red flag, multiple interpretations                                │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│  OUTPUT: StructuredVisitNotes ( + processingWarnings )                       │
│  - All fields populated where possible; empty list/low confidence if not     │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Offline implementation notes

- **ASR**: Vosk (or similar) for local speech-to-text; output `transcriptRaw` + optional `transcriptConfidence`. For **noisy environments**: use noise-robust model or preprocessing (e.g. gain, simple VAD); accept that transcript may be incomplete or contain [inaudible] placeholders.
- **Mixed language**: Maintain small **bilingual term lists** (local ↔ English) for symptoms, severity, duration, red flags; normalise to standardised codes; tag segments by language for attribution.
- **Extraction**: Rule-based (keywords, regex, synonym lists for local + English). No LLM required for v1.
- **Noise**: Prefer **robust keywords** and partial matches; **downgrade confidence** when segment is short, fragmented, or ambiguous; add `ProcessingWarning` (e.g. `NOISY_OR_SHORT`) so CHW is prompted to confirm or re-speak.

---

## 3. Example Input → Output

### Example input (transcript only; no audio stored)

**Transcript (mixed Swahili + English):**  
*“Mtoto ana homa tangu siku tatu. Cough pia. Mother says hawezi kunywa vizuri. Niliangalia, skin yake ni dry. Alikuwa na diarrhea jana, sasa kidogo tu.”*

**Normalised / segments (conceptual):**  
1. Mtoto ana homa tangu siku tatu.  
2. Cough pia.  
3. Mother says hawezi kunywa vizuri.  
4. Niliangalia, skin yake ni dry.  
5. Alikuwa na diarrhea jana, sasa kidogo tu.

### Example output (structured notes)

```json
{
  "id": "notes-001",
  "voiceCaptureId": "vc-001",
  "visitId": "v-001",
  "generatedAt": 1700000000000,

  "chiefComplaint": {
    "value": {
      "text": "Child has fever for three days and cough",
      "standardisedCode": "FEVER_COUGH"
    },
    "confidence": "HIGH",
    "confidenceScore": 0.9,
    "sourceSnippet": "Mtoto ana homa tangu siku tatu. Cough pia.",
    "chwAccepted": null,
    "chwCorrectedValue": null
  },

  "symptoms": [
    {
      "value": {
        "name": "Fever",
        "code": "FEVER",
        "duration": "3 days",
        "severity": null,
        "isNegated": false
      },
      "confidence": "HIGH",
      "confidenceScore": 0.95,
      "sourceSnippet": "homa tangu siku tatu",
      "chwAccepted": null,
      "chwCorrectedValue": null
    },
    {
      "value": {
        "name": "Cough",
        "code": "COUGH",
        "duration": null,
        "severity": null,
        "isNegated": false
      },
      "confidence": "HIGH",
      "confidenceScore": 0.9,
      "sourceSnippet": "Cough pia",
      "chwAccepted": null,
      "chwCorrectedValue": null
    },
    {
      "value": {
        "name": "Poor intake / not drinking well",
        "code": "POOR_INTAKE",
        "duration": null,
        "severity": null,
        "isNegated": false
      },
      "confidence": "HIGH",
      "confidenceScore": 0.85,
      "sourceSnippet": "hawezi kunywa vizuri",
      "chwAccepted": null,
      "chwCorrectedValue": null
    },
    {
      "value": {
        "name": "Diarrhea",
        "code": "DIARRHEA",
        "duration": "yesterday, now a little",
        "severity": "MILD",
        "isNegated": false
      },
      "confidence": "MEDIUM",
      "confidenceScore": 0.75,
      "sourceSnippet": "Alikuwa na diarrhea jana, sasa kidogo tu",
      "chwAccepted": null,
      "chwCorrectedValue": null
    }
  ],

  "duration": {
    "value": { "value": 3, "unit": "DAYS", "rawText": "siku tatu" },
    "confidence": "HIGH",
    "confidenceScore": 0.85,
    "sourceSnippet": "tangu siku tatu",
    "chwAccepted": null,
    "chwCorrectedValue": null
  },

  "severity": {
    "value": "MODERATE",
    "confidence": "LOW",
    "confidenceScore": 0.4,
    "sourceSnippet": null,
    "chwAccepted": null,
    "chwCorrectedValue": null
  },

  "redFlags": [
    {
      "value": {
        "code": "POOR_INTAKE",
        "name": "Not drinking well",
        "mentionedInTranscript": true,
        "inferredFromContext": false
      },
      "confidence": "HIGH",
      "confidenceScore": 0.85,
      "sourceSnippet": "hawezi kunywa vizuri",
      "chwAccepted": null,
      "chwCorrectedValue": null
    }
  ],

  "chwObservations": [
    {
      "value": {
        "category": "skin",
        "text": "Skin is dry",
        "standardisedCode": "DRY_SKIN"
      },
      "confidence": "HIGH",
      "confidenceScore": 0.9,
      "sourceSnippet": "skin yake ni dry",
      "chwAccepted": null,
      "chwCorrectedValue": null
    }
  ],

  "freeTextNotes": {
    "value": "Mtoto ana homa tangu siku tatu. Cough pia. Mother says hawezi kunywa vizuri. Niliangalia, skin yake ni dry. Alikuwa na diarrhea jana, sasa kidogo tu.",
    "confidence": "HIGH",
    "confidenceScore": 1.0,
    "sourceSnippet": null,
    "chwAccepted": null,
    "chwCorrectedValue": null
  },

  "chwCorrections": [],
  "processingWarnings": [
    {
      "code": "POSSIBLE_RED_FLAG",
      "message": "Not drinking well may be a danger sign",
      "fieldName": "redFlags",
      "suggestion": "Confirm with caregiver: can child drink or breastfeed?"
    }
  ]
}
```

### Example 2: Noisy / short input (error-handling illustration)

**Transcript (noisy, fragmented):**  
*“Homa… siku… cough… [inaudible] … not drinking.”*

**Output behaviour:**
- **Chief complaint**: “Fever and cough” (from “homa”, “cough”) with confidence **MEDIUM**; sourceSnippet short.
- **Symptoms**: Fever (duration unclear), Cough; both confidence **MEDIUM**; duration field confidence **LOW** or omitted.
- **Red flags**: “Not drinking well” with confidence **HIGH**; add `ProcessingWarning`: `POSSIBLE_RED_FLAG` + “Confirm: can child drink or breastfeed?”
- **ProcessingWarnings**: `SHORT_TRANSCRIPT` (“Transcript is short; please check if anything is missing”), optionally `AMBIGUOUS_DURATION` (“Duration unclear; confirm with caregiver”).
- **Free text**: Full transcript preserved; CHW can correct structured fields or add details in free text.

This shows how **noisy environment** and **short transcript** lead to lower confidence and explicit warnings while still producing usable structured fields and preserving the option to correct.

### If audio is stored

- `VoiceCaptureInput.audioFileId` = `"audio-001"`.
- `StructuredVisitNotes.voiceCaptureId` = `"vc-001"` → links to that capture, which holds `audioFileId`.
- Audit log: event `voice_audio_stored` with `voiceCaptureId`, `audioFileId`, `retentionUntil`; and later `voice_audio_deleted` when file is removed.

---

## 4. Error Handling Approach

| Scenario | Handling |
|----------|----------|
| **ASR fails or returns empty** | Show “Couldn’t hear clearly. Try again or type.” Store no transcript; no structured run. Optionally keep audio reference if stored for retry later. |
| **Transcript very short (< N words)** | Still run pipeline; set overall or per-field confidence LOW; add warning `SHORT_TRANSCRIPT`. CHW sees “Check if anything is missing.” |
| **No language model for detected language** | Use English fallback; add warning `LANGUAGE_FALLBACK`. Store `languageHint` from user or “unknown”. |
| **No symptom/chief complaint matched** | Chief complaint = full first sentence with confidence LOW; symptoms = []; add warning `NO_SYMPTOMS_EXTRACTED`. Free-text notes = full transcript. |
| **Ambiguous duration** | Parse best guess; set confidence MEDIUM/LOW; add warning `AMBIGUOUS_DURATION` + suggestion “Confirm duration with caregiver.” |
| **Possible red flag in text** | Always extract as red flag with appropriate confidence; add `ProcessingWarning` so CHW must acknowledge or correct. |
| **Pipeline crash** | Catch exception; save raw transcript + `voiceCaptureId` (+ `audioFileId` if any); set structured notes to “failed” with error code; show “Something went wrong. You can type notes below.” and allow full free-text entry. |
| **CHW corrects a field** | Update `chwCorrectedValue` and append to `chwCorrections`; set `chwAccepted = false` for that field. Log in audit (field, original, corrected). |
| **CHW accepts field** | Set `chwAccepted = true`; no correction logged. |
| **Audio storage full** | Do not store new audio; set `audioFileId = null`; log; optionally prompt “Free space to keep audio for corrections.” |
| **Noisy / fragmented transcript** | Run pipeline on available text; set per-field or overall confidence lower; add warning `NOISY_OR_SHORT` with suggestion “Transcript may be incomplete. Please check and add missing details.” Allow CHW to edit transcript and re-run, or correct structured fields directly. |
| **Mixed language unknown term** | Keep in free text; do not force a standardised code; set confidence LOW for that segment; optional warning `UNKNOWN_TERM` with snippet. |

### Confidence thresholds (for UI)

- **HIGH** (e.g. ≥ 0.8): Show as normal; optional “Confirm” for critical fields (e.g. red flags).
- **MEDIUM** (0.5–0.8): Show with “Please check” or icon; CHW should confirm or correct.
- **LOW** (< 0.5): Show as “Uncertain”; encourage correction or re-speaking.

### Audit (for corrections and audio)

- `voice_capture_started`, `voice_capture_completed`, `voice_transcript_corrected`.
- `structured_notes_generated` (notesId, voiceCaptureId, warnings).
- `structured_field_accepted`, `structured_field_corrected` (field, old, new).
- If audio stored: `voice_audio_stored` (id, retentionUntil), `voice_audio_deleted` (id, reason).

---

## 5. Summary

| Item | Design choice |
|------|----------------|
| **Schema** | VoiceCaptureInput (raw) → StructuredVisitNotes with StructuredField&lt;T&gt; per field; ChiefComplaint, Symptom, Duration, Severity, RedFlag, Observation, FreeText; FieldCorrection, ProcessingWarning; optional AudioReference. |
| **Processing** | 7-step offline pipeline: normalise/segment → chief complaint → symptoms/duration/severity → red flags → observations → free text → confidence & warnings. |
| **Example** | Mixed Swahili/English transcript → full structured JSON with confidence, sourceSnippet, and one processing warning for possible red flag. |
| **Errors** | ASR failure → no transcript, retry or type; short/ambiguous/low match → LOW confidence + warnings; pipeline crash → save raw + error, fallback to free text; CHW corrections → chwCorrections + audit; audio full → no store, audioFileId null. |
| **Offline** | ASR and extraction on-device; no PHI to cloud. |
| **Audio reference** | Optional; when stored: audioFileId in VoiceCaptureInput, AudioReference table, audit events, retention and delete. |

This gives a concrete, implementable design for voice-to-structured-notes that fits the CHW app and the existing architecture.
