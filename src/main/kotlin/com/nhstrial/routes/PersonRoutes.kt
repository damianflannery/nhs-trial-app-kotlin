package com.nhstrial.routes

import com.nhstrial.html.nhsPage
import com.nhstrial.html.personFormContent
import com.nhstrial.plugins.EnrolmentSession
import com.nhstrial.repository.PersonRepository
import com.nhstrial.service.TrialService
import com.nhstrial.validation.PersonForm
import com.nhstrial.validation.validatePerson
import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*

fun Route.personRoutes(personRepository: PersonRepository, trialService: TrialService) {
    get("/") { call.respondRedirect("/person") }

    get("/person") {
        call.respondHtml {
            nhsPage("Participant details – step 1 of 2") {
                div(classes = "nhsuk-width-container") {
                    main(classes = "nhsuk-main-wrapper") {
                        id = "main-content"
                        div(classes = "nhsuk-grid-row") {
                            div(classes = "nhsuk-grid-column-two-thirds") {
                                p(classes = "nhsuk-hint") { +"Step 1 of 2" }
                                personFormContent(PersonForm(), emptyMap())
                            }
                        }
                    }
                }
            }
        }
    }

    post("/person") {
        val params = call.receiveParameters()
        val form =
            PersonForm(
                nhsNumber = params["nhsNumber"].orEmpty(),
                firstName = params["firstName"].orEmpty(),
                lastName = params["lastName"].orEmpty(),
                email = params["email"].orEmpty(),
                dobDay = params["dobDay"].orEmpty(),
                dobMonth = params["dobMonth"].orEmpty(),
                dobYear = params["dobYear"].orEmpty(),
                gender = params["gender"].orEmpty(),
            )

        val (result, personData) = validatePerson(form)

        // Uniqueness checks only when format is valid
        val errors =
            if (!result.hasErrors && personData != null) {
                val extraErrors = mutableMapOf<String, String>()
                if (trialService.existsByNhsNumber(personData.nhsNumber)) {
                    extraErrors["nhsNumber"] =
                        "A participant with this NHS number is already registered"
                }
                if (trialService.existsByEmail(personData.email)) {
                    extraErrors["email"] =
                        "A participant with this email address is already registered"
                }
                result.errors + extraErrors
            } else {
                result.errors
            }

        val isHtmx = call.request.headers["HX-Request"] == "true"

        if (errors.isNotEmpty()) {
            if (isHtmx) {
                call.respondHtml { body { personFormContent(form, errors) } }
            } else {
                call.respondHtml {
                    nhsPage("Participant details – step 1 of 2") {
                        div(classes = "nhsuk-width-container") {
                            main(classes = "nhsuk-main-wrapper") {
                                id = "main-content"
                                div(classes = "nhsuk-grid-row") {
                                    div(classes = "nhsuk-grid-column-two-thirds") {
                                        p(classes = "nhsuk-hint") { +"Step 1 of 2" }
                                        personFormContent(form, errors)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return@post
        }

        call.sessions.set(EnrolmentSession(pendingPerson = personData))

        if (isHtmx) {
            call.response.header("HX-Redirect", "/medical")
            call.respond(HttpStatusCode.OK)
        } else {
            call.respondRedirect("/medical")
        }
    }
}
