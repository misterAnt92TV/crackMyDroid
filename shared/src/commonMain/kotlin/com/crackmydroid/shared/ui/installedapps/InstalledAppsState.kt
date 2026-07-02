package com.crackmydroid.shared.ui.installedapps

import com.crackmydroid.shared.domain.model.InstalledAppEntry

data class InstalledAppsState(
    val apps: List<InstalledAppEntry> = emptyList(),
    val filter: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val lastExportPath: String? = null,
    val lastSharedPackage: String? = null,
    val topQuery: String? = null,
    val favorites: Set<String> = emptySet()
)
