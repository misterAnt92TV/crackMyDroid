package com.crackmydroid.shared.presentation

import com.crackmydroid.shared.domain.model.ActivityEntry
import com.crackmydroid.shared.domain.model.ActivityIntentRiskLevel
import com.crackmydroid.shared.domain.model.ExportFormat
import com.crackmydroid.shared.domain.repository.ReportFileRepository
import com.crackmydroid.shared.domain.usecase.activities.GetActivitiesUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetActivityFavoritesUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetActivityPackageFavoritesUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetExportFormatUseCase
import com.crackmydroid.shared.domain.usecase.activities.LaunchActivityUseCase
import com.crackmydroid.shared.domain.usecase.reports.ExportReportUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetActivityFavoritesUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetActivityPackageFavoritesUseCase
import com.crackmydroid.shared.ui.activities.ActivityListViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class ActivityListViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private fun reportExporter() = ExportReportUseCase(mockk<ReportFileRepository>(relaxed = true))

    private fun activityViewModel(
        getActivities: GetActivitiesUseCase,
        launchActivity: LaunchActivityUseCase,
        getExportFormat: GetExportFormatUseCase
    ) = ActivityListViewModel(
        getActivities = getActivities,
        launchActivity = launchActivity,
        getExportFormat = getExportFormat,
        getFavorites = emptyActivityFavorites(),
        setFavorites = mockk<SetActivityFavoritesUseCase>(relaxed = true),
        getPackageFavorites = emptyPackageFavorites(),
        setPackageFavorites = mockk<SetActivityPackageFavoritesUseCase>(relaxed = true),
        reportExporter = reportExporter()
    )

    private fun emptyActivityFavorites(): GetActivityFavoritesUseCase {
        val getFavorites = mockk<GetActivityFavoritesUseCase>()
        coEvery { getFavorites() } returns emptySet()
        return getFavorites
    }

    private fun emptyPackageFavorites(): GetActivityPackageFavoritesUseCase {
        val getFavorites = mockk<GetActivityPackageFavoritesUseCase>()
        coEvery { getFavorites() } returns emptySet()
        return getFavorites
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun refreshLoadsActivitiesAndUpdatesState() = runTest(dispatcher) {
        val get = mockk<GetActivitiesUseCase>()
        val launch = mockk<LaunchActivityUseCase>()
        val format = mockk<GetExportFormatUseCase>()
        coEvery { get() } returns listOf(ActivityEntry("Label", "App", "pkg", "Act"))
        coEvery { format() } returns ExportFormat.TXT
        val vm = activityViewModel(get, launch, format)

        vm.refresh()
        advanceUntilIdle()

        assertEquals(1, vm.state.value.activities.size)
        coVerify { get() }
        vm.onCleared()
    }

    @Test
    fun setFilterUpdatesImmediatelyAndMatchesActivityName() = runTest(dispatcher) {
        val get = mockk<GetActivitiesUseCase>()
        val launch = mockk<LaunchActivityUseCase>()
        val format = mockk<GetExportFormatUseCase>()
        coEvery { get() } returns listOf(
            ActivityEntry(
                label = "Settings",
                appLabel = "Demo App",
                packageName = "com.demo.app",
                activityName = "com.demo.app.HiddenActivity"
            )
        )
        coEvery { format() } returns ExportFormat.TXT
        val vm = activityViewModel(get, launch, format)

        vm.refresh()
        advanceUntilIdle()
        vm.setFilter("Hidden")

        assertEquals("Hidden", vm.state.value.filter)
        advanceUntilIdle()
        assertEquals(1, vm.state.value.groups.size)
        vm.onCleared()
    }

    @Test
    fun setFilterTracksMostFrequentTopQuery() = runTest(dispatcher) {
        val get = mockk<GetActivitiesUseCase>()
        val launch = mockk<LaunchActivityUseCase>()
        val format = mockk<GetExportFormatUseCase>()
        coEvery { get() } returns listOf(ActivityEntry("Label", "App", "pkg", "Act"))
        coEvery { format() } returns ExportFormat.TXT
        val vm = activityViewModel(get, launch, format)

        vm.refresh()
        advanceUntilIdle()
        vm.setFilter("sec", trackUsage = true)
        vm.setFilter("api", trackUsage = true)
        vm.setFilter("sec", trackUsage = true)
        advanceUntilIdle()

        assertEquals("sec", vm.state.value.topQuery)
        vm.onCleared()
    }

    @Test
    fun launchWithIntentPassesGuidedArgumentsToUseCase() = runTest(dispatcher) {
        val get = mockk<GetActivitiesUseCase>()
        val launch = mockk<LaunchActivityUseCase>()
        val format = mockk<GetExportFormatUseCase>()
        val entry = ActivityEntry(
            label = "DeepLink",
            appLabel = "Demo App",
            packageName = "com.demo.app",
            activityName = "com.demo.app.DeepLinkActivity",
            launchableViaShell = false,
            launchIntentAction = "android.intent.action.VIEW",
            launchIntentData = "demo://host/details"
        )
        coEvery { get() } returns listOf(entry)
        coEvery {
            launch(
                entry,
                "android.intent.action.VIEW",
                "demo://host/details",
                null
            )
        } returns Result.success(Unit)
        coEvery { format() } returns ExportFormat.TXT
        val vm = activityViewModel(get, launch, format)

        vm.refresh()
        advanceUntilIdle()
        vm.launchWithIntent(entry, "android.intent.action.VIEW", "demo://host/details", null)
        advanceUntilIdle()

        assertNull(vm.state.value.error)
        coVerify {
            launch(
                entry,
                "android.intent.action.VIEW",
                "demo://host/details",
                null
            )
        }
        vm.onCleared()
    }

    @Test
    fun buildReportAggregatesSelectedAppAnalysis() = runTest(dispatcher) {
        val get = mockk<GetActivitiesUseCase>()
        val launch = mockk<LaunchActivityUseCase>()
        val format = mockk<GetExportFormatUseCase>()
        coEvery { format() } returns ExportFormat.TXT
        coEvery { get() } returns listOf(
            ActivityEntry(
                label = "Home",
                appLabel = "Demo App",
                packageName = "com.demo.app",
                activityName = "com.demo.app.HomeActivity"
            ),
            ActivityEntry(
                label = "DeepLink",
                appLabel = "Demo App",
                packageName = "com.demo.app",
                activityName = "com.demo.app.DeepLinkActivity",
                launchableViaShell = false,
                launchIntentAction = "android.intent.action.VIEW",
                launchIntentData = "demo://details"
            ),
            ActivityEntry(
                label = "Hidden",
                appLabel = "Demo App",
                packageName = "com.demo.app",
                activityName = "com.demo.app.HiddenActivity",
                launchableViaShell = false,
                launchabilityReason = "Resolver non esposto"
            ),
            ActivityEntry(
                label = "Other",
                appLabel = "Other App",
                packageName = "com.other.app",
                activityName = "com.other.app.MainActivity"
            )
        )
        val vm = activityViewModel(get, launch, format)

        vm.refresh()
        advanceUntilIdle()
        vm.toggleFavoritePackage("com.demo.app")
        vm.toggleFavorite(
            ActivityEntry(
                label = "Home",
                appLabel = "Demo App",
                packageName = "com.demo.app",
                activityName = "com.demo.app.HomeActivity"
            )
        )
        advanceUntilIdle()

        val report = vm.buildReport("com.demo.app")

        assertNotNull(report)
        assertEquals(3, report.totalActivities)
        assertEquals(1, report.shellLaunchableCount)
        assertEquals(1, report.guidedLaunchCount)
        assertEquals(1, report.dumpOnlyCount)
        assertEquals(2, report.riskyActivitiesCount)
        assertEquals(1, report.favoriteActivitiesCount)
        assertEquals(true, report.packageFavorite)
        assertEquals(ActivityIntentRiskLevel.High, report.highestRiskLevel)
        vm.onCleared()
    }
}
