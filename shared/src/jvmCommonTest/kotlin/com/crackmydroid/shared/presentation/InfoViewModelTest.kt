package com.crackmydroid.shared.presentation

import com.crackmydroid.shared.domain.model.DeviceInfo
import com.crackmydroid.shared.domain.model.RootStatus
import com.crackmydroid.shared.domain.repository.ReportFileRepository
import com.crackmydroid.shared.domain.repository.DeviceInfoRepository
import com.crackmydroid.shared.domain.repository.RootCheckRepository
import com.crackmydroid.shared.domain.usecase.root.CheckRootUseCase
import com.crackmydroid.shared.domain.usecase.info.GetDeviceInfoUseCase
import com.crackmydroid.shared.domain.usecase.info.BuildDeviceInfoReportUseCase
import com.crackmydroid.shared.domain.usecase.reports.ExportReportUseCase
import com.crackmydroid.shared.ui.info.InfoViewModel
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
class InfoViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun load_success_updatesDeviceAndRootState() = runTest(dispatcher) {
        val info = deviceInfo()
        val infoRepository = mockk<DeviceInfoRepository>()
        val rootRepository = mockk<RootCheckRepository>()
        coEvery { infoRepository.getDeviceInfo() } returns info
        coEvery { rootRepository.checkRoot() } returns RootStatus(true, "su available")
        val viewModel = viewModel(infoRepository, rootRepository)

        viewModel.load()
        advanceUntilIdle()

        assertEquals(info, viewModel.state.value.info)
        assertEquals(RootStatus(true, "su available"), viewModel.state.value.root)
        assertEquals(false, viewModel.state.value.loading)
        viewModel.onCleared()
    }

    @Test
    fun load_failure_storesErrorAndStopsLoading() = runTest(dispatcher) {
        val infoRepository = mockk<DeviceInfoRepository>()
        val rootRepository = mockk<RootCheckRepository>()
        coEvery { infoRepository.getDeviceInfo() } throws IllegalStateException("device unavailable")
        val viewModel = viewModel(infoRepository, rootRepository)

        viewModel.load()
        advanceUntilIdle()

        assertEquals("device unavailable", viewModel.state.value.error)
        assertEquals(false, viewModel.state.value.loading)
        viewModel.onCleared()
    }

    @Test
    fun exportAndShare_successPersistPathAndForwardGeneratedContent() = runTest(dispatcher) {
        val infoRepository = mockk<DeviceInfoRepository>()
        val rootRepository = mockk<RootCheckRepository>()
        val exporter = mockk<ReportFileRepository>()
        coEvery { infoRepository.getDeviceInfo() } returns deviceInfo()
        coEvery { rootRepository.checkRoot() } returns RootStatus(false, "not rooted")
        coEvery { exporter.save("device_info", any()) } returns Result.success("/tmp/device.txt")
        coEvery { exporter.share("/tmp/device.txt") } returns Result.success(Unit)
        val viewModel = viewModel(infoRepository, rootRepository, exporter)
        viewModel.load()
        advanceUntilIdle()

        viewModel.export()
        viewModel.share("override")
        advanceUntilIdle()

        assertEquals("/tmp/device.txt", viewModel.state.value.lastExportPath)
        coVerify { exporter.save("device_info", match { it.contains("Device info") && it.contains("Rooted: false") }) }
        coVerify { exporter.save("device_info", "override") }
        coVerify { exporter.share("/tmp/device.txt") }
        viewModel.onCleared()
    }

    @Test
    fun exportAndShare_failure_storeSpecificErrors() = runTest(dispatcher) {
        val infoRepository = mockk<DeviceInfoRepository>()
        val rootRepository = mockk<RootCheckRepository>()
        val exporter = mockk<ReportFileRepository>()
        coEvery { infoRepository.getDeviceInfo() } returns deviceInfo()
        coEvery { rootRepository.checkRoot() } returns RootStatus(false, "")
        coEvery { exporter.save("device_info", any()) } returnsMany listOf(
            Result.failure(IllegalStateException("save failed")),
            Result.success("/tmp/device.txt")
        )
        coEvery { exporter.share("/tmp/device.txt") } returns Result.failure(IllegalStateException("share failed"))
        val viewModel = viewModel(infoRepository, rootRepository, exporter)
        viewModel.load()
        advanceUntilIdle()

        viewModel.export()
        advanceUntilIdle()
        assertEquals("save failed", viewModel.state.value.error)
        viewModel.share()
        advanceUntilIdle()
        assertEquals("share failed", viewModel.state.value.error)
        viewModel.onCleared()
    }

    private fun viewModel(
        infoRepository: DeviceInfoRepository,
        rootRepository: RootCheckRepository,
        exporter: ReportFileRepository = mockk(relaxed = true)
    ) = InfoViewModel(
        GetDeviceInfoUseCase(infoRepository),
        CheckRootUseCase(rootRepository),
        ExportReportUseCase(exporter),
        BuildDeviceInfoReportUseCase()
    )

    private fun deviceInfo() = DeviceInfo(
        manufacturer = "Acme",
        model = "Model X",
        brand = "Acme",
        device = "device",
        product = "product",
        androidVersion = "14",
        securityPatch = "2026-01-01",
        buildId = "BUILD",
        hardware = "hardware",
        bootloader = "bootloader",
        batteryLevel = 80,
        networkType = "wifi",
        totalMemory = 100L,
        freeMemory = 50L,
        screenResolution = "1080x2400",
        densityDpi = 420,
        systemPropDump = "ro.test=true",
        systemPropSource = "adb"
    )
}
