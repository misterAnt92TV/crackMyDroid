package com.crackmydroid.shared.presentation

import com.crackmydroid.shared.domain.model.app.AppPermissionEntry
import com.crackmydroid.shared.domain.repository.ReportFileRepository
import com.crackmydroid.shared.domain.usecase.permissions.ListPermissionsUseCase
import com.crackmydroid.shared.domain.usecase.permissions.AnalyzePermissionRisksUseCase
import com.crackmydroid.shared.domain.usecase.permissions.BuildPermissionsReportUseCase
import com.crackmydroid.shared.domain.usecase.reports.ExportReportUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetPermissionFavoritesUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetPermissionFavoritesUseCase
import com.crackmydroid.shared.ui.permissions.PermissionsViewModel
import io.mockk.coEvery
import io.mockk.coJustRun
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
class PermissionsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private fun reportExporter() = ExportReportUseCase(mockk<ReportFileRepository>(relaxed = true))

    private fun emptyFavorites(): GetPermissionFavoritesUseCase {
        val getFavorites = mockk<GetPermissionFavoritesUseCase>()
        coEvery { getFavorites() } returns emptySet()
        return getFavorites
    }

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
        val vm = PermissionsViewModel(
            listPermissions,
            analyzePermissionRisks,
            buildPermissionsReport,
            reportExporter(),
            emptyFavorites(),
            mockk(relaxed = true)
        )

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
            AppPermissionEntry(
                "Risky App",
                "pkg.risky",
                listOf("android.permission.SYSTEM_ALERT_WINDOW")
            )
        )
        val analyzePermissionRisks = AnalyzePermissionRisksUseCase()
        val buildPermissionsReport = BuildPermissionsReportUseCase(analyzePermissionRisks)
        val vm = PermissionsViewModel(
            listPermissions,
            analyzePermissionRisks,
            buildPermissionsReport,
            reportExporter(),
            emptyFavorites(),
            mockk(relaxed = true)
        )

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

    @Test
    fun refreshLoadsFavoritesAndTogglePersistsThem() = runTest(dispatcher) {
        val listPermissions = mockk<ListPermissionsUseCase>()
        val getFavorites = mockk<GetPermissionFavoritesUseCase>()
        val setFavorites = mockk<SetPermissionFavoritesUseCase>()
        coEvery { listPermissions() } returns emptyList()
        coEvery { getFavorites() } returns setOf("pkg.favorite")
        coJustRun { setFavorites(any()) }
        val analyzePermissionRisks = AnalyzePermissionRisksUseCase()
        val vm = PermissionsViewModel(
            listPermissions,
            analyzePermissionRisks,
            BuildPermissionsReportUseCase(analyzePermissionRisks),
            reportExporter(),
            getFavorites,
            setFavorites
        )

        vm.refresh()
        advanceUntilIdle()
        vm.toggleFavorite("pkg.favorite")
        advanceUntilIdle()

        assertEquals(emptySet(), vm.state.value.favorites)
        coVerify { setFavorites(emptySet()) }
        vm.onCleared()
    }
}
