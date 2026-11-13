package com.crackmydroid.architecture.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class SerialEventQueueTest {
    @Test
    fun consumesAcceptedEventsInFifoOrder() = runTest {
        val consumed = mutableListOf<Int>()
        val queue = SerialEventQueue<Int>(this, capacity = 3) { consumed += it }

        assertEquals(QueueResult.Accepted, queue.emit(1))
        assertEquals(QueueResult.Accepted, queue.emit(2))
        assertEquals(QueueResult.Accepted, queue.emit(3))
        queue.awaitIdle()

        assertEquals(listOf(1, 2, 3), consumed)
        queue.close()
    }

    @Test
    fun rejectsOverflowWithoutDroppingAcceptedEvents() = runTest {
        val gate = CompletableDeferred<Unit>()
        val started = CompletableDeferred<Unit>()
        val consumed = mutableListOf<Int>()
        val queue = SerialEventQueue<Int>(this, capacity = 1) { event ->
            started.complete(Unit)
            gate.await()
            consumed += event
        }

        assertEquals(QueueResult.Accepted, queue.emit(1))
        runCurrent()
        started.await()
        assertEquals(QueueResult.Accepted, queue.emit(2))
        assertEquals(QueueResult.Full, queue.emit(3))
        gate.complete(Unit)
        queue.awaitIdle()

        assertEquals(listOf(1, 2), consumed)
        queue.close()
    }

    @Test
    fun rejectsEventsAfterClose() = runTest {
        val queue = SerialEventQueue<Int>(this) { }

        queue.close()

        assertEquals(QueueResult.Closed, queue.emit(1))
    }

    @Test
    fun closesWhenItsOwnerScopeIsCancelled() = runTest {
        val owner = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler))
        val queue = SerialEventQueue<Int>(owner) { }

        owner.cancel()
        runCurrent()

        assertEquals(QueueResult.Closed, queue.emit(1))
    }
}
