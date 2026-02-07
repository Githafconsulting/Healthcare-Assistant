package com.afya.assistant.domain.models

import java.util.UUID

/**
 * Simple audit log entry.
 * Tracks who did what when - especially AI interactions.
 */
data class AuditEntry(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val chwId: String,
    val action: String,           // "viewed_patient", "accepted_suggestion", etc.
    val entityType: String,       // "patient", "visit", "suggestion"
    val entityId: String,
    val details: String? = null,  // JSON or plain text for extra context
    val synced: Boolean = false
) {
    companion object {
        // Common actions
        const val PATIENT_CREATED = "patient_created"
        const val PATIENT_VIEWED = "patient_viewed"
        const val VISIT_STARTED = "visit_started"
        const val VISIT_COMPLETED = "visit_completed"
        const val SUGGESTION_SHOWN = "suggestion_shown"
        const val SUGGESTION_ACCEPTED = "suggestion_accepted"
        const val SUGGESTION_REJECTED = "suggestion_rejected"
        const val DANGER_SIGN_DETECTED = "danger_sign_detected"
        const val REFERRAL_MADE = "referral_made"
    }
}
