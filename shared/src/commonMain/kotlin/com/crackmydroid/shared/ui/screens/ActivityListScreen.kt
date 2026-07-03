package com.crackmydroid.shared.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Snackbar
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.crackmydroid.shared.domain.model.ActivityEntry
import com.crackmydroid.shared.i18n.LocalStrings
import com.crackmydroid.shared.presentation.components.AppAccordion
import com.crackmydroid.shared.presentation.components.AppButton
import com.crackmydroid.shared.presentation.components.ButtonVariant
import com.crackmydroid.shared.presentation.components.AppCard
import com.crackmydroid.shared.presentation.components.AppListItem
import com.crackmydroid.shared.presentation.components.AppText
import com.crackmydroid.shared.presentation.components.ErrorDialog
import com.crackmydroid.shared.presentation.components.PackageIcon
import com.crackmydroid.shared.presentation.components.SectionHeader
import com.crackmydroid.shared.presentation.components.SearchFieldWithHistory
import com.crackmydroid.shared.presentation.components.ScrollToTopButton
import com.crackmydroid.shared.ui.activities.ActivityListViewModel

@Composable
fun ActivityListScreen(
    viewModel: ActivityListViewModel,
    padding: androidx.compose.foundation.layout.PaddingValues,
    headerHelp: String,
    onHelp: (String) -> Unit,
    onBack: (() -> Unit)? = null,
    onMenu: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val strings = LocalStrings.current
    val clipboard = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val groups = state.groups
    val favoriteQueries = remember(state.favorites) { state.favorites.toList().sorted() }
    val listState = rememberLazyListState()
    var searchToolsExpanded by remember { mutableStateOf(true) }
    var guidedLaunchTarget by remember { mutableStateOf<ActivityEntry?>(null) }
    val showScrollTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 1 || listState.firstVisibleItemScrollOffset > 240 }
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
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 28.dp)
        ) {
            item {
                SectionHeader(
                    title = strings.navActivities,
                    help = headerHelp,
                    onHelp = onHelp,
                    beforeHelpAction = {
                        IconButton(onClick = viewModel::refresh) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Aggiorna attività",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    onBack = onBack,
                    onMenu = onMenu
                )
            }
            item {
                AppAccordion(
                    title = strings.filterActivities,
                    subtitle = "Suggerimenti e filtri",
                    meta = favoriteQueries.takeIf { it.isNotEmpty() }?.size?.toString(),
                    expanded = searchToolsExpanded,
                    onToggle = { searchToolsExpanded = !searchToolsExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SearchFieldWithHistory(
                            value = state.filter,
                            onValueChange = { viewModel.setFilter(it) },
                            onSubmit = { viewModel.setFilter(it, trackUsage = true) },
                            label = strings.filterActivities,
                            historyKey = "activities",
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            showClearAction = true,
                            onCleared = {
                                viewModel.setFilter("")
                                scope.launch { snackbarHostState.showSnackbar("Suggerimenti svuotati") }
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
                                        label = { Text(fav.takeLast(24)) }
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
                        "Risultati: $currentActivities / $totalActivities attività",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!state.loading && groups.isEmpty()) {
                item {
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        val hasFilter = state.filter.isNotBlank()
                        AppText(
                            if (hasFilter) "Nessun risultato per \"${state.filter}\"." else "Nessuna activity disponibile.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        AppText(
                            if (hasFilter) "Prova con package, nome app o nome activity."
                            else "Aggiorna la lista per rileggere le activity installate.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (hasFilter) {
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
                    AppText(
                        err,
                        modifier = Modifier.padding(horizontal = 4.dp),
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                    )
                }
            }
            items(groups, key = { it.packageName }) { group ->
                val expanded = state.expandedPackages.contains(group.packageName)
                val favPkg = state.favoritePackages.contains(group.packageName)
                AppAccordion(
                    title = group.label ?: group.packageName,
                    subtitle = group.packageName,
                    meta = "${group.activities.size} act",
                    expanded = expanded,
                    onToggle = { viewModel.toggleExpanded(group.packageName) },
                    leading = { PackageIcon(packageName = group.packageName, modifier = Modifier.size(44.dp)) },
                    actions = {
                        IconButton(onClick = {
                            viewModel.groupContent(group.packageName)?.let { content ->
                                clipboard.setText(AnnotatedString(content))
                            }
                            viewModel.exportGroup(group.packageName)
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Condividi attività app",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { viewModel.toggleFavoritePackage(group.packageName) }) {
                            Icon(
                                imageVector = if (favPkg) Icons.Filled.Star else Icons.Filled.StarBorder,
                                contentDescription = "Preferito app",
                                tint = if (favPkg) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    group.activities.forEach { activity ->
                        val favKey = activity.packageName + activity.activityName
                        val activityMeta = when {
                            !activity.launchableViaShell && activity.launchContextHint != null -> "intent required"
                            !activity.launchableViaShell -> "dump only"
                            activity.launchContextHint != null -> "am start + action"
                            else -> "am start"
                        }
                        AppListItem(
                            title = activity.label,
                            subtitle = when {
                                activity.launchabilityReason != null -> "${activity.activityName} • ${activity.launchabilityReason}"
                                activity.launchContextHint != null -> "${activity.activityName} • ${activity.launchContextHint}"
                                else -> activity.activityName
                            },
                            meta = activityMeta,
                            onClick = when {
                                activity.launchableViaShell -> ({ viewModel.launch(activity) })
                                activity.supportsGuidedLaunch() -> ({ guidedLaunchTarget = activity })
                                else -> null
                            },
                            trailing = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (!activity.launchableViaShell) {
                                        Icon(
                                            imageVector = Icons.Filled.Warning,
                                            contentDescription = "Non avviabile via shell",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.padding(end = 4.dp)
                                        )
                                    }
                                    IconButton(onClick = { viewModel.toggleFavorite(activity) }) {
                                        Icon(
                                            imageVector = if (state.favorites.contains(favKey)) Icons.Filled.Star else Icons.Filled.StarBorder,
                                            contentDescription = "Preferito",
                                            tint = if (state.favorites.contains(favKey)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
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
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp)
        )
    }

    state.error?.let {
        ErrorDialog(message = it, onDismiss = viewModel::clearError)
    }

    guidedLaunchTarget?.let { activity ->
        GuidedActivityLaunchDialog(
            entry = activity,
            onDismiss = { guidedLaunchTarget = null },
            onLaunch = { action, dataUri, mimeType ->
                viewModel.launchWithIntent(activity, action, dataUri, mimeType)
            }
        )
    }
}

private fun ActivityEntry.supportsGuidedLaunch(): Boolean =
    !launchIntentAction.isNullOrBlank() ||
        !launchIntentData.isNullOrBlank() ||
        !launchIntentMimeType.isNullOrBlank()

@Composable
private fun GuidedActivityLaunchDialog(
    entry: ActivityEntry,
    onDismiss: () -> Unit,
    onLaunch: (String?, String?, String?) -> Unit
) {
    var action by remember(entry.activityName) { mutableStateOf(entry.launchIntentAction.orEmpty()) }
    var dataUri by remember(entry.activityName) { mutableStateOf(entry.launchIntentData.orEmpty()) }
    var mimeType by remember(entry.activityName) { mutableStateOf(entry.launchIntentMimeType.orEmpty()) }
    val canLaunch = action.isNotBlank() || dataUri.isNotBlank() || mimeType.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Launcher guidato") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AppText(
                    text = entry.label,
                    style = MaterialTheme.typography.titleSmall
                )
                AppText(
                    text = entry.activityName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                entry.launchContextHint?.let { hint ->
                    AppText(
                        text = "Resolver: $hint",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                OutlinedTextField(
                    value = action,
                    onValueChange = { action = it },
                    label = { Text("Action") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = dataUri,
                    onValueChange = { dataUri = it },
                    label = { Text("Data URI") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = mimeType,
                    onValueChange = { mimeType = it },
                    label = { Text("MIME type") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onLaunch(action.ifBlank { null }, dataUri.ifBlank { null }, mimeType.ifBlank { null })
                    onDismiss()
                },
                enabled = canLaunch
            ) {
                Text("Avvia")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}
