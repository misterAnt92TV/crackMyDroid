package com.crackmydroid.shared.domain.usecase

import com.crackmydroid.shared.domain.model.activity.ActivityEntry
import com.crackmydroid.shared.domain.model.InstalledAppEntry
import com.crackmydroid.shared.domain.model.DeviceInfo
import com.crackmydroid.shared.domain.model.PlayIntegrityResult
import com.crackmydroid.shared.domain.model.RootStatus
import com.crackmydroid.shared.domain.model.app.AppPermissionEntry
import com.crackmydroid.shared.domain.model.root.RootCheckMode
import com.crackmydroid.shared.domain.model.shell.ShellCommand
import com.crackmydroid.shared.domain.model.shell.ShellCommandResult
import com.crackmydroid.shared.domain.repository.ActivityRepository
import com.crackmydroid.shared.domain.repository.InstalledAppsRepository
import com.crackmydroid.shared.domain.repository.ShellCommandRepository
import com.crackmydroid.shared.domain.repository.DeviceInfoRepository
import com.crackmydroid.shared.domain.repository.PermissionsRepository
import com.crackmydroid.shared.domain.repository.RootCheckRepository
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
import com.crackmydroid.shared.domain.usecase.root.CheckPlayIntegrityUseCase
import com.crackmydroid.shared.domain.usecase.root.CheckRootUseCase
import com.crackmydroid.shared.domain.usecase.trick.ExecuteShellCommandUseCase
import com.crackmydroid.shared.domain.usecase.trick.ListShellCommandsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class UseCaseDelegationTest {

    @Test
    fun getActivities_delegatesToRepository() = runTest {
        val repository = mockk<ActivityRepository>()
        val expected = listOf(
            ActivityEntry(label = "Act A", appLabel = "App A", packageName = "pkg.a", activityName = "A", exported = true),
            ActivityEntry(label = "Act B", appLabel = "App B", packageName = "pkg.b", activityName = "B", exported = false)
        )
        coEvery { repository.getActivities() } returns expected

        val useCase = GetActivitiesUseCase(repository)
        val result = useCase()

        assertEquals(expected, result)
        coVerify(exactly = 1) { repository.getActivities() }
    }

    @Test
    fun listInstalledApps_delegatesToRepository() = runTest {
        val repository = mockk<InstalledAppsRepository>()
        val expected = listOf(
            InstalledAppEntry("App A", "pkg.a", "1.0", true),
            InstalledAppEntry("App B", "pkg.b", "2.0", false)
        )
        coEvery { repository.listInstalled() } returns expected

        val useCase = ListInstalledAppsUseCase(repository)
        val result = useCase()

        assertEquals(expected, result)
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

        assertEquals(expected, result)
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
        assertSame(failure, assertFailsWith<IllegalStateException> { useCase(entry) })
    }

    @Test
    fun `device information returns repository data and propagates errors`() = runTest {
        val repository = mockk<DeviceInfoRepository>()
        val useCase = GetDeviceInfoUseCase(repository)
        val info = DeviceInfo(model = "Demo", batteryLevel = null, totalMemory = null, freeMemory = null)
        coEvery { repository.getDeviceInfo() } returns info
        assertSame(info, useCase())
        coEvery { repository.getDeviceInfo() } throws IllegalStateException("Device unavailable")
        assertEquals("Device unavailable", assertFailsWith<IllegalStateException> { useCase() }.message)
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
        assertEquals(emptyList(), GetActivitiesUseCase(activities)())
        assertEquals(emptyList(), ListInstalledAppsUseCase(apps)())
        assertEquals(emptyList(), GetLogsUseCase(logs)())
        assertEquals(emptyList(), ListPermissionsUseCase(permissions)())
        assertEquals(emptyList(), ListShellCommandsUseCase(commands)())

        val failure = IllegalStateException("Read failed")
        coEvery { activities.getActivities() } throws failure
        coEvery { apps.listInstalled() } throws failure
        coEvery { logs.fetchLog() } throws failure
        coEvery { permissions.listAppPermissions() } throws failure
        coEvery { commands.listCommands() } throws failure
        assertSame(failure, assertFailsWith<IllegalStateException> { GetActivitiesUseCase(activities)() })
        assertSame(failure, assertFailsWith<IllegalStateException> { ListInstalledAppsUseCase(apps)() })
        assertSame(failure, assertFailsWith<IllegalStateException> { GetLogsUseCase(logs)() })
        assertSame(failure, assertFailsWith<IllegalStateException> { ListPermissionsUseCase(permissions)() })
        assertSame(failure, assertFailsWith<IllegalStateException> { ListShellCommandsUseCase(commands)() })
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
        assertEquals(logLines, GetLogsUseCase(logs)())
        assertEquals(listOf(app), ListPermissionsUseCase(permissions)())
        assertEquals(listOf(command), ListShellCommandsUseCase(commands)())
    }

    @Test
    fun `APK and log exports forward inputs and preserve failure results and exceptions`() = runTest {
        val apps = mockk<InstalledAppsRepository>()
        val logs = mockk<LogRepository>()
        val failure = IllegalStateException("Export failed")
        coEvery { apps.exportApk("pkg") } returns Result.failure(failure)
        coEvery { logs.export(emptyList()) } returns Result.success("/logs.txt")
        assertSame(failure, ExportApkUseCase(apps)("pkg").exceptionOrNull())
        assertEquals("/logs.txt", ExportLogsUseCase(logs)(emptyList()).getOrThrow())
        val lines = listOf("line", "line")
        coEvery { logs.export(lines) } returns Result.failure(failure)
        assertSame(failure, ExportLogsUseCase(logs)(lines).exceptionOrNull())
        for (bluetooth in listOf(false, true)) {
            coEvery { apps.shareApk("pkg", bluetooth) } returns Result.failure(failure)
            assertSame(failure, ShareApkUseCase(apps)("pkg", bluetooth).exceptionOrNull())
            coEvery { apps.shareApk("pkg", bluetooth) } returns Result.success(Unit)
            assertEquals(Unit, ShareApkUseCase(apps)("pkg", bluetooth).getOrThrow())
            coVerify(exactly = 2) { apps.shareApk("pkg", bluetooth) }
        }
        coEvery { apps.exportApk("pkg") } throws failure
        coEvery { apps.shareApk("pkg", false) } throws failure
        coEvery { logs.export(lines) } throws failure
        assertSame(failure, assertFailsWith<IllegalStateException> { ExportApkUseCase(apps)("pkg") })
        assertSame(failure, assertFailsWith<IllegalStateException> { ShareApkUseCase(apps)("pkg", false) })
        assertSame(failure, assertFailsWith<IllegalStateException> { ExportLogsUseCase(logs)(lines) })
    }

    @Test
    fun `root checks forward default and privileged modes and propagate errors`() = runTest {
        val repository = mockk<RootCheckRepository>()
        val useCase = CheckRootUseCase(repository)
        val passive = RootStatus(false, "not rooted")
        val privileged = RootStatus(true, "access granted")
        coEvery { repository.checkRoot(RootCheckMode.PASSIVE) } returns passive
        coEvery { repository.checkRoot(RootCheckMode.PRIVILEGE_ACCESS) } returns privileged
        assertEquals(passive, useCase())
        assertEquals(privileged, useCase(RootCheckMode.PRIVILEGE_ACCESS))
        coEvery { repository.checkRoot(any()) } throws IllegalStateException("Root failed")
        assertEquals("Root failed", assertFailsWith<IllegalStateException> { useCase() }.message)
    }

    @Test
    fun `integrity checks forward default and explicit nonces including unavailable results`() = runTest {
        val repository = mockk<RootCheckRepository>()
        val useCase = CheckPlayIntegrityUseCase(repository)
        val available = PlayIntegrityResult(true, false, "checked")
        val unavailable = PlayIntegrityResult(details = "unavailable")
        coEvery { repository.checkPlayIntegrity("sample-nonce") } returns available
        coEvery { repository.checkPlayIntegrity("custom-nonce") } returns unavailable
        assertEquals(available, useCase())
        assertEquals(unavailable, useCase("custom-nonce"))
        coEvery { repository.checkPlayIntegrity(any()) } throws IllegalStateException("Integrity failed")
        assertEquals("Integrity failed", assertFailsWith<IllegalStateException> { useCase() }.message)
    }

    @Test
    fun `shell execution preserves unsuccessful output and thrown errors`() = runTest {
        val repository = mockk<ShellCommandRepository>()
        val command = ShellCommand("Root", "id", requiresRoot = true)
        val failureResult = ShellCommandResult(false, "", "denied", 1)
        coEvery { repository.execute(command) } returns failureResult
        assertEquals(failureResult, ExecuteShellCommandUseCase(repository)(command))
        coEvery { repository.execute(command) } throws IllegalStateException("Shell unavailable")
        assertEquals(
            "Shell unavailable",
            assertFailsWith<IllegalStateException> { ExecuteShellCommandUseCase(repository)(command) }.message
        )
    }
}
