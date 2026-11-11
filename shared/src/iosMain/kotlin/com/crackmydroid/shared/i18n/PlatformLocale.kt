package com.crackmydroid.shared.i18n

import platform.Foundation.NSLocale
import platform.Foundation.preferredLanguages

actual fun platformLanguage(): String {
    val preferred = NSLocale.preferredLanguages.firstOrNull() as? String
    return preferred?.takeWhile { it != '-' && it != '_' } ?: "en"
}
