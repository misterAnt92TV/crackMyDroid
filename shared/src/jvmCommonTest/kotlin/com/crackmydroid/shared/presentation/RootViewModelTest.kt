package com.crackmydroid.shared.presentation

import com.crackmydroid.shared.domain.model.PlayIntegrityResult
import com.crackmydroid.shared.domain.model.RootStatus
import com.crackmydroid.shared.domain.repository.ReportFileRepository
import com.crackmydroid.shared.domain.repository.RootCheckRepository
import com.crackmydroid.shared.domain.usecase.CheckPlayIntegrityUseCase
import com.crackmydroid.shared.domain.usecase.CheckRootUseCase
import com.crackmydroid.shared.domain.usecase.reports.ExportReportUseCase
import com.crackmydroid.shared.i18n.stringsFor
import com.crackmydroid.shared.ui.root.RootViewModel
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
            details = "su available"
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
            details = "device verdict"
        )
        val viewModel = viewModel(repository)

        viewModel.verifyPlayIntegrity("nonce")
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
    fun shareReport_success_storesPathAndExportsRootDetails() = runTest(dispatcher) {
        val repository = mockk<RootCheckRepository>()
        val files = mockk<ReportFileRepository>()
        coEvery { repository.checkRoot() } returns RootStatus(true, "su available")
        coEvery { files.save(any(), any()) } returns Result.success("/tmp/root.txt")
        coEvery { files.share("/tmp/root.txt") } returns Result.success(Unit)
        val viewModel = RootViewModel(
            CheckRootUseCase(repository),
            CheckPlayIntegrityUseCase(repository),
            ExportReportUseCase(files)
        )
        viewModel.verify()
        advanceUntilIdle()

        viewModel.shareReport(stringsFor("en"))
        advanceUntilIdle()

        assertEquals("/tmp/root.txt", viewModel.state.value.reportPath)
        coVerify { files.save("root_test_report", match { it.contains("su available") }) }
        coVerify { files.share("/tmp/root.txt") }
        viewModel.onCleared()
    }

    private fun viewModel(repository: RootCheckRepository): RootViewModel = RootViewModel(
        CheckRootUseCase(repository),
        CheckPlayIntegrityUseCase(repository),
        ExportReportUseCase(mockk<ReportFileRepository>(relaxed = true))
    )
}
