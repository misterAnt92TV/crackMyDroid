package com.crackmydroid.shared.domain.usecase

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.StaticConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class BaseUseCaseTest {
    @Test
    fun preservesSuccessNullUnitAndExceptionIdentity() = runTest {
        val useCase = TestUseCase()
        assertEquals(42, useCase.run { 42 }.getOrThrow())
        assertEquals(null, useCase.run<String?> { null }.getOrThrow())
        assertEquals(Unit, useCase.run { Unit }.getOrThrow())
        val failure = IllegalStateException("original")
        assertSame(failure, useCase.run<Unit> { throw failure }.exceptionOrNull())
        assertFailsWith<AssertionError> { useCase.run<Unit> { throw AssertionError("fatal") } }
    }

    @Test
    fun cancelledCallerDoesNotRunBlockOrLogFailure() = runTest {
        val writer = RecordingWriter()
        val useCase = TestUseCase(writer, verbose = { true })
        var ran = false
        val job = launch {
            currentCoroutineContext().cancel()
            useCase.run { ran = true }
        }
        job.join()
        assertTrue(job.isCancelled)
        assertFalse(ran)
        assertTrue(writer.entries.any { it.message.contains("outcome=cancelled") })
        assertFalse(writer.entries.any { it.severity == Severity.Error })
    }

    @Test
    fun cancellationAfterNonSuspendingWorkCannotBecomeSuccess() = runTest {
        val useCase = TestUseCase()
        var returned = false
        val job = launch {
            useCase.run {
                currentCoroutineContext().cancel()
                "value"
            }
            returned = true
        }
        job.join()
        assertTrue(job.isCancelled)
        assertFalse(returned)
    }

    @Test
    fun cancellationDominatesOrdinaryFailureAfterCallerWasCancelled() = runTest {
        val writer = RecordingWriter()
        val useCase = TestUseCase(writer)
        val job = launch {
            useCase.run<Unit> {
                currentCoroutineContext().cancel()
                throw IllegalStateException("failed during cleanup")
            }
        }
        job.join()
        assertTrue(job.isCancelled)
        assertTrue(writer.entries.isEmpty())
    }

    @Test
    fun callerCancellationReachesStructuredChildren() = runTest {
        val useCase = TestUseCase()
        var cleanupRan = false
        lateinit var child: Job
        val caller = launch {
            useCase.run {
                coroutineScope {
                    child = launch(start = CoroutineStart.UNDISPATCHED) {
                        try {
                            awaitCancellation()
                        } finally {
                            cleanupRan = true
                        }
                    }
                    awaitCancellation()
                }
            }
        }
        runCurrent()
        caller.cancel()
        caller.join()
        assertTrue(child.isCancelled)
        assertTrue(cleanupRan)
    }

    @Test
    fun explicitAndResultWrappedCancellationAreRethrown() = runTest {
        val useCase = TestUseCase()
        val cancellation = CancellationException("cancel")
        assertSame(
            cancellation,
            assertFailsWith<CancellationException> {
                useCase.run<Unit> { throw cancellation }
            },
        )
        assertSame(
            cancellation,
            assertFailsWith<CancellationException> {
                useCase.run { Result.failure<Unit>(cancellation).getOrThrow() }
            },
        )
    }

    @Test
    fun injectedDispatcherSchedulesWorkInsteadOfRunningOnCaller() = runTest {
        val useCase = TestUseCase(dispatcher = StandardTestDispatcher(testScheduler))
        var ran = false
        val caller = launch(start = CoroutineStart.UNDISPATCHED) {
            useCase.run { ran = true }
        }
        assertFalse(ran)
        runCurrent()
        caller.join()
        assertTrue(ran)
    }

    @Test
    fun verbosityControlsDebugButNotErrorsAndLogsNeverContainPayloads() = runTest {
        val writer = RecordingWriter()
        var verbose = false
        val useCase = TestUseCase(writer, verbose = { verbose })
        val sensitive = "SENTINEL_TOKEN_COMMAND_DEVICE_PATH"
        useCase.run { sensitive }
        assertTrue(writer.entries.isEmpty())

        val failure = IllegalStateException(sensitive, IllegalArgumentException(sensitive))
        assertSame(failure, useCase.run<Unit> { throw failure }.exceptionOrNull())
        assertEquals(Severity.Error, writer.entries.single().severity)

        verbose = true
        useCase.run { sensitive }
        assertEquals(2, writer.entries.count { it.severity == Severity.Debug })
        assertTrue(writer.entries.any { it.message.contains("outcome=started") })
        assertTrue(writer.entries.any { it.message.contains("outcome=succeeded") })

        assertFailsWith<CancellationException> { useCase.run<Unit> { throw CancellationException(sensitive) } }
        assertTrue(writer.entries.any { it.message.contains("outcome=cancelled") })
        assertEquals(1, writer.entries.count { it.severity == Severity.Error })
        writer.entries.forEach { entry ->
            assertTrue(entry.message.contains("useCase=TestUseCase operation=sample"))
            assertTrue(Regex("durationMs=\\d+").containsMatchIn(entry.message))
            assertFalse(entry.message.contains(sensitive))
            assertEquals(null, entry.throwable)
        }
    }

    private class TestUseCase(
        writer: RecordingWriter = RecordingWriter(),
        dispatcher: CoroutineDispatcher? = null,
        verbose: () -> Boolean = { false },
    ) : BaseUseCase(
        name = TAG,
        dispatcher = dispatcher,
        logger = Logger(StaticConfig(logWriterList = listOf(writer)), "UseCaseTest"),
        verbose = verbose,
    ) {
        private companion object {
            private const val TAG = "TestUseCase"
        }

        suspend fun <T> run(block: suspend () -> T): Result<T> = execute("sample", block)
    }

    private class RecordingWriter : LogWriter() {
        val entries = mutableListOf<Entry>()
        override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
            entries += Entry(severity, message, throwable)
        }
    }

    private data class Entry(val severity: Severity, val message: String, val throwable: Throwable?)
}
