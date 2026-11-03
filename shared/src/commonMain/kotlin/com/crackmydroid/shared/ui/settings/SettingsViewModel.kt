package com.crackmydroid.shared.ui.settings

import com.crackmydroid.shared.domain.model.AppLanguage
import com.crackmydroid.shared.domain.model.AppTheme
import com.crackmydroid.shared.domain.model.ExportFormat
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
    private val setLanguage: SetLanguageUseCase
) : BaseViewModel() {
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state
    private var savingFormat = false

    fun load() {
        scope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching {
                LoadedSettings(
                    language = getLanguage(),
                    theme = getTheme(),
                    verbose = getVerbose(),
                    format = getExportFormat(),
                    highContrast = getHighContrast(),
                    reduceMotion = getReduceMotion(),
                    largeText = getLargeText(),
                    suggestionsEnabled = getSuggestionsEnabled(),
                    featureHintsEnabled = getFeatureHintsEnabled(),
                    introSeen = getIntroSeen()
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
                        loading = false
                    )
                }
            }.onFailure { err ->
                _state.update { it.copy(error = err.message, loading = false) }
            }
        }
    }

    fun selectTheme(theme: AppTheme) {
        scope.launch {
            setTheme(theme)
            _state.update { it.copy(theme = theme) }
        }
    }

    fun selectLanguage(language: AppLanguage) {
        scope.launch {
            runCatching { setLanguage(language) }
                .onSuccess { _state.update { it.copy(language = language, error = null) } }
                .onFailure { err -> _state.update { it.copy(error = err.message) } }
        }
    }

    fun toggleVerbose(enabled: Boolean) {
        scope.launch {
            setVerbose(enabled)
            _state.update { it.copy(verbose = enabled) }
        }
    }

    fun setHighContrast(enabled: Boolean) {
        scope.launch {
            setHighContrast(enabled)
            _state.update { it.copy(highContrast = enabled) }
        }
    }

    fun setReduceMotion(enabled: Boolean) {
        scope.launch {
            setReduceMotion(enabled)
            _state.update { it.copy(reduceMotion = enabled) }
        }
    }

    fun setLargeText(enabled: Boolean) {
        scope.launch {
            setLargeText(enabled)
            _state.update { it.copy(largeText = enabled) }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun setSuggestionsEnabled(enabled: Boolean) {
        scope.launch {
            setSuggestionsEnabled(enabled)
            _state.update { it.copy(suggestionsEnabled = enabled) }
            com.crackmydroid.shared.presentation.components.SearchHistoryStore.setEnabled(enabled)
        }
    }

    fun setFeatureHintsEnabled(enabled: Boolean) {
        scope.launch {
            setFeatureHintsEnabled(enabled)
            _state.update { it.copy(featureHintsEnabled = enabled) }
        }
    }

    fun setShowIntroOnLaunch(enabled: Boolean) {
        scope.launch {
            setIntroSeen(!enabled)
            _state.update { it.copy(showIntroOnLaunch = enabled) }
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
            runCatching { setExportFormat(format) }
                .onSuccess { _state.update { it.copy(exportFormat = format, error = null) } }
                .onFailure { err -> _state.update { it.copy(error = err.message) } }
            savingFormat = false
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
    val introSeen: Boolean
)
