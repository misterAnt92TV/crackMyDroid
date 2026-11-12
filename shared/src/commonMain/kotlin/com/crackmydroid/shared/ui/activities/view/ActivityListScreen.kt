package com.crackmydroid.shared.ui.activities.view
import com.crackmydroid.ui.icons.AppIcons

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.crackmydroid.shared.domain.model.activity.ActivityEntry
import com.crackmydroid.shared.domain.model.activity.favoriteKey
import com.crackmydroid.shared.i18n.LocalStrings
import com.crackmydroid.shared.i18n.ScreenUiStrings
import com.crackmydroid.shared.i18n.format
import com.crackmydroid.shared.presentation.components.SearchFieldWithHistoryComponent
import com.crackmydroid.shared.presentation.preview.AppPreviewEnvironment
import com.crackmydroid.shared.presentation.preview.AppScreenPreviews
import com.crackmydroid.shared.ui.activities.ActivityAppReport
import com.crackmydroid.shared.ui.activities.ActivityGroup
import com.crackmydroid.shared.ui.activities.ActivityListEvent
import com.crackmydroid.shared.ui.activities.ActivityListState
import com.crackmydroid.shared.ui.activities.ActivityListViewModel
import com.crackmydroid.shared.ui.activities.model.ActivityListEffect
import com.crackmydroid.shared.ui.activities.model.uiLabel
import com.crackmydroid.shared.ui.screens.screenContentPadding
import com.crackmydroid.ui.components.AppAccordion
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
fun ActivityListScreen(
    viewModel: ActivityListViewModel,
    padding: PaddingValues,
    searchRequestId: Long = 0,
) {
    val state by viewModel.state.collectAsState()

    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            if (effect is ActivityListEffect.CopyToClipboard) clipboard.setText(AnnotatedString(effect.content))
        }
    }
    val callbacks = remember(viewModel) {
        ActivityListCallbacks(
            onRefresh = { viewModel.handleEvent(ActivityListEvent.Refresh()) },
            onSetFilter = { value, trackUsage ->
                viewModel.handleEvent(ActivityListEvent.FilterChanged(value, trackUsage))
            },
            onReportRequested = { viewModel.handleEvent(ActivityListEvent.ReportRequested(it)) },
            onReportDismissed = { viewModel.handleEvent(ActivityListEvent.ReportDismissed()) },
            onExportVisualReport = { viewModel.handleEvent(ActivityListEvent.VisualReportExportRequested(it)) },
            onClearError = { viewModel.handleEvent(ActivityListEvent.ErrorDismissed()) },
            onToggleExpanded = { viewModel.handleEvent(ActivityListEvent.GroupExpansionToggled(it)) },
            onGroupClipboardCopy = { viewModel.handleEvent(ActivityListEvent.GroupClipboardCopyRequested(it)) },
            onExportGroup = { viewModel.handleEvent(ActivityListEvent.GroupExportRequested(it)) },
            onToggleFavoritePackage = { viewModel.handleEvent(ActivityListEvent.PackageFavoriteToggled(it)) },
            onLaunch = { viewModel.handleEvent(ActivityListEvent.ActivityLaunched(it)) },
            onOpenGuidedLaunch = { viewModel.handleEvent(ActivityListEvent.GuidedLaunchOpened(it)) },
            onToggleFavorite = { viewModel.handleEvent(ActivityListEvent.FavoriteToggled(it)) },
            onLaunchGuided = { action, dataUri, mimeType ->
                viewModel.handleEvent(ActivityListEvent.GuidedLaunchConfirmed(action, dataUri, mimeType))
            },
            onDismissGuidedLaunch = { viewModel.handleEvent(ActivityListEvent.GuidedLaunchDismissed()) },
        )
    }

    ActivityListContent(
        state = state,
        padding = padding,
        searchRequestId = searchRequestId,
        callbacks = callbacks,
    )
}

private data class ActivityListCallbacks(
    val onRefresh: () -> Unit,
    val onSetFilter: (String, Boolean) -> Unit,
    val onReportRequested: (String) -> Unit,
    val onReportDismissed: () -> Unit,
    val onExportVisualReport: (String) -> Unit,
    val onClearError: () -> Unit,
    val onToggleExpanded: (String) -> Unit,
    val onGroupClipboardCopy: (String) -> Unit,
    val onExportGroup: (String) -> Unit,
    val onToggleFavoritePackage: (String) -> Unit,
    val onLaunch: (ActivityEntry) -> Unit,
    val onOpenGuidedLaunch: (ActivityEntry) -> Unit,
    val onToggleFavorite: (ActivityEntry) -> Unit,
    val onLaunchGuided: (String?, String?, String?) -> Unit,
    val onDismissGuidedLaunch: () -> Unit,
)

@Composable
private fun ActivityListContent(
    state: ActivityListState,
    padding: PaddingValues,
    searchRequestId: Long,
    callbacks: ActivityListCallbacks,
) {
    val strings = LocalStrings.current

    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val expandedActivities = remember { mutableStateMapOf<String, Boolean>() }
    val groups = state.groups
    val favoriteQueries = state.favoriteQueries
    val listState = rememberLazyListState()
    var searchToolsExpanded by remember { mutableStateOf(true) }
    LaunchedEffect(searchRequestId) {
        if (searchRequestId > 0) {
            searchToolsExpanded = true
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
                AppAccordion(
                    title = strings.filterActivities,
                    subtitle = strings.activityUi.searchToolsSubtitle,
                    meta = favoriteQueries.takeIf { it.isNotEmpty() }?.size?.toString(),
                    expanded = searchToolsExpanded,
                    onToggle = { searchToolsExpanded = !searchToolsExpanded },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SearchFieldWithHistoryComponent(
                            value = state.filter,
                            onValueChange = { callbacks.onSetFilter(it, false) },
                            onSubmit = { callbacks.onSetFilter(it, true) },
                            label = strings.filterActivities,
                            historyKey = "activities",
                            modifier = Modifier.fillMaxWidth(),
                            showClearAction = true,
                            onCleared = {
                                callbacks.onSetFilter("", false)
                                scope.launch { snackbarHostState.showSnackbar(strings.activityUi.suggestionsCleared) }
                            },
                            focusRequestId = searchRequestId,
                        )
                        state.topQuery?.let { top ->
                            AppText(
                                strings.activityUi.frequentSearch.format(top),
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
                                        onClick = { callbacks.onSetFilter(fav, true) },
                                        label = { AppText(fav.takeLast(24)) },
                                    )
                                }
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

            if (!state.loading && state.error == null) {
                item {
                    val totalActivities = state.activities.size
                    val currentActivities = groups.sumOf { it.activities.size }
                    AppText(
                        strings.activityUi.resultsSummary.format(currentActivities, totalActivities),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            state.selectedReport?.let { report ->
                item(key = "activity-report-${report.packageName}") {
                    ActivityReportCard(
                        report = report,
                        lastExportPath = state.reportExportPath,
                        onDismiss = callbacks.onReportDismissed,
                        onExport = { callbacks.onExportVisualReport(report.packageName) },
                    )
                }
            }

            if (!state.loading && groups.isEmpty()) {
                item {
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        val hasFilter = state.filter.isNotBlank()
                        AppText(
                            if (hasFilter) {
                                strings.activityUi.noResults.format(
                                    state.filter,
                                )
                            } else {
                                strings.activityUi.noActivities
                            },
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        AppText(
                            if (hasFilter) {
                                strings.activityUi.searchHint
                            } else {
                                strings.activityUi.refresh
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            if (hasFilter) {
                                AppButton(
                                    text = strings.activityUi.resetFilter,
                                    onClick = { callbacks.onSetFilter("", false) },
                                    variant = ButtonVariant.Secondary,
                                    modifier = Modifier.weight(1f),
                                    fullWidth = false,
                                )
                            }
                            AppButton(
                                text = strings.activityUi.refresh,
                                onClick = callbacks.onRefresh,
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
                    AppText(
                        err,
                        modifier = Modifier.padding(horizontal = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            items(groups, key = { it.packageName }) { group ->
                val expanded = state.expandedPackages.contains(group.packageName)
                val favPkg = state.favoritePackages.contains(group.packageName)
                AppAccordion(
                    title = group.label ?: group.packageName,
                    subtitle = group.packageName,
                    meta = strings.activityUi.activityCount.format(group.activities.size),
                    expanded = expanded,
                    onToggle = { callbacks.onToggleExpanded(group.packageName) },
                    titleMaxLines = 3,
                    subtitleMaxLines = 3,
                    textOverflow = TextOverflow.Clip,
                    headerVerticalPadding = 10.dp,
                    headerMinHeight = 88.dp,
                    actionsBelowHeader = true,
                    leading = {
                        PackageIcon(
                            packageName = group.packageName,
                            modifier = Modifier.size(44.dp),
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                if (state.selectedReport?.packageName == group.packageName) {
                                    callbacks.onReportDismissed()
                                } else {
                                    callbacks.onReportRequested(group.packageName)
                                }
                            },
                        ) {
                            Icon(
                                imageVector = AppIcons.Filled.Description,
                                contentDescription = strings.reportActivitiesContentDescription,
                                tint = if (state.selectedReport?.packageName == group.packageName) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                        IconButton(
                            onClick = {
                                callbacks.onGroupClipboardCopy(group.packageName)
                                callbacks.onExportGroup(group.packageName)
                            },
                        ) {
                            Icon(
                                imageVector = AppIcons.Filled.Share,
                                contentDescription = strings.shareActivitiesContentDescription,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        IconButton(onClick = { callbacks.onToggleFavoritePackage(group.packageName) }) {
                            Icon(
                                imageVector = if (favPkg) AppIcons.Filled.Star else AppIcons.Filled.StarBorder,
                                contentDescription = strings.favoriteContentDescription,
                                tint = if (favPkg) {
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
                    group.activities.forEach { activity ->
                        val favKey = activity.favoriteKey()
                        val isFav = state.favorites.contains(favKey)
                        val isActivityExpanded = expandedActivities[favKey] == true
                        val activityMeta = when {
                            !activity.launchableViaShell && activity.launchContextHint != null ->
                                strings.activityUi.intentRequired

                            !activity.launchableViaShell -> strings.activityUi.dumpOnly

                            activity.launchContextHint != null -> strings.activityUi.shellWithAction

                            else -> strings.activityUi.shellStart
                        }
                        AppAccordion(
                            title = activity.label,
                            subtitle = activity.activityName,
                            meta = activityMeta,
                            expanded = isActivityExpanded,
                            onToggle = { expandedActivities[favKey] = !isActivityExpanded },
                            titleMaxLines = 2,
                            subtitleMaxLines = 2,
                            textOverflow = TextOverflow.Ellipsis,
                            headerVerticalPadding = 6.dp,
                            actionsBelowHeader = false,
                            actions = {
                                if (!activity.launchableViaShell) {
                                    Icon(
                                        imageVector = AppIcons.Filled.Warning,
                                        contentDescription = strings.shellUnavailableContentDescription,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp).padding(end = 2.dp),
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        if (activity.launchableViaShell) {
                                            callbacks.onLaunch(activity)
                                        } else {
                                            callbacks.onOpenGuidedLaunch(activity)
                                        }
                                    },
                                    modifier = Modifier.size(36.dp),
                                ) {
                                    Icon(
                                        imageVector = AppIcons.Filled.PlayArrow,
                                        contentDescription = strings.activityUi.launch,
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                                IconButton(
                                    onClick = { callbacks.onToggleFavorite(activity) },
                                    modifier = Modifier.size(36.dp),
                                ) {
                                    Icon(
                                        imageVector = if (isFav) AppIcons.Filled.Star else AppIcons.Filled.StarBorder,
                                        contentDescription = strings.favoriteContentDescription,
                                        tint = if (isFav) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        ) {
                            ActivityDetailRow(
                                label = strings.activityUi.packageLabel,
                                value = activity.packageName,
                                maxLines = Int.MAX_VALUE,
                                overflow = TextOverflow.Clip,
                            )
                            ActivityDetailRow(
                                label = strings.activityUi.componentLabel,
                                value = activity.activityName,
                                maxLines = Int.MAX_VALUE,
                                overflow = TextOverflow.Clip,
                            )
                            ActivityDetailRow(
                                label = strings.activityUi.exportedLabel,
                                value = if (activity.exported) {
                                    strings.activityUi.yesLabel
                                } else {
                                    strings.activityUi.noLabel
                                },
                            )
                            ActivityDetailRow(
                                label = strings.activityUi.permissionLabel,
                                value = activity.permission ?: strings.activityUi.noneLabel,
                            )
                            ActivityDetailRow(
                                label = strings.activityUi.shellLaunchableLabel,
                                value = if (activity.launchableViaShell) {
                                    strings.activityUi.yesLabel
                                } else {
                                    strings.activityUi.noLabel
                                },
                            )
                            activity.launchMode?.let { mode ->
                                ActivityDetailRow(
                                    label = strings.activityUi.launchModeLabel,
                                    value = mode,
                                )
                            }
                            activity.targetActivity?.let { target ->
                                ActivityDetailRow(
                                    label = strings.activityUi.targetActivityLabel,
                                    value = target,
                                )
                            }
                            activity.launchabilityReason?.let { reason ->
                                ActivityDetailRow(
                                    label = strings.activityUi.constraintLabel,
                                    value = reason,
                                )
                            }
                            activity.launchContextHint?.let { hint ->
                                ActivityDetailRow(
                                    label = strings.activityUi.hintLabel,
                                    value = hint,
                                )
                            }
                            activity.launchIntentAction?.let { action ->
                                ActivityDetailRow(
                                    label = strings.activityUi.actionLabel,
                                    value = action,
                                )
                            }
                            activity.launchIntentData?.let { dataUri ->
                                ActivityDetailRow(
                                    label = strings.activityUi.dataUriLabel,
                                    value = dataUri,
                                )
                            }
                            activity.launchIntentMimeType?.let { mime ->
                                ActivityDetailRow(
                                    label = strings.activityUi.mimeTypeLabel,
                                    value = mime,
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                if (activity.launchableViaShell) {
                                    AppButton(
                                        text = strings.activityUi.launch,
                                        onClick = { callbacks.onLaunch(activity) },
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                                AppButton(
                                    text = strings.activityUi.guidedLauncherTitle,
                                    onClick = { callbacks.onOpenGuidedLaunch(activity) },
                                    variant = if (activity.launchableViaShell) {
                                        ButtonVariant.Secondary
                                    } else {
                                        ButtonVariant.Primary
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                AppButton(
                                    text = strings.activityUi.copyComponent,
                                    onClick = {
                                        clipboard.setText(AnnotatedString(activity.activityName))
                                        scope.launch {
                                            snackbarHostState.showSnackbar(strings.activityUi.componentCopied)
                                        }
                                    },
                                    variant = ButtonVariant.Tonal,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
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

        SnackbarHost(
            hostState = snackbarHostState,
            snackbar = { Snackbar(it) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp),
        )
    }

    state.error?.let {
        ErrorDialogComponent(message = it, onDismiss = callbacks.onClearError)
    }

    state.guidedLaunch?.entry?.let { activity ->
        GuidedActivityLaunchDialog(
            entry = activity,
            onDismiss = callbacks.onDismissGuidedLaunch,
            onLaunch = { action, dataUri, mimeType ->
                callbacks.onLaunchGuided(action, dataUri, mimeType)
            },
        )
    }
}

@Composable
private fun GuidedActivityLaunchDialog(
    entry: ActivityEntry,
    onDismiss: () -> Unit,
    onLaunch: (String?, String?, String?) -> Unit,
) {
    var action by remember(entry.activityName) { mutableStateOf(entry.launchIntentAction.orEmpty()) }
    var dataUri by remember(entry.activityName) { mutableStateOf(entry.launchIntentData.orEmpty()) }
    var mimeType by remember(entry.activityName) { mutableStateOf(entry.launchIntentMimeType.orEmpty()) }
    val canLaunch = action.isNotBlank() || dataUri.isNotBlank() || mimeType.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { AppText(LocalStrings.current.activityUi.guidedLauncherTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AppText(
                    text = entry.label,
                    style = MaterialTheme.typography.titleSmall,
                )
                AppText(
                    text = entry.activityName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                entry.launchContextHint?.let { hint ->
                    AppText(
                        text = LocalStrings.current.activityUi.resolver.format(hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                OutlinedTextField(
                    value = action,
                    onValueChange = { action = it },
                    label = { AppText(LocalStrings.current.activityUi.actionLabel) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = dataUri,
                    onValueChange = { dataUri = it },
                    label = { AppText(LocalStrings.current.activityUi.dataUriLabel) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = mimeType,
                    onValueChange = { mimeType = it },
                    label = { AppText(LocalStrings.current.activityUi.mimeTypeLabel) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onLaunch(
                        action.ifBlank { null },
                        dataUri.ifBlank { null },
                        mimeType.ifBlank { null },
                    )
                    onDismiss()
                },
                enabled = canLaunch,
            ) {
                AppText(LocalStrings.current.activityUi.launch)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                AppText(LocalStrings.current.activityUi.cancel)
            }
        },
    )
}

@Composable
private fun ActivityReportCard(
    report: ActivityAppReport,
    lastExportPath: String?,
    onDismiss: () -> Unit,
    onExport: () -> Unit,
) {
    val strings = LocalStrings.current
    val screenStrings = ScreenUiStrings.from(strings)
    val activityUi = strings.activityUi
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    AppText(
                        activityUi.reportTitle,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    AppText(
                        report.appLabel,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    AppText(
                        report.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                TextButton(onClick = onDismiss) {
                    AppText(activityUi.close)
                }
            }

            AppText(
                activityUi.totals.format(
                    report.totalActivities,
                    report.shellLaunchableCount,
                    report.guidedLaunchCount,
                    report.dumpOnlyCount,
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            AppText(
                activityUi.riskSummary.format(
                    report.contextualLaunchCount,
                    report.favoriteActivitiesCount,
                    report.highestRiskLevel.uiLabel(activityUi),
                    if (report.packageFavorite) activityUi.favoriteAppSuffix else "",
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (report.recommendations.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    AppText(
                        activityUi.recommendations,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    report.recommendations.forEach { recommendation ->
                        AppText(
                            screenStrings.bullet.format(recommendation),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                report.findings.forEach { finding ->
                    AppText(
                        screenStrings.bullet.format(finding),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (report.highlights.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    AppText(
                        activityUi.highlights,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    report.highlights.forEach { activity ->
                        val detail = when {
                            activity.entry.launchabilityReason != null -> activity.entry.launchabilityReason
                            activity.entry.launchContextHint != null -> activity.entry.launchContextHint
                            !activity.entry.launchIntentAction.isNullOrBlank() -> activity.entry.launchIntentAction
                            !activity.entry.launchIntentData.isNullOrBlank() -> activity.entry.launchIntentData
                            else -> activityUi.noAdditionalHint
                        }
                        AppText(
                            "• ${activity.entry.label} • ${activity.entry.activityName} • ${
                                activity.level.uiLabel(activityUi)
                            } • $detail",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AppButton(
                    text = activityUi.exportHtml,
                    onClick = onExport,
                    variant = ButtonVariant.Tonal,
                    modifier = Modifier.weight(1f),
                    fullWidth = false,
                )
                AppButton(
                    text = activityUi.closeReport,
                    onClick = onDismiss,
                    variant = ButtonVariant.Secondary,
                    modifier = Modifier.weight(1f),
                    fullWidth = false,
                )
            }

            lastExportPath?.let { path ->
                ExportDetailsCard(
                    path = path,
                    title = activityUi.exportedTitle,
                    summary = activityUi.exportedSummary,
                    details = listOf(
                        activityUi.appDetail.format(report.appLabel),
                        activityUi.packageDetail.format(report.packageName),
                        activityUi.activitiesDetail.format(report.totalActivities),
                        activityUi.maximumRiskDetail.format(report.highestRiskLevel.uiLabel(activityUi)),
                    ),
                )
            }
        }
    }
}

@AppScreenPreviews
@Composable
private fun ActivityListScreenPreview() {
    val packageName = "com.example.security.monitor"
    val mainActivity = ActivityEntry(
        label = "Security monitor main activity",
        appLabel = "Security Monitor",
        packageName = packageName,
        activityName = "$packageName.MainActivity",
        exported = true,
        launchMode = "standard",
    )
    val settingsActivity = ActivityEntry(
        label = "Settings and diagnostics activity with a long title",
        appLabel = "Security Monitor",
        packageName = packageName,
        activityName = "$packageName.SettingsActivity",
        launchableViaShell = false,
        launchabilityReason = "Requires app context",
        launchContextHint = "Open from the app",
        permission = "android.permission.BIND_QUICK_SETTINGS_TILE",
        launchMode = "singleTop",
    )
    val activities = listOf(mainActivity, settingsActivity)
    val callbacks = ActivityListCallbacks(
        onRefresh = {},
        onSetFilter = { _, _ -> },
        onReportRequested = {},
        onReportDismissed = {},
        onExportVisualReport = {},
        onClearError = {},
        onToggleExpanded = {},
        onGroupClipboardCopy = {},
        onExportGroup = {},
        onToggleFavoritePackage = {},
        onLaunch = {},
        onOpenGuidedLaunch = {},
        onToggleFavorite = {},
        onLaunchGuided = { _, _, _ -> },
        onDismissGuidedLaunch = {},
    )

    AppPreviewEnvironment {
        ActivityListContent(
            state = ActivityListState(
                activities = activities,
                topQuery = packageName,
                favorites = setOf(mainActivity.favoriteKey()),
                favoritePackages = setOf(packageName),
                expandedPackages = setOf(packageName),
                groups = listOf(ActivityGroup(packageName, "CrackMyDroid", activities)),
                favoriteQueries = listOf(packageName, "MainActivity"),
            ),
            padding = PaddingValues(),
            searchRequestId = 0,
            callbacks = callbacks,
        )
    }
}

@Composable
private fun ActivityDetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 3,
    overflow: TextOverflow = TextOverflow.Ellipsis,
) {
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
            maxLines = maxLines,
            overflow = overflow,
        )
    }
}
