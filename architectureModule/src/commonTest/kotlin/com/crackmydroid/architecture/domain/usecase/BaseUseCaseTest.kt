package com.crackmydroid.architecture.domain.usecase

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.StaticConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class BaseUseCaseTest {
    @Test
    fun preservesSuccessFailureAndCancellationContracts() = runTest {
        val useCase = TestUseCase()
        assertEquals(42, useCase.run { 42 }.getOrThrow())
        val failure = IllegalStateException("original")
        assertSame(failure, useCase.run<Unit> { throw failure }.exceptionOrNull())
        assertFailsWith<AssertionError> { useCase.run<Unit> { throw AssertionError("fatal") } }
        val cancellation = CancellationException("cancel")
        assertSame(cancellation, assertFailsWith<CancellationException> { useCase.run<Unit> { throw cancellation } })
    }

    @Test
    fun usesInjectedDispatcherAndDoesNotLogPayloads() = runTest {
        val writer = RecordingWriter()
        val useCase = TestUseCase(
            dispatcher = StandardTestDispatcher(testScheduler),
            writer = writer,
            verbose = { true },
        )
        var completed = false
        val job = launch { useCase.run { completed = true } }
        assertFalse(completed)
        runCurrent()
        job.join()
        assertTrue(completed)

        val sensitive = "SENTINEL_TOKEN_COMMAND_DEVICE_PATH"
        assertEquals(sensitive, useCase.run { sensitive }.getOrThrow())
        assertTrue(writer.entries.all { !it.message.contains(sensitive) && it.throwable == null })
    }

    private class TestUseCase(
        dispatcher: CoroutineDispatcher? = null,
        writer: RecordingWriter = RecordingWriter(),
        verbose: () -> Boolean = { false },
    ) : BaseUseCase(
        name = "TestUseCase",
        dispatcher = dispatcher,
        logger = Logger(StaticConfig(logWriterList = listOf(writer)), "UseCaseTest"),
        verbose = verbose,
    ) {
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
