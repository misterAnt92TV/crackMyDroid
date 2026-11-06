package com.crackmydroid.shared.presentation

import com.crackmydroid.shared.util.AppDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class BaseViewModelTest {
    @Test
    fun onClearedCancelsChildCoroutines() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val viewModel = TestViewModel(
            object : AppDispatchers {
                override val main: CoroutineDispatcher = dispatcher
                override val default: CoroutineDispatcher = dispatcher
            }
        )
        var cleanupRan = false
        val job = viewModel.launch {
            try {
                awaitCancellation()
            } finally {
                cleanupRan = true
            }
        }

        runCurrent()
        assertTrue(job.isActive)

        viewModel.onCleared()
        runCurrent()

        assertTrue(job.isCancelled)
        assertTrue(cleanupRan)
    }

    private class TestViewModel(dispatchers: AppDispatchers) : BaseViewModel(dispatchers) {
        fun launch(block: suspend () -> Unit): Job = scope.launch { block() }
    }
}
