package com.crackmydroid.shared.presentation

import com.crackmydroid.architecture.testing.*


import com.crackmydroid.shared.domain.model.shell.ShellCommand
import com.crackmydroid.shared.domain.model.shell.ShellCommandResult
import com.crackmydroid.shared.domain.usecase.trick.ExecuteShellCommandUseCase
import com.crackmydroid.shared.domain.usecase.trick.ListShellCommandsUseCase
import com.crackmydroid.shared.ui.trick.TrickEvent
import com.crackmydroid.shared.ui.trick.TrickViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.yield
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class TrickViewModelTest : BaseViewModelUnitTest() {
    private fun createViewModel(
        listUseCase: ListShellCommandsUseCase = mockk(),
        execUseCase: ExecuteShellCommandUseCase = mockk(),
    ) = autoClear(TrickViewModel(listUseCase, execUseCase, appDispatchers))

    @Test
    fun loadPopulatesCommands() = runViewModelTest {
        val listUseCase = mockk<ListShellCommandsUseCase>()
        val execUseCase = mockk<ExecuteShellCommandUseCase>()
        val cmd = ShellCommand("Elenco pacchetti", "pm list packages", requiresRoot = false)
        coEvery { listUseCase() } returns Result.success(listOf(cmd))
        val vm = createViewModel(listUseCase, execUseCase)

        val state = vm.emitEventAndAwaitIdle(TrickEvent.Load())

        assertEquals(1, state.commands.size)
        assertEquals("Elenco pacchetti", state.commands.first().title)
    }

    @Test
    fun secondExecuteWhileRunningIsBlockedAndShowsHint() = runViewModelTest {
        val listUseCase = mockk<ListShellCommandsUseCase>()
        val execUseCase = mockk<ExecuteShellCommandUseCase>()
        val packages = ShellCommand("Elenco pacchetti", "pm list packages", requiresRoot = false)
        val top = ShellCommand("Mostra top process", "top -n 1", requiresRoot = false)
        coEvery { execUseCase(packages) } coAnswers {
            yield()
            Result.success(ShellCommandResult(success = true, stdout = "package:com.test", stderr = "", code = 0))
        }
        coEvery { execUseCase(top) } returns Result.success(
            ShellCommandResult(
                success = true,
                stdout = "ok",
                stderr = "",
                code = 0,
            ),
        )
        val vm = createViewModel(listUseCase, execUseCase)

        vm.handleEvent(TrickEvent.Execute(packages))
        assertTrue(vm.state.value.running)
        assertEquals("Elenco pacchetti", vm.state.value.runningCommand?.title)

        vm.handleEvent(TrickEvent.Execute(top))
        assertTrue(vm.state.value.running)
        assertTrue(vm.state.value.interactionHint?.contains("Elenco pacchetti") == true)

        advanceUntilIdle()

        assertEquals(false, vm.state.value.running)
        assertEquals("Elenco pacchetti", vm.state.value.lastCommandTitle)
        coVerify(exactly = 1) { execUseCase(packages) }
        coVerify(exactly = 0) { execUseCase(top) }
    }

    @Test
    fun execute_failedCommand_storesErrorAndClearsRunningState() = runViewModelTest {
        val listUseCase = mockk<ListShellCommandsUseCase>()
        val execUseCase = mockk<ExecuteShellCommandUseCase>()
        val command = ShellCommand("Root check", "id", requiresRoot = true)
        coEvery { execUseCase(command) } returns Result.failure(IllegalStateException("su unavailable"))
        val vm = createViewModel(listUseCase, execUseCase)

        val state = vm.emitEventAndAwaitIdle(TrickEvent.Execute(command))

        assertEquals(false, state.running)
        assertEquals("Root check", state.lastCommandTitle)
        assertEquals("su unavailable", state.errorMessage)

        val dismissedState = vm.emitEventAndAwaitIdle(TrickEvent.ErrorDismissed())
        assertEquals(null, dismissedState.errorMessage)
    }

    @Test
    fun unsupportedCommand_andClearHint_manageInteractionHint() = runViewModelTest {
        val vm = createViewModel()
        val command = ShellCommand("Root command", "id", requiresRoot = true, unsupportedReason = "Root unavailable")

        val stateWithHint = vm.emitEventAndAwaitIdle(TrickEvent.UnsupportedCommandNotified(command))
        assertEquals("Root unavailable", stateWithHint.interactionHint)

        val stateAfterDismiss = vm.emitEventAndAwaitIdle(TrickEvent.InteractionHintDismissed())
        assertEquals(null, stateAfterDismiss.interactionHint)
    }
}
