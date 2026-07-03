package com.crackmydroid.android

import android.app.Application
import com.crackmydroid.shared.data.snapshot.AndroidDeviceSnapshotCoordinator
import com.crackmydroid.shared.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext

class CrackMyDroidApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@CrackMyDroidApp)
        }
        GlobalContext.get().get<AndroidDeviceSnapshotCoordinator>().start()
    }
}
