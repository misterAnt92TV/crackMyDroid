package com.crackmydroid.ui.components.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.crackmydroid.ui.components.AppIcon
import com.crackmydroid.ui.components.LocalAccessibilityPrefs
import com.crackmydroid.ui.components.AccessibilityPrefs
import com.crackmydroid.ui.components.text.AppText
import com.crackmydroid.ui.icons.AppIcons
import com.crackmydroid.ui.preview.AppComponentPreviews
import com.crackmydroid.ui.preview.ComponentPreviewContainer
import com.crackmydroid.ui.strings.LocalUiStrings

enum class AppTopBarNavigationAction { Menu, Back, None }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    navigationAction: AppTopBarNavigationAction,
    onNavigationClick: () -> Unit,
    showSearch: Boolean,
    onSearchClick: () -> Unit,
    showHelp: Boolean,
    onHelpClick: () -> Unit,
    modifier: Modifier = Modifier,
    additionalActions: (@Composable RowScope.() -> Unit)? = null,
) {
    val prefs = LocalAccessibilityPrefs.current
    val uiStrings = LocalUiStrings.current
    val iconContainer = MaterialTheme.colorScheme.surfaceVariant
    val containerColor = MaterialTheme.colorScheme.surface
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
            AppText(
                title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = if (prefs.largeText) 2 else 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.semantics { heading() },
            )
        },
        navigationIcon = {
            when (navigationAction) {
                AppTopBarNavigationAction.Menu -> TopBarIconButton(onNavigationClick, iconContainer) {
                    AppIcon(AppIcons.Filled.Menu, contentDescription = uiStrings.menu)
                }

                AppTopBarNavigationAction.Back -> TopBarIconButton(onNavigationClick, iconContainer) {
                    AppIcon(AppIcons.AutoMirrored.Filled.ArrowBack, contentDescription = uiStrings.back)
                }

                AppTopBarNavigationAction.None -> Unit
            }
        },
        actions = {
            additionalActions?.invoke(this)
            if (showSearch) {
                TopBarIconButton(onSearchClick, iconContainer) {
                    AppIcon(AppIcons.Filled.Search, contentDescription = uiStrings.search)
                }
            }
            if (showHelp) {
                TopBarIconButton(onHelpClick, iconContainer) {
                    AppIcon(AppIcons.AutoMirrored.Filled.HelpOutline, contentDescription = uiStrings.help)
                }
            }
        },
    )
}

@Composable
private fun TopBarIconButton(
    onClick: () -> Unit,
    containerColor: androidx.compose.ui.graphics.Color,
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(containerColor),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

@AppComponentPreviews
@Composable
private fun AppTopBarPreview() = ComponentPreviewContainer {
    AppTopBar(
        title = "Analisi delle applicazioni installate con nome molto lungo",
        navigationAction = AppTopBarNavigationAction.Back,
        onNavigationClick = {},
        showSearch = true,
        onSearchClick = {},
        showHelp = true,
        onHelpClick = {},
    )
}

@AppComponentPreviews
@Composable
private fun AppTopBarLargeTextPreview() = ComponentPreviewContainer(
    accessibilityPrefs = AccessibilityPrefs(largeText = true),
) {
    AppTopBar(
        title = "Informazioni di sicurezza e configurazione del dispositivo",
        navigationAction = AppTopBarNavigationAction.Menu,
        onNavigationClick = {},
        showSearch = false,
        onSearchClick = {},
        showHelp = true,
        onHelpClick = {},
    )
}
