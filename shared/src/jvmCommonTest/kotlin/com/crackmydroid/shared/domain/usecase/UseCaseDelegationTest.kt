package com.crackmydroid.shared.domain.usecase

import com.crackmydroid.shared.domain.model.ActivityEntry
import com.crackmydroid.shared.domain.model.InstalledAppEntry
import com.crackmydroid.shared.domain.model.ShellCommand
import com.crackmydroid.shared.domain.model.ShellCommandResult
import com.crackmydroid.shared.domain.repository.ActivityRepository
import com.crackmydroid.shared.domain.repository.InstalledAppsRepository
import com.crackmydroid.shared.domain.repository.ShellCommandRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

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
}
