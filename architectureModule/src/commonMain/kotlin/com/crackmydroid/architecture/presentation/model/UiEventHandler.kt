package com.crackmydroid.architecture.presentation.model

/** Public input boundary for an event-driven presentation model. */
interface UiEventHandler<E : UiEvent> {
    fun handleEvent(event: E)
}
