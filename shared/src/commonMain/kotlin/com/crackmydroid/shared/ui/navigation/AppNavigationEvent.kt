package com.crackmydroid.shared.ui.navigation

import com.crackmydroid.architecture.presentation.model.UiEvent
import com.crackmydroid.shared.presentation.navigation.Screen

sealed class AppNavigationEvent : UiEvent() {
    class Initialize : AppNavigationEvent()
    class ScreenSelected(
        val screen: Screen,
        val origin: NavigationSource,
        val hintsEnabled: Boolean,
    ) : AppNavigationEvent()
    class BackRequested : AppNavigationEvent()
    class TopBarSearchRequested : AppNavigationEvent()
    class TopBarHelpRequested : AppNavigationEvent()
    class HintConfirmed(val disableHints: Boolean, val target: Screen) : AppNavigationEvent()
    class HintDismissed : AppNavigationEvent()
    class HelpDismissed : AppNavigationEvent()
    class IntroDismissed : AppNavigationEvent()
}

enum class NavigationSource { Drawer, Rail, BottomBar, InContent }
