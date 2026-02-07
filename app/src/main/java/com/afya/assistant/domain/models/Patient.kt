package com.afya.assistant.domain.models

import kotlinx.datetime.LocalDate
import java.util.UUID

/**
 * Patient record - minimal data needed for CHW workflow.
 */
data class Patient(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val dateOfBirth: LocalDate,
    val sex: Sex,
    val village: String,
    val phone: String? = null,
    val caregiverName: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
) {
    fun ageInMonths(today: LocalDate): Int {
        return (today.year - dateOfBirth.year) * 12 + 
               (today.monthNumber - dateOfBirth.monthNumber)
    }
    
    fun isUnderFive(today: LocalDate): Boolean = ageInMonths(today) < 60
}

enum class Sex { MALE, FEMALE, OTHER }
