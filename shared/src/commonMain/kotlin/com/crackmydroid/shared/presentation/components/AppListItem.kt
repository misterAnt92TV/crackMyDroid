package com.crackmydroid.shared.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.crackmydroid.shared.presentation.components.text.AppText

@Composable
fun AppListItem(
    title: String,
    subtitle: String? = null,
    meta: String? = null,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val prefs = LocalAccessibilityPrefs.current
    val rowModifier = modifier
        .fillMaxWidth()
        .padding(vertical = 6.dp, horizontal = 4.dp)
        .let { base ->
            if (onClick != null) {
                base.clickable(role = Role.Button, onClick = onClick)
            } else base
        }

    Row(
        modifier = rowModifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = if (prefs.largeText) Alignment.Top else Alignment.CenterVertically
    ) {
        leading?.let {
            Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) { it() }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            AppText(
                title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = if (prefs.largeText) 3 else 1,
                overflow = TextOverflow.Ellipsis
            )
            subtitle?.let {
                AppText(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (prefs.largeText) 3 else 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (meta != null && prefs.largeText) {
                AppText(
                    meta,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (meta != null && !prefs.largeText) {
            AppText(
                meta,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        trailing?.invoke()
    }
}