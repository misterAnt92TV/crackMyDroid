package com.crackmydroid.architecture.presentation

import com.crackmydroid.architecture.coroutines.AppDispatchers
import com.crackmydroid.architecture.presentation.model.UiEvent
import com.crackmydroid.architecture.presentation.model.UiViewState
import kotlinx.coroutines.flow.Flow

abstract class UdfViewModel<Event : UiEvent, State : UiViewState>(dispatchers: AppDispatchers) :
    BaseViewModel(dispatchers) {
    abstract val state: Flow<State>

    abstract suspend fun handleEvent(event: Event)
}
