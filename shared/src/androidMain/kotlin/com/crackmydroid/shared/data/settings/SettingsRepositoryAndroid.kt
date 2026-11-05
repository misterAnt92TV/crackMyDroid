package com.crackmydroid.shared.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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
import kotlinx.coroutines.flow.first

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

class SettingsRepositoryAndroid(private val context: Context) : SettingsRepository {

    private val themeKey = stringPreferencesKey(THEME)
    private val languageKey = stringPreferencesKey(LANGUAGE)
    private val verboseKey = booleanPreferencesKey(VERBOSE_LOG)
    private val introKey = booleanPreferencesKey(INTRO_SEEN)
    private val exportFormatKey = stringPreferencesKey(EXPORT_FORMAT)
    private val highContrastKey = booleanPreferencesKey(HIGH_CONTRAST)
    private val reduceMotionKey = booleanPreferencesKey(REDUCE_MOTION)
    private val largeTextKey = booleanPreferencesKey(LARGE_TEXT)
    private val suggestionsKey = booleanPreferencesKey(SUGGESTIONS_ENABLED)
    private val featureHintsKey = booleanPreferencesKey(FEATURE_HINTS_ENABLED)
    private val adbPathKey = stringPreferencesKey(ADB_PATH)
    private val activityFavoritesKey = stringSetPreferencesKey(ACTIVITY_FAVORITES)
    private val activityPackageFavoritesKey = stringSetPreferencesKey(ACTIVITY_PACKAGE_FAVORITES)
    private val installedAppFavoritesKey = stringSetPreferencesKey(INSTALLED_APP_FAVORITES)
    private val permissionFavoritesKey = stringSetPreferencesKey(PERMISSION_FAVORITES)

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
        return prefs[exportFormatKey]?.let { runCatching { ExportFormat.valueOf(it) }.getOrNull() }
            ?: ExportFormat.TXT
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

    override suspend fun getActivityFavorites() =
        context.settingsDataStore.data.first()[activityFavoritesKey].orEmpty()

    override suspend fun setActivityFavorites(values: Set<String>) {
        context.settingsDataStore.edit { it[activityFavoritesKey] = values }
    }

    override suspend fun getActivityPackageFavorites() =
        context.settingsDataStore.data.first()[activityPackageFavoritesKey].orEmpty()

    override suspend fun setActivityPackageFavorites(values: Set<String>) {
        context.settingsDataStore.edit { it[activityPackageFavoritesKey] = values }
    }

    override suspend fun getInstalledAppFavorites() =
        context.settingsDataStore.data.first()[installedAppFavoritesKey].orEmpty()

    override suspend fun setInstalledAppFavorites(values: Set<String>) {
        context.settingsDataStore.edit { it[installedAppFavoritesKey] = values }
    }

    override suspend fun getPermissionFavorites() =
        context.settingsDataStore.data.first()[permissionFavoritesKey].orEmpty()

    override suspend fun setPermissionFavorites(values: Set<String>) {
        context.settingsDataStore.edit { it[permissionFavoritesKey] = values }
    }
}
