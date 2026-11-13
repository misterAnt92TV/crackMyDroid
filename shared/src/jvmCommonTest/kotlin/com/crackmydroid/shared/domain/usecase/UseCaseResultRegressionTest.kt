package com.crackmydroid.shared.domain.usecase

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.StaticConfig
import com.crackmydroid.shared.domain.model.DeviceInfo
import com.crackmydroid.shared.domain.model.activity.ActivityEntry
import com.crackmydroid.shared.domain.model.activity.ActivityIntentRiskLevel
import com.crackmydroid.shared.domain.model.activity.ActivityIntentRiskReport
import com.crackmydroid.shared.domain.model.app.AppPermissionEntry
import com.crackmydroid.shared.domain.repository.ActivityRepository
import com.crackmydroid.shared.domain.repository.InstalledAppsRepository
import com.crackmydroid.shared.domain.repository.SettingsRepository
import com.crackmydroid.shared.domain.usecase.activities.GetActivitiesUseCase
import com.crackmydroid.shared.domain.usecase.info.BuildDeviceInfoReportUseCase
import com.crackmydroid.shared.domain.usecase.installedapps.ExportApkUseCase
import com.crackmydroid.shared.domain.usecase.pentest.PenTestCheckStatus
import com.crackmydroid.shared.domain.usecase.pentest.RunPenTestChecksUseCase
import com.crackmydroid.shared.domain.usecase.permissions.AnalyzePermissionRisksUseCase
import com.crackmydroid.shared.domain.usecase.permissions.BuildPermissionsReportUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetVerboseLogUseCase
import com.crackmydroid.shared.i18n.usecase.UseCaseTexts
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.test.runTest

class UseCaseResultRegressionTest {
    @Test
    fun `concrete use case failure log retains its class identifier`() = runTest {
        val messages = mutableListOf<String>()
        val writer = object : LogWriter() {
            override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
                assertEquals(null, throwable)
                if (severity == Severity.Error) messages += message
            }
        }
        val logger = Logger(StaticConfig(logWriterList = listOf(writer)), "UseCaseTest")
        val repository = mockk<ActivityRepository>()
        val failure = IllegalStateException("unavailable")
        coEvery { repository.getActivities() } throws failure

        val result = GetActivitiesUseCase(repository, logger = logger)()

        assertSame(failure, result.exceptionOrNull())
        assertEquals(1, messages.count { it.contains("outcome=failed") })
        assertTrue(
            messages.single { it.contains("outcome=failed") }
                .startsWith("useCase=GetActivitiesUseCase operation=invoke "),
        )
    }

    @Test
    fun `repository values and Unit successes are not nested results`() = runTest {
        val apps = mockk<InstalledAppsRepository>()
        val settings = mockk<SettingsRepository>()
        coEvery { apps.exportApk("pkg") } returns Result.success("/pkg.apk")
        coEvery { settings.setVerboseLog(true) } returns Unit
        val exported: Result<String> = ExportApkUseCase(apps)("pkg")
        val written: Result<Unit> = SetVerboseLogUseCase(settings)(true)
        assertEquals("/pkg.apk", exported.getOrThrow())
        assertEquals(Unit, written.getOrThrow())
    }

    @Test
    fun `list and setter operations rethrow repository cancellation`() = runTest {
        val activities = mockk<ActivityRepository>()
        val settings = mockk<SettingsRepository>()
        val cancellation = CancellationException("cancelled")
        coEvery { activities.getActivities() } throws cancellation
        coEvery { settings.setVerboseLog(false) } throws cancellation
        assertSame(
            cancellation,
            assertFailsWith<CancellationException> {
                GetActivitiesUseCase(activities)()
            },
        )
        assertSame(
            cancellation,
            assertFailsWith<CancellationException> {
                SetVerboseLogUseCase(settings)(false)
            },
        )
    }

    @Test
    fun `composed analysis preserves technical failures and cancellations`() = runTest {
        val dispatchers = UseCaseTestDispatchers(
            kotlinx.coroutines.test.StandardTestDispatcher(testScheduler),
        )
        val analyzer = mockk<AnalyzePermissionRisksUseCase>()
        val useCase = RunPenTestChecksUseCase(analyzer, dispatchers)
        val app = AppPermissionEntry("App", "pkg", listOf("CAMERA"))
        val failure = object : Exception("analysis failed") {}
        coEvery { analyzer(app) } returns Result.failure(failure)
        assertSame(failure, useCase("permission_risk", app, null, emptyList()).exceptionOrNull())
        coEvery { analyzer(app) } throws failure
        assertSame(failure, useCase("permission_risk", app, null, emptyList()).exceptionOrNull())
        val cancellation = object : CancellationException("cancelled") {}
        coEvery { analyzer(app) } returns Result.failure(cancellation)
        assertSame(
            cancellation,
            assertFailsWith<CancellationException> {
                useCase("permission_risk", app, null, emptyList())
            },
        )
        coEvery { analyzer(app) } throws cancellation
        assertSame(
            cancellation,
            assertFailsWith<CancellationException> {
                useCase("permission_risk", app, null, emptyList())
            },
        )
        coEvery { analyzer(app) } returns Result.success(null)
        assertEquals(
            PenTestCheckStatus.Success,
            useCase("permission_risk", app, null, emptyList()).getOrThrow().status,
        )
    }

    @Test
    fun `analysis cooperatively stops iterating after cancellation`() = runTest {
        val dispatchers = UseCaseTestDispatchers(
            kotlinx.coroutines.test.StandardTestDispatcher(testScheduler),
        )
        var reads = 0
        val work = async {
            val job = currentCoroutineContext()[Job]!!
            val permissions = object : AbstractList<String>() {
                override val size = 10_000
                override fun get(index: Int): String {
                    reads++
                    if (index == 25) job.cancel()
                    return "CAMERA"
                }
            }
            AnalyzePermissionRisksUseCase(dispatchers)(
                AppPermissionEntry("App", "pkg", permissions),
            )
        }
        assertFailsWith<CancellationException> { work.await() }
        assertTrue(reads in 1..27, "CPU analysis must stop before reading the full list")
    }

    @Test
    fun `report generation cooperatively stops iterating after cancellation`() = runTest {
        val dispatchers = UseCaseTestDispatchers(
            kotlinx.coroutines.test.StandardTestDispatcher(testScheduler),
        )
        var reads = 0
        val work = async {
            val job = currentCoroutineContext()[Job]!!
            val apps = object : AbstractList<AppPermissionEntry>() {
                override val size = 10_000
                override fun get(index: Int): AppPermissionEntry {
                    reads++
                    if (index == 25) job.cancel()
                    return AppPermissionEntry("App $index", "pkg.$index", emptyList())
                }
            }
            BuildPermissionsReportUseCase(AnalyzePermissionRisksUseCase(dispatchers), dispatchers)(
                apps,
                emptyList(),
                UseCaseTexts("en"),
            )
        }
        assertFailsWith<CancellationException> { work.await() }
        assertTrue(reads < 10_000, "CPU report must stop before reading the full list")
    }

    @Test
    fun `pentest cooperatively stops scanning activity risks after cancellation`() = runTest {
        val dispatchers = UseCaseTestDispatchers(
            kotlinx.coroutines.test.StandardTestDispatcher(testScheduler),
        )
        var reads = 0
        val work = async {
            val job = currentCoroutineContext()[Job]!!
            val risks = object : AbstractList<ActivityIntentRiskReport>() {
                override val size = 10_000
                override fun get(index: Int): ActivityIntentRiskReport {
                    reads++
                    if (index == 25) job.cancel()
                    return ActivityIntentRiskReport(
                        entry = ActivityEntry("Activity", "App", "pkg", "pkg.Main"),
                        level = ActivityIntentRiskLevel.High,
                        score = 3,
                        launchMode = "guided",
                        recommendation = "review",
                        findings = emptyList(),
                    )
                }
            }
            RunPenTestChecksUseCase(AnalyzePermissionRisksUseCase(dispatchers), dispatchers)(
                "manifest_flags",
                null,
                null,
                risks,
            )
        }
        assertFailsWith<CancellationException> { work.await() }
        assertTrue(reads in 1..27, "CPU pentest must stop before reading the full list")
    }

    @Test
    fun `device report preserves optional fields and root output`() = runTest {
        val dispatchers = UseCaseTestDispatchers(
            kotlinx.coroutines.test.StandardTestDispatcher(testScheduler),
        )
        val info = DeviceInfo(model = "Demo", batteryLevel = null, totalMemory = null, freeMemory = null)
        val useCase = BuildDeviceInfoReportUseCase(dispatchers)
        val text = UseCaseTexts("en")
        val report = useCase(info, null, text).getOrThrow()
        assertTrue(report.startsWith("Device information\n"))
        assertTrue(report.contains("Model: Demo\n"))
        assertTrue(report.contains("Battery: -1%\n"))
        assertTrue(!report.contains("Rooted:"))
        val rooted = useCase(
            info,
            com.crackmydroid.shared.domain.model.RootStatus(false, "unavailable"),
            text,
        ).getOrThrow()
        assertTrue(rooted.contains("Rooted: No\nRoot details: unavailable\n"))
    }
}
