package com.crackmydroid.shared.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.crackmydroid.shared.i18n.Strings
import com.crackmydroid.shared.presentation.navigation.Screen
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

data class DrawerItemUiState(val screen: Screen, val label: String, val icon: ImageVector, val selected: Boolean)

data class DrawerSectionUiState(val title: String?, val items: List<DrawerItemUiState>)

internal object NavigationChromeMapper {
    fun topBar(
        screen: Screen,
        canNavigateBack: Boolean,
        strings: Strings,
        penTestStep: PenTestWizardStep,
        apkContentStep: Int = 1,
        searchRequestId: Long,
        searchVisible: Boolean = true,
    ) = TopBarUiState(
        title = screen.title(strings),
        navigationAction = if (canNavigateBack) AppTopBarNavigationAction.Back else AppTopBarNavigationAction.Menu,
        showSearch = screen.supportsSearch(penTestStep, apkContentStep),
        showHelp = true,
        helpMessage = screen.helpMessage(strings),
        searchRequestId = searchRequestId,
        searchVisible = searchVisible,
    )

    fun drawerSections(selectedScreen: Screen, strings: Strings): List<DrawerSectionUiState> = listOf(
        drawerSection(null, listOf(Screen.Dashboard), selectedScreen, strings),
        drawerSection(strings.navSectionAnalyzeApps, listOf(Screen.InstalledApps, Screen.AnalyzeApkContent, Screen.Permissions, Screen.Activities), selectedScreen, strings),
        drawerSection(strings.navSectionDeviceSecurity, listOf(Screen.Pentest, Screen.Root, Screen.Info), selectedScreen, strings),
        drawerSection(strings.navSectionTools, listOf(Screen.Trick, Screen.Log), selectedScreen, strings),
        drawerSection(strings.navSectionActivity, listOf(Screen.History), selectedScreen, strings),
        drawerSection(strings.navSectionSupport, listOf(Screen.Help), selectedScreen, strings),
        drawerSection(null, listOf(Screen.Settings), selectedScreen, strings),
    )

    private fun drawerSection(
        title: String?,
        screens: List<Screen>,
        selectedScreen: Screen,
        strings: Strings,
    ) = DrawerSectionUiState(
        title = title,
        items = screens.map { screen ->
            DrawerItemUiState(screen, screen.title(strings), screen.icon, screen == selectedScreen)
        },
    )

    private fun Screen.supportsSearch(penTestStep: PenTestWizardStep, apkContentStep: Int): Boolean = when (this) {
        Screen.Activities,
        Screen.InstalledApps,
        Screen.Permissions,
        Screen.Log,
        -> true

        Screen.Pentest -> penTestStep == PenTestWizardStep.SelectApp

        Screen.AnalyzeApkContent -> apkContentStep == 1

        else -> false
    }

    private fun Screen.helpMessage(strings: Strings): String = when (this) {
        Screen.Dashboard -> strings.helpHome
        Screen.Activities -> strings.helpActivities
        Screen.Root -> strings.helpRoot
        Screen.Info -> strings.helpInfo
        Screen.Trick -> strings.helpTrick
        Screen.Settings -> strings.helpSettings
        Screen.Log -> strings.helpLog
        Screen.Permissions -> strings.helpPermissions
        Screen.InstalledApps -> strings.helpApk
        Screen.Pentest -> strings.helpPentest
        Screen.AnalyzeApkContent -> strings.helpAnalyzeApkContent
        Screen.Help -> strings.helpOverview
        Screen.History -> strings.navHistory
    }
}
