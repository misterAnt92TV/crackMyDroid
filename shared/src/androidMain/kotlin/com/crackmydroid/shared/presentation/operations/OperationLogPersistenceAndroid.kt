package com.crackmydroid.shared.presentation.operations

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.crackmydroid.shared.domain.model.operation.OperationEntry
import kotlinx.coroutines.flow.first

private val Context.operationLogDataStore by preferencesDataStore(name = "operation_log")

private val logKey = stringPreferencesKey("entries")
class OperationLogPersistenceAndroid(private val context: Context) : OperationLogPersistence {
    override suspend fun load(): List<OperationEntry> {
        val prefs = context.operationLogDataStore.data.first()
        val raw = prefs[logKey] ?: return emptyList()
        return OperationLogCodec.decode(raw) { System.currentTimeMillis() }
    }

    override suspend fun save(entries: List<OperationEntry>) {
        val encoded = OperationLogCodec.encode(entries)
        context.operationLogDataStore.edit { prefs ->
            prefs[logKey] = encoded
        }
    }
}
