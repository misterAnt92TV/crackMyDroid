package com.crackmydroid.shared.ui.activities

import com.crackmydroid.shared.domain.model.ActivityEntry
import com.crackmydroid.shared.domain.model.ActivityIntentRiskLevel
import com.crackmydroid.shared.domain.model.ActivityIntentRiskReport

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

data class ActivityAppReport(
    val packageName: String,
    val appLabel: String,
    val totalActivities: Int,
    val shellLaunchableCount: Int,
    val contextualLaunchCount: Int,
    val guidedLaunchCount: Int,
    val dumpOnlyCount: Int,
    val riskyActivitiesCount: Int,
    val favoriteActivitiesCount: Int,
    val packageFavorite: Boolean,
    val highestRiskLevel: ActivityIntentRiskLevel,
    val recommendations: List<String>,
    val findings: List<String>,
    val highlights: List<ActivityIntentRiskReport>
)
