package com.crackmydroid.shared.ui.home.view

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crackmydroid.shared.i18n.LocalStrings
import com.crackmydroid.shared.i18n.format
import com.crackmydroid.shared.ui.home.HomeEvent
import com.crackmydroid.shared.ui.home.HomeUiState
import com.crackmydroid.shared.ui.home.HomeViewModel
import com.crackmydroid.shared.ui.home.model.HomeUiStateMapper
import com.crackmydroid.shared.ui.screens.screenContentPadding
import com.crackmydroid.ui.components.AppAccordion
import com.crackmydroid.ui.components.PackageIcon
import com.crackmydroid.ui.components.button.ScrollToTopButton
import com.crackmydroid.ui.components.card.AppCard
import com.crackmydroid.ui.components.text.AppText
import com.crackmydroid.ui.theme.CrackMyDroidTheme
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    padding: PaddingValues,
    languageCode: String = "en",
) {
    val state = viewModel.state.collectAsState()
    val strings = LocalStrings.current
    val uiState = remember(state.value, strings, languageCode) {
        HomeUiStateMapper().map(state.value, strings, languageCode)
    }
    HomeContent(
        state = uiState,
        padding = padding,
        onHideRecent = { viewModel.handleEvent(HomeEvent.HideFromRecent(it)) },
        onRemove = { viewModel.handleEvent(HomeEvent.Remove(it)) },
    )
}

@Composable
internal fun HomeContent(
    state: HomeUiState,
    padding: PaddingValues,
    onHideRecent: (String) -> Unit = {},
    onRemove: (String) -> Unit = {},
    initiallyExpandedHistory: Boolean = false,
) {
    val strings = LocalStrings.current
    val historyExpanded = rememberSaveable { mutableStateOf(initiallyExpandedHistory) }
    val uniqueHistory = state.operationHistory
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val showScrollTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 1 || listState.firstVisibleItemScrollOffset > 240 }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = screenContentPadding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    borderColor = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(CrackMyDroidTheme.statusColors.successContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            PackageIcon(
                                packageName = "com.crackmydroid.android",
                                size = 42.dp,
                                backgroundColor = CrackMyDroidTheme.statusColors.successContainer,
                                contentPadding = 0.dp,
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            AppText(
                                "CrackMyDroid",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            AppText(
                                strings.homeDescription,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }
            }
            item {
                AppText(
                    strings.homeLatestOperations,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (uniqueHistory.isEmpty()) {
                item {
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        AppText(
                            strings.homeLatestOperationsEmpty,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                items(uniqueHistory) { entry ->
                    LastOperationItemComponent(
                        item = entry,
                        onAction = onHideRecent,
                        actionContentDescription = strings.hideRecentOperationContentDescription,
                    )
                }
            }
            item {
                AppText(
                    strings.homePurposeTitle,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
            item {
                AppText(
                    strings.homePurposeBody,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            item {
                AppText(
                    strings.faqTitle,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
            items(
                items = strings.faqItems,
                key = { (question, _) -> question },
            ) { faq ->
                val (q, a) = faq
                val expanded = rememberSaveable(q) { mutableStateOf(false) }
                AppAccordion(
                    title = q,
                    expanded = expanded.value,
                    onToggle = { expanded.value = !expanded.value },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    AppText(a, style = MaterialTheme.typography.bodySmall)
                }
            }
            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
            item {
                AppText(
                    if (uniqueHistory.isEmpty()) {
                        strings.homeOperationHistoryTitle
                    } else {
                        strings.homeOperationHistoryTitleWithCount.format(uniqueHistory.size)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                    fontWeight = FontWeight.SemiBold,
                )
            }
            item {
                AppAccordion(
                    title = null,
                    accessibilityLabel = strings.homeOperationHistory,
                    expanded = historyExpanded.value,
                    onToggle = { historyExpanded.value = !historyExpanded.value },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (uniqueHistory.isEmpty()) {
                        AppText(
                            strings.homeLatestOperationsEmpty,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            uniqueHistory.forEach { entry ->
                                LastOperationItemComponent(
                                    item = entry,
                                    onAction = onRemove,
                                    actionContentDescription = strings.removeOperationContentDescription,
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
    }
}
