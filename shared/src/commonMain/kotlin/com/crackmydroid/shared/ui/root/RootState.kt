package com.crackmydroid.shared.ui.root

import com.crackmydroid.shared.domain.model.PlayIntegrityResult
import com.crackmydroid.shared.domain.model.RootStatus

data class RootState(
    val status: RootStatus? = null,
    val playIntegrity: PlayIntegrityResult? = null,
    val loading: Boolean = false,
    val error: String? = null,
    val reportPath: String? = null
)
