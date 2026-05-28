package com.nhstrial.service

import com.nhstrial.model.PersonSessionData
import com.nhstrial.model.TrialSummary
import com.nhstrial.repository.MedicalData
import com.nhstrial.repository.MedicalRepository
import com.nhstrial.repository.PersonRepository
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.slf4j.LoggerFactory

interface TrialService {
    suspend fun existsByNhsNumber(nhsNumber: String): Boolean

    suspend fun existsByEmail(email: String): Boolean

    suspend fun saveTrial(person: PersonSessionData, medical: MedicalData)

    suspend fun findAllTrials(): List<TrialSummary>
}

class TrialServiceImpl(
    private val personRepository: PersonRepository,
    private val medicalRepository: MedicalRepository,
) : TrialService {

    private val log = LoggerFactory.getLogger(TrialServiceImpl::class.java)

    override suspend fun existsByNhsNumber(nhsNumber: String): Boolean =
        newSuspendedTransaction(Dispatchers.IO) { personRepository.existsByNhsNumber(nhsNumber) }

    override suspend fun existsByEmail(email: String): Boolean =
        newSuspendedTransaction(Dispatchers.IO) { personRepository.existsByEmail(email) }

    override suspend fun saveTrial(person: PersonSessionData, medical: MedicalData) {
        newSuspendedTransaction(Dispatchers.IO) {
            val personId = personRepository.insert(person)
            log.info("Inserted person record id={}", personId)
            // person object is not logged — NHS number is PII
            medicalRepository.insert(medical, personId)
            log.info("Inserted medical record for personId={}", personId)
        }
    }

    override suspend fun findAllTrials(): List<TrialSummary> =
        newSuspendedTransaction(Dispatchers.IO) { personRepository.findAllTrials() }
}
