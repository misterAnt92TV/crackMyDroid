package com.crackmydroid.shared.presentation

import com.crackmydroid.shared.util.AppDispatchers
import com.crackmydroid.shared.util.DefaultAppDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class BaseViewModelTest {
    @Test
    fun onClearedCancelsChildCoroutines() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val viewModel = TestViewModel(
            object : AppDispatchers {
                override val main: CoroutineDispatcher = dispatcher
                override val default: CoroutineDispatcher = dispatcher
            },
        )
        var cleanupRan = false
        val job = viewModel.launch {
            try {
                awaitCancellation()
            } finally {
                cleanupRan = true
            }
        }

        runCurrent()
        assertTrue(job.isActive)

        viewModel.onCleared()
        runCurrent()

        assertTrue(job.isCancelled)
        assertTrue(cleanupRan)
    }

    @Test
    fun updateLanguageUpdatesAllCatalogsAndNotifiesOnlyForEffectiveChanges() {
        val viewModel = TestViewModel(DefaultAppDispatchers)

        viewModel.updateLanguage("fr-FR")

        assertEquals("fr", viewModel.useCaseLanguage)
        assertEquals("Test root", viewModel.localizedStrings.navRoot)
        assertEquals("Erreur", viewModel.localizedCopy.error)
        assertEquals(1, viewModel.languageChanges)

        viewModel.updateLanguage("fr")
        viewModel.updateLanguage("unknown")

        assertEquals(2, viewModel.languageChanges)
        assertEquals("en", viewModel.useCaseLanguage)
        viewModel.onCleared()
    }

    private class TestViewModel(dispatchers: AppDispatchers) : BaseViewModel(dispatchers) {
        var languageChanges = 0
        val useCaseLanguage get() = useCaseTexts.languageCode
        val localizedStrings get() = strings
        val localizedCopy get() = presentationTexts

        fun launch(block: suspend () -> Unit): Job = scope.launch { block() }

        override fun onLanguageChanged() {
            languageChanges++
        }
    }
}
