package com.crackmydroid.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ConnectedDevice(
    val serial: String,
    val state: String,
    val model: String? = null,
    val product: String? = null,
    val device: String? = null,
    val transportId: String? = null,
)
