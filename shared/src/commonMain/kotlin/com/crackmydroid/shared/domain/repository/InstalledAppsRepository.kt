package com.crackmydroid.shared.domain.repository

import com.crackmydroid.shared.domain.model.InstalledAppEntry
import com.crackmydroid.shared.domain.model.app.ManifestSecurityReport

interface InstalledAppsRepository {
    suspend fun listInstalled(): List<InstalledAppEntry>
    suspend fun listInstalledPaginated(offset: Int = 0, limit: Int = 50): List<InstalledAppEntry>
    suspend fun analyzeManifestSecurity(): List<ManifestSecurityReport>
    suspend fun analyzeManifestSecurityCached(): List<ManifestSecurityReport>
    suspend fun exportApk(packageName: String): Result<String>
    suspend fun shareApk(packageName: String, bluetoothOnly: Boolean): Result<Unit>
}