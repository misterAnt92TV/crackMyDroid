@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.crackmydroid.shared.util

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSThread
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIModalPresentationFormSheet
import platform.UIKit.UIViewController
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fwrite

actual fun saveTextFile(baseName: String, content: String): Result<String> = runCatching {
    val path = exportFilePath(baseName)
    writeBytes(path, content.encodeToByteArray())
    path
}

internal fun savePdfFile(baseName: String, content: String): Result<String> = runCatching {
    val path = exportFilePath(baseName)
    writeBytes(path, TextPdfWriter.write(content))
    check(NSFileManager.defaultManager.fileExistsAtPath(path)) { "Impossibile creare il PDF" }
    path
}

actual fun shareTextFile(path: String): Result<Unit> = runCatching {
    check(NSThread.isMainThread) { "La condivisione deve essere avviata dal thread principale" }
    check(NSFileManager.defaultManager.fileExistsAtPath(path)) { "File non trovato" }
    val window = UIApplication.sharedApplication.keyWindow
        ?: error("Finestra iOS non disponibile per la condivisione")
    val presenter = window.rootViewController
        ?: error("Controller iOS non disponibile per la condivisione")
    val activityController = UIActivityViewController(
        activityItems = listOf(NSURL.fileURLWithPath(path)),
        applicationActivities = null,
    )
    activityController.modalPresentationStyle = UIModalPresentationFormSheet
    topViewController(presenter).presentViewController(activityController, true, null)
}

private fun exportFilePath(baseName: String): String {
    val directory = NSFileManager.defaultManager.URLsForDirectory(
        NSDocumentDirectory,
        NSUserDomainMask,
    ).firstOrNull() as? NSURL ?: error("Directory documenti iOS non disponibile")
    val fileName = if (baseName.contains('.')) baseName else "$baseName.txt"
    val path = directory.URLByAppendingPathComponent(fileName)?.path
    return path ?: error("Percorso file di export non valido")
}

private fun writeBytes(path: String, bytes: ByteArray) {
    val file = fopen(path, "wb") ?: error("Impossibile aprire il file di export")
    try {
        val written = if (bytes.isEmpty()) {
            0uL
        } else {
            bytes.usePinned { pinned -> fwrite(pinned.addressOf(0), 1u, bytes.size.toULong(), file) }
        }
        check(written == bytes.size.toULong()) { "Scrittura incompleta del file di export" }
    } finally {
        check(fclose(file) == 0) { "Impossibile chiudere il file di export" }
    }
}

private fun topViewController(root: UIViewController): UIViewController {
    var current = root
    while (true) {
        current = current.presentedViewController ?: return current
    }
}
