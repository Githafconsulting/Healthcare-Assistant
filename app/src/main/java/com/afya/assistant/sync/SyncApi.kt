package com.afya.assistant.sync

import com.afya.assistant.data.local.AuditEntity
import com.afya.assistant.data.local.PatientEntity
import com.afya.assistant.data.local.VisitEntity
import retrofit2.http.*

/**
 * Simple sync API - just upload endpoints.
 * Download/conflict resolution can be added when needed (YAGNI).
 */
interface SyncApi {
    
    @POST("api/v1/patients")
    suspend fun uploadPatient(@Body patient: PatientEntity)
    
    @POST("api/v1/visits")
    suspend fun uploadVisit(@Body visit: VisitEntity)
    
    @POST("api/v1/audit/batch")
    suspend fun uploadAuditBatch(@Body entries: List<AuditEntity>)
    
    @GET("api/v1/health")
    suspend fun healthCheck(): HealthResponse
}

data class HealthResponse(val status: String)
