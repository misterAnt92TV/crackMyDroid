package com.crackmydroid.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AppComponent(
    val label: String,
    val appLabel: String,
    val packageName: String,
    val name: String,
    val type: ComponentType,
    val exported: Boolean = false,
    val permission: String? = null,
    val launchableViaShell: Boolean = true,
    val launchabilityReason: String? = null,
    val launchContextHint: String? = null,
    val launchIntentAction: String? = null,
    val launchIntentData: String? = null,
    val launchIntentMimeType: String? = null
)
