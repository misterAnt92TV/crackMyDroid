package com.crackmydroid.android

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.crackmydroid.shared.AppLogger
import com.crackmydroid.shared.data.snapshot.AndroidDeviceSnapshotCoordinator
import kotlinx.coroutines.CancellationException
import org.koin.core.context.GlobalContext
import java.io.IOException
import java.util.concurrent.TimeUnit

internal object SnapshotWorkScheduler {
    private const val UNIQUE_WORK_NAME = "android-device-snapshot"

    fun enqueue(context: Context) {
        val request = OneTimeWorkRequestBuilder<AndroidSnapshotWorker>()
            .setConstraints(constraints())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .addTag(UNIQUE_WORK_NAME)
            .build()

        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            UNIQUE_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request,
        )
    }

    internal fun constraints(): Constraints = Constraints.Builder()
        .setRequiresCharging(true)
        .setRequiresBatteryNotLow(true)
        .build()
}

class AndroidSnapshotWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result = when (
        executeSnapshot {
            GlobalContext.get().get<AndroidDeviceSnapshotCoordinator>().runSnapshotIfIdle()
        }
    ) {
        SnapshotWorkResult.Success -> Result.success()
        SnapshotWorkResult.Retry -> Result.retry()
        SnapshotWorkResult.Failure -> Result.failure()
    }
}

internal enum class SnapshotWorkResult {
    Success,
    Retry,
    Failure
}

internal suspend fun executeSnapshot(snapshot: suspend () -> Boolean): SnapshotWorkResult =
    try {
        snapshot()
        SnapshotWorkResult.Success
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (error: IOException) {
        AppLogger.logger.w(error) { "Snapshot Android rinviato dopo errore I/O" }
        SnapshotWorkResult.Retry
    } catch (error: Throwable) {
        AppLogger.logger.e(error) { "Snapshot Android non eseguito dal worker" }
        SnapshotWorkResult.Failure
    }
