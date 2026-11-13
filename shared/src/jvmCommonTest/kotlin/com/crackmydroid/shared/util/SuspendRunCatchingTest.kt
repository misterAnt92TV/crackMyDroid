package com.crackmydroid.shared.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest

class SuspendRunCatchingTest {
    @Test
    fun capturesNonCancellationExceptions() = runTest {
        val failure = IllegalStateException("operation failed")

        val result = runSuspendCatching<Int> { throw failure }

        assertSame(failure, result.exceptionOrNull())
    }

    @Test
    fun rethrowsCancellation() = runTest {
        val cancellation = CancellationException("cancelled")

        val thrown = assertFailsWith<CancellationException> {
            runSuspendCatching<Int> { throw cancellation }
        }

        assertSame(cancellation, thrown)
    }

    @Test
    fun returnsSuccessfulValues() = runTest {
        assertEquals(42, runSuspendCatching { 42 }.getOrThrow())
    }
}
