package com.nhstrial.repository

import com.nhstrial.table.MedicalTable
import org.jetbrains.exposed.sql.insert
import java.time.LocalDateTime

data class MedicalData(
    val bpSystolic: Int,
    val bpDiastolic: Int,
    val treatment: String,
    val sideEffects: String?,
)

interface MedicalRepository {
    suspend fun insert(medical: MedicalData, personId: Int): Int
}

class MedicalRepositoryImpl : MedicalRepository {

    override suspend fun insert(medical: MedicalData, personId: Int): Int {
        val result = MedicalTable.insert { row ->
            row[MedicalTable.personId] = personId
            row[bpSystolic] = medical.bpSystolic
            row[bpDiastolic] = medical.bpDiastolic
            row[treatment] = medical.treatment
            row[sideEffects] = medical.sideEffects
            row[createdAt] = LocalDateTime.now()
        }
        return result[MedicalTable.id]
    }
}
