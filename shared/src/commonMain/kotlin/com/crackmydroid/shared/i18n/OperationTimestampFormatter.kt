package com.crackmydroid.shared.i18n

/** Formats persisted operation timestamps using the app language and platform time zone. */
expect fun formatOperationTimestamp(timestamp: Long, languageCode: String): String
