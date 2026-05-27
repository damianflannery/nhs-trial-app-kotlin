package com.nhstrial.html

import com.nhstrial.validation.MedicalForm
import kotlinx.html.*

fun FlowContent.medicalFormContent(form: MedicalForm, errors: Map<String, String>) {
    form(action = "/medical", method = FormMethod.post) {
        id = "medical-form"
        attributes["hx-post"] = "/medical"
        attributes["hx-target"] = "#medical-form"
        attributes["hx-swap"] = "outerHTML"
        attributes["hx-push-url"] = "true"

        nhsErrorSummary(errors)

        h1(classes = "nhsuk-heading-xl") { +"Clinical measurements" }
        p(classes = "nhsuk-body-s nhsuk-u-secondary-text-color nhsuk-u-margin-top-0 nhsuk-u-margin-bottom-6") {
            +"Step 2 of 2"
        }

        nhsTextInput(
            id = "bpSystolic",
            label = "Systolic blood pressure (mmHg)",
            value = form.bpSystolic,
            error = errors["bpSystolic"],
            hint = "The higher number on your monitor, representing the pressure when your heart beats",
            inputType = "number",
            extraClasses = "nhsuk-input--width-5",
        )
        nhsTextInput(
            id = "bpDiastolic",
            label = "Diastolic blood pressure (mmHg)",
            value = form.bpDiastolic,
            error = errors["bpDiastolic"],
            hint = "The lower number on your monitor, representing the pressure when your heart rests between beats",
            inputType = "number",
            extraClasses = "nhsuk-input--width-5",
        )
        nhsRadios(
            id = "treatment",
            fieldLegend = "Treatment",
            options = listOf("Drug", "Placebo"),
            selectedValue = form.treatment,
            hint = "Whether you are taking the drug or are on placebo (obviously this is handled behind the scenes irl)",
            error = errors["treatment"],
        )
        nhsTextarea(
            id = "sideEffects",
            label = "Side effects",
            value = form.sideEffects,
            hint = "Optional – describe any observed side effects",
            error = errors["sideEffects"],
        )

        button(classes = "nhsuk-button", type = ButtonType.submit) {
            +"Finish"
        }
    }
}
