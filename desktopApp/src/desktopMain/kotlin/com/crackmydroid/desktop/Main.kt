package com.crackmydroid.desktop

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.crackmydroid.shared.di.initKoin
import java.awt.Dimension

fun main() = application {
    initKoin()
    val windowState = rememberWindowState(size = DpSize(1240.dp, 820.dp))
    Window(
        onCloseRequest = ::exitApplication,
        title = "CrackMyDroid Desktop",
        state = windowState,
    ) {
        window.minimumSize = Dimension(800, 600)
        DesktopCrackMyDroidApp()
    }
}
