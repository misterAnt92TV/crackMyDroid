package com.crackmydroid.shared.presentation

import com.crackmydroid.shared.domain.model.ExportDocument
import com.crackmydroid.shared.domain.model.ExportFormat
import com.crackmydroid.shared.domain.model.InstalledAppEntry
import com.crackmydroid.shared.domain.repository.ReportFileRepository
import com.crackmydroid.shared.domain.usecase.installedapps.ExportApkUseCase
import com.crackmydroid.shared.domain.usecase.installedapps.ListInstalledAppsUseCase
import com.crackmydroid.shared.domain.usecase.installedapps.ShareApkUseCase
import com.crackmydroid.shared.domain.usecase.reports.ExportReportUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetExportFormatUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetInstalledAppFavoritesUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetInstalledAppFavoritesUseCase
import com.crackmydroid.shared.ui.installedapps.InstalledAppsViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class InstalledAppsViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    private fun reportExporter() = ExportReportUseCase(mockk<ReportFileRepository>())

    private fun installedAppsViewModel(
        list: ListInstalledAppsUseCase,
        export: ExportApkUseCase,
        share: ShareApkUseCase,
        reportExporter: ExportReportUseCase,
    ) = InstalledAppsViewModel(
        listUseCase = list,
        exportUseCase = export,
        shareUseCase = share,
        reportExporter = reportExporter,
        getFavorites = emptyFavorites(),
        setFavorites = mockk<SetInstalledAppFavoritesUseCase>(relaxed = true),
    )

    private fun emptyFavorites(): GetInstalledAppFavoritesUseCase {
        val getFavorites = mockk<GetInstalledAppFavoritesUseCase>()
        coEvery { getFavorites() } returns Result.success(emptySet())
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
    fun refreshLoadsInstalledApps() = runTest(dispatcher) {
        val list = mockk<ListInstalledAppsUseCase>()
        val export = mockk<ExportApkUseCase>()
        val share = mockk<ShareApkUseCase>()
        coEvery { list() } returns Result.success(listOf(InstalledAppEntry("App", "pkg", "/path")))
        val vm = installedAppsViewModel(list, export, share, reportExporter())

        vm.refresh()
        advanceUntilIdle()

        assertEquals(1, vm.state.value.apps.size)
        assertNull(vm.state.value.error)
        coVerify { list() }
        vm.onCleared()
    }

    @Test
    fun exportStoresPathOnSuccess() = runTest(dispatcher) {
        val list = mockk<ListInstalledAppsUseCase>()
        val export = mockk<ExportApkUseCase>()
        val share = mockk<ShareApkUseCase>()
        coEvery { list() } returns Result.success(emptyList())
        coEvery { export("pkg") } returns Result.success("/tmp/pkg.apk")
        val vm = installedAppsViewModel(list, export, share, reportExporter())

        vm.export("pkg")
        advanceUntilIdle()

        assertEquals("/tmp/pkg.apk", vm.state.value.lastExportPath)
        assertNull(vm.state.value.error)
        coVerify { export("pkg") }
        vm.onCleared()
    }

    @Test
    fun exportAllWritesTheGeneratedReportThroughTheReportPort() = runTest(dispatcher) {
        val list = mockk<ListInstalledAppsUseCase>()
        val export = mockk<ExportApkUseCase>()
        val share = mockk<ShareApkUseCase>()
        val format = mockk<GetExportFormatUseCase>()
        val reportFiles = mockk<ReportFileRepository>()
        val document = slot<ExportDocument>()
        coEvery { list() } returns Result.success(listOf(InstalledAppEntry("App", "pkg", "/path")))
        coEvery { format() } returns Result.success(ExportFormat.PDF)
        coEvery {
            reportFiles.save(capture(document))
        } returns Result.success("/tmp/apps.pdf")
        val vm = installedAppsViewModel(list, export, share, ExportReportUseCase(reportFiles, format))

        vm.refresh()
        advanceUntilIdle()
        vm.exportAll()
        advanceUntilIdle()

        assertEquals("/tmp/apps.pdf", vm.state.value.lastExportPath)
        assertNull(vm.state.value.error)
        assertEquals("installed_apps.pdf", document.captured.fileName)
        assertEquals("installed_apps", document.captured.reportType)
        assertEquals(
            listOf(InstalledAppEntry("App", "pkg", "/path")),
            Json.decodeFromJsonElement<List<InstalledAppEntry>>(document.captured.data),
        )
        coVerify(exactly = 1) { reportFiles.save(any<ExportDocument>()) }
        vm.onCleared()
    }

    @Test
    fun shareSetsLastSharedOnSuccess() = runTest(dispatcher) {
        val list = mockk<ListInstalledAppsUseCase>()
        val export = mockk<ExportApkUseCase>()
        val share = mockk<ShareApkUseCase>()
        coEvery { list() } returns Result.success(emptyList())
        coEvery { share("pkg", false) } returns Result.success(Unit)
        val vm = installedAppsViewModel(list, export, share, reportExporter())

        vm.share("pkg", false)
        advanceUntilIdle()

        assertEquals("pkg", vm.state.value.lastSharedPackage)
        assertNull(vm.state.value.error)
        coVerify { share("pkg", false) }
        vm.onCleared()
    }

    @Test
    fun shareErrorSetsErrorMessage() = runTest(dispatcher) {
        val list = mockk<ListInstalledAppsUseCase>()
        val export = mockk<ExportApkUseCase>()
        val share = mockk<ShareApkUseCase>()
        coEvery { list() } returns Result.success(emptyList())
        coEvery { share("pkg", true) } returns Result.failure(IllegalStateException("fail"))
        val vm = installedAppsViewModel(list, export, share, reportExporter())

        vm.share("pkg", true)
        advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        coVerify { share("pkg", true) }
        vm.onCleared()
    }

    @Test
    fun filterTracksTopQueryAndFavoritesArePrioritized() = runTest(dispatcher) {
        val list = mockk<ListInstalledAppsUseCase>()
        val export = mockk<ExportApkUseCase>()
        val share = mockk<ShareApkUseCase>()
        coEvery { list() } returns Result.success(
            listOf(
                InstalledAppEntry("Alpha App", "pkg.alpha", "/a.apk"),
                InstalledAppEntry("Beta App", "pkg.beta", "/b.apk"),
            ),
        )
        val vm = installedAppsViewModel(list, export, share, reportExporter())

        vm.refresh()
        advanceUntilIdle()
        vm.setFilter("app", trackUsage = true)
        vm.setFilter("beta", trackUsage = true)
        vm.setFilter("app", trackUsage = true)
        vm.toggleFavorite("pkg.beta")

        val filtered = vm.state.value.visibleApps

        assertEquals("app", vm.state.value.topQuery)
        assertEquals("pkg.beta", filtered.first().packageName)
        vm.onCleared()
    }

    @Test
    fun setFilterDoesNotTrackTopQueryUntilExplicitSubmit() = runTest(dispatcher) {
        val list = mockk<ListInstalledAppsUseCase>()
        val export = mockk<ExportApkUseCase>()
        val share = mockk<ShareApkUseCase>()
        coEvery { list() } returns Result.success(emptyList())
        val vm = installedAppsViewModel(list, export, share, reportExporter())

        vm.setFilter("alpha")
        assertNull(vm.state.value.topQuery)

        vm.setFilter("alpha", trackUsage = true)
        assertEquals("alpha", vm.state.value.topQuery)
        vm.onCleared()
    }
}
