package com.crackmydroid.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class InstalledAppEntry(
    val appLabel: String,
    val packageName: String,
    val sourcePath: String,
    val debuggable: Boolean = false,
    val allowBackup: Boolean = false,
    val testOnly: Boolean = false,
    val targetSdkVersion: Int? = null,
    val minSdkVersion: Int? = null,
    val installSource: InstallSourceInfo = InstallSourceInfo()
)
