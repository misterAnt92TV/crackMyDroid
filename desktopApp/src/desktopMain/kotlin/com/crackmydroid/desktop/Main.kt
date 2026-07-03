package com.crackmydroid.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.crackmydroid.shared.di.initKoin
import com.crackmydroid.shared.presentation.operations.OperationLogStore
import com.crackmydroid.shared.presentation.operations.init

fun main() = application {
    initKoin()
    OperationLogStore.init()

    Window(
        onCloseRequest = ::exitApplication,
        title = "CrackMyDroid Desktop"
    ) {
        DesktopCrackMyDroidApp()
    }
}
