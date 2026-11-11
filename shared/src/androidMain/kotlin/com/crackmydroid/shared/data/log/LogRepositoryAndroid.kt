package com.crackmydroid.shared.data.log

import com.crackmydroid.shared.domain.repository.log.LogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext

class LogRepositoryAndroid : LogRepository {
    override suspend fun fetchLog(): List<String> = withContext(Dispatchers.IO) {
        coroutineScope {
            val process =
                Runtime.getRuntime().exec(arrayOf("logcat", "-d", "-t", "1500", "-v", "time"))
            try {
                val logs = async { process.inputStream.bufferedReader().use { it.readLines() } }
                val errors = async { process.errorStream.bufferedReader().use { it.readText() } }
                val exitCode = runInterruptible { process.waitFor() }
                val errorOutput = errors.await()
                check(exitCode == 0) {
                    errorOutput.ifBlank { "logcat exited with code $exitCode" }
                }
                logs.await()
            } finally {
                process.destroy()
            }
        }
    }
}
