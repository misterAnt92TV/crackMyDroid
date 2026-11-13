package com.crackmydroid.architecture.testing

import com.crackmydroid.architecture.coroutines.AppDispatchers
import com.crackmydroid.architecture.presentation.BaseViewModel
import com.crackmydroid.architecture.presentation.model.UiEvent
import com.crackmydroid.architecture.presentation.model.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseViewModelUnitTest(
    val testDispatcher: TestDispatcher = StandardTestDispatcher(),
) {
    val dispatcher: TestDispatcher get() = testDispatcher
    val appDispatchers: AppDispatchers = TestAppDispatchers(testDispatcher)

    private val trackedViewModels = mutableListOf<BaseViewModel<*, *>>()

    open fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    open fun tearDown() {
        trackedViewModels.forEach { it.onCleared() }
        trackedViewModels.clear()
        Dispatchers.resetMain()
    }

    fun <VM : BaseViewModel<*, *>> autoClear(viewModel: VM): VM = viewModel.also(trackedViewModels::add)

    fun runViewModelTest(block: suspend TestScope.() -> Unit) = runTest(testDispatcher, testBody = block)

    /** Dispatches and drains virtual time for tests that assert a settled asynchronous state. */
    fun <E : UiEvent, S : UiState> BaseViewModel<E, S>.emitEventAndAwaitIdle(event: E): S {
        emitEvent(event)
        advanceUntilIdle()
        return state.value
    }

    fun advanceUntilIdle() = testDispatcher.scheduler.advanceUntilIdle()

    fun runCurrent() = testDispatcher.scheduler.runCurrent()

    fun advanceTimeBy(delayTimeMillis: Long) = testDispatcher.scheduler.advanceTimeBy(delayTimeMillis)
}

class TestAppDispatchers(dispatcher: TestDispatcher) : AppDispatchers {
    override val main = dispatcher
    override val default = dispatcher
}

/** Dispatches a sequence through the ViewModel's public event entry point. */
fun <E : UiEvent, S : UiState> BaseViewModel<E, S>.emitEvents(vararg events: E): S {
    events.forEach(::emitEvent)
    return state.value
}
