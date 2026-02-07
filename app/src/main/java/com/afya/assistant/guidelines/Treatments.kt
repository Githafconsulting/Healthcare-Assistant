package com.afya.assistant.guidelines

/**
 * Simple treatment protocols CHWs can administer.
 * Based on WHO IMCI - just the essentials.
 */
object Treatments {
    
    val ORS = Treatment(
        name = "ORS (Oral Rehydration Salts)",
        indication = "Diarrhea",
        dosing = mapOf(
            "Under 2 years" to "50-100ml after each loose stool",
            "2-5 years" to "100-200ml after each loose stool"
        ),
        instructions = listOf(
            "Mix 1 packet with 1 liter clean water",
            "Give small sips frequently",
            "If child vomits, wait 10 minutes, continue slowly",
            "Good for 24 hours after mixing"
        ),
        guidelineRef = "WHO IMCI Plan A"
    )
    
    val ZINC = Treatment(
        name = "Zinc",
        indication = "Diarrhea (with ORS)",
        dosing = mapOf(
            "Under 6 months" to "10mg once daily for 10-14 days",
            "6 months to 5 years" to "20mg once daily for 10-14 days"
        ),
        instructions = listOf(
            "Continue even after diarrhea stops",
            "Dissolve in breast milk or water for infants"
        ),
        guidelineRef = "WHO IMCI"
    )
    
    val PARACETAMOL = Treatment(
        name = "Paracetamol",
        indication = "Fever ≥38°C",
        dosing = mapOf(
            "4-6 kg" to "60mg (quarter tablet)",
            "6-10 kg" to "125mg (half tablet)",
            "10-19 kg" to "250mg (1 tablet)"
        ),
        instructions = listOf(
            "Give every 6 hours if fever continues",
            "Maximum 4 doses per day",
            "Use tepid sponging as well"
        ),
        guidelineRef = "WHO IMCI"
    )
    
    /**
     * Get applicable treatments for symptoms.
     */
    fun forSymptoms(symptoms: List<String>): List<Treatment> {
        val treatments = mutableListOf<Treatment>()
        
        if (symptoms.any { it.contains("diarr", ignoreCase = true) }) {
            treatments.add(ORS)
            treatments.add(ZINC)
        }
        if (symptoms.any { it.contains("fever", ignoreCase = true) }) {
            treatments.add(PARACETAMOL)
        }
        
        return treatments
    }
}

data class Treatment(
    val name: String,
    val indication: String,
    val dosing: Map<String, String>,
    val instructions: List<String>,
    val guidelineRef: String
)
