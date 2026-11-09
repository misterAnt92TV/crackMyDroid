package com.crackmydroid.shared.presentation

import com.crackmydroid.shared.domain.model.shell.ShellCommand
import com.crackmydroid.shared.domain.model.shell.ShellCommandResult
import com.crackmydroid.shared.domain.usecase.trick.ExecuteShellCommandUseCase
import com.crackmydroid.shared.domain.usecase.trick.ListShellCommandsUseCase
import com.crackmydroid.shared.ui.trick.TrickViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TrickViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadPopulatesCommands() = runTest(dispatcher) {
        val listUseCase = mockk<ListShellCommandsUseCase>()
        val execUseCase = mockk<ExecuteShellCommandUseCase>()
        val cmd = ShellCommand("Elenco pacchetti", "pm list packages", requiresRoot = false)
        coEvery { listUseCase() } returns Result.success(listOf(cmd))
        val vm = TrickViewModel(listUseCase, execUseCase)

        vm.load()
        advanceUntilIdle()

        assertEquals(1, vm.state.value.commands.size)
        assertEquals("Elenco pacchetti", vm.state.value.commands.first().title)
        vm.onCleared()
    }

    @Test
    fun secondExecuteWhileRunningIsBlockedAndShowsHint() = runTest(dispatcher) {
        val listUseCase = mockk<ListShellCommandsUseCase>()
        val execUseCase = mockk<ExecuteShellCommandUseCase>()
        val packages = ShellCommand("Elenco pacchetti", "pm list packages", requiresRoot = false)
        val top = ShellCommand("Mostra top process", "top -n 1", requiresRoot = false)
        coEvery { execUseCase(packages) } coAnswers {
            delay(120)
            Result.success(ShellCommandResult(success = true, stdout = "package:com.test", stderr = "", code = 0))
        }
        coEvery { execUseCase(top) } returns Result.success(ShellCommandResult(
            success = true,
            stdout = "ok",
            stderr = "",
            code = 0
        ))
        val vm = TrickViewModel(listUseCase, execUseCase)

        vm.execute(packages)
        assertTrue(vm.state.value.running)
        assertEquals("Elenco pacchetti", vm.state.value.runningCommand?.title)

        vm.execute(top)
        assertTrue(vm.state.value.running)
        assertTrue(vm.state.value.interactionHint?.contains("Elenco pacchetti") == true)

        advanceUntilIdle()

        assertEquals(false, vm.state.value.running)
        assertEquals("Elenco pacchetti", vm.state.value.lastCommandTitle)
        coVerify(exactly = 1) { execUseCase(packages) }
        coVerify(exactly = 0) { execUseCase(top) }
        vm.onCleared()
    }

    @Test
    fun execute_failedCommand_storesErrorAndClearsRunningState() = runTest(dispatcher) {
        val listUseCase = mockk<ListShellCommandsUseCase>()
        val execUseCase = mockk<ExecuteShellCommandUseCase>()
        val command = ShellCommand("Root check", "id", requiresRoot = true)
        coEvery { execUseCase(command) } returns Result.failure(IllegalStateException("su unavailable"))
        val vm = TrickViewModel(listUseCase, execUseCase)

        vm.execute(command)
        advanceUntilIdle()

        assertEquals(false, vm.state.value.running)
        assertEquals("Root check", vm.state.value.lastCommandTitle)
        assertEquals("su unavailable", vm.state.value.error)
        vm.clearError()
        assertEquals(null, vm.state.value.error)
        vm.onCleared()
    }

    @Test
    fun unsupportedCommand_andClearHint_manageInteractionHint() {
        val vm = TrickViewModel(mockk(), mockk())
        val command = ShellCommand("Root command", "id", requiresRoot = true, unsupportedReason = "Root unavailable")

        vm.notifyUnsupportedCommand(command)
        assertEquals("Root unavailable", vm.state.value.interactionHint)
        vm.clearInteractionHint()

        assertEquals(null, vm.state.value.interactionHint)
        vm.onCleared()
    }
}
