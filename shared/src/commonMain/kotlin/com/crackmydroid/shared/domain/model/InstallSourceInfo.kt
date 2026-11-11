package com.crackmydroid.shared.domain.model

import kotlinx.serialization.Serializable

/**
 * Provenance reported by the operating system, not a certificate of the APK or its safety.
 */
@Serializable
data class InstallSourceInfo(
    val classification: InstallSourceClassification = InstallSourceClassification.UNAVAILABLE,
    val installingPackageName: String? = null,
    val initiatingPackageName: String? = null,
    val originatingPackageName: String? = null,
    val observedAtEpochMs: Long? = null,
    val unavailableReason: String? = null,
)

@Serializable
enum class InstallSourceClassification {
    GOOGLE_PLAY,
    OTHER,
    UNKNOWN,
    UNAVAILABLE,
}
