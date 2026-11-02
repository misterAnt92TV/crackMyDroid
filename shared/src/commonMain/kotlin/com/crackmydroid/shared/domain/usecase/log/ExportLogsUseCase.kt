package com.crackmydroid.shared.domain.usecase

import com.crackmydroid.shared.domain.repository.LogRepository

class ExportLogsUseCase(private val repository: LogRepository) {
    suspend operator fun invoke(logs: List<String>): Result<String> = repository.export(logs)
}
