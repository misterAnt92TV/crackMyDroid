package com.crackmydroid.android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.crackmydroid.shared.AppLogger
import com.crackmydroid.shared.data.snapshot.AndroidDeviceSnapshotCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                GlobalContext.get().get<AndroidDeviceSnapshotCoordinator>().runSnapshotIfIdle()
            } catch (error: Throwable) {
                AppLogger.logger.e(error) { "Snapshot Android non avviato da BOOT_COMPLETED" }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
