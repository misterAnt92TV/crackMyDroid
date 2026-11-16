package com.crackmydroid.shared.ui.navigation

import com.crackmydroid.architecture.presentation.model.UiState
import com.crackmydroid.shared.i18n.stringsFor
import com.crackmydroid.shared.presentation.navigation.Screen
import com.crackmydroid.shared.ui.pentest.model.PenTestWizardStep

data class AppNavigationState(
    val selectedScreen: Screen = Screen.Dashboard,
    val navigationHistory: List<Screen> = emptyList(),
    val topBar: TopBarUiState = NavigationChromeMapper.topBar(
        screen = Screen.Dashboard,
        canNavigateBack = false,
        strings = stringsFor("en"),
        penTestStep = PenTestWizardStep.SelectApp,
        searchRequestId = 0,
    ),
    val searchVisibilityByScreen: Map<Screen, Boolean> = emptyMap(),
    val drawerSections: List<DrawerSectionUiState> = NavigationChromeMapper.drawerSections(
        selectedScreen = Screen.Dashboard,
        strings = stringsFor("en"),
    ),
    val showIntro: Boolean = true,
    val pendingHintScreen: Screen? = null,
    val pendingNavigationSource: NavigationSource? = null,
    val helpDialog: String? = null,
    val initialized: Boolean = false,
) : UiState
