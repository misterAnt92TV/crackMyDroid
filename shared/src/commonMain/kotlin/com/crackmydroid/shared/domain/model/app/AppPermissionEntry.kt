package com.crackmydroid.shared.domain.model.app

import kotlinx.serialization.Serializable

@Serializable
data class AppPermissionEntry(val appLabel: String, val packageName: String, val permissions: List<String>)
