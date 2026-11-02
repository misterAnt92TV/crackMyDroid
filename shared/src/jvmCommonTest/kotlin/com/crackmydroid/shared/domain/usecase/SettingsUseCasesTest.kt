package com.crackmydroid.shared.domain.usecase

import com.crackmydroid.shared.domain.model.AppLanguage
import com.crackmydroid.shared.domain.model.AppTheme
import com.crackmydroid.shared.domain.model.ExportFormat
import com.crackmydroid.shared.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsUseCasesTest {
    @Test
    fun getters_delegateToSettingsRepository() = runTest {
        val repository = mockk<SettingsRepository>()
        coEvery { repository.getLanguage() } returns AppLanguage.ITALIAN
        coEvery { repository.getTheme() } returns AppTheme.DARK
        coEvery { repository.isVerboseLog() } returns true
        coEvery { repository.isIntroSeen() } returns true
        coEvery { repository.getExportFormat() } returns ExportFormat.JSON
        coEvery { repository.isHighContrast() } returns true
        coEvery { repository.isReduceMotion() } returns true
        coEvery { repository.isLargeText() } returns true
        coEvery { repository.isSuggestionsEnabled() } returns false
        coEvery { repository.isFeatureHintsEnabled() } returns false
        coEvery { repository.getActivityFavorites() } returns setOf("activity")
        coEvery { repository.getActivityPackageFavorites() } returns setOf("package")
        coEvery { repository.getInstalledAppFavorites() } returns setOf("installed")
        coEvery { repository.getPermissionFavorites() } returns setOf("permission")

        assertEquals(AppLanguage.ITALIAN, GetLanguageUseCase(repository)())
        assertEquals(AppTheme.DARK, GetThemeUseCase(repository)())
        assertEquals(true, GetVerboseLogUseCase(repository)())
        assertEquals(true, GetIntroSeenUseCase(repository)())
        assertEquals(ExportFormat.JSON, GetExportFormatUseCase(repository)())
        assertEquals(true, GetHighContrastUseCase(repository)())
        assertEquals(true, GetReduceMotionUseCase(repository)())
        assertEquals(true, GetLargeTextUseCase(repository)())
        assertEquals(false, GetSuggestionsEnabledUseCase(repository)())
        assertEquals(false, GetFeatureHintsEnabledUseCase(repository)())
        assertEquals(setOf("activity"), GetActivityFavoritesUseCase(repository)())
        assertEquals(setOf("package"), GetActivityPackageFavoritesUseCase(repository)())
        assertEquals(setOf("installed"), GetInstalledAppFavoritesUseCase(repository)())
        assertEquals(setOf("permission"), GetPermissionFavoritesUseCase(repository)())

        coVerify(exactly = 1) { repository.getLanguage() }
        coVerify(exactly = 1) { repository.getActivityPackageFavorites() }
    }

    @Test
    fun setters_delegateAllValuesToSettingsRepository() = runTest {
        val repository = mockk<SettingsRepository>()
        coJustRun { repository.setLanguage(any()) }
        coJustRun { repository.setTheme(any()) }
        coJustRun { repository.setVerboseLog(any()) }
        coJustRun { repository.setIntroSeen(any()) }
        coJustRun { repository.setExportFormat(any()) }
        coJustRun { repository.setHighContrast(any()) }
        coJustRun { repository.setReduceMotion(any()) }
        coJustRun { repository.setLargeText(any()) }
        coJustRun { repository.setSuggestionsEnabled(any()) }
        coJustRun { repository.setFeatureHintsEnabled(any()) }
        coJustRun { repository.setActivityFavorites(any()) }
        coJustRun { repository.setActivityPackageFavorites(any()) }
        coJustRun { repository.setInstalledAppFavorites(any()) }
        coJustRun { repository.setPermissionFavorites(any()) }

        SetLanguageUseCase(repository)(AppLanguage.GERMAN)
        SetThemeUseCase(repository)(AppTheme.LIGHT)
        SetVerboseLogUseCase(repository)(true)
        SetIntroSeenUseCase(repository)(false)
        SetExportFormatUseCase(repository)(ExportFormat.JSON)
        SetHighContrastUseCase(repository)(true)
        SetReduceMotionUseCase(repository)(true)
        SetLargeTextUseCase(repository)(true)
        SetSuggestionsEnabledUseCase(repository)(false)
        SetFeatureHintsEnabledUseCase(repository)(false)
        SetActivityFavoritesUseCase(repository)(setOf("activity"))
        SetActivityPackageFavoritesUseCase(repository)(setOf("package"))
        SetInstalledAppFavoritesUseCase(repository)(setOf("installed"))
        SetPermissionFavoritesUseCase(repository)(setOf("permission"))

        coVerify { repository.setLanguage(AppLanguage.GERMAN) }
        coVerify { repository.setTheme(AppTheme.LIGHT) }
        coVerify { repository.setExportFormat(ExportFormat.JSON) }
        coVerify { repository.setPermissionFavorites(setOf("permission")) }
    }
}
