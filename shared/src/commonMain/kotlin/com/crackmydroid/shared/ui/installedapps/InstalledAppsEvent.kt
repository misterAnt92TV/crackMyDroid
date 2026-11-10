package com.crackmydroid.shared.ui.installedapps

import com.crackmydroid.architecture.presentation.model.UiEvent

sealed class InstalledAppsEvent : UiEvent() {
    class Refresh : InstalledAppsEvent()
    class FilterChanged(val value: String, val trackUsage: Boolean = false) : InstalledAppsEvent()
    class ApkExportRequested(val packageName: String) : InstalledAppsEvent()
    class AllExportRequested(val baseName: String = "installed_apps") : InstalledAppsEvent()
    class AllShareRequested(val baseName: String = "installed_apps") : InstalledAppsEvent()
    class ApkShareRequested(val packageName: String, val bluetoothOnly: Boolean) : InstalledAppsEvent()
    class ErrorDismissed : InstalledAppsEvent()
    class FavoriteToggled(val packageName: String) : InstalledAppsEvent()
}
