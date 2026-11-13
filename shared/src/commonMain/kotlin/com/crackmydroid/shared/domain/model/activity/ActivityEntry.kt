package com.crackmydroid.shared.domain.model.activity

import kotlinx.serialization.Serializable

@Serializable
data class ActivityEntry(
    val label: String,
    val appLabel: String,
    val packageName: String,
    val activityName: String,
    val launchableViaShell: Boolean = true,
    val launchabilityReason: String? = null,
    val launchContextHint: String? = null,
    val launchIntentAction: String? = null,
    val launchIntentData: String? = null,
    val launchIntentMimeType: String? = null,
    val exported: Boolean = false,
    val permission: String? = null,
    val launchMode: String? = null,
    val targetActivity: String? = null,
)
