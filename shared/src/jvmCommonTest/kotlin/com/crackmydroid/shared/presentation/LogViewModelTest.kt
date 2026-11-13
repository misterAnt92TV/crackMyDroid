package com.crackmydroid.shared.presentation

import com.crackmydroid.architecture.testing.*


import com.crackmydroid.architecture.coroutines.AppDispatchers
import com.crackmydroid.shared.domain.model.ExportDocument
import com.crackmydroid.shared.domain.model.ExportFormat
import com.crackmydroid.shared.domain.repository.ReportFileRepository
import com.crackmydroid.shared.domain.repository.log.LogRepository
import com.crackmydroid.shared.domain.usecase.log.ExportLogsUseCase
import com.crackmydroid.shared.domain.usecase.log.GetLogsUseCase
import com.crackmydroid.shared.domain.usecase.reports.ExportReportUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetExportFormatUseCase
import com.crackmydroid.shared.ui.logs.LogEvent
import com.crackmydroid.shared.ui.logs.LogViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class LogViewModelTest : BaseViewModelUnitTest() {
    @Test
    fun refreshFiltersLogsByTextAndLevel() = runViewModelTest {
        val repository = mockk<LogRepository>()
        coEvery { repository.fetchLog() } returns listOf(
            "I/App: started",
            "E/App: failed",
            "W/App: retrying",
        )
        val viewModel = autoClear(
            LogViewModel(
                GetLogsUseCase(repository),
                ExportLogsUseCase(
                    ExportReportUseCase(mockk<ReportFileRepository>(), mockk<GetExportFormatUseCase>()),
                ),
                appDispatchers,
            ),
        )

        viewModel.emitEventAndAwaitIdle(LogEvent.Refresh())
        val filteredState = viewModel.emitEventAndAwaitIdle(LogEvent.FilterChanged("app: failed"))

        assertEquals(listOf("E/App: failed"), filteredState.visibleLogs)
    }

    @Test
    fun exportUsesCurrentlyVisibleLogs() = runViewModelTest {
        val repository = mockk<LogRepository>()
        val reportFiles = mockk<ReportFileRepository>()
        val format = mockk<GetExportFormatUseCase>()
        coEvery { repository.fetchLog() } returns listOf("I/App: started", "E/App: failed")
        coEvery { format() } returns Result.success(ExportFormat.TXT)
        coEvery { reportFiles.save(any<ExportDocument>()) } returns Result.success("/tmp/log.txt")
        val viewModel = autoClear(
            LogViewModel(
                GetLogsUseCase(repository),
                ExportLogsUseCase(ExportReportUseCase(reportFiles, format)),
                appDispatchers,
            ),
        )

        viewModel.emitEventAndAwaitIdle(LogEvent.Refresh())
        viewModel.emitEventAndAwaitIdle(LogEvent.FilterChanged("failed"))
        val exportState = viewModel.emitEventAndAwaitIdle(LogEvent.Export())

        coVerify {
            reportFiles.save(match { it.fileName == "crackmydroid-log.txt" && it.textContent == "E/App: failed" })
        }
        assertEquals("/tmp/log.txt", exportState.message)
    }
}
