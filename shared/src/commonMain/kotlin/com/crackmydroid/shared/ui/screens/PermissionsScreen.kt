package com.crackmydroid.shared.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Wifi
import com.crackmydroid.shared.domain.model.PermissionRiskFinding
import com.crackmydroid.shared.domain.model.PermissionRiskLevel
import com.crackmydroid.shared.i18n.LocalStrings
import com.crackmydroid.shared.presentation.components.AppButton
import com.crackmydroid.shared.presentation.components.AppCard
import com.crackmydroid.shared.presentation.components.AppAccordion
import com.crackmydroid.shared.presentation.components.AppText
import com.crackmydroid.shared.presentation.components.ButtonVariant
import com.crackmydroid.shared.presentation.components.ErrorDialog
import com.crackmydroid.shared.presentation.components.ExportDetailsCard
import com.crackmydroid.shared.presentation.components.SectionHeader
import com.crackmydroid.shared.presentation.components.PackageIcon
import com.crackmydroid.shared.presentation.components.SearchFieldWithHistory
import com.crackmydroid.shared.presentation.components.ScrollToTopButton
import com.crackmydroid.shared.ui.permissions.PermissionsViewModel
import kotlinx.coroutines.launch

@Composable
fun PermissionsScreen(
    viewModel: PermissionsViewModel,
    padding: androidx.compose.foundation.layout.PaddingValues,
    headerHelp: String,
    onHelp: (String) -> Unit,
    onBack: (() -> Unit)? = null,
    onMenu: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val strings = LocalStrings.current
    val clipboard = LocalClipboardManager.current
    val expandedPackages = remember { mutableStateMapOf<String, Boolean>() }
    val favoriteQueries = remember(state.favorites) { state.favorites.toList().sorted() }
    val filteredApps = remember(
        state.apps,
        state.filter,
        state.favorites,
        state.showOnlyRisky,
        state.riskReports
    ) { viewModel.filtered() }
    val riskByPackage = remember(state.riskReports) { state.riskReports.associateBy { it.packageName } }
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
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 30.dp)
        ) {
            item {
                SectionHeader(
                    title = strings.navPermissions,
                    help = headerHelp,
                    onHelp = onHelp,
                    beforeHelpAction = {
                        IconButton(onClick = viewModel::refresh) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Aggiorna permessi",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    onBack = onBack,
                    onMenu = onMenu
                )
            }
            item {
                AppText(
                    strings.helpPermissions,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    AppText(
                        "Scansione automatica permessi rischiosi",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    AppText(
                        "Analizza tutte le app installate e segnala permessi pericolosi o pattern potenzialmente malevoli.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppButton(
                            text = "Avvia scansione",
                            onClick = viewModel::startAutoRiskScan,
                            enabled = state.apps.isNotEmpty() && !state.autoScanRunning,
                            variant = ButtonVariant.Tonal,
                            modifier = Modifier.weight(1f),
                            fullWidth = false,
                            leadingIcon = Icons.Filled.Warning
                        )
                        AppButton(
                            text = "Interrompi",
                            onClick = viewModel::cancelAutoRiskScan,
                            enabled = state.autoScanRunning,
                            variant = ButtonVariant.Secondary,
                            modifier = Modifier.weight(1f),
                            fullWidth = false,
                            leadingIcon = Icons.Filled.Stop
                        )
                    }

                    if (state.autoScanRunning) {
                        AppText(
                            "Scansione in corso: ${state.autoScanScannedApps}/${state.autoScanTotalApps} app",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        state.autoScanCurrentLabel?.let { label ->
                            AppText(
                                "Analisi: $label",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        LinearProgressIndicator(
                            progress = { scanProgress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp)
                        )
                    }

                    if (state.riskReports.isNotEmpty()) {
                        AppText(
                            "Riepilogo: ${state.riskReports.size} app a rischio • Critici $criticalTotal • Alti $highTotal • Medi $mediumTotal",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
            item {
                AppAccordion(
                    title = strings.permissionsSearch,
                    subtitle = "Suggerimenti e filtri",
                    meta = buildString {
                        if (state.showOnlyRisky) append("rischio")
                        if (favoriteQueries.isNotEmpty()) {
                            if (isNotEmpty()) append(" • ")
                            append(favoriteQueries.size)
                        }
                    }.ifBlank { null },
                    expanded = searchToolsExpanded,
                    onToggle = { searchToolsExpanded = !searchToolsExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SearchFieldWithHistory(
                        value = state.filter,
                        onValueChange = { viewModel.setFilter(it) },
                        onSubmit = { viewModel.setFilter(it, trackUsage = true) },
                        label = strings.permissionsSearch,
                        historyKey = "permissions",
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        showClearAction = true,
                        onCleared = {
                            viewModel.setFilter("")
                        }
                    )
                    state.topQuery?.let { top ->
                        AppText(
                            "Ricerca frequente: $top",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (favoriteQueries.isNotEmpty()) {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 6.dp)
                        ) {
                            items(favoriteQueries, key = { it }) { fav ->
                                FilterChip(
                                    selected = true,
                                    onClick = { viewModel.setFilter(fav, trackUsage = true) },
                                    label = { Text(fav) }
                                )
                            }
                        }
                    }
                    if (state.riskReports.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = state.showOnlyRisky,
                                onClick = viewModel::toggleShowOnlyRisky,
                                label = { Text("Solo app a rischio") }
                            )
                            FilterChip(
                                selected = false,
                                onClick = { viewModel.setFilter("") },
                                label = { Text("Reset filtro") }
                            )
                        }
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.riskReports.take(8), key = { it.packageName }) { report ->
                                FilterChip(
                                    selected = false,
                                    onClick = { viewModel.setFilter(report.packageName, trackUsage = true) },
                                    label = { Text("${report.appLabel} (${report.findings.size})") }
                                )
                            }
                        }
                    }
                }
            }

            if (!state.loading && state.error == null) {
                item {
                    AppText(
                        "Risultati: ${filteredApps.size} / ${state.apps.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            state.lastExportPath?.let { path ->
                item {
                    ExportDetailsCard(
                        path = path,
                        title = strings.apkExported,
                        summary = "Report permessi esportato in formato testuale, già pronto alla condivisione.",
                        details = listOf(
                            "Contiene riepilogo rischi (critici/alti/medi) e lista permessi per app.",
                            "Formato leggibile: blocchi separati per app e finding."
                        )
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
                            if (hasFilter) "Nessun risultato con i filtri correnti." else "Nessun permesso disponibile.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        AppText(
                            if (hasFilter) "Prova a disattivare \"Solo app a rischio\" o a cambiare ricerca."
                            else "Aggiorna i dati permessi per iniziare l'analisi.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (state.showOnlyRisky) {
                                AppButton(
                                    text = "Mostra tutte",
                                    onClick = { viewModel.setShowOnlyRisky(false) },
                                    variant = ButtonVariant.Secondary,
                                    modifier = Modifier.weight(1f),
                                    fullWidth = false
                                )
                            } else if (state.filter.isNotBlank()) {
                                AppButton(
                                    text = "Reset filtro",
                                    onClick = { viewModel.setFilter("") },
                                    variant = ButtonVariant.Secondary,
                                    modifier = Modifier.weight(1f),
                                    fullWidth = false
                                )
                            }
                            AppButton(
                                text = "Aggiorna",
                                onClick = viewModel::refresh,
                                variant = ButtonVariant.Tonal,
                                modifier = Modifier.weight(1f),
                                fullWidth = false
                            )
                        }
                    }
                }
            }

            state.error?.let { err ->
                item {
                    AppText(err, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
                }
            }
            items(filteredApps, key = { it.packageName }) { app ->
                val expanded = expandedPackages[app.packageName] ?: false
                val isFavorite = state.favorites.contains(app.packageName)
                val riskReport = riskByPackage[app.packageName]
                val permissionFindings = remember(riskReport) {
                    riskReport
                        ?.findings
                        ?.filter { !it.isPattern }
                        ?.associateBy { it.key }
                        ?: emptyMap()
                }
                val appMeta = if (riskReport == null) {
                    "${app.permissions.size} perm"
                } else {
                    "${app.permissions.size} perm • rischi ${riskReport.findings.size}"
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
                            modifier = Modifier.size(44.dp)
                        )
                    },
                    actions = {
                        IconButton(onClick = {
                            val content = viewModel.singleReportPreview(app.packageName)
                            clipboard.setText(AnnotatedString(content))
                            viewModel.shareSingle(app.packageName)
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Condividi permessi",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { viewModel.toggleFavorite(app.packageName) }) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                                contentDescription = "Preferito",
                                tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    AppText(
                        if (expanded) strings.close else strings.helpPermissions,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    riskReport?.let { report ->
                        AppText(
                            "Rischi rilevati: ${report.findings.size} (Critici ${report.criticalCount}, Alti ${report.highCount}, Medi ${report.mediumCount})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        report.findings.take(6).forEach { finding ->
                            RiskFindingRow(finding = finding)
                        }
                        if (report.findings.size > 6) {
                            AppText(
                                "+${report.findings.size - 6} finding aggiuntivi nel report condivisibile",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        app.permissions.forEach { perm ->
                            PermissionRow(
                                permission = perm,
                                finding = permissionFindings[perm]
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
                .padding(12.dp)
        )
    }

    state.error?.let { ErrorDialog(it, viewModel::clearError) }
}

@Composable
private fun RiskFindingRow(finding: PermissionRiskFinding) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        AppText(
            "[${riskLevelLabel(finding.level)}] ${finding.title}",
            style = MaterialTheme.typography.bodySmall,
            color = riskLevelColor(finding.level)
        )
        AppText(
            finding.reason,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PermissionRow(
    permission: String,
    finding: PermissionRiskFinding? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = permissionIconFor(permission),
            contentDescription = permission,
            tint = finding?.let { riskLevelColor(it.level) } ?: permissionColor(permission),
            modifier = Modifier.size(18.dp)
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            AppText(
                permission,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            finding?.let {
                AppText(
                    "${riskLevelLabel(it.level)}: ${it.reason}",
                    style = MaterialTheme.typography.labelSmall,
                    color = riskLevelColor(it.level)
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
private fun permissionColor(permission: String): androidx.compose.ui.graphics.Color {
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
private fun riskLevelColor(level: PermissionRiskLevel): androidx.compose.ui.graphics.Color {
    val scheme = MaterialTheme.colorScheme
    return when (level) {
        PermissionRiskLevel.Critical -> scheme.error
        PermissionRiskLevel.High -> scheme.tertiary
        PermissionRiskLevel.Medium -> scheme.secondary
    }
}

private fun riskLevelLabel(level: PermissionRiskLevel): String = when (level) {
    PermissionRiskLevel.Critical -> "CRITICO"
    PermissionRiskLevel.High -> "ALTO"
    PermissionRiskLevel.Medium -> "MEDIO"
}
