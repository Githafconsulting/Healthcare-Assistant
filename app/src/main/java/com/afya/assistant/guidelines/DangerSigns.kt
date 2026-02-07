package com.afya.assistant.guidelines

import com.afya.assistant.domain.models.*

/**
 * WHO IMCI General Danger Signs - the most critical clinical rules.
 * Simple, hardcoded, always available offline.
 */
object DangerSigns {
    
    // General danger signs (apply to all sick children under 5)
    val GENERAL = listOf(
        DangerSign(
            id = "unable_to_drink",
            name = "Unable to drink or breastfeed",
            action = "Refer URGENTLY to hospital"
        ),
        DangerSign(
            id = "vomits_everything",
            name = "Vomits everything",
            action = "Refer URGENTLY to hospital"
        ),
        DangerSign(
            id = "convulsions",
            name = "Convulsions (now or during this illness)",
            action = "Refer URGENTLY to hospital"
        ),
        DangerSign(
            id = "lethargic",
            name = "Lethargic or unconscious",
            action = "Refer URGENTLY to hospital"
        )
    )
    
    // Vital-based danger signs
    fun checkVitals(vitals: Vitals, ageMonths: Int): List<DangerSign> {
        val dangers = mutableListOf<DangerSign>()
        
        vitals.temperature?.let { temp ->
            if (temp >= 39.5f) {
                dangers.add(DangerSign("high_fever", "Very high fever (${temp}°C)", "Give paracetamol, refer urgently"))
            }
            if (temp <= 35.5f) {
                dangers.add(DangerSign("hypothermia", "Low temperature (${temp}°C)", "Warm the child, refer urgently"))
            }
        }
        
        vitals.respiratoryRate?.let { rr ->
            val threshold = when {
                ageMonths < 2 -> 60
                ageMonths < 12 -> 50
                else -> 40
            }
            if (rr >= threshold) {
                dangers.add(DangerSign("fast_breathing", "Fast breathing ($rr/min)", "Check for chest indrawing, may need referral"))
            }
        }
        
        vitals.muac?.let { muac ->
            if (muac < 115) {
                dangers.add(DangerSign("severe_malnutrition", "Severe malnutrition (MUAC ${muac}mm)", "Refer for nutrition program"))
            }
        }
        
        return dangers
    }
    
    /**
     * Check symptoms for danger signs.
     */
    fun checkSymptoms(symptoms: List<Symptom>): List<DangerSign> {
        val dangers = mutableListOf<DangerSign>()
        
        val dangerSymptomMap = mapOf(
            "unable to drink" to GENERAL[0],
            "not drinking" to GENERAL[0],
            "refuses feeds" to GENERAL[0],
            "vomits everything" to GENERAL[1],
            "convulsion" to GENERAL[2],
            "seizure" to GENERAL[2],
            "lethargic" to GENERAL[3],
            "unconscious" to GENERAL[3],
            "difficult to wake" to GENERAL[3]
        )
        
        for (symptom in symptoms.filter { it.present }) {
            val name = symptom.name.lowercase()
            dangerSymptomMap.entries.find { name.contains(it.key) }?.let {
                dangers.add(it.value)
            }
        }
        
        return dangers.distinctBy { it.id }
    }
}

data class DangerSign(
    val id: String,
    val name: String,
    val action: String
)
