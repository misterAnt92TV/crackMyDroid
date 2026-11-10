package com.crackmydroid.shared.domain.usecase

import co.touchlab.kermit.Logger
import com.crackmydroid.shared.AppLogger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlin.time.TimeSource

abstract class BaseUseCase(
    private val name: String,
    private val dispatcher: CoroutineDispatcher? = null,
    private val logger: Logger = AppLogger.logger,
    private val verbose: () -> Boolean = AppLogger::isVerbose,
) {
    protected suspend fun <T> execute(
        operation: String = "invoke",
        block: suspend () -> T,
    ): Result<T> {
        val started = TimeSource.Monotonic.markNow()
        fun debug(outcome: String) {
            if (verbose()) {
                logger.d {
                    "useCase=$name operation=$operation outcome=$outcome durationMs=${started.elapsedNow().inWholeMilliseconds}"
                }
            }
        }
        debug("started")
        try {
            val value = try {
                currentCoroutineContext().ensureActive()
                val value = if (dispatcher == null) block() else withContext(dispatcher) { block() }
                currentCoroutineContext().ensureActive()
                value
            } catch (cancellation: CancellationException) {
                debug("cancelled")
                throw cancellation
            } catch (failure: Exception) {
                currentCoroutineContext().ensureActive()
                logger.e {
                    "useCase=$name operation=$operation outcome=failed durationMs=${started.elapsedNow().inWholeMilliseconds} category=${failure::class.simpleName ?: "Exception"}"
                }
                return Result.failure(failure)
            }
            debug("succeeded")
            return Result.success(value)
        } catch (cancellation: CancellationException) {
            debug("cancelled")
            throw cancellation
        }
    }
}
