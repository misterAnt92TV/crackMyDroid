package com.crackmydroid.android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        scheduleBootCompletedWork(
            enqueueSnapshot = { SnapshotWorkScheduler.enqueue(context) },
            enqueueArchiveCleanup = {
                NavigableApkArchiveCleanupWorkScheduler.enqueueImmediate(context)
            },
        )
    }
}

internal fun scheduleBootCompletedWork(
    enqueueSnapshot: () -> Unit,
    enqueueArchiveCleanup: () -> Unit,
) {
    enqueueSnapshot()
    enqueueArchiveCleanup()
}
