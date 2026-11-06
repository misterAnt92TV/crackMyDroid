package com.crackmydroid.shared.presentation

import com.crackmydroid.shared.domain.model.app.AppPermissionEntry
import com.crackmydroid.shared.domain.repository.ReportFileRepository
import com.crackmydroid.shared.domain.usecase.permissions.ListPermissionsUseCase
import com.crackmydroid.shared.domain.usecase.permissions.AnalyzePermissionRisksUseCase
import com.crackmydroid.shared.domain.usecase.permissions.BuildPermissionsReportUseCase
import com.crackmydroid.shared.domain.usecase.reports.ExportReportUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetPermissionFavoritesUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetPermissionFavoritesUseCase
import com.crackmydroid.shared.ui.permissions.PermissionsViewModel
import com.crackmydroid.shared.domain.model.UseCaseReportText
import com.crackmydroid.shared.i18n.UseCaseTexts
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class PermissionsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private fun reportExporter() = ExportReportUseCase(mockk<ReportFileRepository>(relaxed = true))

    private fun emptyFavorites(): GetPermissionFavoritesUseCase {
        val getFavorites = mockk<GetPermissionFavoritesUseCase>()
        coEvery { getFavorites() } returns Result.success(emptySet())
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
    fun setFilterTracksMostFrequentTopQuery() = runTest(dispatcher) {
        val listPermissions = mockk<ListPermissionsUseCase>()
        coEvery { listPermissions() } returns Result.success(listOf(
            AppPermissionEntry("Demo", "pkg.demo", listOf("android.permission.INTERNET"))
        ))
        val analyzePermissionRisks = AnalyzePermissionRisksUseCase(dispatchers = TestAppDispatchers(dispatcher))
        val buildPermissionsReport = BuildPermissionsReportUseCase(analyzePermissionRisks, dispatchers = TestAppDispatchers(dispatcher))
        val vm = PermissionsViewModel(
            listPermissions,
            analyzePermissionRisks,
            buildPermissionsReport,
            reportExporter(),
            emptyFavorites(),
            mockk(relaxed = true),
            TestAppDispatchers(dispatcher)
        )

        vm.refresh()
        advanceUntilIdle()
        vm.setFilter("sms", trackUsage = true)
        vm.setFilter("cam", trackUsage = true)
        vm.setFilter("sms", trackUsage = true)

        assertEquals("sms", vm.state.value.topQuery)
        vm.onCleared()
    }

    @Test
    fun showOnlyRiskyFiltersOutSafeAppsAfterScan() = runTest(dispatcher) {
        val listPermissions = mockk<ListPermissionsUseCase>()
        coEvery { listPermissions() } returns Result.success(listOf(
            AppPermissionEntry("Safe App", "pkg.safe", emptyList()),
            AppPermissionEntry(
                "Risky App",
                "pkg.risky",
                listOf("android.permission.SYSTEM_ALERT_WINDOW")
            )
        ))
        val analyzePermissionRisks = AnalyzePermissionRisksUseCase(dispatchers = TestAppDispatchers(dispatcher))
        val buildPermissionsReport = BuildPermissionsReportUseCase(analyzePermissionRisks, dispatchers = TestAppDispatchers(dispatcher))
        val vm = PermissionsViewModel(
            listPermissions,
            analyzePermissionRisks,
            buildPermissionsReport,
            reportExporter(),
            emptyFavorites(),
            mockk(relaxed = true),
            TestAppDispatchers(dispatcher)
        )

        vm.refresh()
        advanceUntilIdle()
        vm.startAutoRiskScan()
        advanceUntilIdle()
        vm.setShowOnlyRisky(true)

        val filtered = vm.state.value.visibleApps
        assertEquals(1, filtered.size)
        assertEquals("pkg.risky", filtered.first().packageName)
        vm.onCleared()
    }

    @Test
    fun refreshLoadsFavoritesAndTogglePersistsThem() = runTest(dispatcher) {
        val listPermissions = mockk<ListPermissionsUseCase>()
        val getFavorites = mockk<GetPermissionFavoritesUseCase>()
        val setFavorites = mockk<SetPermissionFavoritesUseCase>()
        coEvery { listPermissions() } returns Result.success(emptyList())
        coEvery { getFavorites() } returns Result.success(setOf("pkg.favorite"))
        coEvery { setFavorites(any()) } returns Result.success(Unit)
        val analyzePermissionRisks = AnalyzePermissionRisksUseCase(dispatchers = TestAppDispatchers(dispatcher))
        val vm = PermissionsViewModel(
            listPermissions,
            analyzePermissionRisks,
            BuildPermissionsReportUseCase(analyzePermissionRisks, dispatchers = TestAppDispatchers(dispatcher)),
            reportExporter(),
            getFavorites,
            setFavorites,
            TestAppDispatchers(dispatcher)
        )

        vm.refresh()
        advanceUntilIdle()
        vm.toggleFavorite("pkg.favorite")
        advanceUntilIdle()

        assertEquals(emptySet(), vm.state.value.favorites)
        coVerify { setFavorites(emptySet()) }
        vm.onCleared()
    }

    @Test
    fun previewIsAsynchronousAndSharingReusesTheGeneratedContent() = runTest(dispatcher) {
        val builder = mockk<BuildPermissionsReportUseCase>()
        val files = mockk<ReportFileRepository>()
        coEvery { builder.single(any(), any(), any()) } returns Result.success("generated content")
        coEvery { files.save(any(), any()) } returns Result.success("/tmp/report")
        coEvery { files.share("/tmp/report") } returns Result.success(Unit)
        io.mockk.every { builder.singleFileName("pkg.first") } returns "first.txt"
        val vm = previewViewModel(builder, ExportReportUseCase(files))
        vm.refresh()
        advanceUntilIdle()

        vm.requestReportPreview("pkg.first")
        assertTrue(vm.state.value.reportPreviewLoading)
        assertNull(vm.state.value.reportPreviewContent)
        advanceUntilIdle()
        assertEquals("generated content", vm.state.value.reportPreviewContent)
        assertEquals(false, vm.state.value.reportPreviewLoading)

        vm.shareSingle("pkg.first", vm.state.value.reportPreviewContent)
        advanceUntilIdle()
        coVerify(exactly = 1) { builder.single(any(), any(), any()) }
        coVerify { files.save("first.txt", "generated content") }
        vm.onCleared()
    }

    @Test
    fun obsoletePreviewIsCancelledWhenAnotherPackageIsSelected() = runTest(dispatcher) {
        val builder = mockk<BuildPermissionsReportUseCase>()
        var firstCancelled = false
        coEvery { builder.single(match { it.packageName == "pkg.first" }, any(), any()) } coAnswers {
            try {
                delay(1_000)
                Result.success("first")
            } finally {
                firstCancelled = true
            }
        }
        coEvery { builder.single(match { it.packageName == "pkg.second" }, any(), any()) } returns Result.success("second")
        val vm = previewViewModel(builder)
        vm.refresh()
        advanceUntilIdle()
        vm.requestReportPreview("pkg.first")
        runCurrent()
        vm.requestReportPreview("pkg.second")
        advanceUntilIdle()

        assertTrue(firstCancelled)
        assertEquals("pkg.second", vm.state.value.reportPreviewPackage)
        assertEquals("second", vm.state.value.reportPreviewContent)
        assertNull(vm.state.value.reportPreviewError)
        vm.onCleared()
    }

    @Test
    fun dismissCancelsPreviewWithoutErrorOrLateContent() = runTest(dispatcher) {
        val builder = mockk<BuildPermissionsReportUseCase>()
        coEvery { builder.single(any(), any(), any()) } coAnswers {
            delay(1_000)
            Result.success("late")
        }
        val vm = previewViewModel(builder)
        vm.refresh()
        advanceUntilIdle()
        vm.requestReportPreview("pkg.first")
        runCurrent()
        vm.dismissReportPreview()
        advanceUntilIdle()

        assertNull(vm.state.value.reportPreviewPackage)
        assertNull(vm.state.value.reportPreviewContent)
        assertNull(vm.state.value.reportPreviewError)
        assertEquals(false, vm.state.value.reportPreviewLoading)
        vm.onCleared()
    }

    @Test
    fun previewFailureIsNotReportedAsGeneratedContent() = runTest(dispatcher) {
        val builder = mockk<BuildPermissionsReportUseCase>()
        coEvery { builder.single(any(), any(), any()) } returns Result.failure(IllegalStateException("report failed"))
        val vm = previewViewModel(builder)
        vm.refresh()
        advanceUntilIdle()
        vm.requestReportPreview("pkg.first")
        advanceUntilIdle()

        assertNull(vm.state.value.reportPreviewContent)
        assertEquals("report failed", vm.state.value.reportPreviewError)
        assertEquals(false, vm.state.value.reportPreviewLoading)
        vm.onCleared()
    }

    @Test
    fun scanFailureStopsProgressWithoutTurningItIntoAnEmptySuccess() = runTest(dispatcher) {
        val analyzer = mockk<AnalyzePermissionRisksUseCase>()
        coEvery { analyzer(any()) } returns Result.failure(IllegalStateException("analysis failed"))
        val vm = previewViewModel(mockk(), analyzer = analyzer)
        vm.refresh()
        advanceUntilIdle()
        vm.startAutoRiskScan()
        advanceUntilIdle()

        assertEquals("analysis failed", vm.state.value.error)
        assertEquals(false, vm.state.value.autoScanRunning)
        assertEquals(0, vm.state.value.autoScanScannedApps)
        vm.onCleared()
    }

    @Test
    fun languageSwitchRemapsCompletedFindingsWithoutRepeatingAnalysis() = runTest(dispatcher) {
        val dispatchers = TestAppDispatchers(dispatcher)
        val analyzer = AnalyzePermissionRisksUseCase(dispatchers)
        val list = mockk<ListPermissionsUseCase>()
        coEvery { list() } returns Result.success(listOf(
            AppPermissionEntry("Demo", "pkg.demo", listOf("android.permission.CAMERA"))
        ))
        val vm = PermissionsViewModel(
            list, analyzer, BuildPermissionsReportUseCase(analyzer, dispatchers), reportExporter(),
            emptyFavorites(), mockk(), dispatchers
        )
        vm.refresh()
        advanceUntilIdle()
        vm.startAutoRiskScan()
        advanceUntilIdle()
        val report = vm.state.value.riskReports.single()

        for (language in listOf("it", "en", "de", "es", "fr")) {
            vm.updateLanguage(language)
            assertEquals(report, vm.state.value.riskReports.single())
            assertEquals(
                UseCaseTexts(language).permissionReason(report.findings.single()),
                vm.state.value.findingsByPackage["pkg.demo"]?.single()?.reason
            )
            assertEquals("android.permission.CAMERA", vm.state.value.findingsByPackage["pkg.demo"]?.single()?.title)
        }
        coVerify(exactly = 1) { list() }
        assertEquals(false, vm.state.value.autoScanRunning)
        vm.onCleared()
    }

    @Test
    fun languageSwitchCancelsObsoletePreviewAndSharesNewLanguageContent() = runTest(dispatcher) {
        val builder = mockk<BuildPermissionsReportUseCase>()
        val files = mockk<ReportFileRepository>()
        var cancelled = false
        coEvery { builder.single(any(), any(), any()) } coAnswers {
            val reportText = thirdArg<UseCaseReportText>()
            if (reportText.noDataAvailable() == UseCaseTexts("en").noDataAvailable()) {
                try {
                    delay(1_000)
                } finally {
                    cancelled = true
                }
            }
            Result.success(reportText.noDataAvailable())
        }
        io.mockk.every { builder.singleFileName("pkg.first") } returns "first.txt"
        coEvery { files.save(any(), any()) } returns Result.success("/tmp/report")
        coEvery { files.share("/tmp/report") } returns Result.success(Unit)
        val vm = previewViewModel(builder, ExportReportUseCase(files))
        vm.refresh()
        advanceUntilIdle()
        vm.requestReportPreview("pkg.first")
        runCurrent()
        vm.updateLanguage("fr")
        advanceUntilIdle()
        val content = vm.state.value.reportPreviewContent

        assertTrue(cancelled)
        assertEquals(UseCaseTexts("fr").noDataAvailable(), content)
        vm.shareSingle("pkg.first", content)
        advanceUntilIdle()
        coVerify { files.save("first.txt", UseCaseTexts("fr").noDataAvailable()) }
        vm.onCleared()
    }

    private fun previewViewModel(
        builder: BuildPermissionsReportUseCase,
        exporter: ExportReportUseCase = reportExporter(),
        analyzer: AnalyzePermissionRisksUseCase = AnalyzePermissionRisksUseCase(dispatchers = TestAppDispatchers(dispatcher))
    ): PermissionsViewModel {
        val list = mockk<ListPermissionsUseCase>()
        coEvery { list() } returns Result.success(listOf(
            AppPermissionEntry("First", "pkg.first", emptyList()),
            AppPermissionEntry("Second", "pkg.second", emptyList())
        ))
        return PermissionsViewModel(
            list, analyzer, builder, exporter, emptyFavorites(), mockk(),
            TestAppDispatchers(dispatcher)
        )
    }
}
