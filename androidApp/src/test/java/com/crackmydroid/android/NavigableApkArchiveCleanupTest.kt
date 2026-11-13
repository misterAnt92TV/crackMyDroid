package com.crackmydroid.android

import androidx.work.NetworkType
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.io.path.createTempDirectory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigableApkArchiveCleanupTest {
    @Test
    fun clean_removesOnlyArchivesOlderThanSevenDays_andDeletesEmptyArchiveDirectory() {
        val cacheDirectory = createTempDirectory("apk-archive-cleanup-").toFile()
        try {
            val now = 1_000_000_000L
            val cutoff = now - TimeUnit.DAYS.toMillis(7)
            val archiveDirectory = File(cacheDirectory, "navigable_apks").apply { mkdirs() }
            val expiredArchive = archiveDirectory.createFile("expired.zip", cutoff - 1)
            val exactBoundaryArchive = archiveDirectory.createFile("boundary.zip", cutoff)
            val recentArchive = archiveDirectory.createFile("recent.zip", cutoff + 1)

            val result = NavigableApkArchiveCleanup(cacheDirectory) { now }.clean()

            assertEquals(1, result.deletedArchives)
            assertFalse(expiredArchive.exists())
            assertTrue(exactBoundaryArchive.exists())
            assertTrue(recentArchive.exists())
            assertTrue(archiveDirectory.exists())
        } finally {
            cacheDirectory.deleteRecursively()
        }
    }

    @Test
    fun clean_deletesArchiveDirectoryWhenItsOnlyArchiveHasExpired() {
        val cacheDirectory = createTempDirectory("apk-archive-cleanup-").toFile()
        try {
            val now = 1_000_000_000L
            val archiveDirectory = File(cacheDirectory, "navigable_apks").apply { mkdirs() }
            archiveDirectory.createFile("expired.zip", now - TimeUnit.DAYS.toMillis(7) - 1)

            NavigableApkArchiveCleanup(cacheDirectory) { now }.clean()

            assertFalse(archiveDirectory.exists())
        } finally {
            cacheDirectory.deleteRecursively()
        }
    }

    @Test
    fun clean_removesOnlyExpiredNavigationWorkspaces() {
        val cacheDirectory = createTempDirectory("apk-archive-cleanup-").toFile()
        try {
            val now = 1_000_000_000L
            val cutoff = now - TimeUnit.DAYS.toMillis(7)
            val expiredWorkspace = File(cacheDirectory, "apk-navigation-expired").apply {
                mkdirs()
                createFile("archive.zip", cutoff - 1)
                setLastModified(cutoff - 1)
            }
            val recentWorkspace = File(cacheDirectory, "apk-navigation-recent").apply {
                mkdirs()
                createFile("archive.zip", cutoff + 1)
                setLastModified(cutoff + 1)
            }
            val unrelatedDirectory = File(cacheDirectory, "apk-content-expired").apply {
                mkdirs()
                setLastModified(cutoff - 1)
            }

            val result = NavigableApkArchiveCleanup(cacheDirectory) { now }.clean()

            assertEquals(1, result.deletedWorkspaces)
            assertFalse(expiredWorkspace.exists())
            assertTrue(recentWorkspace.exists())
            assertTrue(unrelatedDirectory.exists())
        } finally {
            cacheDirectory.deleteRecursively()
        }
    }

    @Test
    fun workRequests_areUniqueAndHaveNoRuntimeConstraints() {
        val immediate = NavigableApkArchiveCleanupWorkScheduler.immediateRequest()
        val periodic = NavigableApkArchiveCleanupWorkScheduler.periodicRequest()

        assertTrue(immediate.tags.contains(NavigableApkArchiveCleanupWorkScheduler.IMMEDIATE_WORK_NAME))
        assertFalse(immediate.workSpec.constraints.requiresCharging())
        assertEquals(NetworkType.NOT_REQUIRED, immediate.workSpec.constraints.requiredNetworkType)
        assertTrue(periodic.tags.contains(NavigableApkArchiveCleanupWorkScheduler.PERIODIC_WORK_NAME))
        assertEquals(TimeUnit.DAYS.toMillis(1), periodic.workSpec.intervalDuration)
    }

    @Test
    fun scheduleBootCompletedWork_enqueuesSnapshotAndArchiveCleanup() {
        var snapshotScheduled = false
        var archiveCleanupScheduled = false

        scheduleBootCompletedWork(
            enqueueSnapshot = { snapshotScheduled = true },
            enqueueArchiveCleanup = { archiveCleanupScheduled = true },
        )

        assertTrue(snapshotScheduled)
        assertTrue(archiveCleanupScheduled)
    }

    private fun File.createFile(name: String, modifiedAt: Long): File =
        File(this, name).apply {
            writeText("test")
            check(setLastModified(modifiedAt))
        }
}
