package com.nhstrial.plugins

import com.nhstrial.repository.PersonRepository
import com.nhstrial.routes.dashboardRoutes
import com.nhstrial.routes.medicalRoutes
import com.nhstrial.routes.personRoutes
import com.nhstrial.routes.thankYouRoute
import com.nhstrial.service.TrialService
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    personRepository: PersonRepository,
    trialService: TrialService,
) {
    routing {
        staticResources("/nhsuk", "static/nhsuk")

        personRoutes(personRepository, trialService)
        medicalRoutes(trialService)
        dashboardRoutes(trialService)
        thankYouRoute()
    }
}
