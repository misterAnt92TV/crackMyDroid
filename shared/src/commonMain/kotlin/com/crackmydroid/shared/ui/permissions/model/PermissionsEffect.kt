package com.crackmydroid.shared.ui.permissions.model

sealed interface PermissionsEffect {
    data class CopyToClipboard(val content: String) : PermissionsEffect
}
