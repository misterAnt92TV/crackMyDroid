package com.crackmydroid.shared.ui.info

import com.crackmydroid.shared.domain.model.DeviceInfo
import com.crackmydroid.shared.domain.model.RootStatus

data class InfoState(
    val info: DeviceInfo? = null,
    val root: RootStatus? = null,
    val loading: Boolean = false,
    val error: String? = null,
    val lastExportPath: String? = null
)
