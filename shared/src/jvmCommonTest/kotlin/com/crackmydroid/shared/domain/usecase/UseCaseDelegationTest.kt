package com.crackmydroid.shared.domain.usecase

import com.crackmydroid.shared.domain.model.DeviceInfo
import com.crackmydroid.shared.domain.model.ExportDocument
import com.crackmydroid.shared.domain.model.ExportFormat
import com.crackmydroid.shared.domain.model.InstalledAppEntry
import com.crackmydroid.shared.domain.model.PlayIntegrityResult
import com.crackmydroid.shared.domain.model.RootStatus
import com.crackmydroid.shared.domain.model.activity.ActivityEntry
import com.crackmydroid.shared.domain.model.app.AppPermissionEntry
import com.crackmydroid.shared.domain.model.root.RootCheckMode
import com.crackmydroid.shared.domain.model.shell.ShellCommand
import com.crackmydroid.shared.domain.model.shell.ShellCommandResult
import com.crackmydroid.shared.domain.repository.ActivityRepository
import com.crackmydroid.shared.domain.repository.DeviceInfoRepository
import com.crackmydroid.shared.domain.repository.InstalledAppsRepository
import com.crackmydroid.shared.domain.repository.PermissionsRepository
import com.crackmydroid.shared.domain.repository.ReportFileRepository
import com.crackmydroid.shared.domain.repository.RootCheckRepository
import com.crackmydroid.shared.domain.repository.ShellCommandRepository
import com.crackmydroid.shared.domain.repository.log.LogRepository
import com.crackmydroid.shared.domain.usecase.activities.GetActivitiesUseCase
import com.crackmydroid.shared.domain.usecase.activities.LaunchActivityUseCase
import com.crackmydroid.shared.domain.usecase.info.GetDeviceInfoUseCase
import com.crackmydroid.shared.domain.usecase.installedapps.ExportApkUseCase
import com.crackmydroid.shared.domain.usecase.installedapps.ListInstalledAppsUseCase
import com.crackmydroid.shared.domain.usecase.installedapps.ShareApkUseCase
import com.crackmydroid.shared.domain.usecase.log.ExportLogsUseCase
import com.crackmydroid.shared.domain.usecase.log.GetLogsUseCase
import com.crackmydroid.shared.domain.usecase.permissions.ListPermissionsUseCase
import com.crackmydroid.shared.domain.usecase.reports.ExportReportUseCase
import com.crackmydroid.shared.domain.usecase.root.CheckPlayIntegrityUseCase
import com.crackmydroid.shared.domain.usecase.root.CheckRootUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetExportFormatUseCase
import com.crackmydroid.shared.domain.usecase.trick.ExecuteShellCommandUseCase
import com.crackmydroid.shared.domain.usecase.trick.ListShellCommandsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest

class UseCaseDelegationTest {

    @Test
    fun getActivities_delegatesToRepository() = runTest {
        val repository = mockk<ActivityRepository>()
        val expected = listOf(
            ActivityEntry(
                label = "Act A",
                appLabel = "App A",
                packageName = "pkg.a",
                activityName = "A",
                exported = true,
            ),
            ActivityEntry(
                label = "Act B",
                appLabel = "App B",
                packageName = "pkg.b",
                activityName = "B",
                exported = false,
            ),
        )
        coEvery { repository.getActivities() } returns expected

        val useCase = GetActivitiesUseCase(repository)
        val result = useCase()

        assertEquals(expected, result.getOrThrow())
        coVerify(exactly = 1) { repository.getActivities() }
    }

    @Test
    fun listInstalledApps_delegatesToRepository() = runTest {
        val repository = mockk<InstalledAppsRepository>()
        val expected = listOf(
            InstalledAppEntry("App A", "pkg.a", "1.0", true),
            InstalledAppEntry("App B", "pkg.b", "2.0", false),
        )
        coEvery { repository.listInstalled() } returns expected

        val useCase = ListInstalledAppsUseCase(repository)
        val result = useCase()

        assertEquals(expected, result.getOrThrow())
        coVerify(exactly = 1) { repository.listInstalled() }
    }

    @Test
    fun executeShellCommand_delegatesToRepository() = runTest {
        val repository = mockk<ShellCommandRepository>()
        val command = ShellCommand(title = "List", command = "ls", requiresRoot = false)
        val expected = ShellCommandResult(success = true, stdout = "ok", stderr = "", code = 0)
        coEvery { repository.execute(command) } returns expected

        val useCase = ExecuteShellCommandUseCase(repository)
        val result = useCase(command)

        assertEquals(expected, result.getOrThrow())
        coVerify(exactly = 1) { repository.execute(command) }
    }

    @Test
    fun `activity launch forwards defaults and explicit intent parameters`() = runTest {
        val repository = mockk<ActivityRepository>()
        val entry = ActivityEntry("Demo", "App", "pkg", "pkg.Main")
        val useCase = LaunchActivityUseCase(repository)
        coEvery { repository.launch(entry, null, null, null) } returns Result.success(Unit)
        assertEquals(Unit, useCase(entry).getOrThrow())
        coVerify(exactly = 1) { repository.launch(entry, null, null, null) }

        val failure = IllegalStateException("Activity unavailable")
        coEvery { repository.launch(entry, "VIEW", "demo://item", "text/plain") } returns Result.failure(failure)
        assertSame(failure, useCase(entry, "VIEW", "demo://item", "text/plain").exceptionOrNull())
        coEvery { repository.launch(entry, null, null, null) } throws failure
        assertSame(failure, useCase(entry).exceptionOrNull())
    }

    @Test
    fun `device information returns repository data and propagates errors`() = runTest {
        val repository = mockk<DeviceInfoRepository>()
        val useCase = GetDeviceInfoUseCase(repository)
        val info = DeviceInfo(model = "Demo", batteryLevel = null, totalMemory = null, freeMemory = null)
        coEvery { repository.getDeviceInfo() } returns info
        assertSame(info, useCase().getOrThrow())
        coEvery { repository.getDeviceInfo() } throws IllegalStateException("Device unavailable")
        assertEquals("Device unavailable", useCase().exceptionOrNull()?.message)
    }

    @Test
    fun `list use cases preserve empty results and repository exceptions`() = runTest {
        val activities = mockk<ActivityRepository>()
        val apps = mockk<InstalledAppsRepository>()
        val logs = mockk<LogRepository>()
        val permissions = mockk<PermissionsRepository>()
        val commands = mockk<ShellCommandRepository>()
        coEvery { activities.getActivities() } returns emptyList()
        coEvery { apps.listInstalled() } returns emptyList()
        coEvery { logs.fetchLog() } returns emptyList()
        coEvery { permissions.listAppPermissions() } returns emptyList()
        coEvery { commands.listCommands() } returns emptyList()
        assertEquals(emptyList(), GetActivitiesUseCase(activities)().getOrThrow())
        assertEquals(emptyList(), ListInstalledAppsUseCase(apps)().getOrThrow())
        assertEquals(emptyList(), GetLogsUseCase(logs)().getOrThrow())
        assertEquals(emptyList(), ListPermissionsUseCase(permissions)().getOrThrow())
        assertEquals(emptyList(), ListShellCommandsUseCase(commands)().getOrThrow())

        val failure = IllegalStateException("Read failed")
        coEvery { activities.getActivities() } throws failure
        coEvery { apps.listInstalled() } throws failure
        coEvery { logs.fetchLog() } throws failure
        coEvery { permissions.listAppPermissions() } throws failure
        coEvery { commands.listCommands() } throws failure
        assertSame(failure, GetActivitiesUseCase(activities)().exceptionOrNull())
        assertSame(failure, ListInstalledAppsUseCase(apps)().exceptionOrNull())
        assertSame(failure, GetLogsUseCase(logs)().exceptionOrNull())
        assertSame(failure, ListPermissionsUseCase(permissions)().exceptionOrNull())
        assertSame(failure, ListShellCommandsUseCase(commands)().exceptionOrNull())
    }

    @Test
    fun `logs permissions and shell lists return complete repository values`() = runTest {
        val logs = mockk<LogRepository>()
        val permissions = mockk<PermissionsRepository>()
        val commands = mockk<ShellCommandRepository>()
        val logLines = listOf("first", "second", "first")
        val app = AppPermissionEntry("Demo", "pkg", listOf("CAMERA"))
        val command = ShellCommand("List", "ls", requiresRoot = false)
        coEvery { logs.fetchLog() } returns logLines
        coEvery { permissions.listAppPermissions() } returns listOf(app)
        coEvery { commands.listCommands() } returns listOf(command)
        assertEquals(logLines, GetLogsUseCase(logs)().getOrThrow())
        assertEquals(listOf(app), ListPermissionsUseCase(permissions)().getOrThrow())
        assertEquals(listOf(command), ListShellCommandsUseCase(commands)().getOrThrow())
    }

    @Test
    fun `APK and log exports forward inputs and convert thrown exceptions to failures`() = runTest {
        val apps = mockk<InstalledAppsRepository>()
        val reportFiles = mockk<ReportFileRepository>()
        val format = mockk<GetExportFormatUseCase>()
        val failure = IllegalStateException("Export failed")
        coEvery { apps.exportApk("pkg") } returns Result.failure(failure)
        coEvery { format() } returns Result.success(ExportFormat.TXT)
        coEvery { reportFiles.save(any<ExportDocument>()) } returns Result.success("/logs.txt")
        val exportLogs = ExportLogsUseCase(ExportReportUseCase(reportFiles, format))
        assertSame(failure, ExportApkUseCase(apps)("pkg").exceptionOrNull())
        assertEquals("/logs.txt", exportLogs(emptyList()).getOrThrow())
        val lines = listOf("line", "line")
        coEvery { reportFiles.save(any<ExportDocument>()) } returns Result.failure(failure)
        assertSame(failure, exportLogs(lines).exceptionOrNull())
        for (bluetooth in listOf(false, true)) {
            coEvery { apps.shareApk("pkg", bluetooth) } returns Result.failure(failure)
            assertSame(failure, ShareApkUseCase(apps)("pkg", bluetooth).exceptionOrNull())
            coEvery { apps.shareApk("pkg", bluetooth) } returns Result.success(Unit)
            assertEquals(Unit, ShareApkUseCase(apps)("pkg", bluetooth).getOrThrow())
            coVerify(exactly = 2) { apps.shareApk("pkg", bluetooth) }
        }
        coEvery { apps.exportApk("pkg") } throws failure
        coEvery { apps.shareApk("pkg", false) } throws failure
        coEvery { reportFiles.save(any<ExportDocument>()) } throws failure
        assertSame(failure, ExportApkUseCase(apps)("pkg").exceptionOrNull())
        assertSame(failure, ShareApkUseCase(apps)("pkg", false).exceptionOrNull())
        assertSame(failure, exportLogs(lines).exceptionOrNull())
    }

    @Test
    fun `result use cases propagate cancellation from repository failures`() = runTest {
        val repository = mockk<InstalledAppsRepository>()
        val cancellation = CancellationException("cancelled")
        coEvery { repository.exportApk("pkg") } returns Result.failure(cancellation)

        val failure = assertFailsWith<CancellationException> {
            ExportApkUseCase(repository)("pkg")
        }

        assertSame(cancellation, failure)
    }

    @Test
    fun `root checks forward default and privileged modes and propagate errors`() = runTest {
        val repository = mockk<RootCheckRepository>()
        val useCase = CheckRootUseCase(repository)
        val passive = RootStatus(false, "not rooted")
        val privileged = RootStatus(true, "access granted")
        coEvery { repository.checkRoot(RootCheckMode.PASSIVE) } returns passive
        coEvery { repository.checkRoot(RootCheckMode.PRIVILEGE_ACCESS) } returns privileged
        assertEquals(passive, useCase().getOrThrow())
        assertEquals(privileged, useCase(RootCheckMode.PRIVILEGE_ACCESS).getOrThrow())
        coEvery { repository.checkRoot(any()) } throws IllegalStateException("Root failed")
        assertEquals("Root failed", useCase().exceptionOrNull()?.message)
    }

    @Test
    fun `integrity checks forward default and explicit nonces including unavailable results`() = runTest {
        val repository = mockk<RootCheckRepository>()
        val useCase = CheckPlayIntegrityUseCase(repository)
        val available = PlayIntegrityResult(true, false, "checked")
        val unavailable = PlayIntegrityResult(details = "unavailable")
        coEvery { repository.checkPlayIntegrity("sample-nonce") } returns available
        coEvery { repository.checkPlayIntegrity("custom-nonce") } returns unavailable
        assertEquals(available, useCase().getOrThrow())
        assertEquals(unavailable, useCase("custom-nonce").getOrThrow())
        coEvery { repository.checkPlayIntegrity(any()) } throws IllegalStateException("Integrity failed")
        assertEquals("Integrity failed", useCase().exceptionOrNull()?.message)
    }

    @Test
    fun `shell execution preserves unsuccessful output and thrown errors`() = runTest {
        val repository = mockk<ShellCommandRepository>()
        val command = ShellCommand("Root", "id", requiresRoot = true)
        val failureResult = ShellCommandResult(false, "", "denied", 1)
        coEvery { repository.execute(command) } returns failureResult
        assertEquals(failureResult, ExecuteShellCommandUseCase(repository)(command).getOrThrow())
        coEvery { repository.execute(command) } throws IllegalStateException("Shell unavailable")
        assertEquals(
            "Shell unavailable",
            ExecuteShellCommandUseCase(repository)(command).exceptionOrNull()?.message,
        )
    }
}
