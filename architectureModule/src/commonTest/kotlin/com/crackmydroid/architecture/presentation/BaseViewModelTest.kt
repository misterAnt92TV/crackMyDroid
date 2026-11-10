package com.crackmydroid.architecture.presentation

import com.crackmydroid.architecture.coroutines.AppDispatchers
import com.crackmydroid.architecture.presentation.model.UiEvent
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalUuidApi::class)
class BaseViewModelTest {
    @Test
    fun eventIdIsStableAndUniqueForNewInstances() {
        val first = TestEvent()
        val second = TestEvent()

        assertEquals(first.id, first.id)
        assertNotEquals(first.id, second.id)
    }

    @Test
    fun handleEventDelegatesToOnEvent() {
        val viewModel = TestViewModel(testDispatchers(StandardTestDispatcher()))
        val event = TestEvent()

        viewModel.handleEvent(event)

        assertEquals(event, viewModel.handledEvent)
    }
    @Test
    fun onClearedCancelsChildCoroutines() = runTest {
        val viewModel = TestViewModel(testDispatchers(StandardTestDispatcher(testScheduler)))
        var cleanupRan = false
        val job = viewModel.launch {
            try {
                awaitCancellation()
            } finally {
                cleanupRan = true
            }
        }

        runCurrent()
        viewModel.onCleared()
        runCurrent()

        assertTrue(job.isCancelled)
        assertTrue(cleanupRan)
    }

    @Test
    fun latestTaskCancelsPreviousAndRejectsLateResults() = runTest {
        val viewModel = TestViewModel(testDispatchers(StandardTestDispatcher(testScheduler)))
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

    private fun testDispatchers(dispatcher: CoroutineDispatcher): AppDispatchers = object : AppDispatchers {
        override val main = dispatcher
        override val default = dispatcher
    }

    private class TestViewModel(dispatchers: AppDispatchers) : BaseViewModel<TestEvent>(dispatchers) {
        var handledEvent: TestEvent? = null

        override fun onEvent(event: TestEvent) {
            handledEvent = event
        }

        fun launch(block: suspend () -> Unit): Job = scope.launch { block() }
        fun latest(key: String, block: suspend () -> Unit): Job = launchLatest(key) { block() }
        fun nonCooperativeLatest(key: String, gate: CompletableDeferred<Unit>, publish: () -> Unit): Job =
            launchLatest(key) {
                withContext(NonCancellable) { gate.await() }
                ensureCurrent()
                publish()
            }
    }

    private class TestEvent : UiEvent()
}
