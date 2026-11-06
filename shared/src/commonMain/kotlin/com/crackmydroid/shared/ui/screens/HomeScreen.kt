package com.crackmydroid.shared.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crackmydroid.shared.i18n.LocalStrings
import com.crackmydroid.shared.presentation.components.AppAccordion
import com.crackmydroid.shared.presentation.components.PackageIcon
import com.crackmydroid.shared.presentation.components.SectionHeader
import com.crackmydroid.shared.presentation.components.card.AppCard
import com.crackmydroid.shared.presentation.components.text.AppText
import com.crackmydroid.shared.ui.home.HomeUiState
import com.crackmydroid.shared.ui.home.HomeUiStateMapper
import com.crackmydroid.shared.ui.home.HomeViewModel
import com.crackmydroid.shared.ui.home.LastOperationItemComponent

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    padding: androidx.compose.foundation.layout.PaddingValues,
    headerHelp: String,
    onHelp: (String) -> Unit,
    onMenu: () -> Unit = {},
    languageCode: String = "en"
) {
    val state = viewModel.state.collectAsState()
    val strings = LocalStrings.current
    val uiState = remember(state.value, strings, languageCode) {
        HomeUiStateMapper().map(state.value, strings, languageCode)
    }
    HomeContent(
        state = uiState,
        padding = padding,
        headerHelp = headerHelp,
        onHelp = onHelp,
        onMenu = onMenu,
        onHideRecent = viewModel::hideFromRecent,
        onRemove = viewModel::remove
    )
}

@Composable
internal fun HomeContent(
    state: HomeUiState,
    padding: androidx.compose.foundation.layout.PaddingValues,
    headerHelp: String,
    onHelp: (String) -> Unit,
    onMenu: () -> Unit = {},
    onHideRecent: (String) -> Unit = {},
    onRemove: (String) -> Unit = {},
    initiallyExpandedHistory: Boolean = false,
) {
    val strings = LocalStrings.current
    val historyExpanded = remember { mutableStateOf(initiallyExpandedHistory) }
    val uniqueHistory = state.operationHistory

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = screenContentPadding(padding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = strings.navHome,
                help = headerHelp,
                onHelp = onHelp,
                onMenu = onMenu
            )
        }
        item {
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.secondaryContainer
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        PackageIcon(
                            packageName = "com.crackmydroid.android",
                            size = 42.dp,
                            backgroundColor = Color.Transparent,
                            contentPadding = 0.dp
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AppText(
                            "CrackMyDroid",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        AppText(
                            strings.homeDescription,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                fontWeight = FontWeight.SemiBold
            )
        }
        if (uniqueHistory.isEmpty()) {
            item {
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    AppText(
                        strings.homeLatestOperationsEmpty,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(uniqueHistory) { entry ->
                LastOperationItemComponent(
                    item = entry,
                    onAction = onHideRecent,
                    actionContentDescription = strings.hideRecentOperationContentDescription
                )
            }
        }
        item {
            AppText(
                strings.homePurposeTitle,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
        item {
            AppText(
                strings.homePurposeBody,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        item {
            AppText(
                strings.faqTitle,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
        items(strings.faqItems) { faq ->
            val (q, a) = faq
            val expanded = remember { mutableStateOf(false) }
            AppAccordion(
                title = q,
                expanded = expanded.value,
                onToggle = { expanded.value = !expanded.value },
                modifier = Modifier.fillMaxWidth()
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
                fontWeight = FontWeight.SemiBold
            )
        }
        item {
            AppAccordion(
                title = null,
                accessibilityLabel = strings.homeOperationHistory,
                expanded = historyExpanded.value,
                onToggle = { historyExpanded.value = !historyExpanded.value },
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uniqueHistory.isEmpty()) {
                    AppText(
                        strings.homeLatestOperationsEmpty,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uniqueHistory.forEach { entry ->
                            LastOperationItemComponent(
                                item = entry,
                                onAction = onRemove,
                                actionContentDescription = strings.removeOperationContentDescription
                            )
                        }
                    }
                }
            }
        }
    }
}
