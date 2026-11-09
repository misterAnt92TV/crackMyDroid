package com.crackmydroid.shared.domain.repository.log

interface LogRepository {
    suspend fun fetchLog(): List<String>
}
