package com.crackmydroid.shared.ui.appshell

import com.crackmydroid.shared.AppLogger
import com.crackmydroid.shared.domain.usecase.settings.GetIntroSeenUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetVerboseLogUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetIntroSeenUseCase
import com.crackmydroid.shared.i18n.platformLanguage
import com.crackmydroid.shared.i18n.resolveLanguageCode
import com.crackmydroid.shared.presentation.BaseViewModel
import com.crackmydroid.shared.presentation.navigation.Screen
import com.crackmydroid.shared.ui.activities.ActivityListViewModel
import com.crackmydroid.shared.ui.info.InfoViewModel
import com.crackmydroid.shared.ui.installedapps.InstalledAppsViewModel
import com.crackmydroid.shared.ui.logs.LogViewModel
import com.crackmydroid.shared.ui.pentest.PenTestingViewModel
import com.crackmydroid.shared.ui.permissions.PermissionsViewModel
import com.crackmydroid.shared.ui.settings.SettingsViewModel
import com.crackmydroid.shared.ui.trick.TrickViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppShellViewModel(
    private val getIntroSeen: GetIntroSeenUseCase,
    private val setIntroSeen: SetIntroSeenUseCase,
    private val activity: ActivityListViewModel,
    private val info: InfoViewModel,
    private val trick: TrickViewModel,
    private val settings: SettingsViewModel,
    private val logs: LogViewModel,
    private val permissions: PermissionsViewModel,
    private val installedApps: InstalledAppsViewModel,
    private val penTesting: PenTestingViewModel,
    private val getVerbose: GetVerboseLogUseCase,
) : BaseViewModel() {
    private val _state = MutableStateFlow(AppShellState())
    val state: StateFlow<AppShellState> = _state
    private var initializing = false

    fun initialize() {
        if (_state.value.initialized || initializing) return
        initializing = true
        scope.launch {
            try {
                getVerbose().onSuccess(AppLogger::setVerbose)
                val settingsLoad = settings.load()
                settingsLoad.join()
                if (settingsLoad.isCancelled) throw CancellationException("Settings initialization cancelled")
                updateLanguage(resolveLanguageCode(settings.state.value.language, platformLanguage()))
                val introSeen = getIntroSeen().getOrDefault(false)
                _state.update { it.copy(showIntro = !introSeen, initialized = true) }
                activity.refresh(); info.load(); trick.load(); logs.refresh()
                permissions.refresh(); installedApps.refresh(); penTesting.refresh()
                settings.state.map { it.language }.distinctUntilChanged().collect { language ->
                    updateLanguage(resolveLanguageCode(language, platformLanguage()))
                }
            } finally {
                initializing = false
            }
        }
    }

    fun updateLanguage(languageCode: String) {
        info.updateLanguage(languageCode)
        permissions.updateLanguage(languageCode)
        penTesting.updateLanguage(languageCode)
    }

    fun navigate(screen: Screen, hintsEnabled: Boolean) {
        if (hintsEnabled && screen in setOf(
                Screen.Permissions,
                Screen.InstalledApps,
                Screen.Pentest,
                Screen.Activities,
                Screen.Root,
                Screen.Trick,
            )
        ) {
            _state.update { it.copy(pendingHintScreen = screen) }
        } else {
            _state.update { it.copy(selectedScreen = screen) }
        }
    }

    fun confirmHint(disableHints: Boolean, target: Screen) {
        if (disableHints) settings.setFeatureHintsEnabled(false)
        _state.update { it.copy(selectedScreen = target, pendingHintScreen = null) }
    }

    fun dismissHint() = _state.update { it.copy(pendingHintScreen = null) }
    fun dismissHelp() = _state.update { it.copy(helpDialog = null) }
    fun showHelp(message: String) = _state.update { it.copy(helpDialog = message) }
    fun dismissIntro() {
        _state.update { it.copy(showIntro = false) }
        scope.launch {
            setIntroSeen(true)
        }
    }
}
