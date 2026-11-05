package com.crackmydroid.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ComponentType {
    ACTIVITY,
    SERVICE,
    RECEIVER,
    PROVIDER
}
