package com.crackmydroid.shared.presentation.navigation
import com.crackmydroid.ui.icons.AppIcons

import androidx.compose.ui.graphics.vector.ImageVector
import com.crackmydroid.shared.i18n.Strings

sealed class Screen(val title: (Strings) -> String, val icon: ImageVector) {
    object Home : Screen({ it.navHome }, AppIcons.Rounded.Home)
    object Activities : Screen({ it.navActivities }, AppIcons.AutoMirrored.Rounded.List)
    object Root : Screen({ it.navRoot }, AppIcons.Rounded.VerifiedUser)
    object Info : Screen({ it.navInfo }, AppIcons.Rounded.Info)
    object Trick : Screen({ it.navTrick }, AppIcons.Rounded.Build)
    object Settings : Screen({ it.navSettings }, AppIcons.Rounded.Settings)
    object Log : Screen({ it.navLog }, AppIcons.AutoMirrored.Rounded.ReceiptLong)
    object Permissions : Screen({ it.navPermissions }, AppIcons.Rounded.PrivacyTip)
    object InstalledApps : Screen({ it.navApk }, AppIcons.Rounded.Apps)
    object Pentest : Screen({ it.navPentest }, AppIcons.Rounded.Security)
    object Help : Screen({ it.navHelp }, AppIcons.AutoMirrored.Rounded.HelpOutline)
}

val bottomScreens = listOf(
    Screen.Home,
    Screen.Info,
    Screen.Settings,
)

val drawerScreens = listOf(
    Screen.InstalledApps,
    Screen.Pentest,
    Screen.Permissions,
    Screen.Activities,
    Screen.Root,
    Screen.Trick,
    Screen.Log,
    Screen.Help,
)
