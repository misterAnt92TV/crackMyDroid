package com.crackmydroid.shared.ui.activities

import com.crackmydroid.architecture.presentation.model.UiEvent
import com.crackmydroid.shared.domain.model.activity.ActivityEntry

sealed class ActivityListEvent : UiEvent() {
    class Refresh : ActivityListEvent()
    class FilterChanged(val value: String, val trackUsage: Boolean = false) : ActivityListEvent()
    class ActivityLaunched(val entry: ActivityEntry) : ActivityListEvent()
    class GuidedLaunchOpened(val entry: ActivityEntry) : ActivityListEvent()
    class GuidedLaunchDismissed : ActivityListEvent()
    class GuidedLaunchConfirmed(val action: String?, val dataUri: String?, val mimeType: String?) : ActivityListEvent()
    class ActivityLaunchedWithIntent(val entry: ActivityEntry, val action: String?, val dataUri: String?, val mimeType: String?) : ActivityListEvent()
    class ErrorDismissed : ActivityListEvent()
    class FavoriteToggled(val entry: ActivityEntry) : ActivityListEvent()
    class PackageFavoriteToggled(val packageName: String) : ActivityListEvent()
    class GroupExpansionToggled(val packageName: String) : ActivityListEvent()
    class AllExportRequested : ActivityListEvent()
    class GroupExportRequested(val packageName: String) : ActivityListEvent()
    class GroupClipboardCopyRequested(val packageName: String) : ActivityListEvent()
    class ReportRequested(val packageName: String) : ActivityListEvent()
    class ReportDismissed : ActivityListEvent()
    class VisualReportExportRequested(val packageName: String) : ActivityListEvent()
}
