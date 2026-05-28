package com.nhstrial.html

import com.nhstrial.validation.PersonForm
import kotlinx.html.*

fun FlowContent.personFormContent(form: PersonForm, errors: Map<String, String>) {
    form(action = "/person", method = FormMethod.post) {
        id = "person-form"
        attributes["hx-post"] = "/person"
        attributes["hx-target"] = "#person-form"
        attributes["hx-swap"] = "outerHTML"
        attributes["hx-push-url"] = "true"

        nhsErrorSummary(errors)

        h1(classes = "nhsuk-heading-xl") { +"Participant details" }
        p(
            classes =
                "nhsuk-body-s nhsuk-u-secondary-text-color nhsuk-u-margin-top-0 nhsuk-u-margin-bottom-6"
        ) {
            +"Step 1 of 2"
        }

        nhsTextInput(
            id = "nhsNumber",
            label = "NHS number",
            value = form.nhsNumber,
            error = errors["nhsNumber"],
            hint =
                "This is a 10 digit number (like 999 123 4567) that you can find on an NHS letter, prescription or in the NHS App",
            inputType = "text",
            extraClasses = "nhsuk-input--width-10",
            autocomplete = "off",
        )
        nhsTextInput(
            id = "firstName",
            label = "First name",
            value = form.firstName,
            error = errors["firstName"],
            autocomplete = "given-name",
        )
        nhsTextInput(
            id = "lastName",
            label = "Last name",
            value = form.lastName,
            error = errors["lastName"],
            autocomplete = "family-name",
        )
        nhsTextInput(
            id = "email",
            label = "Email address",
            value = form.email,
            error = errors["email"],
            inputType = "email",
            autocomplete = "email",
        )
        nhsDateInput(
            id = "dob",
            fieldLegend = "Date of birth",
            hint = "For example, 15 3 1984",
            dayValue = form.dobDay,
            monthValue = form.dobMonth,
            yearValue = form.dobYear,
            error = errors["dob"],
        )
        nhsRadios(
            id = "gender",
            fieldLegend = "Gender",
            options = listOf("Male", "Female"),
            selectedValue = form.gender,
            error = errors["gender"],
        )

        button(classes = "nhsuk-button", type = ButtonType.submit) { +"Next" }
    }
}
