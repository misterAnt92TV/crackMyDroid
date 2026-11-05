package com.crackmydroid.shared.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

fun createIosHttpClient(): HttpClient = HttpClient(Darwin) {
    configureCrackMyDroidNetwork(debugLogging = false)
}
