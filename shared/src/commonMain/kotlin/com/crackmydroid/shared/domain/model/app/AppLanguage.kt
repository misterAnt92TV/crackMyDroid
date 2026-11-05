package com.crackmydroid.shared.domain.model.app

enum class AppLanguage(val code: String) {
    SYSTEM("system"),
    ENGLISH("en"), ITALIAN("it"), GERMAN("de"),
    FRENCH("fr"), SPANISH("es");

    companion object {
        fun fromCode(code: String?): AppLanguage {
            val normalizedCode = code?.trim()?.lowercase()
            if (normalizedCode == SYSTEM.code) return SYSTEM
            return entries.firstOrNull { it.code == normalizedCode?.take(2) } ?: SYSTEM
        }
    }
}
