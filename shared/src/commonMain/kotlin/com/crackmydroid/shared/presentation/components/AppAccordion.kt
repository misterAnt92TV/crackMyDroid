package com.crackmydroid.shared.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.crackmydroid.shared.presentation.components.card.AppCard
import com.crackmydroid.shared.presentation.components.text.AppText
import com.crackmydroid.shared.presentation.preview.AppComponentPreviews
import com.crackmydroid.shared.presentation.preview.ComponentPreviewContainer

@Composable
fun AppAccordion(
    title: String,
    subtitle: String? = null,
    meta: String? = null,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    leading: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable () -> Unit
) {
    val prefs = LocalAccessibilityPrefs.current
    val reduceMotion = LocalAccessibilityPrefs.current.reduceMotion
    AppCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button, onClick = onToggle)
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = if (prefs.largeText) Alignment.Top else Alignment.CenterVertically
        ) {
            leading?.let {
                Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) { it() }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = if (prefs.largeText) Alignment.Top else Alignment.CenterVertically
            ) {
                actions()
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = if (reduceMotion) fadeIn() else fadeIn(
                animationSpec = androidx.compose.animation.core.tween(
                    220
                )
            ) + slideInVertically { it / 8 },
            exit = fadeOut()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                androidx.compose.material3.HorizontalDivider()
                Spacer(modifier = Modifier.height(4.dp))
                content()
            }
        }
    }
}

@AppComponentPreviews
@Composable
private fun AppAccordionPreview() = ComponentPreviewContainer {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AppAccordion(
            title = "Controlli di sicurezza",
            subtitle = "Verifica configurazioni e permessi",
            meta = "3 risultati",
            expanded = false,
            onToggle = {},
            leading = { Icon(Icons.Filled.Info, contentDescription = null) }
        ) { AppText("Contenuto non visibile") }}
        AppAccordion(
            title = "Controlli di sicurezza",
            subtitle = "Verifica configurazioni e permessi",
            meta = "3 risultati",
            expanded = true,
            onToggle = {},
            leading = { Icon(Icons.Filled.Info, contentDescription = null) }
        ) { AppText("Il dispositivo non espone backup, debug o permessi inattesi.") }

}
