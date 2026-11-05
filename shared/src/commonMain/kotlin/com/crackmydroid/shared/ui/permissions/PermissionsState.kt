package com.crackmydroid.shared.ui.permissions

import com.crackmydroid.shared.domain.model.app.AppPermissionEntry
import com.crackmydroid.shared.domain.model.PermissionRiskReport

data class PermissionsState(
    val apps: List<AppPermissionEntry> = emptyList(),
    val filter: String = "",
    val loading: Boolean = false,
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
    val favoriteQueries: List<String> = emptyList()
    ,val visibleFindingsByPackage: Map<String, List<com.crackmydroid.shared.domain.model.PermissionRiskFinding>> = emptyMap()
)
