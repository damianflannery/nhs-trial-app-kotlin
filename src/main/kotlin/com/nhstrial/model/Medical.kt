package com.nhstrial.model

import java.time.LocalDateTime

data class Medical(
    val id: Int,
    val personId: Int,
    val bpSystolic: Int,
    val bpDiastolic: Int,
    val treatment: String,
    val sideEffects: String?,
    val createdAt: LocalDateTime,
) {
    // sideEffects excluded — may contain sensitive patient-reported data
    override fun toString() =
        "Medical(id=$id, personId=$personId, bpSystolic=$bpSystolic, bpDiastolic=$bpDiastolic, treatment=$treatment)"
}
