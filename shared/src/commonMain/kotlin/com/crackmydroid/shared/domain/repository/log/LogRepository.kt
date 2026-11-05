package com.crackmydroid.shared.domain.repository.log

interface LogRepository {
    suspend fun fetchLog(): List<String>
    suspend fun export(logs: List<String>): Result<String>
}
