package com.afya.assistant.domain.models

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * A clinical visit - simplified to essential data.
 */
data class Visit(
    val id: String = UUID.randomUUID().toString(),
    val patientId: String,
    val chwId: String,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    
    // Clinical data
    val symptoms: List<Symptom> = emptyList(),
    val vitals: Vitals? = null,
    val dangerSigns: List<String> = emptyList(),
    
    // Outcome
    val assessment: String? = null,
    val treatment: String? = null,
    val referral: Referral? = null,
    val notes: String? = null,
    
    // Sync
    val synced: Boolean = false
)

@Serializable
data class Symptom(
    val name: String,
    val present: Boolean,
    val duration: String? = null,  // "3 days", "1 week"
    val severity: Severity? = null
)

@Serializable
enum class Severity { MILD, MODERATE, SEVERE }

@Serializable
data class Vitals(
    val temperature: Float? = null,      // Celsius
    val respiratoryRate: Int? = null,    // per minute
    val weight: Float? = null,           // kg
    val muac: Int? = null                // mm (mid-upper arm)
)

@Serializable
data class Referral(
    val facility: String,
    val urgency: Urgency,
    val reason: String
)

@Serializable
enum class Urgency { EMERGENCY, URGENT, ROUTINE }
