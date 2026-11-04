package com.crackmydroid.shared.domain.repository

import com.crackmydroid.shared.domain.model.RootStatus
import com.crackmydroid.shared.domain.model.root.RootCheckMode

interface RootCheckRepository {
    suspend fun checkRoot(mode: RootCheckMode = RootCheckMode.PASSIVE): RootStatus
    suspend fun checkPlayIntegrity(nonce: String = "sample-nonce"): com.crackmydroid.shared.domain.model.PlayIntegrityResult
}
