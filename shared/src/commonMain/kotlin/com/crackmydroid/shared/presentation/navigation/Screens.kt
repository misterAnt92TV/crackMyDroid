package com.crackmydroid.shared.presentation.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.crackmydroid.shared.i18n.Strings
import com.crackmydroid.ui.icons.AppIcons

sealed class Screen(val id: String, val title: (Strings) -> String, val icon: ImageVector) {
    object Home : Screen("home", { it.navHome }, AppIcons.Rounded.Home)
    object Activities : Screen("activities", { it.navActivities }, AppIcons.AutoMirrored.Rounded.List)
    object Root : Screen("root", { it.navRoot }, AppIcons.Rounded.VerifiedUser)
    object Info : Screen("info", { it.navInfo }, AppIcons.Rounded.Info)
    object Trick : Screen("trick", { it.navTrick }, AppIcons.Rounded.Build)
    object Settings : Screen("settings", { it.navSettings }, AppIcons.Rounded.Settings)
    object Log : Screen("log", { it.navLog }, AppIcons.AutoMirrored.Rounded.ReceiptLong)
    object Permissions : Screen("permissions", { it.navPermissions }, AppIcons.Rounded.PrivacyTip)
    object InstalledApps : Screen("installed_apps", { it.navApk }, AppIcons.Rounded.Apps)
    object Pentest : Screen("pentest", { it.navPentest }, AppIcons.Rounded.Security)
    object AnalyzeApkContent : Screen("analyze_apk_content", { it.navAnalyzeApkContent }, AppIcons.Rounded.Analyze)
    object Help : Screen("help", { it.navHelp }, AppIcons.AutoMirrored.Rounded.HelpOutline)
}

val bottomScreens = listOf(
    Screen.Home,
    Screen.Info,
    Screen.Settings,
)

val drawerScreens = listOf(
    Screen.InstalledApps,
    Screen.Pentest,
    Screen.AnalyzeApkContent,
    Screen.Permissions,
    Screen.Activities,
    Screen.Root,
    Screen.Trick,
    Screen.Log,
    Screen.Help,
)
