package com.crackmydroid.shared.data.root

import com.crackmydroid.shared.domain.model.PlayIntegrityResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.coroutines.CancellationException

class IntegrityBackendClient(private val httpClient: HttpClient) {
    suspend fun verify(
        endpoint: String,
        integrityToken: String,
        requestBinding: String,
        userAgent: String
    ): PlayIntegrityResult {
        val response = httpClient.post(endpoint) {
            header("User-Agent", userAgent)
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(IntegrityVerificationRequest(integrityToken, requestBinding))
        }
        if (!response.status.isSuccess()) throw IntegrityBackendFailure.Http(response.status)
        val payload = try {
            response.body<IntegrityVerificationResponse>()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            throw IntegrityBackendFailure.MalformedResponse(error)
        }
        if (payload.requestBinding != requestBinding) throw IntegrityBackendFailure.BindingMismatch
        return payload.toDomain()
    }
}

@Serializable
internal data class IntegrityVerificationRequest(
    val integrityToken: String,
    val requestBinding: String
)

@Serializable
internal data class IntegrityVerificationResponse(
    val requestBinding: String,
    val basicIntegrity: Boolean? = null,
    val deviceIntegrity: Boolean? = null,
    val appRecognitionVerdict: String? = null,
    val appLicensingVerdict: String? = null,
    val deviceRecognitionVerdicts: List<String> = emptyList(),
    val playProtectVerdict: String? = null,
    val verifiedAtEpochMs: Long? = null,
    val details: String? = null
) {
    fun toDomain() = PlayIntegrityResult(
        basicIntegrity = basicIntegrity,
        deviceIntegrity = deviceIntegrity,
        details = details ?: "Verdict verificato dal backend",
        appRecognitionVerdict = appRecognitionVerdict,
        appLicensingVerdict = appLicensingVerdict,
        deviceRecognitionVerdicts = deviceRecognitionVerdicts,
        playProtectVerdict = playProtectVerdict,
        verifiedAtEpochMs = verifiedAtEpochMs
    )
}

sealed class IntegrityBackendFailure(message: String) : Exception(message) {
    data class Http(val status: HttpStatusCode) : IntegrityBackendFailure("HTTP ${status.value}")
    data class MalformedResponse(val source: Throwable) : IntegrityBackendFailure("Risposta backend non valida")
    data object BindingMismatch : IntegrityBackendFailure("Risposta backend non associata alla richiesta")
}
