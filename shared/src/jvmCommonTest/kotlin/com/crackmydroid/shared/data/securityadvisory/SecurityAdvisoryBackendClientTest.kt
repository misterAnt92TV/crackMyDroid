package com.crackmydroid.shared.data.securityadvisory

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json

class SecurityAdvisoryBackendClientTest {
    @Test
    fun `associates results by purl regardless of response order`() = runTest {
        val second = com.crackmydroid.shared.domain.model.apkcontent.VerifiedVulnerabilityLibrary(
            packageUrl = "pkg:maven/com.example/second@1.0.0", version = "1.0.0",
        )
        val client = SecurityAdvisoryBackendClient(
            clientResponding(HttpStatusCode.OK, """
                {"results":[
                  {"packageUrl":"pkg:maven/com.example/second@1.0.0","advisories":[]},
                  {"packageUrl":"pkg:maven/com.squareup.okhttp3/okhttp@4.12.0","advisories":[]}
                ]}
            """.trimIndent()),
        )

        val results = client.lookup("https://example.test/advisories", listOf(okHttp(), second))

        assertEquals(
            listOf("pkg:maven/com.example/second@1.0.0", "pkg:maven/com.squareup.okhttp3/okhttp@4.12.0"),
            results.map { it.packageUrl },
        )
    }
    @Test
    fun `maps response associated with requested purl`() = runTest {
        val client = SecurityAdvisoryBackendClient(clientResponding(HttpStatusCode.OK, validResponse()))

        val results = client.lookup("https://example.test/advisories", listOf(okHttp()))

        assertEquals("pkg:maven/com.squareup.okhttp3/okhttp@4.12.0", results.single().packageUrl)
        assertEquals("GHSA-1234", results.single().advisories.single().advisoryId)
    }

    @Test
    fun `rejects unexpected response purl`() = runTest {
        val client = SecurityAdvisoryBackendClient(
            clientResponding(HttpStatusCode.OK, validResponse("pkg:maven/other/library@1.0.0")),
        )

        assertFailsWith<SecurityAdvisoryBackendFailure.UnexpectedPackages> {
            client.lookup("https://example.test/advisories", listOf(okHttp()))
        }
    }

    @Test
    fun `maps http and malformed payload failures`() = runTest {
        val httpClient = SecurityAdvisoryBackendClient(clientResponding(HttpStatusCode.BadGateway, "ignored"))
        val malformedClient = SecurityAdvisoryBackendClient(clientResponding(HttpStatusCode.OK, "not-json"))

        assertFailsWith<SecurityAdvisoryBackendFailure.Http> {
            httpClient.lookup("https://example.test/advisories", listOf(okHttp()))
        }
        assertFailsWith<SecurityAdvisoryBackendFailure.MalformedResponse> {
            malformedClient.lookup("https://example.test/advisories", listOf(okHttp()))
        }
    }

    private fun okHttp() = com.crackmydroid.shared.domain.model.apkcontent.VerifiedVulnerabilityLibrary(
        packageUrl = "pkg:maven/com.squareup.okhttp3/okhttp@4.12.0",
        version = "4.12.0",
    )

    private fun clientResponding(status: HttpStatusCode, body: String): HttpClient = HttpClient(
        MockEngine { respond(body, status, headersOf(HttpHeaders.ContentType, "application/json")) },
    ) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    private fun validResponse(
        packageUrl: String = "pkg:maven/com.squareup.okhttp3/okhttp@4.12.0",
    ) = """
        {"results":[{"packageUrl":"$packageUrl","advisories":[{
          "source":"github","advisoryId":"GHSA-1234","severity":"high",
          "summary":"Example advisory","url":"https://example.test/GHSA-1234"
        }]}]}
    """.trimIndent()
}
