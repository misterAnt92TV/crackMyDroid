package com.crackmydroid.shared.presentation

import com.crackmydroid.architecture.presentation.model.UiLoadState
import com.crackmydroid.architecture.testing.BaseViewModelUnitTest
import com.crackmydroid.shared.domain.model.InstalledAppEntry
import com.crackmydroid.shared.domain.model.apkcontent.ApkArchiveMetadata
import com.crackmydroid.shared.domain.model.apkcontent.ApkContentAnalysis
import com.crackmydroid.shared.domain.model.apkcontent.ApkContentFinding
import com.crackmydroid.shared.domain.model.apkcontent.ApkFindingSeverity
import com.crackmydroid.shared.domain.model.apkcontent.ApkLibrary
import com.crackmydroid.shared.domain.repository.ApkContentRepository
import com.crackmydroid.shared.domain.repository.InstalledAppsRepository
import com.crackmydroid.shared.domain.repository.ReportFileRepository
import com.crackmydroid.shared.domain.usecase.apkcontent.AnalyzeApkContentUseCase
import com.crackmydroid.shared.domain.usecase.apkcontent.OpenAnalyzedApkArchiveUseCase
import com.crackmydroid.shared.domain.usecase.installedapps.ListInstalledAppsUseCase
import com.crackmydroid.shared.domain.usecase.reports.ExportReportUseCase
import com.crackmydroid.shared.ui.apkcontent.AnalyzeApkContentEvent
import com.crackmydroid.shared.ui.apkcontent.AnalyzeApkContentViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyzeApkContentViewModelTest : BaseViewModelUnitTest() {

    private val appsRepo = mockk<InstalledAppsRepository>()
    private val apkContentRepo = mockk<ApkContentRepository>()
    private val reportFileRepo = mockk<ReportFileRepository>()

    private val listAppsUseCase = ListInstalledAppsUseCase(appsRepo)
    private val analyzeApkUseCase = AnalyzeApkContentUseCase(apkContentRepo)
    private val openArchiveUseCase = OpenAnalyzedApkArchiveUseCase(apkContentRepo)
    private val exportReportUseCase = ExportReportUseCase(reportFileRepo)

    @BeforeTest
    override fun setUp() {
        super.setUp()
    }

    @AfterTest
    override fun tearDown() {
        super.tearDown()
    }

    private fun createViewModel() = autoClear(
        AnalyzeApkContentViewModel(
            listApps = listAppsUseCase,
            analyzeApk = analyzeApkUseCase,
            openArchive = openArchiveUseCase,
            reportExporter = exportReportUseCase,
            dispatchers = appDispatchers,
        ),
    )

    private val sampleApps = listOf(
        InstalledAppEntry(
            appLabel = "Vulnerable App",
            packageName = "com.example.vulnerable",
            sourcePath = "/data/app/com.example.vulnerable.apk",
        ),
        InstalledAppEntry(
            appLabel = "Safe Calculator",
            packageName = "com.example.calculator",
            sourcePath = "/data/app/com.example.calculator.apk",
        ),
    )

    private val sampleAnalysis = ApkContentAnalysis(
        packageName = "com.example.vulnerable",
        metadata = ApkArchiveMetadata(
            sha256 = "abcd1234efgh5678",
            archiveBytes = 1024L,
            entryCount = 42,
            uncompressedBytes = 2048L,
            dexCount = 1,
            nativeLibraries = listOf("libnative.so"),
            certificateEntries = listOf("META-INF/CERT.RSA"),
        ),
        libraries = listOf(
            ApkLibrary(name = "Retrofit", evidence = "okhttp3/OkHttpClient.class"),
        ),
        findings = listOf(
            ApkContentFinding(
                severity = ApkFindingSeverity.High,
                category = "Native Code",
                evidence = "libnative.so",
                remediation = "Verify binary protections",
            ),
        ),
        structuralEntries = listOf("AndroidManifest.xml", "classes.dex"),
    )

    @Test
    fun `initial state starts at step 1 with empty apps`() {
        val vm = createViewModel()
        val state = vm.state.value

        assertEquals(1, state.step)
        assertTrue(state.apps.isEmpty())
        assertNull(state.selectedPackage)
        assertNull(state.analysis)
    }

    @Test
    fun `refresh loads apps and transitions to success`() = runViewModelTest {
        coEvery { appsRepo.listInstalled() } returns sampleApps

        val vm = createViewModel()
        vm.emitEventAndAwaitIdle(AnalyzeApkContentEvent.Refresh())

        val state = vm.state.value
        assertEquals(2, state.apps.size)
        assertEquals(UiLoadState.Success, state.loadState)
        assertEquals(1, state.step)
    }

    @Test
    fun `query changed filters visible apps`() = runViewModelTest {
        coEvery { appsRepo.listInstalled() } returns sampleApps

        val vm = createViewModel()
        vm.emitEventAndAwaitIdle(AnalyzeApkContentEvent.Refresh())

        vm.emitEventAndAwaitIdle(AnalyzeApkContentEvent.QueryChanged("calc"))
        assertEquals("calc", vm.state.value.query)
        assertEquals(1, vm.state.value.filteredApps.size)
        assertEquals("com.example.calculator", vm.state.value.filteredApps.first().packageName)
    }

    @Test
    fun `package selected advances to step 2 and clear selection returns to step 1`() = runViewModelTest {
        coEvery { appsRepo.listInstalled() } returns sampleApps

        val vm = createViewModel()
        vm.emitEventAndAwaitIdle(AnalyzeApkContentEvent.Refresh())

        vm.emitEventAndAwaitIdle(AnalyzeApkContentEvent.PackageSelected("com.example.vulnerable"))
        assertEquals("com.example.vulnerable", vm.state.value.selectedPackage)
        assertEquals(2, vm.state.value.step)

        vm.emitEventAndAwaitIdle(AnalyzeApkContentEvent.ClearSelection())
        assertNull(vm.state.value.selectedPackage)
        assertEquals(1, vm.state.value.step)
    }

    @Test
    fun `analysis requested runs analysis and transitions to step 3`() = runViewModelTest {
        coEvery { appsRepo.listInstalled() } returns sampleApps
        coEvery { apkContentRepo.analyze("com.example.vulnerable") } returns Result.success(sampleAnalysis)

        val vm = createViewModel()
        vm.emitEventAndAwaitIdle(AnalyzeApkContentEvent.Refresh())

        vm.emitEventAndAwaitIdle(AnalyzeApkContentEvent.PackageSelected("com.example.vulnerable"))
        vm.emitEventAndAwaitIdle(AnalyzeApkContentEvent.AnalysisRequested())

        val state = vm.state.value
        assertEquals(3, state.step)
        assertEquals(UiLoadState.Success, state.analysisState)
        assertEquals(sampleAnalysis, state.analysis)
        assertEquals(ApkFindingSeverity.High, state.analysis?.highestSeverity)
    }

    @Test
    fun `reset event clears selection and analysis returning to step 1`() = runViewModelTest {
        coEvery { appsRepo.listInstalled() } returns sampleApps
        coEvery { apkContentRepo.analyze("com.example.vulnerable") } returns Result.success(sampleAnalysis)

        val vm = createViewModel()
        vm.emitEventAndAwaitIdle(AnalyzeApkContentEvent.Refresh())

        vm.emitEventAndAwaitIdle(AnalyzeApkContentEvent.PackageSelected("com.example.vulnerable"))
        vm.emitEventAndAwaitIdle(AnalyzeApkContentEvent.AnalysisRequested())
        assertEquals(3, vm.state.value.step)

        // Reset to repeat procedure
        vm.emitEventAndAwaitIdle(AnalyzeApkContentEvent.Reset())
        val state = vm.state.value
        assertEquals(1, state.step)
        assertNull(state.selectedPackage)
        assertNull(state.analysis)
        assertEquals(UiLoadState.Idle, state.analysisState)
        assertEquals("", state.query)
        assertEquals(2, state.apps.size)
    }
}
