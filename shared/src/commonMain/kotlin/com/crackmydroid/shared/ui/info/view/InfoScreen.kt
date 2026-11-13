@file:OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class,
)

package com.crackmydroid.shared.ui.info.view

import com.crackmydroid.architecture.presentation.model.UiLoadState
import com.crackmydroid.ui.icons.AppIcons
import com.crackmydroid.ui.typography.AppTypography

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.crackmydroid.shared.domain.model.DeviceInfo
import com.crackmydroid.shared.i18n.LocalStrings
import com.crackmydroid.shared.presentation.preview.AppPreviewEnvironment
import com.crackmydroid.shared.presentation.preview.AppScreenPreviews
import com.crackmydroid.shared.ui.info.InfoEvent
import com.crackmydroid.shared.ui.info.InfoState
import com.crackmydroid.shared.ui.info.InfoViewModel
import com.crackmydroid.ui.components.AppIcon
import com.crackmydroid.ui.components.progress.AppProgressIndicator
import com.crackmydroid.ui.components.button.ScrollToTopButton
import com.crackmydroid.ui.components.card.AppCard
import com.crackmydroid.ui.components.progress.AppProgressBar
import com.crackmydroid.ui.components.text.AppText
import kotlinx.coroutines.launch

@Composable
fun InfoScreen(
    viewModel: InfoViewModel,
    padding: PaddingValues,
) {
    val state by viewModel.state.collectAsState()
    InfoScreenContent(
        state = state,
        padding = padding,
        onShare = { content -> viewModel.emitEvent(InfoEvent.Share(content)) },
    )
}

@Composable
private fun InfoScreenContent(
    state: InfoState,
    padding: PaddingValues,
    onShare: (String) -> Unit,
) {
    val strings = LocalStrings.current
    val root = state.root

    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val showScrollTop by remember {
        derivedStateOf { scrollState.value > 260 }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            state.info?.let { info ->
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                AppIcon(
                                    AppIcons.Filled.Smartphone,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp),
                                )
                                AppText(
                                    strings.infoDeviceSection,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            IconButton(
                                enabled = state.reportContent != null && !(state.reportLoadState is UiLoadState.Loading),
                                onClick = {
                                    state.reportContent?.let { content ->
                                        clipboard.setText(AnnotatedString(content))
                                        onShare(content)
                                    }
                                },
                            ) {
                                AppIcon(
                                    AppIcons.Filled.Share,
                                    contentDescription = strings.shareDeviceInfoContentDescription,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                        if (state.reportLoadState is UiLoadState.Loading && state.reportContent == null) {
                            AppProgressBar(modifier = Modifier.fillMaxWidth())
                        }
                        InfoRow(strings.manufacturer, info.manufacturer ?: strings.notAvailable)
                        InfoRow(strings.model, info.model ?: strings.notAvailable)
                        InfoRow(strings.infoBrand, info.brand ?: strings.notAvailable)
                        InfoRow(strings.infoDevice, info.device ?: strings.notAvailable)
                        InfoRow(strings.infoProduct, info.product ?: strings.notAvailable)
                        InfoRow(strings.infoImei, info.imei ?: strings.notAvailable)
                        InfoRow(strings.infoSerial, info.serial ?: strings.notAvailable)

                        HorizontalDivider()
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            AppIcon(
                                AppIcons.Filled.Memory,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                            AppText(
                                strings.infoSystemSection,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        InfoRow(strings.androidVersion, info.androidVersion ?: strings.notAvailable)
                        InfoRow(strings.securityPatch, info.securityPatch ?: strings.notAvailable)
                        InfoRow(strings.infoBuildId, info.buildId ?: strings.notAvailable)
                        InfoRow(strings.infoDisplay, info.buildDisplay ?: strings.notAvailable)
                        InfoRow(strings.infoFingerprint, info.buildFingerprint ?: strings.notAvailable)
                        InfoRow(strings.infoRadio, info.radio ?: strings.notAvailable)
                        InfoRow(strings.infoKernel, info.kernelVersion ?: strings.notAvailable)
                        InfoRow(strings.hardware, info.hardware ?: strings.notAvailable)
                        InfoRow(strings.bootloader, info.bootloader ?: strings.notAvailable)

                        HorizontalDivider()
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            AppIcon(
                                AppIcons.Filled.Wifi,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                            AppText(
                                strings.infoConnectionSection,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        InfoRow(strings.network, info.networkType ?: strings.notAvailable)
                        InfoRow(
                            strings.infoUsbDebug,
                            if (info.adbEnabled) strings.infoEnabled else strings.infoDisabled,
                        )
                        InfoRow(
                            strings.infoDeveloperOptions,
                            if (info.developerOptions) strings.infoEnabledPlural else strings.infoDisabledPlural,
                        )
                        InfoRow(strings.infoAppDebuggable, if (info.appDebuggable) strings.infoYes else strings.infoNo)
                        InfoRow(
                            strings.infoRoot,
                            if (root?.isRooted == true) strings.rootDetected else strings.rootNotDetected,
                            copyValue = root?.details,
                        )

                        HorizontalDivider()
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            AppIcon(
                                AppIcons.Filled.Storage,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                            AppText(
                                strings.infoDisplayMemorySection,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        InfoRow(strings.infoScreen, info.screenResolution ?: strings.notAvailable)
                        InfoRow(
                            strings.infoDensity,
                            info.densityDpi?.let { "$it ${strings.infoDpi}" } ?: strings.notAvailable,
                        )
                        InfoRow(
                            strings.infoRamTotal,
                            info.totalMemory?.let { "${it / 1024 / 1024} MB" } ?: strings.notAvailable,
                        )
                        InfoRow(
                            strings.infoRamFree,
                            info.freeMemory?.let { "${it / 1024 / 1024} MB" } ?: strings.notAvailable,
                        )
                        InfoRow(strings.battery, info.batteryLevel?.let { "$it%" } ?: strings.notAvailable)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                info.systemPropDump?.takeIf { it.isNotBlank() }?.let { dump ->
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppText(
                                strings.infoSystemProperties,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            InfoRow(strings.infoSystemPropertiesSource, info.systemPropSource ?: strings.notAvailable)
                            SelectionContainer {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 280.dp)
                                        .verticalScroll(rememberScrollState()),
                                ) {
                                    AppText(
                                        text = dump,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = AppTypography.monospace,
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }

                state.lastExportPath?.let { path ->
                    AppText(
                        "${strings.apkExported} $path",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            if (state.loadState is UiLoadState.Loading && state.info == null) {
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        AppProgressIndicator(label = strings.infoDeviceSection)
                    }
                }
            }

            (state.loadError ?: state.reportError ?: state.error)?.let { err ->
                AppText(err)
            }
        }

        ScrollToTopButton(
            visible = showScrollTop,
            onClick = { scope.launch { scrollState.animateScrollTo(0) } },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
        )
    }
}

@AppScreenPreviews
@Composable
private fun InfoScreenPreview() {
    AppPreviewEnvironment {
        InfoScreenContent(
            state = InfoState(
                info = DeviceInfo(
                    manufacturer = "Google",
                    model = "Pixel",
                    brand = "Google",
                    device = "pixel",
                    product = "pixel",
                    androidVersion = "15",
                    securityPatch = "2025-01-05",
                    buildId = "AP4A.250105.002",
                    batteryLevel = 82,
                    networkType = "Wi-Fi",
                    totalMemory = 8L * 1024 * 1024 * 1024,
                    freeMemory = 3L * 1024 * 1024 * 1024,
                    screenResolution = "1080 x 2400",
                    densityDpi = 420,
                    adbEnabled = true,
                ),
                reportContent = "Device information report",
            ),
            padding = PaddingValues(),
            onShare = {},
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String, copyValue: String? = null) {
    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val copyText = copyValue ?: value
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppText(
            label,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable {
                clipboard.setText(AnnotatedString("$label: $copyText"))
            },
        )
        Spacer(modifier = Modifier.width(12.dp))
        AppText(
            value,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
