package com.crackmydroid.shared.data.log

import android.content.Context
import com.crackmydroid.shared.domain.repository.LogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class LogRepositoryAndroid(private val context: Context) : LogRepository {
    override suspend fun fetchLog(): List<String> = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec(arrayOf("logcat", "-d", "-t", "1500", "-v", "time"))
            process.inputStream.bufferedReader().readLines()
        } catch (t: Throwable) {
            listOf("Errore lettura log: ${t.message}")
        }
    }

    override suspend fun export(logs: List<String>): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(context.cacheDir, "crackmydroid-log.txt")
            file.writeText(logs.joinToString("\n"))
            file.absolutePath
        }
    }
}
