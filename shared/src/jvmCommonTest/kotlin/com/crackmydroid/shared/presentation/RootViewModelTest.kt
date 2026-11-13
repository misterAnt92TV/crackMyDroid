package com.crackmydroid.shared.presentation

import com.crackmydroid.architecture.testing.*


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
import com.crackmydroid.shared.ui.root.RootEvent
import com.crackmydroid.shared.ui.root.RootViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
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

class RootViewModelTest : BaseViewModelUnitTest() {
    @Test
    fun verifyUpdatesRootStatus() = runViewModelTest {
        val repository = mockk<RootCheckRepository>()
        coEvery { repository.checkRoot() } returns RootStatus(
            isRooted = true,
            details = "su available",
        )
        val viewModel = viewModel(repository)

        val state = viewModel.emitEventAndAwaitIdle(RootEvent.Verify())

        assertEquals(true, state.status?.isRooted)
        assertEquals("su available", state.status?.details)
    }

    @Test
    fun verifyPlayIntegrityUpdatesIntegrityResult() = runViewModelTest {
        val repository = mockk<RootCheckRepository>()
        coEvery { repository.checkPlayIntegrity("nonce") } returns PlayIntegrityResult(
            basicIntegrity = true,
            deviceIntegrity = false,
            details = "device verdict",
        )
        val viewModel = viewModel(repository)

        viewModel.emitEventAndAwaitIdle(RootEvent.PlayIntegrityNonceChanged("nonce"))
        val state = viewModel.emitEventAndAwaitIdle(RootEvent.VerifyPlayIntegrity())

        assertEquals(true, state.playIntegrity?.basicIntegrity)
        assertEquals(false, state.playIntegrity?.deviceIntegrity)
    }

    @Test
    fun verify_failure_storesErrorAndStopsLoading() = runViewModelTest {
        val repository = mockk<RootCheckRepository>()
        coEvery { repository.checkRoot() } throws IllegalStateException("root service unavailable")
        val viewModel = viewModel(repository)

        val state = viewModel.emitEventAndAwaitIdle(RootEvent.Verify())

        assertEquals(false, state.loading)
        assertEquals("root service unavailable", state.loadError)
    }

    @Test
    fun verifyRootAccess_isExplicitAndPublishesPrivilegeResult() = runViewModelTest {
        val repository = mockk<RootCheckRepository>()
        coEvery { repository.checkRoot(RootCheckMode.PRIVILEGE_ACCESS) } returns RootStatus(
            isRooted = true,
            details = "uid=0(root)",
        )
        val viewModel = viewModel(repository)

        val state = viewModel.emitEventAndAwaitIdle(RootEvent.VerifyRootAccess())

        assertEquals(true, state.status?.isRooted)
        assertEquals("uid=0(root)", state.status?.details)
        coVerify(exactly = 1) { repository.checkRoot(RootCheckMode.PRIVILEGE_ACCESS) }
    }

    @Test
    fun shareReport_success_storesPathAndExportsRootDetails() = runViewModelTest {
        val repository = mockk<RootCheckRepository>()
        val files = mockk<ReportFileRepository>()
        val format = mockk<GetExportFormatUseCase>()
        val shared = slot<ExportDocument>()
        coEvery { repository.checkRoot() } returns RootStatus(true, "su available")
        coEvery { format() } returns Result.success(ExportFormat.PDF)
        coEvery { files.share(capture(shared)) } returns Result.success("/tmp/root.pdf")
        val viewModel = autoClear(
            RootViewModel(
                CheckRootUseCase(repository),
                CheckPlayIntegrityUseCase(repository),
                ExportReportUseCase(files, format),
                appDispatchers,
            ),
        )
        viewModel.emitEventAndAwaitIdle(RootEvent.Verify())

        viewModel.updateLanguage("en")
        val state = viewModel.emitEventAndAwaitIdle(RootEvent.ShareReport())

        assertEquals("/tmp/root.pdf", state.reportPath)
        assertEquals("root_test_report.pdf", shared.captured.fileName)
        assertEquals("root_test", shared.captured.reportType)
        assertEquals(true, shared.captured.textContent.contains("su available"))
        coVerify(exactly = 1) { files.share(any<ExportDocument>()) }
    }

    private fun viewModel(repository: RootCheckRepository): RootViewModel = autoClear(
        RootViewModel(
            CheckRootUseCase(repository),
            CheckPlayIntegrityUseCase(repository),
            ExportReportUseCase(mockk<ReportFileRepository>(relaxed = true)),
            appDispatchers,
        ),
    )
}
