package com.crackmydroid.shared.domain.repository

interface ReportFileRepository {
    suspend fun save(fileName: String, content: String): Result<String>
    suspend fun share(path: String): Result<Unit>
}