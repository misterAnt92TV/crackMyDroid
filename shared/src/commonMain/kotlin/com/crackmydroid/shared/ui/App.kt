package com.crackmydroid.shared.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
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
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.ui.draw.shadow
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
import org.koin.mp.KoinPlatform
import com.crackmydroid.shared.presentation.components.LocalAccessibilityPrefs
import com.crackmydroid.shared.presentation.components.AccessibilityPrefs
import com.crackmydroid.shared.presentation.components.SearchHistoryStore
import com.crackmydroid.shared.presentation.components.PackageIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    val helpDialog = remember { mutableStateOf<String?>(null) }
    val introSeenUse = remember { KoinPlatform.getKoin().get<GetIntroSeenUseCase>() }
    val setIntroSeenUse = remember { KoinPlatform.getKoin().get<SetIntroSeenUseCase>() }
    val settingsState = settingsVm.state.collectAsState()
    val strings = stringsFor(platformLanguage())
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
            val gradient = if (darkTheme) {
                listOf(Color(0xFF0F172A), Color(0xFF121B2F), Color(0xFF0F221A))
            } else {
                listOf(Color(0xFFE8F0FE), Color(0xFFF8FAFF), Color(0xFFE6F4EA))
            }
            val drawerVisual = drawerVisualStyle(
                variant = ACTIVE_DRAWER_VISUAL_VARIANT,
                darkTheme = darkTheme
            )
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
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = drawerVisual.sheetColor
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 10.dp, vertical = 10.dp)
                                .width(286.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(drawerVisual.headerGradient)
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(drawerVisual.headerIconContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        PackageIcon(
                                            packageName = appPackageName(),
                                            size = 42.dp,
                                            backgroundColor = Color.Transparent,
                                            contentPadding = 0.dp
                                        )
                                    }
                                    Column(
                                        modifier = Modifier.padding(start = 12.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = "CrackMyDroid",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            color = drawerVisual.headerTitleColor
                                        )
                                        Text(
                                            text = "Toolkit Android per test rapidi di sicurezza",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = drawerVisual.headerSubtitleColor,
                                            maxLines = 2
                                        )
                                    }
                                }
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalArrangement = Arrangement.SpaceEvenly
                            ) {
                                drawerScreens.forEach { screen ->
                                    val isSelected = selected.value == screen
                                    NavigationDrawerItem(
                                        icon = { Icon(screen.icon, contentDescription = null) },
                                        label = {
                                            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                                Text(
                                                    text = screen.title(strings),
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    text = drawerMiniDescription(screen),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = if (isSelected) {
                                                        drawerVisual.selectedSubtitleColor
                                                    } else {
                                                        drawerVisual.unselectedSubtitleColor
                                                    },
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        },
                                        selected = isSelected,
                                        onClick = { navigateFromDrawer(screen) },
                                        colors = NavigationDrawerItemDefaults.colors(
                                            selectedContainerColor = drawerVisual.selectedContainerColor,
                                            unselectedContainerColor = drawerVisual.unselectedContainerColor,
                                            selectedIconColor = drawerVisual.selectedIconColor,
                                            selectedTextColor = drawerVisual.selectedTextColor,
                                            unselectedIconColor = drawerVisual.unselectedIconColor,
                                            unselectedTextColor = drawerVisual.unselectedTextColor
                                        ),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 62.dp)
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Versione $androidAppVersionName",
                                style = MaterialTheme.typography.labelMedium,
                                color = drawerVisual.footerTextColor,
                                modifier = Modifier
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            ) {
                val showBottomBar = bottomScreens.contains(selected.value)
                val navHeight = 56.dp // keeps >=48dp touch targets when visible
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            val navShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                            NavigationBar(
                                modifier = Modifier
                                    .navigationBarsPadding()
                                    .shadow(10.dp, shape = navShape, clip = false)
                                    .clip(navShape)
                                    .height(navHeight),
                                windowInsets = WindowInsets(0, 0, 0, 0),
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = if (darkTheme) 0.96f else 0.98f),
                                tonalElevation = 6.dp
                            ) {
                                val itemIconSize = 20.dp
                                bottomScreens.forEach { screen ->
                                    val accent = googleAccentForScreen(screen, darkTheme)
                                    NavigationBarItem(
                                        selected = selected.value == screen,
                                        onClick = { selected.value = screen },
                                        icon = { Icon(screen.icon, contentDescription = screen.title(strings), modifier = Modifier.size(itemIconSize)) },
                                        label = { Text(screen.title(strings)) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = accent,
                                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                            indicatorColor = accent.copy(alpha = if (darkTheme) 0.26f else 0.18f),
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
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
                            .background(Brush.verticalGradient(gradient))
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
                            }
                        }
                    }
                }
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

private enum class DrawerVisualVariant {
    Clean,
    Bold
}

private data class DrawerVisualStyle(
    val sheetColor: Color,
    val headerGradient: Brush,
    val headerIconContainer: Color,
    val headerIconTint: Color,
    val headerTitleColor: Color,
    val headerSubtitleColor: Color,
    val selectedContainerColor: Color,
    val unselectedContainerColor: Color,
    val selectedIconColor: Color,
    val selectedTextColor: Color,
    val unselectedIconColor: Color,
    val unselectedTextColor: Color,
    val selectedSubtitleColor: Color,
    val unselectedSubtitleColor: Color,
    val footerTextColor: Color
)

private val ACTIVE_DRAWER_VISUAL_VARIANT = DrawerVisualVariant.Clean

private fun googleAccentForScreen(screen: Screen, darkTheme: Boolean): Color {
    return when (screen) {
        Screen.Home -> if (darkTheme) Color(0xFFA8C7FA) else Color(0xFF1A73E8)
        Screen.Info -> if (darkTheme) Color(0xFF81C995) else Color(0xFF188038)
        Screen.Settings -> if (darkTheme) Color(0xFFFDD663) else Color(0xFFF9AB00)
        Screen.InstalledApps -> if (darkTheme) Color(0xFFA8C7FA) else Color(0xFF1A73E8)
        Screen.Pentest -> if (darkTheme) Color(0xFFFDD663) else Color(0xFFF9AB00)
        Screen.Permissions -> if (darkTheme) Color(0xFFF28B82) else Color(0xFFD93025)
        Screen.Activities -> if (darkTheme) Color(0xFF81C995) else Color(0xFF188038)
        Screen.Root -> if (darkTheme) Color(0xFFF28B82) else Color(0xFFD93025)
        Screen.Trick -> if (darkTheme) Color(0xFFA8C7FA) else Color(0xFF1A73E8)
        Screen.Log -> if (darkTheme) Color(0xFF81C995) else Color(0xFF188038)
    }
}

private fun drawerVisualStyle(
    variant: DrawerVisualVariant,
    darkTheme: Boolean
): DrawerVisualStyle {
    return when (variant) {
        DrawerVisualVariant.Clean -> {
            if (darkTheme) {
                DrawerVisualStyle(
                    sheetColor = Color(0xFF121C2B),
                    headerGradient = Brush.linearGradient(listOf(Color(0xFF1A2C43), Color(0xFF1A4758))),
                    headerIconContainer = Color(0x55FFFFFF),
                    headerIconTint = Color(0xFFB7D7FF),
                    headerTitleColor = Color.White,
                    headerSubtitleColor = Color(0xFFE1EEFF),
                    selectedContainerColor = Color(0xFF2A4A67),
                    unselectedContainerColor = Color(0xFF1C2837),
                    selectedIconColor = Color(0xFF9AC7FF),
                    selectedTextColor = Color(0xFFE8F3FF),
                    unselectedIconColor = Color(0xFF9EB3C8),
                    unselectedTextColor = Color(0xFFD2DDE8),
                    selectedSubtitleColor = Color(0xFFBFE0FF),
                    unselectedSubtitleColor = Color(0xFF91A8BE),
                    footerTextColor = Color(0xFF9CB1C7)
                )
            } else {
                DrawerVisualStyle(
                    sheetColor = Color(0xFFF6FAFF),
                    headerGradient = Brush.linearGradient(listOf(Color(0xFFE8F3FF), Color(0xFFEAF8F2))),
                    headerIconContainer = Color(0xD9FFFFFF),
                    headerIconTint = Color(0xFF226AA1),
                    headerTitleColor = Color(0xFF12314A),
                    headerSubtitleColor = Color(0xFF335B77),
                    selectedContainerColor = Color(0xFFDAECFF),
                    unselectedContainerColor = Color(0xFFEFF4F9),
                    selectedIconColor = Color(0xFF1B5D93),
                    selectedTextColor = Color(0xFF12324A),
                    unselectedIconColor = Color(0xFF5A6F84),
                    unselectedTextColor = Color(0xFF243A4F),
                    selectedSubtitleColor = Color(0xFF1F5D8A),
                    unselectedSubtitleColor = Color(0xFF60798F),
                    footerTextColor = Color(0xFF5D7489)
                )
            }
        }

        DrawerVisualVariant.Bold -> {
            if (darkTheme) {
                DrawerVisualStyle(
                    sheetColor = Color(0xFF0E1724),
                    headerGradient = Brush.linearGradient(listOf(Color(0xFF0E3155), Color(0xFF0D5A47))),
                    headerIconContainer = Color(0x44FFFFFF),
                    headerIconTint = Color.White,
                    headerTitleColor = Color.White,
                    headerSubtitleColor = Color(0xFFD9EEFF),
                    selectedContainerColor = Color(0xFF14507D),
                    unselectedContainerColor = Color(0xFF1A293A),
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    unselectedIconColor = Color(0xFF96AEC8),
                    unselectedTextColor = Color(0xFFD0DEEE),
                    selectedSubtitleColor = Color(0xFFCCEBFF),
                    unselectedSubtitleColor = Color(0xFF8FA7C0),
                    footerTextColor = Color(0xFF97B2CC)
                )
            } else {
                DrawerVisualStyle(
                    sheetColor = Color(0xFFF3F7FF),
                    headerGradient = Brush.linearGradient(listOf(Color(0xFF15558F), Color(0xFF1E7D5D))),
                    headerIconContainer = Color(0x38FFFFFF),
                    headerIconTint = Color.White,
                    headerTitleColor = Color.White,
                    headerSubtitleColor = Color(0xFFE4F3FF),
                    selectedContainerColor = Color(0xFF1A5F9E),
                    unselectedContainerColor = Color(0xFFDDE8F6),
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    unselectedIconColor = Color(0xFF31557A),
                    unselectedTextColor = Color(0xFF1E3E60),
                    selectedSubtitleColor = Color(0xFFD9EEFF),
                    unselectedSubtitleColor = Color(0xFF3E5F80),
                    footerTextColor = Color(0xFF4A6886)
                )
            }
        }
    }
}

private fun drawerMiniDescription(screen: Screen): String {
    return when (screen) {
        Screen.InstalledApps -> "Lista app installate con azioni rapide su APK."
        Screen.Pentest -> "Wizard guidato per test rapidi di hardening."
        Screen.Permissions -> "Verifica permessi sensibili e rischi principali."
        Screen.Activities -> "Esplora e filtra activity esportate e invocabili."
        Screen.Root -> "Check root e integrita del dispositivo."
        Screen.Trick -> "Tool tecnici e comandi utili per analisi rapida."
        Screen.Log -> "Filtra log e condividi evidenze in pochi tap."
        Screen.Home -> "Panoramica operativa del progetto."
        Screen.Info -> "Dettagli tecnici e metadati dell'app target."
        Screen.Settings -> "Preferenze UI, accessibilita e suggerimenti."
    }
}
