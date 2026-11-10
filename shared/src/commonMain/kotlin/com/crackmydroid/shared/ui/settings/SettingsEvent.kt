package com.crackmydroid.shared.ui.settings

import com.crackmydroid.architecture.presentation.model.UiEvent
import com.crackmydroid.shared.domain.model.ExportFormat
import com.crackmydroid.shared.domain.model.app.AppLanguage
import com.crackmydroid.shared.domain.model.app.AppTheme

sealed class SettingsEvent : UiEvent() {
    class Load : SettingsEvent()
    class ThemeSelected(val theme: AppTheme) : SettingsEvent()
    class LanguageSelected(val language: AppLanguage) : SettingsEvent()
    class VerboseLoggingChanged(val enabled: Boolean) : SettingsEvent()
    class HighContrastChanged(val enabled: Boolean) : SettingsEvent()
    class ReduceMotionChanged(val enabled: Boolean) : SettingsEvent()
    class LargeTextChanged(val enabled: Boolean) : SettingsEvent()
    class ErrorDismissed : SettingsEvent()
    class SuggestionsEnabledChanged(val enabled: Boolean) : SettingsEvent()
    class FeatureHintsEnabledChanged(val enabled: Boolean) : SettingsEvent()
    class IntroOnLaunchChanged(val enabled: Boolean) : SettingsEvent()
    class SuggestionsCleared : SettingsEvent()
    class ExportFormatSelected(val format: ExportFormat) : SettingsEvent()
}
