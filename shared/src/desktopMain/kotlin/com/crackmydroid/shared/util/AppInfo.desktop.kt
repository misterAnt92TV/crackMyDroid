package com.crackmydroid.shared.util

import androidx.compose.runtime.Composable

@Composable
actual fun appVersionName(): String = "desktop"

@Composable
actual fun appPackageName(): String = "com.crackmydroid.desktop"

@Composable
actual fun appRequestedPermissions(): List<String> = emptyList()

@Composable
actual fun appSdkInfo(): SdkInfo? = null
