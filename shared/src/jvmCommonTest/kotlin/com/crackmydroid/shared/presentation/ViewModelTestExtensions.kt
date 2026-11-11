package com.crackmydroid.shared.presentation

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.crackmydroid.architecture.presentation.BaseViewModel
import com.crackmydroid.architecture.presentation.model.UiEvent
import com.crackmydroid.architecture.presentation.model.UiState
import kotlin.time.Duration
import kotlinx.coroutines.flow.SharedFlow

/**
 * Asserts the emissions of a ViewModel's [StateFlow] after dispatching an [event] through [BaseViewModel.handleEvent].
 */
suspend fun <E : UiEvent, S : UiState> BaseViewModel<E, S>.assertEvent(
    event: E,
    timeout: Duration? = null,
    validate: suspend ReceiveTurbine<S>.() -> Unit,
) {
    if (timeout != null) {
        state.test(timeout = timeout) {
            handleEvent(event)
            validate()
        }
    } else {
        state.test {
            handleEvent(event)
            validate()
        }
    }
}

/**
 * Asserts the emissions of a ViewModel's [SharedFlow] (e.g. effects) after dispatching an [event] through [BaseViewModel.handleEvent].
 */
suspend fun <E : UiEvent, S : UiState, F> BaseViewModel<E, S>.assertEffect(
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
