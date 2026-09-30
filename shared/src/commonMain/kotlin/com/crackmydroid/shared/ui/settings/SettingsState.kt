package com.crackmydroid.shared.ui.settings

import com.crackmydroid.shared.domain.model.AppTheme
import com.crackmydroid.shared.domain.model.ExportFormat
import com.crackmydroid.shared.domain.model.AppLanguage

data class SettingsState(
    val language: AppLanguage = AppLanguage.ENGLISH,
    val theme: AppTheme = AppTheme.SYSTEM,
    val verbose: Boolean = false,
    val highContrast: Boolean = false,
    val reduceMotion: Boolean = false,
    val largeText: Boolean = false,
    val suggestionsEnabled: Boolean = true,
    val featureHintsEnabled: Boolean = true,
    val showIntroOnLaunch: Boolean = true,
    val exportFormat: ExportFormat = ExportFormat.TXT,
    val loading: Boolean = false,
    val error: String? = null
)
