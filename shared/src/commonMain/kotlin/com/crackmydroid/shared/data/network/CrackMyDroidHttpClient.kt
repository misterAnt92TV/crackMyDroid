package com.crackmydroid.shared.data.network

import co.touchlab.kermit.Logger
import com.crackmydroid.shared.AppLogger
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import io.ktor.client.plugins.logging.Logger as KtorLogger

internal fun <T : HttpClientEngineConfig> HttpClientConfig<T>.configureCrackMyDroidNetwork(
    debugLogging: Boolean,
) {
    expectSuccess = false
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    install(HttpTimeout) {
        connectTimeoutMillis = CONNECT_TIMEOUT_MS
        requestTimeoutMillis = REQUEST_TIMEOUT_MS
        socketTimeoutMillis = SOCKET_TIMEOUT_MS
    }
    install(DefaultRequest) {
        headers.append(HttpHeaders.Accept, ContentType.Application.Json.toString())
        headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
    }
    if (debugLogging) {
        install(Logging) {
            logger = KermitKtorLogger(AppLogger.logger)
            level = LogLevel.HEADERS
            sanitizeHeader { name -> name.lowercase() in SENSITIVE_HEADERS }
        }
    }
}

private class KermitKtorLogger(private val logger: Logger) : KtorLogger {
    override fun log(message: String) {
        // Ktor is configured at HEADERS level: never log request or response bodies.
        logger.d { message }
    }
}

private val SENSITIVE_HEADERS = setOf(
    HttpHeaders.Authorization.lowercase(),
    HttpHeaders.Cookie.lowercase(),
    HttpHeaders.SetCookie.lowercase(),
    "x-api-key",
    "x-integrity-token",
)

private const val CONNECT_TIMEOUT_MS = 10_000L
private const val REQUEST_TIMEOUT_MS = 15_000L
private const val SOCKET_TIMEOUT_MS = 15_000L
