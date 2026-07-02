package com.crackmydroid.shared.domain.usecase

import com.crackmydroid.shared.domain.repository.LogRepository

class GetLogsUseCase(private val repo: LogRepository) {
    suspend operator fun invoke(): List<String> = repo.fetchLog()
}

class ExportLogsUseCase(private val repo: LogRepository) {
    suspend operator fun invoke(logs: List<String>): Result<String> = repo.export(logs)
}
