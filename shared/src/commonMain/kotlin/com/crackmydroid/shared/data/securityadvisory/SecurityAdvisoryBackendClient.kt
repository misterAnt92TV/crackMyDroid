package com.crackmydroid.shared.data.securityadvisory

import com.crackmydroid.shared.domain.model.apkcontent.AdvisorySource
import com.crackmydroid.shared.domain.model.apkcontent.AdvisorySeverity
import com.crackmydroid.shared.domain.model.apkcontent.ApkLibraryAdvisory
import com.crackmydroid.shared.domain.model.apkcontent.ApkLibraryVulnerabilityResult
import com.crackmydroid.shared.domain.model.apkcontent.VulnerabilityLookupLibrary
import com.crackmydroid.shared.domain.model.apkcontent.VerifiedVulnerabilityLibrary
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.Serializable

class SecurityAdvisoryBackendClient(private val httpClient: HttpClient) {
    suspend fun lookup(
        endpoint: String,
        libraries: List<VerifiedVulnerabilityLibrary>,
    ): List<ApkLibraryVulnerabilityResult> {
        val response = httpClient.post(endpoint) {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(SecurityAdvisoryLookupRequest(libraries.map { SecurityAdvisoryPackageDto(it.packageUrl) }))
        }
        if (!response.status.isSuccess()) throw SecurityAdvisoryBackendFailure.Http(response.status)
        val payload = try {
            response.body<SecurityAdvisoryLookupResponse>()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            throw SecurityAdvisoryBackendFailure.MalformedResponse(error)
        }
        val requestedPackageUrls = libraries.map { it.packageUrl }.toSet()
        val responsePackageUrls = payload.results.map { it.packageUrl }
        if (responsePackageUrls.size != responsePackageUrls.toSet().size ||
            responsePackageUrls.toSet() != requestedPackageUrls
        ) {
            throw SecurityAdvisoryBackendFailure.UnexpectedPackages
        }
        val versions = libraries.associateBy { it.packageUrl }
        return payload.results.sortedBy { it.packageUrl }.map { result ->
            ApkLibraryVulnerabilityResult(
                packageUrl = result.packageUrl,
                version = versions.getValue(result.packageUrl).version,
                advisories = result.advisories.map(SecurityAdvisoryDto::toDomain),
            )
        }
    }
}

@Serializable
internal data class SecurityAdvisoryLookupRequest(val packages: List<SecurityAdvisoryPackageDto>)

@Serializable
internal data class SecurityAdvisoryPackageDto(val purl: String)

@Serializable
internal data class SecurityAdvisoryLookupResponse(val results: List<SecurityAdvisoryResultDto>)

@Serializable
internal data class SecurityAdvisoryResultDto(
    val packageUrl: String,
    val advisories: List<SecurityAdvisoryDto> = emptyList(),
)

@Serializable
internal data class SecurityAdvisoryDto(
    val source: String,
    val advisoryId: String,
    val cveIds: List<String> = emptyList(),
    val severity: String = "unknown",
    val summary: String,
    val affectedVersions: String? = null,
    val fixedVersion: String? = null,
    val url: String,
) {
    fun toDomain() = ApkLibraryAdvisory(
        source = when (source.lowercase()) {
            "osv" -> AdvisorySource.Osv
            "github" -> AdvisorySource.GitHub
            else -> AdvisorySource.Unknown
        },
        advisoryId = advisoryId,
        cveIds = cveIds,
        severity = AdvisorySeverity.entries.firstOrNull { it.name.equals(severity, ignoreCase = true) }
            ?: AdvisorySeverity.Unknown,
        summary = summary,
        affectedVersions = affectedVersions,
        fixedVersion = fixedVersion,
        url = url.takeIf { it.startsWith("https://") }
            ?: throw SecurityAdvisoryBackendFailure.MalformedResponse(IllegalArgumentException("Invalid advisory URL")),
    )
}

sealed class SecurityAdvisoryBackendFailure(message: String) : Exception(message) {
    data class Http(val status: HttpStatusCode) : SecurityAdvisoryBackendFailure("HTTP ${status.value}")
    data class MalformedResponse(val source: Throwable) :
        SecurityAdvisoryBackendFailure("Invalid security advisory response")

    data object UnexpectedPackages : SecurityAdvisoryBackendFailure("Response packages do not match request")
}
