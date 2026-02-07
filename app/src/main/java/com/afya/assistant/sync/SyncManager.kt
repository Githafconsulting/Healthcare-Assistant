package com.afya.assistant.sync

import com.afya.assistant.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Simple sync manager - uploads unsynced data when connection available.
 * 
 * Strategy: Last-write-wins for simplicity.
 * Safety data (danger signs in visits) is immutable once created.
 */
@Singleton
class SyncManager @Inject constructor(
    private val database: AppDatabase,
    private val api: SyncApi,
    private val connectivity: ConnectivityMonitor
) {
    
    /**
     * Sync all pending data. Returns count of items synced.
     */
    suspend fun syncAll(): SyncResult = withContext(Dispatchers.IO) {
        if (!connectivity.isConnected()) {
            return@withContext SyncResult(0, 0, "No connection")
        }
        
        var uploaded = 0
        var failed = 0
        
        // Sync patients
        val unsyncedPatients = database.patientDao().getUnsynced()
        for (patient in unsyncedPatients) {
            try {
                api.uploadPatient(patient)
                database.patientDao().markSynced(patient.id)
                uploaded++
            } catch (e: Exception) {
                failed++
            }
        }
        
        // Sync visits
        val unsyncedVisits = database.visitDao().getUnsynced()
        for (visit in unsyncedVisits) {
            try {
                api.uploadVisit(visit)
                database.visitDao().markSynced(visit.id)
                uploaded++
            } catch (e: Exception) {
                failed++
            }
        }
        
        // Sync audit logs (batch)
        val unsyncedAudit = database.auditDao().getUnsynced(limit = 100)
        if (unsyncedAudit.isNotEmpty()) {
            try {
                api.uploadAuditBatch(unsyncedAudit)
                database.auditDao().markSyncedBatch(unsyncedAudit.map { it.id })
                uploaded += unsyncedAudit.size
            } catch (e: Exception) {
                failed += unsyncedAudit.size
            }
        }
        
        SyncResult(uploaded, failed, null)
    }
    
    /**
     * Get pending sync count for UI display.
     */
    suspend fun getPendingCount(): Int = withContext(Dispatchers.IO) {
        database.patientDao().getUnsyncedCount() +
        database.visitDao().getUnsyncedCount() +
        database.auditDao().getUnsyncedCount()
    }
}

data class SyncResult(
    val uploaded: Int,
    val failed: Int,
    val error: String?
)
