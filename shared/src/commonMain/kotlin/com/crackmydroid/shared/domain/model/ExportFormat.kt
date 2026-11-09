package com.crackmydroid.shared.domain.model

enum class ExportFormat(
    val extension: String,
    val mimeType: String,
) {
    TXT("txt", "text/plain"),
    JSON("json", "application/json"),
    PDF("pdf", "application/pdf"),
}
