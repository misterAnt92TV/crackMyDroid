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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crackmydroid.shared.domain.model.ConnectedDevice
import com.crackmydroid.shared.domain.model.SnapshotMetadata
import com.crackmydroid.shared.domain.repository.DeviceSessionController
import com.crackmydroid.shared.presentation.components.AppButton
import com.crackmydroid.shared.ui.CrackMyDroidApp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform

@Composable
fun DesktopCrackMyDroidApp() {
    val sessionController = remember {
        KoinPlatform.getKoin().get<DeviceSessionController>()
    }
    val state by sessionController.state.collectAsState()
    val scope = rememberCoroutineScope()
    var adbPath by remember { mutableStateOf(state.adbPath) }
    var showAdbSettings by remember { mutableStateOf(false) }

    LaunchedEffect(state.adbPath) {
        if (state.adbPath.isNotBlank() && adbPath != state.adbPath) {
            adbPath = state.adbPath
        }
    }

    LaunchedEffect(Unit) {
        if (state.availableDevices.isEmpty()) {
            sessionController.refreshDevices(null)
        }
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            if (state.selectedDevice == null) {
                DevicePickerScreen(
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
                    DesktopToolbar(
                        device = requireNotNull(state.selectedDevice),
                        dataSourceLabel = state.dataSourceLabel,
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
                        CrackMyDroidApp()
                    }
                }
            }
        }

        if (showAdbSettings) {
            AdbSettingsDialog(
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

@Composable
private fun DevicePickerScreen(
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
            Text(
                text = "Seleziona device ADB",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "L'app desktop usa adb come bridge verso il dispositivo Android. Scegli prima il device, poi parte la scansione.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!adbPathConfigured) {
                OutlinedTextField(
                    value = adbPath,
                    onValueChange = onAdbPathChange,
                    label = { Text("Percorso adb") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            } else {
                Text(
                    text = "Il percorso adb è configurato. Puoi modificarlo solo da \"Impostazioni ADB\".",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppButton(
                    text = "Rileva device",
                    onClick = onRefresh,
                    leadingIcon = Icons.Filled.Refresh
                )
                AppButton(
                    text = "Impostazioni ADB",
                    onClick = onOpenAdbSettings,
                    variant = com.crackmydroid.shared.presentation.components.ButtonVariant.Tonal,
                    leadingIcon = Icons.Filled.Settings
                )
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(26.dp))
                }
            }
            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 2.dp,
                shape = RoundedCornerShape(16.dp)
            ) {
                if (devices.isEmpty() && !loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nessun device rilevato. Collega un device o avvia un emulator, poi premi \"Rileva device\".",
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
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = if (selectable) {
                                    MaterialTheme.colorScheme.surface
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                                shape = RoundedCornerShape(14.dp),
                                onClick = {
                                    if (selectable) onSelect(device)
                                }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = device.model ?: device.serial,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Serial: ${device.serial}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "State: ${device.state}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (selectable) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.error
                                        }
                                    )
                                    Text(
                                        text = listOfNotNull(
                                            device.product?.let { "Product: $it" },
                                            device.device?.let { "Device: $it" },
                                            device.transportId?.let { "Transport: $it" }
                                        ).joinToString("  |  "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (!selectable) {
                                        Text(
                                            text = when (device.state) {
                                                "offline" -> "Il device e visibile ma offline."
                                                "unauthorized" -> "Autorizza il computer sul device e riprova."
                                                else -> "Stato non pronto per la scansione."
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
private fun DesktopToolbar(
    device: ConnectedDevice,
    dataSourceLabel: String,
    snapshotStatus: String?,
    snapshotMetadata: SnapshotMetadata?,
    onOpenAdbSettings: () -> Unit,
    onChangeDevice: () -> Unit,
    onRescan: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Filled.Devices,
                    contentDescription = null
                )
                Column {
                    Text(
                        text = device.model ?: device.serial,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${device.serial} • ${device.state}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = buildSourceLabel(dataSourceLabel, snapshotMetadata),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    snapshotStatus?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AppButton(
                    text = "Impostazioni ADB",
                    onClick = onOpenAdbSettings,
                    variant = com.crackmydroid.shared.presentation.components.ButtonVariant.Tonal,
                    leadingIcon = Icons.Filled.Edit
                )
                AppButton(
                    text = "Cambia device",
                    onClick = onChangeDevice,
                    variant = com.crackmydroid.shared.presentation.components.ButtonVariant.Tonal,
                    leadingIcon = Icons.Filled.SwapHoriz
                )
                AppButton(
                    text = "Riscansiona",
                    onClick = onRescan,
                    leadingIcon = Icons.Filled.Refresh
                )
            }
        }
    }
}

private fun buildSourceLabel(
    dataSourceLabel: String,
    snapshotMetadata: SnapshotMetadata?
): String {
    if (snapshotMetadata == null) return "Sorgente dati: $dataSourceLabel"
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    val timestamp = Instant.ofEpochMilli(snapshotMetadata.createdAtEpochMs)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
    return "Sorgente dati: $dataSourceLabel • $timestamp • ${snapshotMetadata.sourceAppVersion}"
}

@Composable
private fun AdbSettingsDialog(
    adbPath: String,
    onAdbPathChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Impostazioni ADB") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Inserisci il percorso del binario adb da usare nella desktop app. Dopo il salvataggio il campo non verrà più mostrato nel picker iniziale.",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = adbPath,
                    onValueChange = onAdbPathChange,
                    label = { Text("Percorso adb") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            AppButton(
                text = "Salva",
                onClick = onSave,
                enabled = adbPath.isNotBlank(),
                fullWidth = false
            )
        },
        dismissButton = {
            AppButton(
                text = "Annulla",
                onClick = onDismiss,
                variant = com.crackmydroid.shared.presentation.components.ButtonVariant.Tonal,
                fullWidth = false
            )
        }
    )
}
