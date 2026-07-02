package com.crackmydroid.shared.ui.info

import com.crackmydroid.shared.domain.model.DeviceInfo
import com.crackmydroid.shared.domain.model.RootStatus
import com.crackmydroid.shared.domain.usecase.CheckRootUseCase
import com.crackmydroid.shared.domain.usecase.GetDeviceInfoUseCase
import com.crackmydroid.shared.presentation.BaseViewModel
import com.crackmydroid.shared.util.saveTextFile
import com.crackmydroid.shared.util.shareTextFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InfoViewModel(
    private val getInfo: GetDeviceInfoUseCase,
    private val checkRoot: CheckRootUseCase
) : BaseViewModel() {
    private val _state = MutableStateFlow(InfoState())
    val state: StateFlow<InfoState> = _state

    fun load() {
        scope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching {
                val info = getInfo()
                val root = checkRoot()
                info to root
            }
                .onSuccess { (info, root) -> _state.update { it.copy(info = info, root = root, loading = false) } }
                .onFailure { err -> _state.update { it.copy(error = err.message, loading = false) } }
        }
    }

    fun export() {
        val info = _state.value.info ?: return
        val content = buildInfoText(info, _state.value.root)
        val result = saveTextFile("device_info", content)
        result.onSuccess { path -> _state.update { it.copy(lastExportPath = path, error = null) } }
            .onFailure { err -> _state.update { it.copy(error = err.message) } }
    }

    fun share(contentOverride: String? = null) {
        val info = _state.value.info ?: return
        val content = contentOverride ?: buildInfoText(info, _state.value.root)
        scope.launch {
            runCatching {
                val path = saveTextFile("device_info", content).getOrThrow()
                shareTextFile(path).getOrThrow()
                path
            }.onSuccess { path -> _state.update { it.copy(lastExportPath = path, error = null) } }
                .onFailure { err -> _state.update { it.copy(error = err.message) } }
        }
    }

    private fun buildInfoText(info: DeviceInfo, root: RootStatus?): String = buildString {
        appendLine("Device info")
        appendLine("Manufacturer: ${info.manufacturer}")
        appendLine("Model: ${info.model}")
        appendLine("Brand: ${info.brand}")
        appendLine("Device: ${info.device}")
        appendLine("Product: ${info.product}")
        appendLine("Android: ${info.androidVersion}")
        appendLine("Patch: ${info.securityPatch}")
        appendLine("Build ID: ${info.buildId}")
        appendLine("Build display: ${info.buildDisplay}")
        appendLine("Fingerprint: ${info.buildFingerprint}")
        appendLine("Radio: ${info.radio}")
        appendLine("Kernel: ${info.kernelVersion}")
        appendLine("Hardware: ${info.hardware}")
        appendLine("Bootloader: ${info.bootloader}")
        appendLine("Battery: ${info.batteryLevel ?: -1}%")
        appendLine("Network: ${info.networkType}")
        appendLine("Total RAM: ${info.totalMemory}")
        appendLine("Free RAM: ${info.freeMemory}")
        appendLine("Screen: ${info.screenResolution} (${info.densityDpi} dpi)")
        appendLine("ADB enabled: ${info.adbEnabled}")
        appendLine("Developer options: ${info.developerOptions}")
        appendLine("App debuggable: ${info.appDebuggable}")
        root?.let {
            appendLine("Rooted: ${it.isRooted}")
            appendLine("Root details: ${it.details}")
        }
        appendLine("IMEI: ${info.imei}")
        appendLine("Serial: ${info.serial}")
        if (!info.systemPropDump.isNullOrBlank()) {
            appendLine()
            appendLine("System properties source: ${info.systemPropSource ?: "n/a"}")
            appendLine("System properties dump:")
            appendLine(info.systemPropDump)
        }
    }
}
