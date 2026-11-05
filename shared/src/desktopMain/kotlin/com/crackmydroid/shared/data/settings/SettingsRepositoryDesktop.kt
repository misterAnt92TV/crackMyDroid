package com.crackmydroid.shared.data.settings

import com.crackmydroid.shared.domain.model.app.AppLanguage
import com.crackmydroid.shared.domain.model.app.AppTheme
import com.crackmydroid.shared.domain.model.ExportFormat
import com.crackmydroid.shared.domain.repository.SettingsRepository
import com.crackmydroid.shared.data.settings.SettingsPreferenceKeys.ADB_PATH
import com.crackmydroid.shared.data.settings.SettingsPreferenceKeys.ACTIVITY_FAVORITES
import com.crackmydroid.shared.data.settings.SettingsPreferenceKeys.ACTIVITY_PACKAGE_FAVORITES
import com.crackmydroid.shared.data.settings.SettingsPreferenceKeys.EXPORT_FORMAT
import com.crackmydroid.shared.data.settings.SettingsPreferenceKeys.FEATURE_HINTS_ENABLED
import com.crackmydroid.shared.data.settings.SettingsPreferenceKeys.HIGH_CONTRAST
import com.crackmydroid.shared.data.settings.SettingsPreferenceKeys.INSTALLED_APP_FAVORITES
import com.crackmydroid.shared.data.settings.SettingsPreferenceKeys.INTRO_SEEN
import com.crackmydroid.shared.data.settings.SettingsPreferenceKeys.LARGE_TEXT
import com.crackmydroid.shared.data.settings.SettingsPreferenceKeys.LANGUAGE
import com.crackmydroid.shared.data.settings.SettingsPreferenceKeys.PERMISSION_FAVORITES
import com.crackmydroid.shared.data.settings.SettingsPreferenceKeys.REDUCE_MOTION
import com.crackmydroid.shared.data.settings.SettingsPreferenceKeys.SUGGESTIONS_ENABLED
import com.crackmydroid.shared.data.settings.SettingsPreferenceKeys.THEME
import com.crackmydroid.shared.data.settings.SettingsPreferenceKeys.VERBOSE_LOG
import java.util.prefs.Preferences

class SettingsRepositoryDesktop : SettingsRepository {
    private val prefs = Preferences.userRoot().node("com.crackmydroid.desktop")
    override suspend fun getLanguage() = AppLanguage.fromCode(prefs.get(LANGUAGE, AppLanguage.SYSTEM.code))
    override suspend fun setLanguage(language: AppLanguage) {
        prefs.put(LANGUAGE, language.code)
    }

    private fun getSet(key: String) =
        prefs.get(key, "").split('\n').filter(String::isNotEmpty).toSet()

    private fun setSet(key: String, values: Set<String>) {
        prefs.put(key, values.joinToString("\n"))
    }

    override suspend fun getTheme(): AppTheme =
        prefs.get(THEME, AppTheme.SYSTEM.name).toAppTheme()

    override suspend fun setTheme(theme: AppTheme) {
        prefs.put(THEME, theme.name)
    }

    override suspend fun isVerboseLog(): Boolean = prefs.getBoolean(VERBOSE_LOG, false)

    override suspend fun setVerboseLog(enabled: Boolean) {
        prefs.putBoolean(VERBOSE_LOG, enabled)
    }

    override suspend fun isIntroSeen(): Boolean = prefs.getBoolean(INTRO_SEEN, false)

    override suspend fun setIntroSeen(seen: Boolean) {
        prefs.putBoolean(INTRO_SEEN, seen)
    }

    override suspend fun getExportFormat(): ExportFormat =
        prefs.get(EXPORT_FORMAT, ExportFormat.TXT.name).toExportFormat()

    override suspend fun setExportFormat(format: ExportFormat) {
        prefs.put(EXPORT_FORMAT, format.name)
    }

    override suspend fun isHighContrast(): Boolean = prefs.getBoolean(HIGH_CONTRAST, false)

    override suspend fun setHighContrast(enabled: Boolean) {
        prefs.putBoolean(HIGH_CONTRAST, enabled)
    }

    override suspend fun isReduceMotion(): Boolean = prefs.getBoolean(REDUCE_MOTION, false)

    override suspend fun setReduceMotion(enabled: Boolean) {
        prefs.putBoolean(REDUCE_MOTION, enabled)
    }

    override suspend fun isLargeText(): Boolean = prefs.getBoolean(LARGE_TEXT, false)

    override suspend fun setLargeText(enabled: Boolean) {
        prefs.putBoolean(LARGE_TEXT, enabled)
    }

    override suspend fun isSuggestionsEnabled(): Boolean =
        prefs.getBoolean(SUGGESTIONS_ENABLED, true)

    override suspend fun setSuggestionsEnabled(enabled: Boolean) {
        prefs.putBoolean(SUGGESTIONS_ENABLED, enabled)
    }

    override suspend fun isFeatureHintsEnabled(): Boolean =
        prefs.getBoolean(FEATURE_HINTS_ENABLED, true)

    override suspend fun setFeatureHintsEnabled(enabled: Boolean) {
        prefs.putBoolean(FEATURE_HINTS_ENABLED, enabled)
    }

    override suspend fun getAdbPath(): String = prefs.get(ADB_PATH, "")

    override suspend fun setAdbPath(path: String) {
        prefs.put(ADB_PATH, path)
    }

    override suspend fun getActivityFavorites() = getSet(ACTIVITY_FAVORITES)
    override suspend fun setActivityFavorites(values: Set<String>) =
        setSet(ACTIVITY_FAVORITES, values)

    override suspend fun getActivityPackageFavorites() = getSet(ACTIVITY_PACKAGE_FAVORITES)
    override suspend fun setActivityPackageFavorites(values: Set<String>) =
        setSet(ACTIVITY_PACKAGE_FAVORITES, values)

    override suspend fun getInstalledAppFavorites() = getSet(INSTALLED_APP_FAVORITES)
    override suspend fun setInstalledAppFavorites(values: Set<String>) =
        setSet(INSTALLED_APP_FAVORITES, values)

    override suspend fun getPermissionFavorites() = getSet(PERMISSION_FAVORITES)
    override suspend fun setPermissionFavorites(values: Set<String>) =
        setSet(PERMISSION_FAVORITES, values)

    private fun String.toAppTheme(): AppTheme =
        runCatching { AppTheme.valueOf(this) }.getOrDefault(AppTheme.SYSTEM)

    private fun String.toExportFormat(): ExportFormat =
        runCatching { ExportFormat.valueOf(this) }.getOrDefault(ExportFormat.TXT)
}
