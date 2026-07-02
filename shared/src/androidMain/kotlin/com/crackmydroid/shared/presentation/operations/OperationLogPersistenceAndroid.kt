package com.crackmydroid.shared.presentation.operations

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.operationLogDataStore by preferencesDataStore(name = "operation_log")

private val logKey = stringPreferencesKey("entries")
private const val SEP = "\u001f" // unit separator

class OperationLogPersistenceAndroid(private val context: Context) : OperationLogPersistence {
    override suspend fun load(): List<OperationEntry> {
        val prefs = context.operationLogDataStore.data.first()
        val raw = prefs[logKey] ?: return emptyList()
        return raw.lines()
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val parts = line.split(SEP)
                if (parts.size < 3) return@mapNotNull null
                val desc = parts[0]
                val success = parts[1] == "1"
                val details = parts[2].ifEmpty { null }
                val ts = parts.getOrNull(3)?.toLongOrNull() ?: System.currentTimeMillis()
                OperationEntry(desc, success, details, ts)
            }
    }

    override suspend fun save(entries: List<OperationEntry>) {
        val encoded = entries.joinToString("\n") { e ->
            listOf(
                e.description.replace(SEP, " "),
                if (e.success) "1" else "0",
                (e.details ?: "").replace(SEP, " "),
                e.timestamp.toString()
            ).joinToString(SEP)
        }
        context.operationLogDataStore.edit { prefs ->
            prefs[logKey] = encoded
        }
    }
}

fun OperationLogStore.init(context: Context) {
    configure(OperationLogPersistenceAndroid(context))
}
