package com.crackmydroid.shared.presentation

import com.crackmydroid.shared.domain.model.ExportFormat
import com.crackmydroid.shared.domain.model.activity.ActivityEntry
import com.crackmydroid.shared.domain.model.activity.favoriteKey
import com.crackmydroid.shared.domain.repository.ReportFileRepository
import com.crackmydroid.shared.domain.repository.SettingsRepository
import com.crackmydroid.shared.domain.usecase.activities.GetActivitiesUseCase
import com.crackmydroid.shared.domain.usecase.activities.LaunchActivityUseCase
import com.crackmydroid.shared.domain.usecase.reports.ExportReportUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetActivityFavoritesUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetActivityPackageFavoritesUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetExportFormatUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetActivityFavoritesUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetActivityPackageFavoritesUseCase
import com.crackmydroid.shared.ui.activities.ActivityListEvent
import com.crackmydroid.shared.ui.activities.ActivityListViewModel
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class ActivityFavoriteViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun refresh_persistedFavorite_recognizesCanonicalKey() = runTest(dispatcher) {
        val entry = ActivityEntry(
            label = "Details",
            appLabel = "Demo",
            packageName = "com.example.demo",
            activityName = "com.example.demo.DetailsActivity",
        )
        val getActivities = mockk<GetActivitiesUseCase>()
        val launchActivity = mockk<LaunchActivityUseCase>()
        val getExportFormat = mockk<GetExportFormatUseCase>()
        val settings = mockk<SettingsRepository>()
        coEvery { getActivities() } returns Result.success(listOf(entry))
        coEvery { getExportFormat() } returns Result.success(ExportFormat.TXT)
        coEvery { settings.getActivityFavorites() } returns setOf(entry.favoriteKey())
        coEvery { settings.getActivityPackageFavorites() } returns emptySet()
        coJustRun { settings.setActivityFavorites(any()) }
        coJustRun { settings.setActivityPackageFavorites(any()) }

        val viewModel = ActivityListViewModel(
            getActivities = getActivities,
            launchActivity = launchActivity,
            getExportFormat = getExportFormat,
            getFavorites = GetActivityFavoritesUseCase(settings),
            setFavorites = SetActivityFavoritesUseCase(settings),
            getPackageFavorites = GetActivityPackageFavoritesUseCase(settings),
            setPackageFavorites = SetActivityPackageFavoritesUseCase(settings),
            reportExporter = ExportReportUseCase(mockk<ReportFileRepository>(relaxed = true)),
            dispatchers = TestAppDispatchers(dispatcher),
        )

        viewModel.handleEvent(ActivityListEvent.Refresh())
        advanceUntilIdle()
        assertEquals(setOf(entry.favoriteKey()), viewModel.state.value.favorites)
        viewModel.handleEvent(ActivityListEvent.FavoriteToggled(entry))
        advanceUntilIdle()

        assertEquals(emptySet(), viewModel.state.value.favorites)
        coVerify { settings.setActivityFavorites(emptySet()) }
        viewModel.onCleared()
    }
}
