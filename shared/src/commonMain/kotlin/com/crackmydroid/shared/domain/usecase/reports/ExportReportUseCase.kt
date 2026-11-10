package com.crackmydroid.shared.domain.usecase.reports

import co.touchlab.kermit.Logger
import com.crackmydroid.architecture.domain.usecase.BaseUseCase
import com.crackmydroid.architecture.logging.AppLogger
import com.crackmydroid.shared.domain.model.ExportDocument
import com.crackmydroid.shared.domain.model.ExportFormat
import com.crackmydroid.shared.domain.repository.ReportFileRepository
import com.crackmydroid.shared.domain.usecase.settings.GetExportFormatUseCase
import kotlinx.serialization.json.JsonElement

class ExportReportUseCase(
    private val reportFileRepository: ReportFileRepository,
    private val getExportFormat: GetExportFormatUseCase? = null,
    logger: Logger = AppLogger.logger,
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

    suspend fun save(document: ExportDocument): Result<String> =
        execute("save_${document.reportType}") { reportFileRepository.save(document).getOrThrow() }

    suspend fun share(document: ExportDocument): Result<String> =
        execute("share_${document.reportType}") { reportFileRepository.share(document).getOrThrow() }

    suspend fun saveTextReport(baseName: String, reportType: String, textContent: String): Result<String> =
        execute("save_$reportType") {
            reportFileRepository.save(textDocument(baseName, reportType, textContent)).getOrThrow()
        }

    suspend fun shareTextReport(baseName: String, reportType: String, textContent: String): Result<String> =
        execute("share_$reportType") {
            reportFileRepository.share(textDocument(baseName, reportType, textContent)).getOrThrow()
        }

    suspend fun save(baseName: String, reportType: String, textContent: String, data: JsonElement): Result<String> =
        save(document(baseName, reportType, textContent, data))

    suspend fun share(baseName: String, reportType: String, textContent: String, data: JsonElement): Result<String> =
        share(document(baseName, reportType, textContent, data))

    private suspend fun document(
        baseName: String,
        reportType: String,
        textContent: String,
        data: JsonElement,
    ): ExportDocument = ExportDocument(
        baseName = baseName,
        reportType = reportType,
        textContent = textContent,
        data = data,
        format = selectedFormat(),
    )

    private suspend fun textDocument(baseName: String, reportType: String, textContent: String): ExportDocument =
        ExportDocument.textOnly(
            baseName = baseName,
            reportType = reportType,
            textContent = textContent,
            format = selectedFormat(),
        )

    private suspend fun selectedFormat(): ExportFormat = checkNotNull(getExportFormat) {
        "GetExportFormatUseCase is required to export a report in the selected format"
    }().getOrThrow()
}
