package com.nhstrial.validation

import com.nhstrial.repository.MedicalData

val VALID_TREATMENTS = setOf("Drug", "Placebo")

data class MedicalForm(
    val bpSystolic: String = "",
    val bpDiastolic: String = "",
    val treatment: String = "",
    val sideEffects: String = "",
)

fun validateMedical(form: MedicalForm): Pair<ValidationResult, MedicalData?> {
    val result = buildValidation {
        val systolic = form.bpSystolic.trim().toIntOrNull()
        when {
            form.bpSystolic.isBlank() -> error("bpSystolic", "Enter a systolic blood pressure")
            systolic == null -> error("bpSystolic", "Systolic blood pressure must be a number")
            systolic !in 50..300 ->
                error("bpSystolic", "Systolic blood pressure must be between 50 and 300")
        }

        val diastolic = form.bpDiastolic.trim().toIntOrNull()
        when {
            form.bpDiastolic.isBlank() -> error("bpDiastolic", "Enter a diastolic blood pressure")
            diastolic == null -> error("bpDiastolic", "Diastolic blood pressure must be a number")
            diastolic !in 50..300 ->
                error("bpDiastolic", "Diastolic blood pressure must be between 50 and 300")
        }

        when {
            form.treatment.isBlank() -> error("treatment", "Select a treatment")
            form.treatment !in VALID_TREATMENTS -> error("treatment", "Select Drug or Placebo")
        }

        if (form.sideEffects.length > 1000) {
            error("sideEffects", "Side effects must be 1000 characters or fewer")
        }
    }

    if (result.hasErrors) return result to null

    return result to
        MedicalData(
            bpSystolic = form.bpSystolic.trim().toInt(),
            bpDiastolic = form.bpDiastolic.trim().toInt(),
            treatment = form.treatment,
            sideEffects = form.sideEffects.trim().ifBlank { null },
        )
}
