package com.crackmydroid.ui.components.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.crackmydroid.ui.components.AppIcon
import com.crackmydroid.ui.components.LocalAccessibilityPrefs
import com.crackmydroid.ui.components.AccessibilityPrefs
import com.crackmydroid.ui.components.text.AppText
import com.crackmydroid.ui.preview.AppComponentPreviews
import com.crackmydroid.ui.preview.ComponentPreviewContainer
import com.crackmydroid.ui.icons.AppIcons

data class AppBottomBarItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val selected: Boolean,
)

@Composable
fun AppBottomBar(
    items: List<AppBottomBarItem>,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val prefs = LocalAccessibilityPrefs.current
    NavigationBar(
        modifier = modifier,
        windowInsets = NavigationBarDefaults.windowInsets,
        tonalElevation = 0.dp,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = item.selected,
                onClick = { onItemClick(item.id) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                icon = {
                    AppIcon(
                        item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(20.dp),
                    )
                },
                label = {
                    AppText(
                        item.label,
                        maxLines = if (prefs.largeText) 2 else 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                alwaysShowLabel = true,
            )
        }
    }
}

@AppComponentPreviews
@Composable
private fun AppBottomBarPreview() = ComponentPreviewContainer {
    AppBottomBar(
        items = listOf(
            AppBottomBarItem("home", "Home", AppIcons.Rounded.Home, true),
            AppBottomBarItem("info", "Informazioni dispositivo", AppIcons.Rounded.Info, false),
            AppBottomBarItem("settings", "Impostazioni", AppIcons.Rounded.Settings, false),
        ),
        onItemClick = {},
    )
}

@AppComponentPreviews
@Composable
private fun AppBottomBarLargeTextPreview() = ComponentPreviewContainer(
    accessibilityPrefs = AccessibilityPrefs(largeText = true),
) {
    AppBottomBar(
        items = listOf(
            AppBottomBarItem("home", "Home", AppIcons.Rounded.Home, false),
            AppBottomBarItem("info", "Info dispositivo", AppIcons.Rounded.Info, true),
            AppBottomBarItem("settings", "Impostazioni", AppIcons.Rounded.Settings, false),
        ),
        onItemClick = {},
    )
}
