package com.crackmydroid.shared.domain.usecase.log

import com.crackmydroid.shared.domain.model.ExportDocument
import com.crackmydroid.shared.domain.model.ExportFormat
import com.crackmydroid.shared.domain.repository.ReportFileRepository
import com.crackmydroid.shared.domain.usecase.reports.ExportReportUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetExportFormatUseCase
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ExportLogsUseCaseTest {
    @Test
    fun `logs use selected format and preserve content`() = runTest {
        for (format in ExportFormat.entries) {
            val files = mockk<ReportFileRepository>()
            val settings = mockk<GetExportFormatUseCase>()
            val document = slot<ExportDocument>()
            coEvery { settings() } returns Result.success(format)
            coEvery { files.save(capture(document)) } returns Result.success("/logs.${format.extension}")

            val result = ExportLogsUseCase(ExportReportUseCase(files, settings))(
                listOf("I/App: started", "E/App: failed"),
            )

            assertEquals("/logs.${format.extension}", result.getOrThrow())
            assertEquals("crackmydroid-log.${format.extension}", document.captured.fileName)
            assertEquals("I/App: started\nE/App: failed", document.captured.textContent)
            assertEquals("logs", document.captured.reportType)
        }
    }
}
