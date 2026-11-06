package com.crackmydroid.shared.data.root

import android.content.Context
import android.content.pm.PackageManager
import com.crackmydroid.shared.domain.model.PlayIntegrityResult
import com.crackmydroid.shared.domain.repository.IntegrityVerificationRepository
import kotlinx.coroutines.CancellationException

class IntegrityVerificationRepositoryAndroid(
    private val context: Context,
    private val backendClient: IntegrityBackendClient
) : IntegrityVerificationRepository {
    override suspend fun verify(integrityToken: String, requestBinding: String): PlayIntegrityResult {
        val endpoint = backendEndpoint() ?: return unavailable("Backend di verifica non configurato")
        return try {
            backendClient.verify(
                endpoint = endpoint,
                integrityToken = integrityToken,
                requestBinding = requestBinding,
                userAgent = userAgent()
            )
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            unavailable(error.message ?: "Verifica Play Integrity non riuscita")
        }
    }

    private fun backendEndpoint(): String? = runCatching {
        val info = context.packageManager.getApplicationInfo(
            context.packageName,
            PackageManager.GET_META_DATA
        )
        info.metaData?.getString(BACKEND_ENDPOINT_KEY)?.trim()?.takeIf { it.startsWith("https://") }
    }.getOrNull()

    private fun userAgent(): String = runCatching {
        val version = context.packageManager.getPackageInfo(context.packageName, 0).versionName
        "CrackMyDroid/${version ?: "unknown"} (Android)"
    }.getOrDefault("CrackMyDroid/unknown (Android)")

    private fun unavailable(detail: String) = PlayIntegrityResult(details = detail)

    private companion object {
        const val BACKEND_ENDPOINT_KEY = "com.crackmydroid.INTEGRITY_VERIFICATION_ENDPOINT"
    }
}
