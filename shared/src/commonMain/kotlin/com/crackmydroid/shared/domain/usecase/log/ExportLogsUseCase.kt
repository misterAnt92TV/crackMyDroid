package com.crackmydroid.shared.domain.usecase.log

import co.touchlab.kermit.Logger
import com.crackmydroid.shared.AppLogger
import com.crackmydroid.shared.domain.usecase.BaseUseCase
import com.crackmydroid.shared.domain.usecase.reports.ExportReportUseCase

class ExportLogsUseCase(
    private val reportExporter: ExportReportUseCase,
    logger: Logger = AppLogger.logger,
) : BaseUseCase(TAG, logger = logger) {
    private companion object {
        private const val TAG = "ExportLogsUseCase"
    }

    suspend operator fun invoke(logs: List<String>): Result<String> =
        execute {
            reportExporter.saveTextReport(
                baseName = "crackmydroid-log",
                reportType = "logs",
                textContent = logs.joinToString("\n"),
            ).getOrThrow()
        }
}
