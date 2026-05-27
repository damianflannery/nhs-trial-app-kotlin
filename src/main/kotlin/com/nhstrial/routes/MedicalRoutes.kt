package com.nhstrial.routes

import com.nhstrial.html.medicalFormContent
import com.nhstrial.html.nhsPage
import com.nhstrial.plugins.EnrolmentSession
import com.nhstrial.service.TrialService
import com.nhstrial.validation.MedicalForm
import com.nhstrial.validation.validateMedical
import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("MedicalRoutes")

fun Route.medicalRoutes(trialService: TrialService) {
    get("/medical") {
        val session = call.sessions.get<EnrolmentSession>()
        if (session?.pendingPerson == null) {
            call.respondRedirect("/person")
            return@get
        }
        call.respondHtml {
            nhsPage("Clinical measurements – step 2 of 2") {
                div(classes = "nhsuk-width-container") {
                    main(classes = "nhsuk-main-wrapper") {
                        id = "main-content"
                        div(classes = "nhsuk-grid-row") {
                            div(classes = "nhsuk-grid-column-two-thirds") {
                                p(classes = "nhsuk-hint") { +"Step 2 of 2" }
                                medicalFormContent(MedicalForm(), emptyMap())
                            }
                        }
                    }
                }
            }
        }
    }

    post("/medical") {
        val session = call.sessions.get<EnrolmentSession>()
        val person = session?.pendingPerson
        if (person == null) {
            call.respondRedirect("/person")
            return@post
        }

        val params = call.receiveParameters()
        val form = MedicalForm(
            bpSystolic = params["bpSystolic"].orEmpty(),
            bpDiastolic = params["bpDiastolic"].orEmpty(),
            treatment = params["treatment"].orEmpty(),
            sideEffects = params["sideEffects"].orEmpty(),
        )

        val (result, medicalData) = validateMedical(form)
        val isHtmx = call.request.headers["HX-Request"] == "true"

        if (result.hasErrors || medicalData == null) {
            if (isHtmx) {
                call.respondHtml {
                    body { medicalFormContent(form, result.errors) }
                }
            } else {
                call.respondHtml {
                    nhsPage("Clinical measurements – step 2 of 2") {
                        div(classes = "nhsuk-width-container") {
                            main(classes = "nhsuk-main-wrapper") {
                                id = "main-content"
                                div(classes = "nhsuk-grid-row") {
                                    div(classes = "nhsuk-grid-column-two-thirds") {
                                        p(classes = "nhsuk-hint") { +"Step 2 of 2" }
                                        medicalFormContent(form, result.errors)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return@post
        }

        try {
            trialService.saveTrial(person, medicalData)
        } catch (e: Exception) {
            log.error("Database error saving trial enrolment", e) // person details not logged
            call.respondHtml(HttpStatusCode.InternalServerError) {
                nhsPage("Sorry, there is a problem with the service") {
                    div(classes = "nhsuk-width-container") {
                        main(classes = "nhsuk-main-wrapper") {
                            h1(classes = "nhsuk-heading-xl") { +"Sorry, there is a problem with the service" }
                            p { +"A database error occurred. Please try again later." }
                        }
                    }
                }
            }
            return@post
        }

        call.sessions.clear<EnrolmentSession>()

        if (isHtmx) {
            call.response.header("HX-Redirect", "/thankyou")
            call.respond(HttpStatusCode.OK)
        } else {
            call.respondRedirect("/thankyou")
        }
    }
}
