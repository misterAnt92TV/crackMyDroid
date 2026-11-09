package com.crackmydroid.shared.i18n

import java.text.DateFormat
import java.util.Date
import java.util.Locale

actual fun formatOperationTimestamp(timestamp: Long, languageCode: String): String {
    if (timestamp <= 0L) return "—"
    return runCatching {
        DateFormat.getDateTimeInstance(
            DateFormat.SHORT,
            DateFormat.SHORT,
            Locale.forLanguageTag(languageCode),
        ).format(Date(timestamp))
    }.getOrDefault("—")
}
