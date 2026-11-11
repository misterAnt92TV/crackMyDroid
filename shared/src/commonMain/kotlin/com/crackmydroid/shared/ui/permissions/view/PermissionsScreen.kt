@file:OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class,
)

package com.crackmydroid.shared.ui.permissions.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.crackmydroid.shared.domain.model.PermissionRiskLevel
import com.crackmydroid.shared.domain.model.app.AppPermissionEntry
import com.crackmydroid.shared.i18n.LocalStrings
import com.crackmydroid.shared.i18n.ScreenUiStrings
import com.crackmydroid.shared.i18n.format
import com.crackmydroid.shared.presentation.components.AppAccordion
import com.crackmydroid.shared.presentation.components.AppButton
import com.crackmydroid.shared.presentation.components.ButtonVariant
import com.crackmydroid.shared.presentation.components.ErrorDialogComponent
import com.crackmydroid.shared.presentation.components.PackageIcon
import com.crackmydroid.shared.presentation.components.ScrollToTopButton
import com.crackmydroid.shared.presentation.components.SearchFieldWithHistoryComponent
import com.crackmydroid.shared.presentation.components.SectionHeader
import com.crackmydroid.shared.presentation.components.card.AppCard
import com.crackmydroid.shared.presentation.components.card.ExportDetailsCard
import com.crackmydroid.shared.presentation.components.text.AppText
import com.crackmydroid.shared.presentation.preview.AppPreviewEnvironment
import com.crackmydroid.shared.presentation.preview.AppScreenPreviews
import com.crackmydroid.shared.ui.permissions.PermissionFindingUi
import com.crackmydroid.shared.ui.permissions.PermissionsEffect
import com.crackmydroid.shared.ui.permissions.PermissionsEvent
import com.crackmydroid.shared.ui.permissions.PermissionsState
import com.crackmydroid.shared.ui.permissions.PermissionsViewModel
import com.crackmydroid.shared.ui.screens.screenContentPadding
import kotlinx.coroutines.launch

@Composable
fun PermissionsScreen(
    viewModel: PermissionsViewModel,
    padding: PaddingValues,
    headerHelp: String,
    onHelp: (String) -> Unit,
    onBack: (() -> Unit)? = null,
    onMenu: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()

    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current
    DisposableEffect(viewModel) {
        onDispose { viewModel.handleEvent(PermissionsEvent.ReportPreviewDismissed()) }
    }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            if (effect is PermissionsEffect.CopyToClipboard) clipboard.setText(AnnotatedString(effect.content))
        }
    }
    LaunchedEffect(state.reportPreviewPackage, state.reportPreviewContent) {
        val packageName = state.reportPreviewPackage
        val content = state.reportPreviewContent
        if (packageName != null && content != null) {
            viewModel.handleEvent(PermissionsEvent.ReportShared(packageName))
        }
    }
    PermissionsScreenContent(
        state = state,
        padding = padding,
        headerHelp = headerHelp,
        onHelp = onHelp,
        onBack = onBack,
        onMenu = onMenu,
        onRefresh = { viewModel.handleEvent(PermissionsEvent.Refresh()) },
        onStartAutoRiskScan = { viewModel.handleEvent(PermissionsEvent.AutoRiskScanStarted()) },
        onCancelAutoRiskScan = { viewModel.handleEvent(PermissionsEvent.AutoRiskScanCancelled()) },
        onFilterChange = { value, trackUsage ->
            viewModel.handleEvent(PermissionsEvent.FilterChanged(value, trackUsage))
        },
        onToggleShowOnlyRisky = { viewModel.handleEvent(PermissionsEvent.RiskFilterToggled()) },
        onSetShowOnlyRisky = { viewModel.handleEvent(PermissionsEvent.RiskFilterChanged(it)) },
        onRequestReportPreview = { viewModel.handleEvent(PermissionsEvent.ReportPreviewRequested(it)) },
        onToggleFavorite = { viewModel.handleEvent(PermissionsEvent.FavoriteToggled(it)) },
        onClearError = { viewModel.handleEvent(PermissionsEvent.ErrorDismissed()) },
    )
}

@Composable
private fun PermissionsScreenContent(
    state: PermissionsState,
    padding: PaddingValues,
    headerHelp: String,
    onHelp: (String) -> Unit,
    onBack: (() -> Unit)?,
    onMenu: () -> Unit,
    onRefresh: () -> Unit,
    onStartAutoRiskScan: () -> Unit,
    onCancelAutoRiskScan: () -> Unit,
    onFilterChange: (String, Boolean) -> Unit,
    onToggleShowOnlyRisky: () -> Unit,
    onSetShowOnlyRisky: (Boolean) -> Unit,
    onRequestReportPreview: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onClearError: () -> Unit,
) {
    val strings = LocalStrings.current
    val screenStrings = ScreenUiStrings.from(strings)

    val expandedPackages = remember { mutableStateMapOf<String, Boolean>() }
    val favoriteQueries = state.favoriteQueries
    val filteredApps = state.visibleApps
    val riskByPackage =
        remember(state.riskReports) { state.riskReports.associateBy { it.packageName } }
    val criticalTotal = state.riskReports.sumOf { it.criticalCount }
    val highTotal = state.riskReports.sumOf { it.highCount }
    val mediumTotal = state.riskReports.sumOf { it.mediumCount }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var searchToolsExpanded by remember { mutableStateOf(true) }
    val showScrollTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 1 || listState.firstVisibleItemScrollOffset > 240 }
    }
    val scanProgress = if (state.autoScanTotalApps > 0) {
        state.autoScanScannedApps.toFloat() / state.autoScanTotalApps.toFloat()
    } else {
        0f
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = screenContentPadding(padding, horizontal = 16.dp, vertical = 10.dp),
        ) {
            item {
                SectionHeader(
                    title = strings.navPermissions,
                    help = headerHelp,
                    onHelp = onHelp,
                    beforeHelpAction = {
                        IconButton(onClick = onRefresh) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = strings.refreshPermissionsContentDescription,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    },
                    onBack = onBack,
                    onMenu = onMenu,
                )
            }
            item {
                AppText(
                    strings.helpPermissions,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    AppText(
                        screenStrings.riskScanTitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    AppText(
                        screenStrings.riskScanDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AppButton(
                            text = screenStrings.startScan,
                            onClick = onStartAutoRiskScan,
                            enabled = state.apps.isNotEmpty() && !state.autoScanRunning,
                            variant = ButtonVariant.Tonal,
                            modifier = Modifier.fillMaxWidth(),
                            fullWidth = true,
                            leadingIcon = Icons.Filled.Warning,
                        )
                        AppButton(
                            text = screenStrings.stopScan,
                            onClick = onCancelAutoRiskScan,
                            enabled = state.autoScanRunning,
                            variant = ButtonVariant.Secondary,
                            modifier = Modifier.fillMaxWidth(),
                            fullWidth = true,
                            leadingIcon = Icons.Filled.Stop,
                        )
                    }

                    if (state.autoScanRunning) {
                        AppText(
                            screenStrings.scanProgress.format(state.autoScanScannedApps, state.autoScanTotalApps),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                        state.autoScanCurrentLabel?.let { label ->
                            AppText(
                                screenStrings.analysis.format(label),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        LinearProgressIndicator(
                            progress = { scanProgress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                        )
                    }

                    if (state.riskReports.isNotEmpty()) {
                        AppText(
                            screenStrings.riskSummary.format(
                                state.riskReports.size,
                                criticalTotal,
                                highTotal,
                                mediumTotal,
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }
            item {
                AppAccordion(
                    title = strings.permissionsSearch,
                    subtitle = screenStrings.searchFilters,
                    meta = buildString {
                        if (state.showOnlyRisky) append(screenStrings.riskOnly)
                        if (favoriteQueries.isNotEmpty()) {
                            if (isNotEmpty()) append(" • ")
                            append(favoriteQueries.size)
                        }
                    }.ifBlank { null },
                    expanded = searchToolsExpanded,
                    onToggle = { searchToolsExpanded = !searchToolsExpanded },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    SearchFieldWithHistoryComponent(
                        value = state.filter,
                        onValueChange = { onFilterChange(it, false) },
                        onSubmit = { onFilterChange(it, true) },
                        label = strings.permissionsSearch,
                        historyKey = "permissions",
                        modifier = Modifier.fillMaxWidth(),
                        showClearAction = true,
                        onCleared = {
                            onFilterChange("", false)
                        },
                    )
                    state.topQuery?.let { top ->
                        AppText(
                            screenStrings.frequentSearch.format(top),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    if (favoriteQueries.isNotEmpty()) {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(
                                vertical = 6.dp,
                            ),
                        ) {
                            items(favoriteQueries, key = { it }) { fav ->
                                FilterChip(
                                    selected = true,
                                    onClick = { onFilterChange(fav, true) },
                                    label = { AppText(fav) },
                                )
                            }
                        }
                    }
                    if (state.riskReports.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            FilterChip(
                                selected = state.showOnlyRisky,
                                onClick = onToggleShowOnlyRisky,
                                label = { AppText(screenStrings.riskOnly) },
                            )
                            FilterChip(
                                selected = false,
                                onClick = { onFilterChange("", false) },
                                label = { AppText(screenStrings.resetFilter) },
                            )
                        }
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(state.riskReports.take(8), key = { it.packageName }) { report ->
                                FilterChip(
                                    selected = false,
                                    onClick = {
                                        onFilterChange(
                                            report.packageName,
                                            true,
                                        )
                                    },
                                    label = { AppText("${report.appLabel} (${report.findings.size})") },
                                )
                            }
                        }
                    }
                }
            }

            if (!state.loading && state.error == null) {
                item {
                    AppText(
                        screenStrings.resultsSummary.format(filteredApps.size, state.apps.size),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            state.lastExportPath?.let { path ->
                item {
                    ExportDetailsCard(
                        path = path,
                        title = strings.apkExported,
                        summary = screenStrings.permissionsExportSummary,
                        details = listOf(
                            screenStrings.permissionsExportDetails,
                            screenStrings.readableFormat,
                        ),
                    )
                }
            }
            if (state.loading) {
                item {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
                }
            }

            if (!state.loading && filteredApps.isEmpty()) {
                item {
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        val hasFilter = state.filter.isNotBlank() || state.showOnlyRisky
                        AppText(
                            if (hasFilter) screenStrings.noRiskResults else screenStrings.noPermissions,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        AppText(
                            if (hasFilter) screenStrings.riskFilterHint else screenStrings.refreshPermissionsHint,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            if (state.showOnlyRisky) {
                                AppButton(
                                    text = screenStrings.allApps,
                                    onClick = { onSetShowOnlyRisky(false) },
                                    variant = ButtonVariant.Secondary,
                                    modifier = Modifier.weight(1f),
                                    fullWidth = false,
                                )
                            } else if (state.filter.isNotBlank()) {
                                AppButton(
                                    text = screenStrings.resetFilter,
                                    onClick = { onFilterChange("", false) },
                                    variant = ButtonVariant.Secondary,
                                    modifier = Modifier.weight(1f),
                                    fullWidth = false,
                                )
                            }
                            AppButton(
                                text = screenStrings.refresh,
                                onClick = onRefresh,
                                variant = ButtonVariant.Tonal,
                                modifier = Modifier.weight(1f),
                                fullWidth = false,
                            )
                        }
                    }
                }
            }

            state.error?.let { err ->
                item {
                    AppText(err, color = MaterialTheme.colorScheme.error)
                }
            }
            if (state.reportPreviewLoading) {
                item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
            }
            state.reportPreviewError?.let { error ->
                item { AppText(error, color = MaterialTheme.colorScheme.error) }
            }
            items(filteredApps, key = { it.packageName }) { app ->
                val expanded = expandedPackages[app.packageName] ?: false
                val isFavorite = state.favorites.contains(app.packageName)
                val riskReport = riskByPackage[app.packageName]
                val permissionFindings = state.visibleFindingsByPackage[app.packageName]
                    .orEmpty().associateBy { it.key }
                val appMeta = if (riskReport == null) {
                    screenStrings.permissionsCount.format(app.permissions.size)
                } else {
                    screenStrings.permissionsRiskCount.format(app.permissions.size, riskReport.findings.size)
                }
                AppAccordion(
                    title = app.appLabel,
                    subtitle = app.packageName,
                    meta = appMeta,
                    expanded = expanded,
                    onToggle = { expandedPackages[app.packageName] = !expanded },
                    leading = {
                        PackageIcon(
                            packageName = app.packageName,
                            modifier = Modifier.size(44.dp),
                        )
                    },
                    titleMaxLines = 3,
                    subtitleMaxLines = 3,
                    textOverflow = TextOverflow.Clip,
                    headerVerticalPadding = 10.dp,
                    headerMinHeight = 88.dp,
                    actionsBelowHeader = true,
                    actions = {
                        IconButton(
                            onClick = {
                                onRequestReportPreview(app.packageName)
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = strings.sharePermissionsContentDescription,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        IconButton(onClick = { onToggleFavorite(app.packageName) }) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                                contentDescription = strings.favoriteContentDescription,
                                tint = if (isFavorite) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                ) {
                    AppText(
                        if (expanded) {
                            strings.permissionsExpandedAppLabel.format(
                                app.appLabel,
                                app.packageName,
                            )
                        } else {
                            strings.helpPermissions
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    riskReport?.let { report ->
                        AppText(
                            screenStrings.risksDetected.format(
                                report.findings.size,
                                report.criticalCount,
                                report.highCount,
                                report.mediumCount,
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = 4.dp),
                        )
                        state.findingsByPackage[app.packageName].orEmpty().take(6).forEach { finding ->
                            RiskFindingRow(finding = finding)
                        }
                        if (report.findings.size > 6) {
                            AppText(
                                screenStrings.findingAdditional.format(report.findings.size - 6),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(
                            4.dp,
                        ),
                    ) {
                        app.permissions.forEach { perm ->
                            PermissionRow(
                                permission = perm,
                                finding = permissionFindings[perm],
                            )
                        }
                    }
                }
            }
        }

        ScrollToTopButton(
            visible = showScrollTop,
            onClick = { scope.launch { listState.animateScrollToItem(0) } },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
        )
    }

    state.error?.let { ErrorDialogComponent(it, onClearError) }
}

@AppScreenPreviews
@Composable
private fun PermissionsScreenPreview() {
    val apps = listOf(
        AppPermissionEntry(
            appLabel = "CrackMyDroid Security Analysis Application",
            packageName = "com.crackmydroid.security.analysis.application",
            permissions = listOf(
                "android.permission.CAMERA",
                "android.permission.ACCESS_FINE_LOCATION",
                "android.permission.INTERNET",
            ),
        ),
    )
    AppPreviewEnvironment {
        PermissionsScreenContent(
            state = PermissionsState(
                apps = apps,
                visibleApps = apps,
                favoriteQueries = listOf("camera"),
            ),
            padding = PaddingValues(),
            headerHelp = "Esamina i permessi e i rischi delle app.",
            onHelp = {},
            onBack = {},
            onMenu = {},
            onRefresh = {},
            onStartAutoRiskScan = {},
            onCancelAutoRiskScan = {},
            onFilterChange = { _, _ -> },
            onToggleShowOnlyRisky = {},
            onSetShowOnlyRisky = {},
            onRequestReportPreview = {},
            onToggleFavorite = {},
            onClearError = {},
        )
    }
}

@Composable
private fun RiskFindingRow(finding: PermissionFindingUi) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        AppText(
            "[${finding.levelLabel}] ${finding.title}",
            style = MaterialTheme.typography.bodySmall,
            color = riskLevelColor(finding.level),
        )
        AppText(
            finding.reason,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PermissionRow(permission: String, finding: PermissionFindingUi? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = permissionIconFor(permission),
            contentDescription = permission,
            tint = finding?.let { riskLevelColor(it.level) } ?: permissionColor(permission),
            modifier = Modifier.size(18.dp),
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            AppText(
                permission,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            finding?.let {
                AppText(
                    "${it.levelLabel}: ${it.reason}",
                    style = MaterialTheme.typography.labelSmall,
                    color = riskLevelColor(it.level),
                )
            }
        }
    }
}

private fun permissionIconFor(permission: String): ImageVector {
    val key = permission.uppercase()
    return when {
        "CAMERA" in key -> Icons.Filled.CameraAlt
        "AUDIO" in key || "MIC" in key -> Icons.Filled.Mic
        "LOCATION" in key -> Icons.Filled.LocationOn
        "NOTIFICATION" in key -> Icons.Filled.Notifications
        "BLUETOOTH" in key -> Icons.Filled.Bluetooth
        "PHONE" in key || "CALL" in key -> Icons.Filled.Phone
        "CONTACT" in key -> Icons.Filled.Contacts
        "SMS" in key || "MMS" in key -> Icons.Filled.Sms
        "WIFI" in key || "NETWORK" in key -> Icons.Filled.Wifi
        "STORAGE" in key || "READ_MEDIA" in key || "FILES" in key -> Icons.Filled.SdStorage
        "CALENDAR" in key -> Icons.Filled.CalendarToday
        "FINGERPRINT" in key || "BIOMETRIC" in key -> Icons.Filled.Fingerprint
        "INTERNET" in key -> Icons.Filled.Cloud
        else -> Icons.Filled.Lock
    }
}

@Composable
private fun permissionColor(permission: String): Color {
    val key = permission.uppercase()
    val scheme = MaterialTheme.colorScheme
    return when {
        "CAMERA" in key -> scheme.tertiary
        "AUDIO" in key || "MIC" in key -> scheme.primary
        "LOCATION" in key -> scheme.secondary
        "NOTIFICATION" in key -> scheme.primaryContainer
        "BLUETOOTH" in key -> scheme.tertiaryContainer
        "PHONE" in key || "CALL" in key -> scheme.secondaryContainer
        "CONTACT" in key -> scheme.inversePrimary
        "SMS" in key || "MMS" in key -> scheme.primary
        "WIFI" in key || "NETWORK" in key -> scheme.secondary
        "STORAGE" in key || "READ_MEDIA" in key || "FILES" in key -> scheme.surfaceVariant
        "CALENDAR" in key -> scheme.tertiary
        "FINGERPRINT" in key || "BIOMETRIC" in key -> scheme.primary
        "INTERNET" in key -> scheme.secondary
        else -> scheme.outline
    }
}

@Composable
private fun riskLevelColor(level: PermissionRiskLevel): Color {
    val scheme = MaterialTheme.colorScheme
    return when (level) {
        PermissionRiskLevel.Critical -> scheme.error
        PermissionRiskLevel.High -> scheme.tertiary
        PermissionRiskLevel.Medium -> scheme.secondary
    }
}
