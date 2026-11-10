package com.crackmydroid.shared.ui.root

import com.crackmydroid.architecture.presentation.model.UiEvent

sealed class RootEvent : UiEvent() {
    class Verify : RootEvent()
    class VerifyRootAccess : RootEvent()
    class PlayIntegrityNonceChanged(val value: String) : RootEvent()
    class VerifyPlayIntegrity : RootEvent()
    class ShareReport : RootEvent()
    class ErrorDismissed : RootEvent()
}
