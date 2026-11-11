package com.crackmydroid.shared.domain.repository

import com.crackmydroid.shared.domain.model.ExportDocument

interface ReportFileRepository {
    suspend fun save(fileName: String, content: String): Result<String>
    suspend fun share(path: String): Result<Unit>

    suspend fun save(document: ExportDocument): Result<String> = save(document.fileName, document.textContent)

    suspend fun share(document: ExportDocument): Result<String> {
        val path = save(document).getOrThrow()
        share(path).getOrThrow()
        return Result.success(path)
    }
}
