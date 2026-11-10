package com.crackmydroid.shared.ui.trick

import com.crackmydroid.shared.domain.model.shell.ShellCommand
import com.crackmydroid.shared.domain.usecase.trick.ExecuteShellCommandUseCase
import com.crackmydroid.shared.domain.usecase.trick.ListShellCommandsUseCase
import com.crackmydroid.shared.presentation.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class TrickViewModel(
    private val listUseCase: ListShellCommandsUseCase,
    private val execUseCase: ExecuteShellCommandUseCase,
    dispatchers: com.crackmydroid.shared.util.AppDispatchers,
) : BaseViewModel(dispatchers) {
    private val _state = MutableStateFlow(TrickState())
    val state: StateFlow<TrickState> = _state

    fun load() {
        launchLatest("trick-load") {
            listUseCase()
                .onSuccess { list -> _state.update { it.copy(commands = list) } }
                .onFailure { error -> _state.update { it.copy(error = error.message) } }
        }
    }

    fun execute(command: ShellCommand) {
        if (_state.value.running) {
            val active = _state.value.runningCommand?.title ?: "un altro comando"
            _state.update {
                it.copy(interactionHint = "Attendi: \"$active\" e ancora in esecuzione.")
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

    fun requestExecution(command: ShellCommand) {
        if (command.confirmation != null) _state.update { it.copy(pendingCommand = command) }
        else execute(command)
    }

    fun confirmPendingExecution() {
        val command = _state.value.pendingCommand ?: return
        _state.update { it.copy(pendingCommand = null) }
        execute(command)
    }

    fun dismissPendingCommand() = _state.update { it.copy(pendingCommand = null) }
    fun dismissResult() = _state.update { it.copy(resultToDisplay = null) }

    fun notifyBusyInteraction(requestedCommand: ShellCommand) {
        val active = _state.value.runningCommand?.title ?: "un altro comando"
        _state.update {
            it.copy(interactionHint = "\"${requestedCommand.title}\" non avviato: prima termina \"$active\".")
        }
    }

    fun notifyUnsupportedCommand(command: ShellCommand) {
        _state.update {
            it.copy(
                interactionHint = command.unsupportedReason
                    ?: "\"${command.title}\" non disponibile sul device selezionato.",
            )
        }
    }

    fun clearInteractionHint() {
        _state.update { it.copy(interactionHint = null) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
