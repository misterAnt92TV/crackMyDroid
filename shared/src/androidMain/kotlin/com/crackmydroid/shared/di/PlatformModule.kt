package com.crackmydroid.shared.di

import com.crackmydroid.shared.data.activity.ActivityRepositoryAndroid
import com.crackmydroid.shared.data.cache.CacheMaintenanceRepositoryAndroid
import com.crackmydroid.shared.data.cache.PackageChangeReceiver
import com.crackmydroid.shared.data.db.DatabaseDriverFactory
import com.crackmydroid.shared.data.device.DeviceInfoRepositoryAndroid
import com.crackmydroid.shared.data.root.RootCheckRepositoryAndroid
import com.crackmydroid.shared.data.settings.SettingsRepositoryAndroid
import com.crackmydroid.shared.data.shell.ShellCommandRepositoryAndroid
import com.crackmydroid.shared.data.log.LogRepositoryAndroid
import com.crackmydroid.shared.data.permissions.PermissionsRepositoryAndroid
import com.crackmydroid.shared.data.apps.InstalledAppsRepositoryAndroid
import com.crackmydroid.shared.data.snapshot.AndroidDeviceSnapshotCoordinator
import com.crackmydroid.shared.data.reports.ReportFileRepositoryAndroid
import com.crackmydroid.shared.presentation.operations.DefaultOperationLogRepository
import com.crackmydroid.shared.presentation.operations.OperationLogPersistence
import com.crackmydroid.shared.presentation.operations.OperationLogPersistenceAndroid
import com.crackmydroid.database.CacheDatabase
import com.crackmydroid.shared.domain.repository.ActivityRepository
import com.crackmydroid.shared.domain.repository.CacheMaintenance
import com.crackmydroid.shared.domain.repository.DeviceInfoRepository
import com.crackmydroid.shared.domain.repository.RootCheckRepository
import com.crackmydroid.shared.domain.repository.SettingsRepository
import com.crackmydroid.shared.domain.repository.ShellCommandRepository
import com.crackmydroid.shared.domain.repository.LogRepository
import com.crackmydroid.shared.domain.repository.PermissionsRepository
import com.crackmydroid.shared.domain.repository.InstalledAppsRepository
import com.crackmydroid.shared.domain.repository.ReportFileRepository
import com.crackmydroid.shared.domain.repository.OperationLogRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single { DatabaseDriverFactory(androidContext()) }
    single { CacheDatabase(get<DatabaseDriverFactory>().createDriver()) }

    single<ActivityRepository> { ActivityRepositoryAndroid(androidContext(), get()) }
    single<RootCheckRepository> { RootCheckRepositoryAndroid(androidContext()) }
    single<DeviceInfoRepository> { DeviceInfoRepositoryAndroid(androidContext(), get()) }
    single<ShellCommandRepository> { ShellCommandRepositoryAndroid() }
    single<SettingsRepository> { SettingsRepositoryAndroid(androidContext()) }
    single<LogRepository> { LogRepositoryAndroid(androidContext()) }
    single<PermissionsRepository> { PermissionsRepositoryAndroid(androidContext(), get()) }
    single<InstalledAppsRepository> { InstalledAppsRepositoryAndroid(androidContext(), get()) }
    single<ReportFileRepository> { ReportFileRepositoryAndroid() }
    single<OperationLogPersistence> { OperationLogPersistenceAndroid(androidContext()) }
    single<OperationLogRepository> { DefaultOperationLogRepository(get()) }
    single<CacheMaintenance> { CacheMaintenanceRepositoryAndroid(get()) }
    single { AndroidDeviceSnapshotCoordinator(androidContext(), get(), get(), get(), get(), get(), get(), get()) }
    
    // Register PackageChangeReceiver to invalidate caches on package install/uninstall
    single {
        PackageChangeReceiver.registerReceiver(androidContext(), get<CacheMaintenance>())
    }
}
