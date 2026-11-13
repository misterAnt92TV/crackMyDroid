package com.crackmydroid.shared.ui.permissions

import com.crackmydroid.architecture.presentation.model.UiState
import com.crackmydroid.architecture.presentation.model.UiLoadState
import com.crackmydroid.shared.domain.model.PermissionRiskReport
import com.crackmydroid.shared.domain.model.app.AppPermissionEntry
import com.crackmydroid.shared.ui.permissions.model.PermissionFindingUi

data class PermissionsState(
    val apps: List<AppPermissionEntry> = emptyList(),
    val filter: String = "",
    val loadState: UiLoadState = UiLoadState.Idle,
    val scanLoadState: UiLoadState = UiLoadState.Idle,
    val exportLoadState: UiLoadState = UiLoadState.Idle,
    val error: String? = null,
    val lastExportPath: String? = null,
    val topQuery: String? = null,
    val favorites: Set<String> = emptySet(),
    val autoScanRunning: Boolean = false,
    val autoScanTotalApps: Int = 0,
    val autoScanScannedApps: Int = 0,
    val autoScanCurrentLabel: String? = null,
    val showOnlyRisky: Boolean = false,
    val riskReports: List<PermissionRiskReport> = emptyList(),
    val visibleApps: List<AppPermissionEntry> = emptyList(),
    val favoriteQueries: List<String> = emptyList(),
    val findingsByPackage: Map<String, List<PermissionFindingUi>> = emptyMap(),
    val visibleFindingsByPackage: Map<String, List<PermissionFindingUi>> = emptyMap(),
    val reportPreviewPackage: String? = null,
    val reportPreviewLoadState: UiLoadState = UiLoadState.Idle,
    val reportPreviewContent: String? = null,
) : UiState {
    val loading: Boolean get() = loadState is UiLoadState.Loading
    val reportPreviewLoading: Boolean get() = reportPreviewLoadState is UiLoadState.Loading
    val loadError: String? get() = (loadState as? UiLoadState.Error)?.message
    val reportPreviewErrorFromState: String? get() = (reportPreviewLoadState as? UiLoadState.Error)?.message
    val scanError: String? get() = (scanLoadState as? UiLoadState.Error)?.message
    val exportError: String? get() = (exportLoadState as? UiLoadState.Error)?.message
}
