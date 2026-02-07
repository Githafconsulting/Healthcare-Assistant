package com.afya.assistant.domain

import com.afya.assistant.ai.DecisionSupport
import com.afya.assistant.audit.AuditLogger
import com.afya.assistant.data.local.AppDatabase
import com.afya.assistant.data.local.PatientEntity
import com.afya.assistant.data.local.VisitEntity
import com.afya.assistant.domain.models.*
import com.afya.assistant.voice.VoiceCapture
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orchestrates the 6-step visit workflow.
 * Single class, simple state machine.
 */
@Singleton
class VisitWorkflow @Inject constructor(
    private val database: AppDatabase,
    private val decisionSupport: DecisionSupport,
    private val voiceCapture: VoiceCapture,
    private val auditLogger: AuditLogger
) {
    private val json = Json { ignoreUnknownKeys = true }
    
    // Current workflow state
    private val _state = MutableStateFlow<WorkflowState>(WorkflowState.Idle)
    val state: StateFlow<WorkflowState> = _state
    
    // Current visit being built
    private var currentPatient: Patient? = null
    private var currentVisit: Visit? = null
    private var currentSuggestions: List<Suggestion> = emptyList()
    
    private val chwId = "CHW-001"  // TODO: from auth
    
    // ==================== STEP 1: Find/Create Patient ====================
    
    suspend fun searchPatients(query: String): List<Patient> {
        return database.patientDao().search(query).map { it.toDomain() }
    }
    
    fun getRecentPatients(): Flow<List<Patient>> {
        return kotlinx.coroutines.flow.flow {
            emit(database.patientDao().search("").map { it.toDomain() })
        }
    }
    
    suspend fun createPatient(name: String, dateOfBirth: Long, sex: Sex, village: String): Patient {
        val patient = Patient(
            name = name,
            dateOfBirth = kotlinx.datetime.LocalDate.fromEpochDays(dateOfBirth.toInt()),
            sex = sex,
            village = village
        )
        database.patientDao().insert(patient.toEntity())
        auditLogger.log(chwId, "patient_created", "patient", patient.id)
        return patient
    }
    
    suspend fun selectPatient(patientId: String) {
        val entity = database.patientDao().getById(patientId)
        currentPatient = entity?.toDomain()
        if (currentPatient != null) {
            auditLogger.logPatientViewed(chwId, patientId)
            _state.value = WorkflowState.PatientSelected(currentPatient!!)
        }
    }
    
    // ==================== STEP 2: Start Visit ====================
    
    suspend fun startVisit() {
        val patient = currentPatient ?: return
        
        currentVisit = Visit(
            patientId = patient.id,
            chwId = chwId
        )
        
        auditLogger.logVisitStarted(chwId, currentVisit!!.id, patient.id)
        
        // Load last visit for context
        val history = database.visitDao().getByPatient(patient.id)
        
        _state.value = WorkflowState.Capturing(
            patient = patient,
            visit = currentVisit!!,
            lastVisitSummary = null  // TODO: extract from history
        )
    }
    
    // ==================== STEP 3: Capture Symptoms ====================
    
    fun startVoiceCapture() {
        voiceCapture.start()
    }
    
    fun stopVoiceCapture() {
        voiceCapture.stop()
    }
    
    fun getTranscript(): StateFlow<String> = voiceCapture.transcript
    
    /**
     * Extract symptoms from voice transcript.
     * Simple keyword matching - not ML.
     */
    fun extractSymptoms(transcript: String): List<Symptom> {
        val symptoms = mutableListOf<Symptom>()
        val text = transcript.lowercase()
        
        // Simple keyword extraction
        val symptomPatterns = listOf(
            "fever" to "fever",
            "diarr" to "diarrhea",
            "cough" to "cough",
            "vomit" to "vomiting",
            "not drinking" to "poor intake",
            "not eating" to "poor intake",
            "won't drink" to "poor intake",
            "won't eat" to "poor intake",
            "breathing fast" to "fast breathing",
            "difficult breath" to "difficulty breathing"
        )
        
        for ((keyword, symptomName) in symptomPatterns) {
            if (text.contains(keyword)) {
                // Try to extract duration
                val duration = extractDuration(text, keyword)
                symptoms.add(Symptom(name = symptomName, present = true, duration = duration))
            }
        }
        
        return symptoms.distinctBy { it.name }
    }
    
    private fun extractDuration(text: String, afterKeyword: String): String? {
        // Look for "X days" pattern after keyword
        val pattern = "$afterKeyword.{0,20}(\\d+)\\s*(day|days|week|weeks|hour|hours)".toRegex()
        val match = pattern.find(text)
        return match?.let { "${it.groupValues[1]} ${it.groupValues[2]}" }
    }
    
    fun addSymptom(symptom: Symptom) {
        currentVisit = currentVisit?.copy(
            symptoms = currentVisit!!.symptoms + symptom
        )
        updateCapturingState()
    }
    
    fun removeSymptom(symptomName: String) {
        currentVisit = currentVisit?.copy(
            symptoms = currentVisit!!.symptoms.filter { it.name != symptomName }
        )
        updateCapturingState()
    }
    
    fun setVitals(vitals: Vitals) {
        currentVisit = currentVisit?.copy(vitals = vitals)
        updateCapturingState()
    }
    
    /**
     * Check for danger signs - called after "not drinking" type symptoms added.
     */
    fun checkDangerSigns(): List<String> {
        val patient = currentPatient ?: return emptyList()
        val visit = currentVisit ?: return emptyList()
        
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        return decisionSupport.quickDangerCheck(
            visit.symptoms,
            visit.vitals,
            patient.ageInMonths(today)
        )
    }
    
    fun confirmDangerSign(dangerSign: String) {
        currentVisit = currentVisit?.copy(
            dangerSigns = currentVisit!!.dangerSigns + dangerSign
        )
        auditLogger.logDangerSign(chwId, currentVisit!!.id, dangerSign)
        updateCapturingState()
    }
    
    private fun updateCapturingState() {
        val patient = currentPatient ?: return
        val visit = currentVisit ?: return
        _state.value = WorkflowState.Capturing(patient, visit, null)
    }
    
    // ==================== STEP 4: Review Suggestions ====================
    
    fun proceedToSuggestions() {
        val patient = currentPatient ?: return
        val visit = currentVisit ?: return
        
        currentSuggestions = decisionSupport.evaluate(patient, visit)
        
        // Log that suggestions were shown
        for (s in currentSuggestions) {
            auditLogger.log(chwId, "suggestion_shown", "suggestion", s.id, s.title)
        }
        
        _state.value = WorkflowState.Reviewing(patient, visit, currentSuggestions)
    }
    
    // ==================== STEP 5: Decide & Treat ====================
    
    fun acceptSuggestion(suggestionId: String) {
        val suggestion = currentSuggestions.find { it.id == suggestionId } ?: return
        currentSuggestions = currentSuggestions.map {
            if (it.id == suggestionId) it.copy(accepted = true) else it
        }
        auditLogger.logSuggestionAccepted(chwId, suggestionId, suggestion.title)
        updateReviewingState()
    }
    
    fun rejectSuggestion(suggestionId: String, reason: String) {
        currentSuggestions = currentSuggestions.map {
            if (it.id == suggestionId) it.copy(accepted = false, chwResponse = reason) else it
        }
        auditLogger.logSuggestionRejected(chwId, suggestionId, reason)
        updateReviewingState()
    }
    
    fun createReferral(facility: String, urgency: Urgency, reason: String) {
        currentVisit = currentVisit?.copy(
            referral = Referral(facility, urgency, reason)
        )
        auditLogger.logReferral(chwId, currentVisit!!.id, facility)
        updateReviewingState()
    }
    
    private fun updateReviewingState() {
        val patient = currentPatient ?: return
        val visit = currentVisit ?: return
        _state.value = WorkflowState.Reviewing(patient, visit, currentSuggestions)
    }
    
    // ==================== STEP 6: Close Visit ====================
    
    fun buildTreatmentSummary(): String {
        val accepted = currentSuggestions.filter { it.accepted == true && it.type == SuggestionType.TREATMENT }
        return accepted.joinToString(", ") { it.title.removePrefix("Consider: ") }
    }
    
    suspend fun completeVisit(followUpDays: Int?) {
        val patient = currentPatient ?: return
        var visit = currentVisit ?: return
        
        // Finalize visit
        visit = visit.copy(
            endTime = System.currentTimeMillis(),
            treatment = buildTreatmentSummary(),
            assessment = visit.symptoms.filter { it.present }.joinToString(", ") { it.name }
        )
        
        // Save to database
        database.visitDao().insert(visit.toEntity())
        auditLogger.logVisitCompleted(chwId, visit.id)
        
        // Reset workflow
        currentPatient = null
        currentVisit = null
        currentSuggestions = emptyList()
        
        _state.value = WorkflowState.Completed(
            visitId = visit.id,
            patientName = patient.name,
            duration = ((visit.endTime ?: 0) - visit.startTime) / 1000
        )
    }
    
    fun reset() {
        currentPatient = null
        currentVisit = null
        currentSuggestions = emptyList()
        _state.value = WorkflowState.Idle
    }
}

// ==================== Workflow States ====================

sealed class WorkflowState {
    object Idle : WorkflowState()
    
    data class PatientSelected(val patient: Patient) : WorkflowState()
    
    data class Capturing(
        val patient: Patient,
        val visit: Visit,
        val lastVisitSummary: String?
    ) : WorkflowState()
    
    data class Reviewing(
        val patient: Patient,
        val visit: Visit,
        val suggestions: List<Suggestion>
    ) : WorkflowState()
    
    data class Completed(
        val visitId: String,
        val patientName: String,
        val duration: Long  // seconds
    ) : WorkflowState()
}

// ==================== Entity Conversions ====================

private fun PatientEntity.toDomain() = Patient(
    id = id,
    name = name,
    dateOfBirth = kotlinx.datetime.LocalDate.fromEpochDays(dateOfBirth.toInt()),
    sex = Sex.valueOf(sex),
    village = village,
    phone = phone,
    caregiverName = caregiverName,
    createdAt = createdAt,
    updatedAt = updatedAt,
    synced = synced
)

private fun Patient.toEntity() = PatientEntity(
    id = id,
    name = name,
    dateOfBirth = dateOfBirth.toEpochDays().toLong(),
    sex = sex.name,
    village = village,
    phone = phone,
    caregiverName = caregiverName,
    createdAt = createdAt,
    updatedAt = updatedAt,
    synced = synced
)

private fun Visit.toEntity(): VisitEntity {
    val json = Json { ignoreUnknownKeys = true }
    return VisitEntity(
        id = id,
        patientId = patientId,
        chwId = chwId,
        startTime = startTime,
        endTime = endTime,
        symptomsJson = json.encodeToString(symptoms),
        vitalsJson = vitals?.let { json.encodeToString(it) },
        dangerSignsJson = json.encodeToString(dangerSigns),
        assessment = assessment,
        treatment = treatment,
        referralJson = referral?.let { json.encodeToString(it) },
        notes = notes,
        synced = synced
    )
}
