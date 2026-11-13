package com.crackmydroid.shared.presentation

import com.crackmydroid.architecture.testing.*


import com.crackmydroid.architecture.coroutines.DefaultAppDispatchers
import com.crackmydroid.architecture.presentation.model.UiEvent
import com.crackmydroid.architecture.presentation.model.UiState
import kotlin.test.Test
import kotlin.test.assertEquals

class LocalizedBaseViewModelTest : BaseViewModelUnitTest() {
    @Test
    fun updateLanguageUpdatesAllCatalogsAndNotifiesOnlyForEffectiveChanges() = runViewModelTest {
        val viewModel = autoClear(TestViewModel())

        viewModel.updateLanguage("fr-FR")

        assertEquals("fr", viewModel.useCaseLanguage)
        assertEquals("Test root", viewModel.localizedStrings.navRoot)
        assertEquals("Erreur", viewModel.localizedCopy.error)
        assertEquals(1, viewModel.languageChanges)

        viewModel.updateLanguage("fr")
        viewModel.updateLanguage("unknown")

        assertEquals(2, viewModel.languageChanges)
        assertEquals("en", viewModel.useCaseLanguage)
    }

    private inner class TestViewModel :
        LocalizedBaseViewModel<TestEvent, TestState>(TestState, appDispatchers) {
        var languageChanges = 0
        val useCaseLanguage get() = useCaseTexts.languageCode
        val localizedStrings get() = strings
        val localizedCopy get() = presentationTexts

        override fun onLanguageChanged() {
            languageChanges++
        }

        override fun onEvent(event: TestEvent) = Unit
    }

    private class TestEvent : UiEvent()
    private data object TestState : UiState
}
