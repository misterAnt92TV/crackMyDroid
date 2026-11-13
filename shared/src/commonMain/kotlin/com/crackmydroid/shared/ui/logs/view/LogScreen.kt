package com.crackmydroid.shared.ui.logs.view

import com.crackmydroid.architecture.presentation.model.UiLoadState
import com.crackmydroid.ui.typography.AppTypography
import com.crackmydroid.ui.icons.AppIcons

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crackmydroid.shared.i18n.LocalStrings
import com.crackmydroid.shared.i18n.ScreenUiStrings
import com.crackmydroid.shared.presentation.components.SearchPanelComponent
import com.crackmydroid.shared.presentation.preview.AppPreviewEnvironment
import com.crackmydroid.shared.presentation.preview.AppScreenPreviews
import com.crackmydroid.shared.ui.logs.LogEvent
import com.crackmydroid.shared.ui.logs.LogState
import com.crackmydroid.shared.ui.logs.LogViewModel
import com.crackmydroid.ui.components.AppAccordion
import com.crackmydroid.ui.components.AppIcon
import com.crackmydroid.ui.components.AppListSkeleton
import com.crackmydroid.ui.components.button.AppButton
import com.crackmydroid.ui.components.dialog.ErrorDialogComponent
import com.crackmydroid.ui.components.button.ScrollToTopButton
import com.crackmydroid.ui.components.text.AppText
import com.crackmydroid.ui.theme.CrackMyDroidTheme
import kotlinx.coroutines.launch

@Composable
fun LogScreen(
    viewModel: LogViewModel,
    padding: PaddingValues,
    searchRequestId: Long = 0,
    searchVisible: Boolean = true,
    onSearchVisibilityChanged: (Boolean) -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    LogScreenContent(
        state = state,
        padding = padding,
        searchRequestId = searchRequestId,
        searchVisible = searchVisible,
        onSearchVisibilityChanged = onSearchVisibilityChanged,
        onFilterChange = { viewModel.emitEvent(LogEvent.FilterChanged(it)) },
        onLevelToggle = { viewModel.emitEvent(LogEvent.LevelToggled(it)) },
        onRefresh = { viewModel.emitEvent(LogEvent.Refresh()) },
        onExport = { viewModel.emitEvent(LogEvent.Export()) },
        onColorByLevelChange = { viewModel.emitEvent(LogEvent.ColorByLevelChanged(it)) },
        onClearError = { viewModel.emitEvent(LogEvent.ErrorDismissed()) },
    )
}

@Composable
private fun LogScreenContent(
    state: LogState,
    padding: PaddingValues,
    searchRequestId: Long,
    searchVisible: Boolean = true,
    onSearchVisibilityChanged: (Boolean) -> Unit = {},
    onFilterChange: (String) -> Unit,
    onLevelToggle: (Char) -> Unit,
    onRefresh: () -> Unit,
    onExport: () -> Unit,
    onColorByLevelChange: (Boolean) -> Unit,
    onClearError: () -> Unit,
) {
    val strings = LocalStrings.current
    val screenStrings = ScreenUiStrings.from(strings)
    val fontSize = remember { mutableStateOf(13f) }
    val wrap = remember { mutableStateOf(true) }
    val logListState = rememberLazyListState()
    LaunchedEffect(searchRequestId) { if (searchRequestId > 0) logListState.animateScrollToItem(0) }
    val scope = rememberCoroutineScope()
    val showScrollTop by remember {
        derivedStateOf { logListState.firstVisibleItemIndex > 8 || logListState.firstVisibleItemScrollOffset > 400 }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            SearchPanelComponent(
                title = screenStrings.searchFilters,
                subtitle = strings.searchLog,
                expanded = searchVisible,
                onExpandedChange = onSearchVisibilityChanged,
                query = state.filter,
                onQueryChange = onFilterChange,
                onSubmit = onFilterChange,
                label = strings.searchLog,
                historyKey = "logs",
                focusRequestId = searchRequestId,
                showClearAction = true,
                onCleared = { onFilterChange("") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
            ) {
                    val levels = listOf('E', 'W', 'I', 'D', 'V')
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(levels) { lv ->
                            val lvlColor = levelColor(lv)
                            val chipColors = FilterChipDefaults.filterChipColors(
                                containerColor = lvlColor.copy(alpha = 0.18f),
                                selectedContainerColor = lvlColor.copy(alpha = 0.32f),
                                selectedLabelColor = lvlColor,
                                labelColor = lvlColor,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                            FilterChip(
                                selected = state.levels.contains(lv),
                                onClick = { onLevelToggle(lv) },
                                label = { AppText(lv.toString()) },
                                colors = chipColors,
                                shape = CircleShape,
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AppButton(
                            text = strings.refreshLog,
                            leadingIcon = AppIcons.Filled.Refresh,
                            onClick = onRefresh,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = onExport) {
                            AppIcon(
                                imageVector = AppIcons.Filled.Share,
                                contentDescription = strings.exportLog,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AppText(screenStrings.logWrapLines, style = MaterialTheme.typography.bodySmall)
                        Switch(checked = wrap.value, onCheckedChange = { wrap.value = it })
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AppText(screenStrings.logColorByLevel, style = MaterialTheme.typography.bodySmall)
                        Switch(
                            checked = state.colorByLevel,
                            onCheckedChange = onColorByLevelChange,
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AppText(screenStrings.logTextSize, style = MaterialTheme.typography.bodySmall)
                        Slider(
                            value = fontSize.value,
                            onValueChange = { fontSize.value = it },
                            valueRange = 10f..18f,
                            modifier = Modifier.weight(1f),
                        )
                        AppText(
                            "${fontSize.value.toInt()}sp",
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
            }

            if (state.loadState is UiLoadState.Loading && state.logs.isEmpty()) {
                AppListSkeleton(modifier = Modifier.fillMaxWidth())
            } else if (state.loadState is UiLoadState.Loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            state.message?.let {
                AppText(
                    it,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (state.trimmed) {
                AppText(
                    screenStrings.logTrimmed,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            state.errorMessage?.let {
                AppText(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            val horizontalScrollState = rememberScrollState()
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 2.dp,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(horizontalScrollState),
                ) {
                    LazyColumn(
                        state = logListState,
                        modifier = Modifier
                            .fillMaxHeight()
                            .wrapContentWidth(unbounded = true)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        itemsIndexed(
                            items = state.visibleLogs,
                            key = { index, _ -> index },
                        ) { _, line ->
                            val lvl = extractLevel(line)
                            val lineColor =
                                if (state.colorByLevel) levelColor(lvl) else MaterialTheme.colorScheme.onSurface
                            Text(
                                line,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = AppTypography.monospace,
                                    fontSize = fontSize.value.sp,
                                ),
                                softWrap = wrap.value,
                                color = lineColor,
                                modifier = Modifier.padding(vertical = 2.dp),
                            )
                        }
                        item { Spacer(modifier = Modifier.height(40.dp)) }
                    }
                }
            }
        }

        ScrollToTopButton(
            visible = showScrollTop,
            onClick = { scope.launch { logListState.animateScrollToItem(0) } },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
        )
    }

    state.errorMessage?.let { ErrorDialogComponent(it, onClearError) }
}

@AppScreenPreviews
@Composable
private fun LogScreenPreview() {
    val lines = listOf(
        "I/CrackMyDroid: Application initialized",
        "W/PackageManager: Optional package not found",
        "E/ActivityManager: Activity launch failed",
    )
    AppPreviewEnvironment {
        LogScreenContent(
            state = LogState(
                logs = lines,
                levels = setOf('E', 'W', 'I'),
                visibleLogs = lines,
            ),
            padding = PaddingValues(),
            searchRequestId = 0,
            onFilterChange = {},
            onLevelToggle = {},
            onRefresh = {},
            onExport = {},
            onColorByLevelChange = {},
            onClearError = {},
        )
    }
}

private fun extractLevel(line: String): Char? {
    // Typical logcat starts with "E/" "W/" etc.
    return line.firstOrNull()?.takeIf { it in setOf('E', 'W', 'I', 'D', 'V') }
}

@Composable
private fun levelColor(level: Char?): Color =
    CrackMyDroidTheme.logColors.colorFor(level, MaterialTheme.colorScheme.onBackground)
