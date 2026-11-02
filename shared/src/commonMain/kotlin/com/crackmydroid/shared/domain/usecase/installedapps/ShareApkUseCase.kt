package com.crackmydroid.shared.domain.usecase

import com.crackmydroid.shared.domain.repository.InstalledAppsRepository

class ShareApkUseCase(private val repository: InstalledAppsRepository) {
    suspend operator fun invoke(packageName: String, bluetoothOnly: Boolean): Result<Unit> =
        repository.shareApk(packageName, bluetoothOnly)
}
