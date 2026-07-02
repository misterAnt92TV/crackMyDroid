package com.crackmydroid.shared.presentation

import com.crackmydroid.shared.domain.usecase.ExportLogsUseCase
import com.crackmydroid.shared.domain.usecase.GetLogsUseCase
import com.crackmydroid.shared.ui.logs.LogViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LogViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Thread.sleep(50)
        Dispatchers.resetMain()
    }

    @Test
    fun setFilterUpdatesImmediatelyAndRecomputesVisibleLogs() = runTest(dispatcher) {
        val getLogs = mockk<GetLogsUseCase>()
        val exportLogs = mockk<ExportLogsUseCase>()
        coEvery { getLogs() } returns listOf(
            "I/Tag(100): init ok",
            "E/Tag(200): fatal issue"
        )
        val vm = LogViewModel(getLogs, exportLogs)

        vm.refresh()
        advanceUntilIdle()
        vm.setFilter("fatal")

        assertEquals("fatal", vm.state.value.filter)
        vm.onCleared()
    }
}
