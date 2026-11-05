package com.crackmydroid.shared.presentation

import kotlinx.coroutines.flow.Flow

/**
 * UDF Pattern - Events da UI verso ViewModel
 */
interface UiEvent

/**
 * UDF Pattern - State immutabile da ViewModel verso UI
 */
interface UiViewState

/**
 * UDF Pattern - Base ViewModel con proper state management
 * Usa MVI (Model-View-Intent) pattern con structured concurrency
 */
abstract class UdfViewModel<Event : UiEvent, State : UiViewState> : BaseViewModel() {
    abstract val state: Flow<State>

    abstract suspend fun handleEvent(event: Event)
}
