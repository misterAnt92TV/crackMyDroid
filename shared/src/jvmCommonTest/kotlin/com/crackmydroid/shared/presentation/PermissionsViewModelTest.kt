package com.crackmydroid.shared.presentation

import com.crackmydroid.shared.domain.model.AppPermissionEntry
import com.crackmydroid.shared.domain.usecase.ListPermissionsUseCase
import com.crackmydroid.shared.domain.usecase.permissions.AnalyzePermissionRisksUseCase
import com.crackmydroid.shared.domain.usecase.permissions.BuildPermissionsReportUseCase
import com.crackmydroid.shared.ui.permissions.PermissionsViewModel
import io.mockk.coEvery
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
class PermissionsViewModelTest {
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
    fun setFilterTracksMostFrequentTopQuery() = runTest(dispatcher) {
        val listPermissions = mockk<ListPermissionsUseCase>()
        coEvery { listPermissions() } returns listOf(
            AppPermissionEntry("Demo", "pkg.demo", listOf("android.permission.INTERNET"))
        )
        val analyzePermissionRisks = AnalyzePermissionRisksUseCase()
        val buildPermissionsReport = BuildPermissionsReportUseCase(analyzePermissionRisks)
        val vm = PermissionsViewModel(listPermissions, analyzePermissionRisks, buildPermissionsReport)

        vm.refresh()
        advanceUntilIdle()
        vm.setFilter("sms", trackUsage = true)
        vm.setFilter("cam", trackUsage = true)
        vm.setFilter("sms", trackUsage = true)

        assertEquals("sms", vm.state.value.topQuery)
        vm.onCleared()
    }

    @Test
    fun showOnlyRiskyFiltersOutSafeAppsAfterScan() = runTest(dispatcher) {
        val listPermissions = mockk<ListPermissionsUseCase>()
        coEvery { listPermissions() } returns listOf(
            AppPermissionEntry("Safe App", "pkg.safe", emptyList()),
            AppPermissionEntry("Risky App", "pkg.risky", listOf("android.permission.SYSTEM_ALERT_WINDOW"))
        )
        val analyzePermissionRisks = AnalyzePermissionRisksUseCase()
        val buildPermissionsReport = BuildPermissionsReportUseCase(analyzePermissionRisks)
        val vm = PermissionsViewModel(listPermissions, analyzePermissionRisks, buildPermissionsReport)

        vm.refresh()
        advanceUntilIdle()
        vm.startAutoRiskScan()
        advanceUntilIdle()
        vm.setShowOnlyRisky(true)

        val filtered = vm.filtered()
        assertEquals(1, filtered.size)
        assertEquals("pkg.risky", filtered.first().packageName)
        vm.onCleared()
    }
}
