package com.crackmydroid.shared.presentation

import com.crackmydroid.shared.domain.model.ActivityEntry
import com.crackmydroid.shared.domain.model.ExportFormat
import com.crackmydroid.shared.domain.usecase.GetActivitiesUseCase
import com.crackmydroid.shared.domain.usecase.GetExportFormatUseCase
import com.crackmydroid.shared.domain.usecase.LaunchActivityUseCase
import com.crackmydroid.shared.ui.activities.ActivityListViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ActivityListViewModelTest {
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
    fun refreshLoadsActivitiesAndUpdatesState() = runTest(dispatcher) {
        val get = mockk<GetActivitiesUseCase>()
        val launch = mockk<LaunchActivityUseCase>()
        val format = mockk<GetExportFormatUseCase>()
        coEvery { get() } returns listOf(ActivityEntry("Label", "App", "pkg", "Act"))
        coEvery { format() } returns ExportFormat.TXT
        val vm = ActivityListViewModel(get, launch, format)

        vm.refresh()
        advanceUntilIdle()

        assertEquals(1, vm.state.value.activities.size)
        coVerify { get() }
        vm.onCleared()
    }

    @Test
    fun setFilterUpdatesImmediatelyAndMatchesActivityName() = runTest(dispatcher) {
        val get = mockk<GetActivitiesUseCase>()
        val launch = mockk<LaunchActivityUseCase>()
        val format = mockk<GetExportFormatUseCase>()
        coEvery { get() } returns listOf(
            ActivityEntry(
                label = "Settings",
                appLabel = "Demo App",
                packageName = "com.demo.app",
                activityName = "com.demo.app.HiddenActivity"
            )
        )
        coEvery { format() } returns ExportFormat.TXT
        val vm = ActivityListViewModel(get, launch, format)

        vm.refresh()
        advanceUntilIdle()
        vm.setFilter("Hidden")

        assertEquals("Hidden", vm.state.value.filter)
        advanceUntilIdle()
        assertEquals(1, vm.state.value.groups.size)
        vm.onCleared()
    }

    @Test
    fun setFilterTracksMostFrequentTopQuery() = runTest(dispatcher) {
        val get = mockk<GetActivitiesUseCase>()
        val launch = mockk<LaunchActivityUseCase>()
        val format = mockk<GetExportFormatUseCase>()
        coEvery { get() } returns listOf(ActivityEntry("Label", "App", "pkg", "Act"))
        coEvery { format() } returns ExportFormat.TXT
        val vm = ActivityListViewModel(get, launch, format)

        vm.refresh()
        advanceUntilIdle()
        vm.setFilter("sec", trackUsage = true)
        vm.setFilter("api", trackUsage = true)
        vm.setFilter("sec", trackUsage = true)
        advanceUntilIdle()

        assertEquals("sec", vm.state.value.topQuery)
        vm.onCleared()
    }
}
