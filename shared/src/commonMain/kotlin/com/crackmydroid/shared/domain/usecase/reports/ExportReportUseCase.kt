package com.crackmydroid.shared.domain.usecase.reports

import com.crackmydroid.shared.domain.repository.ReportFileRepository
import co.touchlab.kermit.Logger
import com.crackmydroid.shared.AppLogger
import com.crackmydroid.shared.domain.usecase.BaseUseCase

class ExportReportUseCase(
    private val reportFileRepository: ReportFileRepository,
    logger: Logger = AppLogger.logger
) : BaseUseCase(TAG, logger = logger) {
    private companion object {
        private const val TAG = "ExportReportUseCase"
    }

    suspend fun save(fileName: String, content: String): Result<String> =
        execute("save") { reportFileRepository.save(fileName, content).getOrThrow() }

    suspend fun share(fileName: String, content: String): Result<String> = execute("share") {
        val path = reportFileRepository.save(fileName, content).getOrThrow()
        reportFileRepository.share(path).getOrThrow()
        path
    }
}
