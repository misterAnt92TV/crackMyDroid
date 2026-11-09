package com.crackmydroid.shared.presentation

import com.crackmydroid.shared.util.AppDispatchers
import com.crackmydroid.shared.util.DefaultAppDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

open class BaseViewModel(
    protected val dispatchers: AppDispatchers = DefaultAppDispatchers,
) {
    private val job = SupervisorJob()
    protected val scope = CoroutineScope(job + dispatchers.main)

    open fun onCleared() {
        scope.cancel()
    }
}
