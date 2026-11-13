package com.crackmydroid.architecture.presentation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Opt-in FIFO processor for workflows that must execute accepted commands one at a time.
 * It is intentionally independent from [BaseViewModel] so ordinary UI events stay synchronous.
 */
class SerialEventQueue<E>(
    scope: CoroutineScope,
    capacity: Int = DEFAULT_CAPACITY,
    private val consume: suspend (E) -> Unit,
) {
    private val events = Channel<E>(capacity.also { require(it > 0) })
    private val pendingEvents = MutableStateFlow(0)
    private val closed = MutableStateFlow(false)
    private val consumer: Job = scope.launch {
        try {
            for (event in events) {
                try {
                    consume(event)
                } finally {
                    pendingEvents.update { count -> (count - 1).coerceAtLeast(0) }
                }
            }
        } finally {
            closed.value = true
            pendingEvents.value = 0
            events.close()
        }
    }

    init {
        consumer.invokeOnCompletion {
            closed.value = true
            pendingEvents.value = 0
            events.close()
        }
    }

    fun emit(event: E): QueueResult {
        if (closed.value) return QueueResult.Closed

        pendingEvents.update { it + 1 }
        val result = events.trySend(event)
        if (result.isSuccess) return QueueResult.Accepted

        pendingEvents.update { count -> (count - 1).coerceAtLeast(0) }
        return if (result.isClosed || closed.value) QueueResult.Closed else QueueResult.Full
    }

    suspend fun awaitIdle() {
        pendingEvents.first { it == 0 }
    }

    fun close() {
        if (closed.value) return
        closed.value = true
        events.close()
    }

    companion object {
        const val DEFAULT_CAPACITY = 64
    }
}

sealed interface QueueResult {
    data object Accepted : QueueResult
    data object Full : QueueResult
    data object Closed : QueueResult
}
