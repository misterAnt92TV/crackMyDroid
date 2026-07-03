package com.crackmydroid.shared.presentation

import com.crackmydroid.shared.domain.model.InstalledAppEntry
import com.crackmydroid.shared.domain.usecase.ExportApkUseCase
import com.crackmydroid.shared.domain.usecase.ListInstalledAppsUseCase
import com.crackmydroid.shared.domain.usecase.ShareApkUseCase
import com.crackmydroid.shared.domain.usecase.GetExportFormatUseCase
import com.crackmydroid.shared.domain.model.ExportFormat
import com.crackmydroid.shared.ui.installedapps.InstalledAppsViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class InstalledAppsViewModelTest {
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
    fun refreshLoadsInstalledApps() = runTest(dispatcher) {
        val list = mockk<ListInstalledAppsUseCase>()
        val export = mockk<ExportApkUseCase>()
        val share = mockk<ShareApkUseCase>()
        val format = mockk<GetExportFormatUseCase>()
        coEvery { list() } returns listOf(InstalledAppEntry("App", "pkg", "/path"))
        coEvery { format() } returns ExportFormat.TXT
        val vm = InstalledAppsViewModel(list, export, share, format)

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
        val format = mockk<GetExportFormatUseCase>()
        coEvery { list() } returns emptyList()
        coEvery { export("pkg") } returns Result.success("/tmp/pkg.apk")
        coEvery { format() } returns ExportFormat.TXT
        val vm = InstalledAppsViewModel(list, export, share, format)

        vm.export("pkg")
        advanceUntilIdle()

        assertEquals("/tmp/pkg.apk", vm.state.value.lastExportPath)
        assertNull(vm.state.value.error)
        coVerify { export("pkg") }
        vm.onCleared()
    }

    @Test
    fun shareSetsLastSharedOnSuccess() = runTest(dispatcher) {
        val list = mockk<ListInstalledAppsUseCase>()
        val export = mockk<ExportApkUseCase>()
        val share = mockk<ShareApkUseCase>()
        val format = mockk<GetExportFormatUseCase>()
        coEvery { list() } returns emptyList()
        coEvery { share("pkg", false) } returns Result.success(Unit)
        coEvery { format() } returns ExportFormat.TXT
        val vm = InstalledAppsViewModel(list, export, share, format)

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
        val format = mockk<GetExportFormatUseCase>()
        coEvery { list() } returns emptyList()
        coEvery { share("pkg", true) } returns Result.failure(IllegalStateException("fail"))
        coEvery { format() } returns ExportFormat.TXT
        val vm = InstalledAppsViewModel(list, export, share, format)

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
        val format = mockk<GetExportFormatUseCase>()
        coEvery { list() } returns listOf(
            InstalledAppEntry("Alpha App", "pkg.alpha", "/a.apk"),
            InstalledAppEntry("Beta App", "pkg.beta", "/b.apk")
        )
        coEvery { format() } returns ExportFormat.TXT
        val vm = InstalledAppsViewModel(list, export, share, format)

        vm.refresh()
        advanceUntilIdle()
        vm.setFilter("app", trackUsage = true)
        vm.setFilter("beta", trackUsage = true)
        vm.setFilter("app", trackUsage = true)
        vm.toggleFavorite("pkg.beta")

        val filtered = vm.filtered()

        assertEquals("app", vm.state.value.topQuery)
        assertEquals("pkg.beta", filtered.first().packageName)
        vm.onCleared()
    }

    @Test
    fun setFilterDoesNotTrackTopQueryUntilExplicitSubmit() = runTest(dispatcher) {
        val list = mockk<ListInstalledAppsUseCase>()
        val export = mockk<ExportApkUseCase>()
        val share = mockk<ShareApkUseCase>()
        val format = mockk<GetExportFormatUseCase>()
        coEvery { list() } returns emptyList()
        coEvery { format() } returns ExportFormat.TXT
        val vm = InstalledAppsViewModel(list, export, share, format)

        vm.setFilter("alpha")
        assertNull(vm.state.value.topQuery)

        vm.setFilter("alpha", trackUsage = true)
        assertEquals("alpha", vm.state.value.topQuery)
        vm.onCleared()
    }
}
