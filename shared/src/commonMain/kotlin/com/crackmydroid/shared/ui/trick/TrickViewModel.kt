package com.crackmydroid.shared.ui.trick

import com.crackmydroid.architecture.coroutines.AppDispatchers
import com.crackmydroid.shared.domain.model.shell.ShellCommand
import com.crackmydroid.shared.domain.usecase.trick.ExecuteShellCommandUseCase
import com.crackmydroid.shared.domain.usecase.trick.ListShellCommandsUseCase
import com.crackmydroid.shared.i18n.ScreenUiStrings
import com.crackmydroid.shared.i18n.format
import com.crackmydroid.shared.presentation.LocalizedBaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class TrickViewModel(
    private val listUseCase: ListShellCommandsUseCase,
    private val execUseCase: ExecuteShellCommandUseCase,
    dispatchers: AppDispatchers,
) : LocalizedBaseViewModel<TrickEvent>(dispatchers) {
    private val _state = MutableStateFlow(TrickState())
    val state: StateFlow<TrickState> = _state
    private val uiStrings get() = ScreenUiStrings.from(strings)

    override fun onEvent(event: TrickEvent) = when (event) {
        is TrickEvent.Load -> load()
        is TrickEvent.Execute -> execute(event.command)
        is TrickEvent.ExecutionRequested -> requestExecution(event.command)
        is TrickEvent.ExecutionConfirmed -> confirmPendingExecution()
        is TrickEvent.PendingCommandDismissed -> dismissPendingCommand()
        is TrickEvent.ResultDismissed -> dismissResult()
        is TrickEvent.BusyInteractionNotified -> notifyBusyInteraction(event.command)
        is TrickEvent.UnsupportedCommandNotified -> notifyUnsupportedCommand(event.command)
        is TrickEvent.InteractionHintDismissed -> clearInteractionHint()
        is TrickEvent.ErrorDismissed -> clearError()
    }

    private fun load() {
        launchLatest("trick-load") {
            listUseCase()
                .onSuccess { list -> _state.update { it.copy(commands = list) } }
                .onFailure { error -> _state.update { it.copy(error = error.message) } }
        }
    }

    private fun execute(command: ShellCommand) {
        if (_state.value.running) {
            val active = _state.value.runningCommand?.title ?: uiStrings.anotherCommand
            _state.update {
                it.copy(interactionHint = uiStrings.waitCommandRunning.format(active))
            }
            return
        }
        _state.update {
            it.copy(
                running = true,
                runningCommand = command,
                interactionHint = null,
                error = null,
            )
        }
        launchTask {
            execUseCase(command)
                .onSuccess { res ->
                    _state.update {
                        it.copy(
                            lastResult = res,
                            resultToDisplay = res,
                            lastCommandTitle = command.title,
                            stdoutLines = res.stdout.lines().map(String::trim).filter(String::isNotEmpty),
                            stderrLines = res.stderr.lines().map(String::trim).filter(String::isNotEmpty),
                            packageLines = res.stdout.lines().map(String::trim)
                                .filter { line -> line.startsWith("package:") }
                                .map { line -> line.removePrefix("package:").trim() },
                            stdoutIsPackageList = res.stdout.lines().map(String::trim)
                                .filter(String::isNotEmpty).all { line -> line.startsWith("package:") },
                            running = false,
                            runningCommand = null,
                        )
                    }
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(
                            error = err.message,
                            lastCommandTitle = command.title,
                            running = false,
                            runningCommand = null,
                        )
                    }
                }
        }
    }

    private fun requestExecution(command: ShellCommand) {
        if (command.confirmation != null) {
            _state.update { it.copy(pendingCommand = command) }
        } else {
            execute(command)
        }
    }

    private fun confirmPendingExecution() {
        val command = _state.value.pendingCommand ?: return
        _state.update { it.copy(pendingCommand = null) }
        execute(command)
    }

    private fun dismissPendingCommand() = _state.update { it.copy(pendingCommand = null) }
    private fun dismissResult() = _state.update { it.copy(resultToDisplay = null) }

    private fun notifyBusyInteraction(requestedCommand: ShellCommand) {
        val active = _state.value.runningCommand?.title ?: uiStrings.anotherCommand
        _state.update {
            it.copy(interactionHint = uiStrings.commandNotStartedUntilCurrentEnds.format(requestedCommand.title, active))
        }
    }

    private fun notifyUnsupportedCommand(command: ShellCommand) {
        _state.update {
            it.copy(
                interactionHint = command.unsupportedReason
                    ?: uiStrings.commandNotAvailableOnSelectedDevice.format(command.title),
            )
        }
    }

    private fun clearInteractionHint() {
        _state.update { it.copy(interactionHint = null) }
    }

    private fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
