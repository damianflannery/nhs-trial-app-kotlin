package com.nhstrial.routes

import com.nhstrial.html.dashboardContent
import com.nhstrial.html.nhsPage
import com.nhstrial.model.TrialSummary
import com.nhstrial.service.TrialService
import io.ktor.server.html.*
import io.ktor.server.routing.*
import kotlinx.html.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.Period

@Serializable
data class BpDataPoint(
    val name: String,
    val systolic: Int,
    val diastolic: Int,
    val gender: String,
    val treatment: String,
    val age: Int,
    val sideEffects: String?,
)

private fun TrialSummary.toDataPoint(): BpDataPoint {
    val age = Period.between(dob, LocalDate.now()).years
    return BpDataPoint(
        name = "$firstName ${lastName[0]}.",
        systolic = bpSystolic,
        diastolic = bpDiastolic,
        gender = gender,
        treatment = treatment,
        age = age,
        sideEffects = sideEffects,
    )
}

fun Route.dashboardRoutes(trialService: TrialService) {
    get("/dashboard") {
        val trials = trialService.findAllTrials()
        val dataPoints = trials.map { it.toDataPoint() }
        val inlineJson = Json.encodeToString(ListSerializer(BpDataPoint.serializer()), dataPoints)
        call.respondHtml {
            nhsPage("Blood pressure dashboard") {
                div(classes = "nhsuk-width-container") {
                    main(classes = "nhsuk-main-wrapper") {
                        id = "main-content"
                        dashboardContent(trials, inlineJson)
                    }
                }
            }
        }
    }

    // HTMX/JS refresh endpoint — returns JSON data wrapped in a script tag
    // so the chart can reload without a full page refresh
    get("/dashboard/chart-data") {
        val trials = trialService.findAllTrials()
        val dataPoints = trials.map { it.toDataPoint() }
        val json = Json.encodeToString(ListSerializer(BpDataPoint.serializer()), dataPoints)
        call.respondHtml {
            body {
                script {
                    unsafe { +"window.__bpData = $json;" }
                }
            }
        }
    }
}
