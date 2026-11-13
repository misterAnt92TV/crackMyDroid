package com.crackmydroid.shared.ui.logs

import com.crackmydroid.architecture.presentation.model.UiEvent

sealed class LogEvent : UiEvent() {
    class Refresh : LogEvent()
    class FilterChanged(val value: String) : LogEvent()
    class Export : LogEvent()
    class ErrorDismissed : LogEvent()
    class LevelToggled(val level: Char) : LogEvent()
    class ColorByLevelChanged(val enabled: Boolean) : LogEvent()
}
