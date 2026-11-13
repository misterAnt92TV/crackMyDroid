package com.crackmydroid.shared.presentation

import com.crackmydroid.architecture.testing.*


import com.crackmydroid.shared.domain.usecase.settings.GetIntroSeenUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetVerboseLogUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetIntroSeenUseCase
import com.crackmydroid.shared.presentation.navigation.Screen
import com.crackmydroid.shared.ui.activities.ActivityListEvent
import com.crackmydroid.shared.ui.activities.ActivityListViewModel
import com.crackmydroid.shared.ui.apkcontent.AnalyzeApkContentViewModel
import com.crackmydroid.shared.ui.home.HomeViewModel
import com.crackmydroid.shared.ui.info.InfoEvent
import com.crackmydroid.shared.ui.info.InfoViewModel
import com.crackmydroid.shared.ui.installedapps.InstalledAppsViewModel
import com.crackmydroid.shared.ui.logs.LogViewModel
import com.crackmydroid.shared.ui.navigation.AppNavigationEvent
import com.crackmydroid.shared.ui.navigation.AppNavigationViewModel
import com.crackmydroid.shared.ui.navigation.NavigationSource
import com.crackmydroid.shared.ui.pentest.PenTestingState
import com.crackmydroid.shared.ui.pentest.PenTestingViewModel
import com.crackmydroid.shared.ui.permissions.PermissionsViewModel
import com.crackmydroid.shared.ui.root.RootViewModel
import com.crackmydroid.shared.ui.settings.SettingsEvent
import com.crackmydroid.shared.ui.settings.SettingsState
import com.crackmydroid.shared.ui.settings.SettingsViewModel
import com.crackmydroid.shared.ui.trick.TrickViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(ExperimentalCoroutinesApi::class)
class AppNavigationViewModelTest : BaseViewModelUnitTest() {
    private val getIntroSeen = mockk<GetIntroSeenUseCase>()
    private val setIntroSeen = mockk<SetIntroSeenUseCase>()
    private val getVerboseLog = mockk<GetVerboseLogUseCase>()

    private val activityVm = mockk<ActivityListViewModel>(relaxed = true)
    private val homeVm = mockk<HomeViewModel>(relaxed = true)
    private val rootVm = mockk<RootViewModel>(relaxed = true)
    private val infoVm = mockk<InfoViewModel>(relaxed = true)
    private val trickVm = mockk<TrickViewModel>(relaxed = true)
    private val settingsVm = mockk<SettingsViewModel>(relaxed = true)
    private val logsVm = mockk<LogViewModel>(relaxed = true)
    private val permissionsVm = mockk<PermissionsViewModel>(relaxed = true)
    private val installedAppsVm = mockk<InstalledAppsViewModel>(relaxed = true)
    private val penTestingVm = mockk<PenTestingViewModel>(relaxed = true)
    private val apkContentVm = mockk<AnalyzeApkContentViewModel>(relaxed = true)

    private val penTestingState = MutableStateFlow(PenTestingState())
    private val settingsState = MutableStateFlow(SettingsState())
    private val apkContentState = MutableStateFlow(com.crackmydroid.shared.ui.apkcontent.AnalyzeApkContentState())

    override fun setUp() {
        super.setUp()
        coEvery { getIntroSeen() } returns Result.success(true)
        coEvery { setIntroSeen(any()) } returns Result.success(Unit)
        coEvery { getVerboseLog() } returns Result.success(false)
        every { penTestingVm.state } returns penTestingState
        every { settingsVm.state } returns settingsState
        every { apkContentVm.state } returns apkContentState
    }

    private fun createViewModel() = autoClear(
        AppNavigationViewModel(
            getIntroSeenUseCase = getIntroSeen,
            setIntroSeenUseCase = setIntroSeen,
            activity = activityVm,
            home = homeVm,
            root = rootVm,
            info = infoVm,
            trick = trickVm,
            settings = settingsVm,
            logs = logsVm,
            permissions = permissionsVm,
            installedApps = installedAppsVm,
            penTesting = penTestingVm,
            apkContent = apkContentVm,
            getVerboseLogUseCase = getVerboseLog,
            dispatchers = appDispatchers,
        ),
    )

    @Test
    fun initialize_whenIntroSeen_marksInitializedAndHidesIntro() = runViewModelTest {
        coEvery { getIntroSeen() } returns Result.success(true)
        val vm = createViewModel()

        val state = vm.emitEventAndAwaitIdle(AppNavigationEvent.Initialize())

        assertTrue(state.initialized)
        assertFalse(state.showIntro)
        verify { settingsVm.emitEvent(match { it is SettingsEvent.Load }) }
        verify { activityVm.emitEvent(match { it is ActivityListEvent.Refresh }) }
        verify { infoVm.emitEvent(match { it is InfoEvent.Load }) }
    }

    @Test
    fun initialize_whenIntroNotSeen_showsIntro() = runViewModelTest {
        coEvery { getIntroSeen() } returns Result.success(false)
        val vm = createViewModel()

        val state = vm.emitEventAndAwaitIdle(AppNavigationEvent.Initialize())

        assertTrue(state.initialized)
        assertTrue(state.showIntro)
    }

    @Test
    fun screenSelected_withoutHints_navigatesDirectlyAndAppendsHistory() = runViewModelTest {
        val vm = createViewModel()

        val state = vm.emitEventAndAwaitIdle(
            AppNavigationEvent.ScreenSelected(
                screen = Screen.Info,
                origin = NavigationSource.Drawer,
                hintsEnabled = false,
            ),
        )

        assertEquals(Screen.Info, state.selectedScreen)
        assertEquals(listOf(Screen.Home), state.navigationHistory)
        assertNull(state.pendingHintScreen)
    }

    @Test
    fun screenSelected_withHintsOnHintedScreen_setsPendingHint() = runViewModelTest {
        val vm = createViewModel()

        val state = vm.emitEventAndAwaitIdle(
            AppNavigationEvent.ScreenSelected(
                screen = Screen.Permissions,
                origin = NavigationSource.Drawer,
                hintsEnabled = true,
            ),
        )

        assertEquals(Screen.Home, state.selectedScreen)
        assertEquals(Screen.Permissions, state.pendingHintScreen)
        assertEquals(NavigationSource.Drawer, state.pendingNavigationSource)
    }

    @Test
    fun hintConfirmed_navigatesAndCanDisableHints() = runViewModelTest {
        val vm = createViewModel()
        vm.emitEventAndAwaitIdle(
            AppNavigationEvent.ScreenSelected(
                screen = Screen.Permissions,
                origin = NavigationSource.Drawer,
                hintsEnabled = true,
            ),
        )

        val state = vm.emitEventAndAwaitIdle(
            AppNavigationEvent.HintConfirmed(
                disableHints = true,
                target = Screen.Permissions,
            ),
        )

        assertEquals(Screen.Permissions, state.selectedScreen)
        assertNull(state.pendingHintScreen)
        verify { settingsVm.emitEvent(match { it is SettingsEvent.FeatureHintsEnabledChanged && !it.enabled }) }
    }

    @Test
    fun hintDismissed_cancelsPendingNavigation() = runViewModelTest {
        val vm = createViewModel()
        vm.emitEventAndAwaitIdle(
            AppNavigationEvent.ScreenSelected(
                screen = Screen.Permissions,
                origin = NavigationSource.Drawer,
                hintsEnabled = true,
            ),
        )

        val state = vm.emitEventAndAwaitIdle(AppNavigationEvent.HintDismissed())

        assertEquals(Screen.Home, state.selectedScreen)
        assertNull(state.pendingHintScreen)
        assertNull(state.pendingNavigationSource)
    }

    @Test
    fun backRequested_navigatesBackThroughHistory() = runViewModelTest {
        val vm = createViewModel()
        vm.emitEventAndAwaitIdle(AppNavigationEvent.ScreenSelected(Screen.Info, NavigationSource.Drawer, false))
        vm.emitEventAndAwaitIdle(AppNavigationEvent.ScreenSelected(Screen.Settings, NavigationSource.Drawer, false))

        val step1 = vm.emitEventAndAwaitIdle(AppNavigationEvent.BackRequested())
        assertEquals(Screen.Info, step1.selectedScreen)
        assertEquals(listOf(Screen.Home), step1.navigationHistory)

        val step2 = vm.emitEventAndAwaitIdle(AppNavigationEvent.BackRequested())
        assertEquals(Screen.Home, step2.selectedScreen)
        assertEquals(emptyList(), step2.navigationHistory)
    }

    @Test
    fun topBarSearchRequested_and_searchPanelVisibilityChanged() = runViewModelTest {
        val vm = createViewModel()
        vm.emitEventAndAwaitIdle(AppNavigationEvent.ScreenSelected(Screen.Activities, NavigationSource.Drawer, false))

        val toggled = vm.emitEventAndAwaitIdle(AppNavigationEvent.TopBarSearchRequested())
        assertEquals(false, toggled.topBar.searchVisible)

        val explicit = vm.emitEventAndAwaitIdle(AppNavigationEvent.SearchPanelVisibilityChanged(true))
        assertEquals(true, explicit.topBar.searchVisible)
    }

    @Test
    fun helpDialog_requestedAndDismissed() = runViewModelTest {
        val vm = createViewModel()

        val shown = vm.emitEventAndAwaitIdle(AppNavigationEvent.TopBarHelpRequested())
        assertTrue(shown.helpDialog != null)

        val dismissed = vm.emitEventAndAwaitIdle(AppNavigationEvent.HelpDismissed())
        assertNull(dismissed.helpDialog)
    }

    @Test
    fun introDismissed_persistsSeenAndHidesIntro() = runViewModelTest {
        coEvery { getIntroSeen() } returns Result.success(false)
        val vm = createViewModel()
        vm.emitEventAndAwaitIdle(AppNavigationEvent.Initialize())

        val state = vm.emitEventAndAwaitIdle(AppNavigationEvent.IntroDismissed())

        assertFalse(state.showIntro)
        coVerify { setIntroSeen(true) }
    }
}
