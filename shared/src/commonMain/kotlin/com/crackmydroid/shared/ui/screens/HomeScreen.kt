package com.crackmydroid.shared.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import com.crackmydroid.shared.i18n.LocalStrings
import com.crackmydroid.shared.presentation.components.AppAccordion
import com.crackmydroid.shared.presentation.components.AppCard
import com.crackmydroid.shared.presentation.components.AppText
import com.crackmydroid.shared.presentation.components.PackageIcon
import com.crackmydroid.shared.presentation.components.SectionHeader
import com.crackmydroid.shared.presentation.operations.OperationLogStore
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.font.FontWeight

@Composable
fun HomeScreen(
    padding: androidx.compose.foundation.layout.PaddingValues,
    headerHelp: String,
    onHelp: (String) -> Unit,
    onMenu: () -> Unit = {}
) {
    val strings = LocalStrings.current
    val history = OperationLogStore.entries.collectAsState()
    val uniqueHistory = history.value
        .distinctBy { it.description to it.success }
        .take(5)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
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
                            "Material look con palette Google per un flusso piu rapido e leggibile.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        item {
            AppText(
                "Ultime operazioni",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                fontWeight = FontWeight.SemiBold
            )
        }
        items(uniqueHistory) { entry ->
            val statusColor = if (entry.success) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
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
                    Column {
                        AppText(entry.description, style = MaterialTheme.typography.bodyMedium)
                        AppText(
                            entry.details ?: if (entry.success) "Successo" else "Fallita",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        item {
            AppText(strings.homePurposeTitle, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 12.dp))
        }
        item {
            AppText(
                strings.homePurposeBody,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        item {
            AppText(strings.faqTitle, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 12.dp))
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
    }
}
