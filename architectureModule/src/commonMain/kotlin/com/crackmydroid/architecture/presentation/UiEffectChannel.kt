package com.crackmydroid.architecture.presentation

import com.crackmydroid.architecture.presentation.model.UiEffect
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * One-shot effect stream with no replay.
 *
 * A channel keeps an effect while the UI collector is being recreated. The channel is closed by
 * [BaseViewModel.onCleared] through the owning ViewModel.
 */
class UiEffectChannel<F : UiEffect> internal constructor() {
    private val channel = Channel<F>(Channel.BUFFERED)

    val effects: Flow<F> = channel.receiveAsFlow()

    suspend fun emit(effect: F) {
        channel.send(effect)
    }

    internal fun close() = channel.close()
}
