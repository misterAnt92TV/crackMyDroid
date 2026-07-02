package com.crackmydroid.shared.domain.usecase

import com.crackmydroid.shared.domain.model.InstalledAppEntry
import com.crackmydroid.shared.domain.repository.InstalledAppsRepository

class ListInstalledAppsUseCase(private val repo: InstalledAppsRepository) {
    suspend operator fun invoke(): List<InstalledAppEntry> = repo.listInstalled()
}

class ExportApkUseCase(private val repo: InstalledAppsRepository) {
    suspend operator fun invoke(packageName: String): Result<String> = repo.exportApk(packageName)
}

class ShareApkUseCase(private val repo: InstalledAppsRepository) {
    suspend operator fun invoke(packageName: String, bluetoothOnly: Boolean): Result<Unit> =
        repo.shareApk(packageName, bluetoothOnly)
}
