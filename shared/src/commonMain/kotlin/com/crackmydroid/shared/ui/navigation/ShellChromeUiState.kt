package com.crackmydroid.shared.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.crackmydroid.shared.i18n.Strings
import com.crackmydroid.shared.presentation.navigation.Screen
import com.crackmydroid.shared.presentation.navigation.bottomScreens
import com.crackmydroid.shared.ui.pentest.model.PenTestWizardStep
import com.crackmydroid.ui.components.navigation.AppTopBarNavigationAction

data class TopBarUiState(
    val title: String,
    val navigationAction: AppTopBarNavigationAction,
    val showSearch: Boolean,
    val showHelp: Boolean = true,
    val helpMessage: String,
    val searchRequestId: Long = 0,
    val searchVisible: Boolean = true,
)

data class BottomBarItemUiState(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
    val selected: Boolean,
)

internal object NavigationChromeMapper {
    fun topBar(
        screen: Screen,
        canNavigateBack: Boolean,
        strings: Strings,
        penTestStep: PenTestWizardStep,
        searchRequestId: Long,
        searchVisible: Boolean = true,
    ) = TopBarUiState(
        title = screen.title(strings),
        navigationAction = if (canNavigateBack) AppTopBarNavigationAction.Back else AppTopBarNavigationAction.Menu,
        showSearch = screen.supportsSearch(penTestStep),
        showHelp = true,
        helpMessage = screen.helpMessage(strings),
        searchRequestId = searchRequestId,
        searchVisible = searchVisible,
    )

    fun bottomItems(selectedScreen: Screen, strings: Strings): List<BottomBarItemUiState> = bottomScreens.map { screen ->
        BottomBarItemUiState(
            screen = screen,
            label = screen.title(strings),
            icon = screen.icon,
            selected = screen == selectedScreen,
        )
    }

    private fun Screen.supportsSearch(penTestStep: PenTestWizardStep): Boolean = when (this) {
        Screen.Activities,
        Screen.InstalledApps,
        Screen.Permissions,
        Screen.Log -> true
        Screen.Pentest -> penTestStep == PenTestWizardStep.SelectApp
        else -> false
    }

    private fun Screen.helpMessage(strings: Strings): String = when (this) {
        Screen.Home -> strings.helpHome
        Screen.Activities -> strings.helpActivities
        Screen.Root -> strings.helpRoot
        Screen.Info -> strings.helpInfo
        Screen.Trick -> strings.helpTrick
        Screen.Settings -> strings.helpSettings
        Screen.Log -> strings.helpLog
        Screen.Permissions -> strings.helpPermissions
        Screen.InstalledApps -> strings.helpApk
        Screen.Pentest -> strings.helpPentest
        Screen.Help -> strings.helpOverview
    }
}
