package com.crackmydroid.shared.data.device

import com.crackmydroid.shared.data.DesktopScanCache
import com.crackmydroid.shared.data.snapshot.RemoteDeviceSnapshotImporter
import com.crackmydroid.shared.domain.model.ConnectedDevice
import com.crackmydroid.shared.domain.repository.AdbBridge
import com.crackmydroid.shared.domain.repository.DeviceSessionController
import com.crackmydroid.shared.domain.repository.DeviceSessionState
import com.crackmydroid.shared.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking

class DeviceSessionControllerDesktop(
    private val adbBridge: AdbBridge,
    private val settingsRepository: SettingsRepository,
    private val cache: DesktopScanCache,
    private val snapshotImporter: RemoteDeviceSnapshotImporter? = null
) : DeviceSessionController {
    private val configuredAdbPath = runBlocking { settingsRepository.getAdbPath() }
    private val _state = MutableStateFlow(
        DeviceSessionState(
            adbPath = configuredAdbPath.takeIf { it.isNotBlank() }
                ?: runBlocking { adbBridge.resolveBinary().getOrDefault("") },
            adbPathConfigured = configuredAdbPath.isNotBlank()
        )
    )

    override val state: StateFlow<DeviceSessionState> = _state.asStateFlow()

    override suspend fun refreshDevices(adbPathOverride: String?): Result<List<ConnectedDevice>> {
        val adbPath = adbPathOverride?.trim().orEmpty().ifBlank { _state.value.adbPath }
        val configured = !adbPathOverride.isNullOrBlank()
        if (configured) {
            settingsRepository.setAdbPath(adbPath)
        }
        _state.update { it.copy(loading = true, error = null, adbPath = adbPath) }
        return adbBridge.listDevices(adbPath)
            .onSuccess { devices ->
                val currentSerial = _state.value.selectedDevice?.serial
                val selected = devices.firstOrNull { it.serial == currentSerial }
                _state.update {
                    it.copy(
                        availableDevices = devices,
                        adbPathConfigured = it.adbPathConfigured || configured,
                        selectedDevice = selected,
                        loading = false,
                        error = null
                    )
                }
            }
            .onFailure { error ->
                _state.update {
                    it.copy(
                        availableDevices = emptyList(),
                        loading = false,
                        error = error.message ?: "Errore ADB"
                    )
                }
            }
    }

    override suspend fun selectDevice(device: ConnectedDevice, adbPathOverride: String?): Result<Unit> {
        val adbPath = adbPathOverride?.trim().orEmpty().ifBlank { _state.value.adbPath }
        val configured = !adbPathOverride.isNullOrBlank()
        return runCatching {
            if (configured) {
                settingsRepository.setAdbPath(adbPath)
            }
            clearCaches()
            val snapshotOutcome = snapshotImporter?.import(device, adbPath)
            _state.update {
                it.copy(
                    adbPath = adbPath,
                    adbPathConfigured = it.adbPathConfigured || configured,
                    selectedDevice = device,
                    error = null,
                    dataSourceLabel = if (snapshotOutcome?.usedSnapshot == true) "Snapshot Android" else "ADB live",
                    snapshotStatus = snapshotOutcome?.message,
                    snapshotMetadata = snapshotOutcome?.metadata,
                    sessionVersion = it.sessionVersion + 1
                )
            }
        }
    }

    override suspend fun clearSelection() {
        clearCaches()
        _state.update {
            it.copy(
                selectedDevice = null,
                error = null,
                dataSourceLabel = "ADB live",
                snapshotStatus = null,
                snapshotMetadata = null
            )
        }
    }

    override suspend fun rescanSelectedDevice(): Result<Unit> = runCatching {
        val device = requireNotNull(_state.value.selectedDevice) { "Nessun device selezionato" }
        clearCaches()
        val snapshotOutcome = snapshotImporter?.import(device, _state.value.adbPath)
        _state.update {
            it.copy(
                error = null,
                dataSourceLabel = if (snapshotOutcome?.usedSnapshot == true) "Snapshot Android" else "ADB live",
                snapshotStatus = snapshotOutcome?.message,
                snapshotMetadata = snapshotOutcome?.metadata,
                sessionVersion = it.sessionVersion + 1
            )
        }
    }

    private fun clearCaches() {
        cache.clearAll()
    }
}
