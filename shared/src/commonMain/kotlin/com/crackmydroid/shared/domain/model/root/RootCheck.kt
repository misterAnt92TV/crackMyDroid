package com.crackmydroid.shared.domain.model.root

import kotlinx.serialization.Serializable

@Serializable
enum class RootCheckId {
    SU_BINARY,
    BUSYBOX,
    SUDO,
    ROOT_MANAGER,
    MAGISK_TRACES,
    ROOTBEER,
    ROOT_ACCESS
}

@Serializable
enum class RootCheckOutcome {
    DETECTED,
    NOT_DETECTED,
    DENIED,
    TIMEOUT,
    UNAVAILABLE
}

@Serializable
data class RootCheck(
    val id: RootCheckId,
    val outcome: RootCheckOutcome,
    val evidence: String = "",
)

@Serializable
enum class RootCheckMode {
    PASSIVE,
    PRIVILEGE_ACCESS
}
