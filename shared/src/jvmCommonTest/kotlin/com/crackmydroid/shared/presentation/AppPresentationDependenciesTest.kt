package com.crackmydroid.shared.presentation

import com.crackmydroid.architecture.coroutines.AppDispatchers
import com.crackmydroid.architecture.logging.AppLogger
import com.crackmydroid.shared.di.commonModule
import com.crackmydroid.shared.di.presentationModule
import com.crackmydroid.shared.domain.model.DeviceInfo
import com.crackmydroid.shared.domain.model.ExportFormat
import com.crackmydroid.shared.domain.model.InstalledAppEntry
import com.crackmydroid.shared.domain.model.ReportLabel
import com.crackmydroid.shared.domain.model.RootStatus
import com.crackmydroid.shared.domain.model.app.AppLanguage
import com.crackmydroid.shared.domain.model.app.AppTheme
import com.crackmydroid.shared.domain.model.shell.ShellCommand
import com.crackmydroid.shared.domain.repository.ActivityRepository
import com.crackmydroid.shared.domain.repository.DeviceInfoRepository
import com.crackmydroid.shared.domain.repository.InstalledAppsRepository
import com.crackmydroid.shared.domain.repository.PermissionsRepository
import com.crackmydroid.shared.domain.repository.ReportFileRepository
import com.crackmydroid.shared.domain.repository.RootCheckRepository
import com.crackmydroid.shared.domain.repository.SettingsRepository
import com.crackmydroid.shared.domain.repository.ShellCommandRepository
import com.crackmydroid.shared.domain.repository.log.LogRepository
import com.crackmydroid.shared.domain.repository.log.OperationLogRepository
import com.crackmydroid.shared.i18n.usecase.UseCaseTexts
import com.crackmydroid.shared.presentation.navigation.Screen
import com.crackmydroid.shared.presentation.operations.NoOpOperationLogRepository
import com.crackmydroid.shared.ui.AppPresentationDependencies
import com.crackmydroid.shared.ui.navigation.AppNavigationEvent
import com.crackmydroid.shared.ui.navigation.NavigationSource
import com.crackmydroid.shared.ui.pentest.PenTestingEvent
import com.crackmydroid.shared.ui.pentest.model.PenTestWizardStep
import com.crackmydroid.shared.ui.settings.SettingsEvent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication
import org.koin.dsl.module

@OptIn(ExperimentalCoroutinesApi::class)
class AppPresentationDependenciesTest {
    private val dispatcher = StandardTestDispatcher()
    private val deviceRepository = mockk<DeviceInfoRepository>()
    private val rootRepository = mockk<RootCheckRepository>()
    private val settingsRepository = mockk<SettingsRepository>()
    private val activityRepository = mockk<ActivityRepository>()
    private val installedRepository = mockk<InstalledAppsRepository>()
    private val permissionsRepository = mockk<PermissionsRepository>()
    private val shellRepository = mockk<ShellCommandRepository>()
    private val logRepository = mockk<LogRepository>()
    private val graphs = mutableListOf<AppPresentationDependencies>()
    private lateinit var application: KoinApplication
    private val device = DeviceInfo(
        manufacturer = "Acme",
        model = "Device under test",
        batteryLevel = 75,
        totalMemory = 4096L,
        freeMemory = 2048L,
    )
    private val root = RootStatus(false, "not rooted")
    private val apps = listOf(InstalledAppEntry("Test app", "test.app", "/test.apk"))
    private val commands = listOf(ShellCommand("Properties", "getprop", false))

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        AppLogger.setVerbose(false)
        coEvery { deviceRepository.getDeviceInfo() } returns device
        coEvery { rootRepository.checkRoot() } returns root
        coEvery { settingsRepository.isIntroSeen() } returns false
        coEvery { settingsRepository.setIntroSeen(true) } returns Unit
        coEvery { settingsRepository.getLanguage() } returns AppLanguage.ITALIAN
        coEvery { settingsRepository.getTheme() } returns AppTheme.DARK
        coEvery { settingsRepository.isVerboseLog() } returns true
        coEvery { settingsRepository.getExportFormat() } returns ExportFormat.TXT
        coEvery { settingsRepository.isHighContrast() } returns true
        coEvery { settingsRepository.isReduceMotion() } returns true
        coEvery { settingsRepository.isLargeText() } returns true
        coEvery { settingsRepository.isSuggestionsEnabled() } returns false
        coEvery { settingsRepository.isFeatureHintsEnabled() } returns true
        coEvery { settingsRepository.setFeatureHintsEnabled(false) } returns Unit
        coEvery { settingsRepository.getActivityFavorites() } returns setOf("activity")
        coEvery { settingsRepository.getActivityPackageFavorites() } returns setOf("test.app")
        coEvery { settingsRepository.getInstalledAppFavorites() } returns setOf("test.app")
        coEvery { settingsRepository.getPermissionFavorites() } returns setOf("test.app")
        coEvery { activityRepository.getActivities() } returns emptyList()
        coEvery { installedRepository.listInstalled() } returns apps
        coEvery { permissionsRepository.listAppPermissions() } returns emptyList()
        coEvery { shellRepository.listCommands() } returns commands
        coEvery { logRepository.fetchLog() } returns listOf("I/Test: loaded")
        application = koinApplication {
            modules(
                commonModule(),
                presentationModule(),
                module {
                    single { deviceRepository }
                    single { rootRepository }
                    single { settingsRepository }
                    single { activityRepository }
                    single { installedRepository }
                    single { permissionsRepository }
                    single { shellRepository }
                    single { logRepository }
                    single<ReportFileRepository> { mockk() }
                    single<OperationLogRepository> { NoOpOperationLogRepository }
                    single<AppDispatchers> {
                        object : AppDispatchers {
                            override val main = dispatcher
                            override val default = dispatcher
                        }
                    }
                },
            )
        }
    }

    @AfterTest
    fun tearDown() {
        graphs.forEach { graph -> viewModels(graph).forEach { it.onCleared() } }
        application.close()
        AppLogger.setVerbose(false)
        Dispatchers.resetMain()
    }

    @Test
    fun persistedLanguageIsUsedForTheFirstReportAndUpdatesWithoutReloads() = runTest(dispatcher) {
        coEvery { settingsRepository.getLanguage() } returns AppLanguage.FRENCH
        coEvery { settingsRepository.setLanguage(AppLanguage.GERMAN) } returns Unit
        val graph = resolveGraph()
        graph.navigation.handleEvent(AppNavigationEvent.Initialize())
        advanceUntilIdle()
        assertTrue(
            graph.info.state.value.reportContent.orEmpty()
                .startsWith(UseCaseTexts("fr").label(ReportLabel.DEVICE_INFO_TITLE)),
        )

        graph.settings.handleEvent(SettingsEvent.LanguageSelected(AppLanguage.GERMAN))
        advanceUntilIdle()
        assertTrue(
            graph.info.state.value.reportContent.orEmpty()
                .startsWith(UseCaseTexts("de").label(ReportLabel.DEVICE_INFO_TITLE)),
        )
        coVerify(exactly = 1) { deviceRepository.getDeviceInfo() }
        coVerify(exactly = 1) { rootRepository.checkRoot() }
    }

    @Test
    fun failedVerboseBootstrapDoesNotBlockFeatureInitialization() = runTest(dispatcher) {
        coEvery { settingsRepository.isVerboseLog() } throws IllegalStateException("diagnostics unavailable")
        val graph = resolveGraph()
        graph.navigation.handleEvent(AppNavigationEvent.Initialize())
        advanceUntilIdle()

        assertFalse(AppLogger.isVerbose())
        assertTrue(graph.navigation.state.value.initialized)
        assertEquals(apps, graph.installedApps.state.value.apps)
        assertEquals("diagnostics unavailable", graph.settings.state.value.error)
    }

    @Test
    fun initialize_populatesTheViewModelsExposedToScreens_onlyOnce() = runTest(dispatcher) {
        val graph = resolveGraph()

        graph.navigation.handleEvent(AppNavigationEvent.Initialize())
        advanceUntilIdle()

        assertEquals(device, graph.info.state.value.info)
        assertEquals(root, graph.info.state.value.root)
        assertTrue(graph.info.state.value.reportContent.orEmpty().contains("Device under test"))
        assertFalse(graph.info.state.value.loading)
        assertNull(graph.info.state.value.error)
        assertEquals(setOf("activity"), graph.activity.state.value.favorites)
        assertEquals(setOf("test.app"), graph.activity.state.value.favoritePackages)
        assertEquals(commands, graph.trick.state.value.commands)
        assertEquals(AppLanguage.ITALIAN, graph.settings.state.value.language)
        assertEquals(AppTheme.DARK, graph.settings.state.value.theme)
        assertTrue(graph.settings.state.value.highContrast)
        assertEquals(listOf("I/Test: loaded"), graph.logs.state.value.logs)
        assertEquals(setOf("test.app"), graph.permissions.state.value.favorites)
        assertEquals(apps, graph.installedApps.state.value.apps)
        assertEquals(apps, graph.penTesting.state.value.apps)
        assertTrue(graph.navigation.state.value.initialized)
        assertTrue(AppLogger.isVerbose())
        assertTrue(graph.navigation.state.value.showIntro)

        graph.navigation.handleEvent(AppNavigationEvent.Initialize())
        advanceUntilIdle()

        coVerify(exactly = 1) { deviceRepository.getDeviceInfo() }
        coVerify(exactly = 1) { rootRepository.checkRoot() }
        coVerify(exactly = 1) { activityRepository.getActivities() }
        coVerify(exactly = 1) { shellRepository.listCommands() }
        coVerify(exactly = 1) { settingsRepository.getTheme() }
        coVerify(exactly = 1) { logRepository.fetchLog() }
        coVerify(exactly = 1) { permissionsRepository.listAppPermissions() }
        coVerify(exactly = 2) { installedRepository.listInstalled() }

        graph.navigation.handleEvent(AppNavigationEvent.HintConfirmed(disableHints = true, target = Screen.Permissions))
        graph.navigation.handleEvent(AppNavigationEvent.IntroDismissed())
        advanceUntilIdle()

        assertFalse(graph.settings.state.value.featureHintsEnabled)
        assertFalse(graph.navigation.state.value.showIntro)
        assertEquals(Screen.Permissions, graph.navigation.state.value.selectedScreen)
        coVerify(exactly = 1) { settingsRepository.setFeatureHintsEnabled(false) }
        coVerify(exactly = 1) { settingsRepository.setIntroSeen(true) }
    }

    @Test
    fun separateContainers_haveIndependentViewModelsAndState() = runTest(dispatcher) {
        val first = resolveGraph()
        val second = resolveGraph()

        viewModels(first).zip(viewModels(second)).forEach { (left, right) ->
            assertNotSame(left, right)
        }
        first.navigation.handleEvent(AppNavigationEvent.Initialize())
        advanceUntilIdle()

        assertEquals(device, first.info.state.value.info)
        assertNull(second.info.state.value.info)
        assertFalse(second.navigation.state.value.initialized)
    }

    @Test
    fun navigationChrome_preservesSearchTokenAndNormalizesHistory() = runTest(dispatcher) {
        val graph = resolveGraph()
        graph.navigation.handleEvent(AppNavigationEvent.Initialize())
        advanceUntilIdle()

        assertEquals("Home", graph.navigation.state.value.topBar.title)
        assertEquals(listOf("Home", "Info device", "Impostazioni"), graph.navigation.state.value.bottomBarItems.map { it.label })

        graph.navigation.handleEvent(
            AppNavigationEvent.ScreenSelected(Screen.Permissions, NavigationSource.Drawer, hintsEnabled = false),
        )
        assertEquals(Screen.Permissions, graph.navigation.state.value.selectedScreen)
        assertEquals(listOf(Screen.Home), graph.navigation.state.value.navigationHistory)
        assertTrue(graph.navigation.state.value.topBar.showSearch)

        val requestId = graph.navigation.state.value.topBar.searchRequestId
        graph.navigation.handleEvent(AppNavigationEvent.TopBarSearchRequested())
        assertEquals(requestId + 1, graph.navigation.state.value.topBar.searchRequestId)

        graph.navigation.handleEvent(AppNavigationEvent.BackRequested())
        assertEquals(Screen.Home, graph.navigation.state.value.selectedScreen)
        assertTrue(graph.navigation.state.value.navigationHistory.isEmpty())

        graph.navigation.handleEvent(
            AppNavigationEvent.ScreenSelected(Screen.Pentest, NavigationSource.Drawer, hintsEnabled = false),
        )
        graph.penTesting.handleEvent(PenTestingEvent.StepSelected(PenTestWizardStep.SelectChecks))
        advanceUntilIdle()
        assertFalse(graph.navigation.state.value.topBar.showSearch)

        graph.navigation.handleEvent(
            AppNavigationEvent.ScreenSelected(Screen.Settings, NavigationSource.BottomBar, hintsEnabled = false),
        )
        assertTrue(graph.navigation.state.value.navigationHistory.isEmpty())
        assertTrue(graph.navigation.state.value.bottomBarItems.single { it.screen == Screen.Settings }.selected)
        assertEquals(requestId + 1, graph.navigation.state.value.topBar.searchRequestId)

        val selectedSettings = graph.navigation.state.value
        graph.navigation.handleEvent(
            AppNavigationEvent.ScreenSelected(Screen.Settings, NavigationSource.BottomBar, hintsEnabled = false),
        )
        assertEquals(selectedSettings, graph.navigation.state.value)

        graph.navigation.handleEvent(
            AppNavigationEvent.ScreenSelected(Screen.Permissions, NavigationSource.Drawer, hintsEnabled = false),
        )
        graph.navigation.handleEvent(
            AppNavigationEvent.ScreenSelected(Screen.Root, NavigationSource.Drawer, hintsEnabled = false),
        )
        assertEquals(listOf(Screen.Settings, Screen.Permissions), graph.navigation.state.value.navigationHistory)

        graph.navigation.handleEvent(
            AppNavigationEvent.ScreenSelected(Screen.Root, NavigationSource.Drawer, hintsEnabled = false),
        )
        assertEquals(listOf(Screen.Settings, Screen.Permissions), graph.navigation.state.value.navigationHistory)
    }

    private fun resolveGraph(): AppPresentationDependencies =
        application.koin.get<AppPresentationDependencies>().also { graphs += it }

    private fun viewModels(graph: AppPresentationDependencies): List<LocalizedBaseViewModel<*, *>> = with(graph) {
        listOf(navigation, activity, home, root, info, trick, settings, logs, permissions, installedApps, penTesting)
    }
}
