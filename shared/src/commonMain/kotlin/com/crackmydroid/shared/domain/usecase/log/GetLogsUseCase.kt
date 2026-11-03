package com.crackmydroid.shared.domain.usecase.log

import com.crackmydroid.shared.domain.repository.LogRepository

class GetLogsUseCase(private val repository: LogRepository) {
    suspend operator fun invoke(): List<String> = repository.fetchLog()
}
