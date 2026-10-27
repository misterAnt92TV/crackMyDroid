package com.crackmydroid.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.crackmydroid.shared.di.initKoin

fun main() = application {
    initKoin()
    Window(
        onCloseRequest = ::exitApplication,
        title = "CrackMyDroid Desktop"
    ) {
        DesktopCrackMyDroidApp()
    }
}
