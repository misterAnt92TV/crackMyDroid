package com.crackmydroid.shared.ui.settings.model

import com.crackmydroid.shared.domain.model.ExportFormat
import com.crackmydroid.shared.domain.model.app.AppLanguage
import com.crackmydroid.shared.domain.model.app.AppTheme

data class SettingsActions(
    val onSelectTheme: (AppTheme) -> Unit = {},
    val onSelectLanguage: (AppLanguage) -> Unit = {},
    val onSetSuggestionsEnabled: (Boolean) -> Unit = {},
    val onClearSuggestions: () -> Unit = {},
    val onSetFeatureHintsEnabled: (Boolean) -> Unit = {},
    val onSetShowIntroOnLaunch: (Boolean) -> Unit = {},
    val onToggleVerbose: (Boolean) -> Unit = {},
    val onSetExportFormat: (ExportFormat) -> Unit = {},
    val onSetHighContrast: (Boolean) -> Unit = {},
    val onSetReduceMotion: (Boolean) -> Unit = {},
    val onSetLargeText: (Boolean) -> Unit = {},
    val onOpenUri: (String) -> Unit = {},
    val onHelp: (String) -> Unit = {},
    val onMenu: () -> Unit = {},
    val onClearError: () -> Unit = {},
)
