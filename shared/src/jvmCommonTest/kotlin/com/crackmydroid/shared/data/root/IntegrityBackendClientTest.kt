package com.crackmydroid.shared.data.root

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class IntegrityBackendClientTest {
    @Test
    fun mapsVerifiedBackendResponse() = runTest {
        val client = IntegrityBackendClient(clientResponding(HttpStatusCode.OK, validResponse("nonce")))

        val result = client.verify("https://example.test/integrity", "token", "nonce", "CrackMyDroid/test")

        assertEquals("PLAY_RECOGNIZED", result.appRecognitionVerdict)
        assertEquals("LICENSED", result.appLicensingVerdict)
        assertEquals(listOf("MEETS_DEVICE_INTEGRITY"), result.deviceRecognitionVerdicts)
    }

    @Test
    fun rejectsResponseBoundToAnotherRequest() = runTest {
        val client = IntegrityBackendClient(clientResponding(HttpStatusCode.OK, validResponse("other")))

        assertFailsWith<IntegrityBackendFailure.BindingMismatch> {
            client.verify("https://example.test/integrity", "token", "nonce", "CrackMyDroid/test")
        }
    }

    @Test
    fun mapsHttpFailureWithoutReadingSensitiveBody() = runTest {
        val client = IntegrityBackendClient(clientResponding(HttpStatusCode.Conflict, "token must not be parsed"))

        val failure = assertFailsWith<IntegrityBackendFailure.Http> {
            client.verify("https://example.test/integrity", "token", "nonce", "CrackMyDroid/test")
        }

        assertEquals(HttpStatusCode.Conflict, failure.status)
    }

    @Test
    fun mapsMalformedJsonToTypedFailure() = runTest {
        val client = IntegrityBackendClient(clientResponding(HttpStatusCode.OK, "not-json"))

        assertFailsWith<IntegrityBackendFailure.MalformedResponse> {
            client.verify("https://example.test/integrity", "token", "nonce", "CrackMyDroid/test")
        }
    }

    @Test
    fun propagatesCancellationFromHttpRequest() = runTest {
        val cancellation = CancellationException("cancelled")
        val client = IntegrityBackendClient(
            HttpClient(
                MockEngine { throw cancellation },
            ) {
                install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            },
        )

        val thrown = assertFailsWith<CancellationException> {
            client.verify("https://example.test/integrity", "token", "nonce", "CrackMyDroid/test")
        }

        assertIs<CancellationException>(thrown)
    }

    private fun clientResponding(status: HttpStatusCode, body: String): HttpClient = HttpClient(
        MockEngine {
            respond(
                content = body,
                status = status,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        },
    ) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    private fun validResponse(binding: String) = """
        {
          "requestBinding": "$binding",
          "appRecognitionVerdict": "PLAY_RECOGNIZED",
          "appLicensingVerdict": "LICENSED",
          "deviceRecognitionVerdicts": ["MEETS_DEVICE_INTEGRITY"],
          "details": "verified"
        }
    """.trimIndent()
}
