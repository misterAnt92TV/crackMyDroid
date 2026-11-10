package com.crackmydroid.shared.presentation

import com.crackmydroid.shared.domain.model.ExportDocument
import com.crackmydroid.shared.domain.model.ExportFormat
import com.crackmydroid.shared.domain.model.PlayIntegrityResult
import com.crackmydroid.shared.domain.model.RootStatus
import com.crackmydroid.shared.domain.model.root.RootCheckMode
import com.crackmydroid.shared.domain.repository.ReportFileRepository
import com.crackmydroid.shared.domain.repository.RootCheckRepository
import com.crackmydroid.shared.domain.usecase.reports.ExportReportUseCase
import com.crackmydroid.shared.domain.usecase.root.CheckPlayIntegrityUseCase
import com.crackmydroid.shared.domain.usecase.root.CheckRootUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetExportFormatUseCase
import com.crackmydroid.shared.i18n.stringsFor
import com.crackmydroid.shared.ui.root.RootViewModel
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
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class RootViewModelTest {
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
    fun verifyUpdatesRootStatus() = runTest(dispatcher) {
        val repository = mockk<RootCheckRepository>()
        coEvery { repository.checkRoot() } returns RootStatus(
            isRooted = true,
            details = "su available",
        )
        val viewModel = viewModel(repository)

        viewModel.verify()
        advanceUntilIdle()

        assertEquals(true, viewModel.state.value.status?.isRooted)
        assertEquals("su available", viewModel.state.value.status?.details)
        viewModel.onCleared()
    }

    @Test
    fun verifyPlayIntegrityUpdatesIntegrityResult() = runTest(dispatcher) {
        val repository = mockk<RootCheckRepository>()
        coEvery { repository.checkPlayIntegrity("nonce") } returns PlayIntegrityResult(
            basicIntegrity = true,
            deviceIntegrity = false,
            details = "device verdict",
        )
        val viewModel = viewModel(repository)

        viewModel.updatePlayIntegrityNonce("nonce")
        viewModel.verifyPlayIntegrity()
        advanceUntilIdle()

        assertEquals(true, viewModel.state.value.playIntegrity?.basicIntegrity)
        assertEquals(false, viewModel.state.value.playIntegrity?.deviceIntegrity)
        viewModel.onCleared()
    }

    @Test
    fun verify_failure_storesErrorAndStopsLoading() = runTest(dispatcher) {
        val repository = mockk<RootCheckRepository>()
        coEvery { repository.checkRoot() } throws IllegalStateException("root service unavailable")
        val viewModel = viewModel(repository)

        viewModel.verify()
        advanceUntilIdle()

        assertEquals(false, viewModel.state.value.loading)
        assertEquals("root service unavailable", viewModel.state.value.error)
        viewModel.onCleared()
    }

    @Test
    fun verifyRootAccess_isExplicitAndPublishesPrivilegeResult() = runTest(dispatcher) {
        val repository = mockk<RootCheckRepository>()
        coEvery { repository.checkRoot(RootCheckMode.PRIVILEGE_ACCESS) } returns RootStatus(
            isRooted = true,
            details = "uid=0(root)",
        )
        val viewModel = viewModel(repository)

        viewModel.verifyRootAccess()
        advanceUntilIdle()

        assertEquals(true, viewModel.state.value.status?.isRooted)
        assertEquals("uid=0(root)", viewModel.state.value.status?.details)
        coVerify(exactly = 1) { repository.checkRoot(RootCheckMode.PRIVILEGE_ACCESS) }
        viewModel.onCleared()
    }

    @Test
    fun shareReport_success_storesPathAndExportsRootDetails() = runTest(dispatcher) {
        val repository = mockk<RootCheckRepository>()
        val files = mockk<ReportFileRepository>()
        val format = mockk<GetExportFormatUseCase>()
        val shared = slot<ExportDocument>()
        coEvery { repository.checkRoot() } returns RootStatus(true, "su available")
        coEvery { format() } returns Result.success(ExportFormat.PDF)
        coEvery { files.share(capture(shared)) } returns Result.success("/tmp/root.pdf")
        val viewModel = RootViewModel(
            CheckRootUseCase(repository),
            CheckPlayIntegrityUseCase(repository),
            ExportReportUseCase(files, format),
        )
        viewModel.verify()
        advanceUntilIdle()

        viewModel.updateLanguage("en")
        viewModel.shareReport()
        advanceUntilIdle()

        assertEquals("/tmp/root.pdf", viewModel.state.value.reportPath)
        assertEquals("root_test_report.pdf", shared.captured.fileName)
        assertEquals("root_test", shared.captured.reportType)
        assertEquals(true, shared.captured.textContent.contains("su available"))
        coVerify(exactly = 1) { files.share(any<ExportDocument>()) }
        viewModel.onCleared()
    }

    private fun viewModel(repository: RootCheckRepository): RootViewModel = RootViewModel(
        CheckRootUseCase(repository),
        CheckPlayIntegrityUseCase(repository),
        ExportReportUseCase(mockk<ReportFileRepository>(relaxed = true)),
    )
}
