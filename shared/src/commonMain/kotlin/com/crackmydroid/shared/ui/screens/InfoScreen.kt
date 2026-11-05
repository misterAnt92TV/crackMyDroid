@file:OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.ui.ExperimentalComposeUiApi::class
)

package com.crackmydroid.shared.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.crackmydroid.shared.i18n.LocalStrings
import com.crackmydroid.shared.i18n.Strings
import com.crackmydroid.shared.presentation.components.ScrollToTopButton
import com.crackmydroid.shared.presentation.components.SectionHeader
import com.crackmydroid.shared.presentation.components.card.AppCard
import com.crackmydroid.shared.presentation.components.text.AppText
import com.crackmydroid.shared.ui.info.InfoViewModel
import kotlinx.coroutines.launch

private fun buildInfoContent(
    info: com.crackmydroid.shared.domain.model.DeviceInfo,
    root: com.crackmydroid.shared.domain.model.RootStatus?,
    strings: Strings,
): String =
    buildString {
        val brand = info.brand ?: strings.notAvailable
        val device = info.device ?: strings.notAvailable
        val product = info.product ?: strings.notAvailable
        val buildId = info.buildId ?: strings.notAvailable
        val display = info.buildDisplay ?: strings.notAvailable
        val fingerprint = info.buildFingerprint ?: strings.notAvailable
        val radio = info.radio ?: strings.notAvailable
        val kernel = info.kernelVersion ?: strings.notAvailable
        val battery = info.batteryLevel?.let { "$it%" } ?: strings.notAvailable
        val screen = info.screenResolution ?: strings.notAvailable
        val density = info.densityDpi?.toString() ?: strings.notAvailable
        val imei = info.imei ?: strings.notAvailable
        val serial = info.serial ?: strings.notAvailable

        appendLine(strings.infoReportTitle)
        appendLine("${strings.manufacturer}: ${info.manufacturer}")
        appendLine("${strings.model}: ${info.model}")
        appendLine("${strings.infoBrand}: $brand")
        appendLine("${strings.infoDevice}: $device")
        appendLine("${strings.infoProduct}: $product")
        appendLine("${strings.androidVersion}: ${info.androidVersion}")
        appendLine("${strings.securityPatch}: ${info.securityPatch}")
        appendLine("${strings.infoBuildId}: $buildId")
        appendLine("${strings.infoDisplay}: $display")
        appendLine("${strings.infoFingerprint}: $fingerprint")
        appendLine("${strings.infoRadio}: $radio")
        appendLine("${strings.infoKernel}: $kernel")
        appendLine("${strings.hardware}: ${info.hardware}")
        appendLine("${strings.bootloader}: ${info.bootloader}")
        appendLine("${strings.battery}: $battery")
        appendLine("${strings.network}: ${info.networkType}")
        appendLine("${strings.infoRamTotal}: ${info.totalMemory}")
        appendLine("${strings.infoRamFree}: ${info.freeMemory}")
        appendLine("${strings.infoScreen}: $screen ($density ${strings.infoDpi})")
        appendLine("${strings.infoUsbDebug}: ${if (info.adbEnabled) strings.infoEnabled else strings.infoDisabled}")
        appendLine("${strings.infoDeveloperOptions}: ${if (info.developerOptions) strings.infoEnabledPlural else strings.infoDisabledPlural}")
        appendLine("${strings.infoAppDebuggable}: ${if (info.appDebuggable) strings.infoYes else strings.infoNo}")
        root?.let {
            appendLine("${strings.infoRoot}: ${if (it.isRooted) strings.infoYes else strings.infoNo}")
            appendLine("${strings.infoRootDetails}: ${it.details}")
        }
        appendLine("${strings.infoImei}: $imei")
        appendLine("${strings.infoSerial}: $serial")
        if (!info.systemPropDump.isNullOrBlank()) {
            appendLine()
            appendLine("${strings.infoSystemPropertiesSource}: ${info.systemPropSource ?: strings.notAvailable}")
            appendLine("${strings.infoSystemPropertiesDump}:")
            appendLine(info.systemPropDump)
        }
    }

@Composable
fun InfoScreen(
    viewModel: InfoViewModel,
    padding: androidx.compose.foundation.layout.PaddingValues,
    headerHelp: String,
    onHelp: (String) -> Unit,
    onMenu: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
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
            .padding(padding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionHeader(
                title = strings.navInfo,
                help = headerHelp,
                onHelp = onHelp,
                onMenu = onMenu
            )
            state.info?.let { info ->
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Smartphone,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                AppText(
                                    strings.infoDeviceSection,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            IconButton(
                                onClick = {
                                    state.reportContent?.let { content ->
                                        clipboard.setText(AnnotatedString(content))
                                        viewModel.share(content)
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Filled.Share,
                                    contentDescription = strings.shareDeviceInfoContentDescription,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Filled.Memory,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            AppText(
                                strings.infoSystemSection,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Filled.Wifi,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            AppText(
                                strings.infoConnectionSection,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        InfoRow(strings.network, info.networkType ?: strings.notAvailable)
                        InfoRow(strings.infoUsbDebug, if (info.adbEnabled) strings.infoEnabled else strings.infoDisabled)
                        InfoRow(
                            strings.infoDeveloperOptions,
                            if (info.developerOptions) strings.infoEnabledPlural else strings.infoDisabledPlural
                        )
                        InfoRow(strings.infoAppDebuggable, if (info.appDebuggable) strings.infoYes else strings.infoNo)
                        InfoRow(
                            strings.infoRoot,
                            if (root?.isRooted == true) strings.rootDetected else strings.rootNotDetected,
                            copyValue = root?.details
                        )

                        HorizontalDivider()
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Filled.Storage,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            AppText(
                                strings.infoDisplayMemorySection,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        InfoRow(strings.infoScreen, info.screenResolution ?: strings.notAvailable)
                        InfoRow(strings.infoDensity, info.densityDpi?.let { "$it ${strings.infoDpi}" } ?: strings.notAvailable)
                        InfoRow(
                            strings.infoRamTotal,
                            info.totalMemory?.let { "${it / 1024 / 1024} MB" } ?: strings.notAvailable)
                        InfoRow(
                            strings.infoRamFree,
                            info.freeMemory?.let { "${it / 1024 / 1024} MB" } ?: strings.notAvailable)
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
                                fontWeight = FontWeight.SemiBold
                            )
                            InfoRow(strings.infoSystemPropertiesSource, info.systemPropSource ?: strings.notAvailable)
                            SelectionContainer {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 280.dp)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    Text(
                                        text = dump,
                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                state.lastExportPath?.let { path ->
                    AppText(
                        "${strings.apkExported} $path",
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                    )
                }
            }

            state.error?.let { err ->
                AppText(err)
            }
        }

        ScrollToTopButton(
            visible = showScrollTop,
            onClick = { scope.launch { scrollState.animateScrollTo(0) } },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppText(
            label,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable {
                clipboard.setText(AnnotatedString("$label: $copyText"))
            }
        )
        Spacer(modifier = Modifier.width(12.dp))
        AppText(
            value,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
