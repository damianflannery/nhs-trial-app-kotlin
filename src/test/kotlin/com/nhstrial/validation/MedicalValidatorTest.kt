package com.nhstrial.validation

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MedicalValidatorTest {

    private fun validForm() =
        MedicalForm(bpSystolic = "120", bpDiastolic = "80", treatment = "Drug", sideEffects = "")

    @Test
    fun `valid form produces no errors`() {
        val (result, data) = validateMedical(validForm())
        assertFalse(result.hasErrors)
        assertNotNull(data)
        assertEquals(120, data!!.bpSystolic)
        assertEquals(80, data.bpDiastolic)
        assertEquals("Drug", data.treatment)
        assertNull(data.sideEffects)
    }

    @Test
    fun `blank side effects are stored as null`() {
        val (_, data) = validateMedical(validForm().copy(sideEffects = "   "))
        assertNull(data!!.sideEffects)
    }

    @Test
    fun `non-blank side effects are stored`() {
        val (_, data) = validateMedical(validForm().copy(sideEffects = "Nausea"))
        assertEquals("Nausea", data!!.sideEffects)
    }

    @Test
    fun `empty systolic triggers error`() {
        val (result, _) = validateMedical(validForm().copy(bpSystolic = ""))
        assertTrue(result.errors.containsKey("bpSystolic"))
    }

    @Test
    fun `non-numeric systolic triggers error`() {
        val (result, _) = validateMedical(validForm().copy(bpSystolic = "abc"))
        assertTrue(result.errors.containsKey("bpSystolic"))
    }

    @Test
    fun `systolic below range triggers error`() {
        val (result, _) = validateMedical(validForm().copy(bpSystolic = "49"))
        assertTrue(result.errors.containsKey("bpSystolic"))
    }

    @Test
    fun `systolic above range triggers error`() {
        val (result, _) = validateMedical(validForm().copy(bpSystolic = "301"))
        assertTrue(result.errors.containsKey("bpSystolic"))
    }

    @Test
    fun `invalid treatment triggers error`() {
        val (result, _) = validateMedical(validForm().copy(treatment = "Unknown"))
        assertTrue(result.errors.containsKey("treatment"))
    }

    @Test
    fun `side effects exceeding 1000 chars triggers error`() {
        val (result, _) = validateMedical(validForm().copy(sideEffects = "x".repeat(1001)))
        assertTrue(result.errors.containsKey("sideEffects"))
    }
}
