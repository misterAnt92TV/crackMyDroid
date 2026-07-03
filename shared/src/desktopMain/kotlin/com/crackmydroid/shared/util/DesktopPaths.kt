package com.crackmydroid.shared.util

import java.io.File

internal object DesktopPaths {
    private val baseDir: File by lazy {
        File(System.getProperty("user.home"), ".crackmydroid").apply { mkdirs() }
    }

    fun appDir(): File = baseDir

    fun exportsDir(): File = File(baseDir, "exports").apply { mkdirs() }

    fun snapshotsDir(): File = File(baseDir, "snapshots").apply { mkdirs() }

    fun dbFile(): File = File(baseDir, "cache.db")

    fun logsFile(): File = File(baseDir, "operation-log.txt")
}
