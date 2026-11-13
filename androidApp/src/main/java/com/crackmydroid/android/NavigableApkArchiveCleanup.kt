package com.crackmydroid.android

import java.io.File
import java.util.concurrent.TimeUnit

internal class NavigableApkArchiveCleanup(
    private val cacheDirectory: File,
    private val nowMillis: () -> Long = System::currentTimeMillis,
) {
    fun clean(): NavigableApkArchiveCleanupResult {
        val expiration = nowMillis() - ARCHIVE_RETENTION_MILLIS
        val expiredArchives = cleanExpiredFiles(
            directory = File(cacheDirectory, NAVIGABLE_ARCHIVES_DIRECTORY),
            expiresBefore = expiration,
        )
        val expiredWorkspaces = cleanExpiredDirectories(
            expiresBefore = expiration,
        )
        return NavigableApkArchiveCleanupResult(expiredArchives, expiredWorkspaces)
    }

    private fun cleanExpiredFiles(directory: File, expiresBefore: Long): Int {
        if (!directory.isDirectory) return 0

        val deletedCount = directory.listFiles()
            .orEmpty()
            .filter {
                it.isFile &&
                    it.extension.equals("zip", ignoreCase = true) &&
                    it.lastModified() < expiresBefore
            }
            .count { it.delete() }

        if (directory.listFiles().orEmpty().isEmpty()) {
            directory.delete()
        }
        return deletedCount
    }

    private fun cleanExpiredDirectories(expiresBefore: Long): Int = cacheDirectory.listFiles()
        .orEmpty()
        .filter { it.isDirectory && it.name.startsWith(NAVIGATION_WORKSPACE_PREFIX) }
        .filter { it.lastModified() < expiresBefore }
        .count { it.deleteRecursively() }

    private companion object {
        const val NAVIGABLE_ARCHIVES_DIRECTORY = "navigable_apks"
        const val NAVIGATION_WORKSPACE_PREFIX = "apk-navigation-"
        val ARCHIVE_RETENTION_MILLIS = TimeUnit.DAYS.toMillis(7)
    }
}

internal data class NavigableApkArchiveCleanupResult(
    val deletedArchives: Int,
    val deletedWorkspaces: Int,
)
