package com.crackmydroid.shared.util

import kotlinx.coroutines.CancellationException

internal suspend fun <T> runSuspendCatching(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (failure: Exception) {
        Result.failure(failure)
    }
