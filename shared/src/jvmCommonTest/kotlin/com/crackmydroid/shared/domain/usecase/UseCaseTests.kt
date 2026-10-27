package com.crackmydroid.shared.domain.usecase

import com.crackmydroid.shared.domain.model.ActivityEntry
import com.crackmydroid.shared.domain.model.InstalledAppEntry
import com.crackmydroid.shared.domain.model.RootStatus
import com.crackmydroid.shared.domain.repository.ActivityRepository
import com.crackmydroid.shared.domain.repository.InstalledAppsRepository
import com.crackmydroid.shared.domain.repository.RootCheckRepository
import com.crackmydroid.shared.domain.repository.ReportFileRepository
import com.crackmydroid.shared.domain.usecase.reports.ExportReportUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UseCaseTests {
    @Test
    fun getActivitiesDelegatesToRepo() = runTest {
        val repo = mockk<ActivityRepository>()
        coEvery { repo.getActivities() } returns listOf(ActivityEntry("A", "App", "pkg", "Act"))
        val uc = GetActivitiesUseCase(repo)

        val result = uc()

        assertEquals(1, result.size)
        coVerify { repo.getActivities() }
    }

    @Test
    fun checkRootReturnsRepoValue() = runTest {
        val repo = mockk<RootCheckRepository>()
        coEvery { repo.checkRoot() } returns RootStatus(true, "root")
        val uc = CheckRootUseCase(repo)

        val result = uc()

        assertTrue(result.isRooted)
        coVerify { repo.checkRoot() }
    }

    @Test
    fun listInstalledAppsDelegatesToRepo() = runTest {
        val repo = mockk<InstalledAppsRepository>()
        coEvery { repo.listInstalled() } returns listOf(InstalledAppEntry("App", "pkg", "/path"))
        val uc = ListInstalledAppsUseCase(repo)

        val result = uc()

        assertEquals(1, result.size)
        coVerify { repo.listInstalled() }
    }

    @Test
    fun exportApkDelegatesToRepo() = runTest {
        val repo = mockk<InstalledAppsRepository>()
        coEvery { repo.exportApk("pkg") } returns Result.success("/tmp/pkg.apk")
        val uc = ExportApkUseCase(repo)

        val result = uc("pkg")

        assertTrue(result.isSuccess)
        coVerify { repo.exportApk("pkg") }
    }

    @Test
    fun shareApkDelegatesToRepo() = runTest {
        val repo = mockk<InstalledAppsRepository>()
        coEvery { repo.shareApk("pkg", true) } returns Result.success(Unit)
        val uc = ShareApkUseCase(repo)

        val result = uc("pkg", true)

        assertTrue(result.isSuccess)
        coVerify { repo.shareApk("pkg", true) }
    }

    @Test
    fun shareReportSavesThenSharesTheGeneratedPath() = runTest {
        val repo = mockk<ReportFileRepository>()
        coEvery { repo.save("report.txt", "content") } returns Result.success("/tmp/report.txt")
        coEvery { repo.share("/tmp/report.txt") } returns Result.success(Unit)
        val useCase = ExportReportUseCase(repo)

        val result = useCase.share("report.txt", "content")

        assertEquals("/tmp/report.txt", result.getOrNull())
        coVerifyOrder {
            repo.save("report.txt", "content")
            repo.share("/tmp/report.txt")
        }
    }
}
