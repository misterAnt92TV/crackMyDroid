package com.crackmydroid.shared.ui.info

import com.crackmydroid.architecture.presentation.model.UiState
import com.crackmydroid.architecture.presentation.model.UiLoadState
import com.crackmydroid.shared.domain.model.DeviceInfo
import com.crackmydroid.shared.domain.model.RootStatus

data class InfoState(
    val info: DeviceInfo? = null,
    val root: RootStatus? = null,
    val loadState: UiLoadState = UiLoadState.Idle,
    val error: String? = null,
    val lastExportPath: String? = null,
    val reportContent: String? = null,
    val reportLoadState: UiLoadState = UiLoadState.Idle,
) : UiState {
    val loading: Boolean get() = loadState is UiLoadState.Loading
    val reportLoading: Boolean get() = reportLoadState is UiLoadState.Loading
    val loadError: String? get() = (loadState as? UiLoadState.Error)?.message
    val reportError: String? get() = (reportLoadState as? UiLoadState.Error)?.message
}
