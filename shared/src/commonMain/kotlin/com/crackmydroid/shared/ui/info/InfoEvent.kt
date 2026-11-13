package com.crackmydroid.shared.ui.info

import com.crackmydroid.architecture.presentation.model.UiEvent

sealed class InfoEvent : UiEvent() {
    class Load : InfoEvent()
    class Export : InfoEvent()
    class Share(val contentOverride: String? = null) : InfoEvent()
}
