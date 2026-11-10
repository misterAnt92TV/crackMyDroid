package com.crackmydroid.shared.ui.activities

sealed interface ActivityListEffect {
    data class CopyToClipboard(val content: String) : ActivityListEffect
}
