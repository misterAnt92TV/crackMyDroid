package com.crackmydroid.android

import android.app.Application
import com.crackmydroid.shared.di.initKoin
import org.koin.android.ext.koin.androidContext

class CrackMyDroidApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@CrackMyDroidApp)
        }
    }
}
