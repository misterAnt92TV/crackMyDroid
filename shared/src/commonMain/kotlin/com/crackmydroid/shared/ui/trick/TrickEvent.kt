package com.crackmydroid.shared.ui.trick

import com.crackmydroid.architecture.presentation.model.UiEvent
import com.crackmydroid.shared.domain.model.shell.ShellCommand

sealed class TrickEvent : UiEvent() {
    class Load : TrickEvent()
    class ExecutionRequested(val command: ShellCommand) : TrickEvent()
    class ExecutionConfirmed : TrickEvent()
    class PendingCommandDismissed : TrickEvent()
    class ResultDismissed : TrickEvent()
    class BusyInteractionNotified(val command: ShellCommand) : TrickEvent()
    class UnsupportedCommandNotified(val command: ShellCommand) : TrickEvent()
    class InteractionHintDismissed : TrickEvent()
    class ErrorDismissed : TrickEvent()
}
