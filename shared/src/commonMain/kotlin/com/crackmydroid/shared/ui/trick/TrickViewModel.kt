package com.crackmydroid.shared.ui.trick

import com.crackmydroid.shared.domain.model.ShellCommand
import com.crackmydroid.shared.domain.model.ShellCommandResult
import com.crackmydroid.shared.domain.usecase.ExecuteShellCommandUseCase
import com.crackmydroid.shared.domain.usecase.ListShellCommandsUseCase
import com.crackmydroid.shared.presentation.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TrickViewModel(
    private val listUseCase: ListShellCommandsUseCase,
    private val execUseCase: ExecuteShellCommandUseCase
) : BaseViewModel() {
    private val _state = MutableStateFlow(TrickState())
    val state: StateFlow<TrickState> = _state

    fun load() {
        scope.launch {
            val list = listUseCase()
            _state.update { it.copy(commands = list) }
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
                error = null
            )
        }
        scope.launch {
            runCatching { execUseCase(command) }
                .onSuccess { res ->
                    _state.update {
                        it.copy(
                            lastResult = res,
                            lastCommandTitle = command.title,
                            running = false,
                            runningCommand = null
                        )
                    }
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(
                            error = err.message,
                            lastCommandTitle = command.title,
                            running = false,
                            runningCommand = null
                        )
                    }
                }
        }
    }

    fun notifyBusyInteraction(requestedCommand: ShellCommand) {
        val active = _state.value.runningCommand?.title ?: "un altro comando"
        _state.update {
            it.copy(interactionHint = "\"${requestedCommand.title}\" non avviato: prima termina \"$active\".")
        }
    }

    fun clearInteractionHint() {
        _state.update { it.copy(interactionHint = null) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
