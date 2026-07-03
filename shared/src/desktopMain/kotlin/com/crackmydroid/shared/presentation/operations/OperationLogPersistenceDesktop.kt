package com.crackmydroid.shared.presentation.operations

import com.crackmydroid.shared.util.DesktopPaths

private const val SEP = "\u001f"

class OperationLogPersistenceDesktop : OperationLogPersistence {
    override suspend fun load(): List<OperationEntry> {
        val file = DesktopPaths.logsFile()
        if (!file.exists()) return emptyList()
        return file.readLines()
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val parts = line.split(SEP)
                if (parts.size < 3) return@mapNotNull null
                OperationEntry(
                    description = parts[0],
                    success = parts[1] == "1",
                    details = parts[2].ifEmpty { null },
                    timestamp = parts.getOrNull(3)?.toLongOrNull() ?: System.currentTimeMillis()
                )
            }
    }

    override suspend fun save(entries: List<OperationEntry>) {
        val encoded = entries.joinToString("\n") { entry ->
            listOf(
                entry.description.replace(SEP, " "),
                if (entry.success) "1" else "0",
                (entry.details ?: "").replace(SEP, " "),
                entry.timestamp.toString()
            ).joinToString(SEP)
        }
        DesktopPaths.logsFile().writeText(encoded)
    }
}

fun OperationLogStore.init() {
    configure(OperationLogPersistenceDesktop())
}
