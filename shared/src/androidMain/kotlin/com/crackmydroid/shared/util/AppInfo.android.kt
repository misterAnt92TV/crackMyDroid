package com.crackmydroid.shared.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import android.content.pm.PackageManager

@Composable
actual fun appVersionName(): String {
    val context = LocalContext.current
    return remember {
        runCatching {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            info.versionName ?: "n/d"
        }.getOrDefault("n/d")
    }
}

@Composable
actual fun appPackageName(): String {
    val context = LocalContext.current
    return remember { context.packageName }
}

@Composable
actual fun appRequestedPermissions(): List<String> {
    val context = LocalContext.current
    return remember {
        runCatching {
            val pkgInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_PERMISSIONS
            )
            pkgInfo.requestedPermissions?.toList() ?: emptyList()
        }.getOrDefault(emptyList())
    }
}

@Composable
actual fun appSdkInfo(): SdkInfo? {
    val context = LocalContext.current
    return remember {
        runCatching {
            val appInfo = context.applicationInfo
            SdkInfo(
                minSdk = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) appInfo.minSdkVersion else null,
                targetSdk = appInfo.targetSdkVersion
            )
        }.getOrNull()
    }
}
