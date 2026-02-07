package com.afya.assistant.domain.models

import java.util.UUID

/**
 * AI suggestion with required explainability.
 * Kept simple - one class instead of nested hierarchy.
 */
data class Suggestion(
    val id: String = UUID.randomUUID().toString(),
    val type: SuggestionType,
    val title: String,
    val description: String,
    
    // Explainability (required)
    val reason: String,              // Why this suggestion
    val basedOn: List<String>,       // What data led to this
    val guidelineRef: String,        // e.g., "WHO IMCI - Diarrhea"
    
    // Confidence
    val confidence: Confidence,
    
    // Safety
    val isUrgent: Boolean = false,
    
    // Tracking
    val accepted: Boolean? = null,   // null = not yet responded
    val chwResponse: String? = null  // reason if rejected
)

enum class SuggestionType {
    DANGER_SIGN,    // Immediate attention needed
    ASSESSMENT,     // Possible condition
    TREATMENT,      // Suggested treatment
    REFERRAL,       // Should refer
    QUESTION        // Ask patient this
}

enum class Confidence { HIGH, MEDIUM, LOW }
