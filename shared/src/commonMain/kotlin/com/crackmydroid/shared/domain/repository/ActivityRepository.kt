package com.crackmydroid.shared.domain.repository

import com.crackmydroid.shared.domain.model.activity.ActivityEntry
import com.crackmydroid.shared.domain.model.app.AppComponent

interface ActivityRepository {
    suspend fun getActivities(): List<ActivityEntry>
    suspend fun getActivitiesPaginated(offset: Int = 0, limit: Int = 50): List<ActivityEntry>
    suspend fun getComponents(): List<AppComponent>
    suspend fun getComponentsCached(): List<AppComponent>
    suspend fun launch(
        entry: ActivityEntry,
        action: String? = null,
        dataUri: String? = null,
        mimeType: String? = null,
    ): Result<Unit>
}





