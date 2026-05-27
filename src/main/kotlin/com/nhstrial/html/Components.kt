package com.nhstrial.html

import kotlinx.html.*

fun FlowContent.nhsErrorSummary(errors: Map<String, String>) {
    if (errors.isEmpty()) return
    div(classes = "nhsuk-error-summary") {
        attributes["aria-labelledby"] = "error-summary-title"
        attributes["role"] = "alert"
        h2(classes = "nhsuk-error-summary__title") {
            id = "error-summary-title"
            +"There is a problem"
        }
        div(classes = "nhsuk-error-summary__body") {
            ul(classes = "nhsuk-list nhsuk-error-summary__list") {
                errors.forEach { (field, message) ->
                    li { a(href = "#$field") { +message } }
                }
            }
        }
    }
}

fun FlowContent.nhsTextInput(
    id: String,
    label: String,
    name: String = id,
    value: String = "",
    error: String? = null,
    hint: String? = null,
    inputType: String = "text",
    extraClasses: String = "",
    autocomplete: String? = null,
) {
    val hasError = error != null
    div(classes = "nhsuk-form-group${if (hasError) " nhsuk-form-group--error" else ""}") {
        label(classes = "nhsuk-label nhsuk-label--m") {
            htmlFor = id
            +label
        }
        hint?.let {
            div(classes = "nhsuk-hint") {
                this.id = "$id-hint"
                +it
            }
        }
        error?.let {
            span(classes = "nhsuk-error-message") {
                this.id = "$id-error"
                span(classes = "nhsuk-u-visually-hidden") { +"Error:" }
                +" $it"
            }
        }
        input(classes = "nhsuk-input${if (hasError) " nhsuk-input--error" else ""}${if (extraClasses.isNotEmpty()) " $extraClasses" else ""}") {
            this.id = id
            this.name = name
            this.type = InputType.values().firstOrNull { it.realValue == inputType } ?: InputType.text
            this.value = value
            autocomplete?.let { attributes["autocomplete"] = it }
            val describedBy = listOfNotNull(
                if (hint != null) "$id-hint" else null,
                if (hasError) "$id-error" else null,
            )
            if (describedBy.isNotEmpty()) attributes["aria-describedby"] = describedBy.joinToString(" ")
        }
    }
}

fun FlowContent.nhsDateInput(
    id: String,
    fieldLegend: String,
    hint: String? = null,
    dayValue: String = "",
    monthValue: String = "",
    yearValue: String = "",
    error: String? = null,
) {
    val hasError = error != null
    div(classes = "nhsuk-form-group${if (hasError) " nhsuk-form-group--error" else ""}") {
        // kotlinx.html doesn't expose fieldset on DIV/FlowContent, so use the tag constructor directly
        FIELDSET(attributesMapOf("class", "nhsuk-fieldset"), consumer).visit {
            LEGEND(attributesMapOf("class", "nhsuk-fieldset__legend nhsuk-fieldset__legend--m"), consumer).visit {
                +fieldLegend
            }
            hint?.let {
                div(classes = "nhsuk-hint") {
                    this.id = "$id-hint"
                    +it
                }
            }
            error?.let {
                span(classes = "nhsuk-error-message") {
                    this.id = "$id-error"
                    span(classes = "nhsuk-u-visually-hidden") { +"Error:" }
                    +" $it"
                }
            }
            div(classes = "nhsuk-date-input") {
                this.id = id
                listOf(
                    Triple("dobDay", "Day", dayValue),
                    Triple("dobMonth", "Month", monthValue),
                    Triple("dobYear", "Year", yearValue),
                ).forEach { (fieldName, fieldLabel, fieldValue) ->
                    div(classes = "nhsuk-date-input__item") {
                        div(classes = "nhsuk-form-group") {
                            label(classes = "nhsuk-label nhsuk-date-input__label") {
                                htmlFor = fieldName
                                +fieldLabel
                            }
                            input(classes = "nhsuk-input nhsuk-date-input__input${if (hasError) " nhsuk-input--error" else ""} ${if (fieldName == "dobYear") "nhsuk-input--width-4" else "nhsuk-input--width-2"}") {
                                this.id = fieldName
                                this.name = fieldName
                                this.type = InputType.text
                                this.value = fieldValue
                                attributes["inputmode"] = "numeric"
                                if (hasError) attributes["aria-describedby"] = "$id-error"
                            }
                        }
                    }
                }
            }
        }
    }
}

fun FlowContent.nhsRadios(
    id: String,
    fieldLegend: String,
    options: List<String>,
    selectedValue: String = "",
    hint: String? = null,
    error: String? = null,
) {
    val hasError = error != null
    div(classes = "nhsuk-form-group${if (hasError) " nhsuk-form-group--error" else ""}") {
        FIELDSET(attributesMapOf("class", "nhsuk-fieldset"), consumer).visit {
            LEGEND(attributesMapOf("class", "nhsuk-fieldset__legend nhsuk-fieldset__legend--m"), consumer).visit {
                +fieldLegend
            }
            hint?.let { h ->
                div(classes = "nhsuk-hint") { +h }
            }
            error?.let {
                span(classes = "nhsuk-error-message") {
                    this.id = "$id-error"
                    span(classes = "nhsuk-u-visually-hidden") { +"Error:" }
                    +" $it"
                }
            }
            div(classes = "nhsuk-radios") {
                this.id = id
                options.forEach { option ->
                    div(classes = "nhsuk-radios__item") {
                        input(classes = "nhsuk-radios__input") {
                            this.id = "${id}_${option.replace(" ", "_")}"
                            this.name = id
                            this.type = InputType.radio
                            this.value = option
                            if (option == selectedValue) checked = true
                            if (hasError) attributes["aria-describedby"] = "$id-error"
                        }
                        label(classes = "nhsuk-label nhsuk-radios__label") {
                            htmlFor = "${id}_${option.replace(" ", "_")}"
                            +option
                        }
                    }
                }
            }
        }
    }
}

fun FlowContent.nhsSelect(
    id: String,
    label: String,
    options: List<String>,
    selectedValue: String = "",
    error: String? = null,
) {
    val hasError = error != null
    div(classes = "nhsuk-form-group${if (hasError) " nhsuk-form-group--error" else ""}") {
        label(classes = "nhsuk-label nhsuk-label--m") {
            htmlFor = id
            +label
        }
        error?.let {
            span(classes = "nhsuk-error-message") {
                this.id = "$id-error"
                span(classes = "nhsuk-u-visually-hidden") { +"Error:" }
                +" $it"
            }
        }
        select(classes = "nhsuk-select${if (hasError) " nhsuk-input--error" else ""}") {
            this.id = id
            name = id
            if (hasError) attributes["aria-describedby"] = "$id-error"
            option {
                value = ""
                attributes["disabled"] = ""
                if (selectedValue.isEmpty()) attributes["selected"] = ""
                +"Select treatment arm"
            }
            options.forEach { opt ->
                option {
                    value = opt
                    if (opt == selectedValue) attributes["selected"] = ""
                    +opt
                }
            }
        }
    }
}

fun FlowContent.nhsTextarea(
    id: String,
    label: String,
    value: String = "",
    rows: Int = 5,
    hint: String? = null,
    error: String? = null,
) {
    val hasError = error != null
    div(classes = "nhsuk-form-group${if (hasError) " nhsuk-form-group--error" else ""}") {
        label(classes = "nhsuk-label nhsuk-label--m") {
            htmlFor = id
            +label
        }
        hint?.let { div(classes = "nhsuk-hint") { +it } }
        error?.let {
            span(classes = "nhsuk-error-message") {
                span(classes = "nhsuk-u-visually-hidden") { +"Error:" }
                +" $it"
            }
        }
        textArea(classes = "nhsuk-textarea${if (hasError) " nhsuk-textarea--error" else ""}") {
            this.id = id
            name = id
            this.rows = rows.toString()
            +value
        }
    }
}
