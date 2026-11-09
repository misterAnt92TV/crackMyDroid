package com.crackmydroid.shared.data.reports

import com.crackmydroid.shared.domain.model.ExportDocument
import com.crackmydroid.shared.domain.model.ExportFormat
import com.crackmydroid.shared.domain.repository.ReportFileRepository
import com.crackmydroid.shared.util.savePdfFile
import com.crackmydroid.shared.util.saveTextFile
import com.crackmydroid.shared.util.shareTextFile

class ReportFileRepositoryIos : ReportFileRepository {
    override suspend fun save(fileName: String, content: String): Result<String> = saveTextFile(fileName, content)

    override suspend fun share(path: String): Result<Unit> = shareTextFile(path)

    override suspend fun save(document: ExportDocument): Result<String> = when (document.format) {
        ExportFormat.TXT -> saveTextFile(document.fileName, document.textContent)
        ExportFormat.JSON -> saveTextFile(document.fileName, document.jsonEnvelope().toString())
        ExportFormat.PDF -> savePdfFile(document.fileName, document.textContent)
    }
}
