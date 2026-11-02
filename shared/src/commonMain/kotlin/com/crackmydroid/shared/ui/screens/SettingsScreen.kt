package com.crackmydroid.shared.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Description
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import com.crackmydroid.shared.domain.model.AppTheme
import com.crackmydroid.shared.domain.model.AppLanguage
import com.crackmydroid.shared.i18n.LocalStrings
import com.crackmydroid.shared.ui.settings.SettingsViewModel
import com.crackmydroid.shared.ui.settings.SettingsActions
import com.crackmydroid.shared.ui.settings.SettingsAppInfo
import com.crackmydroid.shared.ui.settings.SettingsState
import com.crackmydroid.shared.presentation.components.CardBlock
import com.crackmydroid.shared.presentation.components.AppButton
import com.crackmydroid.shared.presentation.components.AppText
import com.crackmydroid.shared.presentation.components.SectionHeader
import com.crackmydroid.shared.presentation.components.ButtonVariant
import com.crackmydroid.shared.presentation.components.ErrorDialog
import com.crackmydroid.shared.presentation.components.ScrollToTopButton
import com.crackmydroid.shared.util.appVersionName
import com.crackmydroid.shared.util.appPackageName
import com.crackmydroid.shared.util.appRequestedPermissions
import com.crackmydroid.shared.util.appSdkInfo
import kotlinx.coroutines.launch
import com.crackmydroid.shared.presentation.components.LocalAccessibilityPrefs

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    padding: androidx.compose.foundation.layout.PaddingValues,
    headerHelp: String,
    onHelp: (String) -> Unit,
    onMenu: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val uriHandler = LocalUriHandler.current
    val sdkInfo = appSdkInfo()
    SettingsContent(
        state = state,
        padding = padding,
        headerHelp = headerHelp,
        appInfo = SettingsAppInfo(
            versionName = appVersionName(),
            packageName = appPackageName(),
            minSdk = sdkInfo?.minSdk,
            targetSdk = sdkInfo?.targetSdk,
            requestedPermissions = appRequestedPermissions()
        ),
        actions = SettingsActions(
            onSelectTheme = viewModel::selectTheme,
            onSelectLanguage = viewModel::selectLanguage,
            onSetSuggestionsEnabled = viewModel::setSuggestionsEnabled,
            onClearSuggestions = viewModel::clearSuggestions,
            onSetFeatureHintsEnabled = viewModel::setFeatureHintsEnabled,
            onSetShowIntroOnLaunch = viewModel::setShowIntroOnLaunch,
            onToggleVerbose = viewModel::toggleVerbose,
            onSetExportFormat = viewModel::setExportFormat,
            onSetHighContrast = viewModel::setHighContrast,
            onSetReduceMotion = viewModel::setReduceMotion,
            onSetLargeText = viewModel::setLargeText,
            onOpenUri = uriHandler::openUri,
            onHelp = onHelp,
            onMenu = onMenu,
            onClearError = viewModel::clearError
        )
    )
}

@Composable
internal fun SettingsContent(
    state: SettingsState,
    padding: androidx.compose.foundation.layout.PaddingValues,
    headerHelp: String,
    appInfo: SettingsAppInfo,
    actions: SettingsActions
) {
    val strings = LocalStrings.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val showScrollTop by remember {
        derivedStateOf { scrollState.value > 260 }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.TopCenter)
                .widthIn(max = 840.dp)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionHeader(title = strings.navSettings, help = headerHelp, onHelp = actions.onHelp, onMenu = actions.onMenu)
            CardBlock(strings.themeTitle, leadingIcon = Icons.Filled.ColorLens) {
                ThemeRow(strings.light, AppTheme.LIGHT, state.theme) { actions.onSelectTheme(AppTheme.LIGHT) }
                ThemeRow(strings.dark, AppTheme.DARK, state.theme) { actions.onSelectTheme(AppTheme.DARK) }
                ThemeRow(strings.system, AppTheme.SYSTEM, state.theme) { actions.onSelectTheme(AppTheme.SYSTEM) }
            }

            CardBlock(strings.languageTitle, leadingIcon = Icons.Filled.Settings) {
                AppText(strings.languageDescription, style = MaterialTheme.typography.bodySmall)
                AppLanguage.entries.forEach { language ->
                    AppButton(
                        text = if (state.language == language) "✔︎ ${language.displayName}" else language.displayName,
                        onClick = { actions.onSelectLanguage(language) },
                        modifier = Modifier.padding(vertical = 4.dp),
                        variant = ButtonVariant.Tonal
                    )
                }
            }

            CardBlock("Suggerimenti ricerca", leadingIcon = Icons.Filled.History) {
                SwitchRow(
                    label = "Abilita suggerimenti",
                    checked = state.suggestionsEnabled,
                    onCheckedChange = actions.onSetSuggestionsEnabled
                )
                AppText(
                    "Memorizza le ultime ricerche e riproponile come chip rapidi nelle aree di cerca.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AppButton(
                    text = "Cancella suggerimenti",
                    onClick = actions.onClearSuggestions,
                    variant = ButtonVariant.Tonal,
                    modifier = Modifier.padding(top = 8.dp),
                    leadingIcon = Icons.Filled.History
                )
            }

            CardBlock("Dialog di ingresso sezioni", leadingIcon = Icons.Filled.Settings) {
                SwitchRow(
                    label = "Abilita dialog di suggerimento",
                    checked = state.featureHintsEnabled,
                    onCheckedChange = actions.onSetFeatureHintsEnabled
                )
                AppText(
                    "Mostra la dialog di ingresso nelle sezioni tecniche. Da ogni dialog puoi anche selezionare \"Non visualizzare più\" per disattivarla.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            CardBlock("Onboarding", leadingIcon = Icons.Filled.Info) {
                SwitchRow(
                    label = "Mostra introduzione all'avvio",
                    checked = state.showIntroOnLaunch,
                    onCheckedChange = actions.onSetShowIntroOnLaunch
                )
                AppText(
                    "Se attivo, la schermata introduttiva viene mostrata all'apertura dell'app.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            CardBlock(strings.verboseTitle, leadingIcon = Icons.Filled.BugReport) {
                SwitchRow(
                    label = strings.verboseTitle,
                    checked = state.verbose,
                    onCheckedChange = actions.onToggleVerbose
                )
                AppText(strings.verboseSubtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            CardBlock("Formato esportazione", leadingIcon = Icons.Filled.Description) {
                ExportFormatRow(
                    label = "Testo (.txt)",
                    selected = state.exportFormat == com.crackmydroid.shared.domain.model.ExportFormat.TXT
                ) { actions.onSetExportFormat(com.crackmydroid.shared.domain.model.ExportFormat.TXT) }
                ExportFormatRow(
                    label = "JSON formattato",
                    selected = state.exportFormat == com.crackmydroid.shared.domain.model.ExportFormat.JSON
                ) { actions.onSetExportFormat(com.crackmydroid.shared.domain.model.ExportFormat.JSON) }
                AppText(
                    "Si applica a export e condividi (liste activity / app).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            CardBlock(strings.accessibilityTitle, leadingIcon = Icons.Filled.Accessibility) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    AppText(strings.accessibilityDesc, style = MaterialTheme.typography.bodySmall)
                    SwitchRow(label = strings.accessibilityContrast, checked = state.highContrast, onCheckedChange = actions.onSetHighContrast)
                    SwitchRow(label = strings.accessibilityReduceMotion, checked = state.reduceMotion, onCheckedChange = actions.onSetReduceMotion)
                    SwitchRow(label = strings.accessibilityLargeText, checked = state.largeText, onCheckedChange = actions.onSetLargeText)
                }
            }

            CardBlock(strings.thirdPartyTitle, leadingIcon = Icons.Filled.Link) {
                AppText(strings.thirdPartyIntro, style = MaterialTheme.typography.bodySmall)
                val libs = listOf(
                    "Jetpack Compose Multiplatform" to "https://github.com/JetBrains/compose-jb",
                    "Koin" to "https://insert-koin.io/",
                    "Kermit" to "https://github.com/touchlab/Kermit",
                    "RootBeer" to "https://github.com/scottyab/rootbeer",
                    "Play Integrity API" to "https://developer.android.com/google/play/integrity",
                    "Kotlin Coroutines" to "https://github.com/Kotlin/kotlinx.coroutines",
                    "DataStore" to "https://developer.android.com/topic/libraries/architecture/datastore",
                    "MockK" to "https://mockk.io/"
                )
                libs.forEach { (name, url) ->
                    AppText(
                        "• $name",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(vertical = 2.dp)
                            .clickable { actions.onOpenUri(url) }
                    )
                }
            }

            DisclaimerBox()

            CardBlock("Info app", leadingIcon = Icons.Filled.Info) {
                AppText("Versione: ${appInfo.versionName}", style = MaterialTheme.typography.titleMedium)
                AppText("Package: ${appInfo.packageName}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (appInfo.minSdk != null || appInfo.targetSdk != null) {
                    AppText(
                        "SDK min/target: ${appInfo.minSdk ?: "?"} / ${appInfo.targetSdk ?: "?"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                val perms = appInfo.requestedPermissions
                if (perms.isNotEmpty()) {
                    AppText("Permessi richiesti:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    perms.forEach { perm ->
                        AppText("• $perm", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    AppText("Permessi richiesti: nessuno", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }


            state.error?.let { err ->
                AppText(err, color = MaterialTheme.colorScheme.error)
            }
        }

        ScrollToTopButton(
            visible = showScrollTop,
            onClick = { scope.launch { scrollState.animateScrollTo(0) } },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
        )
    }

    state.error?.let { ErrorDialog(it, actions.onClearError) }
}

@Composable
private fun ThemeRow(label: String, theme: AppTheme, current: AppTheme, onClick: () -> Unit) {
    AppButton(text = if (current == theme) "✔︎ $label" else label, onClick = onClick, modifier = Modifier.padding(vertical = 4.dp), variant = ButtonVariant.Tonal)
}

@Composable
private fun ExportFormatRow(label: String, selected: Boolean, onClick: () -> Unit) {
    AppButton(
        text = if (selected) "✔︎ $label" else label,
        onClick = onClick,
        modifier = Modifier.padding(vertical = 4.dp),
        variant = ButtonVariant.Tonal
    )
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    subtitle: String? = null
) {
    val accessibilityPrefs = LocalAccessibilityPrefs.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = if (accessibilityPrefs.largeText) Alignment.Top else Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            AppText(label, style = MaterialTheme.typography.bodyLarge)
            subtitle?.let {
                AppText(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun DisclaimerBox() {
    val strings = LocalStrings.current
    CardBlock(strings.disclaimerTitle, leadingIcon = Icons.Filled.Warning) {
        AppText(strings.disclaimerBody, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
    }
}
