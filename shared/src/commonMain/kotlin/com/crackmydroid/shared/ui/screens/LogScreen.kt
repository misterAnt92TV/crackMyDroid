package com.crackmydroid.shared.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Switch
import androidx.compose.material3.Slider
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import com.crackmydroid.shared.i18n.LocalStrings
import com.crackmydroid.shared.presentation.components.AppAccordion
import com.crackmydroid.shared.presentation.components.AppButton
import com.crackmydroid.shared.presentation.components.AppText
import com.crackmydroid.shared.presentation.components.ErrorDialog
import com.crackmydroid.shared.presentation.components.SearchFieldWithHistory
import com.crackmydroid.shared.presentation.components.SectionHeader
import com.crackmydroid.shared.presentation.components.ScrollToTopButton
import com.crackmydroid.shared.ui.logs.LogViewModel
import kotlinx.coroutines.launch

@Composable
fun LogScreen(
    viewModel: LogViewModel,
    padding: androidx.compose.foundation.layout.PaddingValues,
    headerHelp: String,
    onHelp: (String) -> Unit,
    onBack: (() -> Unit)? = null,
    onMenu: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val strings = LocalStrings.current
    val fontSize = remember { mutableStateOf(13f) }
    val wrap = remember { mutableStateOf(true) }
    var optionsExpanded by remember { mutableStateOf(true) }
    val logListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val showScrollTop by remember {
        derivedStateOf { logListState.firstVisibleItemIndex > 8 || logListState.firstVisibleItemScrollOffset > 400 }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            SectionHeader(
                title = strings.navLog,
                help = headerHelp,
                onHelp = onHelp,
                onBack = onBack,
                onMenu = onMenu
            )
            AppAccordion(
                title = "Ricerca e filtri log",
                subtitle = strings.searchLog,
                expanded = optionsExpanded,
                onToggle = { optionsExpanded = !optionsExpanded },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SearchFieldWithHistory(
                        value = state.filter,
                        onValueChange = viewModel::setFilter,
                        onSubmit = viewModel::setFilter,
                        label = strings.searchLog,
                        historyKey = "logs",
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        showClearAction = true,
                        onCleared = {
                            viewModel.setFilter("")
                        }
                    )
                    val levels = listOf('E', 'W', 'I', 'D', 'V')
                    androidx.compose.foundation.lazy.LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(levels) { lv ->
                            val lvlColor = levelColor(lv)
                            val chipColors = FilterChipDefaults.filterChipColors(
                                containerColor = lvlColor.copy(alpha = 0.18f),
                                selectedContainerColor = lvlColor.copy(alpha = 0.32f),
                                selectedLabelColor = lvlColor,
                                labelColor = lvlColor,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            FilterChip(
                                selected = state.levels.contains(lv),
                                onClick = { viewModel.toggleLevel(lv) },
                                label = { Text(lv.toString()) },
                                colors = chipColors,
                                shape = CircleShape
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        AppButton(
                            text = strings.refreshLog,
                            leadingIcon = Icons.Filled.Refresh,
                            onClick = { viewModel.refresh() },
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.export() }) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = strings.exportLog,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        AppText("Avvolgi righe", style = MaterialTheme.typography.bodySmall)
                        Switch(checked = wrap.value, onCheckedChange = { wrap.value = it })
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        AppText("Colora per livello", style = MaterialTheme.typography.bodySmall)
                        Switch(
                            checked = state.colorByLevel,
                            onCheckedChange = { viewModel.setColorByLevel(it) }
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        AppText("Dimensione testo", style = MaterialTheme.typography.bodySmall)
                        Slider(
                            value = fontSize.value,
                            onValueChange = { fontSize.value = it },
                            valueRange = 10f..18f,
                            modifier = Modifier.weight(1f)
                        )
                        AppText("${fontSize.value.toInt()}sp", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            if (state.loading) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 12.dp))
            }

            state.message?.let {
                AppText(it, color = androidx.compose.material3.MaterialTheme.colorScheme.primary, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
            }
            if (state.trimmed) {
                AppText("Mostro solo le ultime 1500 righe del log per prestazioni.", style = androidx.compose.material3.MaterialTheme.typography.bodySmall, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
            }

            state.error?.let {
                AppText(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
            }

            val horizontalScrollState = rememberScrollState()
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 2.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(horizontalScrollState)
                ) {
                    LazyColumn(
                        state = logListState,
                        modifier = Modifier
                            .fillMaxHeight()
                            .wrapContentWidth(unbounded = true)
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        itemsIndexed(
                            items = state.visibleLogs,
                            key = { index, _ -> index }
                        ) { _, line ->
                            val lvl = extractLevel(line)
                            val lineColor = if (state.colorByLevel) levelColor(lvl) else MaterialTheme.colorScheme.onSurface
                            Text(
                                line,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = fontSize.value.sp
                                ),
                                softWrap = wrap.value,
                                color = lineColor,
                                modifier = Modifier.padding(vertical = 2.dp)
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
                .align(androidx.compose.ui.Alignment.BottomEnd)
                .padding(12.dp)
        )
    }

    state.error?.let { ErrorDialog(it, viewModel::clearError) }
}

private fun extractLevel(line: String): Char? {
    // Typical logcat starts with "E/" "W/" etc.
    return line.firstOrNull()?.takeIf { it in setOf('E', 'W', 'I', 'D', 'V') }
}

@Composable
private fun levelColor(level: Char?): Color {
    return when (level) {
        'E' -> Color(0xFFFF6B6B)
        'W' -> Color(0xFFF4A261)
        'I' -> Color(0xFF2A9D8F)
        'D' -> Color(0xFF4D6DE3)
        'V' -> Color(0xFF9AA0A6)
        else -> MaterialTheme.colorScheme.onBackground
    }
}
