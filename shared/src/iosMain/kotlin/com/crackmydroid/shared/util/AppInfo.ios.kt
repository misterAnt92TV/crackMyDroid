package com.crackmydroid.shared.util

import androidx.compose.runtime.Composable

@Composable
actual fun appVersionName(): String = "n/d"

@Composable
actual fun appPackageName(): String = "n/d"

@Composable
actual fun appRequestedPermissions(): List<String> = emptyList()

@Composable
actual fun appSdkInfo(): SdkInfo? = null
