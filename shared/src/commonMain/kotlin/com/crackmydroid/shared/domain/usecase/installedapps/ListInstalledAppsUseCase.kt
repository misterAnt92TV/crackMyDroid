package com.crackmydroid.shared.domain.usecase

import com.crackmydroid.shared.domain.model.InstalledAppEntry
import com.crackmydroid.shared.domain.repository.InstalledAppsRepository

class ListInstalledAppsUseCase(private val repository: InstalledAppsRepository) {
    suspend operator fun invoke(): List<InstalledAppEntry> = repository.listInstalled()
}
