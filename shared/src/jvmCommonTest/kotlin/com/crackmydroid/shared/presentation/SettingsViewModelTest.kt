package com.crackmydroid.shared.presentation

import com.crackmydroid.shared.AppLogger
import com.crackmydroid.shared.domain.model.app.AppLanguage
import com.crackmydroid.shared.domain.model.app.AppTheme
import com.crackmydroid.shared.domain.model.ExportFormat
import com.crackmydroid.shared.domain.usecase.settings.GetExportFormatUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetFeatureHintsEnabledUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetHighContrastUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetIntroSeenUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetLanguageUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetLargeTextUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetReduceMotionUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetSuggestionsEnabledUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetThemeUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetVerboseLogUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetExportFormatUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetFeatureHintsEnabledUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetHighContrastUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetIntroSeenUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetLanguageUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetLargeTextUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetReduceMotionUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetSuggestionsEnabledUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetThemeUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetVerboseLogUseCase
import com.crackmydroid.shared.presentation.components.SearchHistoryStore
import com.crackmydroid.shared.ui.settings.SettingsViewModel
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        AppLogger.setVerbose(false)
        SearchHistoryStore.clearAll()
        SearchHistoryStore.setEnabled(true)
    }

    @AfterTest
    fun tearDown() {
        SearchHistoryStore.clearAll()
        SearchHistoryStore.setEnabled(true)
        Dispatchers.resetMain()
        AppLogger.setVerbose(false)
    }

    @Test
    fun load_success_populatesAllSettingsAndHidesIntroWhenSeen() = runTest(dispatcher) {
        val getLanguage = mockk<GetLanguageUseCase>()
        val getTheme = mockk<GetThemeUseCase>()
        val getVerbose = mockk<GetVerboseLogUseCase>()
        val getFormat = mockk<GetExportFormatUseCase>()
        val getHighContrast = mockk<GetHighContrastUseCase>()
        val getReduceMotion = mockk<GetReduceMotionUseCase>()
        val getLargeText = mockk<GetLargeTextUseCase>()
        val getSuggestions = mockk<GetSuggestionsEnabledUseCase>()
        val getHints = mockk<GetFeatureHintsEnabledUseCase>()
        val getIntroSeen = mockk<GetIntroSeenUseCase>()
        coEvery { getLanguage() } returns Result.success(AppLanguage.ITALIAN)
        coEvery { getTheme() } returns Result.success(AppTheme.DARK)
        coEvery { getVerbose() } returns Result.success(true)
        coEvery { getFormat() } returns Result.success(ExportFormat.JSON)
        coEvery { getHighContrast() } returns Result.success(true)
        coEvery { getReduceMotion() } returns Result.success(true)
        coEvery { getLargeText() } returns Result.success(true)
        coEvery { getSuggestions() } returns Result.success(false)
        coEvery { getHints() } returns Result.success(false)
        coEvery { getIntroSeen() } returns Result.success(true)

        val viewModel = viewModel(
            getLanguage = getLanguage,
            getTheme = getTheme,
            getVerbose = getVerbose,
            getFormat = getFormat,
            getHighContrast = getHighContrast,
            getReduceMotion = getReduceMotion,
            getLargeText = getLargeText,
            getSuggestions = getSuggestions,
            getHints = getHints,
            getIntroSeen = getIntroSeen
        )

        viewModel.load()
        advanceUntilIdle()

        assertEquals(false, viewModel.state.value.loading)
        assertEquals(AppLanguage.ITALIAN, viewModel.state.value.language)
        assertEquals(AppTheme.DARK, viewModel.state.value.theme)
        assertEquals(ExportFormat.JSON, viewModel.state.value.exportFormat)
        assertEquals(true, viewModel.state.value.verbose)
        assertEquals(true, AppLogger.isVerbose())
        assertEquals(true, viewModel.state.value.highContrast)
        assertEquals(true, viewModel.state.value.reduceMotion)
        assertEquals(true, viewModel.state.value.largeText)
        assertEquals(false, viewModel.state.value.suggestionsEnabled)
        assertEquals(false, viewModel.state.value.featureHintsEnabled)
        assertEquals(false, viewModel.state.value.showIntroOnLaunch)
        viewModel.onCleared()
    }

    @Test
    fun load_failure_storesMessageAndStopsLoading() = runTest(dispatcher) {
        val getTheme = mockk<GetThemeUseCase>()
        coEvery { getTheme() } returns Result.failure(IllegalStateException("settings unavailable"))
        val viewModel = viewModel(getTheme = getTheme)

        viewModel.load()
        advanceUntilIdle()

        assertEquals(false, viewModel.state.value.loading)
        assertEquals("settings unavailable", viewModel.state.value.error)
        viewModel.onCleared()
    }

    @Test
    fun exportFormat_newValue_persistsOnceAndUpdatesState() = runTest(dispatcher) {
        val setFormat = mockk<SetExportFormatUseCase>()
        coEvery { setFormat(ExportFormat.JSON) } returns Result.success(Unit)
        val viewModel = viewModel(setFormat = setFormat)

        viewModel.setExportFormat(ExportFormat.TXT)
        viewModel.setExportFormat(ExportFormat.JSON)
        advanceUntilIdle()

        assertEquals(ExportFormat.JSON, viewModel.state.value.exportFormat)
        assertEquals(null, viewModel.state.value.error)
        coVerify(exactly = 1) { setFormat(ExportFormat.JSON) }
        viewModel.onCleared()
    }

    @Test
    fun selectSystemLanguage_persistsAndUpdatesState() = runTest(dispatcher) {
        val setLanguage = mockk<SetLanguageUseCase>()
        coEvery { setLanguage(AppLanguage.SYSTEM) } returns Result.success(Unit)
        val viewModel = viewModel(setLanguage = setLanguage)

        viewModel.selectLanguage(AppLanguage.SYSTEM)
        advanceUntilIdle()

        assertEquals(AppLanguage.SYSTEM, viewModel.state.value.language)
        coVerify(exactly = 1) { setLanguage(AppLanguage.SYSTEM) }
        viewModel.onCleared()
    }

    @Test
    fun settingSaveFailure_keepsPreviousValueAndShowsError() = runTest(dispatcher) {
        val setVerbose = mockk<SetVerboseLogUseCase>()
        coEvery { setVerbose(true) } returns Result.failure(IllegalStateException("settings write failed"))
        val viewModel = viewModel(setVerbose = setVerbose)

        viewModel.toggleVerbose(true)
        advanceUntilIdle()

        assertEquals(false, viewModel.state.value.verbose)
        assertEquals(false, AppLogger.isVerbose())
        assertEquals("settings write failed", viewModel.state.value.error)
        viewModel.onCleared()
    }

    @Test
    fun verbosePolicyChangesOnlyAfterSuccessfulPersistence() = runTest(dispatcher) {
        val setVerbose = mockk<SetVerboseLogUseCase>()
        coEvery { setVerbose(true) } returns Result.success(Unit)
        coEvery { setVerbose(false) } returns Result.failure(IllegalStateException("write failed"))
        val viewModel = viewModel(setVerbose = setVerbose)

        viewModel.toggleVerbose(true)
        assertEquals(false, AppLogger.isVerbose())
        advanceUntilIdle()
        assertEquals(true, AppLogger.isVerbose())
        viewModel.toggleVerbose(false)
        advanceUntilIdle()
        assertEquals(true, AppLogger.isVerbose())
        assertEquals(true, viewModel.state.value.verbose)
        assertEquals("write failed", viewModel.state.value.error)
        viewModel.onCleared()
    }

    @Test
    fun failedVerboseReadKeepsDebugDisabledAndSurfacesTheError() = runTest(dispatcher) {
        val getVerbose = mockk<GetVerboseLogUseCase>()
        coEvery { getVerbose() } returns Result.failure(IllegalStateException("read failed"))
        val viewModel = viewModel(getVerbose = getVerbose)
        viewModel.load()
        advanceUntilIdle()
        assertEquals(false, AppLogger.isVerbose())
        assertEquals("read failed", viewModel.state.value.error)
        assertEquals(false, viewModel.state.value.loading)
        viewModel.onCleared()
    }

    private fun viewModel(
        getLanguage: GetLanguageUseCase = mockk<GetLanguageUseCase>().also { coEvery { it() } returns Result.success(AppLanguage.SYSTEM) },
        getTheme: GetThemeUseCase = mockk<GetThemeUseCase>().also { coEvery { it() } returns Result.success(AppTheme.SYSTEM) },
        getVerbose: GetVerboseLogUseCase = mockk<GetVerboseLogUseCase>().also { coEvery { it() } returns Result.success(false) },
        getFormat: GetExportFormatUseCase = mockk<GetExportFormatUseCase>().also { coEvery { it() } returns Result.success(ExportFormat.TXT) },
        getHighContrast: GetHighContrastUseCase = mockk<GetHighContrastUseCase>().also { coEvery { it() } returns Result.success(false) },
        getReduceMotion: GetReduceMotionUseCase = mockk<GetReduceMotionUseCase>().also { coEvery { it() } returns Result.success(false) },
        getLargeText: GetLargeTextUseCase = mockk<GetLargeTextUseCase>().also { coEvery { it() } returns Result.success(false) },
        getSuggestions: GetSuggestionsEnabledUseCase = mockk<GetSuggestionsEnabledUseCase>().also { coEvery { it() } returns Result.success(true) },
        getHints: GetFeatureHintsEnabledUseCase = mockk<GetFeatureHintsEnabledUseCase>().also { coEvery { it() } returns Result.success(true) },
        getIntroSeen: GetIntroSeenUseCase = mockk<GetIntroSeenUseCase>().also { coEvery { it() } returns Result.success(false) },
        setTheme: SetThemeUseCase = mockk(relaxed = true),
        setVerbose: SetVerboseLogUseCase = mockk(relaxed = true),
        setFormat: SetExportFormatUseCase = mockk(relaxed = true),
        setHighContrast: SetHighContrastUseCase = mockk(relaxed = true),
        setReduceMotion: SetReduceMotionUseCase = mockk(relaxed = true),
        setLargeText: SetLargeTextUseCase = mockk(relaxed = true),
        setSuggestions: SetSuggestionsEnabledUseCase = mockk(relaxed = true),
        setHints: SetFeatureHintsEnabledUseCase = mockk(relaxed = true),
        setIntro: SetIntroSeenUseCase = mockk(relaxed = true),
        setLanguage: SetLanguageUseCase = mockk(relaxed = true)
    ) = SettingsViewModel(
        getTheme, setTheme, getVerbose, setVerbose, getFormat, setFormat,
        getHighContrast, setHighContrast, getReduceMotion, setReduceMotion,
        getLargeText, setLargeText, getSuggestions, setSuggestions,
        getHints, setHints, getIntroSeen, setIntro, getLanguage, setLanguage
    )
}
