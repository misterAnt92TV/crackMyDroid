package com.crackmydroid.shared.ui.activities.model

import com.crackmydroid.architecture.presentation.model.UiEffect

sealed interface ActivityListEffect : UiEffect {
    data class CopyToClipboard(val content: String) : ActivityListEffect
}
