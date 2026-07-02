package com.crackmydroid.shared.di

import com.crackmydroid.shared.domain.usecase.CheckPlayIntegrityUseCase
import com.crackmydroid.shared.domain.usecase.CheckRootUseCase
import com.crackmydroid.shared.domain.usecase.ExecuteShellCommandUseCase
import com.crackmydroid.shared.domain.usecase.GetActivitiesUseCase
import com.crackmydroid.shared.domain.usecase.GetDeviceInfoUseCase
import com.crackmydroid.shared.domain.usecase.GetThemeUseCase
import com.crackmydroid.shared.domain.usecase.GetVerboseLogUseCase
import com.crackmydroid.shared.domain.usecase.GetLogsUseCase
import com.crackmydroid.shared.domain.usecase.LaunchActivityUseCase
import com.crackmydroid.shared.domain.usecase.ListShellCommandsUseCase
import com.crackmydroid.shared.domain.usecase.SetThemeUseCase
import com.crackmydroid.shared.domain.usecase.SetVerboseLogUseCase
import com.crackmydroid.shared.domain.usecase.ExportLogsUseCase
import com.crackmydroid.shared.domain.usecase.GetIntroSeenUseCase
import com.crackmydroid.shared.domain.usecase.SetIntroSeenUseCase
import com.crackmydroid.shared.domain.usecase.ListPermissionsUseCase
import com.crackmydroid.shared.domain.usecase.ListInstalledAppsUseCase
import com.crackmydroid.shared.domain.usecase.ExportApkUseCase
import com.crackmydroid.shared.domain.usecase.ShareApkUseCase
import com.crackmydroid.shared.domain.usecase.GetExportFormatUseCase
import com.crackmydroid.shared.domain.usecase.SetExportFormatUseCase
import com.crackmydroid.shared.domain.usecase.GetHighContrastUseCase
import com.crackmydroid.shared.domain.usecase.SetHighContrastUseCase
import com.crackmydroid.shared.domain.usecase.GetReduceMotionUseCase
import com.crackmydroid.shared.domain.usecase.SetReduceMotionUseCase
import com.crackmydroid.shared.domain.usecase.GetLargeTextUseCase
import com.crackmydroid.shared.domain.usecase.SetLargeTextUseCase
import com.crackmydroid.shared.domain.usecase.GetSuggestionsEnabledUseCase
import com.crackmydroid.shared.domain.usecase.SetSuggestionsEnabledUseCase
import com.crackmydroid.shared.domain.usecase.GetFeatureHintsEnabledUseCase
import com.crackmydroid.shared.domain.usecase.SetFeatureHintsEnabledUseCase
import com.crackmydroid.shared.domain.usecase.permissions.AnalyzePermissionRisksUseCase
import com.crackmydroid.shared.domain.usecase.permissions.BuildPermissionsReportUseCase
import com.crackmydroid.shared.ui.activities.ActivityListViewModel
import com.crackmydroid.shared.ui.info.InfoViewModel
import com.crackmydroid.shared.ui.root.RootViewModel
import com.crackmydroid.shared.ui.settings.SettingsViewModel
import com.crackmydroid.shared.ui.trick.TrickViewModel
import com.crackmydroid.shared.ui.logs.LogViewModel
import com.crackmydroid.shared.ui.permissions.PermissionsViewModel
import com.crackmydroid.shared.ui.installedapps.InstalledAppsViewModel
import com.crackmydroid.shared.ui.pentest.PenTestingViewModel
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
    factory { GetActivitiesUseCase(get()) }
    factory { LaunchActivityUseCase(get()) }
    factory { CheckRootUseCase(get()) }
    factory { CheckPlayIntegrityUseCase(get()) }
    factory { GetDeviceInfoUseCase(get()) }
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
    factory { ListInstalledAppsUseCase(get()) }
    factory { ExportApkUseCase(get()) }
    factory { ShareApkUseCase(get()) }
}

fun presentationModule() = module {
    factory { ActivityListViewModel(get(), get(), get()) }
    factory { RootViewModel(get(), get()) }
    factory { InfoViewModel(get(), get()) }
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
        )
    }
    factory { LogViewModel(get(), get()) }
    factory { PermissionsViewModel(get(), get(), get()) }
    factory { InstalledAppsViewModel(get(), get(), get(), get()) }
    factory { PenTestingViewModel(get(), get(), get(), get()) }
}
