package com.crackmydroid.shared.ui.appshell

import com.crackmydroid.architecture.presentation.model.UiEvent
import com.crackmydroid.shared.presentation.navigation.Screen

sealed class AppShellEvent : UiEvent() {
    class Initialize : AppShellEvent()
    class ScreenSelected(val screen: Screen, val hintsEnabled: Boolean) : AppShellEvent()
    class HintConfirmed(val disableHints: Boolean, val target: Screen) : AppShellEvent()
    class HintDismissed : AppShellEvent()
    class HelpDismissed : AppShellEvent()
    class HelpRequested(val message: String) : AppShellEvent()
    class IntroDismissed : AppShellEvent()
}
