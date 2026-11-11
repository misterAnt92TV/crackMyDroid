package com.crackmydroid.shared.ui.activities.model

sealed interface ActivityListEffect {
    data class CopyToClipboard(val content: String) : ActivityListEffect
}
