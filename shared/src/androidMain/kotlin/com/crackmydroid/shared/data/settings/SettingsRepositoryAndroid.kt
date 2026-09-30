package com.crackmydroid.shared.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.crackmydroid.shared.domain.model.AppTheme
import com.crackmydroid.shared.domain.model.AppLanguage
import com.crackmydroid.shared.domain.model.ExportFormat
import com.crackmydroid.shared.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

class SettingsRepositoryAndroid(private val context: Context) : SettingsRepository {

    private val themeKey = stringPreferencesKey("theme")
    private val languageKey = stringPreferencesKey("language")
    private val verboseKey = booleanPreferencesKey("verbose_log")
    private val introKey = booleanPreferencesKey("intro_seen")
    private val exportFormatKey = stringPreferencesKey("export_format")
    private val highContrastKey = booleanPreferencesKey("high_contrast")
    private val reduceMotionKey = booleanPreferencesKey("reduce_motion")
    private val largeTextKey = booleanPreferencesKey("large_text")
    private val suggestionsKey = booleanPreferencesKey("suggestions_enabled")
    private val featureHintsKey = booleanPreferencesKey("feature_hints_enabled")
    private val adbPathKey = stringPreferencesKey("adb_path")
    private val activityFavoritesKey = stringSetPreferencesKey("activity_favorites")
    private val activityPackageFavoritesKey = stringSetPreferencesKey("activity_package_favorites")
    private val installedAppFavoritesKey = stringSetPreferencesKey("installed_app_favorites")
    private val permissionFavoritesKey = stringSetPreferencesKey("permission_favorites")

    override suspend fun getTheme(): AppTheme {
        val prefs = context.settingsDataStore.data.first()
        val stored = prefs[themeKey]
        return stored?.let { runCatching { AppTheme.valueOf(it) }.getOrNull() } ?: AppTheme.SYSTEM
    }

    override suspend fun getLanguage(): AppLanguage =
        AppLanguage.fromCode(context.settingsDataStore.data.first()[languageKey])

    override suspend fun setLanguage(language: AppLanguage) {
        context.settingsDataStore.edit { it[languageKey] = language.code }
    }

    override suspend fun setTheme(theme: AppTheme) {
        context.settingsDataStore.edit { prefs ->
            prefs[themeKey] = theme.name
        }
    }

    override suspend fun isVerboseLog(): Boolean {
        val prefs = context.settingsDataStore.data.first()
        return prefs[verboseKey] ?: false
    }

    override suspend fun setVerboseLog(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[verboseKey] = enabled
        }
    }

    override suspend fun isIntroSeen(): Boolean {
        val prefs = context.settingsDataStore.data.first()
        return prefs[introKey] ?: false
    }

    override suspend fun setIntroSeen(seen: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[introKey] = seen
        }
    }

    override suspend fun getExportFormat(): ExportFormat {
        val prefs = context.settingsDataStore.data.first()
        return prefs[exportFormatKey]?.let { runCatching { ExportFormat.valueOf(it) }.getOrNull() } ?: ExportFormat.TXT
    }

    override suspend fun setExportFormat(format: ExportFormat) {
        context.settingsDataStore.edit { prefs ->
            prefs[exportFormatKey] = format.name
        }
    }

    override suspend fun isHighContrast(): Boolean {
        val prefs = context.settingsDataStore.data.first()
        return prefs[highContrastKey] ?: false
    }

    override suspend fun setHighContrast(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[highContrastKey] = enabled
        }
    }

    override suspend fun isReduceMotion(): Boolean {
        val prefs = context.settingsDataStore.data.first()
        return prefs[reduceMotionKey] ?: false
    }

    override suspend fun setReduceMotion(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[reduceMotionKey] = enabled
        }
    }

    override suspend fun isLargeText(): Boolean {
        val prefs = context.settingsDataStore.data.first()
        return prefs[largeTextKey] ?: false
    }

    override suspend fun setLargeText(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[largeTextKey] = enabled
        }
    }

    override suspend fun isSuggestionsEnabled(): Boolean {
        val prefs = context.settingsDataStore.data.first()
        return prefs[suggestionsKey] ?: true
    }

    override suspend fun setSuggestionsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[suggestionsKey] = enabled
        }
    }

    override suspend fun isFeatureHintsEnabled(): Boolean {
        val prefs = context.settingsDataStore.data.first()
        return prefs[featureHintsKey] ?: true
    }

    override suspend fun setFeatureHintsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[featureHintsKey] = enabled
        }
    }

    override suspend fun getAdbPath(): String {
        val prefs = context.settingsDataStore.data.first()
        return prefs[adbPathKey] ?: ""
    }

    override suspend fun setAdbPath(path: String) {
        context.settingsDataStore.edit { prefs ->
            prefs[adbPathKey] = path
        }
    }

    override suspend fun getActivityFavorites() = context.settingsDataStore.data.first()[activityFavoritesKey].orEmpty()
    override suspend fun setActivityFavorites(values: Set<String>) { context.settingsDataStore.edit { it[activityFavoritesKey] = values } }
    override suspend fun getActivityPackageFavorites() = context.settingsDataStore.data.first()[activityPackageFavoritesKey].orEmpty()
    override suspend fun setActivityPackageFavorites(values: Set<String>) { context.settingsDataStore.edit { it[activityPackageFavoritesKey] = values } }
    override suspend fun getInstalledAppFavorites() = context.settingsDataStore.data.first()[installedAppFavoritesKey].orEmpty()
    override suspend fun setInstalledAppFavorites(values: Set<String>) { context.settingsDataStore.edit { it[installedAppFavoritesKey] = values } }
    override suspend fun getPermissionFavorites() = context.settingsDataStore.data.first()[permissionFavoritesKey].orEmpty()
    override suspend fun setPermissionFavorites(values: Set<String>) { context.settingsDataStore.edit { it[permissionFavoritesKey] = values } }
}
