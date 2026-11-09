package com.crackmydroid.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crackmydroid.shared.domain.model.ConnectedDevice
import com.crackmydroid.shared.domain.model.DeviceSessionSource
import com.crackmydroid.shared.domain.model.scansnapshot.SnapshotMetadata
import com.crackmydroid.shared.domain.repository.DeviceSessionController
import com.crackmydroid.shared.domain.usecase.settings.GetLanguageUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetThemeUseCase
import com.crackmydroid.shared.domain.model.app.AppTheme
import com.crackmydroid.shared.i18n.LocalStrings
import com.crackmydroid.shared.i18n.Strings
import com.crackmydroid.shared.i18n.platformLanguage
import com.crackmydroid.shared.i18n.resolveLanguageCode
import com.crackmydroid.shared.i18n.stringsFor
import com.crackmydroid.shared.presentation.components.AppButton
import com.crackmydroid.shared.presentation.components.AppDialog
import com.crackmydroid.shared.presentation.components.AppProgressIndicator
import com.crackmydroid.shared.presentation.components.AppTextField
import com.crackmydroid.shared.presentation.components.card.AppCard
import com.crackmydroid.shared.presentation.components.text.AppText
import com.crackmydroid.shared.presentation.theme.CrackMyDroidTheme
import com.crackmydroid.shared.ui.CrackMyDroidApp
import com.crackmydroid.shared.ui.AppPresentationDependencies
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun DesktopCrackMyDroidApp() {
    val sessionController = remember {
        KoinPlatform.getKoin().get<DeviceSessionController>()
    }
    val getLanguage = remember {
        KoinPlatform.getKoin().get<GetLanguageUseCase>()
    }
    val getTheme = remember { KoinPlatform.getKoin().get<GetThemeUseCase>() }
    val presentationDependencies = remember {
        KoinPlatform.getKoin().get<AppPresentationDependencies>()
    }
    val state by sessionController.state.collectAsState()
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
        CrackMyDroidTheme(darkTheme = when (appTheme) {
            AppTheme.DARK -> true
            AppTheme.LIGHT -> false
            AppTheme.SYSTEM -> isSystemInDarkTheme()
        }) {
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
                                adbPath.takeIf { !state.adbPathConfigured }
                            )
                        }
                    },
                    onSelect = { device ->
                        scope.launch {
                            sessionController.selectDevice(
                                device,
                                adbPath.takeIf { !state.adbPathConfigured }
                            )
                        }
                    }
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
                        }
                    )
                    key(state.sessionVersion) {
                        CrackMyDroidApp(
                            dependencies = presentationDependencies,
                            onLanguageChanged = { language ->
                                strings = stringsFor(resolveLanguageCode(language, platformLanguage()))
                            }
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
                }
            )
        }
        }
    }
}

@Composable
private fun DevicePickerContent(
    adbPath: String,
    adbPathConfigured: Boolean,
    onAdbPathChange: (String) -> Unit,
    devices: List<ConnectedDevice>,
    loading: Boolean,
    error: String?,
    onOpenAdbSettings: () -> Unit,
    onRefresh: () -> Unit,
    onSelect: (ConnectedDevice) -> Unit
) {
    val strings = LocalStrings.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppText(
                text = strings.desktopAdbPickerTitle,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            AppText(
                text = strings.desktopAdbPickerDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!adbPathConfigured) {
                AppTextField(
                    value = adbPath,
                    onValueChange = onAdbPathChange,
                    label = strings.desktopAdbPathLabel,
                    singleLine = true
                )
            } else {
                AppText(
                    text = strings.desktopAdbPathConfigured,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppButton(
                    text = strings.desktopDetectDevices,
                    onClick = onRefresh,
                    leadingIcon = Icons.Filled.Refresh,
                    fullWidth = false
                )
                AppButton(
                    text = strings.desktopAdbSettings,
                    onClick = onOpenAdbSettings,
                    variant = com.crackmydroid.shared.presentation.components.ButtonVariant.Tonal,
                    leadingIcon = Icons.Filled.Settings,
                    fullWidth = false
                )
                if (loading) {
                    AppProgressIndicator(strings.desktopDetectDevices, modifier = Modifier.size(26.dp))
                }
            }
            error?.let {
                AppText(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            AppCard(
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (devices.isEmpty() && !loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AppText(
                            text = strings.desktopNoDevices,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(devices) { device ->
                            val selectable = device.state == "device"
                            AppCard(
                                modifier = Modifier.fillMaxWidth(),
                                enabled = selectable,
                                onClick = { onSelect(device) }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    AppText(
                                        text = device.model ?: device.serial,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    AppText(
                                        text = "${strings.desktopSerialLabel}: ${device.serial}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    AppText(
                                        text = "${strings.desktopStateLabel}: ${device.state}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (selectable) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.error
                                        }
                                    )
                                    AppText(
                                        text = listOfNotNull(
                                            device.product?.let { "${strings.desktopProductLabel}: $it" },
                                            device.device?.let { "${strings.desktopDeviceLabel}: $it" },
                                            device.transportId?.let { "${strings.desktopTransportLabel}: $it" }
                                        ).joinToString("  |  "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (!selectable) {
                                        AppText(
                                            text = when (device.state) {
                                                "offline" -> strings.desktopDeviceOffline
                                                "unauthorized" -> strings.desktopDeviceUnauthorized
                                                else -> strings.desktopDeviceNotReady
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DesktopToolbarComponent(
    device: ConnectedDevice,
    dataSource: DeviceSessionSource,
    snapshotStatus: String?,
    snapshotMetadata: SnapshotMetadata?,
    onOpenAdbSettings: () -> Unit,
    onChangeDevice: () -> Unit,
    onRescan: () -> Unit
) {
    val strings = LocalStrings.current
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Filled.Devices,
                    contentDescription = null
                )
                Column(modifier = Modifier.weight(1f)) {
                    AppText(
                        text = device.model ?: device.serial,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    AppText(
                        text = "${device.serial} • ${device.state}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AppText(
                        text = buildSourceLabel(strings, dataSource, snapshotMetadata),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    snapshotStatus?.let {
                        AppText(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.padding(start = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppButton(
                    text = strings.desktopAdbSettings,
                    onClick = onOpenAdbSettings,
                    variant = com.crackmydroid.shared.presentation.components.ButtonVariant.Tonal,
                    leadingIcon = Icons.Filled.Edit,
                    fullWidth = false
                )
                AppButton(
                    text = strings.desktopChangeDevice,
                    onClick = onChangeDevice,
                    variant = com.crackmydroid.shared.presentation.components.ButtonVariant.Tonal,
                    leadingIcon = Icons.Filled.SwapHoriz,
                    fullWidth = false
                )
                AppButton(
                    text = strings.desktopRescan,
                    onClick = onRescan,
                    leadingIcon = Icons.Filled.Refresh,
                    fullWidth = false
                )
            }
        }
    }
}

private fun buildSourceLabel(
    strings: Strings,
    dataSource: DeviceSessionSource,
    snapshotMetadata: SnapshotMetadata?
): String {
    val sourceLabel = when (dataSource) {
        DeviceSessionSource.ADB -> strings.desktopAdbSource
        DeviceSessionSource.SNAPSHOT -> strings.desktopSnapshotSource
    }
    if (snapshotMetadata == null) return strings.desktopDataSourceLabel.format(sourceLabel)
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    val timestamp = Instant.ofEpochMilli(snapshotMetadata.createdAtEpochMs)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
    return "${strings.desktopDataSourceLabel.format(sourceLabel)} • $timestamp • ${snapshotMetadata.sourceAppVersion}"
}

@Composable
private fun AdbSettingsDialogComponent(
    adbPath: String,
    onAdbPathChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val strings = LocalStrings.current
    AppDialog(
        title = strings.desktopAdbSettings,
        onDismiss = onDismiss,
        confirmLabel = strings.desktopSave,
        onConfirm = onSave,
        confirmEnabled = adbPath.isNotBlank(),
        dismissLabel = strings.desktopCancel,
        onDismissAction = onDismiss
    ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AppText(
                    strings.desktopAdbSettingsDescription,
                    style = MaterialTheme.typography.bodyMedium
                )
                AppTextField(
                    value = adbPath,
                    onValueChange = onAdbPathChange,
                    label = strings.desktopAdbPathLabel,
                    singleLine = true
                )
            }
    }
}
