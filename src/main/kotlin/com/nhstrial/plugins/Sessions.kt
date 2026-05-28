package com.nhstrial.plugins

import com.nhstrial.model.PersonSessionData
import io.ktor.server.application.*
import io.ktor.server.sessions.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable data class EnrolmentSession(val pendingPerson: PersonSessionData? = null)

fun Application.configureSessions() {
    install(Sessions) {
        cookie<EnrolmentSession>("ENROLMENT", SessionStorageMemory()) {
            cookie.path = "/"
            cookie.httpOnly = true
            serializer =
                object : SessionSerializer<EnrolmentSession> {
                    private val json = Json { ignoreUnknownKeys = true }

                    override fun serialize(session: EnrolmentSession) =
                        json.encodeToString(EnrolmentSession.serializer(), session)

                    override fun deserialize(text: String) =
                        json.decodeFromString(EnrolmentSession.serializer(), text)
                }
        }
    }
}
