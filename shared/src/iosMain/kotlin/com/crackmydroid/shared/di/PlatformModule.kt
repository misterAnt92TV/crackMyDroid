package com.crackmydroid.shared.di

import com.crackmydroid.shared.data.ActivityRepositoryIos
import com.crackmydroid.shared.data.ApkContentRepositoryIos
import com.crackmydroid.shared.data.CacheMaintenanceRepositoryIos
import com.crackmydroid.shared.data.DeviceInfoRepositoryIos
import com.crackmydroid.shared.data.InstalledAppsRepositoryIos
import com.crackmydroid.shared.data.IntegrityVerificationRepositoryIos
import com.crackmydroid.shared.data.LogRepositoryIos
import com.crackmydroid.shared.data.PermissionsRepositoryIos
import com.crackmydroid.shared.data.RootCheckRepositoryIos
import com.crackmydroid.shared.data.SettingsRepositoryIos
import com.crackmydroid.shared.data.ShellCommandRepositoryIos
import com.crackmydroid.shared.data.network.createIosHttpClient
import com.crackmydroid.shared.data.reports.ReportFileRepositoryIos
import com.crackmydroid.shared.domain.repository.ActivityRepository
import com.crackmydroid.shared.domain.repository.ApkContentRepository
import com.crackmydroid.shared.domain.repository.CacheMaintenance
import com.crackmydroid.shared.domain.repository.DeviceInfoRepository
import com.crackmydroid.shared.domain.repository.InstalledAppsRepository
import com.crackmydroid.shared.domain.repository.IntegrityVerificationRepository
import com.crackmydroid.shared.domain.repository.PermissionsRepository
import com.crackmydroid.shared.domain.repository.ReportFileRepository
import com.crackmydroid.shared.domain.repository.RootCheckRepository
import com.crackmydroid.shared.domain.repository.SettingsRepository
import com.crackmydroid.shared.domain.repository.ShellCommandRepository
import com.crackmydroid.shared.domain.repository.log.LogRepository
import com.crackmydroid.shared.domain.repository.log.OperationLogRepository
import com.crackmydroid.shared.presentation.operations.DefaultOperationLogRepository
import com.crackmydroid.shared.presentation.operations.InMemoryOperationLogPersistence
import com.crackmydroid.shared.presentation.operations.OperationLogPersistence
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<HttpClient> { createIosHttpClient() }
    single<ReportFileRepository> { ReportFileRepositoryIos() }
    single<OperationLogPersistence> { InMemoryOperationLogPersistence() }
    single<OperationLogRepository> { DefaultOperationLogRepository(get()) }
    single<ActivityRepository> { ActivityRepositoryIos() }
    single<RootCheckRepository> { RootCheckRepositoryIos() }
    single<IntegrityVerificationRepository> { IntegrityVerificationRepositoryIos() }
    single<DeviceInfoRepository> { DeviceInfoRepositoryIos() }
    single<ShellCommandRepository> { ShellCommandRepositoryIos() }
    single<SettingsRepository> { SettingsRepositoryIos() }
    single<LogRepository> { LogRepositoryIos() }
    single<PermissionsRepository> { PermissionsRepositoryIos() }
    single<InstalledAppsRepository> { InstalledAppsRepositoryIos() }
    single<ApkContentRepository> { ApkContentRepositoryIos() }
    single<CacheMaintenance> { CacheMaintenanceRepositoryIos() }
}
