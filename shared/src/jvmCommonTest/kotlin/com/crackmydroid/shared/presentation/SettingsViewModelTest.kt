package com.crackmydroid.shared.presentation

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
        SearchHistoryStore.clearAll()
        SearchHistoryStore.setEnabled(true)
    }

    @AfterTest
    fun tearDown() {
        SearchHistoryStore.clearAll()
        SearchHistoryStore.setEnabled(true)
        Dispatchers.resetMain()
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
        coEvery { getLanguage() } returns AppLanguage.ITALIAN
        coEvery { getTheme() } returns AppTheme.DARK
        coEvery { getVerbose() } returns true
        coEvery { getFormat() } returns ExportFormat.JSON
        coEvery { getHighContrast() } returns true
        coEvery { getReduceMotion() } returns true
        coEvery { getLargeText() } returns true
        coEvery { getSuggestions() } returns false
        coEvery { getHints() } returns false
        coEvery { getIntroSeen() } returns true

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
        coEvery { getTheme() } throws IllegalStateException("settings unavailable")
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
        coJustRun { setFormat(ExportFormat.JSON) }
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
        coJustRun { setLanguage(AppLanguage.SYSTEM) }
        val viewModel = viewModel(setLanguage = setLanguage)

        viewModel.selectLanguage(AppLanguage.SYSTEM)
        advanceUntilIdle()

        assertEquals(AppLanguage.SYSTEM, viewModel.state.value.language)
        coVerify(exactly = 1) { setLanguage(AppLanguage.SYSTEM) }
        viewModel.onCleared()
    }

    private fun viewModel(
        getLanguage: GetLanguageUseCase = mockk(relaxed = true),
        getTheme: GetThemeUseCase = mockk(relaxed = true),
        getVerbose: GetVerboseLogUseCase = mockk(relaxed = true),
        getFormat: GetExportFormatUseCase = mockk(relaxed = true),
        getHighContrast: GetHighContrastUseCase = mockk(relaxed = true),
        getReduceMotion: GetReduceMotionUseCase = mockk(relaxed = true),
        getLargeText: GetLargeTextUseCase = mockk(relaxed = true),
        getSuggestions: GetSuggestionsEnabledUseCase = mockk(relaxed = true),
        getHints: GetFeatureHintsEnabledUseCase = mockk(relaxed = true),
        getIntroSeen: GetIntroSeenUseCase = mockk(relaxed = true),
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
