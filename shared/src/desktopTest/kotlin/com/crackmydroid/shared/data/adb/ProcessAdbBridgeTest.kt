package com.crackmydroid.shared.data.adb

import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class ProcessAdbBridgeTest {
    @Test
    fun rejectsConfiguredDirectoryAsAdbBinary() = runTest {
        val directory = createTempDirectory("adb-test-").toFile()

        try {
            val result = ProcessAdbBridge().listDevices(directory.absolutePath)

            assertTrue(result.isFailure)
            assertFalse(result.exceptionOrNull()?.message.isNullOrBlank())
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun rejectsConfiguredNonExecutableFileAsAdbBinary() = runTest {
        val file = createTempDirectory("adb-test-").resolve("adb").toFile()
        file.writeText("not an executable")
        file.setExecutable(false)

        try {
            val result = ProcessAdbBridge().listDevices(file.absolutePath)

            assertTrue(result.isFailure)
            assertFalse(result.exceptionOrNull()?.message.isNullOrBlank())
        } finally {
            file.parentFile?.deleteRecursively()
        }
    }
}
