package com.crackmydroid.shared.domain.repository

import com.crackmydroid.shared.domain.model.InstalledAppEntry

interface RemotePackageManager {
    suspend fun listInstalledApps(): List<InstalledAppEntry>
    suspend fun readPackageDump(packageName: String): String
    suspend fun readPackagePaths(packageName: String): List<String>
}