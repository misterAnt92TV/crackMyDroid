package com.crackmydroid.shared.domain.usecase.log

import com.crackmydroid.shared.domain.repository.log.LogRepository

class ExportLogsUseCase(private val repository: LogRepository) {
    suspend operator fun invoke(logs: List<String>): Result<String> = repository.export(logs)
}
