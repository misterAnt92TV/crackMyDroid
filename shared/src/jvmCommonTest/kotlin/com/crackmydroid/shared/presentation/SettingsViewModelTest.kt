package com.crackmydroid.shared.presentation

import com.crackmydroid.shared.domain.model.AppLanguage
import com.crackmydroid.shared.domain.model.AppTheme
import com.crackmydroid.shared.domain.model.ExportFormat
import com.crackmydroid.shared.domain.usecase.GetExportFormatUseCase
import com.crackmydroid.shared.domain.usecase.GetFeatureHintsEnabledUseCase
import com.crackmydroid.shared.domain.usecase.GetHighContrastUseCase
import com.crackmydroid.shared.domain.usecase.GetIntroSeenUseCase
import com.crackmydroid.shared.domain.usecase.GetLanguageUseCase
import com.crackmydroid.shared.domain.usecase.GetLargeTextUseCase
import com.crackmydroid.shared.domain.usecase.GetReduceMotionUseCase
import com.crackmydroid.shared.domain.usecase.GetSuggestionsEnabledUseCase
import com.crackmydroid.shared.domain.usecase.GetThemeUseCase
import com.crackmydroid.shared.domain.usecase.GetVerboseLogUseCase
import com.crackmydroid.shared.domain.usecase.SetExportFormatUseCase
import com.crackmydroid.shared.domain.usecase.SetFeatureHintsEnabledUseCase
import com.crackmydroid.shared.domain.usecase.SetHighContrastUseCase
import com.crackmydroid.shared.domain.usecase.SetIntroSeenUseCase
import com.crackmydroid.shared.domain.usecase.SetLanguageUseCase
import com.crackmydroid.shared.domain.usecase.SetLargeTextUseCase
import com.crackmydroid.shared.domain.usecase.SetReduceMotionUseCase
import com.crackmydroid.shared.domain.usecase.SetSuggestionsEnabledUseCase
import com.crackmydroid.shared.domain.usecase.SetThemeUseCase
import com.crackmydroid.shared.domain.usecase.SetVerboseLogUseCase
import com.crackmydroid.shared.presentation.components.SearchHistoryStore
import com.crackmydroid.shared.ui.settings.SettingsViewModel
import io.mockk.coEvery
import io.mockk.coJustRun
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
        viewModel.onCleared()
    }

    private fun viewModel(
        getLanguage: GetLanguageUseCase? = null,
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
        setLanguage: SetLanguageUseCase? = null
    ) = SettingsViewModel(
        getTheme, setTheme, getVerbose, setVerbose, getFormat, setFormat,
        getHighContrast, setHighContrast, getReduceMotion, setReduceMotion,
        getLargeText, setLargeText, getSuggestions, setSuggestions,
        getHints, setHints, getIntroSeen, setIntro, getLanguage, setLanguage
    )
}
