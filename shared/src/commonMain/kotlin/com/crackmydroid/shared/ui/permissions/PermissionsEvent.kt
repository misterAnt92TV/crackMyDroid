package com.crackmydroid.shared.ui.permissions

import com.crackmydroid.architecture.presentation.model.UiEvent

sealed class PermissionsEvent : UiEvent() {
    class Refresh : PermissionsEvent()
    class FilterChanged(val value: String, val trackUsage: Boolean = false) : PermissionsEvent()
    class RiskFilterChanged(val enabled: Boolean) : PermissionsEvent()
    class RiskFilterToggled : PermissionsEvent()
    class ErrorDismissed : PermissionsEvent()
    class ExportRequested : PermissionsEvent()
    class FavoriteToggled(val packageName: String) : PermissionsEvent()
    class ReportShared(val packageName: String, val contentOverride: String? = null) : PermissionsEvent()
    class ReportPreviewRequested(val packageName: String) : PermissionsEvent()
    class ReportPreviewDismissed : PermissionsEvent()
    class AutoRiskScanStarted : PermissionsEvent()
    class AutoRiskScanCancelled : PermissionsEvent()
}
