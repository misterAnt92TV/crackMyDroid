package com.crackmydroid.shared.domain.usecase

import com.crackmydroid.shared.domain.model.ExportFormat
import com.crackmydroid.shared.domain.model.app.AppLanguage
import com.crackmydroid.shared.domain.model.app.AppTheme
import com.crackmydroid.shared.domain.repository.SettingsRepository
import com.crackmydroid.shared.domain.usecase.settings.GetActivityFavoritesUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetActivityPackageFavoritesUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetExportFormatUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetFeatureHintsEnabledUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetHighContrastUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetInstalledAppFavoritesUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetIntroSeenUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetLanguageUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetLargeTextUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetPermissionFavoritesUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetReduceMotionUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetSuggestionsEnabledUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetThemeUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetVerboseLogUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetActivityFavoritesUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetActivityPackageFavoritesUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetExportFormatUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetFeatureHintsEnabledUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetHighContrastUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetInstalledAppFavoritesUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetIntroSeenUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetLanguageUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetLargeTextUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetPermissionFavoritesUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetReduceMotionUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetSuggestionsEnabledUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetThemeUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetVerboseLogUseCase
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

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

        assertEquals(AppLanguage.ITALIAN, GetLanguageUseCase(repository)().getOrThrow())
        assertEquals(AppTheme.DARK, GetThemeUseCase(repository)().getOrThrow())
        assertEquals(true, GetVerboseLogUseCase(repository)().getOrThrow())
        assertEquals(true, GetIntroSeenUseCase(repository)().getOrThrow())
        assertEquals(ExportFormat.JSON, GetExportFormatUseCase(repository)().getOrThrow())
        assertEquals(true, GetHighContrastUseCase(repository)().getOrThrow())
        assertEquals(true, GetReduceMotionUseCase(repository)().getOrThrow())
        assertEquals(true, GetLargeTextUseCase(repository)().getOrThrow())
        assertEquals(false, GetSuggestionsEnabledUseCase(repository)().getOrThrow())
        assertEquals(false, GetFeatureHintsEnabledUseCase(repository)().getOrThrow())
        assertEquals(setOf("activity"), GetActivityFavoritesUseCase(repository)().getOrThrow())
        assertEquals(setOf("package"), GetActivityPackageFavoritesUseCase(repository)().getOrThrow())
        assertEquals(setOf("installed"), GetInstalledAppFavoritesUseCase(repository)().getOrThrow())
        assertEquals(setOf("permission"), GetPermissionFavoritesUseCase(repository)().getOrThrow())

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
        SetLanguageUseCase(repository)(AppLanguage.SYSTEM)
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
        coVerify { repository.setLanguage(AppLanguage.SYSTEM) }
        coVerify { repository.setTheme(AppTheme.LIGHT) }
        coVerify { repository.setVerboseLog(true) }
        coVerify { repository.setIntroSeen(false) }
        coVerify { repository.setExportFormat(ExportFormat.JSON) }
        coVerify { repository.setHighContrast(true) }
        coVerify { repository.setReduceMotion(true) }
        coVerify { repository.setLargeText(true) }
        coVerify { repository.setSuggestionsEnabled(false) }
        coVerify { repository.setFeatureHintsEnabled(false) }
        coVerify { repository.setActivityFavorites(setOf("activity")) }
        coVerify { repository.setActivityPackageFavorites(setOf("package")) }
        coVerify { repository.setInstalledAppFavorites(setOf("installed")) }
        coVerify { repository.setPermissionFavorites(setOf("permission")) }
    }

    @Test
    fun `boolean settings preserve both enabled and disabled values`() = runTest {
        val repository = mockk<SettingsRepository>()
        for (value in listOf(false, true)) {
            coEvery { repository.isVerboseLog() } returns value
            coEvery { repository.isIntroSeen() } returns value
            coEvery { repository.isHighContrast() } returns value
            coEvery { repository.isReduceMotion() } returns value
            coEvery { repository.isLargeText() } returns value
            coEvery { repository.isSuggestionsEnabled() } returns value
            coEvery { repository.isFeatureHintsEnabled() } returns value
            coJustRun { repository.setVerboseLog(value) }
            coJustRun { repository.setIntroSeen(value) }
            coJustRun { repository.setHighContrast(value) }
            coJustRun { repository.setReduceMotion(value) }
            coJustRun { repository.setLargeText(value) }
            coJustRun { repository.setSuggestionsEnabled(value) }
            coJustRun { repository.setFeatureHintsEnabled(value) }

            assertEquals(value, GetVerboseLogUseCase(repository)().getOrThrow())
            assertEquals(value, GetIntroSeenUseCase(repository)().getOrThrow())
            assertEquals(value, GetHighContrastUseCase(repository)().getOrThrow())
            assertEquals(value, GetReduceMotionUseCase(repository)().getOrThrow())
            assertEquals(value, GetLargeTextUseCase(repository)().getOrThrow())
            assertEquals(value, GetSuggestionsEnabledUseCase(repository)().getOrThrow())
            assertEquals(value, GetFeatureHintsEnabledUseCase(repository)().getOrThrow())
            SetVerboseLogUseCase(repository)(value)
            SetIntroSeenUseCase(repository)(value)
            SetHighContrastUseCase(repository)(value)
            SetReduceMotionUseCase(repository)(value)
            SetLargeTextUseCase(repository)(value)
            SetSuggestionsEnabledUseCase(repository)(value)
            SetFeatureHintsEnabledUseCase(repository)(value)

            coVerify(exactly = 1) { repository.setVerboseLog(value) }
            coVerify(exactly = 1) { repository.setIntroSeen(value) }
            coVerify(exactly = 1) { repository.setHighContrast(value) }
            coVerify(exactly = 1) { repository.setReduceMotion(value) }
            coVerify(exactly = 1) { repository.setLargeText(value) }
            coVerify(exactly = 1) { repository.setSuggestionsEnabled(value) }
            coVerify(exactly = 1) { repository.setFeatureHintsEnabled(value) }
        }
    }

    @Test
    fun `enum settings preserve every supported value`() = runTest {
        val repository = mockk<SettingsRepository>()
        for (value in AppLanguage.entries) {
            coEvery { repository.getLanguage() } returns value
            coJustRun { repository.setLanguage(value) }
            assertEquals(value, GetLanguageUseCase(repository)().getOrThrow())
            SetLanguageUseCase(repository)(value)
            coVerify(exactly = 1) { repository.setLanguage(value) }
        }
        for (value in AppTheme.entries) {
            coEvery { repository.getTheme() } returns value
            coJustRun { repository.setTheme(value) }
            assertEquals(value, GetThemeUseCase(repository)().getOrThrow())
            SetThemeUseCase(repository)(value)
            coVerify(exactly = 1) { repository.setTheme(value) }
        }
        for (value in ExportFormat.entries) {
            coEvery { repository.getExportFormat() } returns value
            coJustRun { repository.setExportFormat(value) }
            assertEquals(value, GetExportFormatUseCase(repository)().getOrThrow())
            SetExportFormatUseCase(repository)(value)
            coVerify(exactly = 1) { repository.setExportFormat(value) }
        }
    }

    @Test
    fun `favorite settings preserve empty and populated sets`() = runTest {
        val repository = mockk<SettingsRepository>()
        for (value in listOf(emptySet(), setOf("first", "second"))) {
            coEvery { repository.getActivityFavorites() } returns value
            coEvery { repository.getActivityPackageFavorites() } returns value
            coEvery { repository.getInstalledAppFavorites() } returns value
            coEvery { repository.getPermissionFavorites() } returns value
            coJustRun { repository.setActivityFavorites(value) }
            coJustRun { repository.setActivityPackageFavorites(value) }
            coJustRun { repository.setInstalledAppFavorites(value) }
            coJustRun { repository.setPermissionFavorites(value) }

            assertEquals(value, GetActivityFavoritesUseCase(repository)().getOrThrow())
            assertEquals(value, GetActivityPackageFavoritesUseCase(repository)().getOrThrow())
            assertEquals(value, GetInstalledAppFavoritesUseCase(repository)().getOrThrow())
            assertEquals(value, GetPermissionFavoritesUseCase(repository)().getOrThrow())
            SetActivityFavoritesUseCase(repository)(value)
            SetActivityPackageFavoritesUseCase(repository)(value)
            SetInstalledAppFavoritesUseCase(repository)(value)
            SetPermissionFavoritesUseCase(repository)(value)

            coVerify(exactly = 1) { repository.setActivityFavorites(value) }
            coVerify(exactly = 1) { repository.setActivityPackageFavorites(value) }
            coVerify(exactly = 1) { repository.setInstalledAppFavorites(value) }
            coVerify(exactly = 1) { repository.setPermissionFavorites(value) }
        }
    }

    @Test
    fun `all getters propagate repository read failures`() = runTest {
        val repository = mockk<SettingsRepository>()
        val failure = IllegalStateException("Read failed")
        coEvery { repository.getLanguage() } throws failure
        coEvery { repository.getTheme() } throws failure
        coEvery { repository.getExportFormat() } throws failure
        coEvery { repository.isVerboseLog() } throws failure
        coEvery { repository.isIntroSeen() } throws failure
        coEvery { repository.isHighContrast() } throws failure
        coEvery { repository.isReduceMotion() } throws failure
        coEvery { repository.isLargeText() } throws failure
        coEvery { repository.isSuggestionsEnabled() } throws failure
        coEvery { repository.isFeatureHintsEnabled() } throws failure
        coEvery { repository.getActivityFavorites() } throws failure
        coEvery { repository.getActivityPackageFavorites() } throws failure
        coEvery { repository.getInstalledAppFavorites() } throws failure
        coEvery { repository.getPermissionFavorites() } throws failure
        assertSame(failure, GetLanguageUseCase(repository)().exceptionOrNull())
        assertSame(failure, GetThemeUseCase(repository)().exceptionOrNull())
        assertSame(failure, GetExportFormatUseCase(repository)().exceptionOrNull())
        assertSame(failure, GetVerboseLogUseCase(repository)().exceptionOrNull())
        assertSame(failure, GetIntroSeenUseCase(repository)().exceptionOrNull())
        assertSame(failure, GetHighContrastUseCase(repository)().exceptionOrNull())
        assertSame(failure, GetReduceMotionUseCase(repository)().exceptionOrNull())
        assertSame(failure, GetLargeTextUseCase(repository)().exceptionOrNull())
        assertSame(failure, GetSuggestionsEnabledUseCase(repository)().exceptionOrNull())
        assertSame(failure, GetFeatureHintsEnabledUseCase(repository)().exceptionOrNull())
        assertSame(failure, GetActivityFavoritesUseCase(repository)().exceptionOrNull())
        assertSame(failure, GetActivityPackageFavoritesUseCase(repository)().exceptionOrNull())
        assertSame(failure, GetInstalledAppFavoritesUseCase(repository)().exceptionOrNull())
        assertSame(failure, GetPermissionFavoritesUseCase(repository)().exceptionOrNull())
    }

    @Test
    fun `all setters propagate repository write failures`() = runTest {
        val repository = mockk<SettingsRepository>()
        val failure = IllegalStateException("Write failed")
        coEvery { repository.setLanguage(any()) } throws failure
        coEvery { repository.setTheme(any()) } throws failure
        coEvery { repository.setExportFormat(any()) } throws failure
        coEvery { repository.setVerboseLog(any()) } throws failure
        coEvery { repository.setIntroSeen(any()) } throws failure
        coEvery { repository.setHighContrast(any()) } throws failure
        coEvery { repository.setReduceMotion(any()) } throws failure
        coEvery { repository.setLargeText(any()) } throws failure
        coEvery { repository.setSuggestionsEnabled(any()) } throws failure
        coEvery { repository.setFeatureHintsEnabled(any()) } throws failure
        coEvery { repository.setActivityFavorites(any()) } throws failure
        coEvery { repository.setActivityPackageFavorites(any()) } throws failure
        coEvery { repository.setInstalledAppFavorites(any()) } throws failure
        coEvery { repository.setPermissionFavorites(any()) } throws failure
        assertSame(failure, SetLanguageUseCase(repository)(AppLanguage.SYSTEM).exceptionOrNull())
        assertSame(failure, SetThemeUseCase(repository)(AppTheme.SYSTEM).exceptionOrNull())
        assertSame(failure, SetExportFormatUseCase(repository)(ExportFormat.TXT).exceptionOrNull())
        assertSame(failure, SetVerboseLogUseCase(repository)(false).exceptionOrNull())
        assertSame(failure, SetIntroSeenUseCase(repository)(false).exceptionOrNull())
        assertSame(failure, SetHighContrastUseCase(repository)(false).exceptionOrNull())
        assertSame(failure, SetReduceMotionUseCase(repository)(false).exceptionOrNull())
        assertSame(failure, SetLargeTextUseCase(repository)(false).exceptionOrNull())
        assertSame(failure, SetSuggestionsEnabledUseCase(repository)(false).exceptionOrNull())
        assertSame(failure, SetFeatureHintsEnabledUseCase(repository)(false).exceptionOrNull())
        assertSame(failure, SetActivityFavoritesUseCase(repository)(emptySet()).exceptionOrNull())
        assertSame(failure, SetActivityPackageFavoritesUseCase(repository)(emptySet()).exceptionOrNull())
        assertSame(failure, SetInstalledAppFavoritesUseCase(repository)(emptySet()).exceptionOrNull())
        assertSame(failure, SetPermissionFavoritesUseCase(repository)(emptySet()).exceptionOrNull())
    }
}
