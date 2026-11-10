package com.crackmydroid.shared.ui.home

import com.crackmydroid.architecture.presentation.model.UiEvent

sealed class HomeEvent : UiEvent() {
    class HideFromRecent(val operationId: String) : HomeEvent()
    class Remove(val operationId: String) : HomeEvent()
}
