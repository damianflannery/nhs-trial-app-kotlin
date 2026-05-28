package com.nhstrial.repository

import com.nhstrial.model.PersonSessionData
import com.nhstrial.model.TrialSummary
import com.nhstrial.table.MedicalTable
import com.nhstrial.table.PersonTable
import java.time.LocalDate
import java.time.LocalDateTime
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

interface PersonRepository {
    suspend fun existsByNhsNumber(nhsNumber: String): Boolean

    suspend fun existsByEmail(email: String): Boolean

    suspend fun insert(person: PersonSessionData): Int

    suspend fun findAllTrials(): List<TrialSummary>
}

class PersonRepositoryImpl : PersonRepository {

    override suspend fun existsByNhsNumber(nhsNumber: String): Boolean =
        PersonTable.selectAll().where { PersonTable.nhsNumber eq nhsNumber }.count() > 0

    override suspend fun existsByEmail(email: String): Boolean =
        PersonTable.selectAll().where { PersonTable.email eq email }.count() > 0

    override suspend fun insert(person: PersonSessionData): Int {
        // NHS number is not logged anywhere in this method — PII
        val result = PersonTable.insert { row ->
            row[nhsNumber] = person.nhsNumber
            row[firstName] = person.firstName
            row[lastName] = person.lastName
            row[email] = person.email
            row[dob] = LocalDate.parse(person.dob)
            row[gender] = person.gender
            row[createdAt] = LocalDateTime.now()
        }
        return result[PersonTable.id]
    }

    override suspend fun findAllTrials(): List<TrialSummary> =
        (PersonTable innerJoin MedicalTable).selectAll().orderBy(MedicalTable.createdAt).map { row
            ->
            TrialSummary(
                personId = row[PersonTable.id],
                firstName = row[PersonTable.firstName],
                lastName = row[PersonTable.lastName],
                gender = row[PersonTable.gender],
                dob = row[PersonTable.dob],
                bpSystolic = row[MedicalTable.bpSystolic],
                bpDiastolic = row[MedicalTable.bpDiastolic],
                treatment = row[MedicalTable.treatment],
                sideEffects = row[MedicalTable.sideEffects],
                createdAt = row[MedicalTable.createdAt],
            )
        }
}
