package com.nhstrial

import com.nhstrial.plugins.configureDatabase
import com.nhstrial.plugins.configureRouting
import com.nhstrial.plugins.configureSessions
import com.nhstrial.plugins.configureStatusPages
import com.nhstrial.repository.MedicalRepositoryImpl
import com.nhstrial.repository.PersonRepositoryImpl
import com.nhstrial.service.TrialServiceImpl
import io.ktor.server.application.*

fun Application.module() {
    configureDatabase()

    val personRepo = PersonRepositoryImpl()
    val medicalRepo = MedicalRepositoryImpl()
    val trialService = TrialServiceImpl(personRepo, medicalRepo)

    configureSessions()
    configureStatusPages()
    configureRouting(personRepo, trialService)
}
