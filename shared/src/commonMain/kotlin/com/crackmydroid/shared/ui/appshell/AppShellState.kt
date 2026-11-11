package com.crackmydroid.shared.ui.appshell

import com.crackmydroid.architecture.presentation.model.UiState
import com.crackmydroid.shared.presentation.navigation.Screen

data class AppShellState(
    val selectedScreen: Screen = Screen.Home,
    val showIntro: Boolean = true,
    val pendingHintScreen: Screen? = null,
    val helpDialog: String? = null,
    val initialized: Boolean = false,
) : UiState
