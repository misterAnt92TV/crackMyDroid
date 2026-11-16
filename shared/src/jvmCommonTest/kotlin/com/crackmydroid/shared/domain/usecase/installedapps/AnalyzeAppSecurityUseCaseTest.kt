package com.crackmydroid.shared.domain.usecase.installedapps

import com.crackmydroid.shared.domain.model.InstalledAppEntry
import com.crackmydroid.shared.domain.model.app.AppManifestSecurity
import com.crackmydroid.shared.domain.model.app.ManifestRiskLevel
import com.crackmydroid.shared.domain.model.app.ManifestSecurityReport
import com.crackmydroid.shared.domain.repository.InstalledAppsRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AnalyzeAppSecurityUseCaseTest {
    private class FakeInstalledAppsRepository : InstalledAppsRepository {
        var requestedPackage: String? = null
        var shouldFail = false

        override suspend fun listInstalled(): List<InstalledAppEntry> = emptyList()
        override suspend fun listInstalledPaginated(offset: Int, limit: Int): List<InstalledAppEntry> = emptyList()
        override suspend fun analyzeManifestSecurity(): List<ManifestSecurityReport> = emptyList()
        override suspend fun analyzeManifestSecurityCached(): List<ManifestSecurityReport> = emptyList()

        override suspend fun analyzePackageManifestSecurity(packageName: String): Result<ManifestSecurityReport> {
            requestedPackage = packageName
            if (shouldFail) return Result.failure(IllegalStateException("App non trovata"))
            return Result.success(
                ManifestSecurityReport(
                    info = AppManifestSecurity(
                        appLabel = "CrackMyDroid",
                        packageName = packageName,
                        debuggable = false,
                        allowBackup = true,
                    ),
                    level = ManifestRiskLevel.High,
                    score = 3,
                    findings = emptyList(),
                ),
            )
        }

        override suspend fun exportApk(packageName: String): Result<String> = Result.success("/tmp/test.apk")
        override suspend fun shareApk(packageName: String, bluetoothOnly: Boolean): Result<Unit> = Result.success(Unit)
    }

    @Test
    fun returnsManifestSecurityReportForPackage() = runTest {
        val repo = FakeInstalledAppsRepository()
        val useCase = AnalyzeAppSecurityUseCase(repo)

        val result = useCase("com.crackmydroid")

        assertTrue(result.isSuccess)
        assertEquals("com.crackmydroid", repo.requestedPackage)
        val report = result.getOrThrow()
        assertEquals("CrackMyDroid", report.info.appLabel)
        assertEquals(ManifestRiskLevel.High, report.level)
    }

    @Test
    fun propagatesFailureWhenPackageNotFound() = runTest {
        val repo = FakeInstalledAppsRepository().apply { shouldFail = true }
        val useCase = AnalyzeAppSecurityUseCase(repo)

        val result = useCase("com.unknown")

        assertTrue(result.isFailure)
        assertEquals("App non trovata", result.exceptionOrNull()?.message)
    }
}
