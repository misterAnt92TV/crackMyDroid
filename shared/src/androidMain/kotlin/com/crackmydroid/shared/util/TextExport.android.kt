package com.crackmydroid.shared.util

import android.annotation.SuppressLint
import android.content.Context
import java.io.File

actual fun saveTextFile(baseName: String, content: String): Result<String> = runCatching {
    // Keep the filename passed by the caller if it already contains an extension; otherwise default to .txt
    val dir = File(System.getProperty("java.io.tmpdir") ?: "/data/local/tmp")
    dir.mkdirs()
    val filename = if (baseName.contains('.')) baseName else "$baseName.txt"
    val file = File(dir, filename)
    file.writeText(content)
    file.absolutePath
}

@SuppressLint("PrivateApi")
actual fun shareTextFile(path: String): Result<Unit> = runCatching {
    val context: Context = try {
        val clazz = Class.forName("android.app.ActivityThread")
        val method = clazz.getMethod("currentApplication")
        method.invoke(null) as? Context
    } catch (e: Exception) {
        null
    } ?: throw IllegalStateException("Context non disponibile")

    val file = File(path)
    require(file.exists()) { "File non trovato" }

    val uri = androidx.core.content.FileProvider.getUriForFile(
        context,
        context.packageName + ".fileprovider",
        file
    )
    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = when (file.extension.lowercase()) {
            "json" -> "application/json"
            else -> "text/plain"
        }
        putExtra(android.content.Intent.EXTRA_STREAM, uri)
        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val chooser = android.content.Intent.createChooser(intent, null).apply {
        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(chooser)
}
