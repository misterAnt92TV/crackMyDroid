package com.crackmydroid.shared.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.crackmydroid.shared.presentation.navigation.Screen
import com.crackmydroid.shared.presentation.components.AppButton
import com.crackmydroid.shared.presentation.components.CardBlock
import com.crackmydroid.shared.presentation.components.SectionHeader
import com.crackmydroid.shared.i18n.LocalStrings

@Composable
fun MoreScreen(
    padding: androidx.compose.foundation.layout.PaddingValues,
    onNavigate: (Screen) -> Unit,
    headerHelp: String,
    onHelp: (String) -> Unit
) {
    val strings = LocalStrings.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(title = strings.navMore, help = headerHelp, onHelp = onHelp)
        CardBlock(title = strings.navMore) {
            AppButton(text = strings.navActivities, leadingIcon = Icons.AutoMirrored.Filled.ListAlt, onClick = { onNavigate(Screen.Activities) })
            AppButton(text = strings.navRoot, leadingIcon = Icons.Filled.VerifiedUser, onClick = { onNavigate(Screen.Root) }, modifier = Modifier.padding(top = 4.dp))
            AppButton(text = strings.navTrick, leadingIcon = Icons.Filled.TipsAndUpdates, onClick = { onNavigate(Screen.Trick) }, modifier = Modifier.padding(top = 4.dp))
            AppButton(text = strings.navLog, leadingIcon = Icons.Filled.Settings, onClick = { onNavigate(Screen.Log) }, modifier = Modifier.padding(top = 4.dp))
        }
    }
}
