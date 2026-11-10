package com.crackmydroid.shared.i18n.operation

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterShortStyle
import platform.Foundation.NSLocale

actual fun formatOperationTimestamp(timestamp: Long, languageCode: String): String {
    if (timestamp <= 0L) return "—"
    val formatter = NSDateFormatter().apply {
        locale = NSLocale(localeIdentifier = languageCode)
        dateStyle = NSDateFormatterShortStyle
        timeStyle = NSDateFormatterShortStyle
    }
    return formatter.stringFromDate(NSDate(timeIntervalSince1970 = timestamp / 1000.0)) ?: "—"
}
