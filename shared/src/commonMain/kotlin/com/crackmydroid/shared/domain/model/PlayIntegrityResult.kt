package com.crackmydroid.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PlayIntegrityResult(
    val basicIntegrity: Boolean? = null,
    val deviceIntegrity: Boolean? = null,
    val details: String,
    val appRecognitionVerdict: String? = null,
    val appLicensingVerdict: String? = null,
    val deviceRecognitionVerdicts: List<String> = emptyList(),
    val playProtectVerdict: String? = null,
    val verifiedAtEpochMs: Long? = null,
)
