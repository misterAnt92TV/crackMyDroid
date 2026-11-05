package com.crackmydroid.shared.ui

import com.crackmydroid.shared.ui.activities.ActivityListViewModel
import com.crackmydroid.shared.ui.home.HomeViewModel
import com.crackmydroid.shared.ui.info.InfoViewModel
import com.crackmydroid.shared.ui.installedapps.InstalledAppsViewModel
import com.crackmydroid.shared.ui.logs.LogViewModel
import com.crackmydroid.shared.ui.pentest.PenTestingViewModel
import com.crackmydroid.shared.ui.permissions.PermissionsViewModel
import com.crackmydroid.shared.ui.root.RootViewModel
import com.crackmydroid.shared.ui.settings.SettingsViewModel
import com.crackmydroid.shared.ui.trick.TrickViewModel

data class AppPresentationDependencies(
    val shell: AppShellViewModel,
    val activity: ActivityListViewModel,
    val home: HomeViewModel,
    val root: RootViewModel,
    val info: InfoViewModel,
    val trick: TrickViewModel,
    val settings: SettingsViewModel,
    val logs: LogViewModel,
    val permissions: PermissionsViewModel,
    val installedApps: InstalledAppsViewModel,
    val penTesting: PenTestingViewModel
)
