package com.crackmydroid.shared.presentation.operations

import com.crackmydroid.shared.domain.model.OperationEntry
import com.crackmydroid.shared.domain.model.OperationEvent

/** Version-compatible codec shared by Android DataStore and Desktop file persistence. */
object OperationLogCodec {
    private const val SEPARATOR = "\u001f"
    private const val ARGUMENT_SEPARATOR = "\u001e"

    fun decode(raw: String, now: () -> Long): List<OperationEntry> = raw.lines()
        .filter(String::isNotBlank)
        .mapNotNull { line ->
            val parts = line.split(SEPARATOR)
            if (parts.size < 3) return@mapNotNull null
            val event = parts.getOrNull(4)?.takeIf(String::isNotBlank)?.let { key ->
                OperationEvent(key, parts.getOrNull(5).orEmpty().split(ARGUMENT_SEPARATOR).filter(String::isNotEmpty))
            }
            OperationEntry(
                description = parts[0],
                success = parts[1] == "1",
                details = parts[2].ifEmpty { null },
                timestamp = parts.getOrNull(3)?.toLongOrNull() ?: now(),
                event = event,
                legacyDescription = if (event == null) parts[0] else null,
            )
        }

    fun encode(entries: List<OperationEntry>): String = entries.joinToString("\n") { entry ->
        val event = entry.event
        listOf(
            sanitize(entry.legacyDescription ?: entry.description),
            if (entry.success) "1" else "0",
            sanitize(entry.details.orEmpty()),
            entry.timestamp.toString(),
            event?.key.orEmpty(),
            event?.arguments.orEmpty().joinToString(ARGUMENT_SEPARATOR, transform = ::sanitize),
        ).joinToString(SEPARATOR)
    }

    private fun sanitize(value: String): String = value.replace(SEPARATOR, " ").replace(ARGUMENT_SEPARATOR, " ")
}
