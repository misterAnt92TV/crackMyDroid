package com.crackmydroid.shared.data.securityadvisory

import android.content.Context
import android.content.pm.PackageManager

class SecurityAdvisoryEndpointProviderAndroid(private val context: Context) : SecurityAdvisoryEndpointProvider {
    override fun endpoint(): String? = runCatching {
        val applicationInfo = context.packageManager.getApplicationInfo(
            context.packageName,
            PackageManager.GET_META_DATA,
        )
        applicationInfo.metaData?.getString(BACKEND_ENDPOINT_KEY)?.asHttpsEndpointOrNull()
    }.getOrNull()

    private companion object {
        const val BACKEND_ENDPOINT_KEY = "com.crackmydroid.SECURITY_ADVISORY_ENDPOINT"
    }
}
