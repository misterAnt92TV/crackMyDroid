package com.crackmydroid.shared.ui.trick

import com.crackmydroid.architecture.coroutines.AppDispatchers
import com.crackmydroid.architecture.presentation.model.UiLoadState
import com.crackmydroid.shared.domain.model.shell.ShellCommand
import com.crackmydroid.shared.domain.usecase.trick.ExecuteShellCommandUseCase
import com.crackmydroid.shared.domain.usecase.trick.ListShellCommandsUseCase
import com.crackmydroid.shared.i18n.ScreenUiStrings
import com.crackmydroid.shared.i18n.format
import com.crackmydroid.shared.presentation.LocalizedBaseViewModel

class TrickViewModel(
    private val listShellCommandsUseCase: ListShellCommandsUseCase,
    private val executeShellCommandUseCase: ExecuteShellCommandUseCase,
    dispatchers: AppDispatchers,
) : LocalizedBaseViewModel<TrickEvent, TrickState>(TrickState(), dispatchers) {
    private companion object {
        const val LOAD_KEY = "trick-load"
    }

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
        launchLatest(LOAD_KEY) {
            updateState { it.copy(commandsLoadState = UiLoadState.Loading) }
            listShellCommandsUseCase()
                .onSuccess { list -> updateState { it.copy(commands = list, commandsLoadState = UiLoadState.Success) } }
                .onFailure { error ->
                    updateState { it.copy(commandsLoadState = UiLoadState.Error(error.message ?: presentationTexts.error)) }
                }
        }
    }

    private fun execute(command: ShellCommand) {
        if (currentState.running) {
            val active = currentState.runningCommand?.title ?: uiStrings.anotherCommand
            updateState {
                it.copy(interactionHint = uiStrings.waitCommandRunning.format(active))
            }
            return
        }
        updateState {
            it.copy(
                executionLoadState = UiLoadState.Loading,
                runningCommand = command,
                interactionHint = null,
            )
        }
        launchTask {
            executeShellCommandUseCase(command)
                .onSuccess { res ->
                    updateState {
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
                            executionLoadState = UiLoadState.Success,
                            runningCommand = null,
                        )
                    }
                }
                .onFailure { err ->
                    updateState {
                        it.copy(
                            executionLoadState = UiLoadState.Error(err.message ?: presentationTexts.error),
                            lastCommandTitle = command.title,
                            runningCommand = null,
                        )
                    }
                }
        }
    }

    private fun requestExecution(command: ShellCommand) {
        if (command.confirmation != null) {
            updateState { it.copy(pendingCommand = command) }
        } else {
            execute(command)
        }
    }

    private fun confirmPendingExecution() {
        val command = currentState.pendingCommand ?: return
        updateState { it.copy(pendingCommand = null) }
        execute(command)
    }

    private fun dismissPendingCommand() = updateState { it.copy(pendingCommand = null) }
    private fun dismissResult() = updateState { it.copy(resultToDisplay = null) }

    private fun notifyBusyInteraction(requestedCommand: ShellCommand) {
        val active = currentState.runningCommand?.title ?: uiStrings.anotherCommand
        updateState {
            it.copy(
                interactionHint = uiStrings.commandNotStartedUntilCurrentEnds.format(requestedCommand.title, active),
            )
        }
    }

    private fun notifyUnsupportedCommand(command: ShellCommand) {
        updateState {
            it.copy(
                interactionHint = command.unsupportedReason
                    ?: uiStrings.commandNotAvailableOnSelectedDevice.format(command.title),
            )
        }
    }

    private fun clearInteractionHint() {
        updateState { it.copy(interactionHint = null) }
    }

    private fun clearError() {
        updateState {
            it.copy(
                commandsLoadState = if (it.commandsLoadState is UiLoadState.Error) UiLoadState.Idle else it.commandsLoadState,
                executionLoadState = if (it.executionLoadState is UiLoadState.Error) UiLoadState.Idle else it.executionLoadState,
            )
        }
    }
}
