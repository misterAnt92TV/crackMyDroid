package com.crackmydroid.shared.util

import java.io.File

internal object DesktopPaths {
    private val baseDir: File by lazy {
        File(System.getProperty("user.home"), ".crackmydroid").apply { mkdirs() }
    }

    fun exportsDir(): File = File(baseDir, "exports").apply { mkdirs() }

    fun snapshotsDir(): File = File(baseDir, "snapshots").apply { mkdirs() }

    fun logsFile(): File = File(baseDir, "operation-log.txt")
}
