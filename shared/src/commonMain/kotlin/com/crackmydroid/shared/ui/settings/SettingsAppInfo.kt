package com.crackmydroid.shared.ui.settings

data class SettingsAppInfo(
    val versionName: String,
    val packageName: String,
    val minSdk: Int?,
    val targetSdk: Int?,
    val requestedPermissions: List<String>
)
