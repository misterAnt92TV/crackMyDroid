package com.crackmydroid.shared.i18n

import com.crackmydroid.shared.domain.model.app.AppLanguage

fun resolveLanguageCode(language: AppLanguage, systemLanguage: String): String =
    if (language == AppLanguage.SYSTEM) systemLanguage.lowercase().take(2) else language.code
