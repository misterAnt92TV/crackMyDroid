package com.crackmydroid.shared.presentation

import com.crackmydroid.shared.domain.repository.LogRepository
import com.crackmydroid.shared.domain.usecase.ExportLogsUseCase
import com.crackmydroid.shared.domain.usecase.GetLogsUseCase
import com.crackmydroid.shared.ui.logs.LogViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LogViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val appDispatchers = object : AppDispatchers {
        override val main = dispatcher
        override val default = dispatcher
    }

    @Test
    fun refreshFiltersLogsByTextAndLevel() = runTest(dispatcher) {
        val repository = mockk<LogRepository>()
        coEvery { repository.fetchLog() } returns listOf(
            "I/App: started",
            "E/App: failed",
            "W/App: retrying"
        )
        val viewModel = LogViewModel(
            GetLogsUseCase(repository),
            ExportLogsUseCase(repository),
            appDispatchers
        )

        viewModel.refresh()
        advanceUntilIdle()
        viewModel.setFilter("app: failed")
        advanceUntilIdle()

        assertEquals(listOf("E/App: failed"), viewModel.filtered())
        viewModel.onCleared()
    }

    @Test
    fun exportUsesCurrentlyVisibleLogs() = runTest(dispatcher) {
        val repository = mockk<LogRepository>()
        coEvery { repository.fetchLog() } returns listOf("I/App: started", "E/App: failed")
        coEvery { repository.export(listOf("E/App: failed")) } returns Result.success("/tmp/log.txt")
        val viewModel = LogViewModel(
            GetLogsUseCase(repository),
            ExportLogsUseCase(repository),
            appDispatchers
        )

        viewModel.refresh()
        advanceUntilIdle()
        viewModel.setFilter("failed")
        advanceUntilIdle()
        viewModel.export()
        advanceUntilIdle()

        coVerify { repository.export(listOf("E/App: failed")) }
        assertEquals("/tmp/log.txt", viewModel.state.value.message)
        viewModel.onCleared()
    }
}
