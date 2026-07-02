package com.crackmydroid.shared.ui.activities

import com.crackmydroid.shared.domain.model.ActivityEntry

data class ActivityListState(
    val activities: List<ActivityEntry> = emptyList(),
    val filter: String = "",
    val error: String? = null,
    val loading: Boolean = false,
    val topQuery: String? = null,
    val favorites: Set<String> = emptySet(),
    val favoritePackages: Set<String> = emptySet(),
    val expandedPackages: Set<String> = emptySet(),
    val groups: List<ActivityGroup> = emptyList()
)

data class ActivityGroup(
    val packageName: String,
    val label: String?,
    val activities: List<ActivityEntry>
)
