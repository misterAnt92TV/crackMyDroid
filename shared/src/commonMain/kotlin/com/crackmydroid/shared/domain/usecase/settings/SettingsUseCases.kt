package com.crackmydroid.shared.domain.usecase

import com.crackmydroid.shared.domain.model.AppTheme
import com.crackmydroid.shared.domain.model.AppLanguage
import com.crackmydroid.shared.domain.model.ExportFormat
import com.crackmydroid.shared.domain.repository.SettingsRepository

class GetThemeUseCase(private val repo: SettingsRepository) {
    suspend operator fun invoke(): AppTheme = repo.getTheme()
}

class GetLanguageUseCase(private val repo: SettingsRepository) { suspend operator fun invoke() = repo.getLanguage() }
class SetLanguageUseCase(private val repo: SettingsRepository) { suspend operator fun invoke(language: AppLanguage) = repo.setLanguage(language) }

class SetThemeUseCase(private val repo: SettingsRepository) {
    suspend operator fun invoke(theme: AppTheme) = repo.setTheme(theme)
}

class GetVerboseLogUseCase(private val repo: SettingsRepository) {
    suspend operator fun invoke(): Boolean = repo.isVerboseLog()
}

class SetVerboseLogUseCase(private val repo: SettingsRepository) {
    suspend operator fun invoke(enabled: Boolean) = repo.setVerboseLog(enabled)
}

class GetExportFormatUseCase(private val repo: SettingsRepository) {
    suspend operator fun invoke(): ExportFormat = repo.getExportFormat()
}

class SetExportFormatUseCase(private val repo: SettingsRepository) {
    suspend operator fun invoke(format: ExportFormat) = repo.setExportFormat(format)
}

class GetHighContrastUseCase(private val repo: SettingsRepository) {
    suspend operator fun invoke(): Boolean = repo.isHighContrast()
}

class SetHighContrastUseCase(private val repo: SettingsRepository) {
    suspend operator fun invoke(enabled: Boolean) = repo.setHighContrast(enabled)
}

class GetReduceMotionUseCase(private val repo: SettingsRepository) {
    suspend operator fun invoke(): Boolean = repo.isReduceMotion()
}

class SetReduceMotionUseCase(private val repo: SettingsRepository) {
    suspend operator fun invoke(enabled: Boolean) = repo.setReduceMotion(enabled)
}

class GetLargeTextUseCase(private val repo: SettingsRepository) {
    suspend operator fun invoke(): Boolean = repo.isLargeText()
}

class SetLargeTextUseCase(private val repo: SettingsRepository) {
    suspend operator fun invoke(enabled: Boolean) = repo.setLargeText(enabled)
}

class GetSuggestionsEnabledUseCase(private val repo: SettingsRepository) {
    suspend operator fun invoke(): Boolean = repo.isSuggestionsEnabled()
}

class SetSuggestionsEnabledUseCase(private val repo: SettingsRepository) {
    suspend operator fun invoke(enabled: Boolean) = repo.setSuggestionsEnabled(enabled)
}

class GetFeatureHintsEnabledUseCase(private val repo: SettingsRepository) {
    suspend operator fun invoke(): Boolean = repo.isFeatureHintsEnabled()
}

class SetFeatureHintsEnabledUseCase(private val repo: SettingsRepository) {
    suspend operator fun invoke(enabled: Boolean) = repo.setFeatureHintsEnabled(enabled)
}

class GetIntroSeenUseCase(private val repo: SettingsRepository) {
    suspend operator fun invoke(): Boolean = repo.isIntroSeen()
}

class SetIntroSeenUseCase(private val repo: SettingsRepository) {
    suspend operator fun invoke(seen: Boolean) = repo.setIntroSeen(seen)
}

class GetActivityFavoritesUseCase(private val repo: SettingsRepository) {
    suspend operator fun invoke(): Set<String> = repo.getActivityFavorites()
}
class SetActivityFavoritesUseCase(private val repo: SettingsRepository) {
    suspend operator fun invoke(values: Set<String>) = repo.setActivityFavorites(values)
}
class GetActivityPackageFavoritesUseCase(private val repo: SettingsRepository) {
    suspend operator fun invoke(): Set<String> = repo.getActivityPackageFavorites()
}
class SetActivityPackageFavoritesUseCase(private val repo: SettingsRepository) {
    suspend operator fun invoke(values: Set<String>) = repo.setActivityPackageFavorites(values)
}
class GetInstalledAppFavoritesUseCase(private val repo: SettingsRepository) {
    suspend operator fun invoke(): Set<String> = repo.getInstalledAppFavorites()
}
class SetInstalledAppFavoritesUseCase(private val repo: SettingsRepository) {
    suspend operator fun invoke(values: Set<String>) = repo.setInstalledAppFavorites(values)
}
class GetPermissionFavoritesUseCase(private val repo: SettingsRepository) {
    suspend operator fun invoke(): Set<String> = repo.getPermissionFavorites()
}
class SetPermissionFavoritesUseCase(private val repo: SettingsRepository) {
    suspend operator fun invoke(values: Set<String>) = repo.setPermissionFavorites(values)
}
