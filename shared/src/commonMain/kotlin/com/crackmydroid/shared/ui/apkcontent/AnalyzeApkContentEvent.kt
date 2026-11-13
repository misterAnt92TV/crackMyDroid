package com.crackmydroid.shared.ui.apkcontent

import com.crackmydroid.architecture.presentation.model.UiEvent

sealed class AnalyzeApkContentEvent : UiEvent() {
    class Refresh : AnalyzeApkContentEvent()
    class QueryChanged(val query: String) : AnalyzeApkContentEvent()
    class PackageSelected(val packageName: String) : AnalyzeApkContentEvent()
    class ClearSelection : AnalyzeApkContentEvent()
    class AnalysisRequested : AnalyzeApkContentEvent()
    class ReportExportRequested : AnalyzeApkContentEvent()
    class ReportShareRequested : AnalyzeApkContentEvent()
    class ErrorDismissed : AnalyzeApkContentEvent()
    class Reset : AnalyzeApkContentEvent()
}
