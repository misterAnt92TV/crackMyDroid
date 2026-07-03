package com.crackmydroid.shared.ui.settings

import com.crackmydroid.shared.domain.model.AppTheme
import com.crackmydroid.shared.domain.usecase.GetThemeUseCase
import com.crackmydroid.shared.domain.usecase.GetVerboseLogUseCase
import com.crackmydroid.shared.domain.usecase.SetThemeUseCase
import com.crackmydroid.shared.domain.usecase.SetVerboseLogUseCase
import com.crackmydroid.shared.domain.usecase.GetExportFormatUseCase
import com.crackmydroid.shared.domain.usecase.SetExportFormatUseCase
import com.crackmydroid.shared.domain.usecase.GetHighContrastUseCase
import com.crackmydroid.shared.domain.usecase.SetHighContrastUseCase
import com.crackmydroid.shared.domain.usecase.GetReduceMotionUseCase
import com.crackmydroid.shared.domain.usecase.SetReduceMotionUseCase
import com.crackmydroid.shared.domain.usecase.GetLargeTextUseCase
import com.crackmydroid.shared.domain.usecase.SetLargeTextUseCase
import com.crackmydroid.shared.domain.usecase.GetSuggestionsEnabledUseCase
import com.crackmydroid.shared.domain.usecase.SetSuggestionsEnabledUseCase
import com.crackmydroid.shared.domain.usecase.GetFeatureHintsEnabledUseCase
import com.crackmydroid.shared.domain.usecase.SetFeatureHintsEnabledUseCase
import com.crackmydroid.shared.domain.usecase.GetIntroSeenUseCase
import com.crackmydroid.shared.domain.usecase.SetIntroSeenUseCase
import com.crackmydroid.shared.presentation.BaseViewModel
import com.crackmydroid.shared.domain.model.ExportFormat
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
    private val setIntroSeen: SetIntroSeenUseCase
) : BaseViewModel() {
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state
    private var savingFormat = false

    fun load() {
        scope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching {
                LoadedSettings(
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
