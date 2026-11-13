package com.crackmydroid.shared.ui.settings.view
import com.crackmydroid.ui.icons.AppIcons

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crackmydroid.shared.domain.model.ExportFormat
import com.crackmydroid.shared.domain.model.app.AppLanguage
import com.crackmydroid.shared.domain.model.app.AppTheme
import com.crackmydroid.shared.i18n.LocalStrings
import com.crackmydroid.shared.i18n.ScreenUiStrings
import com.crackmydroid.shared.i18n.format
import com.crackmydroid.shared.ui.settings.SettingsEvent
import com.crackmydroid.shared.ui.settings.SettingsState
import com.crackmydroid.shared.ui.settings.SettingsViewModel
import com.crackmydroid.shared.ui.settings.model.SettingsActions
import com.crackmydroid.shared.ui.settings.model.SettingsAppInfo
import com.crackmydroid.ui.components.button.AppButton
import com.crackmydroid.ui.components.button.ButtonVariant
import com.crackmydroid.ui.components.dialog.ErrorDialogComponent
import com.crackmydroid.ui.components.LocalAccessibilityPrefs
import com.crackmydroid.ui.components.button.ScrollToTopButton
import com.crackmydroid.ui.components.card.CardBlock
import com.crackmydroid.ui.components.text.AppText
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    padding: PaddingValues,
    appInfo: SettingsAppInfo,
) {
    val state by viewModel.state.collectAsState()
    val uriHandler = LocalUriHandler.current
    SettingsContent(
        state = state,
        padding = padding,
        appInfo = appInfo,
        actions = SettingsActions(
            onSelectTheme = { viewModel.emitEvent(SettingsEvent.ThemeSelected(it)) },
            onSelectLanguage = { viewModel.emitEvent(SettingsEvent.LanguageSelected(it)) },
            onSetSuggestionsEnabled = { viewModel.emitEvent(SettingsEvent.SuggestionsEnabledChanged(it)) },
            onClearSuggestions = { viewModel.emitEvent(SettingsEvent.SuggestionsCleared()) },
            onSetFeatureHintsEnabled = { viewModel.emitEvent(SettingsEvent.FeatureHintsEnabledChanged(it)) },
            onSetShowIntroOnLaunch = { viewModel.emitEvent(SettingsEvent.IntroOnLaunchChanged(it)) },
            onToggleVerbose = { viewModel.emitEvent(SettingsEvent.VerboseLoggingChanged(it)) },
            onSetExportFormat = { viewModel.emitEvent(SettingsEvent.ExportFormatSelected(it)) },
            onSetHighContrast = { viewModel.emitEvent(SettingsEvent.HighContrastChanged(it)) },
            onSetReduceMotion = { viewModel.emitEvent(SettingsEvent.ReduceMotionChanged(it)) },
            onSetLargeText = { viewModel.emitEvent(SettingsEvent.LargeTextChanged(it)) },
            onOpenUri = uriHandler::openUri,
            onClearError = { viewModel.emitEvent(SettingsEvent.ErrorDismissed()) },
        ),
    )
}

@Composable
internal fun SettingsContent(
    state: SettingsState,
    padding: PaddingValues,
    appInfo: SettingsAppInfo,
    actions: SettingsActions,
) {
    val strings = LocalStrings.current
    val screenStrings = ScreenUiStrings.from(strings)
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val showScrollTop by remember {
        derivedStateOf { scrollState.value > 260 }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.TopCenter)
                .widthIn(max = 840.dp)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CardBlock(strings.themeTitle, leadingIcon = AppIcons.Filled.ColorLens) {
                ThemeRow(strings.light, AppTheme.LIGHT, state.theme) {
                    actions.onSelectTheme(
                        AppTheme.LIGHT,
                    )
                }
                ThemeRow(
                    strings.dark,
                    AppTheme.DARK,
                    state.theme,
                ) { actions.onSelectTheme(AppTheme.DARK) }
                ThemeRow(strings.system, AppTheme.SYSTEM, state.theme) {
                    actions.onSelectTheme(
                        AppTheme.SYSTEM,
                    )
                }
            }

            CardBlock(strings.languageTitle, leadingIcon = AppIcons.Filled.Settings) {
                AppText(strings.languageDescription, style = MaterialTheme.typography.bodySmall)
                AppLanguage.entries.forEach { language ->
                    val label = when (language) {
                        AppLanguage.SYSTEM -> strings.system
                        AppLanguage.ENGLISH -> strings.languageEnglish
                        AppLanguage.ITALIAN -> strings.languageItalian
                        AppLanguage.GERMAN -> strings.languageGerman
                        AppLanguage.FRENCH -> strings.languageFrench
                        AppLanguage.SPANISH -> strings.languageSpanish
                    }
                    AppButton(
                        text = if (state.language == language) screenStrings.selected.format(label) else label,
                        onClick = { actions.onSelectLanguage(language) },
                        modifier = Modifier.padding(vertical = 4.dp),
                        variant = if (state.language == language) ButtonVariant.Primary else ButtonVariant.Secondary,
                        fullWidth = false,
                    )
                }
            }

            CardBlock(strings.settingsSearchSuggestionsTitle, leadingIcon = AppIcons.Filled.History) {
                SwitchRow(
                    label = strings.settingsSearchSuggestionsEnabled,
                    checked = state.suggestionsEnabled,
                    onCheckedChange = actions.onSetSuggestionsEnabled,
                )
                AppText(
                    strings.settingsSearchSuggestionsDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AppButton(
                    text = strings.settingsClearSuggestions,
                    onClick = actions.onClearSuggestions,
                    variant = ButtonVariant.Tonal,
                    modifier = Modifier.padding(top = 8.dp),
                    leadingIcon = AppIcons.Filled.History,
                )
            }

            CardBlock(strings.settingsSectionHintsTitle, leadingIcon = AppIcons.Filled.Settings) {
                SwitchRow(
                    label = strings.settingsSectionHintsEnabled,
                    checked = state.featureHintsEnabled,
                    onCheckedChange = actions.onSetFeatureHintsEnabled,
                )
                AppText(
                    strings.settingsSectionHintsDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            CardBlock(strings.settingsOnboardingTitle, leadingIcon = AppIcons.Filled.Info) {
                SwitchRow(
                    label = strings.settingsShowIntroOnLaunch,
                    checked = state.showIntroOnLaunch,
                    onCheckedChange = actions.onSetShowIntroOnLaunch,
                )
                AppText(
                    strings.settingsOnboardingDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            CardBlock(strings.verboseTitle, leadingIcon = AppIcons.Filled.BugReport) {
                SwitchRow(
                    label = strings.verboseTitle,
                    checked = state.verbose,
                    onCheckedChange = actions.onToggleVerbose,
                )
                AppText(
                    strings.verboseSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            CardBlock(strings.settingsExportFormatTitle, leadingIcon = AppIcons.Filled.Description) {
                ExportFormatRow(
                    label = strings.settingsExportText,
                    selected = state.exportFormat == ExportFormat.TXT,
                ) { actions.onSetExportFormat(ExportFormat.TXT) }
                ExportFormatRow(
                    label = strings.settingsExportJson,
                    selected = state.exportFormat == ExportFormat.JSON,
                ) { actions.onSetExportFormat(ExportFormat.JSON) }
                ExportFormatRow(
                    label = strings.settingsExportPdf,
                    selected = state.exportFormat == ExportFormat.PDF,
                ) { actions.onSetExportFormat(ExportFormat.PDF) }
                AppText(
                    strings.settingsExportDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }

            CardBlock(strings.accessibilityTitle, leadingIcon = AppIcons.Filled.Accessibility) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    AppText(strings.accessibilityDesc, style = MaterialTheme.typography.bodySmall)
                    SwitchRow(
                        label = strings.accessibilityContrast,
                        checked = state.highContrast,
                        onCheckedChange = actions.onSetHighContrast,
                    )
                    SwitchRow(
                        label = strings.accessibilityReduceMotion,
                        checked = state.reduceMotion,
                        onCheckedChange = actions.onSetReduceMotion,
                    )
                    SwitchRow(
                        label = strings.accessibilityLargeText,
                        checked = state.largeText,
                        onCheckedChange = actions.onSetLargeText,
                    )
                }
            }

            CardBlock(strings.thirdPartyTitle, leadingIcon = AppIcons.Filled.Link) {
                AppText(strings.thirdPartyIntro, style = MaterialTheme.typography.bodySmall)
                state.thirdPartyLibraries.forEach { library ->
                    AppText(
                        screenStrings.bullet.format(library.name),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(vertical = 2.dp)
                            .clickable { actions.onOpenUri(library.url) },
                    )
                }
            }

            DisclaimerBox()

            CardBlock(strings.settingsAppInfoTitle, leadingIcon = AppIcons.Filled.Info) {
                AppText(
                    "${strings.settingsAppVersion}: ${appInfo.versionName}",
                    style = MaterialTheme.typography.titleMedium,
                )
                AppText(
                    "${strings.settingsAppPackage}: ${appInfo.packageName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (appInfo.minSdk != null || appInfo.targetSdk != null) {
                    AppText(
                        "${strings.settingsAppSdk}: ${appInfo.minSdk ?: screenStrings.missingValue} / ${appInfo.targetSdk ?: screenStrings.missingValue}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                val perms = appInfo.requestedPermissions
                if (perms.isNotEmpty()) {
                    AppText(
                        "${strings.settingsRequestedPermissions}:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    perms.forEach { perm ->
                        AppText(
                            screenStrings.bullet.format(perm),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    AppText(
                        strings.settingsNoRequestedPermissions,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            (state.loadError ?: state.error)?.let { err ->
                AppText(err, color = MaterialTheme.colorScheme.error)
            }
        }

        ScrollToTopButton(
            visible = showScrollTop,
            onClick = { scope.launch { scrollState.animateScrollTo(0) } },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
        )
    }

    (state.loadError ?: state.error)?.let { ErrorDialogComponent(it, actions.onClearError) }
}

@Composable
private fun ThemeRow(label: String, theme: AppTheme, current: AppTheme, onClick: () -> Unit) {
    val screenStrings = ScreenUiStrings.from(LocalStrings.current)
    AppButton(
        text = if (current == theme) screenStrings.selected.format(label) else label,
        onClick = onClick,
        modifier = Modifier.padding(vertical = 4.dp),
        variant = ButtonVariant.Tonal,
    )
}

@Composable
private fun ExportFormatRow(label: String, selected: Boolean, onClick: () -> Unit) {
    val screenStrings = ScreenUiStrings.from(LocalStrings.current)
    AppButton(
        text = if (selected) screenStrings.selected.format(label) else label,
        onClick = onClick,
        modifier = Modifier.padding(vertical = 4.dp),
        variant = ButtonVariant.Tonal,
    )
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, subtitle: String? = null) {
    val accessibilityPrefs = LocalAccessibilityPrefs.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = if (accessibilityPrefs.largeText) Alignment.Top else Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            AppText(label, style = MaterialTheme.typography.bodyLarge)
            subtitle?.let {
                AppText(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun DisclaimerBox() {
    val strings = LocalStrings.current
    CardBlock(strings.disclaimerTitle, leadingIcon = AppIcons.Filled.Warning) {
        AppText(
            strings.disclaimerBody,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
