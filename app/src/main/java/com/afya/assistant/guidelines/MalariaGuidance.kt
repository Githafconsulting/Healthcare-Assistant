package com.afya.assistant.guidelines

import com.afya.assistant.domain.models.Suggestion
import com.afya.assistant.domain.models.SuggestionType
import com.afya.assistant.domain.models.Confidence

/**
 * Malaria guidance for CHWs (WHO / national placeholder).
 * Does NOT diagnose; only suggests actions based on fever/RDT/danger signs.
 * All suggestions require CHW confirmation.
 */
object MalariaGuidance {

    /**
     * If danger signs present → urgent referral (handled by main CDS).
     * If fever (or history of fever):
     * - RDT positive → suggest ACT + paracetamol
     * - RDT negative → other fever care
     * - RDT not done → suggest do RDT or per national policy
     */
    fun suggestForFever(
        hasDangerSigns: Boolean,
        hasFever: Boolean,
        rdtPositive: Boolean?,
        rdtDone: Boolean,
        ageMonths: Int
    ): List<Suggestion> {
        if (hasDangerSigns) return emptyList() // Main CDS adds referral
        if (!hasFever) return emptyList()

        val list = mutableListOf<Suggestion>()
        when {
            rdtDone && rdtPositive == true -> {
                list.add(
                    Suggestion(
                        type = SuggestionType.TREATMENT,
                        title = "Consider: ACT for malaria",
                        description = "Give ACT per weight/age. Guideline: WHO malaria.",
                        reason = "RDT positive – treat for malaria",
                        basedOn = listOf("Fever", "RDT positive"),
                        guidelineRef = "WHO malaria / NATIONAL_MALARIA",
                        confidence = Confidence.HIGH,
                        isUrgent = false
                    )
                )
                list.add(
                    Suggestion(
                        type = SuggestionType.TREATMENT,
                        title = "Consider: Paracetamol for fever",
                        description = "If fever ≥38°C, give paracetamol per weight.",
                        reason = "Fever present",
                        basedOn = listOf("Fever"),
                        guidelineRef = "WHO IMCI",
                        confidence = Confidence.HIGH,
                        isUrgent = false
                    )
                )
            }
            rdtDone && rdtPositive == false -> {
                list.add(
                    Suggestion(
                        type = SuggestionType.TREATMENT,
                        title = "Consider: Paracetamol for fever",
                        description = "Malaria unlikely. Other causes of fever – give paracetamol if fever.",
                        reason = "RDT negative – other fever care",
                        basedOn = listOf("Fever", "RDT negative"),
                        guidelineRef = "WHO malaria / NATIONAL_MALARIA",
                        confidence = Confidence.HIGH,
                        isUrgent = false
                    )
                )
            }
            !rdtDone -> {
                list.add(
                    Suggestion(
                        type = SuggestionType.ASSESSMENT,
                        title = "Consider: Do RDT if available",
                        description = "If RDT not available, follow national policy (treat as malaria or refer).",
                        reason = "Fever without RDT result",
                        basedOn = listOf("Fever"),
                        guidelineRef = "WHO malaria / NATIONAL_MALARIA",
                        confidence = Confidence.MEDIUM,
                        isUrgent = false
                    )
                )
            }
        }
        return list
    }
}
