package com.crackmydroid.shared.ui
import com.crackmydroid.ui.icons.AppIcons

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.crackmydroid.shared.domain.model.app.AppLanguage
import com.crackmydroid.shared.domain.model.app.AppTheme
import com.crackmydroid.shared.i18n.LocalStrings
import com.crackmydroid.shared.i18n.ScreenUiStrings
import com.crackmydroid.shared.i18n.Strings
import com.crackmydroid.shared.i18n.format
import com.crackmydroid.shared.i18n.platformLanguage
import com.crackmydroid.shared.i18n.resolveLanguageCode
import com.crackmydroid.shared.i18n.stringsFor
import com.crackmydroid.shared.i18n.toUiStrings
import com.crackmydroid.shared.presentation.navigation.Screen
import com.crackmydroid.shared.presentation.navigation.drawerScreens
import com.crackmydroid.shared.ui.activities.view.ActivityListScreen
import com.crackmydroid.shared.ui.apkcontent.view.AnalyzeApkContentScreen
import com.crackmydroid.shared.ui.navigation.AppNavigationEvent
import com.crackmydroid.shared.ui.help.HelpScreen
import com.crackmydroid.shared.ui.home.view.HomeScreen
import com.crackmydroid.shared.ui.info.view.InfoScreen
import com.crackmydroid.shared.ui.installedapps.view.InstalledAppsScreen
import com.crackmydroid.shared.ui.logs.view.LogScreen
import com.crackmydroid.shared.ui.pentest.view.PenTestingScreen
import com.crackmydroid.shared.ui.permissions.view.PermissionsScreen
import com.crackmydroid.shared.ui.root.view.RootScreen
import com.crackmydroid.shared.ui.settings.model.SettingsAppInfo
import com.crackmydroid.shared.ui.settings.view.SettingsScreen
import com.crackmydroid.shared.ui.trick.view.TrickScreen
import com.crackmydroid.shared.util.appPackageName
import com.crackmydroid.shared.util.appRequestedPermissions
import com.crackmydroid.shared.util.appSdkInfo
import com.crackmydroid.shared.util.appVersionName
import com.crackmydroid.ui.components.AccessibilityPrefs
import com.crackmydroid.ui.components.AppBackHandler
import com.crackmydroid.ui.components.AppIcon
import com.crackmydroid.ui.components.dialog.AppDialog
import com.crackmydroid.ui.components.LocalAccessibilityPrefs
import com.crackmydroid.ui.components.PackageIcon
import com.crackmydroid.ui.components.effectiveReduceMotion
import com.crackmydroid.ui.components.text.AppText
import com.crackmydroid.ui.components.navigation.AppBottomBar
import com.crackmydroid.ui.components.navigation.AppBottomBarItem
import com.crackmydroid.ui.components.navigation.AppTopBar
import com.crackmydroid.ui.components.navigation.AppTopBarNavigationAction
import com.crackmydroid.ui.strings.LocalUiStrings
import com.crackmydroid.ui.theme.CrackMyDroidTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrackMyDroidApp(
    dependencies: AppPresentationDependencies,
    onLanguageChanged: (AppLanguage) -> Unit = {},
    systemMotionReductionEnabled: Boolean = false,
    onExitApp: (() -> Unit)? = null,
) {
    val drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val androidAppVersionName = appVersionName()
    val runtimePackageName = appPackageName()
    val sdkInfo = appSdkInfo()
    val requestedPermissions = appRequestedPermissions()
    val settingsAppInfo = remember(
        androidAppVersionName,
        runtimePackageName,
        sdkInfo,
        requestedPermissions,
    ) {
        SettingsAppInfo(
            versionName = androidAppVersionName,
            packageName = runtimePackageName,
            minSdk = sdkInfo?.minSdk,
            targetSdk = sdkInfo?.targetSdk,
            requestedPermissions = requestedPermissions,
        )
    }
    val screenStateHolder = rememberSaveableStateHolder()
    var showExitConfirmDialog by remember { mutableStateOf(false) }

    val activityVm = dependencies.activity
    val rootVm = dependencies.root
    val infoVm = dependencies.info
    val trickVm = dependencies.trick
    val settingsVm = dependencies.settings
    val logVm = dependencies.logs
    val permissionsVm = dependencies.permissions
    val installedAppsVm = dependencies.installedApps
    val penTestingVm = dependencies.penTesting
    val apkContentVm = dependencies.apkContent
    val homeVm = dependencies.home
    val navigationVm = dependencies.navigation
    val navigationState by navigationVm.state.collectAsState()
    val settingsState = settingsVm.state.collectAsState()
    val languageCode = resolveLanguageCode(settingsState.value.language, platformLanguage())

    AppBackHandler(enabled = true) {
        when {
            showExitConfirmDialog -> {
                showExitConfirmDialog = false
            }

            drawerState.isOpen -> {
                scope.launch { drawerState.close() }
            }

            navigationState.selectedScreen != Screen.Home || navigationState.navigationHistory.isNotEmpty() -> {
                navigationVm.emitEvent(AppNavigationEvent.BackRequested())
            }

            onExitApp != null -> {
                showExitConfirmDialog = true
            }
        }
    }
    val strings = stringsFor(languageCode)
    val screenUi = remember(languageCode) { ScreenUiStrings(languageCode) }
    val accessibilityPrefs = AccessibilityPrefs(
        highContrast = settingsState.value.highContrast,
        reduceMotion = effectiveReduceMotion(
            preferenceEnabled = settingsState.value.reduceMotion,
            systemMotionReductionEnabled = systemMotionReductionEnabled,
        ),
        largeText = settingsState.value.largeText,
    )

    LaunchedEffect(Unit) { navigationVm.emitEvent(AppNavigationEvent.Initialize()) }

    LaunchedEffect(settingsState.value.featureHintsEnabled) {
        if (!settingsState.value.featureHintsEnabled) {
            navigationVm.emitEvent(AppNavigationEvent.HintDismissed())
        }
    }

    LaunchedEffect(settingsState.value.language) {
        onLanguageChanged(settingsState.value.language)
    }
    LaunchedEffect(languageCode, navigationState.initialized) {
        if (navigationState.initialized) navigationVm.updateLanguage(languageCode)
    }

    val darkTheme = when (settingsState.value.theme) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }

    CompositionLocalProvider(
        LocalStrings provides strings,
        LocalUiStrings provides strings.toUiStrings(),
        LocalAccessibilityPrefs provides accessibilityPrefs,
    ) {
        CrackMyDroidTheme(
            darkTheme = darkTheme,
            highContrast = accessibilityPrefs.highContrast,
        ) {
            val navigateFromDrawer: (Screen) -> Unit = { target ->
                scope.launch { drawerState.close() }
                navigationVm.emitEvent(
                    AppNavigationEvent.ScreenSelected(
                        screen = target,
                        origin = com.crackmydroid.shared.ui.navigation.NavigationSource.Drawer,
                        hintsEnabled = settingsState.value.featureHintsEnabled,
                    ),
                )
            }
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val isWideScreen = maxWidth >= 720.dp
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp, vertical = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    PackageIcon(
                                        packageName = runtimePackageName,
                                        size = 40.dp,
                                        contentPadding = 0.dp,
                                    )
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        AppText(
                                            text = "CrackMyDroid",
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                        AppText(
                                            text = strings.appSubtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = if (accessibilityPrefs.largeText) 3 else 2,
                                        )
                                    }
                                }
                                HorizontalDivider()
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                ) {
                                    drawerScreens.forEach { screen ->
                                        NavigationDrawerItem(
                                            icon = { AppIcon(screen.icon, contentDescription = null) },
                                            label = { AppText(screen.title(strings)) },
                                            selected = navigationState.selectedScreen == screen,
                                            onClick = { navigateFromDrawer(screen) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp),
                                        )
                                    }
                                }
                                HorizontalDivider()
                                AppText(
                                    text = strings.appVersionLabel.format(androidAppVersionName),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .padding(vertical = 8.dp),
                                )
                            }
                        }
                    },
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        if (isWideScreen) {
                            NavigationRail(
                                modifier = Modifier.fillMaxHeight(),
                                header = {
                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                if (drawerState.isOpen) drawerState.close() else drawerState.open()
                                            }
                                        },
                                    ) {
                                    AppIcon(AppIcons.Filled.Menu, contentDescription = "Menu")
                                    }
                                },
                            ) {
                                val itemIconSize = 22.dp
                                val railScreens = listOf(
                                    Screen.Home,
                                    Screen.InstalledApps,
                                    Screen.Pentest,
                                    Screen.AnalyzeApkContent,
                                    Screen.Permissions,
                                    Screen.Activities,
                                    Screen.Root,
                                    Screen.Info,
                                    Screen.Settings,
                                )
                                Spacer(Modifier.height(8.dp))
                                railScreens.forEach { screen ->
                                    NavigationRailItem(
                                        selected = navigationState.selectedScreen == screen,
                                        onClick = {
                                            navigationVm.emitEvent(
                                                AppNavigationEvent.ScreenSelected(
                                                    screen,
                                                    com.crackmydroid.shared.ui.navigation.NavigationSource.Rail,
                                                    false,
                                                ),
                                            )
                                        },
                                        icon = {
                                            AppIcon(
                                                screen.icon,
                                                contentDescription = screen.title(strings),
                                                modifier = Modifier.size(itemIconSize),
                                            )
                                        },
                                        label = {
                                            AppText(
                                                screen.title(strings),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        },
                                        alwaysShowLabel = false,
                                    )
                                }
                            }
                        }
                        Scaffold(
                            modifier = Modifier.weight(1f),
                            contentWindowInsets = WindowInsets.safeDrawing,
                            topBar = {
                                AppTopBar(
                                    title = navigationState.topBar.title,
                                    navigationAction = navigationState.topBar.navigationAction,
                                    onNavigationClick = {
                                        when (navigationState.topBar.navigationAction) {
                                            AppTopBarNavigationAction.Menu -> scope.launch { drawerState.open() }
                                            AppTopBarNavigationAction.Back -> navigationVm.emitEvent(AppNavigationEvent.BackRequested())
                                            AppTopBarNavigationAction.None -> Unit
                                        }
                                    },
                                    showSearch = navigationState.topBar.showSearch,
                                    onSearchClick = { navigationVm.emitEvent(AppNavigationEvent.TopBarSearchRequested()) },
                                    showHelp = navigationState.topBar.showHelp,
                                    onHelpClick = { navigationVm.emitEvent(AppNavigationEvent.TopBarHelpRequested()) },
                                )
                            },
                            bottomBar = {
                                if (!isWideScreen) {
                                    val bottomBarItems = remember(navigationState.bottomBarItems) {
                                        navigationState.bottomBarItems.map { item ->
                                            AppBottomBarItem(
                                                id = item.screen.id,
                                                label = item.label,
                                                icon = item.icon,
                                                selected = item.selected,
                                            )
                                        }
                                    }
                                    AppBottomBar(
                                        items = bottomBarItems,
                                        onItemClick = { id ->
                                            navigationState.bottomBarItems
                                                .firstOrNull { it.screen.id == id }
                                                ?.screen
                                                ?.let { screen ->
                                                    navigationVm.emitEvent(
                                                        AppNavigationEvent.ScreenSelected(
                                                            screen,
                                                            com.crackmydroid.shared.ui.navigation.NavigationSource.BottomBar,
                                                            false,
                                                        ),
                                                    )
                                                }
                                        },
                                    )
                                }
                            },
                        ) { padding ->
                        val focusManager = LocalFocusManager.current
                        val adjustedPadding = padding
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    awaitEachGesture {
                                        awaitFirstDown(pass = PointerEventPass.Final)
                                        val up =
                                            waitForUpOrCancellation(pass = PointerEventPass.Final)
                                        if (up != null && !up.isConsumed) {
                                            focusManager.clearFocus()
                                        }
                                    }
                                }
                                .background(MaterialTheme.colorScheme.background),
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                Crossfade(
                                    targetState = navigationState.selectedScreen,
                                    animationSpec = if (accessibilityPrefs.reduceMotion) tween(0) else tween(150),
                                    label = "ScreenCrossfade",
                                ) { targetScreen ->
                                    screenStateHolder.SaveableStateProvider(targetScreen.id) {
                                        when (targetScreen) {
                                    Screen.Home -> HomeScreen(
                                        viewModel = homeVm,
                                        padding = adjustedPadding,
                                        languageCode = languageCode,
                                    )

                                    Screen.Activities -> ActivityListScreen(
                                        viewModel = activityVm,
                                        padding = adjustedPadding,
                                        searchRequestId = navigationState.topBar.searchRequestId,
                                        searchVisible = navigationState.topBar.searchVisible,
                                        onSearchVisibilityChanged = { visible ->
                                            navigationVm.emitEvent(AppNavigationEvent.SearchPanelVisibilityChanged(visible))
                                        },
                                    )

                                    Screen.Root -> RootScreen(
                                        rootVm,
                                        adjustedPadding,
                                    )

                                    Screen.Info -> InfoScreen(
                                        infoVm,
                                        adjustedPadding,
                                    )

                                    Screen.Trick -> TrickScreen(
                                        trickVm,
                                        adjustedPadding,
                                    )

                                    Screen.Pentest -> PenTestingScreen(
                                        penTestingVm,
                                        adjustedPadding,
                                        searchRequestId = navigationState.topBar.searchRequestId,
                                        searchVisible = navigationState.topBar.searchVisible,
                                        onSearchVisibilityChanged = { visible ->
                                            navigationVm.emitEvent(AppNavigationEvent.SearchPanelVisibilityChanged(visible))
                                        },
                                    )

                                    Screen.AnalyzeApkContent -> AnalyzeApkContentScreen(
                                        apkContentVm,
                                        adjustedPadding,
                                        searchRequestId = navigationState.topBar.searchRequestId,
                                        searchVisible = navigationState.topBar.searchVisible,
                                        onSearchVisibilityChanged = { visible ->
                                            navigationVm.emitEvent(AppNavigationEvent.SearchPanelVisibilityChanged(visible))
                                        },
                                    )

                                    Screen.InstalledApps -> InstalledAppsScreen(
                                        installedAppsVm,
                                        adjustedPadding,
                                        searchRequestId = navigationState.topBar.searchRequestId,
                                        searchVisible = navigationState.topBar.searchVisible,
                                        onSearchVisibilityChanged = { visible ->
                                            navigationVm.emitEvent(AppNavigationEvent.SearchPanelVisibilityChanged(visible))
                                        },
                                    )

                                    Screen.Permissions -> PermissionsScreen(
                                        permissionsVm,
                                        adjustedPadding,
                                        searchRequestId = navigationState.topBar.searchRequestId,
                                        searchVisible = navigationState.topBar.searchVisible,
                                        onSearchVisibilityChanged = { visible ->
                                            navigationVm.emitEvent(AppNavigationEvent.SearchPanelVisibilityChanged(visible))
                                        },
                                    )

                                    Screen.Log -> LogScreen(
                                        logVm,
                                        adjustedPadding,
                                        searchRequestId = navigationState.topBar.searchRequestId,
                                        searchVisible = navigationState.topBar.searchVisible,
                                        onSearchVisibilityChanged = { visible ->
                                            navigationVm.emitEvent(AppNavigationEvent.SearchPanelVisibilityChanged(visible))
                                        },
                                    )

                                    Screen.Settings -> SettingsScreen(
                                        settingsVm,
                                        adjustedPadding,
                                        settingsAppInfo,
                                    )

                                    Screen.Help -> HelpScreen(
                                        padding = adjustedPadding,
                                        onNavigate = { target ->
                                            navigationVm.emitEvent(
                                                AppNavigationEvent.ScreenSelected(
                                                    target,
                                                    com.crackmydroid.shared.ui.navigation.NavigationSource.InContent,
                                                    false,
                                                ),
                                            )
                                        },
                                    )
                                }
                            }
                                }
                        }
                    }
                }
            }
            }
                Spacer(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .windowInsetsTopHeight(WindowInsets.statusBars)
                        .background(MaterialTheme.colorScheme.background),
                )
            }

            if (showExitConfirmDialog) {
                AppDialog(
                    title = screenUi.exitConfirmTitle,
                    confirmLabel = screenUi.exitConfirmButton,
                    dismissLabel = screenUi.exitCancelButton,
                    onConfirm = {
                        showExitConfirmDialog = false
                        onExitApp?.invoke()
                    },
                    onDismiss = { showExitConfirmDialog = false },
                    onDismissAction = { showExitConfirmDialog = false },
                ) {
                    AppText(screenUi.exitConfirmMessage)
                }
            }
        }

        if (navigationState.showIntro) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { navigationVm.emitEvent(AppNavigationEvent.IntroDismissed()) },
                title = { AppText(strings.introTitle) },
                text = { AppText(strings.introBody) },
                confirmButton = {
                    androidx.compose.material3.Button(onClick = {
                        navigationVm.emitEvent(AppNavigationEvent.IntroDismissed())
                    }) {
                        AppText(strings.introAgree)
                    }
                },
            )
        }

        navigationState.pendingHintScreen?.let { target ->
            sectionEntryHintText(strings, target)?.let { hint ->
                var disableHintsForever by remember(target) { mutableStateOf(false) }
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = {},
                    title = { AppText(strings.sectionHintTitle.format(target.title(strings))) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            AppText(hint)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(
                                    checked = disableHintsForever,
                                    onCheckedChange = { disableHintsForever = it },
                                )
                                AppText(strings.sectionHintDisable)
                            }
                        }
                    },
                    confirmButton = {
                        androidx.compose.material3.Button(
                            onClick = {
                                navigationVm.emitEvent(AppNavigationEvent.HintConfirmed(disableHintsForever, target))
                            },
                        ) {
                            AppText(strings.sectionHintConfirm)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                navigationVm.emitEvent(AppNavigationEvent.HintDismissed())
                            },
                        ) {
                            AppText(strings.close)
                        }
                    },
                )
            }
        }

        navigationState.helpDialog?.let { msg ->
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { navigationVm.emitEvent(AppNavigationEvent.HelpDismissed()) },
                confirmButton = {
                    androidx.compose.material3.TextButton(onClick = {
                        navigationVm.emitEvent(AppNavigationEvent.HelpDismissed())
                    }) {
                        AppText(strings.close)
                    }
                },
                title = { AppText(strings.faqTitle) },
                text = { AppText(msg) },
            )
        }
    }
}

private fun sectionEntryHintText(strings: Strings, screen: Screen): String? = when (screen) {
    Screen.Permissions -> "${strings.helpPermissions}\n\nSuggerimento: usa la ricerca per app o permesso e condividi rapidamente i risultati dalla riga dell'app."
    Screen.InstalledApps -> "${strings.helpApk}\n\nSuggerimento: cerca per nome/package e usa le azioni rapide per estrarre o condividere APK."
    Screen.Pentest -> "${strings.pentestIntro}\n\nWorkflow consigliato: 1) scegli target o auto-scan, 2) seleziona test, 3) controlla risultati a video, 4) condividi subito."
    Screen.AnalyzeApkContent -> "Select an installed app, inspect its APK locally, then export the static-analysis report."
    Screen.Activities -> "${strings.helpActivities}\n\nSuggerimento: filtra con cerca/chip preferiti e condividi le activity per singola app."
    Screen.Root -> "Esegue un check rapido del root e della Play Integrity: usa RootBeer per rilevare tracce di rooting e verifica il nonce con Play Integrity."
    Screen.Trick -> "${strings.helpTrick}\n\nSuggerimento: conferma i comandi sensibili e controlla sempre output/exit code."
    else -> null
}
