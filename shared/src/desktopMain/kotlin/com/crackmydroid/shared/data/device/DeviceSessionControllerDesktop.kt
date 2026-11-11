package com.crackmydroid.shared.data.device

import com.crackmydroid.shared.data.DesktopScanCache
import com.crackmydroid.shared.data.snapshot.RemoteDeviceSnapshotImporter
import com.crackmydroid.shared.domain.model.ConnectedDevice
import com.crackmydroid.shared.domain.model.DeviceSessionSource
import com.crackmydroid.shared.domain.model.DeviceSessionState
import com.crackmydroid.shared.domain.repository.AdbBridge
import com.crackmydroid.shared.domain.repository.DeviceSessionController
import com.crackmydroid.shared.domain.repository.SettingsRepository
import com.crackmydroid.shared.util.runSuspendCatching
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DeviceSessionControllerDesktop(
    private val adbBridge: AdbBridge,
    private val settingsRepository: SettingsRepository,
    private val cache: DesktopScanCache,
    private val snapshotImporter: RemoteDeviceSnapshotImporter? = null,
) : DeviceSessionController {
    private val _state = MutableStateFlow(DeviceSessionState())

    override val state: StateFlow<DeviceSessionState> = _state.asStateFlow()

    private suspend fun resolveEffectiveAdbPath(override: String?): Pair<String, Boolean> {
        val trimmed = override?.trim().orEmpty()
        if (trimmed.isNotBlank()) {
            return trimmed to true
        }
        val current = _state.value.adbPath
        if (current.isNotBlank()) {
            return current to _state.value.adbPathConfigured
        }
        val configured = settingsRepository.getAdbPath()
        if (configured.isNotBlank()) {
            return configured to true
        }
        val resolved = adbBridge.resolveBinary().getOrDefault("")
        return resolved to false
    }

    override suspend fun refreshDevices(adbPathOverride: String?): Result<List<ConnectedDevice>> {
        val (adbPath, isConfigured) = resolveEffectiveAdbPath(adbPathOverride)
        val explicitlyOverridden = !adbPathOverride.isNullOrBlank()
        if (explicitlyOverridden) {
            settingsRepository.setAdbPath(adbPath)
        }
        _state.update {
            it.copy(
                loading = true,
                error = null,
                adbPath = adbPath,
                adbPathConfigured = isConfigured,
            )
        }
        return adbBridge.listDevices(adbPath)
            .onSuccess { devices ->
                val currentSerial = _state.value.selectedDevice?.serial
                val selected = devices.firstOrNull { it.serial == currentSerial }
                _state.update {
                    it.copy(
                        availableDevices = devices,
                        adbPathConfigured = it.adbPathConfigured || explicitlyOverridden,
                        selectedDevice = selected,
                        loading = false,
                        error = null,
                    )
                }
            }
            .onFailure { error ->
                _state.update {
                    it.copy(
                        availableDevices = emptyList(),
                        loading = false,
                        error = error.message ?: "Errore ADB",
                    )
                }
            }
    }

    override suspend fun selectDevice(device: ConnectedDevice, adbPathOverride: String?): Result<Unit> {
        val (adbPath, isConfigured) = resolveEffectiveAdbPath(adbPathOverride)
        val explicitlyOverridden = !adbPathOverride.isNullOrBlank()
        return runSuspendCatching {
            if (explicitlyOverridden) {
                settingsRepository.setAdbPath(adbPath)
            }
            clearCaches()
            val snapshotOutcome = snapshotImporter?.import(device, adbPath)
            _state.update {
                it.copy(
                    adbPath = adbPath,
                    adbPathConfigured = it.adbPathConfigured || explicitlyOverridden,
                    selectedDevice = device,
                    error = null,
                    dataSource = if (snapshotOutcome?.usedSnapshot ==
                        true
                    ) {
                        DeviceSessionSource.SNAPSHOT
                    } else {
                        DeviceSessionSource.ADB
                    },
                    snapshotStatus = snapshotOutcome?.message,
                    snapshotMetadata = snapshotOutcome?.metadata,
                    sessionVersion = it.sessionVersion + 1,
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
                dataSource = DeviceSessionSource.ADB,
                snapshotStatus = null,
                snapshotMetadata = null,
            )
        }
    }

    override suspend fun rescanSelectedDevice(): Result<Unit> = runSuspendCatching {
        val device = requireNotNull(_state.value.selectedDevice) { "Nessun device selezionato" }
        clearCaches()
        val snapshotOutcome = snapshotImporter?.import(device, _state.value.adbPath)
        _state.update {
            it.copy(
                error = null,
                dataSource = if (snapshotOutcome?.usedSnapshot ==
                    true
                ) {
                    DeviceSessionSource.SNAPSHOT
                } else {
                    DeviceSessionSource.ADB
                },
                snapshotStatus = snapshotOutcome?.message,
                snapshotMetadata = snapshotOutcome?.metadata,
                sessionVersion = it.sessionVersion + 1,
            )
        }
    }

    private fun clearCaches() {
        cache.clearAll()
    }
}
