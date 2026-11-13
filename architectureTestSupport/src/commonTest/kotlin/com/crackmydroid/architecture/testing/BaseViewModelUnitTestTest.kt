package com.crackmydroid.architecture.testing

import com.crackmydroid.architecture.presentation.BaseViewModel
import com.crackmydroid.architecture.presentation.model.UiEvent
import com.crackmydroid.architecture.presentation.model.UiState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.MutableSharedFlow

class BaseViewModelUnitTestTest : BaseViewModelUnitTest() {
    @Test
    fun emitEventDispatchesSynchronouslyBeforeReturning() = runViewModelTest {
        val viewModel = autoClear(TestViewModel(appDispatchers))

        viewModel.emitEvent(Increment)

        assertEquals(1, viewModel.state.value.count)
        assertEquals(1, viewModel.receivedEvents)
        tearDown()
    }

    @Test
    fun assertEventStateSubscribesBeforeDispatch() = runViewModelTest {
        val viewModel = autoClear(TestViewModel(appDispatchers))

        viewModel.assertEventState(Increment) {
            assertEquals(0, awaitItem().count)
            assertEquals(1, awaitItem().count)
            cancelAndIgnoreRemainingEvents()
        }
        tearDown()
    }

    @Test
    fun assertEventEffectSubscribesBeforeDispatch() = runViewModelTest {
        val viewModel = autoClear(TestViewModel(appDispatchers))

        viewModel.assertEventEffect(EmitEffect, viewModel.effects) {
            assertEquals("handled", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        tearDown()
    }

    private class TestViewModel(dispatchers: com.crackmydroid.architecture.coroutines.AppDispatchers) :
        BaseViewModel<TestEvent, TestState>(TestState(), dispatchers) {
        private val mutableEffects = MutableSharedFlow<String>()
        val effects = mutableEffects
        var receivedEvents = 0

        override fun onEvent(event: TestEvent) {
            receivedEvents++
            when (event) {
                Increment -> updateState { it.copy(count = it.count + 1) }
                EmitEffect -> launchTask { mutableEffects.emit("handled") }
            }
        }
    }

    private sealed class TestEvent : UiEvent()
    private data object Increment : TestEvent()
    private data object EmitEffect : TestEvent()
    private data class TestState(val count: Int = 0) : UiState
}
