package com.crackmydroid.shared.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Security
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val title: (com.crackmydroid.shared.i18n.Strings) -> String, val icon: ImageVector) {
    object Home : Screen({ it.navHome }, Icons.Rounded.Home)
    object Activities : Screen({ it.navActivities }, Icons.Rounded.List)
    object Root : Screen({ it.navRoot }, Icons.Rounded.VerifiedUser)
    object Info : Screen({ it.navInfo }, Icons.Rounded.Info)
    object Trick : Screen({ it.navTrick }, Icons.Rounded.Build)
    object Settings : Screen({ it.navSettings }, Icons.Rounded.Settings)
    object Log : Screen({ it.navLog }, Icons.Rounded.ReceiptLong)
    object Permissions : Screen({ it.navPermissions }, Icons.Rounded.PrivacyTip)
    object InstalledApps : Screen({ it.navApk }, Icons.Rounded.Apps)
    object Pentest : Screen({ it.navPentest }, Icons.Rounded.Security)
}

val bottomScreens = listOf(
    Screen.Home,
    Screen.Info,
    Screen.Settings
)

val drawerScreens = listOf(
    Screen.InstalledApps,
    Screen.Pentest,
    Screen.Permissions,
    Screen.Activities,
    Screen.Root,
    Screen.Trick,
    Screen.Log
)
