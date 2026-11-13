package com.crackmydroid.shared.ui.installedapps

import com.crackmydroid.architecture.presentation.model.UiState
import com.crackmydroid.architecture.presentation.model.UiLoadState
import com.crackmydroid.shared.domain.model.InstalledAppEntry

data class InstalledAppsState(
    val apps: List<InstalledAppEntry> = emptyList(),
    val filter: String = "",
    val loadState: UiLoadState = UiLoadState.Idle,
    val error: String? = null,
    val lastExportPath: String? = null,
    val lastSharedPackage: String? = null,
    val topQuery: String? = null,
    val favorites: Set<String> = emptySet(),
    val visibleApps: List<InstalledAppEntry> = emptyList(),
    val favoriteQueries: List<String> = emptyList(),
) : UiState {
    val loading: Boolean get() = loadState is UiLoadState.Loading
    val loadError: String? get() = (loadState as? UiLoadState.Error)?.message
}
