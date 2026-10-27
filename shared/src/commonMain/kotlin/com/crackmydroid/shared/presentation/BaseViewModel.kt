package com.crackmydroid.shared.presentation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

interface AppDispatchers {
    val main: CoroutineDispatcher
    val default: CoroutineDispatcher
}

object DefaultAppDispatchers : AppDispatchers {
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val default: CoroutineDispatcher = Dispatchers.Default
}

open class BaseViewModel(
    protected val dispatchers: AppDispatchers = DefaultAppDispatchers
) {
    private val job = SupervisorJob()
    protected val scope = CoroutineScope(job + dispatchers.main)

    open fun onCleared() {
        scope.cancel()
    }
}
