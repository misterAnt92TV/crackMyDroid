package com.crackmydroid.shared.domain.usecase

import com.crackmydroid.shared.util.AppDispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher

class UseCaseTestDispatchers(
    override val default: TestDispatcher = StandardTestDispatcher()
) : AppDispatchers {
    override val main: TestDispatcher = default
}
