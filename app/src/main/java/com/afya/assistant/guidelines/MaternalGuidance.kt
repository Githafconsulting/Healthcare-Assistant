package com.afya.assistant.guidelines

import com.afya.assistant.domain.models.Suggestion
import com.afya.assistant.domain.models.SuggestionType
import com.afya.assistant.domain.models.Confidence

/**
 * Maternal health guidance: ANC and PNC (WHO / national placeholder).
 * Does NOT diagnose; only suggests referral for danger signs and counsel.
 * All suggestions require CHW confirmation.
 */
object MaternalGuidance {

    /** ANC danger signs → urgent referral. */
    val ANC_DANGER_SIGNS = listOf(
        "Severe headache",
        "Blurred vision",
        "Fits",
        "Vaginal bleeding",
        "Fever",
        "Severe abdominal pain",
        "Severe breathlessness"
    )

    /** PNC danger signs (mother or newborn) → urgent referral. */
    val PNC_DANGER_SIGNS = listOf(
        "Heavy bleeding",
        "Fever",
        "Fits",
        "Newborn not feeding",
        "Newborn convulsions",
        "Newborn fast breathing"
    )

    /**
     * If any maternal danger sign mentioned or confirmed → suggest urgent referral.
     */
    fun suggestForMaternalDanger(
        isAntenatal: Boolean,
        dangerSignsMentioned: List<String>
    ): List<Suggestion> {
        if (dangerSignsMentioned.isEmpty()) return emptyList()
        val list = listOf(
            Suggestion(
                type = SuggestionType.REFERRAL,
                title = "Urgent referral – maternal danger sign",
                description = "Refer to nearest health facility immediately.",
                reason = "Danger sign: ${dangerSignsMentioned.joinToString(", ")}",
                basedOn = dangerSignsMentioned,
                guidelineRef = if (isAntenatal) "WHO ANC / NATIONAL_ANC" else "WHO PNC / NATIONAL_PNC",
                confidence = Confidence.HIGH,
                isUrgent = true
            )
        )
        return list
    }

    /**
     * No danger signs → suggest counsel and next visit.
     */
    fun suggestCounselAndNextVisit(isAntenatal: Boolean): Suggestion {
        return Suggestion(
            type = SuggestionType.ASSESSMENT,
            title = if (isAntenatal) "Continue ANC" else "Continue PNC",
            description = "Counsel on danger signs, nutrition, rest. Schedule next contact per national schedule.",
            reason = "No danger signs – routine care",
            basedOn = emptyList(),
            guidelineRef = if (isAntenatal) "WHO ANC / NATIONAL_ANC" else "WHO PNC / NATIONAL_PNC",
            confidence = Confidence.HIGH,
            isUrgent = false
        )
    }
}
