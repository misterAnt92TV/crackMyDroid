package com.crackmydroid.android

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.crackmydroid.architecture.logging.AppLogger
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException

internal object NavigableApkArchiveCleanupWorkScheduler {
    internal const val IMMEDIATE_WORK_NAME = "navigable-apk-archive-cleanup-immediate"
    internal const val PERIODIC_WORK_NAME = "navigable-apk-archive-cleanup-periodic"

    fun schedule(context: Context) {
        enqueueImmediate(context)
        enqueuePeriodic(context)
    }

    fun enqueueImmediate(context: Context) {
        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            IMMEDIATE_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            immediateRequest(),
        )
    }

    private fun enqueuePeriodic(context: Context) {
        WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicRequest(),
        )
    }

    internal fun immediateRequest(): OneTimeWorkRequest =
        OneTimeWorkRequestBuilder<NavigableApkArchiveCleanupWorker>()
            .addTag(IMMEDIATE_WORK_NAME)
            .build()

    internal fun periodicRequest(): PeriodicWorkRequest =
        PeriodicWorkRequestBuilder<NavigableApkArchiveCleanupWorker>(1, TimeUnit.DAYS)
            .addTag(PERIODIC_WORK_NAME)
            .build()
}

class NavigableApkArchiveCleanupWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result = try {
        NavigableApkArchiveCleanup(applicationContext.cacheDir).clean()
        Result.success()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (error: Throwable) {
        AppLogger.logger.w(error) { "Pulizia degli archivi APK navigabili rinviata" }
        Result.retry()
    }
}
