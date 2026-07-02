package com.crackmydroid.shared.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Snackbar
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.crackmydroid.shared.i18n.LocalStrings
import com.crackmydroid.shared.presentation.components.AppAccordion
import com.crackmydroid.shared.presentation.components.AppCard
import com.crackmydroid.shared.presentation.components.AppButton
import com.crackmydroid.shared.presentation.components.ButtonVariant
import com.crackmydroid.shared.presentation.components.AppText
import com.crackmydroid.shared.presentation.components.ErrorDialog
import com.crackmydroid.shared.presentation.components.ExportDetailsCard
import com.crackmydroid.shared.presentation.components.SectionHeader
import com.crackmydroid.shared.presentation.components.PackageIcon
import com.crackmydroid.shared.presentation.components.SearchFieldWithHistory
import com.crackmydroid.shared.presentation.components.ScrollToTopButton
import com.crackmydroid.shared.ui.installedapps.InstalledAppsViewModel

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
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val expandedPackages = remember { mutableStateMapOf<String, Boolean>() }
    val favoriteQueries = remember(state.favorites) { state.favorites.toList().sorted() }
    val filteredApps = remember(state.apps, state.filter, state.favorites) { viewModel.filtered() }
    val listState = rememberLazyListState()
    var searchToolsExpanded by remember { mutableStateOf(true) }
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
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 30.dp)
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
                                contentDescription = "Aggiorna lista app"
                            )
                        }
                        IconButton(
                            onClick = { viewModel.shareAll(state.topQuery ?: "apps") },
                            modifier = Modifier.size(36.dp),
                            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Condividi lista app"
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
                    subtitle = "Suggerimenti e filtri",
                    meta = favoriteQueries.takeIf { it.isNotEmpty() }?.size?.toString(),
                    expanded = searchToolsExpanded,
                    onToggle = { searchToolsExpanded = !searchToolsExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SearchFieldWithHistory(
                        value = state.filter,
                        onValueChange = { viewModel.setFilter(it) },
                        onSubmit = { viewModel.setFilter(it, trackUsage = true) },
                        label = strings.apkSearch,
                        historyKey = "installed_apps",
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
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp)
                        ) {
                            items(favoriteQueries, key = { it }) { fav ->
                                @OptIn(ExperimentalFoundationApi::class)
                                FilterChip(
                                    selected = true,
                                    onClick = { viewModel.setFilter(fav, trackUsage = true) },
                                    label = { Text(fav) },
                                    modifier = Modifier.combinedClickable(
                                        onClick = { viewModel.setFilter(fav, trackUsage = true) },
                                        onLongClick = {
                                            viewModel.toggleFavorite(fav)
                                            scope.launch { snackbarHostState.showSnackbar("Rimosso dai preferiti") }
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
                        summary = "Esportazione completata. Puoi usare il file per backup o condivisione tecnica.",
                        details = listOf(
                            "Contenuto: lista applicazioni installate (label, package, path APK).",
                            "Stato: pronto per invio."
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
                        "Risultati: ${filteredApps.size} / ${state.apps.size}",
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
                            if (hasFilter) "Nessun risultato per \"${state.filter}\"." else "Nessuna app disponibile al momento.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        AppText(
                            if (hasFilter) "Rimuovi o cambia filtro per vedere altre app."
                            else "Prova ad aggiornare la lista per rileggere le app installate.",
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

            items(filteredApps, key = { it.packageName }) { app ->
                val expanded = expandedPackages[app.packageName] ?: false
                val isFavorite = state.favorites.contains(app.packageName)
                AppAccordion(
                    title = app.appLabel,
                    subtitle = app.packageName,
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
                                contentDescription = "Condividi APK",
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
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AppText(
                        "${strings.apkPath}: ${app.sourcePath}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
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

    state.error?.let { ErrorDialog(it, viewModel::clearError) }
}
