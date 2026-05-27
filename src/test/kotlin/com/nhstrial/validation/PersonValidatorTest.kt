package com.nhstrial.validation

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class PersonValidatorTest {

    // Valid test NHS numbers (pass Modulus 11)
    private val validNhs1 = "9434765919"
    private val validNhs2 = "1112223339"

    private fun validForm(nhsNumber: String = validNhs1) = PersonForm(
        nhsNumber = nhsNumber,
        firstName = "Jane",
        lastName = "Doe",
        email = "jane.doe@example.com",
        dobDay = "15",
        dobMonth = "6",
        dobYear = "1990",
        gender = "Female",
    )

    @Test
    fun `valid form produces no errors`() {
        val (result, person) = validatePerson(validForm())
        assertFalse(result.hasErrors)
        assertNotNull(person)
        assertEquals("JaneDoe".filter { it.isLetter() }, person!!.firstName + person.lastName)
    }

    @Test
    fun `any 10-digit NHS number is accepted`() {
        val (result, _) = validatePerson(validForm("1234567890"))
        assertFalse(result.hasErrors)
    }

    @Test
    fun `empty NHS number triggers error`() {
        val (result, _) = validatePerson(validForm(""))
        assertTrue(result.errors.containsKey("nhsNumber"))
    }

    @Test
    fun `9-digit NHS number triggers error`() {
        val (result, _) = validatePerson(validForm("123456789"))
        assertTrue(result.errors.containsKey("nhsNumber"))
    }

    @Test
    fun `NHS number with spaces is normalised and validated`() {
        val (result, person) = validatePerson(validForm("943 476 5919"))
        assertFalse(result.hasErrors, "Spaces should be stripped before validation")
        assertEquals(validNhs1, person?.nhsNumber)
    }

    @Test
    fun `empty first name triggers error`() {
        val (result, _) = validatePerson(validForm().copy(firstName = ""))
        assertTrue(result.errors.containsKey("firstName"))
    }

    @Test
    fun `invalid email triggers error`() {
        val (result, _) = validatePerson(validForm().copy(email = "not-an-email"))
        assertTrue(result.errors.containsKey("email"))
    }

    @Test
    fun `all DOB fields empty gives single error`() {
        val (result, _) = validatePerson(validForm().copy(dobDay = "", dobMonth = "", dobYear = ""))
        assertTrue(result.errors.containsKey("dob"))
        assertEquals("Enter a date of birth", result.errors["dob"])
    }

    @Test
    fun `missing DOB day gives specific error`() {
        val (result, _) = validatePerson(validForm().copy(dobDay = ""))
        assertEquals("Date of birth must include a day", result.errors["dob"])
    }

    @Test
    fun `impossible date triggers error`() {
        val (result, _) = validatePerson(validForm().copy(dobDay = "31", dobMonth = "2", dobYear = "2000"))
        assertTrue(result.errors.containsKey("dob"))
    }

    @Test
    fun `future date of birth triggers error`() {
        val (result, _) = validatePerson(validForm().copy(dobDay = "1", dobMonth = "1", dobYear = "2099"))
        assertEquals("Date of birth must be in the past", result.errors["dob"])
    }

    @Test
    fun `participant under 18 triggers error`() {
        val (result, _) = validatePerson(validForm().copy(dobDay = "1", dobMonth = "1", dobYear = "2020"))
        assertEquals("Participant must be at least 18 years old", result.errors["dob"])
    }

    @Test
    fun `missing gender triggers error`() {
        val (result, _) = validatePerson(validForm().copy(gender = ""))
        assertTrue(result.errors.containsKey("gender"))
    }

    @Test
    fun `invalid gender value triggers error`() {
        val (result, _) = validatePerson(validForm().copy(gender = "Other"))
        assertTrue(result.errors.containsKey("gender"))
    }

    @ParameterizedTest
    @ValueSource(strings = ["9434765919", "1112223339", "1234567890", "0000000000"])
    fun `any 10-digit number passes format validation`(number: String) {
        val (result, _) = validatePerson(validForm(number))
        assertFalse(result.errors.containsKey("nhsNumber"))
    }
}
