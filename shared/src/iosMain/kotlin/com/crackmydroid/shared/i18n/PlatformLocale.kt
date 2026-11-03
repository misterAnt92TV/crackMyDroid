package com.crackmydroid.shared.i18n

import platform.Foundation.NSLocale

actual fun platformLanguage(): String = NSLocale.currentLocale.languageCode ?: "en"
