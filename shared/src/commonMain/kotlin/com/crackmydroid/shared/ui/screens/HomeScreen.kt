package com.crackmydroid.shared.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.crackmydroid.shared.domain.model.operation.OperationEntry
import com.crackmydroid.shared.domain.repository.log.OperationLogRepository
import com.crackmydroid.shared.i18n.LocalStrings
import com.crackmydroid.shared.i18n.formatOperationTimestamp
import com.crackmydroid.shared.i18n.renderOperation
import com.crackmydroid.shared.presentation.components.AppAccordion
import com.crackmydroid.shared.presentation.components.PackageIcon
import com.crackmydroid.shared.presentation.components.SectionHeader
import com.crackmydroid.shared.presentation.components.card.AppCard
import com.crackmydroid.shared.presentation.components.text.AppText

@Composable
fun HomeScreen(
    operationLogs: OperationLogRepository,
    padding: androidx.compose.foundation.layout.PaddingValues,
    headerHelp: String,
    onHelp: (String) -> Unit,
    onMenu: () -> Unit = {},
    languageCode: String = "en"
) {
    val history = operationLogs.entries.collectAsState()
    HomeContent(
        history = history.value,
        padding = padding,
        headerHelp = headerHelp,
        onHelp = onHelp,
        onMenu = onMenu,
        onHideRecent = operationLogs::hideFromRecent,
        onRemove = operationLogs::remove,
        languageCode = languageCode
    )
}

@Composable
internal fun HomeContent(
    history: List<OperationEntry>,
    padding: androidx.compose.foundation.layout.PaddingValues,
    headerHelp: String,
    onHelp: (String) -> Unit,
    onMenu: () -> Unit = {},
    onHideRecent: (OperationEntry) -> Unit = {},
    onRemove: (OperationEntry) -> Unit = {},
    initiallyExpandedHistory: Boolean = false,
    languageCode: String = "en"
) {
    val strings = LocalStrings.current
    val historyExpanded = remember { mutableStateOf(initiallyExpandedHistory) }
    val uniqueHistory = history
        .filterNot(OperationEntry::hiddenFromRecent)
        .distinctBy { it.description to it.success }
        .take(5)

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
                OperationHistoryEntryComponent(
                    entry = entry,
                    onAction = onHideRecent,
                    actionContentDescription = strings.hideRecentOperationContentDescription,
                    languageCode = languageCode
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
                if (history.isEmpty()) {
                    strings.homeOperationHistoryTitle
                } else {
                    strings.homeOperationHistoryTitleWithCount.format(history.size)
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
                if (history.isEmpty()) {
                    AppText(
                        strings.homeLatestOperationsEmpty,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        history.forEach { entry ->
                            OperationHistoryEntryComponent(
                                entry = entry,
                                onAction = onRemove,
                                actionContentDescription = strings.removeOperationContentDescription,
                                languageCode = languageCode
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OperationHistoryEntryComponent(
    entry: OperationEntry,
    onAction: (OperationEntry) -> Unit,
    actionContentDescription: String,
    languageCode: String = "en"
) {
    val strings = LocalStrings.current
    val operationDescription = entry.event?.let(strings::renderOperation)
        ?: entry.legacyDescription
        ?: entry.description
    val operationResult = entry.details
        ?: if (entry.success) strings.operationSuccess else strings.operationFailed
    val statusColor = if (entry.success) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.error
    }

    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (entry.success) Icons.Filled.CheckCircle else Icons.Filled.Error,
                    contentDescription = null,
                    tint = statusColor
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                AppText(operationDescription, style = MaterialTheme.typography.bodyMedium)
                AppText(
                    strings.operationResultLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AppText(
                    operationResult,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AppText(
                    formatOperationTimestamp(entry.timestamp, languageCode),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = { onAction(entry) },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = actionContentDescription.format(operationDescription),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
