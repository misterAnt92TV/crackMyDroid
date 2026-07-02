package com.crackmydroid.shared.i18n

import java.util.Locale

actual fun platformLanguage(): String = Locale.getDefault().language
