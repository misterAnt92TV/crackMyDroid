package com.crackmydroid.shared.di

import com.crackmydroid.shared.domain.usecase.root.CheckPlayIntegrityUseCase
import com.crackmydroid.shared.domain.usecase.root.VerifyPlayIntegrityTokenUseCase
import com.crackmydroid.shared.domain.usecase.root.CheckRootUseCase
import com.crackmydroid.shared.domain.usecase.trick.ExecuteShellCommandUseCase
import com.crackmydroid.shared.domain.usecase.installedapps.ExportApkUseCase
import com.crackmydroid.shared.domain.usecase.log.ExportLogsUseCase
import com.crackmydroid.shared.domain.usecase.activities.GetActivitiesUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetActivityFavoritesUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetActivityPackageFavoritesUseCase
import com.crackmydroid.shared.domain.usecase.info.GetDeviceInfoUseCase
import com.crackmydroid.shared.domain.usecase.info.BuildDeviceInfoReportUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetExportFormatUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetFeatureHintsEnabledUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetHighContrastUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetInstalledAppFavoritesUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetIntroSeenUseCase
import com.crackmydroid.shared.domain.usecase.log.GetLogsUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetReduceMotionUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetSuggestionsEnabledUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetThemeUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetVerboseLogUseCase
import com.crackmydroid.shared.domain.usecase.activities.LaunchActivityUseCase
import com.crackmydroid.shared.domain.usecase.installedapps.ListInstalledAppsUseCase
import com.crackmydroid.shared.domain.usecase.permissions.ListPermissionsUseCase
import com.crackmydroid.shared.domain.usecase.trick.ListShellCommandsUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetActivityFavoritesUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetActivityPackageFavoritesUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetExportFormatUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetFeatureHintsEnabledUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetHighContrastUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetInstalledAppFavoritesUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetIntroSeenUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetLanguageUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetLargeTextUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetPermissionFavoritesUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetReduceMotionUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetSuggestionsEnabledUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetThemeUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetVerboseLogUseCase
import com.crackmydroid.shared.domain.usecase.installedapps.ShareApkUseCase
import com.crackmydroid.shared.domain.usecase.pentest.RunPenTestChecksUseCase
import com.crackmydroid.shared.domain.usecase.permissions.AnalyzePermissionRisksUseCase
import com.crackmydroid.shared.domain.usecase.permissions.BuildPermissionsReportUseCase
import com.crackmydroid.shared.domain.usecase.reports.ExportReportUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetLanguageUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetLargeTextUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetPermissionFavoritesUseCase
import com.crackmydroid.shared.presentation.AppDispatchers
import com.crackmydroid.shared.presentation.DefaultAppDispatchers
import com.crackmydroid.shared.ui.activities.ActivityListViewModel
import com.crackmydroid.shared.ui.info.InfoViewModel
import com.crackmydroid.shared.ui.AppPresentationDependencies
import com.crackmydroid.shared.ui.AppShellViewModel
import com.crackmydroid.shared.ui.home.HomeViewModel
import com.crackmydroid.shared.ui.installedapps.InstalledAppsViewModel
import com.crackmydroid.shared.ui.logs.LogViewModel
import com.crackmydroid.shared.ui.pentest.PenTestingViewModel
import com.crackmydroid.shared.ui.permissions.PermissionsViewModel
import com.crackmydroid.shared.ui.root.RootViewModel
import com.crackmydroid.shared.ui.settings.SettingsViewModel
import com.crackmydroid.shared.ui.trick.TrickViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

expect fun platformModule(): Module

fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
    modules(commonModule(), platformModule(), presentationModule())
}

fun commonModule() = module {
    factory { GetLanguageUseCase(get()) }
    factory { SetLanguageUseCase(get()) }
    factory { GetActivityFavoritesUseCase(get()) }
    factory { SetActivityFavoritesUseCase(get()) }
    factory { GetActivityPackageFavoritesUseCase(get()) }
    factory { SetActivityPackageFavoritesUseCase(get()) }
    factory { GetInstalledAppFavoritesUseCase(get()) }
    factory { SetInstalledAppFavoritesUseCase(get()) }
    factory { GetPermissionFavoritesUseCase(get()) }
    factory { SetPermissionFavoritesUseCase(get()) }
    factory { GetActivitiesUseCase(get()) }
    factory { LaunchActivityUseCase(get()) }
    factory { CheckRootUseCase(get()) }
    factory { CheckPlayIntegrityUseCase(get()) }
    factory { VerifyPlayIntegrityTokenUseCase(get()) }
    factory { GetDeviceInfoUseCase(get()) }
    factory { BuildDeviceInfoReportUseCase() }
    factory { ListShellCommandsUseCase(get()) }
    factory { ExecuteShellCommandUseCase(get()) }
    factory { GetThemeUseCase(get()) }
    factory { SetThemeUseCase(get()) }
    factory { GetVerboseLogUseCase(get()) }
    factory { SetVerboseLogUseCase(get()) }
    factory { GetExportFormatUseCase(get()) }
    factory { SetExportFormatUseCase(get()) }
    factory { GetHighContrastUseCase(get()) }
    factory { SetHighContrastUseCase(get()) }
    factory { GetReduceMotionUseCase(get()) }
    factory { SetReduceMotionUseCase(get()) }
    factory { GetLargeTextUseCase(get()) }
    factory { SetLargeTextUseCase(get()) }
    factory { GetSuggestionsEnabledUseCase(get()) }
    factory { SetSuggestionsEnabledUseCase(get()) }
    factory { GetFeatureHintsEnabledUseCase(get()) }
    factory { SetFeatureHintsEnabledUseCase(get()) }
    factory { GetLogsUseCase(get()) }
    factory { ExportLogsUseCase(get()) }
    factory { GetIntroSeenUseCase(get()) }
    factory { SetIntroSeenUseCase(get()) }
    factory { ListPermissionsUseCase(get()) }
    factory { AnalyzePermissionRisksUseCase() }
    factory { BuildPermissionsReportUseCase(get()) }
    factory { RunPenTestChecksUseCase(get()) }
    factory { ExportReportUseCase(get()) }
    factory { ListInstalledAppsUseCase(get()) }
    factory { ExportApkUseCase(get()) }
    factory { ShareApkUseCase(get()) }
}

fun presentationModule() = module {
    single<AppDispatchers> { DefaultAppDispatchers }
    factory { ActivityListViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { HomeViewModel(get()) }
    factory {
        val activity = get<ActivityListViewModel>()
        val info = get<InfoViewModel>()
        val trick = get<TrickViewModel>()
        val settings = get<SettingsViewModel>()
        val logs = get<LogViewModel>()
        val permissions = get<PermissionsViewModel>()
        val installedApps = get<InstalledAppsViewModel>()
        val penTesting = get<PenTestingViewModel>()
        AppPresentationDependencies(
            shell = AppShellViewModel(
                getIntroSeen = get(),
                setIntroSeen = get(),
                activity = activity,
                info = info,
                trick = trick,
                settings = settings,
                logs = logs,
                permissions = permissions,
                installedApps = installedApps,
                penTesting = penTesting
            ),
            activity = activity,
            home = get(),
            root = get(),
            info = info,
            trick = trick,
            settings = settings,
            logs = logs,
            permissions = permissions,
            installedApps = installedApps,
            penTesting = penTesting
        )
    }
    factory { RootViewModel(get(), get(), get()) }
    factory { InfoViewModel(get(), get(), get(), get()) }
    factory { TrickViewModel(get(), get()) }
    factory {
        SettingsViewModel(
            get(), get(), // theme
            get(), get(), // verbose
            get(), get(), // export format
            get(), get(), // high contrast
            get(), get(), // reduce motion
            get(), get(), // large text
            get(), get(), // suggestions enabled
            get(), get(), // feature hints enabled
            get(), get()  // intro seen
            , get(), get() // language
        )
    }
    factory { LogViewModel(get(), get(), get()) }
    factory { PermissionsViewModel(get(), get(), get(), get(), get(), get()) }
    factory { InstalledAppsViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { PenTestingViewModel(get(), get(), get(), get(), get(), get()) }
}
