package com.crackmydroid.shared.util

import androidx.compose.runtime.Composable

data class SdkInfo(val minSdk: Int?, val targetSdk: Int?)

@Composable
expect fun appVersionName(): String

@Composable
expect fun appPackageName(): String

@Composable
expect fun appRequestedPermissions(): List<String>

@Composable
expect fun appSdkInfo(): SdkInfo?
