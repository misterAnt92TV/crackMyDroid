package com.crackmydroid.shared.domain.repository

import com.crackmydroid.shared.domain.model.ExportFormat
import com.crackmydroid.shared.domain.model.app.AppLanguage
import com.crackmydroid.shared.domain.model.app.AppTheme

interface SettingsRepository {
    suspend fun getLanguage(): AppLanguage
    suspend fun setLanguage(language: AppLanguage)
    suspend fun getTheme(): AppTheme
    suspend fun setTheme(theme: AppTheme)
    suspend fun isVerboseLog(): Boolean
    suspend fun setVerboseLog(enabled: Boolean)
    suspend fun isIntroSeen(): Boolean
    suspend fun setIntroSeen(seen: Boolean)
    suspend fun getExportFormat(): ExportFormat
    suspend fun setExportFormat(format: ExportFormat)
    suspend fun isHighContrast(): Boolean
    suspend fun setHighContrast(enabled: Boolean)
    suspend fun isReduceMotion(): Boolean
    suspend fun setReduceMotion(enabled: Boolean)
    suspend fun isLargeText(): Boolean
    suspend fun setLargeText(enabled: Boolean)
    suspend fun isSuggestionsEnabled(): Boolean
    suspend fun setSuggestionsEnabled(enabled: Boolean)
    suspend fun isFeatureHintsEnabled(): Boolean
    suspend fun setFeatureHintsEnabled(enabled: Boolean)
    suspend fun getAdbPath(): String
    suspend fun setAdbPath(path: String)
    suspend fun getActivityFavorites(): Set<String>
    suspend fun setActivityFavorites(values: Set<String>)
    suspend fun getActivityPackageFavorites(): Set<String>
    suspend fun setActivityPackageFavorites(values: Set<String>)
    suspend fun getInstalledAppFavorites(): Set<String>
    suspend fun setInstalledAppFavorites(values: Set<String>)
    suspend fun getPermissionFavorites(): Set<String>
    suspend fun setPermissionFavorites(values: Set<String>)
}