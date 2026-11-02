package com.crackmydroid.shared.domain.usecase

import com.crackmydroid.shared.domain.repository.LogRepository

class GetLogsUseCase(private val repository: LogRepository) {
    suspend operator fun invoke(): List<String> = repository.fetchLog()
}
