package com.crackmydroid.shared.presentation

import com.crackmydroid.shared.domain.model.ExportDocument
import com.crackmydroid.shared.domain.model.ExportFormat
import com.crackmydroid.shared.domain.model.UseCaseReportText
import com.crackmydroid.shared.domain.model.app.AppPermissionEntry
import com.crackmydroid.shared.domain.repository.ReportFileRepository
import com.crackmydroid.shared.domain.usecase.permissions.AnalyzePermissionRisksUseCase
import com.crackmydroid.shared.domain.usecase.permissions.BuildPermissionsReportUseCase
import com.crackmydroid.shared.domain.usecase.permissions.ListPermissionsUseCase
import com.crackmydroid.shared.domain.usecase.reports.ExportReportUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetExportFormatUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetPermissionFavoritesUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetPermissionFavoritesUseCase
import com.crackmydroid.shared.i18n.UseCaseTexts
import com.crackmydroid.shared.ui.permissions.PermissionsEvent
import com.crackmydroid.shared.ui.permissions.PermissionsViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

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
    fun exportUsesTheSelectedPdfExtension() = runTest(dispatcher) {
        val builder = mockk<BuildPermissionsReportUseCase>()
        val files = mockk<ReportFileRepository>()
        val format = mockk<GetExportFormatUseCase>()
        val saved = slot<ExportDocument>()
        coEvery { builder(emptyList(), emptyList(), any()) } returns Result.success("permissions report")
        coEvery { format() } returns Result.success(ExportFormat.PDF)
        coEvery { files.save(capture(saved)) } returns Result.success("/tmp/permissions_report.pdf")
        val vm = previewViewModel(builder, ExportReportUseCase(files, format))

        vm.handleEvent(PermissionsEvent.ExportRequested())
        advanceUntilIdle()

        assertEquals("/tmp/permissions_report.pdf", vm.state.value.lastExportPath)
        assertEquals("permissions_report.pdf", saved.captured.fileName)
        assertEquals("permissions", saved.captured.reportType)
        assertEquals("permissions report", saved.captured.textContent)
        coVerify(exactly = 1) { files.save(any<ExportDocument>()) }
        vm.onCleared()
    }

    @Test
    fun setFilterTracksMostFrequentTopQuery() = runTest(dispatcher) {
        val listPermissions = mockk<ListPermissionsUseCase>()
        coEvery { listPermissions() } returns Result.success(
            listOf(
                AppPermissionEntry("Demo", "pkg.demo", listOf("android.permission.INTERNET")),
            ),
        )
        val analyzePermissionRisks = AnalyzePermissionRisksUseCase(dispatchers = TestAppDispatchers(dispatcher))
        val buildPermissionsReport =
            BuildPermissionsReportUseCase(analyzePermissionRisks, dispatchers = TestAppDispatchers(dispatcher))
        val vm = PermissionsViewModel(
            listPermissions,
            analyzePermissionRisks,
            buildPermissionsReport,
            reportExporter(),
            emptyFavorites(),
            mockk(relaxed = true),
            TestAppDispatchers(dispatcher),
        )

        vm.handleEvent(PermissionsEvent.Refresh())
        advanceUntilIdle()
        vm.handleEvent(PermissionsEvent.FilterChanged("sms", trackUsage = true))
        vm.handleEvent(PermissionsEvent.FilterChanged("cam", trackUsage = true))
        vm.handleEvent(PermissionsEvent.FilterChanged("sms", trackUsage = true))

        assertEquals("sms", vm.state.value.topQuery)
        vm.onCleared()
    }

    @Test
    fun showOnlyRiskyFiltersOutSafeAppsAfterScan() = runTest(dispatcher) {
        val listPermissions = mockk<ListPermissionsUseCase>()
        coEvery { listPermissions() } returns Result.success(
            listOf(
                AppPermissionEntry("Safe App", "pkg.safe", emptyList()),
                AppPermissionEntry(
                    "Risky App",
                    "pkg.risky",
                    listOf("android.permission.SYSTEM_ALERT_WINDOW"),
                ),
            ),
        )
        val analyzePermissionRisks = AnalyzePermissionRisksUseCase(dispatchers = TestAppDispatchers(dispatcher))
        val buildPermissionsReport =
            BuildPermissionsReportUseCase(analyzePermissionRisks, dispatchers = TestAppDispatchers(dispatcher))
        val vm = PermissionsViewModel(
            listPermissions,
            analyzePermissionRisks,
            buildPermissionsReport,
            reportExporter(),
            emptyFavorites(),
            mockk(relaxed = true),
            TestAppDispatchers(dispatcher),
        )

        vm.handleEvent(PermissionsEvent.Refresh())
        advanceUntilIdle()
        vm.handleEvent(PermissionsEvent.AutoRiskScanStarted())
        advanceUntilIdle()
        vm.handleEvent(PermissionsEvent.RiskFilterChanged(true))

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
            TestAppDispatchers(dispatcher),
        )

        vm.handleEvent(PermissionsEvent.Refresh())
        advanceUntilIdle()
        vm.handleEvent(PermissionsEvent.FavoriteToggled("pkg.favorite"))
        advanceUntilIdle()

        assertEquals(emptySet(), vm.state.value.favorites)
        coVerify { setFavorites(emptySet()) }
        vm.onCleared()
    }

    @Test
    fun previewIsAsynchronousAndSharingReusesTheGeneratedContent() = runTest(dispatcher) {
        val builder = mockk<BuildPermissionsReportUseCase>()
        val files = mockk<ReportFileRepository>()
        val format = mockk<GetExportFormatUseCase>()
        val shared = slot<ExportDocument>()
        coEvery { builder.single(any(), any(), any()) } returns Result.success("generated content")
        coEvery { format() } returns Result.success(ExportFormat.PDF)
        coEvery { files.share(capture(shared)) } returns Result.success("/tmp/report.pdf")
        io.mockk.every { builder.singleFileName("pkg.first") } returns "first.txt"
        val vm = previewViewModel(builder, ExportReportUseCase(files, format))
        vm.handleEvent(PermissionsEvent.Refresh())
        advanceUntilIdle()

        vm.handleEvent(PermissionsEvent.ReportPreviewRequested("pkg.first"))
        assertTrue(vm.state.value.reportPreviewLoading)
        assertNull(vm.state.value.reportPreviewContent)
        advanceUntilIdle()
        assertEquals("generated content", vm.state.value.reportPreviewContent)
        assertEquals(false, vm.state.value.reportPreviewLoading)

        vm.handleEvent(PermissionsEvent.ReportShared("pkg.first", vm.state.value.reportPreviewContent))
        advanceUntilIdle()
        coVerify(exactly = 1) { builder.single(any(), any(), any()) }
        assertEquals("first.pdf", shared.captured.fileName)
        assertEquals("permissions", shared.captured.reportType)
        assertEquals("generated content", shared.captured.textContent)
        coVerify(exactly = 1) { files.share(any<ExportDocument>()) }
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
        coEvery { builder.single(match { it.packageName == "pkg.second" }, any(), any()) } returns
            Result.success("second")
        val vm = previewViewModel(builder)
        vm.handleEvent(PermissionsEvent.Refresh())
        advanceUntilIdle()
        vm.handleEvent(PermissionsEvent.ReportPreviewRequested("pkg.first"))
        runCurrent()
        vm.handleEvent(PermissionsEvent.ReportPreviewRequested("pkg.second"))
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
        vm.handleEvent(PermissionsEvent.Refresh())
        advanceUntilIdle()
        vm.handleEvent(PermissionsEvent.ReportPreviewRequested("pkg.first"))
        runCurrent()
        vm.handleEvent(PermissionsEvent.ReportPreviewDismissed())
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
        vm.handleEvent(PermissionsEvent.Refresh())
        advanceUntilIdle()
        vm.handleEvent(PermissionsEvent.ReportPreviewRequested("pkg.first"))
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
        vm.handleEvent(PermissionsEvent.Refresh())
        advanceUntilIdle()
        vm.handleEvent(PermissionsEvent.AutoRiskScanStarted())
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
        coEvery { list() } returns Result.success(
            listOf(
                AppPermissionEntry("Demo", "pkg.demo", listOf("android.permission.CAMERA")),
            ),
        )
        val vm = PermissionsViewModel(
            list,
            analyzer,
            BuildPermissionsReportUseCase(analyzer, dispatchers),
            reportExporter(),
            emptyFavorites(),
            mockk(),
            dispatchers,
        )
        vm.handleEvent(PermissionsEvent.Refresh())
        advanceUntilIdle()
        vm.handleEvent(PermissionsEvent.AutoRiskScanStarted())
        advanceUntilIdle()
        val report = vm.state.value.riskReports.single()

        for (language in listOf("it", "en", "de", "es", "fr")) {
            vm.updateLanguage(language)
            assertEquals(report, vm.state.value.riskReports.single())
            assertEquals(
                UseCaseTexts(language).permissionReason(report.findings.single()),
                vm.state.value.findingsByPackage["pkg.demo"]?.single()?.reason,
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
        val format = mockk<GetExportFormatUseCase>()
        val shared = slot<ExportDocument>()
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
        coEvery { format() } returns Result.success(ExportFormat.PDF)
        io.mockk.every { builder.singleFileName("pkg.first") } returns "first.txt"
        coEvery { files.share(capture(shared)) } returns Result.success("/tmp/report.pdf")
        val vm = previewViewModel(builder, ExportReportUseCase(files, format))
        vm.handleEvent(PermissionsEvent.Refresh())
        advanceUntilIdle()
        vm.handleEvent(PermissionsEvent.ReportPreviewRequested("pkg.first"))
        runCurrent()
        vm.updateLanguage("fr")
        advanceUntilIdle()
        val content = vm.state.value.reportPreviewContent

        assertTrue(cancelled)
        assertEquals(UseCaseTexts("fr").noDataAvailable(), content)
        vm.handleEvent(PermissionsEvent.ReportShared("pkg.first", content))
        advanceUntilIdle()
        assertEquals("first.pdf", shared.captured.fileName)
        assertEquals(UseCaseTexts("fr").noDataAvailable(), shared.captured.textContent)
        coVerify { files.share(any<ExportDocument>()) }
        vm.onCleared()
    }

    @Test
    fun oldScanFinallyDoesNotClearReplacementProgress() = runTest(dispatcher) {
        val analyzer = mockk<AnalyzePermissionRisksUseCase>()
        var calls = 0
        coEvery { analyzer(any()) } coAnswers {
            if (++calls == 1) {
                try {
                    awaitCancellation()
                } finally {
                    withContext(NonCancellable) { delay(1_000) }
                }
            }
            delay(2_000)
            Result.success(null)
        }
        val vm = previewViewModel(mockk(), analyzer = analyzer)
        vm.handleEvent(PermissionsEvent.Refresh())
        advanceUntilIdle()
        vm.handleEvent(PermissionsEvent.AutoRiskScanStarted())
        runCurrent()
        vm.handleEvent(PermissionsEvent.AutoRiskScanStarted())
        advanceTimeBy(1_100)
        runCurrent()

        assertTrue(vm.state.value.autoScanRunning)
        assertEquals("First", vm.state.value.autoScanCurrentLabel)
        assertEquals(0, vm.state.value.autoScanScannedApps)
        advanceUntilIdle()
        assertEquals(false, vm.state.value.autoScanRunning)
        assertEquals(2, vm.state.value.autoScanScannedApps)
        assertNull(vm.state.value.error)
        vm.onCleared()
    }

    @Test
    fun refreshInvalidatesScanIgnoringCancellation() = runTest(dispatcher) {
        val analyzer = mockk<AnalyzePermissionRisksUseCase>()
        val app = AppPermissionEntry("First", "pkg.first", listOf("android.permission.CAMERA"))
        val real = AnalyzePermissionRisksUseCase(TestAppDispatchers(dispatcher))
        val report = real(app).getOrThrow()
        coEvery { analyzer(any()) } coAnswers {
            withContext(NonCancellable) { delay(1_000) }
            Result.success(report)
        }
        val vm = previewViewModel(mockk(), analyzer = analyzer)
        vm.handleEvent(PermissionsEvent.Refresh())
        advanceUntilIdle()
        vm.handleEvent(PermissionsEvent.AutoRiskScanStarted())
        runCurrent()
        vm.handleEvent(PermissionsEvent.Refresh())
        advanceUntilIdle()

        assertTrue(vm.state.value.riskReports.isEmpty())
        assertTrue(vm.state.value.findingsByPackage.isEmpty())
        assertEquals(false, vm.state.value.autoScanRunning)
        assertEquals(0, vm.state.value.autoScanScannedApps)
        assertNull(vm.state.value.error)
        vm.onCleared()
    }

    @Test
    fun latestRefreshWinsOverNonCooperativeOldLoad() = runTest(dispatcher) {
        val list = mockk<ListPermissionsUseCase>()
        var calls = 0
        val latest = listOf(AppPermissionEntry("Latest", "pkg.latest", emptyList()))
        coEvery { list() } coAnswers {
            if (++calls == 1) {
                withContext(NonCancellable) { delay(1_000) }
                Result.success(listOf(AppPermissionEntry("Old", "pkg.old", emptyList())))
            } else {
                Result.success(latest)
            }
        }
        val vm = previewViewModel(mockk(), list = list)
        vm.handleEvent(PermissionsEvent.Refresh())
        runCurrent()
        vm.handleEvent(PermissionsEvent.Refresh())
        advanceUntilIdle()

        assertEquals(latest, vm.state.value.apps)
        assertEquals(latest, vm.state.value.visibleApps)
        assertEquals(false, vm.state.value.loading)
        assertNull(vm.state.value.error)
        vm.onCleared()
    }

    @Test
    fun filteringAndSafeScanProgressReuseLocalizedFindings() = runTest(dispatcher) {
        val analyzer = AnalyzePermissionRisksUseCase(TestAppDispatchers(dispatcher))
        val list = mockk<ListPermissionsUseCase>()
        coEvery { list() } returns Result.success(
            listOf(
                AppPermissionEntry("First", "pkg.first", listOf("android.permission.CAMERA")),
                AppPermissionEntry("Second", "pkg.second", emptyList()),
            ),
        )
        val vm = previewViewModel(mockk(), analyzer = analyzer, list = list)
        vm.handleEvent(PermissionsEvent.Refresh())
        advanceUntilIdle()
        vm.handleEvent(PermissionsEvent.AutoRiskScanStarted())
        runCurrent()
        val findings = vm.state.value.findingsByPackage
        val visibleFindings = vm.state.value.visibleFindingsByPackage
        assertEquals(1, findings.size)
        advanceUntilIdle()
        assertSame(findings, vm.state.value.findingsByPackage)
        vm.handleEvent(PermissionsEvent.FilterChanged("First"))
        vm.handleEvent(PermissionsEvent.RiskFilterChanged(true))

        assertSame(findings, vm.state.value.findingsByPackage)
        assertSame(visibleFindings, vm.state.value.visibleFindingsByPackage)
        assertEquals(listOf("pkg.first"), vm.state.value.visibleApps.map { it.packageName })
        assertEquals(2, vm.state.value.autoScanScannedApps)
        vm.onCleared()
    }

    @Test
    fun addingReportReusesUnchangedLocalizedFindingItems() = runTest(dispatcher) {
        val analyzer = AnalyzePermissionRisksUseCase(TestAppDispatchers(dispatcher))
        val list = mockk<ListPermissionsUseCase>()
        coEvery { list() } returns Result.success(
            listOf(
                AppPermissionEntry("First", "pkg.first", listOf("android.permission.CAMERA")),
                AppPermissionEntry("Second", "pkg.second", listOf("android.permission.SYSTEM_ALERT_WINDOW")),
            ),
        )
        val vm = previewViewModel(mockk(), analyzer = analyzer, list = list)
        vm.handleEvent(PermissionsEvent.Refresh())
        advanceUntilIdle()
        vm.handleEvent(PermissionsEvent.AutoRiskScanStarted())
        runCurrent()
        val firstItems = vm.state.value.findingsByPackage.getValue("pkg.first")
        advanceUntilIdle()

        assertSame(firstItems, vm.state.value.findingsByPackage.getValue("pkg.first"))
        assertEquals(2, vm.state.value.findingsByPackage.size)
        assertEquals("pkg.second", vm.state.value.riskReports.first().packageName)
        vm.onCleared()
    }

    @Test
    fun largeScanPreservesRiskOrderingAndFilterResults() = runTest(dispatcher) {
        val analyzer = AnalyzePermissionRisksUseCase(TestAppDispatchers(dispatcher))
        val apps = (1..120).map {
            AppPermissionEntry(
                "App $it",
                "pkg.$it",
                listOf(
                    if (it % 2 == 0) "android.permission.SYSTEM_ALERT_WINDOW" else "android.permission.CAMERA",
                ),
            )
        }
        val expected = analyzer.sortReports(apps.mapNotNull { analyzer(it).getOrThrow() })
        val list = mockk<ListPermissionsUseCase>()
        coEvery { list() } returns Result.success(apps)
        val vm = previewViewModel(mockk(), analyzer = analyzer, list = list)
        vm.handleEvent(PermissionsEvent.Refresh())
        advanceUntilIdle()
        vm.handleEvent(PermissionsEvent.AutoRiskScanStarted())
        advanceUntilIdle()

        assertEquals(expected, vm.state.value.riskReports)
        assertEquals(apps.size, vm.state.value.autoScanScannedApps)
        val findings = vm.state.value.findingsByPackage
        vm.handleEvent(PermissionsEvent.FilterChanged("SYSTEM_ALERT_WINDOW"))
        assertEquals(60, vm.state.value.visibleApps.size)
        assertSame(findings, vm.state.value.findingsByPackage)
        vm.onCleared()
    }

    private fun previewViewModel(
        builder: BuildPermissionsReportUseCase,
        exporter: ExportReportUseCase = reportExporter(),
        analyzer: AnalyzePermissionRisksUseCase =
            AnalyzePermissionRisksUseCase(dispatchers = TestAppDispatchers(dispatcher)),
        list: ListPermissionsUseCase? = null,
    ): PermissionsViewModel {
        val permissions = list ?: mockk<ListPermissionsUseCase>().also {
            coEvery { it() } returns Result.success(
                listOf(
                    AppPermissionEntry("First", "pkg.first", emptyList()),
                    AppPermissionEntry("Second", "pkg.second", emptyList()),
                ),
            )
        }
        return PermissionsViewModel(
            permissions,
            analyzer,
            builder,
            exporter,
            emptyFavorites(),
            mockk(),
            TestAppDispatchers(dispatcher),
        )
    }
}
