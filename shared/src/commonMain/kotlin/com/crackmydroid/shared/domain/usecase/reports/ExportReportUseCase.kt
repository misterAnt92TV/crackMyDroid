package com.crackmydroid.shared.domain.usecase.reports

import com.crackmydroid.shared.domain.repository.ReportFileRepository

class ExportReportUseCase(
    private val reportFileRepository: ReportFileRepository
) {
    suspend fun save(fileName: String, content: String): Result<String> =
        reportFileRepository.save(fileName, content)

    suspend fun share(fileName: String, content: String): Result<String> = runCatching {
        val path = reportFileRepository.save(fileName, content).getOrThrow()
        reportFileRepository.share(path).getOrThrow()
        path
    }
}
