package com.crackmydroid.shared.domain.usecase.installedapps

import com.crackmydroid.shared.domain.repository.InstalledAppsRepository

class ExportApkUseCase(private val repository: InstalledAppsRepository) {
    suspend operator fun invoke(packageName: String): Result<String> =
        repository.exportApk(packageName)
}
