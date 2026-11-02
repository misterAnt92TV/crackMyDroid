package com.crackmydroid.shared.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Checkbox
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import kotlinx.coroutines.launch
import com.crackmydroid.shared.util.appVersionName
import com.crackmydroid.shared.util.appPackageName
import androidx.compose.material.icons.Icons
import com.crackmydroid.shared.domain.model.AppTheme
import com.crackmydroid.shared.i18n.LocalStrings
import com.crackmydroid.shared.i18n.Strings
import com.crackmydroid.shared.i18n.platformLanguage
import com.crackmydroid.shared.i18n.stringsFor
import com.crackmydroid.shared.ui.activities.ActivityListViewModel
import com.crackmydroid.shared.ui.info.InfoViewModel
import com.crackmydroid.shared.ui.root.RootViewModel
import com.crackmydroid.shared.ui.settings.SettingsViewModel
import com.crackmydroid.shared.ui.trick.TrickViewModel
import com.crackmydroid.shared.ui.logs.LogViewModel
import com.crackmydroid.shared.ui.permissions.PermissionsViewModel
import com.crackmydroid.shared.ui.installedapps.InstalledAppsViewModel
import com.crackmydroid.shared.ui.pentest.PenTestingViewModel
import com.crackmydroid.shared.ui.screens.HomeScreen
import com.crackmydroid.shared.domain.usecase.GetIntroSeenUseCase
import com.crackmydroid.shared.domain.usecase.SetIntroSeenUseCase
import com.crackmydroid.shared.domain.repository.OperationLogRepository
import com.crackmydroid.shared.presentation.navigation.Screen
import com.crackmydroid.shared.presentation.navigation.bottomScreens
import com.crackmydroid.shared.presentation.navigation.drawerScreens
import com.crackmydroid.shared.ui.screens.ActivityListScreen
import com.crackmydroid.shared.ui.screens.InfoScreen
import com.crackmydroid.shared.ui.screens.RootScreen
import com.crackmydroid.shared.ui.screens.SettingsScreen
import com.crackmydroid.shared.ui.screens.TrickScreen
import com.crackmydroid.shared.ui.screens.LogScreen
import com.crackmydroid.shared.ui.screens.PermissionsScreen
import com.crackmydroid.shared.ui.screens.InstalledAppsScreen
import com.crackmydroid.shared.ui.screens.PenTestingScreen
import com.crackmydroid.shared.ui.screens.HelpScreen
import org.koin.mp.KoinPlatform
import com.crackmydroid.shared.presentation.components.LocalAccessibilityPrefs
import com.crackmydroid.shared.presentation.components.AccessibilityPrefs
import com.crackmydroid.shared.presentation.components.SearchHistoryStore
import com.crackmydroid.shared.presentation.components.PackageIcon
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun CrackMyDroidApp() {
    val selected: MutableState<Screen> = remember { mutableStateOf<Screen>(Screen.Home) }
    val drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val androidAppVersionName = appVersionName()

    val activityVm = rememberVM<ActivityListViewModel>()
    val rootVm = rememberVM<RootViewModel>()
    val infoVm = rememberVM<InfoViewModel>()
    val trickVm = rememberVM<TrickViewModel>()
    val settingsVm = rememberVM<SettingsViewModel>()
    val logVm = rememberVM<LogViewModel>()
    val permissionsVm = rememberVM<PermissionsViewModel>()
    val installedAppsVm = rememberVM<InstalledAppsViewModel>()
    val penTestingVm = rememberVM<PenTestingViewModel>()
    val operationLogs = remember { KoinPlatform.getKoin().get<OperationLogRepository>() }
    val helpDialog = remember { mutableStateOf<String?>(null) }
    val introSeenUse = remember { KoinPlatform.getKoin().get<GetIntroSeenUseCase>() }
    val setIntroSeenUse = remember { KoinPlatform.getKoin().get<SetIntroSeenUseCase>() }
    val settingsState = settingsVm.state.collectAsState()
    val strings = stringsFor(settingsState.value.language.code)
    val showIntro = remember { mutableStateOf(true) }
    val sectionHintDialog = remember { mutableStateOf<Screen?>(null) }
    val accessibilityPrefs = AccessibilityPrefs(
        highContrast = settingsState.value.highContrast,
        reduceMotion = settingsState.value.reduceMotion,
        largeText = settingsState.value.largeText
    )

    LaunchedEffect(Unit) {
        val seen = introSeenUse()
        showIntro.value = !seen
        activityVm.refresh()
        infoVm.load()
        trickVm.load()
        settingsVm.load()
        logVm.refresh()
        permissionsVm.refresh()
        installedAppsVm.refresh()
        penTestingVm.refresh()
    }

    LaunchedEffect(settingsState.value.suggestionsEnabled) {
        SearchHistoryStore.setEnabled(settingsState.value.suggestionsEnabled)
    }

    LaunchedEffect(settingsState.value.featureHintsEnabled) {
        if (!settingsState.value.featureHintsEnabled) {
            sectionHintDialog.value = null
        }
    }

    val darkTheme = when (settingsState.value.theme) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }

    val lightPalette = lightColorScheme(
        primary = Color(0xFF1A73E8),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFD2E3FC),
        onPrimaryContainer = Color(0xFF0B3C74),
        secondary = Color(0xFF188038),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFCEEAD6),
        onSecondaryContainer = Color(0xFF0F3A1B),
        tertiary = Color(0xFFF9AB00),
        onTertiary = Color(0xFF2F1F00),
        tertiaryContainer = Color(0xFFFEEFC3),
        onTertiaryContainer = Color(0xFF4A3300),
        error = Color(0xFFD93025),
        onError = Color.White,
        errorContainer = Color(0xFFFDE8E7),
        onErrorContainer = Color(0xFF5C0A05),
        background = Color(0xFFF7F9FC),
        onBackground = Color(0xFF1C1F26),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF1C1F26),
        surfaceVariant = Color(0xFFE9EDF4),
        onSurfaceVariant = Color(0xFF444A56),
        outline = Color(0xFF737A88),
        outlineVariant = Color(0xFFC2C8D3)
    )
    val darkPalette = darkColorScheme(
        primary = Color(0xFFA8C7FA),
        onPrimary = Color(0xFF053062),
        primaryContainer = Color(0xFF174EA6),
        onPrimaryContainer = Color(0xFFD8E2FF),
        secondary = Color(0xFF81C995),
        onSecondary = Color(0xFF0B3819),
        secondaryContainer = Color(0xFF0F5223),
        onSecondaryContainer = Color(0xFFB7F1C3),
        tertiary = Color(0xFFFDD663),
        onTertiary = Color(0xFF443100),
        tertiaryContainer = Color(0xFF604600),
        onTertiaryContainer = Color(0xFFFFE59A),
        error = Color(0xFFF28B82),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF8C1D18),
        onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF101418),
        onBackground = Color(0xFFE3E8F0),
        surface = Color(0xFF101418),
        onSurface = Color(0xFFE3E8F0),
        surfaceVariant = Color(0xFF3F4652),
        onSurfaceVariant = Color(0xFFC0C7D2),
        outline = Color(0xFF8A92A1),
        outlineVariant = Color(0xFF3F4652)
    )

    val colorScheme = if (darkTheme) darkPalette else lightPalette
    MaterialTheme(colorScheme = colorScheme) {
        CompositionLocalProvider(
            LocalStrings provides strings,
            LocalAccessibilityPrefs provides accessibilityPrefs
        ) {
            val navigateFromDrawer: (Screen) -> Unit = { target ->
                scope.launch { drawerState.close() }
                if (selected.value == target) {
                    Unit
                } else if (
                    settingsState.value.featureHintsEnabled &&
                    shouldShowEntryHintDialog(target)
                ) {
                    sectionHintDialog.value = target
                } else {
                    selected.value = target
                }
            }
            Box(modifier = Modifier.fillMaxSize()) {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                    ModalDrawerSheet(
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                PackageIcon(
                                    packageName = appPackageName(),
                                    size = 40.dp,
                                    contentPadding = 0.dp
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "CrackMyDroid",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Toolkit Android per test rapidi di sicurezza",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = if (accessibilityPrefs.largeText) 3 else 2
                                    )
                                }
                            }
                            HorizontalDivider()
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                drawerScreens.forEach { screen ->
                                    NavigationDrawerItem(
                                        icon = { Icon(screen.icon, contentDescription = null) },
                                        label = { Text(screen.title(strings)) },
                                        selected = selected.value == screen,
                                        onClick = { navigateFromDrawer(screen) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp)
                                    )
                                }
                            }
                            HorizontalDivider()
                            Text(
                                text = "Versione $androidAppVersionName",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                            )
                        }
                    }
                    }
                ) {
                val showBottomBar = bottomScreens.contains(selected.value)
                val navHeight = if (accessibilityPrefs.largeText) 68.dp else 56.dp
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing,
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar(
                                modifier = Modifier
                                    .height(navHeight),
                                windowInsets = NavigationBarDefaults.windowInsets,
                                tonalElevation = 3.dp
                            ) {
                                val itemIconSize = 20.dp
                                bottomScreens.forEach { screen ->
                                    NavigationBarItem(
                                        selected = selected.value == screen,
                                        onClick = { selected.value = screen },
                                        icon = { Icon(screen.icon, contentDescription = screen.title(strings), modifier = Modifier.size(itemIconSize)) },
                                        label = {
                                            Text(
                                                screen.title(strings),
                                                maxLines = if (accessibilityPrefs.largeText) 2 else 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        alwaysShowLabel = true
                                    )
                                }
                            }
                        }
                    }
                ) { padding ->
                    val layoutDirection = LocalLayoutDirection.current
                    val focusManager = LocalFocusManager.current
                    val adjustedPadding = PaddingValues(
                        start = padding.calculateStartPadding(layoutDirection),
                        top = padding.calculateTopPadding(),
                        end = padding.calculateEndPadding(layoutDirection),
                        bottom = if (showBottomBar) navHeight else 0.dp
                    )
                    val openDrawer: () -> Unit = { scope.launch { drawerState.open() } }
                    val backToHome: () -> Unit = { selected.value = Screen.Home }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                awaitEachGesture {
                                    awaitFirstDown(pass = PointerEventPass.Final)
                                    val up = waitForUpOrCancellation(pass = PointerEventPass.Final)
                                    if (up != null && !up.isConsumed) {
                                        focusManager.clearFocus()
                                    }
                                }
                            }
                            .background(MaterialTheme.colorScheme.background)
                            .imePadding()
                    ) {
                        val reduceMotion = accessibilityPrefs.reduceMotion
                        AnimatedContent(
                            targetState = selected.value,
                            transitionSpec = {
                                if (reduceMotion) {
                                    fadeIn(tween(1)) togetherWith fadeOut(tween(1))
                                } else {
                                    fadeIn(tween(250)) togetherWith fadeOut(tween(200))
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) { screen ->
                            when (screen) {
                                Screen.Home -> HomeScreen(
                                    operationLogs = operationLogs,
                                    padding = adjustedPadding,
                                    headerHelp = strings.helpHome,
                                    onHelp = { helpDialog.value = it },
                                    onMenu = openDrawer
                                )
                                Screen.Activities -> ActivityListScreen(
                                    viewModel = activityVm,
                                    padding = adjustedPadding,
                                    headerHelp = strings.helpActivities,
                                    onHelp = { helpDialog.value = it },
                                    onBack = backToHome,
                                    onMenu = openDrawer
                                )
                                Screen.Root -> RootScreen(
                                    rootVm,
                                    adjustedPadding,
                                    headerHelp = strings.helpRoot,
                                    onHelp = { helpDialog.value = it },
                                    onBack = backToHome,
                                    onMenu = openDrawer
                                )
                                Screen.Info -> InfoScreen(
                                    infoVm,
                                    adjustedPadding,
                                    headerHelp = strings.helpInfo,
                                    onHelp = { helpDialog.value = it },
                                    onMenu = openDrawer
                                )
                                Screen.Trick -> TrickScreen(
                                    trickVm,
                                    adjustedPadding,
                                    headerHelp = strings.helpTrick,
                                    onHelp = { helpDialog.value = it },
                                    onBack = backToHome,
                                    onMenu = openDrawer
                                )
                                Screen.Pentest -> PenTestingScreen(
                                    penTestingVm,
                                    adjustedPadding,
                                    headerHelp = strings.helpPentest,
                                    onHelp = { helpDialog.value = it },
                                    onBack = backToHome,
                                    onMenu = openDrawer
                                )
                                Screen.InstalledApps -> InstalledAppsScreen(
                                    installedAppsVm,
                                    adjustedPadding,
                                    headerHelp = strings.helpApk,
                                    onHelp = { helpDialog.value = it },
                                    onBack = backToHome,
                                    onMenu = openDrawer
                                )
                                Screen.Permissions -> PermissionsScreen(
                                    permissionsVm,
                                    adjustedPadding,
                                    headerHelp = strings.helpPermissions,
                                    onHelp = { helpDialog.value = it },
                                    onBack = backToHome,
                                    onMenu = openDrawer
                                )
                                Screen.Log -> LogScreen(
                                    logVm,
                                    adjustedPadding,
                                    headerHelp = strings.helpLog,
                                    onHelp = { helpDialog.value = it },
                                    onBack = backToHome,
                                    onMenu = openDrawer
                                )
                                Screen.Settings -> SettingsScreen(
                                    settingsVm,
                                    adjustedPadding,
                                    headerHelp = strings.helpSettings,
                                    onHelp = { helpDialog.value = it },
                                    onMenu = openDrawer
                                )
                                Screen.Help -> HelpScreen(
                                    padding = adjustedPadding,
                                    onHelp = { helpDialog.value = it },
                                    onMenu = openDrawer,
                                    onNavigate = { target -> selected.value = target }
                                )
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
                        .background(MaterialTheme.colorScheme.background)
                )
            }
        }

        if (showIntro.value) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showIntro.value = false },
                title = { Text(strings.introTitle) },
                text = { Text(strings.introBody) },
                confirmButton = {
                    androidx.compose.material3.Button(onClick = { showIntro.value = false }) {
                        Text(strings.introAgree)
                    }
                }
            )
            LaunchedEffect(showIntro.value) {
                if (!showIntro.value) {
                    setIntroSeenUse(true)
                }
            }
        }

        sectionHintDialog.value?.let { target ->
            sectionEntryHintText(strings, target)?.let { hint ->
                var disableHintsForever by remember(target) { mutableStateOf(false) }
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = {},
                    title = { Text("Suggerimenti • ${target.title(strings)}") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(hint)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = disableHintsForever,
                                    onCheckedChange = { disableHintsForever = it }
                                )
                                Text("Non visualizzare più")
                            }
                        }
                    },
                    confirmButton = {
                        androidx.compose.material3.Button(
                            onClick = {
                                if (disableHintsForever) {
                                    settingsVm.setFeatureHintsEnabled(false)
                                }
                                selected.value = target
                                sectionHintDialog.value = null
                            }
                        ) {
                            Text("Ho capito")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                sectionHintDialog.value = null
                            }
                        ) {
                            Text(strings.close)
                        }
                    }
                )
            }
        }

        helpDialog.value?.let { msg ->
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { helpDialog.value = null },
                confirmButton = {
                    androidx.compose.material3.TextButton(onClick = { helpDialog.value = null }) {
                        Text(strings.close)
                    }
                },
                title = { Text(strings.faqTitle) },
                text = { Text(msg) }
            )
        }
    }
}
@Composable
inline fun <reified T : Any> rememberVM(): T {
    return remember { KoinPlatform.getKoin().get<T>() }
}

private fun shouldShowEntryHintDialog(screen: Screen): Boolean {
    return when (screen) {
        Screen.Permissions,
        Screen.InstalledApps,
        Screen.Pentest,
        Screen.Activities,
        Screen.Root,
        Screen.Trick -> true
        else -> false
    }
}

private fun sectionEntryHintText(strings: Strings, screen: Screen): String? {
    return when (screen) {
        Screen.Permissions -> "${strings.helpPermissions}\n\nSuggerimento: usa la ricerca per app o permesso e condividi rapidamente i risultati dalla riga dell'app."
        Screen.InstalledApps -> "${strings.helpApk}\n\nSuggerimento: cerca per nome/package e usa le azioni rapide per estrarre o condividere APK."
        Screen.Pentest -> "${strings.pentestIntro}\n\nWorkflow consigliato: 1) scegli target o auto-scan, 2) seleziona test, 3) controlla risultati a video, 4) condividi subito."
        Screen.Activities -> "${strings.helpActivities}\n\nSuggerimento: filtra con cerca/chip preferiti e condividi le activity per singola app."
        Screen.Root -> "Esegue un check rapido del root e della Play Integrity: usa RootBeer per rilevare tracce di rooting e verifica il nonce con Play Integrity."
        Screen.Trick -> "${strings.helpTrick}\n\nSuggerimento: conferma i comandi sensibili e controlla sempre output/exit code."
        else -> null
    }
}
