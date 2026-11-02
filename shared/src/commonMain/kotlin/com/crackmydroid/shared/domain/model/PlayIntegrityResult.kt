package com.crackmydroid.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PlayIntegrityResult(
    val basicIntegrity: Boolean? = null,
    val deviceIntegrity: Boolean? = null,
    val details: String
)
