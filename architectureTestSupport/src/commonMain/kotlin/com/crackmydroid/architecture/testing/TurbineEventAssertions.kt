package com.crackmydroid.architecture.testing

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.crackmydroid.architecture.presentation.BaseViewModel
import com.crackmydroid.architecture.presentation.model.UiEvent
import com.crackmydroid.architecture.presentation.model.UiState
import kotlin.time.Duration
import kotlinx.coroutines.flow.Flow

/** Subscribes before dispatch, so the initial state and every following state are observable. */
suspend fun <E : UiEvent, S : UiState> BaseViewModel<E, S>.assertEventState(
    event: E,
    timeout: Duration? = null,
    validate: suspend ReceiveTurbine<S>.() -> Unit,
) {
    if (timeout == null) {
        state.test {
            emitEvent(event)
            validate()
        }
    } else {
        state.test(timeout = timeout) {
            emitEvent(event)
            validate()
        }
    }
}

/** Subscribes before dispatch, preventing a one-shot SharedFlow effect from being missed. */
suspend fun <E : UiEvent, S : UiState, F> BaseViewModel<E, S>.assertEventEffect(
    event: E,
    effectFlow: Flow<F>,
    timeout: Duration? = null,
    validate: suspend ReceiveTurbine<F>.() -> Unit,
) {
    if (timeout == null) {
        effectFlow.test {
            emitEvent(event)
            validate()
        }
    } else {
        effectFlow.test(timeout = timeout) {
            emitEvent(event)
            validate()
        }
    }
}
