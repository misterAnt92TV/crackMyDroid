package com.crackmydroid.shared.ui.navigation

import com.crackmydroid.architecture.coroutines.AppDispatchers
import com.crackmydroid.architecture.logging.AppLogger
import com.crackmydroid.shared.domain.usecase.settings.GetIntroSeenUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetVerboseLogUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetIntroSeenUseCase
import com.crackmydroid.shared.i18n.platformLanguage
import com.crackmydroid.shared.i18n.resolveLanguageCode
import com.crackmydroid.shared.presentation.LocalizedBaseViewModel
import com.crackmydroid.shared.presentation.navigation.Screen
import com.crackmydroid.shared.ui.activities.ActivityListEvent
import com.crackmydroid.shared.ui.activities.ActivityListViewModel
import com.crackmydroid.shared.ui.home.HomeViewModel
import com.crackmydroid.shared.ui.info.InfoEvent
import com.crackmydroid.shared.ui.info.InfoViewModel
import com.crackmydroid.shared.ui.installedapps.InstalledAppsEvent
import com.crackmydroid.shared.ui.installedapps.InstalledAppsViewModel
import com.crackmydroid.shared.ui.logs.LogEvent
import com.crackmydroid.shared.ui.logs.LogViewModel
import com.crackmydroid.shared.ui.pentest.PenTestingEvent
import com.crackmydroid.shared.ui.pentest.PenTestingViewModel
import com.crackmydroid.shared.ui.pentest.model.PenTestWizardStep
import com.crackmydroid.shared.ui.permissions.PermissionsEvent
import com.crackmydroid.shared.ui.permissions.PermissionsViewModel
import com.crackmydroid.shared.ui.root.RootViewModel
import com.crackmydroid.shared.ui.settings.SettingsEvent
import com.crackmydroid.shared.ui.settings.SettingsViewModel
import com.crackmydroid.shared.ui.trick.TrickEvent
import com.crackmydroid.shared.ui.trick.TrickViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class AppNavigationViewModel(
    private val getIntroSeenUseCase: GetIntroSeenUseCase,
    private val setIntroSeenUseCase: SetIntroSeenUseCase,
    private val activity: ActivityListViewModel,
    private val home: HomeViewModel,
    private val root: RootViewModel,
    private val info: InfoViewModel,
    private val trick: TrickViewModel,
    private val settings: SettingsViewModel,
    private val logs: LogViewModel,
    private val permissions: PermissionsViewModel,
    private val installedApps: InstalledAppsViewModel,
    private val penTesting: PenTestingViewModel,
    private val getVerboseLogUseCase: GetVerboseLogUseCase,
    dispatchers: AppDispatchers,
) : LocalizedBaseViewModel<AppNavigationEvent, AppNavigationState>(AppNavigationState(), dispatchers) {
    private var initializing = false

    override fun onEvent(event: AppNavigationEvent) = when (event) {
        is AppNavigationEvent.Initialize -> initialize()
        is AppNavigationEvent.ScreenSelected -> navigate(event.screen, event.origin, event.hintsEnabled)
        is AppNavigationEvent.BackRequested -> navigateBack()
        is AppNavigationEvent.TopBarSearchRequested -> requestTopBarSearch()
        is AppNavigationEvent.TopBarHelpRequested -> showHelp(currentState.topBar.helpMessage)
        is AppNavigationEvent.HintConfirmed -> confirmHint(event.disableHints, event.target)
        is AppNavigationEvent.HintDismissed -> dismissHint()
        is AppNavigationEvent.HelpDismissed -> dismissHelp()
        is AppNavigationEvent.IntroDismissed -> dismissIntro()
    }

    private fun initialize() {
        if (currentState.initialized || initializing) return
        initializing = true
        launchLatest("app-shell-initialize") {
            try {
                getVerboseLogUseCase().onSuccess(AppLogger::setVerbose)
                settings.handleEvent(SettingsEvent.Load())
                updateLanguage(resolveLanguageCode(settings.state.value.language, platformLanguage()))
                val introSeen = getIntroSeenUseCase().getOrDefault(false)
                updateState { it.copy(showIntro = !introSeen, initialized = true) }
                activity.handleEvent(ActivityListEvent.Refresh())
                info.handleEvent(InfoEvent.Load())
                trick.handleEvent(TrickEvent.Load())
                logs.handleEvent(LogEvent.Refresh())
                permissions.handleEvent(PermissionsEvent.Refresh())
                installedApps.handleEvent(InstalledAppsEvent.Refresh())
                penTesting.handleEvent(PenTestingEvent.Refresh())
                launchTask {
                    penTesting.state
                        .map { it.currentStep }
                        .distinctUntilChanged()
                        .collect { refreshChrome(penTestStep = it) }
                }
                settings.state.map { it.language }.distinctUntilChanged().collect { language ->
                    updateLanguage(resolveLanguageCode(language, platformLanguage()))
                }
            } finally {
                initializing = false
            }
        }
    }

    override fun updateLanguage(languageCode: String) {
        super.updateLanguage(languageCode)
        info.updateLanguage(languageCode)
        permissions.updateLanguage(languageCode)
        penTesting.updateLanguage(languageCode)
        activity.updateLanguage(languageCode)
        home.updateLanguage(languageCode)
        root.updateLanguage(languageCode)
        trick.updateLanguage(languageCode)
        settings.updateLanguage(languageCode)
        logs.updateLanguage(languageCode)
        installedApps.updateLanguage(languageCode)
        refreshChrome()
    }

    private fun navigate(screen: Screen, origin: NavigationSource, hintsEnabled: Boolean) {
        if (screen == currentState.selectedScreen) return
        if (hintsEnabled && screen in setOf(
                Screen.Permissions,
                Screen.InstalledApps,
                Screen.Pentest,
                Screen.Activities,
                Screen.Root,
                Screen.Trick,
            )
        ) {
            updateState { it.copy(pendingHintScreen = screen, pendingNavigationSource = origin) }
        } else {
            navigateTo(screen, origin)
        }
    }

    private fun confirmHint(disableHints: Boolean, target: Screen) {
        if (disableHints) settings.handleEvent(SettingsEvent.FeatureHintsEnabledChanged(false))
        navigateTo(target, currentState.pendingNavigationSource ?: NavigationSource.Drawer)
    }

    private fun navigateTo(screen: Screen, origin: NavigationSource) {
        val state = currentState
        val history = when {
            origin == NavigationSource.BottomBar -> emptyList()
            state.navigationHistory.lastOrNull() == state.selectedScreen -> state.navigationHistory
            else -> state.navigationHistory + state.selectedScreen
        }
        updateNavigationState(
            selectedScreen = screen,
            navigationHistory = history,
            pendingHintScreen = null,
            pendingNavigationSource = null,
        )
    }

    private fun navigateBack() {
        val state = currentState
        when {
            state.navigationHistory.isNotEmpty() -> {
                val target = state.navigationHistory.last()
                updateNavigationState(
                    selectedScreen = target,
                    navigationHistory = state.navigationHistory.dropLast(1),
                )
            }

            state.selectedScreen != Screen.Home -> navigateTo(Screen.Home, NavigationSource.BottomBar)
            else -> Unit
        }
    }

    private fun requestTopBarSearch() {
        if (!currentState.topBar.showSearch) return
        updateState { state -> state.copy(topBar = state.topBar.copy(searchRequestId = state.topBar.searchRequestId + 1)) }
    }

    private fun refreshChrome(penTestStep: PenTestWizardStep = penTesting.state.value.currentStep) = updateState { state ->
        stateWithChrome(state, penTestStep = penTestStep)
    }

    private fun updateNavigationState(
        selectedScreen: Screen,
        navigationHistory: List<Screen>,
        pendingHintScreen: Screen? = currentState.pendingHintScreen,
        pendingNavigationSource: NavigationSource? = currentState.pendingNavigationSource,
    ) = updateState { state ->
        stateWithChrome(
            state = state,
            selectedScreen = selectedScreen,
            navigationHistory = navigationHistory,
            pendingHintScreen = pendingHintScreen,
            pendingNavigationSource = pendingNavigationSource,
        )
    }

    private fun stateWithChrome(
        state: AppNavigationState,
        selectedScreen: Screen = state.selectedScreen,
        navigationHistory: List<Screen> = state.navigationHistory,
        pendingHintScreen: Screen? = state.pendingHintScreen,
        pendingNavigationSource: NavigationSource? = state.pendingNavigationSource,
        penTestStep: PenTestWizardStep = penTesting.state.value.currentStep,
    ): AppNavigationState = state.copy(
        selectedScreen = selectedScreen,
        navigationHistory = navigationHistory,
        pendingHintScreen = pendingHintScreen,
        pendingNavigationSource = pendingNavigationSource,
        topBar = NavigationChromeMapper.topBar(
            screen = selectedScreen,
            canNavigateBack = navigationHistory.isNotEmpty(),
            strings = strings,
            penTestStep = penTestStep,
            searchRequestId = state.topBar.searchRequestId,
        ),
        bottomBarItems = NavigationChromeMapper.bottomItems(selectedScreen, strings),
    )

    private fun dismissHint() = updateState { it.copy(pendingHintScreen = null, pendingNavigationSource = null) }
    private fun dismissHelp() = updateState { it.copy(helpDialog = null) }
    private fun showHelp(message: String) = updateState { it.copy(helpDialog = message) }
    private fun dismissIntro() {
        updateState { it.copy(showIntro = false) }
        launchTask {
            setIntroSeenUseCase(true)
        }
    }
}
