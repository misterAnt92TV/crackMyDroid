package com.crackmydroid.desktop

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.crackmydroid.shared.domain.model.app.AppTheme
import com.crackmydroid.shared.domain.repository.DeviceSessionController
import com.crackmydroid.shared.domain.usecase.settings.GetLanguageUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetThemeUseCase
import com.crackmydroid.shared.i18n.LocalStrings
import com.crackmydroid.shared.i18n.platformLanguage
import com.crackmydroid.shared.i18n.resolveLanguageCode
import com.crackmydroid.shared.i18n.stringsFor
import com.crackmydroid.shared.presentation.theme.CrackMyDroidTheme
import com.crackmydroid.shared.ui.AppPresentationDependencies
import com.crackmydroid.shared.ui.CrackMyDroidApp
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform

@Composable
fun DesktopCrackMyDroidApp() {
    val sessionController = remember {
        KoinPlatform.getKoin().get<DeviceSessionController>()
    }
    val getLanguage = remember {
        KoinPlatform.getKoin().get<GetLanguageUseCase>()
    }
    val getTheme = remember { KoinPlatform.getKoin().get<GetThemeUseCase>() }
    val state by sessionController.state.collectAsState()
    val presentationDependencies = remember(state.sessionVersion) {
        KoinPlatform.getKoin().get<AppPresentationDependencies>()
    }
    val scope = rememberCoroutineScope()
    var adbPath by remember { mutableStateOf(state.adbPath) }
    var showAdbSettings by remember { mutableStateOf(false) }
    var strings by remember { mutableStateOf(stringsFor(platformLanguage())) }
    var appTheme by remember { mutableStateOf(AppTheme.SYSTEM) }

    LaunchedEffect(state.adbPath) {
        if (state.adbPath.isNotBlank() && adbPath != state.adbPath) {
            adbPath = state.adbPath
        }
    }

    LaunchedEffect(Unit) {
        getLanguage().onSuccess { language ->
            strings = stringsFor(resolveLanguageCode(language, platformLanguage()))
        }
        getTheme().onSuccess { appTheme = it }
        if (state.availableDevices.isEmpty()) {
            sessionController.refreshDevices(null)
        }
    }

    CompositionLocalProvider(LocalStrings provides strings) {
        CrackMyDroidTheme(
            darkTheme = when (appTheme) {
                AppTheme.DARK -> true
                AppTheme.LIGHT -> false
                AppTheme.SYSTEM -> isSystemInDarkTheme()
            },
        ) {
            Surface(modifier = Modifier.fillMaxSize()) {
                if (state.selectedDevice == null) {
                    DevicePickerContent(
                        adbPath = adbPath,
                        adbPathConfigured = state.adbPathConfigured,
                        onAdbPathChange = { adbPath = it },
                        devices = state.availableDevices,
                        loading = state.loading,
                        error = state.error,
                        onOpenAdbSettings = { showAdbSettings = true },
                        onRefresh = {
                            scope.launch {
                                sessionController.refreshDevices(
                                    adbPath.takeIf { !state.adbPathConfigured },
                                )
                            }
                        },
                        onSelect = { device ->
                            scope.launch {
                                sessionController.selectDevice(
                                    device,
                                    adbPath.takeIf { !state.adbPathConfigured },
                                )
                            }
                        },
                    )
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        DesktopToolbarComponent(
                            device = requireNotNull(state.selectedDevice),
                            dataSource = state.dataSource,
                            snapshotStatus = state.snapshotStatus,
                            snapshotMetadata = state.snapshotMetadata,
                            onOpenAdbSettings = { showAdbSettings = true },
                            onChangeDevice = {
                                scope.launch {
                                    sessionController.clearSelection()
                                    sessionController.refreshDevices(null)
                                }
                            },
                            onRescan = {
                                scope.launch {
                                    sessionController.rescanSelectedDevice()
                                }
                            },
                        )
                        key(state.sessionVersion) {
                            CrackMyDroidApp(
                                dependencies = presentationDependencies,
                                onLanguageChanged = { language ->
                                    strings = stringsFor(resolveLanguageCode(language, platformLanguage()))
                                },
                            )
                        }
                    }
                }
            }

            if (showAdbSettings) {
                AdbSettingsDialogComponent(
                    adbPath = adbPath,
                    onAdbPathChange = { adbPath = it },
                    onDismiss = { showAdbSettings = false },
                    onSave = {
                        scope.launch {
                            sessionController.refreshDevices(adbPath)
                                .onSuccess {
                                    if (state.selectedDevice != null) {
                                        sessionController.rescanSelectedDevice()
                                    }
                                    showAdbSettings = false
                                }
                        }
                    },
                )
            }
        }
    }
}
