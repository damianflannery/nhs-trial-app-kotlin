package com.nhstrial.plugins

import com.nhstrial.repository.PersonRepository
import com.nhstrial.routes.dashboardRoutes
import com.nhstrial.routes.medicalRoutes
import com.nhstrial.routes.personRoutes
import com.nhstrial.routes.thankYouRoute
import com.nhstrial.service.TrialService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

fun Application.configureRouting(personRepository: PersonRepository, trialService: TrialService) {
    routing {
        staticResources("/nhsuk", "static/nhsuk")

        // Liveness + DB readiness probe used by CI and Docker health checks.
        // Runs a real query so it fails if the database is unreachable or Exposed
        // cannot map result columns (e.g. missing exposed-java-time service files).
        get("/health") {
            try {
                newSuspendedTransaction(Dispatchers.IO) { exec("SELECT 1") }
                call.respond(HttpStatusCode.OK, "OK")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.ServiceUnavailable, "DB unavailable: ${e.message}")
            }
        }

        personRoutes(personRepository, trialService)
        medicalRoutes(trialService)
        dashboardRoutes(trialService)
        thankYouRoute()
    }
}
