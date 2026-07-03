package com.crackmydroid.shared.data.settings

import com.crackmydroid.shared.domain.model.AppTheme
import com.crackmydroid.shared.domain.model.ExportFormat
import com.crackmydroid.shared.domain.repository.SettingsRepository
import java.util.prefs.Preferences

class SettingsRepositoryDesktop : SettingsRepository {
    private val prefs = Preferences.userRoot().node("com.crackmydroid.desktop")

    override suspend fun getTheme(): AppTheme =
        prefs.get("theme", AppTheme.SYSTEM.name).toAppTheme()

    override suspend fun setTheme(theme: AppTheme) {
        prefs.put("theme", theme.name)
    }

    override suspend fun isVerboseLog(): Boolean = prefs.getBoolean("verbose_log", false)

    override suspend fun setVerboseLog(enabled: Boolean) {
        prefs.putBoolean("verbose_log", enabled)
    }

    override suspend fun isIntroSeen(): Boolean = prefs.getBoolean("intro_seen", false)

    override suspend fun setIntroSeen(seen: Boolean) {
        prefs.putBoolean("intro_seen", seen)
    }

    override suspend fun getExportFormat(): ExportFormat =
        prefs.get("export_format", ExportFormat.TXT.name).toExportFormat()

    override suspend fun setExportFormat(format: ExportFormat) {
        prefs.put("export_format", format.name)
    }

    override suspend fun isHighContrast(): Boolean = prefs.getBoolean("high_contrast", false)

    override suspend fun setHighContrast(enabled: Boolean) {
        prefs.putBoolean("high_contrast", enabled)
    }

    override suspend fun isReduceMotion(): Boolean = prefs.getBoolean("reduce_motion", false)

    override suspend fun setReduceMotion(enabled: Boolean) {
        prefs.putBoolean("reduce_motion", enabled)
    }

    override suspend fun isLargeText(): Boolean = prefs.getBoolean("large_text", false)

    override suspend fun setLargeText(enabled: Boolean) {
        prefs.putBoolean("large_text", enabled)
    }

    override suspend fun isSuggestionsEnabled(): Boolean = prefs.getBoolean("suggestions_enabled", true)

    override suspend fun setSuggestionsEnabled(enabled: Boolean) {
        prefs.putBoolean("suggestions_enabled", enabled)
    }

    override suspend fun isFeatureHintsEnabled(): Boolean = prefs.getBoolean("feature_hints_enabled", true)

    override suspend fun setFeatureHintsEnabled(enabled: Boolean) {
        prefs.putBoolean("feature_hints_enabled", enabled)
    }

    override suspend fun getAdbPath(): String = prefs.get("adb_path", "")

    override suspend fun setAdbPath(path: String) {
        prefs.put("adb_path", path)
    }

    private fun String.toAppTheme(): AppTheme =
        runCatching { AppTheme.valueOf(this) }.getOrDefault(AppTheme.SYSTEM)

    private fun String.toExportFormat(): ExportFormat =
        runCatching { ExportFormat.valueOf(this) }.getOrDefault(ExportFormat.TXT)
}
