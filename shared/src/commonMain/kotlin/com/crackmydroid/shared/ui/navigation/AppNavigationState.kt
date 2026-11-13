package com.crackmydroid.shared.ui.navigation

import com.crackmydroid.architecture.presentation.model.UiState
import com.crackmydroid.shared.i18n.stringsFor
import com.crackmydroid.shared.presentation.navigation.Screen
import com.crackmydroid.shared.ui.pentest.model.PenTestWizardStep

data class AppNavigationState(
    val selectedScreen: Screen = Screen.Home,
    val navigationHistory: List<Screen> = emptyList(),
    val topBar: TopBarUiState = NavigationChromeMapper.topBar(
        screen = Screen.Home,
        canNavigateBack = false,
        strings = stringsFor("en"),
        penTestStep = PenTestWizardStep.SelectApp,
        searchRequestId = 0,
    ),
    val searchVisibilityByScreen: Map<Screen, Boolean> = emptyMap(),
    val bottomBarItems: List<BottomBarItemUiState> = NavigationChromeMapper.bottomItems(
        selectedScreen = Screen.Home,
        strings = stringsFor("en"),
    ),
    val showIntro: Boolean = true,
    val pendingHintScreen: Screen? = null,
    val pendingNavigationSource: NavigationSource? = null,
    val helpDialog: String? = null,
    val initialized: Boolean = false,
) : UiState
