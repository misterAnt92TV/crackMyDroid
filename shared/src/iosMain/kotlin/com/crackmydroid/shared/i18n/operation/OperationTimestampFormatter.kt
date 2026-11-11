package com.crackmydroid.shared.i18n.operation

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterShortStyle
import platform.Foundation.NSLocale

private const val SECONDS_BETWEEN_1970_AND_2001 = 978307200.0

actual fun formatOperationTimestamp(timestamp: Long, languageCode: String): String {
    if (timestamp <= 0L) return "—"
    val formatter = NSDateFormatter().apply {
        locale = NSLocale(localeIdentifier = languageCode)
        dateStyle = NSDateFormatterShortStyle
        timeStyle = NSDateFormatterShortStyle
    }
    val date = NSDate(timeIntervalSinceReferenceDate = (timestamp / 1000.0) - SECONDS_BETWEEN_1970_AND_2001)
    return formatter.stringFromDate(date)
}
