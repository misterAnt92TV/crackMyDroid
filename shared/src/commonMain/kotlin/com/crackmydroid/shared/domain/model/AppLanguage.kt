package com.crackmydroid.shared.domain.model

enum class AppLanguage(val code: String, val displayName: String) {
    SYSTEM("system", "System"),
    ENGLISH("en", "English"), ITALIAN("it", "Italiano"), GERMAN("de", "Deutsch"),
    FRENCH("fr", "Français"), SPANISH("es", "Español");

    companion object {
        fun fromCode(code: String?): AppLanguage {
            val normalizedCode = code?.trim()?.lowercase()
            if (normalizedCode == SYSTEM.code) return SYSTEM
            return entries.firstOrNull { it.code == normalizedCode?.take(2) } ?: SYSTEM
        }
    }
}
