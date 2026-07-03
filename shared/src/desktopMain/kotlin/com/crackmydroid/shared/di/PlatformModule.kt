package com.crackmydroid.shared.di

import com.crackmydroid.shared.data.activity.ActivityRepositoryDesktop
import com.crackmydroid.shared.data.DesktopScanCache
import com.crackmydroid.shared.data.adb.ProcessAdbBridge
import com.crackmydroid.shared.data.apps.InstalledAppsRepositoryDesktop
import com.crackmydroid.shared.data.device.DeviceInfoRepositoryDesktop
import com.crackmydroid.shared.data.device.DeviceSessionControllerDesktop
import com.crackmydroid.shared.data.log.LogRepositoryDesktop
import com.crackmydroid.shared.data.packages.RemotePackageManagerDesktop
import com.crackmydroid.shared.data.permissions.PermissionsRepositoryDesktop
import com.crackmydroid.shared.data.root.RootCheckRepositoryDesktop
import com.crackmydroid.shared.data.settings.SettingsRepositoryDesktop
import com.crackmydroid.shared.data.shell.ShellCommandRepositoryDesktop
import com.crackmydroid.shared.data.snapshot.RemoteDeviceSnapshotImporter
import com.crackmydroid.shared.domain.repository.ActivityRepository
import com.crackmydroid.shared.domain.repository.AdbBridge
import com.crackmydroid.shared.domain.repository.DeviceInfoRepository
import com.crackmydroid.shared.domain.repository.DeviceSessionController
import com.crackmydroid.shared.domain.repository.InstalledAppsRepository
import com.crackmydroid.shared.domain.repository.LogRepository
import com.crackmydroid.shared.domain.repository.PermissionsRepository
import com.crackmydroid.shared.domain.repository.RemotePackageManager
import com.crackmydroid.shared.domain.repository.RootCheckRepository
import com.crackmydroid.shared.domain.repository.SettingsRepository
import com.crackmydroid.shared.domain.repository.ShellCommandRepository
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single { DesktopScanCache() }
    single<SettingsRepository> { SettingsRepositoryDesktop() }
    single<AdbBridge> { ProcessAdbBridge() }
    single { RemoteDeviceSnapshotImporter(get(), get()) }
    single<DeviceSessionController> { DeviceSessionControllerDesktop(get(), get(), get(), get()) }
    single<RemotePackageManager> { RemotePackageManagerDesktop(get(), get(), get()) }
    single<ActivityRepository> { ActivityRepositoryDesktop(get(), get(), get(), get()) }
    single<RootCheckRepository> { RootCheckRepositoryDesktop(get(), get(), get()) }
    single<DeviceInfoRepository> { DeviceInfoRepositoryDesktop(get(), get(), get()) }
    single<ShellCommandRepository> { ShellCommandRepositoryDesktop(get(), get(), get()) }
    single<LogRepository> { LogRepositoryDesktop(get(), get(), get()) }
    single<PermissionsRepository> { PermissionsRepositoryDesktop(get(), get(), get(), get()) }
    single<InstalledAppsRepository> { InstalledAppsRepositoryDesktop(get(), get(), get()) }
}
