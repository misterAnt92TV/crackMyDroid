package com.crackmydroid.shared.ui.settings

import com.crackmydroid.architecture.coroutines.AppDispatchers
import com.crackmydroid.architecture.logging.AppLogger
import com.crackmydroid.shared.domain.model.ExportFormat
import com.crackmydroid.shared.domain.model.app.AppLanguage
import com.crackmydroid.shared.domain.model.app.AppTheme
import com.crackmydroid.shared.domain.usecase.settings.GetExportFormatUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetFeatureHintsEnabledUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetHighContrastUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetIntroSeenUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetLanguageUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetLargeTextUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetReduceMotionUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetSuggestionsEnabledUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetThemeUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetVerboseLogUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetExportFormatUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetFeatureHintsEnabledUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetHighContrastUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetIntroSeenUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetLanguageUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetLargeTextUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetReduceMotionUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetSuggestionsEnabledUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetThemeUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetVerboseLogUseCase
import com.crackmydroid.shared.presentation.LocalizedBaseViewModel
import com.crackmydroid.shared.presentation.components.SearchHistoryStore
import com.crackmydroid.shared.util.runSuspendCatching

class SettingsViewModel(
    private val getThemeUseCase: GetThemeUseCase,
    private val setThemeUseCase: SetThemeUseCase,
    private val getVerboseLogUseCase: GetVerboseLogUseCase,
    private val setVerboseLogUseCase: SetVerboseLogUseCase,
    private val getExportFormatUseCase: GetExportFormatUseCase,
    private val setExportFormatUseCase: SetExportFormatUseCase,
    private val getHighContrastUseCase: GetHighContrastUseCase,
    private val setHighContrastUseCase: SetHighContrastUseCase,
    private val getReduceMotionUseCase: GetReduceMotionUseCase,
    private val setReduceMotionUseCase: SetReduceMotionUseCase,
    private val getLargeTextUseCase: GetLargeTextUseCase,
    private val setLargeTextUseCase: SetLargeTextUseCase,
    private val getSuggestionsEnabledUseCase: GetSuggestionsEnabledUseCase,
    private val setSuggestionsEnabledUseCase: SetSuggestionsEnabledUseCase,
    private val getFeatureHintsEnabledUseCase: GetFeatureHintsEnabledUseCase,
    private val setFeatureHintsEnabledUseCase: SetFeatureHintsEnabledUseCase,
    private val getIntroSeenUseCase: GetIntroSeenUseCase,
    private val setIntroSeenUseCase: SetIntroSeenUseCase,
    private val getLanguageUseCase: GetLanguageUseCase,
    private val setLanguageUseCase: SetLanguageUseCase,
    dispatchers: AppDispatchers,
) : LocalizedBaseViewModel<SettingsEvent, SettingsState>(SettingsState(), dispatchers) {

    override fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.Load -> load()
            is SettingsEvent.ThemeSelected -> selectTheme(event.theme)
            is SettingsEvent.LanguageSelected -> selectLanguage(event.language)
            is SettingsEvent.VerboseLoggingChanged -> toggleVerbose(event.enabled)
            is SettingsEvent.HighContrastChanged -> setHighContrast(event.enabled)
            is SettingsEvent.ReduceMotionChanged -> setReduceMotion(event.enabled)
            is SettingsEvent.LargeTextChanged -> setLargeText(event.enabled)
            is SettingsEvent.ErrorDismissed -> clearError()
            is SettingsEvent.SuggestionsEnabledChanged -> setSuggestionsEnabled(event.enabled)
            is SettingsEvent.FeatureHintsEnabledChanged -> setFeatureHintsEnabled(event.enabled)
            is SettingsEvent.IntroOnLaunchChanged -> setShowIntroOnLaunch(event.enabled)
            is SettingsEvent.SuggestionsCleared -> clearSuggestions()
            is SettingsEvent.ExportFormatSelected -> setExportFormat(event.format)
        }
    }
    private var savingFormat = false

    private fun load() = launchLatest("settings-load") {
        updateState { it.copy(loading = true, error = null) }
        val language = getLanguageUseCase().onSuccess { value ->
            updateState { it.copy(language = value) }
        }
        val verbose = getVerboseLogUseCase().onSuccess(AppLogger::setVerbose)
        runSuspendCatching {
            LoadedSettings(
                language = language.getOrThrow(),
                theme = getThemeUseCase().getOrThrow(),
                verbose = verbose.getOrThrow(),
                format = getExportFormatUseCase().getOrThrow(),
                highContrast = getHighContrastUseCase().getOrThrow(),
                reduceMotion = getReduceMotionUseCase().getOrThrow(),
                largeText = getLargeTextUseCase().getOrThrow(),
                suggestionsEnabled = getSuggestionsEnabledUseCase().getOrThrow(),
                featureHintsEnabled = getFeatureHintsEnabledUseCase().getOrThrow(),
                introSeen = getIntroSeenUseCase().getOrThrow(),
            )
        }.onSuccess { loaded ->
            updateState {
                it.copy(
                    theme = loaded.theme,
                    language = loaded.language,
                    verbose = loaded.verbose,
                    exportFormat = loaded.format,
                    highContrast = loaded.highContrast,
                    reduceMotion = loaded.reduceMotion,
                    largeText = loaded.largeText,
                    suggestionsEnabled = loaded.suggestionsEnabled,
                    featureHintsEnabled = loaded.featureHintsEnabled,
                    showIntroOnLaunch = !loaded.introSeen,
                    loading = false,
                )
            }
        }.onFailure { err ->
            updateState { it.copy(error = err.message, loading = false) }
        }
    }

    private fun selectTheme(theme: AppTheme) {
        persistSetting(persist = { setThemeUseCase(theme) }) {
            updateState { it.copy(theme = theme, error = null) }
        }
    }

    private fun selectLanguage(language: AppLanguage) {
        persistSetting(persist = { setLanguageUseCase(language) }) {
            updateState { it.copy(language = language, error = null) }
        }
    }

    private fun toggleVerbose(enabled: Boolean) {
        persistSetting(persist = { setVerboseLogUseCase(enabled) }) {
            AppLogger.setVerbose(enabled)
            updateState { it.copy(verbose = enabled, error = null) }
        }
    }

    private fun setHighContrast(enabled: Boolean) {
        persistSetting(
            persist = {
                setHighContrastUseCase(enabled)
            },
        ) {
            updateState { it.copy(highContrast = enabled, error = null) }
        }
    }

    private fun setReduceMotion(enabled: Boolean) {
        persistSetting(
            persist = {
                setReduceMotionUseCase(enabled)
            },
        ) {
            updateState { it.copy(reduceMotion = enabled, error = null) }
        }
    }

    private fun setLargeText(enabled: Boolean) {
        persistSetting(
            persist = {
                setLargeTextUseCase(enabled)
            },
        ) {
            updateState { it.copy(largeText = enabled, error = null) }
        }
    }

    private fun clearError() {
        updateState { it.copy(error = null) }
    }

    private fun setSuggestionsEnabled(enabled: Boolean) {
        persistSetting(
            persist = {
                setSuggestionsEnabledUseCase(enabled).onSuccess {
                    SearchHistoryStore.setEnabled(enabled)
                }
            },
        ) {
            updateState { it.copy(suggestionsEnabled = enabled, error = null) }
        }
    }

    private fun setFeatureHintsEnabled(enabled: Boolean) {
        persistSetting(
            persist = {
                setFeatureHintsEnabledUseCase(enabled)
            },
        ) {
            updateState { it.copy(featureHintsEnabled = enabled, error = null) }
        }
    }

    private fun setShowIntroOnLaunch(enabled: Boolean) {
        persistSetting(
            persist = {
                setIntroSeenUseCase(!enabled)
            },
        ) {
            updateState { it.copy(showIntroOnLaunch = enabled, error = null) }
        }
    }

    private fun clearSuggestions() {
        SearchHistoryStore.clearAll()
    }

    private fun setExportFormat(format: ExportFormat) {
        if (currentState.exportFormat == format) return
        if (savingFormat) return
        savingFormat = true
        launchTask {
            try {
                setExportFormatUseCase(format)
                    .onSuccess { updateState { it.copy(exportFormat = format, error = null) } }
                    .onFailure { err -> updateState { it.copy(error = err.message) } }
            } finally {
                savingFormat = false
            }
        }
    }

    private fun persistSetting(persist: suspend () -> Result<Unit>, updateState: () -> Unit) {
        launchTask {
            runSuspendCatching {
                persist().getOrThrow()
                updateState()
            }.onFailure { error ->
                updateState { it.copy(error = error.message ?: presentationTexts.saveSettingFailed) }
            }
        }
    }
}

private data class LoadedSettings(
    val language: AppLanguage,
    val theme: AppTheme,
    val verbose: Boolean,
    val format: ExportFormat,
    val highContrast: Boolean,
    val reduceMotion: Boolean,
    val largeText: Boolean,
    val suggestionsEnabled: Boolean,
    val featureHintsEnabled: Boolean,
    val introSeen: Boolean,
)
