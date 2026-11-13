package com.crackmydroid.shared.ui.apkcontent

import com.crackmydroid.architecture.presentation.model.UiLoadState
import com.crackmydroid.architecture.presentation.model.UiState
import com.crackmydroid.shared.domain.model.InstalledAppEntry
import com.crackmydroid.shared.domain.model.apkcontent.ApkContentAnalysis

data class AnalyzeApkContentState(
    val apps: List<InstalledAppEntry> = emptyList(),
    val query: String = "",
    val selectedPackage: String? = null,
    val loadState: UiLoadState = UiLoadState.Idle,
    val analysisState: UiLoadState = UiLoadState.Idle,
    val analysis: ApkContentAnalysis? = null,
    val error: String? = null,
    val exportPath: String? = null,
) : UiState {
    val filteredApps get() = apps.filter {
        query.isBlank() || it.appLabel.contains(query, true) ||
            it.packageName.contains(query, true)
    }
    val selectedApp get() = apps.firstOrNull { it.packageName == selectedPackage }
    val step get() = if (analysis != null) {
        3
    } else if (selectedPackage != null) {
        2
    } else {
        1
    }
}
