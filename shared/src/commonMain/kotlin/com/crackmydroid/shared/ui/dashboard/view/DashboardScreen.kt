package com.crackmydroid.shared.ui.dashboard.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crackmydroid.shared.i18n.LocalStrings
import com.crackmydroid.shared.presentation.navigation.Screen
import com.crackmydroid.shared.presentation.preview.AppPreviewEnvironment
import com.crackmydroid.shared.presentation.preview.AppScreenPreviews
import com.crackmydroid.shared.ui.screens.screenContentPadding
import com.crackmydroid.ui.components.AppIcon
import com.crackmydroid.ui.components.card.AppCard
import com.crackmydroid.ui.components.text.AppText
import com.crackmydroid.ui.icons.AppIcons
import com.crackmydroid.ui.theme.CrackMyDroidTheme

private data class DashboardAction(
    val screen: Screen,
    val title: String,
    val description: String,
    val icon: ImageVector,
)

private data class DashboardActionColors(
    val container: Color,
    val content: Color,
    val accent: Color,
)

@Composable
fun DashboardScreen(padding: PaddingValues, onNavigate: (Screen) -> Unit) {
    DashboardContent(padding = padding, onNavigate = onNavigate)
}

@Composable
internal fun DashboardContent(padding: PaddingValues, onNavigate: (Screen) -> Unit) {
    val strings = LocalStrings.current
    val actions = listOf(
        DashboardAction(Screen.InstalledApps, strings.navApk, strings.dashboardInstalledAppsDescription, AppIcons.Rounded.Apps),
        DashboardAction(Screen.AnalyzeApkContent, strings.navAnalyzeApkContent, strings.dashboardAnalyzeApkDescription, AppIcons.Rounded.Analyze),
        DashboardAction(Screen.Permissions, strings.navPermissions, strings.dashboardPermissionsDescription, AppIcons.Rounded.PrivacyTip),
        DashboardAction(Screen.Pentest, strings.navPentest, strings.dashboardPenTestDescription, AppIcons.Rounded.Security),
    )
    val spacing = CrackMyDroidTheme.spacing

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (maxWidth >= 600.dp) 2 else 1),
            modifier = Modifier.fillMaxSize(),
            contentPadding = screenContentPadding(padding),
            verticalArrangement = Arrangement.spacedBy(spacing.itemSpacing),
            horizontalArrangement = Arrangement.spacedBy(spacing.itemSpacing),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) { DashboardHero() }
            item(span = { GridItemSpan(maxLineSpan) }) {
                AppText(
                    text = strings.navSectionAnalyzeApps,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = spacing.small),
                )
            }
            itemsIndexed(actions, key = { _, action -> action.screen.id }) { index, action ->
                DashboardActionCard(action, actionColors(index), onClick = { onNavigate(action.screen) })
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                GettingStartedCard(strings.dashboardGettingStartedTitle, strings.dashboardGettingStartedSteps)
            }
        }
    }
}

@Composable
private fun DashboardHero() {
    val strings = LocalStrings.current
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        borderColor = MaterialTheme.colorScheme.primary,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CrackMyDroidTheme.spacing.medium),
        ) {
            DashboardIconBadge(AppIcons.Rounded.Security, MaterialTheme.colorScheme.primary)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                AppText(
                    text = strings.dashboardHeroEyebrow,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                AppText(
                    text = strings.dashboardHeroTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                AppText(
                    text = strings.dashboardHeroDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
private fun DashboardActionCard(action: DashboardAction, colors: DashboardActionColors, onClick: () -> Unit) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        containerColor = colors.container,
        borderColor = colors.accent,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CrackMyDroidTheme.spacing.medium),
        ) {
            DashboardIconBadge(action.icon, colors.accent)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                AppText(
                    text = action.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.content,
                )
                AppText(text = action.description, style = MaterialTheme.typography.bodySmall, color = colors.content)
            }
            AppIcon(
                imageVector = AppIcons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = colors.content,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun GettingStartedCard(title: String, steps: List<String>) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        borderColor = MaterialTheme.colorScheme.outlineVariant,
    ) {
        AppText(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Column(
            modifier = Modifier.padding(top = CrackMyDroidTheme.spacing.small),
            verticalArrangement = Arrangement.spacedBy(CrackMyDroidTheme.spacing.small),
        ) {
            steps.forEachIndexed { index, step ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppText(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.width(24.dp),
                    )
                    AppText(step, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun DashboardIconBadge(icon: ImageVector, color: Color) {
    Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
        AppIcon(icon, contentDescription = null, tint = color, modifier = Modifier.size(32.dp))
    }
}

@Composable
private fun actionColors(index: Int): DashboardActionColors = when (index % 3) {
    0 -> DashboardActionColors(
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.onPrimaryContainer,
        MaterialTheme.colorScheme.primary,
    )
    1 -> DashboardActionColors(
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.onSecondaryContainer,
        MaterialTheme.colorScheme.secondary,
    )
    else -> DashboardActionColors(
        MaterialTheme.colorScheme.tertiaryContainer,
        MaterialTheme.colorScheme.onTertiaryContainer,
        MaterialTheme.colorScheme.tertiary,
    )
}

@AppScreenPreviews
@Composable
private fun DashboardScreenPreview() = AppPreviewEnvironment {
    DashboardContent(padding = PaddingValues(), onNavigate = {})
}

@Preview(name = "Dashboard 7-inch", widthDp = 600, heightDp = 960, showBackground = true)
@Composable
private fun DashboardSevenInchPreview() = AppPreviewEnvironment {
    DashboardContent(padding = PaddingValues(), onNavigate = {})
}
