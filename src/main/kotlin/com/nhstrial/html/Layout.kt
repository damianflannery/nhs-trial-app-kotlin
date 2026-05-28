package com.nhstrial.html

import kotlinx.html.*

fun HTML.nhsPage(title: String, block: BODY.() -> Unit) {
    lang = "en"
    head {
        meta(charset = "utf-8")
        meta(name = "viewport", content = "width=device-width, initial-scale=1, shrink-to-fit=no")
        title { +"$title | NHS Clinical Trial" }
        link(rel = "stylesheet", href = "/nhsuk/nhsuk.min.css")
        link(rel = "shortcut icon", href = "/nhsuk/assets/favicons/favicon.ico")
        script(src = "https://unpkg.com/htmx.org@2.0.3") {}
        // Chart.js loaded in <head> so it is always available when HTMX swaps in the dashboard body
        script(src = "https://cdn.jsdelivr.net/npm/chart.js@4") {}
    }
    body {
        attributes["hx-boost"] = "true"

        header(classes = "nhsuk-header") {
            div(classes = "nhsuk-width-container nhsuk-header__container") {
                div(classes = "nhsuk-header__logo") {
                    a(href = "/", classes = "nhsuk-header__link") {
                        attributes["aria-label"] = "NHS homepage"
                        span(classes = "nhsuk-organisation-name") {
                            +"NHS "
                            span(classes = "nhsuk-organisation-name-separator") {}
                            +"Clinical Trial"
                        }
                    }
                }
                nav(classes = "nhsuk-header__navigation") {
                    ul(classes = "nhsuk-header__navigation-list") {
                        li(classes = "nhsuk-header__navigation-item") {
                            a(href = "/dashboard", classes = "nhsuk-header__navigation-link") {
                                +"Dashboard"
                            }
                        }
                        li(classes = "nhsuk-header__navigation-item") {
                            a(href = "/person", classes = "nhsuk-header__navigation-link") {
                                +"Enrol participant"
                            }
                        }
                    }
                }
            }
        }

        block()

        footer(classes = "nhsuk-footer") {
            div(classes = "nhsuk-width-container") {
                p(classes = "nhsuk-footer__copyright") { +"© NHS Clinical Trial" }
            }
        }

        script(src = "/nhsuk/nhsuk.min.js") {}
    }
}
