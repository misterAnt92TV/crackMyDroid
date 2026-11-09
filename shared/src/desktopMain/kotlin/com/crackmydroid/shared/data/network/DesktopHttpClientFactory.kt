package com.crackmydroid.shared.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO

fun createDesktopHttpClient(): HttpClient = HttpClient(CIO) {
    configureCrackMyDroidNetwork(debugLogging = false)
}
