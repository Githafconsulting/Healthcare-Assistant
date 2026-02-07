package com.afya.assistant.audit

import com.afya.assistant.data.local.AppDatabase
import com.afya.assistant.data.local.AuditEntity
import com.afya.assistant.domain.models.AuditEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Simple audit logger. Fire-and-forget logging of all actions.
 */
@Singleton
class AuditLogger @Inject constructor(
    private val database: AppDatabase
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    
    /**
     * Log an action. Non-blocking.
     */
    fun log(
        chwId: String,
        action: String,
        entityType: String,
        entityId: String,
        details: String? = null
    ) {
        scope.launch {
            try {
                database.auditDao().insert(
                    AuditEntity(
                        id = UUID.randomUUID().toString(),
                        timestamp = System.currentTimeMillis(),
                        chwId = chwId,
                        action = action,
                        entityType = entityType,
                        entityId = entityId,
                        details = details,
                        synced = false
                    )
                )
            } catch (e: Exception) {
                // Don't crash app for audit failures
            }
        }
    }
    
    // Convenience methods
    fun logPatientViewed(chwId: String, patientId: String) = 
        log(chwId, AuditEntry.PATIENT_VIEWED, "patient", patientId)
    
    fun logVisitStarted(chwId: String, visitId: String, patientId: String) = 
        log(chwId, AuditEntry.VISIT_STARTED, "visit", visitId, "patient:$patientId")
    
    fun logVisitCompleted(chwId: String, visitId: String) = 
        log(chwId, AuditEntry.VISIT_COMPLETED, "visit", visitId)
    
    fun logSuggestionAccepted(chwId: String, suggestionId: String, title: String) = 
        log(chwId, AuditEntry.SUGGESTION_ACCEPTED, "suggestion", suggestionId, title)
    
    fun logSuggestionRejected(chwId: String, suggestionId: String, reason: String) = 
        log(chwId, AuditEntry.SUGGESTION_REJECTED, "suggestion", suggestionId, reason)
    
    fun logDangerSign(chwId: String, visitId: String, dangerSign: String) = 
        log(chwId, AuditEntry.DANGER_SIGN_DETECTED, "visit", visitId, dangerSign)
    
    fun logReferral(chwId: String, visitId: String, facility: String) = 
        log(chwId, AuditEntry.REFERRAL_MADE, "visit", visitId, facility)
}
