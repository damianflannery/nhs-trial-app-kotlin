package com.nhstrial.routes

import com.nhstrial.FakeTrialService
import com.nhstrial.model.PersonSessionData
import com.nhstrial.plugins.EnrolmentSession
import com.nhstrial.testModule
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.server.sessions.*
import org.jsoup.Jsoup
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MedicalRouteTest {

    private val pendingPerson = PersonSessionData(
        nhsNumber = "9434765919",
        firstName = "Jane",
        lastName = "Doe",
        email = "jane.doe@example.com",
        dob = "1990-06-15",
        gender = "Female",
    )

    private fun validMedicalParams() = mapOf(
        "bpSystolic" to "120",
        "bpDiastolic" to "80",
        "treatment" to "Drug",
        "sideEffects" to "",
    )

    @Test
    fun `GET medical without session redirects to person`() = testApplication {
        testModule()
        val noRedirectClient = createClient { followRedirects = false }
        val response = noRedirectClient.get("/medical")
        assertEquals(HttpStatusCode.Found, response.status)
        assertTrue(response.headers["Location"]?.endsWith("/person") == true)
    }

    @Test
    fun `GET medical with valid session returns form`() = testApplication {
        val service = FakeTrialService()
        testModule(trialService = service)
        val cookieClient = createClient { install(HttpCookies); followRedirects = false }

        // Seed the session by going through the person form first
        cookieClient.post("/person") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody("nhsNumber=9434765919&firstName=Jane&lastName=Doe&email=jane.doe@example.com&dobDay=15&dobMonth=6&dobYear=1990&gender=Female")
        }

        val response = cookieClient.get("/medical")
        assertEquals(HttpStatusCode.OK, response.status)
        val doc = Jsoup.parse(response.bodyAsText())
        assertNotNull(doc.getElementById("bpSystolic"))
        assertNotNull(doc.getElementById("bpDiastolic"))
        assertNotNull(doc.getElementById("treatment"))
    }

    @Test
    fun `POST medical with valid data redirects to thankyou`() = testApplication {
        val service = FakeTrialService()
        testModule(trialService = service)
        val cookieClient = createClient { install(HttpCookies); followRedirects = false }

        cookieClient.post("/person") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody("nhsNumber=9434765919&firstName=Jane&lastName=Doe&email=jane.doe@example.com&dobDay=15&dobMonth=6&dobYear=1990&gender=Female")
        }

        val response = cookieClient.post("/medical") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(validMedicalParams().entries.joinToString("&") { "${it.key}=${it.value}" })
        }
        assertEquals(HttpStatusCode.Found, response.status)
        assertTrue(response.headers["Location"]?.endsWith("/thankyou") == true)
        assertEquals(1, service.saved.size)
    }

    @Test
    fun `POST medical with blank systolic shows error`() = testApplication {
        val service = FakeTrialService()
        testModule(trialService = service)
        val cookieClient = createClient { install(HttpCookies); followRedirects = false }

        cookieClient.post("/person") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody("nhsNumber=9434765919&firstName=Jane&lastName=Doe&email=jane.doe@example.com&dobDay=15&dobMonth=6&dobYear=1990&gender=Female")
        }

        val params = validMedicalParams().toMutableMap().apply { put("bpSystolic", "") }
        val response = cookieClient.post("/medical") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(params.entries.joinToString("&") { "${it.key}=${it.value}" })
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val doc = Jsoup.parse(response.bodyAsText())
        assertTrue(doc.select(".nhsuk-error-summary").isNotEmpty())
    }

    @Test
    fun `POST medical with database error returns 500`() = testApplication {
        val service = FakeTrialService(throwOnSave = RuntimeException("DB down"))
        testModule(trialService = service)
        val cookieClient = createClient { install(HttpCookies); followRedirects = false }

        cookieClient.post("/person") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody("nhsNumber=9434765919&firstName=Jane&lastName=Doe&email=jane.doe@example.com&dobDay=15&dobMonth=6&dobYear=1990&gender=Female")
        }

        val response = cookieClient.post("/medical") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(validMedicalParams().entries.joinToString("&") { "${it.key}=${it.value}" })
        }
        assertEquals(HttpStatusCode.InternalServerError, response.status)
        assertTrue(response.bodyAsText().contains("problem with the service"))
    }

    @Test
    fun `HTMX POST medical with valid data returns HX-Redirect`() = testApplication {
        val service = FakeTrialService()
        testModule(trialService = service)
        val cookieClient = createClient { install(HttpCookies); followRedirects = false }

        cookieClient.post("/person") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody("nhsNumber=9434765919&firstName=Jane&lastName=Doe&email=jane.doe@example.com&dobDay=15&dobMonth=6&dobYear=1990&gender=Female")
        }

        val response = cookieClient.post("/medical") {
            contentType(ContentType.Application.FormUrlEncoded)
            header("HX-Request", "true")
            setBody(validMedicalParams().entries.joinToString("&") { "${it.key}=${it.value}" })
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("/thankyou", response.headers["HX-Redirect"])
    }

    @Test
    fun `GET dashboard shows chart canvas and submission count`() = testApplication {
        testModule(trialService = FakeTrialService())
        val response = client.get("/dashboard")
        assertEquals(HttpStatusCode.OK, response.status)
        val doc = Jsoup.parse(response.bodyAsText())
        // Scatter chart canvas present
        assertNotNull(doc.getElementById("bpChart"), "Canvas element for chart should exist")
        // Stats bar shows total submissions count (FakeTrialService returns 2)
        assertTrue(doc.select(".db-stat-value").isNotEmpty(), "Total submissions count should appear")
        assertEquals("2", doc.select(".db-stat-value").first()?.text())
        // Enrol button present
        assertTrue(doc.select(".db-enrol-btn").isNotEmpty(), "Enrol New Patient button should appear")
        // Inline chart JSON contains participant data
        val body = response.bodyAsText()
        assertTrue(body.contains("__bpData"), "Inline JSON data should be present")
        assertTrue(body.contains("Alice"), "Alice should be in the chart data")
    }
}
