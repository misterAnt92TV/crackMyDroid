package com.crackmydroid.shared.ui.home.view
import com.crackmydroid.ui.icons.AppIcons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crackmydroid.shared.i18n.LocalStrings
import com.crackmydroid.shared.i18n.format
import com.crackmydroid.shared.ui.home.LastOperationItem
import com.crackmydroid.shared.ui.home.OperationOutcome
import com.crackmydroid.ui.components.card.AppCard
import com.crackmydroid.ui.components.text.AppText
import com.crackmydroid.ui.preview.AppComponentPreviews
import com.crackmydroid.ui.preview.ComponentPreviewContainer

@Composable
fun LastOperationItemComponent(
    item: LastOperationItem,
    actionContentDescription: String,
    onAction: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val statusColor = when (item.outcome) {
        OperationOutcome.Success -> MaterialTheme.colorScheme.secondary
        OperationOutcome.Failure -> MaterialTheme.colorScheme.error
    }
    AppCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (item.outcome == OperationOutcome.Success) {
                        AppIcons.Filled.CheckCircle
                    } else {
                        AppIcons.Filled.Error
                    },
                    contentDescription = null,
                    tint = statusColor,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                AppText(item.description, style = MaterialTheme.typography.bodyMedium)
                item.result?.takeIf(String::isNotEmpty)?.let { result ->
                    Row {
                        AppText(
                            LocalStrings.current.operationResultLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.width(4.dp))
                        AppText(
                            result,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                AppText(
                    item.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = { onAction(item.operationId) }, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = AppIcons.Filled.Close,
                    contentDescription = actionContentDescription.format(item.description),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@AppComponentPreviews
@Composable
private fun LastOperationItemSuccessPreview() = ComponentPreviewContainer {
    LastOperationItemComponent(
        item = LastOperationItem(
            operationId = "preview-success",
            description = "Scansione APK completata",
            result = "42 applicazioni",
            timestamp = "Oggi, 10:30",
            outcome = OperationOutcome.Success,
        ),
        actionContentDescription = LocalStrings.current.hideRecentOperationContentDescription,
        onAction = {},
    )
}

@AppComponentPreviews
@Composable
private fun LastOperationItemFailurePreview() = ComponentPreviewContainer {
    LastOperationItemComponent(
        item = LastOperationItem(
            operationId = "preview-failure",
            description = "Avvio activity",
            result = "Activity non esportata",
            timestamp = "Ieri, 18:05",
            outcome = OperationOutcome.Failure,
        ),
        actionContentDescription = LocalStrings.current.hideRecentOperationContentDescription,
        onAction = {},
    )
}

@AppComponentPreviews
@Composable
private fun LastOperationItemWithoutResultPreview() = ComponentPreviewContainer {
    LastOperationItemComponent(
        item = LastOperationItem(
            operationId = "preview-without-result",
            description = "Aggiornamento completato",
            result = "",
            timestamp = "Adesso",
            outcome = OperationOutcome.Success,
        ),
        actionContentDescription = LocalStrings.current.hideRecentOperationContentDescription,
        onAction = {},
    )
}
