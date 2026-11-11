package com.crackmydroid.shared.ui.appshell

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
import com.crackmydroid.shared.ui.permissions.PermissionsEvent
import com.crackmydroid.shared.ui.permissions.PermissionsViewModel
import com.crackmydroid.shared.ui.root.RootViewModel
import com.crackmydroid.shared.ui.settings.SettingsEvent
import com.crackmydroid.shared.ui.settings.SettingsViewModel
import com.crackmydroid.shared.ui.trick.TrickEvent
import com.crackmydroid.shared.ui.trick.TrickViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class AppShellViewModel(
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
) : LocalizedBaseViewModel<AppShellEvent, AppShellState>(AppShellState(), dispatchers) {
    private var initializing = false

    override fun onEvent(event: AppShellEvent) = when (event) {
        is AppShellEvent.Initialize -> initialize()
        is AppShellEvent.ScreenSelected -> navigate(event.screen, event.hintsEnabled)
        is AppShellEvent.HintConfirmed -> confirmHint(event.disableHints, event.target)
        is AppShellEvent.HintDismissed -> dismissHint()
        is AppShellEvent.HelpDismissed -> dismissHelp()
        is AppShellEvent.HelpRequested -> showHelp(event.message)
        is AppShellEvent.IntroDismissed -> dismissIntro()
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
    }

    private fun navigate(screen: Screen, hintsEnabled: Boolean) {
        if (hintsEnabled && screen in setOf(
                Screen.Permissions,
                Screen.InstalledApps,
                Screen.Pentest,
                Screen.Activities,
                Screen.Root,
                Screen.Trick,
            )
        ) {
            updateState { it.copy(pendingHintScreen = screen) }
        } else {
            updateState { it.copy(selectedScreen = screen) }
        }
    }

    private fun confirmHint(disableHints: Boolean, target: Screen) {
        if (disableHints) settings.handleEvent(SettingsEvent.FeatureHintsEnabledChanged(false))
        updateState { it.copy(selectedScreen = target, pendingHintScreen = null) }
    }

    private fun dismissHint() = updateState { it.copy(pendingHintScreen = null) }
    private fun dismissHelp() = updateState { it.copy(helpDialog = null) }
    private fun showHelp(message: String) = updateState { it.copy(helpDialog = message) }
    private fun dismissIntro() {
        updateState { it.copy(showIntro = false) }
        launchTask {
            setIntroSeenUseCase(true)
        }
    }
}
