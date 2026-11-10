package com.crackmydroid.shared.presentation

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.crackmydroid.architecture.presentation.BaseViewModel
import com.crackmydroid.architecture.presentation.model.UiEvent
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

/**
 * Asserts the emissions of a ViewModel's [StateFlow] after dispatching an [event] through [BaseViewModel.handleEvent].
 */
suspend fun <E : UiEvent, S> BaseViewModel<E>.assertEvent(
    event: E,
    stateFlow: StateFlow<S>,
    timeout: Duration? = null,
    validate: suspend ReceiveTurbine<S>.() -> Unit,
) {
    if (timeout != null) {
        stateFlow.test(timeout = timeout) {
            handleEvent(event)
            validate()
        }
    } else {
        stateFlow.test {
            handleEvent(event)
            validate()
        }
    }
}

/**
 * Asserts the emissions of a ViewModel's [SharedFlow] (e.g. effects) after dispatching an [event] through [BaseViewModel.handleEvent].
 */
suspend fun <E : UiEvent, F> BaseViewModel<E>.assertEffect(
    event: E,
    effectFlow: SharedFlow<F>,
    timeout: Duration? = null,
    validate: suspend ReceiveTurbine<F>.() -> Unit,
) {
    if (timeout != null) {
        effectFlow.test(timeout = timeout) {
            handleEvent(event)
            validate()
        }
    } else {
        effectFlow.test {
            handleEvent(event)
            validate()
        }
    }
}
