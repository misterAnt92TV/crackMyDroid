package com.crackmydroid.shared.presentation

import com.crackmydroid.shared.domain.model.DeviceInfo
import com.crackmydroid.shared.domain.model.ExportDocument
import com.crackmydroid.shared.domain.model.ExportFormat
import com.crackmydroid.shared.domain.model.ReportLabel
import com.crackmydroid.shared.domain.model.RootStatus
import com.crackmydroid.shared.domain.model.UseCaseReportText
import com.crackmydroid.shared.domain.repository.DeviceInfoRepository
import com.crackmydroid.shared.domain.repository.ReportFileRepository
import com.crackmydroid.shared.domain.repository.RootCheckRepository
import com.crackmydroid.shared.domain.usecase.info.BuildDeviceInfoReportUseCase
import com.crackmydroid.shared.domain.usecase.info.GetDeviceInfoUseCase
import com.crackmydroid.shared.domain.usecase.reports.ExportReportUseCase
import com.crackmydroid.shared.domain.usecase.root.CheckRootUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetExportFormatUseCase
import com.crackmydroid.shared.i18n.usecase.UseCaseTexts
import com.crackmydroid.shared.ui.info.InfoEvent
import com.crackmydroid.shared.ui.info.InfoViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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

        viewModel.handleEvent(InfoEvent.Load())
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

        viewModel.handleEvent(InfoEvent.Load())
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
        val saved = slot<ExportDocument>()
        val shared = slot<ExportDocument>()
        coEvery { infoRepository.getDeviceInfo() } returns deviceInfo()
        coEvery { rootRepository.checkRoot() } returns RootStatus(false, "not rooted")
        coEvery { exporter.save(capture(saved)) } returns Result.success("/tmp/device.pdf")
        coEvery { exporter.share(capture(shared)) } returns Result.success("/tmp/device.pdf")
        val viewModel = viewModel(infoRepository, rootRepository, exporter)
        viewModel.handleEvent(InfoEvent.Load())
        advanceUntilIdle()

        viewModel.handleEvent(InfoEvent.Export())
        viewModel.handleEvent(InfoEvent.Share("override"))
        advanceUntilIdle()

        assertEquals("/tmp/device.pdf", viewModel.state.value.lastExportPath)
        assertEquals("device_info.pdf", saved.captured.fileName)
        assertTrue(
            saved.captured.textContent.contains(UseCaseTexts("en").label(ReportLabel.DEVICE_INFO_TITLE)) &&
                saved.captured.textContent.contains(
                    "${UseCaseTexts("en").label(ReportLabel.ROOTED)}: ${UseCaseTexts("en").boolean(false)}",
                ),
        )
        assertEquals("device_info.pdf", shared.captured.fileName)
        assertEquals("override", shared.captured.textContent)
        coVerify(exactly = 1) { exporter.save(any<ExportDocument>()) }
        coVerify(exactly = 1) { exporter.share(any<ExportDocument>()) }
        viewModel.onCleared()
    }

    @Test
    fun exportAndShare_failure_storeSpecificErrors() = runTest(dispatcher) {
        val infoRepository = mockk<DeviceInfoRepository>()
        val rootRepository = mockk<RootCheckRepository>()
        val exporter = mockk<ReportFileRepository>()
        coEvery { infoRepository.getDeviceInfo() } returns deviceInfo()
        coEvery { rootRepository.checkRoot() } returns RootStatus(false, "")
        coEvery { exporter.save(any<ExportDocument>()) } returnsMany listOf(
            Result.failure(IllegalStateException("save failed")),
            Result.success("/tmp/device.pdf"),
        )
        coEvery { exporter.share(any<ExportDocument>()) } returns Result.failure(IllegalStateException("share failed"))
        val viewModel = viewModel(infoRepository, rootRepository, exporter)
        viewModel.handleEvent(InfoEvent.Load())
        advanceUntilIdle()

        viewModel.handleEvent(InfoEvent.Export())
        advanceUntilIdle()
        assertEquals("save failed", viewModel.state.value.error)
        viewModel.handleEvent(InfoEvent.Share())
        advanceUntilIdle()
        assertEquals("share failed", viewModel.state.value.error)
        viewModel.onCleared()
    }

    @Test
    fun loadedDeviceReportChangesLanguageWithoutReloadingRepositories() = runTest(dispatcher) {
        val infoRepository = mockk<DeviceInfoRepository>()
        val rootRepository = mockk<RootCheckRepository>()
        val files = mockk<ReportFileRepository>()
        val saved = slot<ExportDocument>()
        coEvery { infoRepository.getDeviceInfo() } returns deviceInfo()
        coEvery { rootRepository.checkRoot() } returns RootStatus(false, "raw details")
        coEvery { files.save(capture(saved)) } returns Result.success("/tmp/device.pdf")
        val vm = viewModel(infoRepository, rootRepository, files)
        vm.handleEvent(InfoEvent.Load())
        advanceUntilIdle()

        for (language in listOf("it", "en", "de", "es", "fr")) {
            vm.updateLanguage(language)
            advanceUntilIdle()
            val content = vm.state.value.reportContent.orEmpty()
            assertTrue(content.startsWith(UseCaseTexts(language).label(ReportLabel.DEVICE_INFO_TITLE)))
            assertTrue(content.contains("raw details"))
            assertEquals(false, vm.state.value.reportLoading)
        }
        vm.handleEvent(InfoEvent.Export())
        advanceUntilIdle()
        assertEquals("device_info.pdf", saved.captured.fileName)
        assertTrue(saved.captured.textContent.startsWith(UseCaseTexts("fr").label(ReportLabel.DEVICE_INFO_TITLE)))
        coVerify(exactly = 1) { infoRepository.getDeviceInfo() }
        coVerify(exactly = 1) { rootRepository.checkRoot() }
        vm.onCleared()
    }

    @Test
    fun languageSwitchCannotPublishAnObsoleteDevicePreview() = runTest(dispatcher) {
        val infoRepository = mockk<DeviceInfoRepository>()
        val rootRepository = mockk<RootCheckRepository>()
        val builder = mockk<BuildDeviceInfoReportUseCase>()
        coEvery { infoRepository.getDeviceInfo() } returns deviceInfo()
        coEvery { rootRepository.checkRoot() } returns RootStatus(false, "")
        var cancelled = false
        coEvery { builder(any(), any(), any()) } coAnswers {
            val reportText = thirdArg<UseCaseReportText>()
            if (reportText.label(ReportLabel.DEVICE_INFO_TITLE) ==
                UseCaseTexts("en").label(ReportLabel.DEVICE_INFO_TITLE)
            ) {
                try {
                    delay(1_000)
                } finally {
                    cancelled = true
                }
            }
            Result.success(reportText.label(ReportLabel.DEVICE_INFO_TITLE))
        }
        val vm =
            InfoViewModel(
                GetDeviceInfoUseCase(infoRepository),
                CheckRootUseCase(rootRepository),
                ExportReportUseCase(mockk()),
                builder,
                TestAppDispatchers(dispatcher),
            )
        vm.handleEvent(InfoEvent.Load())
        runCurrent()
        vm.updateLanguage("de")
        advanceUntilIdle()

        assertTrue(cancelled)
        assertEquals(UseCaseTexts("de").label(ReportLabel.DEVICE_INFO_TITLE), vm.state.value.reportContent)
        assertEquals(false, vm.state.value.reportLoading)
        vm.onCleared()
    }

    @Test
    fun handleEvent_load_usingAssertEvent() = runTest(dispatcher) {
        val info = deviceInfo()
        val infoRepository = mockk<DeviceInfoRepository>()
        val rootRepository = mockk<RootCheckRepository>()
        coEvery { infoRepository.getDeviceInfo() } returns info
        coEvery { rootRepository.checkRoot() } returns RootStatus(true, "su available")
        val vm = viewModel(infoRepository, rootRepository)

        vm.assertEvent(InfoEvent.Load(), vm.state) {
            val initialState = awaitItem()
            val loadingState = awaitItem()
            val loadedState = awaitItem()
            assertEquals(info, loadedState.info)
            assertEquals(RootStatus(true, "su available"), loadedState.root)
            assertEquals(false, loadedState.loading)
            cancelAndIgnoreRemainingEvents()
        }
        vm.onCleared()
    }

    private fun viewModel(
        infoRepository: DeviceInfoRepository,
        rootRepository: RootCheckRepository,
        exporter: ReportFileRepository = mockk(relaxed = true),
        format: ExportFormat = ExportFormat.PDF,
    ) = InfoViewModel(
        GetDeviceInfoUseCase(infoRepository),
        CheckRootUseCase(rootRepository),
        ExportReportUseCase(
            exporter,
            mockk<GetExportFormatUseCase>().also {
                coEvery { it() } returns Result.success(format)
            },
        ),
        BuildDeviceInfoReportUseCase(dispatchers = TestAppDispatchers(dispatcher)),
        TestAppDispatchers(dispatcher),
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
        systemPropSource = "adb",
    )
}
