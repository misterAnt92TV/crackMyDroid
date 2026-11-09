package com.crackmydroid.shared.domain.model

import com.crackmydroid.shared.domain.model.root.RootCheck
import kotlinx.serialization.Serializable

@Serializable
data class RootStatus(
    val isRooted: Boolean,
    val details: String,
    val checks: List<RootCheck> = emptyList(),
)
