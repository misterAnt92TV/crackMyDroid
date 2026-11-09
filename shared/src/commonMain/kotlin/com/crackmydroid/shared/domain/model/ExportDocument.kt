package com.crackmydroid.shared.domain.model

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** A report that can be rendered by a platform-specific export adapter. */
data class ExportDocument(
    val baseName: String,
    val reportType: String,
    val textContent: String,
    val data: JsonElement,
    val format: ExportFormat,
) {
    val fileName: String get() = "$baseName.${format.extension}"

    companion object {
        fun textOnly(baseName: String, reportType: String, textContent: String, format: ExportFormat): ExportDocument =
            ExportDocument(
                baseName = baseName,
                reportType = reportType,
                textContent = textContent,
                data = buildJsonObject { put("content", textContent) },
                format = format,
            )
    }

    fun jsonEnvelope(): JsonObject = buildJsonObject {
        put("schemaVersion", 1)
        put("reportType", reportType)
        put("data", data)
    }
}
