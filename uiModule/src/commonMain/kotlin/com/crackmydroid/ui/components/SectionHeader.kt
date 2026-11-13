package com.crackmydroid.ui.components
import com.crackmydroid.ui.icons.AppIcons

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.crackmydroid.ui.strings.LocalUiStrings
import com.crackmydroid.ui.components.text.AppText
import com.crackmydroid.ui.preview.AppComponentPreviews
import com.crackmydroid.ui.preview.ComponentPreviewContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionHeader(
    title: String,
    help: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = AppIcons.AutoMirrored.Filled.HelpOutline,
    onHelp: ((String) -> Unit)? = null,
    beforeHelpAction: (@Composable RowScope.() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    onMenu: (() -> Unit)? = null,
    titleContent: (@Composable () -> Unit)? = null,
) {
    val prefs = LocalAccessibilityPrefs.current
    val containerColor = MaterialTheme.colorScheme.surface
    val iconContainer = MaterialTheme.colorScheme.surfaceVariant
    CenterAlignedTopAppBar(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp)
            .clip(RoundedCornerShape(20.dp)),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            scrolledContainerColor = containerColor,
            navigationIconContentColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            val content = titleContent ?: {
                if (prefs.reduceMotion) {
                    AppText(
                        title,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = if (prefs.largeText) 2 else 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.semantics { heading() },
                    )
                } else {
                    Crossfade(targetState = title) {
                        AppText(
                            it,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = if (prefs.largeText) 2 else 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.semantics { heading() },
                        )
                    }
                }
            }
            content()
        },
        actions = {
            beforeHelpAction?.invoke(this)
            IconButton(
                onClick = { onHelp?.invoke(help) },
                enabled = onHelp != null,
                modifier = Modifier.size(48.dp),
                colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(iconContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    AppIcon(icon, contentDescription = LocalUiStrings.current.help)
                }
            }
        },
        navigationIcon = {
            when {
                onBack != null -> IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(48.dp),
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(iconContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        AppIcon(
                            AppIcons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = LocalUiStrings.current.back,
                        )
                    }
                }

                onMenu != null -> IconButton(
                    onClick = onMenu,
                    modifier = Modifier.size(48.dp),
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(iconContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        AppIcon(AppIcons.Filled.Menu, contentDescription = LocalUiStrings.current.menu)
                    }
                }
            }
        },
    )
}

@AppComponentPreviews
@Composable
private fun SectionHeaderPreview() = ComponentPreviewContainer {
    Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)) {
        SectionHeader(
            title = "Informazioni dispositivo",
            help = "Mostra i dettagli hardware e software.",
            onHelp = {},
            onMenu = {},
        )
        SectionHeader(
            title = "Dettagli analisi",
            help = "Mostra il risultato dell'analisi.",
            onHelp = {},
            onBack = {},
        )
    }
}
