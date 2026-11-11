package com.crackmydroid.shared.ui.screens
import com.crackmydroid.ui.icons.AppIcons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.crackmydroid.shared.i18n.LocalStrings
import com.crackmydroid.shared.presentation.navigation.Screen
import com.crackmydroid.ui.components.button.AppButton
import com.crackmydroid.ui.components.SectionHeader
import com.crackmydroid.ui.components.card.CardBlock

@Composable
fun MoreScreen(
    padding: androidx.compose.foundation.layout.PaddingValues,
    onNavigate: (Screen) -> Unit,
    headerHelp: String,
    onHelp: (String) -> Unit,
) {
    MoreContent(
        padding = padding,
        onNavigate = onNavigate,
        headerHelp = headerHelp,
        onHelp = onHelp,
    )
}

@Composable
internal fun MoreContent(
    padding: androidx.compose.foundation.layout.PaddingValues,
    onNavigate: (Screen) -> Unit,
    headerHelp: String,
    onHelp: (String) -> Unit,
) {
    val strings = LocalStrings.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionHeader(title = strings.navMore, help = headerHelp, onHelp = onHelp)
        CardBlock(title = strings.navMore) {
            AppButton(
                text = strings.navActivities,
                leadingIcon = AppIcons.AutoMirrored.Filled.ListAlt,
                onClick = { onNavigate(Screen.Activities) },
            )
            AppButton(
                text = strings.navRoot,
                leadingIcon = AppIcons.Filled.VerifiedUser,
                onClick = { onNavigate(Screen.Root) },
                modifier = Modifier.padding(top = 4.dp),
            )
            AppButton(
                text = strings.navTrick,
                leadingIcon = AppIcons.Filled.TipsAndUpdates,
                onClick = { onNavigate(Screen.Trick) },
                modifier = Modifier.padding(top = 4.dp),
            )
            AppButton(
                text = strings.navLog,
                leadingIcon = AppIcons.Filled.Settings,
                onClick = { onNavigate(Screen.Log) },
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
