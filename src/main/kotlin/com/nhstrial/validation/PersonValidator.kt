package com.nhstrial.validation

import com.nhstrial.model.PersonSessionData
import java.time.DateTimeException
import java.time.LocalDate

private val EMAIL_REGEX = Regex("""^[^\s@]+@[^\s@]+\.[^\s@]+$""")
private val NHS_DIGITS = Regex("""^\d{10}$""")

data class PersonForm(
    val nhsNumber: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val dobDay: String = "",
    val dobMonth: String = "",
    val dobYear: String = "",
    val gender: String = "",
)

fun validatePerson(form: PersonForm): Pair<ValidationResult, PersonSessionData?> {
    val result = buildValidation {
        val nhsDigits = form.nhsNumber.replace(" ", "")
        when {
            nhsDigits.isEmpty() -> error("nhsNumber", "Enter an NHS number")
            !NHS_DIGITS.matches(nhsDigits) -> error("nhsNumber", "NHS number must be 10 digits")
        }

        if (form.firstName.isBlank()) error("firstName", "Enter a first name")
        if (form.lastName.isBlank()) error("lastName", "Enter a last name")

        when {
            form.email.isBlank() -> error("email", "Enter an email address")
            !EMAIL_REGEX.matches(form.email) ->
                error(
                    "email",
                    "Enter an email address in the correct format, like name@example.com",
                )
        }

        val dayMissing = form.dobDay.isBlank()
        val monthMissing = form.dobMonth.isBlank()
        val yearMissing = form.dobYear.isBlank()
        when {
            dayMissing && monthMissing && yearMissing -> error("dob", "Enter a date of birth")
            dayMissing -> error("dob", "Date of birth must include a day")
            monthMissing -> error("dob", "Date of birth must include a month")
            yearMissing -> error("dob", "Date of birth must include a year")
            else -> {
                val dob = tryParseDate(form.dobDay, form.dobMonth, form.dobYear)
                val today = LocalDate.now()
                when {
                    dob == null -> error("dob", "Enter a real date of birth")
                    !dob.isBefore(today) -> error("dob", "Date of birth must be in the past")
                    !dob.isBefore(today.minusYears(18)) ->
                        error("dob", "Participant must be at least 18 years old")
                }
            }
        }

        if (form.gender.isBlank()) {
            error("gender", "Select a gender")
        } else if (form.gender != "Male" && form.gender != "Female") {
            error("gender", "Select Male or Female")
        }
    }

    if (result.hasErrors) return result to null

    val nhsDigits = form.nhsNumber.replace(" ", "")
    val dob = tryParseDate(form.dobDay, form.dobMonth, form.dobYear)!!
    return result to
        PersonSessionData(
            nhsNumber = nhsDigits,
            firstName = form.firstName.trim(),
            lastName = form.lastName.trim(),
            email = form.email.trim(),
            dob = dob.toString(),
            gender = form.gender,
        )
}

fun isValidNhsModulus11(digits: String): Boolean {
    if (digits.length != 10 || !digits.all { it.isDigit() }) return false
    val weights = intArrayOf(10, 9, 8, 7, 6, 5, 4, 3, 2)
    val sum = weights.indices.sumOf { weights[it] * digits[it].digitToInt() }
    val checkDigit = 11 - (sum % 11)
    return when (checkDigit) {
        11 -> digits[9].digitToInt() == 0
        10 -> false
        else -> digits[9].digitToInt() == checkDigit
    }
}

private fun tryParseDate(day: String, month: String, year: String): LocalDate? =
    try {
        LocalDate.of(year.trim().toInt(), month.trim().toInt(), day.trim().toInt())
    } catch (_: NumberFormatException) {
        null
    } catch (_: DateTimeException) {
        null
    }
