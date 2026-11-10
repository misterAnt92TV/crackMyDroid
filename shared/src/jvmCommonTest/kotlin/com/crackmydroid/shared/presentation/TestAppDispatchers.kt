package com.crackmydroid.shared.presentation

import com.crackmydroid.architecture.coroutines.AppDispatchers
import kotlinx.coroutines.CoroutineDispatcher

internal class TestAppDispatchers(dispatcher: CoroutineDispatcher) : AppDispatchers {
    override val main = dispatcher
    override val default = dispatcher
}
