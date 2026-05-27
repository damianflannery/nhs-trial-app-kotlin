package com.nhstrial.model

import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime

data class Person(
    val id: Int,
    // NHS number deliberately excluded from toString() to prevent accidental PII leakage in logs
    val nhsNumber: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val dob: LocalDate,
    val gender: String,
    val createdAt: LocalDateTime,
) {
    override fun toString() =
        "Person(id=$id, firstName=$firstName, lastName=$lastName, gender=$gender)"
}

@Serializable
data class PersonSessionData(
    val nhsNumber: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val dob: String,
    val gender: String,
)

data class TrialSummary(
    val personId: Int,
    val firstName: String,
    val lastName: String,
    val gender: String,
    val dob: LocalDate,
    val bpSystolic: Int,
    val bpDiastolic: Int,
    val treatment: String,
    val sideEffects: String?,
    val createdAt: LocalDateTime,
)
