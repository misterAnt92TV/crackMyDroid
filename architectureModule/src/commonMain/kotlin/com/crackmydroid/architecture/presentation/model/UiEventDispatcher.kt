package com.crackmydroid.architecture.presentation.model

/** Semantic public entry point used to submit a screen interaction. */
interface UiEventDispatcher<E : UiEvent> {
    fun emitEvent(event: E)
}
