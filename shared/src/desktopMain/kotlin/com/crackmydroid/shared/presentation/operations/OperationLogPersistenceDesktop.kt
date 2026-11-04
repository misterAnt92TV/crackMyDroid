package com.crackmydroid.shared.presentation.operations

import com.crackmydroid.shared.domain.model.operation.OperationEntry
import com.crackmydroid.shared.util.DesktopPaths

class OperationLogPersistenceDesktop : OperationLogPersistence {
    override suspend fun load(): List<OperationEntry> {
        val file = DesktopPaths.logsFile()
        if (!file.exists()) return emptyList()
        return OperationLogCodec.decode(file.readText()) { System.currentTimeMillis() }
    }

    override suspend fun save(entries: List<OperationEntry>) {
        val encoded = OperationLogCodec.encode(entries)
        DesktopPaths.logsFile().writeText(encoded)
    }
}
