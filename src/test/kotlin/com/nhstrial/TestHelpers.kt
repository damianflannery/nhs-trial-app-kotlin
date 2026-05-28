package com.nhstrial

import com.nhstrial.model.PersonSessionData
import com.nhstrial.model.TrialSummary
import com.nhstrial.plugins.configureRouting
import com.nhstrial.plugins.configureSessions
import com.nhstrial.plugins.configureStatusPages
import com.nhstrial.repository.MedicalData
import com.nhstrial.repository.MedicalRepository
import com.nhstrial.repository.PersonRepository
import com.nhstrial.service.TrialService
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import java.time.LocalDate
import java.time.LocalDateTime

// ---------------------------------------------------------------------------
// Fakes — no database required
// ---------------------------------------------------------------------------

class FakePersonRepository(
    private val nhsNumbers: MutableSet<String> = mutableSetOf(),
    private val emails: MutableSet<String> = mutableSetOf(),
) : PersonRepository {
    val inserted = mutableListOf<PersonSessionData>()

    override suspend fun existsByNhsNumber(nhsNumber: String) = nhsNumber in nhsNumbers

    override suspend fun existsByEmail(email: String) = email in emails

    override suspend fun insert(person: PersonSessionData): Int {
        inserted.add(person)
        return inserted.size
    }

    override suspend fun findAllTrials(): List<TrialSummary> = emptyList()
}

class FakeMedicalRepository : MedicalRepository {
    val inserted = mutableListOf<Pair<MedicalData, Int>>()

    override suspend fun insert(medical: MedicalData, personId: Int): Int {
        inserted.add(medical to personId)
        return inserted.size
    }
}

class FakeTrialService(
    private val personRepo: FakePersonRepository = FakePersonRepository(),
    private val medicalRepo: FakeMedicalRepository = FakeMedicalRepository(),
    var throwOnSave: Exception? = null,
) : TrialService {
    val saved = mutableListOf<Pair<PersonSessionData, MedicalData>>()

    override suspend fun existsByNhsNumber(nhsNumber: String) =
        personRepo.existsByNhsNumber(nhsNumber)

    override suspend fun existsByEmail(email: String) = personRepo.existsByEmail(email)

    override suspend fun saveTrial(person: PersonSessionData, medical: MedicalData) {
        throwOnSave?.let { throw it }
        saved.add(person to medical)
    }

    override suspend fun findAllTrials(): List<TrialSummary> =
        listOf(
            TrialSummary(
                personId = 1,
                firstName = "Alice",
                lastName = "Smith",
                gender = "Female",
                dob = LocalDate.of(1985, 3, 12),
                bpSystolic = 120,
                bpDiastolic = 80,
                treatment = "Drug",
                sideEffects = null,
                createdAt = LocalDateTime.now(),
            ),
            TrialSummary(
                personId = 2,
                firstName = "Bob",
                lastName = "Jones",
                gender = "Male",
                dob = LocalDate.of(1972, 7, 4),
                bpSystolic = 135,
                bpDiastolic = 88,
                treatment = "Placebo",
                sideEffects = "Mild headache",
                createdAt = LocalDateTime.now(),
            ),
        )
}

// ---------------------------------------------------------------------------
// Shared test application factory
// ---------------------------------------------------------------------------

fun ApplicationTestBuilder.testModule(
    personRepository: PersonRepository = FakePersonRepository(),
    trialService: TrialService = FakeTrialService(),
) {
    application {
        configureSessions()
        configureStatusPages()
        configureRouting(personRepository, trialService)
    }
}
