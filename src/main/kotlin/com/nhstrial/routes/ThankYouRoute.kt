package com.nhstrial.routes

import com.nhstrial.html.nhsPage
import io.ktor.server.html.*
import io.ktor.server.routing.*
import kotlinx.html.*

fun Route.thankYouRoute() {
    get("/thankyou") {
        call.respondHtml {
            nhsPage("Registration complete") {
                div(classes = "nhsuk-width-container") {
                    main(classes = "nhsuk-main-wrapper") {
                        id = "main-content"
                        div(classes = "nhsuk-grid-row") {
                            div(classes = "nhsuk-grid-column-two-thirds") {
                                div(classes = "nhsuk-panel") {
                                    h1(classes = "nhsuk-panel__title") { +"Registration complete" }
                                    div(classes = "nhsuk-panel__body") {
                                        +"The participant has been successfully enrolled in the clinical trial."
                                    }
                                }
                                h2(classes = "nhsuk-heading-m nhsuk-u-margin-top-6") {
                                    +"What happens next"
                                }
                                p {
                                    +"The participant's details and clinical measurements have been saved to the trial database."
                                }
                                p {
                                    a(href = "/person", classes = "nhsuk-link") {
                                        +"Enrol another participant"
                                    }
                                }
                                p {
                                    a(href = "/dashboard", classes = "nhsuk-link") {
                                        +"Go to dashboard"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
