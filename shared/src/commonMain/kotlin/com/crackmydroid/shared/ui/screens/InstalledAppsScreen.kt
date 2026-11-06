package com.crackmydroid.shared.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.crackmydroid.shared.i18n.LocalStrings
import com.crackmydroid.shared.i18n.ScreenUiStrings
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
import com.crackmydroid.shared.ui.installedapps.InstalledAppsViewModel
import com.crackmydroid.shared.domain.model.InstallSourceClassification
import kotlinx.coroutines.launch

@Composable
fun InstalledAppsScreen(
    viewModel: InstalledAppsViewModel,
    padding: androidx.compose.foundation.layout.PaddingValues,
    headerHelp: String,
    onHelp: (String) -> Unit,
    onBack: (() -> Unit)? = null,
    onMenu: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val strings = LocalStrings.current
    val screenStrings = ScreenUiStrings.from(strings)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val expandedPackages = remember { mutableStateMapOf<String, Boolean>() }
    val favoriteQueries = state.favoriteQueries
    val filteredApps = state.visibleApps
    val listState = rememberLazyListState()
    var searchToolsExpanded by remember { mutableStateOf(true) }
    val showScrollTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 1 || listState.firstVisibleItemScrollOffset > 240 }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = screenContentPadding(padding, horizontal = 16.dp, vertical = 10.dp)
        ) {
            item {
                SectionHeader(
                    title = strings.navApk,
                    help = headerHelp,
                    onHelp = onHelp,
                    beforeHelpAction = {
                        IconButton(
                            onClick = viewModel::refresh,
                            modifier = Modifier.size(36.dp),
                            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = strings.refreshAppsContentDescription
                            )
                        }
                        IconButton(
                            onClick = { viewModel.shareAll(state.topQuery ?: "apps") },
                            modifier = Modifier.size(36.dp),
                            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = strings.shareAppsContentDescription
                            )
                        }
                    },
                    onBack = onBack,
                    onMenu = onMenu
                )
            }
            item {
                AppText(
                    strings.helpApk,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                AppAccordion(
                    title = strings.apkSearch,
                    subtitle = screenStrings.searchFilters,
                    meta = favoriteQueries.takeIf { it.isNotEmpty() }?.size?.toString(),
                    expanded = searchToolsExpanded,
                    onToggle = { searchToolsExpanded = !searchToolsExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SearchFieldWithHistoryComponent(
                        value = state.filter,
                        onValueChange = { viewModel.setFilter(it) },
                        onSubmit = { viewModel.setFilter(it, trackUsage = true) },
                        label = strings.apkSearch,
                        historyKey = "installed_apps",
                        modifier = Modifier.fillMaxWidth(),
                        showClearAction = true,
                        onCleared = {
                            viewModel.setFilter("")
                            scope.launch { snackbarHostState.showSnackbar(screenStrings.suggestionsCleared) }
                        }
                    )
                    state.topQuery?.let { top ->
                        AppText(
                            screenStrings.frequentSearch.format(top),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (favoriteQueries.isNotEmpty()) {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                vertical = 4.dp
                            )
                        ) {
                            items(favoriteQueries, key = { it }) { fav ->
                                @OptIn(ExperimentalFoundationApi::class)
                                FilterChip(
                                    selected = true,
                                    onClick = { viewModel.setFilter(fav, trackUsage = true) },
                                    label = { AppText(fav) },
                                    modifier = Modifier.combinedClickable(
                                        onClick = { viewModel.setFilter(fav, trackUsage = true) },
                                        onLongClick = {
                                            viewModel.toggleFavorite(fav)
                                            scope.launch { snackbarHostState.showSnackbar(screenStrings.removedFromFavorites) }
                                        }
                                    )
                                )
                            }
                        }
                    }
                }
            }

            if (state.loading) {
                item {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
                }
            }

            state.lastExportPath?.let { path ->
                item {
                    ExportDetailsCard(
                        path = path,
                        title = strings.apkExported,
                        summary = screenStrings.appsExportSummary,
                        details = listOf(
                            screenStrings.appsExportContent,
                            screenStrings.exportReady
                        )
                    )
                }
            }

            state.lastSharedPackage?.let { pkg ->
                item {
                    AppText(
                        "${strings.apkShared} ($pkg)",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            state.error?.let { err ->
                item {
                    AppText(
                        err,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (!state.loading && state.error == null) {
                item {
                    AppText(
                        screenStrings.resultsSummary.format(filteredApps.size, state.apps.size),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!state.loading && filteredApps.isEmpty()) {
                item {
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        val hasFilter = state.filter.isNotBlank()
                        AppText(
                            if (hasFilter) screenStrings.noResultsFor.format(state.filter) else screenStrings.noApps,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        AppText(
                            if (hasFilter) screenStrings.changeFilter else screenStrings.refreshAppsHint,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (hasFilter) {
                                AppButton(
                                    text = screenStrings.resetFilter,
                                    onClick = { viewModel.setFilter("") },
                                    variant = ButtonVariant.Secondary,
                                    modifier = Modifier.weight(1f),
                                    fullWidth = false
                                )
                            }
                            AppButton(
                                text = screenStrings.refresh,
                                onClick = viewModel::refresh,
                                variant = ButtonVariant.Tonal,
                                modifier = Modifier.weight(1f),
                                fullWidth = false
                            )
                        }
                    }
                }
            }

            items(filteredApps, key = { it.packageName }) { app ->
                val expanded = expandedPackages[app.packageName] ?: false
                val isFavorite = state.favorites.contains(app.packageName)
                AppAccordion(
                    title = app.appLabel,
                    subtitle = "${app.packageName} · ${app.installSource.label(strings)}",
                    expanded = expanded,
                    onToggle = { expandedPackages[app.packageName] = !expanded },
                    leading = {
                        PackageIcon(
                            packageName = app.packageName,
                            modifier = Modifier.size(48.dp)
                        )
                    },
                    actions = {
                        IconButton(onClick = { viewModel.share(app.packageName, false) }) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = strings.shareApkContentDescription,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { viewModel.toggleFavorite(app.packageName) }) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                                contentDescription = strings.favoriteContentDescription,
                                tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AppText(
                        "${strings.apkPath}: ${app.sourcePath}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    AppText(
                        "${strings.appInstallSource}: ${app.installSource.label(strings)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    app.installSource.installingPackageName?.let { installer ->
                        AppText(
                            "${strings.appInstallerPackage}: $installer",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    app.installSource.initiatingPackageName?.let { initiator ->
                        AppText(
                            "${strings.appInitiatingPackage}: $initiator",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    app.installSource.originatingPackageName?.let { origin ->
                        AppText(
                            "${strings.appOriginatingPackage}: $origin",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    AppText(
                        strings.appInstallSourceLimit,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    AppButton(
                        text = strings.apkExtract,
                        onClick = { viewModel.export(app.packageName) }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppButton(
                            text = strings.apkShare,
                            onClick = { viewModel.share(app.packageName, false) },
                            variant = ButtonVariant.Secondary,
                            modifier = Modifier.weight(1f),
                            fullWidth = false
                        )
                        AppButton(
                            text = strings.apkShareBt,
                            onClick = { viewModel.share(app.packageName, true) },
                            variant = ButtonVariant.Tonal,
                            modifier = Modifier.weight(1f),
                            fullWidth = false
                        )
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

        SnackbarHost(
            hostState = snackbarHostState,
            snackbar = { Snackbar(it) },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    state.error?.let { ErrorDialogComponent(it, viewModel::clearError) }
}

private fun com.crackmydroid.shared.domain.model.InstallSourceInfo.label(strings: com.crackmydroid.shared.i18n.Strings): String =
    when (classification) {
        InstallSourceClassification.GOOGLE_PLAY -> strings.appInstallSourceGooglePlay
        InstallSourceClassification.OTHER -> strings.appInstallSourceOther
        InstallSourceClassification.UNKNOWN -> strings.appInstallSourceUnknown
        InstallSourceClassification.UNAVAILABLE -> strings.appInstallSourceUnavailable
    }
