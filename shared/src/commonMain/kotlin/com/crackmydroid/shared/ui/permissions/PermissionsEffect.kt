package com.crackmydroid.shared.ui.permissions

sealed interface PermissionsEffect {
    data class CopyToClipboard(val content: String) : PermissionsEffect
}
