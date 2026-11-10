package com.crackmydroid.shared.di

import co.touchlab.kermit.Logger
import com.crackmydroid.architecture.coroutines.AppDispatchers
import com.crackmydroid.architecture.coroutines.DefaultAppDispatchers
import com.crackmydroid.architecture.logging.AppLogger
import com.crackmydroid.shared.domain.usecase.activities.GetActivitiesUseCase
import com.crackmydroid.shared.domain.usecase.activities.LaunchActivityUseCase
import com.crackmydroid.shared.domain.usecase.info.BuildDeviceInfoReportUseCase
import com.crackmydroid.shared.domain.usecase.info.GetDeviceInfoUseCase
import com.crackmydroid.shared.domain.usecase.installedapps.ExportApkUseCase
import com.crackmydroid.shared.domain.usecase.installedapps.ListInstalledAppsUseCase
import com.crackmydroid.shared.domain.usecase.installedapps.ShareApkUseCase
import com.crackmydroid.shared.domain.usecase.log.ExportLogsUseCase
import com.crackmydroid.shared.domain.usecase.log.GetLogsUseCase
import com.crackmydroid.shared.domain.usecase.pentest.RunPenTestChecksUseCase
import com.crackmydroid.shared.domain.usecase.permissions.AnalyzePermissionRisksUseCase
import com.crackmydroid.shared.domain.usecase.permissions.BuildPermissionsReportUseCase
import com.crackmydroid.shared.domain.usecase.permissions.ListPermissionsUseCase
import com.crackmydroid.shared.domain.usecase.reports.ExportReportUseCase
import com.crackmydroid.shared.domain.usecase.root.CheckPlayIntegrityUseCase
import com.crackmydroid.shared.domain.usecase.root.CheckRootUseCase
import com.crackmydroid.shared.domain.usecase.root.VerifyPlayIntegrityTokenUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetActivityFavoritesUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetActivityPackageFavoritesUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetExportFormatUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetFeatureHintsEnabledUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetHighContrastUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetInstalledAppFavoritesUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetIntroSeenUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetLanguageUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetLargeTextUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetPermissionFavoritesUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetReduceMotionUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetSuggestionsEnabledUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetThemeUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetVerboseLogUseCase
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
import com.crackmydroid.shared.domain.usecase.trick.ExecuteShellCommandUseCase
import com.crackmydroid.shared.domain.usecase.trick.ListShellCommandsUseCase
import com.crackmydroid.shared.ui.AppPresentationDependencies
import com.crackmydroid.shared.ui.activities.ActivityListViewModel
import com.crackmydroid.shared.ui.appshell.AppShellViewModel
import com.crackmydroid.shared.ui.home.HomeViewModel
import com.crackmydroid.shared.ui.info.InfoViewModel
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
    single<Logger> { AppLogger.logger }
    single<AppDispatchers> { DefaultAppDispatchers }
    factory { GetLanguageUseCase(get(), logger = get()) }
    factory { SetLanguageUseCase(get(), logger = get()) }
    factory { GetActivityFavoritesUseCase(get(), logger = get()) }
    factory { SetActivityFavoritesUseCase(get(), logger = get()) }
    factory { GetActivityPackageFavoritesUseCase(get(), logger = get()) }
    factory { SetActivityPackageFavoritesUseCase(get(), logger = get()) }
    factory { GetInstalledAppFavoritesUseCase(get(), logger = get()) }
    factory { SetInstalledAppFavoritesUseCase(get(), logger = get()) }
    factory { GetPermissionFavoritesUseCase(get(), logger = get()) }
    factory { SetPermissionFavoritesUseCase(get(), logger = get()) }
    factory { GetActivitiesUseCase(get(), logger = get()) }
    factory { LaunchActivityUseCase(get(), logger = get()) }
    factory { CheckRootUseCase(get(), logger = get()) }
    factory { CheckPlayIntegrityUseCase(get(), logger = get()) }
    factory { VerifyPlayIntegrityTokenUseCase(get(), logger = get()) }
    factory { GetDeviceInfoUseCase(get(), logger = get()) }
    factory { BuildDeviceInfoReportUseCase(dispatchers = get(), logger = get()) }
    factory { ListShellCommandsUseCase(get(), logger = get()) }
    factory { ExecuteShellCommandUseCase(get(), logger = get()) }
    factory { GetThemeUseCase(get(), logger = get()) }
    factory { SetThemeUseCase(get(), logger = get()) }
    factory { GetVerboseLogUseCase(get(), logger = get()) }
    factory { SetVerboseLogUseCase(get(), logger = get()) }
    factory { GetExportFormatUseCase(get(), logger = get()) }
    factory { SetExportFormatUseCase(get(), logger = get()) }
    factory { GetHighContrastUseCase(get(), logger = get()) }
    factory { SetHighContrastUseCase(get(), logger = get()) }
    factory { GetReduceMotionUseCase(get(), logger = get()) }
    factory { SetReduceMotionUseCase(get(), logger = get()) }
    factory { GetLargeTextUseCase(get(), logger = get()) }
    factory { SetLargeTextUseCase(get(), logger = get()) }
    factory { GetSuggestionsEnabledUseCase(get(), logger = get()) }
    factory { SetSuggestionsEnabledUseCase(get(), logger = get()) }
    factory { GetFeatureHintsEnabledUseCase(get(), logger = get()) }
    factory { SetFeatureHintsEnabledUseCase(get(), logger = get()) }
    factory { GetLogsUseCase(get(), logger = get()) }
    factory { ExportLogsUseCase(get(), logger = get()) }
    factory { GetIntroSeenUseCase(get(), logger = get()) }
    factory { SetIntroSeenUseCase(get(), logger = get()) }
    factory { ListPermissionsUseCase(get(), logger = get()) }
    factory { AnalyzePermissionRisksUseCase(dispatchers = get(), logger = get()) }
    factory { BuildPermissionsReportUseCase(get(), dispatchers = get(), logger = get()) }
    factory { RunPenTestChecksUseCase(get(), dispatchers = get(), logger = get()) }
    factory { ExportReportUseCase(get(), get(), logger = get()) }
    factory { ListInstalledAppsUseCase(get(), logger = get()) }
    factory { ExportApkUseCase(get(), logger = get()) }
    factory { ShareApkUseCase(get(), logger = get()) }
}

fun presentationModule() = module {
    factory { ActivityListViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { HomeViewModel(get(), get()) }
    factory {
        val activity = get<ActivityListViewModel>()
        val home = get<HomeViewModel>()
        val root = get<RootViewModel>()
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
                home = home,
                root = root,
                info = info,
                trick = trick,
                settings = settings,
                logs = logs,
                permissions = permissions,
                installedApps = installedApps,
                penTesting = penTesting,
                getVerbose = get(),
                dispatchers = get(),
            ),
            activity = activity,
            home = home,
            root = root,
            info = info,
            trick = trick,
            settings = settings,
            logs = logs,
            permissions = permissions,
            installedApps = installedApps,
            penTesting = penTesting,
        )
    }
    factory { RootViewModel(get(), get(), get(), get()) }
    factory { InfoViewModel(get(), get(), get(), get(), get()) }
    factory { TrickViewModel(get(), get(), get()) }
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
            get(), get(), // intro seen
            get(), get(), // language
            get(), // dispatchers
        )
    }
    factory { LogViewModel(get(), get(), get()) }
    factory { PermissionsViewModel(get(), get(), get(), get(), get(), get(), get()) }
    factory { InstalledAppsViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { PenTestingViewModel(get(), get(), get(), get(), get(), get(), get()) }
}
