package com.crackmydroid.shared.domain.repository

import com.crackmydroid.shared.domain.model.RootStatus

interface RootCheckRepository {
    suspend fun checkRoot(): RootStatus
    suspend fun checkPlayIntegrity(nonce: String = "sample-nonce"): com.crackmydroid.shared.domain.model.PlayIntegrityResult
}