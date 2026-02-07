package com.afya.assistant.ai

import com.afya.assistant.domain.models.*
import com.afya.assistant.guidelines.DangerSigns
import com.afya.assistant.guidelines.MalariaGuidance
import com.afya.assistant.guidelines.Treatments
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Decision support engine - rule-based, fully explainable.
 * 
 * CRITICAL: This provides SUPPORT, not diagnosis.
 * All suggestions require CHW confirmation.
 */
@Singleton
class DecisionSupport @Inject constructor() {
    
    /**
     * Evaluate visit and return suggestions.
     * Order: danger signs first, then assessments, then treatments.
     */
    fun evaluate(patient: Patient, visit: Visit): List<Suggestion> {
        val suggestions = mutableListOf<Suggestion>()
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val ageMonths = patient.ageInMonths(today)
        
        // 1. CHECK FOR CONFIRMED DANGER SIGNS → Referral suggestion
        if (visit.dangerSigns.isNotEmpty()) {
            suggestions.add(
                Suggestion(
                    type = SuggestionType.REFERRAL,
                    title = "Urgent Referral Needed",
                    description = "Refer to nearest health facility immediately",
                    reason = "Danger sign confirmed: ${visit.dangerSigns.joinToString()}",
                    basedOn = visit.dangerSigns,
                    guidelineRef = "WHO IMCI General Danger Signs",
                    confidence = Confidence.HIGH,
                    isUrgent = true
                )
            )
        }
        
        // 2. CHECK VITALS FOR DANGER SIGNS
        visit.vitals?.let { vitals ->
            val vitalDangers = DangerSigns.checkVitals(vitals, ageMonths)
            for (ds in vitalDangers) {
                suggestions.add(
                    Suggestion(
                        type = SuggestionType.DANGER_SIGN,
                        title = "Warning: ${ds.name}",
                        description = ds.action,
                        reason = "Vital sign is outside safe range",
                        basedOn = listOf(ds.name),
                        guidelineRef = "WHO IMCI",
                        confidence = Confidence.HIGH,
                        isUrgent = true
                    )
                )
            }
        }
        
        // 3. MALARIA: fever path (no diagnosis; suggest ACT/RDT per guideline)
        val symptomNames = visit.symptoms.filter { it.present }.map { it.name }
        val hasFever = symptomNames.any { it.contains("fever", ignoreCase = true) }
        if (hasFever && visit.dangerSigns.isEmpty()) {
            suggestions.addAll(
                MalariaGuidance.suggestForFever(
                    hasDangerSigns = false,
                    hasFever = true,
                    rdtPositive = null,  // TODO: from visit when RDT field added
                    rdtDone = false,
                    ageMonths = ageMonths
                )
            )
        }

        // 4. SUGGEST TREATMENTS based on symptoms
        val treatments = Treatments.forSymptoms(symptomNames)
        
        for (tx in treatments) {
            val dose = getDoseForAge(tx, ageMonths)
            suggestions.add(
                Suggestion(
                    type = SuggestionType.TREATMENT,
                    title = "Consider: ${tx.name}",
                    description = "$dose\n\n${tx.instructions.joinToString("\n• ", prefix = "• ")}",
                    reason = "Indicated for ${tx.indication}",
                    basedOn = symptomNames.filter { 
                        it.contains(tx.indication.split(" ").first(), ignoreCase = true) 
                    },
                    guidelineRef = tx.guidelineRef,
                    confidence = Confidence.HIGH,
                    isUrgent = false
                )
            )
        }
        
        // 5. Sort: urgent first, then by type
        return suggestions.sortedByDescending { 
            when {
                it.isUrgent -> 100
                it.type == SuggestionType.DANGER_SIGN -> 90
                it.type == SuggestionType.REFERRAL -> 80
                it.type == SuggestionType.TREATMENT -> 70
                else -> 50
            }
        }
    }
    
    /**
     * Quick danger sign check - for real-time alerts during data entry.
     */
    fun quickDangerCheck(symptoms: List<Symptom>, vitals: Vitals?, ageMonths: Int): List<String> {
        val dangers = mutableListOf<String>()
        dangers.addAll(DangerSigns.checkSymptoms(symptoms).map { it.name })
        vitals?.let { dangers.addAll(DangerSigns.checkVitals(it, ageMonths).map { it.name }) }
        return dangers.distinct()
    }
    
    private fun getDoseForAge(treatment: com.afya.assistant.guidelines.Treatment, ageMonths: Int): String {
        // Match age to dosing bracket
        return when {
            ageMonths < 6 -> treatment.dosing.entries.find { 
                it.key.contains("6 month", ignoreCase = true) || 
                it.key.contains("under", ignoreCase = true) 
            }?.let { "${it.key}: ${it.value}" }
            
            ageMonths < 24 -> treatment.dosing.entries.find { 
                it.key.contains("2 year", ignoreCase = true) || 
                it.key.contains("under 2", ignoreCase = true) ||
                it.key.contains("6-", ignoreCase = true)
            }?.let { "${it.key}: ${it.value}" }
            
            else -> treatment.dosing.entries.find {
                it.key.contains("5 year", ignoreCase = true) ||
                it.key.contains("2-5", ignoreCase = true) ||
                it.key.contains("10", ignoreCase = true)
            }?.let { "${it.key}: ${it.value}" }
            
        } ?: treatment.dosing.entries.first().let { "${it.key}: ${it.value}" }
    }
}
