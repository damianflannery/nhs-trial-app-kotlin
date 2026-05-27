package com.nhstrial.plugins

import com.nhstrial.html.nhsPage
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.plugins.statuspages.*
import kotlinx.html.*

fun Application.configureStatusPages() {
    install(StatusPages) {
        status(HttpStatusCode.NotFound) { call, _ ->
            call.respondHtml(HttpStatusCode.NotFound) {
                nhsPage("Page not found") {
                    div(classes = "nhsuk-width-container") {
                        main(classes = "nhsuk-main-wrapper") {
                            h1(classes = "nhsuk-heading-xl") { +"Page not found" }
                            p { +"If you typed the web address, check it is correct." }
                        }
                    }
                }
            }
        }
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception", cause)
            call.respondHtml(HttpStatusCode.InternalServerError) {
                nhsPage("Sorry, there is a problem with the service") {
                    div(classes = "nhsuk-width-container") {
                        main(classes = "nhsuk-main-wrapper") {
                            h1(classes = "nhsuk-heading-xl") { +"Sorry, there is a problem with the service" }
                            p { +"Try again later." }
                        }
                    }
                }
            }
        }
    }
}
