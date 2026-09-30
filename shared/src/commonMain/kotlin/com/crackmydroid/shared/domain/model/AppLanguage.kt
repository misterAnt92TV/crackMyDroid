package com.crackmydroid.shared.domain.model

enum class AppLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"), ITALIAN("it", "Italiano"), GERMAN("de", "Deutsch"),
    FRENCH("fr", "Français"), SPANISH("es", "Español");

    companion object {
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code == code?.lowercase()?.take(2) } ?: ENGLISH
    }
}
