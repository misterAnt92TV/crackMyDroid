package com.crackmydroid.shared.presentation

import com.crackmydroid.shared.util.AppDispatchers
import com.crackmydroid.shared.util.DefaultAppDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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

    @Test
    fun latestTaskCancelsPreviousAndRejectsItsLateResult() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val viewModel = TestViewModel(testDispatchers(dispatcher))
        val oldMayFinish = CompletableDeferred<Unit>()
        var oldPublished = false
        var newPublished = false

        viewModel.nonCooperativeLatest("refresh", oldMayFinish) { oldPublished = true }
        runCurrent()
        viewModel.latest("refresh") { newPublished = true }
        oldMayFinish.complete(Unit)
        runCurrent()

        assertFalse(oldPublished)
        assertTrue(newPublished)
        viewModel.onCleared()
    }

    @Test
    fun cancelLatestCancelsOnlyTheMatchingLatestTask() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val viewModel = TestViewModel(testDispatchers(dispatcher))
        val cancelled = CompletableDeferred<Unit>()
        val otherCompleted = CompletableDeferred<Unit>()

        viewModel.latest("scan") {
            try {
                awaitCancellation()
            } finally {
                cancelled.complete(Unit)
            }
        }
        viewModel.latest("preview") { otherCompleted.complete(Unit) }
        runCurrent()
        viewModel.cancel("scan")
        runCurrent()

        assertTrue(cancelled.isCompleted)
        assertTrue(otherCompleted.isCompleted)
        viewModel.onCleared()
    }

    private fun testDispatchers(dispatcher: CoroutineDispatcher): AppDispatchers = object : AppDispatchers {
        override val main = dispatcher
        override val default = dispatcher
    }

    private class TestViewModel(dispatchers: AppDispatchers) : BaseViewModel(dispatchers) {
        var languageChanges = 0
        val useCaseLanguage get() = useCaseTexts.languageCode
        val localizedStrings get() = strings
        val localizedCopy get() = presentationTexts

        fun launch(block: suspend () -> Unit): Job = scope.launch { block() }
        fun latest(key: String, block: suspend () -> Unit): Job = launchLatest(key) { block() }
        fun nonCooperativeLatest(
            key: String,
            gate: CompletableDeferred<Unit>,
            publish: () -> Unit,
        ): Job = launchLatest(key) {
            withContext(NonCancellable) { gate.await() }
            ensureCurrent()
            publish()
        }
        fun cancel(key: String) = cancelLatest(key)

        override fun onLanguageChanged() {
            languageChanges++
        }
    }
}
