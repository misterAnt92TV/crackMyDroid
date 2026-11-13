package com.crackmydroid.shared.ui.installedapps.view

import com.crackmydroid.architecture.presentation.model.UiLoadState
import com.crackmydroid.ui.icons.AppIcons

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.crackmydroid.shared.domain.model.InstallSourceClassification
import com.crackmydroid.shared.domain.model.InstallSourceInfo
import com.crackmydroid.shared.domain.model.InstalledAppEntry
import com.crackmydroid.shared.i18n.LocalStrings
import com.crackmydroid.shared.i18n.ScreenUiStrings
import com.crackmydroid.shared.i18n.Strings
import com.crackmydroid.shared.i18n.format
import com.crackmydroid.shared.presentation.components.SearchPanelComponent
import com.crackmydroid.shared.presentation.preview.AppPreviewEnvironment
import com.crackmydroid.shared.presentation.preview.AppScreenPreviews
import com.crackmydroid.shared.ui.installedapps.InstalledAppsEvent
import com.crackmydroid.shared.ui.installedapps.InstalledAppsState
import com.crackmydroid.shared.ui.installedapps.InstalledAppsViewModel
import com.crackmydroid.shared.ui.screens.screenContentPadding
import com.crackmydroid.ui.components.AppAccordion
import com.crackmydroid.ui.components.AppIcon
import com.crackmydroid.ui.components.AppListSkeleton
import com.crackmydroid.ui.components.button.AppButton
import com.crackmydroid.ui.components.button.ButtonVariant
import com.crackmydroid.ui.components.dialog.ErrorDialogComponent
import com.crackmydroid.ui.components.PackageIcon
import com.crackmydroid.ui.components.button.ScrollToTopButton
import com.crackmydroid.ui.components.card.AppCard
import com.crackmydroid.ui.components.card.ExportDetailsCard
import com.crackmydroid.ui.components.text.AppText
import kotlinx.coroutines.launch

@Composable
fun InstalledAppsScreen(
    viewModel: InstalledAppsViewModel,
    padding: PaddingValues,
    searchRequestId: Long = 0,
    searchVisible: Boolean = true,
    onSearchVisibilityChanged: (Boolean) -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    InstalledAppsScreenContent(
        state = state,
        padding = padding,
        searchRequestId = searchRequestId,
        searchVisible = searchVisible,
        onSearchVisibilityChanged = onSearchVisibilityChanged,
        onRefresh = { viewModel.emitEvent(InstalledAppsEvent.Refresh()) },
        onShareAll = { viewModel.emitEvent(InstalledAppsEvent.AllShareRequested()) },
        onFilterChange = { value, trackUsage ->
            viewModel.emitEvent(InstalledAppsEvent.FilterChanged(value, trackUsage))
        },
        onToggleFavorite = { viewModel.emitEvent(InstalledAppsEvent.FavoriteToggled(it)) },
        onShare = { viewModel.emitEvent(InstalledAppsEvent.ApkShareRequested(it)) },
        onExport = { viewModel.emitEvent(InstalledAppsEvent.ApkExportRequested(it)) },
        onClearError = { viewModel.emitEvent(InstalledAppsEvent.ErrorDismissed()) },
    )
}

@Composable
private fun InstalledAppsScreenContent(
    state: InstalledAppsState,
    padding: PaddingValues,
    searchRequestId: Long,
    searchVisible: Boolean = true,
    onSearchVisibilityChanged: (Boolean) -> Unit = {},
    onRefresh: () -> Unit,
    onShareAll: (String) -> Unit,
    onFilterChange: (String, Boolean) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onShare: (String) -> Unit,
    onExport: (String) -> Unit,
    onClearError: () -> Unit,
) {
    val strings = LocalStrings.current
    val screenStrings = ScreenUiStrings.from(strings)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val expandedPackages = remember { mutableStateMapOf<String, Boolean>() }
    val favoriteQueries = state.favoriteQueries
    val filteredApps = state.visibleApps
    val listState = rememberLazyListState()
    LaunchedEffect(searchRequestId) {
        if (searchRequestId > 0) {
            listState.animateScrollToItem(0)
        }
    }
    val showScrollTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 1 || listState.firstVisibleItemScrollOffset > 240 }
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
                AppText(
                    strings.helpApk,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                SearchPanelComponent(
                    title = strings.apkSearch,
                    subtitle = screenStrings.searchFilters,
                    meta = favoriteQueries.takeIf { it.isNotEmpty() }?.size?.toString(),
                    expanded = searchVisible,
                    onExpandedChange = onSearchVisibilityChanged,
                    query = state.filter,
                    onQueryChange = { onFilterChange(it, false) },
                    onSubmit = { onFilterChange(it, true) },
                    label = strings.apkSearch,
                    historyKey = "installed_apps",
                    focusRequestId = searchRequestId,
                    showClearAction = true,
                    onCleared = {
                        onFilterChange("", false)
                        scope.launch { snackbarHostState.showSnackbar(screenStrings.suggestionsCleared) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
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
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            contentPadding = PaddingValues(
                                vertical = 4.dp,
                            ),
                        ) {
                            items(favoriteQueries, key = { it }) { fav ->
                                @OptIn(ExperimentalFoundationApi::class)
                                FilterChip(
                                    selected = true,
                                    onClick = { onFilterChange(fav, true) },
                                    label = { AppText(fav) },
                                    modifier = Modifier.combinedClickable(
                                        onClick = { onFilterChange(fav, true) },
                                        onLongClick = {
                                            onToggleFavorite(fav)
                                            scope.launch {
                                                snackbarHostState.showSnackbar(screenStrings.removedFromFavorites)
                                            }
                                        },
                                    ),
                                )
                            }
                        }
                    }
                }
            }

            if (state.loadState is UiLoadState.Loading && state.apps.isEmpty()) {
                item {
                    AppListSkeleton()
                }
            } else if (state.loadState is UiLoadState.Loading) {
                item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
            }

            state.lastExportPath?.let { path ->
                item {
                    ExportDetailsCard(
                        path = path,
                        title = strings.apkExported,
                        summary = screenStrings.appsExportSummary,
                        details = listOf(
                            screenStrings.appsExportContent,
                            screenStrings.exportReady,
                        ),
                    )
                }
            }

            state.lastSharedPackage?.let { pkg ->
                item {
                    AppText(
                        "${strings.apkShared} ($pkg)",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            (state.loadError ?: state.error)?.let { err ->
                item {
                    AppText(
                        err,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            if (state.loadState !is UiLoadState.Loading && state.loadError == null && state.error == null) {
                item {
                    AppText(
                        screenStrings.resultsSummary.format(filteredApps.size, state.apps.size),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (state.loadState !is UiLoadState.Loading && filteredApps.isEmpty()) {
                item {
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        val hasFilter = state.filter.isNotBlank()
                        AppText(
                            if (hasFilter) screenStrings.noResultsFor.format(state.filter) else screenStrings.noApps,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        AppText(
                            if (hasFilter) screenStrings.changeFilter else screenStrings.refreshAppsHint,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            if (hasFilter) {
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

            items(filteredApps, key = { it.packageName }) { app ->
                val expanded = expandedPackages[app.packageName] ?: false
                val isFavorite = state.favorites.contains(app.packageName)
                AppAccordion(
                    title = app.appLabel,
                    subtitle = app.packageName,
                    meta = app.installSource.label(strings),
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
                        IconButton(onClick = { onShare(app.packageName) }) {
                            AppIcon(
                                imageVector = AppIcons.Filled.Share,
                                contentDescription = strings.shareApkContentDescription,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        IconButton(onClick = { onToggleFavorite(app.packageName) }) {
                            AppIcon(
                                imageVector = if (isFavorite) AppIcons.Filled.Star else AppIcons.Filled.StarBorder,
                                contentDescription = strings.favoriteContentDescription,
                                tint = if (isFavorite) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    ApkDetailRow(
                        label = strings.apkSize,
                        value = app.apkSizeBytes?.let { formatApkSize(it) } ?: "-",
                    )
                    ApkDetailRow(
                        label = strings.apkTargetSdk,
                        value = app.targetSdkVersion?.toString() ?: "-",
                    )
                    ApkDetailRow(
                        label = strings.apkMinSdk,
                        value = app.minSdkVersion?.toString() ?: "-",
                    )
                    ApkDetailRow(
                        label = strings.appInstallSource,
                        value = app.installSource.label(strings),
                    )
                    app.installSource.installingPackageName?.let { installer ->
                        ApkDetailRow(
                            label = strings.appInstallerPackage,
                            value = installer,
                        )
                    }
                    app.installSource.initiatingPackageName?.let { initiator ->
                        ApkDetailRow(
                            label = strings.appInitiatingPackage,
                            value = initiator,
                        )
                    }
                    app.installSource.originatingPackageName?.let { origin ->
                        ApkDetailRow(
                            label = strings.appOriginatingPackage,
                            value = origin,
                        )
                    }
                    ApkDetailRow(
                        label = strings.apkPath,
                        value = app.sourcePath,
                    )
                    AppText(
                        strings.appInstallSourceLimit,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AppButton(
                            text = strings.apkExtract,
                            onClick = { onExport(app.packageName) },
                            modifier = Modifier.weight(1f),
                            fullWidth = false,
                        )
                        AppButton(
                            text = strings.apkShare,
                            onClick = { onShare(app.packageName) },
                            variant = ButtonVariant.Secondary,
                            modifier = Modifier.weight(1f),
                            fullWidth = false,
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
                .padding(12.dp),
        )

        SnackbarHost(
            hostState = snackbarHostState,
            snackbar = { Snackbar(it) },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    (state.loadError ?: state.error)?.let { ErrorDialogComponent(it, onClearError) }
}

@AppScreenPreviews
@Composable
private fun InstalledAppsScreenPreview() {
    val apps = listOf(
        InstalledAppEntry(
            appLabel = "Security Monitor",
            packageName = "com.example.security.monitor",
            sourcePath = "/data/app/example/security-monitor/base.apk",
            debuggable = true,
            apkSizeBytes = 12_582_912L,
            minSdkVersion = 26,
            targetSdkVersion = 34,
        ),
        InstalledAppEntry(
            appLabel = "Long Enterprise Device Security Companion",
            packageName = "com.example.enterprise.device.security.companion",
            sourcePath = "/data/app/example/enterprise-companion/base.apk",
            allowBackup = true,
            apkSizeBytes = 45_200_000L,
            minSdkVersion = 21,
            targetSdkVersion = 35,
        ),
    )
    AppPreviewEnvironment {
        InstalledAppsScreenContent(
            state = InstalledAppsState(
                apps = apps,
                visibleApps = apps,
                favorites = setOf(apps.first().packageName),
                favoriteQueries = listOf("com.crackmydroid"),
            ),
            padding = PaddingValues(),
            searchRequestId = 0,
            onRefresh = {},
            onShareAll = {},
            onFilterChange = { _, _ -> },
            onToggleFavorite = {},
            onShare = {},
            onExport = {},
            onClearError = {},
        )
    }
}

@Composable
private fun ApkDetailRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        AppText(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.38f),
        )
        AppText(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.62f),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

internal fun formatApkSize(bytes: Long): String {
    if (bytes <= 0L) return "0 B"
    val units = listOf("B", "KB", "MB", "GB")
    var value = bytes.toDouble()
    var unitIndex = 0
    while (value >= 1024.0 && unitIndex < units.lastIndex) {
        value /= 1024.0
        unitIndex++
    }
    return if (unitIndex == 0) {
        "$bytes B"
    } else {
        val scaled = ((value * 10) + 0.5).toLong()
        val whole = scaled / 10
        val dec = scaled % 10
        if (dec == 0L) {
            "$whole ${units[unitIndex]}"
        } else {
            "$whole.$dec ${units[unitIndex]}"
        }
    }
}

private fun InstallSourceInfo.label(strings: Strings): String = when (classification) {
    InstallSourceClassification.GOOGLE_PLAY -> strings.appInstallSourceGooglePlay
    InstallSourceClassification.OTHER -> strings.appInstallSourceOther
    InstallSourceClassification.UNKNOWN -> strings.appInstallSourceUnknown
    InstallSourceClassification.UNAVAILABLE -> strings.appInstallSourceUnavailable
}
