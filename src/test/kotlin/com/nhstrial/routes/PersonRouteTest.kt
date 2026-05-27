package com.nhstrial.routes

import com.nhstrial.FakePersonRepository
import com.nhstrial.FakeTrialService
import com.nhstrial.testModule
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.jsoup.Jsoup
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PersonRouteTest {

    // Valid test NHS numbers (Modulus 11 compliant)
    private val validNhs = "9434765919"

    private fun validParams(nhsNumber: String = validNhs) = mapOf(
        "nhsNumber" to nhsNumber,
        "firstName" to "Jane",
        "lastName" to "Doe",
        "email" to "jane.doe@example.com",
        "dobDay" to "15",
        "dobMonth" to "6",
        "dobYear" to "1990",
        "gender" to "Female",
    )

    @Test
    fun `GET person returns 200 with form`() = testApplication {
        testModule()
        val response = client.get("/person")
        assertEquals(HttpStatusCode.OK, response.status)
        val doc = Jsoup.parse(response.bodyAsText())
        assertNotNull(doc.getElementById("nhsNumber"))
        assertNotNull(doc.getElementById("firstName"))
    }

    @Test
    fun `GET root redirects to person`() = testApplication {
        testModule()
        val noRedirectClient = createClient { followRedirects = false }
        val response = noRedirectClient.get("/")
        assertEquals(HttpStatusCode.Found, response.status)
        assertTrue(response.headers["Location"]?.endsWith("/person") == true)
    }

    @Test
    fun `POST person with valid data redirects to medical`() = testApplication {
        testModule()
        val noRedirectClient = createClient { followRedirects = false }
        val response = noRedirectClient.post("/person") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(validParams().entries.joinToString("&") { "${it.key}=${it.value}" })
        }
        assertEquals(HttpStatusCode.Found, response.status)
        assertTrue(response.headers["Location"]?.endsWith("/medical") == true)
    }

    @Test
    fun `POST person with blank NHS number shows error`() = testApplication {
        testModule()
        val response = client.post("/person") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(validParams("").entries.joinToString("&") { "${it.key}=${it.value}" })
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val doc = Jsoup.parse(response.bodyAsText())
        assertTrue(doc.select(".nhsuk-error-summary").isNotEmpty(), "Error summary should appear")
        assertTrue(doc.select(".nhsuk-error-message").isNotEmpty(), "Inline error message should appear")
    }

    @Test
    fun `POST person with duplicate NHS number shows error`() = testApplication {
        val repo = FakePersonRepository(nhsNumbers = mutableSetOf(validNhs))
        val service = FakeTrialService(personRepo = repo)
        testModule(personRepository = repo, trialService = service)
        val response = client.post("/person") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(validParams().entries.joinToString("&") { "${it.key}=${it.value}" })
        }
        val doc = Jsoup.parse(response.bodyAsText())
        assertTrue(doc.text().contains("already registered"))
    }

    @Test
    fun `POST person missing first name shows specific field error`() = testApplication {
        testModule()
        val params = validParams().toMutableMap().apply { put("firstName", "") }
        val response = client.post("/person") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(params.entries.joinToString("&") { "${it.key}=${it.value}" })
        }
        val doc = Jsoup.parse(response.bodyAsText())
        val errorLink = doc.select(".nhsuk-error-summary__list a[href='#firstName']")
        assertTrue(errorLink.isNotEmpty(), "Error summary should link to firstName")
    }

    @Test
    fun `HTMX POST with valid data returns HX-Redirect header`() = testApplication {
        testModule()
        val response = client.post("/person") {
            contentType(ContentType.Application.FormUrlEncoded)
            header("HX-Request", "true")
            setBody(validParams().entries.joinToString("&") { "${it.key}=${it.value}" })
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("/medical", response.headers["HX-Redirect"])
    }

    @Test
    fun `HTMX POST with errors returns partial form HTML`() = testApplication {
        testModule()
        val response = client.post("/person") {
            contentType(ContentType.Application.FormUrlEncoded)
            header("HX-Request", "true")
            setBody(validParams("").entries.joinToString("&") { "${it.key}=${it.value}" })
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val doc = Jsoup.parse(response.bodyAsText())
        assertNotNull(doc.getElementById("person-form"), "Should return the form fragment")
    }

    @Test
    fun `POST person with all DOB fields missing shows single error`() = testApplication {
        testModule()
        val params = validParams().toMutableMap().apply {
            put("dobDay", ""); put("dobMonth", ""); put("dobYear", "")
        }
        val response = client.post("/person") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(params.entries.joinToString("&") { "${it.key}=${it.value}" })
        }
        val doc = Jsoup.parse(response.bodyAsText())
        val dobErrors = doc.select("[id=dob-error]")
        assertEquals(1, dobErrors.size, "Should show exactly one DOB error")
    }
}
