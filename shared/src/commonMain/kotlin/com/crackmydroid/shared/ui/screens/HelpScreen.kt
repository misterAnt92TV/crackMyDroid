package com.crackmydroid.shared.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.crackmydroid.shared.i18n.LocalStrings
import com.crackmydroid.shared.i18n.Strings
import com.crackmydroid.shared.presentation.components.AppCard
import com.crackmydroid.shared.presentation.components.AppText
import com.crackmydroid.shared.presentation.components.SectionHeader
import com.crackmydroid.shared.presentation.navigation.Screen

@Composable
fun HelpScreen(
    padding: androidx.compose.foundation.layout.PaddingValues,
    onHelp: (String) -> Unit,
    onMenu: () -> Unit = {},
    onNavigate: (Screen) -> Unit
) {
    HelpContent(
        padding = padding,
        onHelp = onHelp,
        onMenu = onMenu,
        onNavigate = onNavigate
    )
}

@Composable
internal fun HelpContent(
    padding: androidx.compose.foundation.layout.PaddingValues,
    onHelp: (String) -> Unit,
    onMenu: () -> Unit = {},
    onNavigate: (Screen) -> Unit
) {
    val strings = LocalStrings.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = screenContentPadding(padding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = strings.navHelp,
                help = strings.helpOverview,
                onHelp = onHelp,
                onMenu = onMenu
            )
        }
        item {
            AppText(
                text = strings.helpOverview,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            )
        }
        items(helpScreens) { screen ->
            HelpNavigationItem(
                title = screen.title(strings),
                summary = sectionSummary(strings, screen),
                icon = screen.icon,
                onClick = { onNavigate(screen) }
            )
        }
    }
}

@Composable
private fun HelpNavigationItem(
    title: String,
    summary: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp),
        onClick = onClick,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 2.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AppText(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                AppText(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

private val helpScreens = listOf(
    Screen.Home,
    Screen.InstalledApps,
    Screen.Pentest,
    Screen.Permissions,
    Screen.Activities,
    Screen.Root,
    Screen.Trick,
    Screen.Log,
    Screen.Info,
    Screen.Settings
)

private fun sectionSummary(strings: Strings, screen: Screen): String = when (screen) {
    Screen.Home -> strings.helpHome
    Screen.InstalledApps -> strings.helpApk
    Screen.Pentest -> strings.helpPentest
    Screen.Permissions -> strings.helpPermissions
    Screen.Activities -> strings.helpActivities
    Screen.Root -> strings.helpRoot
    Screen.Trick -> strings.helpTrick
    Screen.Log -> strings.helpLog
    Screen.Info -> strings.helpInfo
    Screen.Settings -> strings.helpSettings
    Screen.Help -> strings.helpOverview
}
