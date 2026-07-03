package com.crackmydroid.shared.util

import java.awt.Desktop
import java.io.File

actual fun saveTextFile(baseName: String, content: String): Result<String> = runCatching {
    val dir = DesktopPaths.exportsDir()
    val filename = if (baseName.contains('.')) baseName else "$baseName.txt"
    val file = File(dir, filename)
    file.writeText(content)
    file.absolutePath
}

actual fun shareTextFile(path: String): Result<Unit> = runCatching {
    revealInDesktop(File(path))
}

internal fun revealInDesktop(file: File) {
    require(file.exists()) { "File non trovato: ${file.absolutePath}" }
    val desktop = Desktop.getDesktop()
    val parent = file.parentFile ?: file
    runCatching {
        if (desktop.isSupported(Desktop.Action.BROWSE_FILE_DIR)) {
            desktop.browseFileDirectory(file)
        } else if (desktop.isSupported(Desktop.Action.OPEN)) {
            desktop.open(parent)
        } else {
            error("Operazione non supportata dal desktop corrente")
        }
    }.getOrElse {
        if (desktop.isSupported(Desktop.Action.OPEN)) {
            desktop.open(file)
        } else {
            throw it
        }
    }
}
