package com.crackmydroid.shared.ui.settings

import com.crackmydroid.shared.AppLogger
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
import com.crackmydroid.shared.presentation.BaseViewModel
import com.crackmydroid.shared.util.runSuspendCatching
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val getTheme: GetThemeUseCase,
    private val setTheme: SetThemeUseCase,
    private val getVerbose: GetVerboseLogUseCase,
    private val setVerbose: SetVerboseLogUseCase,
    private val getExportFormat: GetExportFormatUseCase,
    private val setExportFormat: SetExportFormatUseCase,
    private val getHighContrast: GetHighContrastUseCase,
    private val setHighContrast: SetHighContrastUseCase,
    private val getReduceMotion: GetReduceMotionUseCase,
    private val setReduceMotion: SetReduceMotionUseCase,
    private val getLargeText: GetLargeTextUseCase,
    private val setLargeText: SetLargeTextUseCase,
    private val getSuggestionsEnabled: GetSuggestionsEnabledUseCase,
    private val setSuggestionsEnabled: SetSuggestionsEnabledUseCase,
    private val getFeatureHintsEnabled: GetFeatureHintsEnabledUseCase,
    private val setFeatureHintsEnabled: SetFeatureHintsEnabledUseCase,
    private val getIntroSeen: GetIntroSeenUseCase,
    private val setIntroSeen: SetIntroSeenUseCase,
    private val getLanguage: GetLanguageUseCase,
    private val setLanguage: SetLanguageUseCase,
) : BaseViewModel() {
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state
    private var savingFormat = false

    fun load() = scope.launch {
        _state.update { it.copy(loading = true, error = null) }
        val language = getLanguage().onSuccess { value ->
            _state.update { it.copy(language = value) }
        }
        val verbose = getVerbose().onSuccess(AppLogger::setVerbose)
        runSuspendCatching {
            LoadedSettings(
                language = language.getOrThrow(),
                theme = getTheme().getOrThrow(),
                verbose = verbose.getOrThrow(),
                format = getExportFormat().getOrThrow(),
                highContrast = getHighContrast().getOrThrow(),
                reduceMotion = getReduceMotion().getOrThrow(),
                largeText = getLargeText().getOrThrow(),
                suggestionsEnabled = getSuggestionsEnabled().getOrThrow(),
                featureHintsEnabled = getFeatureHintsEnabled().getOrThrow(),
                introSeen = getIntroSeen().getOrThrow(),
            )
        }.onSuccess { loaded ->
            _state.update {
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
            _state.update { it.copy(error = err.message, loading = false) }
        }
    }

    fun selectTheme(theme: AppTheme) {
        persistSetting(persist = { setTheme(theme) }) {
            _state.update { it.copy(theme = theme, error = null) }
        }
    }

    fun selectLanguage(language: AppLanguage) {
        persistSetting(persist = { setLanguage(language) }) {
            _state.update { it.copy(language = language, error = null) }
        }
    }

    fun toggleVerbose(enabled: Boolean) {
        persistSetting(persist = { setVerbose(enabled) }) {
            AppLogger.setVerbose(enabled)
            _state.update { it.copy(verbose = enabled, error = null) }
        }
    }

    fun setHighContrast(enabled: Boolean) {
        persistSetting(
            persist = {
                setHighContrast.invoke(enabled)
            },
        ) {
            _state.update { it.copy(highContrast = enabled, error = null) }
        }
    }

    fun setReduceMotion(enabled: Boolean) {
        persistSetting(
            persist = {
                setReduceMotion.invoke(enabled)
            },
        ) {
            _state.update { it.copy(reduceMotion = enabled, error = null) }
        }
    }

    fun setLargeText(enabled: Boolean) {
        persistSetting(
            persist = {
                setLargeText.invoke(enabled)
            },
        ) {
            _state.update { it.copy(largeText = enabled, error = null) }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun setSuggestionsEnabled(enabled: Boolean) {
        persistSetting(
            persist = {
                setSuggestionsEnabled.invoke(enabled).onSuccess {
                    com.crackmydroid.shared.presentation.components.SearchHistoryStore.setEnabled(enabled)
                }
            },
        ) {
            _state.update { it.copy(suggestionsEnabled = enabled, error = null) }
        }
    }

    fun setFeatureHintsEnabled(enabled: Boolean) {
        persistSetting(
            persist = {
                setFeatureHintsEnabled.invoke(enabled)
            },
        ) {
            _state.update { it.copy(featureHintsEnabled = enabled, error = null) }
        }
    }

    fun setShowIntroOnLaunch(enabled: Boolean) {
        persistSetting(
            persist = {
                setIntroSeen(!enabled)
            },
        ) {
            _state.update { it.copy(showIntroOnLaunch = enabled, error = null) }
        }
    }

    fun clearSuggestions() {
        com.crackmydroid.shared.presentation.components.SearchHistoryStore.clearAll()
    }

    fun setExportFormat(format: ExportFormat) {
        if (_state.value.exportFormat == format) return
        if (savingFormat) return
        savingFormat = true
        scope.launch {
            try {
                setExportFormat.invoke(format)
                    .onSuccess { _state.update { it.copy(exportFormat = format, error = null) } }
                    .onFailure { err -> _state.update { it.copy(error = err.message) } }
            } finally {
                savingFormat = false
            }
        }
    }

    private fun persistSetting(persist: suspend () -> Result<Unit>, updateState: () -> Unit) {
        scope.launch {
            runSuspendCatching {
                persist().getOrThrow()
                updateState()
            }.onFailure { error ->
                _state.update { it.copy(error = error.message ?: "Impossibile salvare l'impostazione") }
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
