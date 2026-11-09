package com.crackmydroid.shared.ui.settings.model

data class SettingsAppInfo(
    val versionName: String,
    val packageName: String,
    val minSdk: Int?,
    val targetSdk: Int?,
    val requestedPermissions: List<String>
)
