package com.crackmydroid.shared.di

import com.crackmydroid.shared.data.ActivityRepositoryIos
import com.crackmydroid.shared.data.DeviceInfoRepositoryIos
import com.crackmydroid.shared.data.RootCheckRepositoryIos
import com.crackmydroid.shared.data.SettingsRepositoryIos
import com.crackmydroid.shared.data.ShellCommandRepositoryIos
import com.crackmydroid.shared.data.LogRepositoryIos
import com.crackmydroid.shared.data.PermissionsRepositoryIos
import com.crackmydroid.shared.data.InstalledAppsRepositoryIos
import com.crackmydroid.shared.domain.repository.ActivityRepository
import com.crackmydroid.shared.domain.repository.DeviceInfoRepository
import com.crackmydroid.shared.domain.repository.LogRepository
import com.crackmydroid.shared.domain.repository.InstalledAppsRepository
import com.crackmydroid.shared.domain.repository.RootCheckRepository
import com.crackmydroid.shared.domain.repository.SettingsRepository
import com.crackmydroid.shared.domain.repository.ShellCommandRepository
import com.crackmydroid.shared.domain.repository.PermissionsRepository
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<ActivityRepository> { ActivityRepositoryIos() }
    single<RootCheckRepository> { RootCheckRepositoryIos() }
    single<DeviceInfoRepository> { DeviceInfoRepositoryIos() }
    single<ShellCommandRepository> { ShellCommandRepositoryIos() }
    single<SettingsRepository> { SettingsRepositoryIos() }
    single<LogRepository> { LogRepositoryIos() }
    single<PermissionsRepository> { PermissionsRepositoryIos() }
    single<InstalledAppsRepository> { InstalledAppsRepositoryIos() }
}
