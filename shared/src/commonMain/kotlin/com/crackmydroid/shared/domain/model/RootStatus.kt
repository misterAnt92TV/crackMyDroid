package com.crackmydroid.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class RootStatus(
    val isRooted: Boolean,
    val details: String
)
