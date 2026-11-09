package com.crackmydroid.shared.data.network

import android.content.Context
import android.content.pm.ApplicationInfo
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

fun createAndroidHttpClient(context: Context): HttpClient = HttpClient(OkHttp) {
    configureCrackMyDroidNetwork(
        debugLogging = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    )
}
