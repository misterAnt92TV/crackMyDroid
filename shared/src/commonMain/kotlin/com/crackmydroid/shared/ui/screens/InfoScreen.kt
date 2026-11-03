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

        appendLine("Device info")
        appendLine("Produttore: ${info.manufacturer}")
        appendLine("Modello: ${info.model}")
        appendLine("Brand: $brand")
        appendLine("Device: $device")
        appendLine("Prodotto: $product")
        appendLine("Android: ${info.androidVersion}")
        appendLine("Patch: ${info.securityPatch}")
        appendLine("Build ID: $buildId")
        appendLine("Display: $display")
        appendLine("Fingerprint: $fingerprint")
        appendLine("Radio: $radio")
        appendLine("Kernel: $kernel")
        appendLine("Hardware: ${info.hardware}")
        appendLine("Bootloader: ${info.bootloader}")
        appendLine("Batteria: $battery")
        appendLine("Rete: ${info.networkType}")
        appendLine("RAM totale: ${info.totalMemory}")
        appendLine("RAM libera: ${info.freeMemory}")
        appendLine("Schermo: $screen ($density dpi)")
        appendLine("USB Debug: ${if (info.adbEnabled) "Attivo" else "Disattivo"}")
        appendLine("Opzioni sviluppatore: ${if (info.developerOptions) "Attive" else "Spente"}")
        appendLine("App debuggable: ${if (info.appDebuggable) "Sì" else "No"}")
        root?.let {
            appendLine("Root: ${if (it.isRooted) "Sì" else "No"}")
            appendLine("Root dettagli: ${it.details}")
        }
        appendLine("IMEI: $imei")
        appendLine("Serial: $serial")
        if (!info.systemPropDump.isNullOrBlank()) {
            appendLine()
        appendLine("System properties source: ${info.systemPropSource ?: strings.notAvailable}")
            appendLine("System properties dump:")
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
                                    "Dispositivo",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            IconButton(
                                onClick = {
                                    state.info?.let { info ->
                                        val content = buildInfoContent(info, root, strings)
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
                        InfoRow("Produttore", info.manufacturer ?: strings.notAvailable)
                        InfoRow("Modello", info.model ?: strings.notAvailable)
                        InfoRow("Brand", info.brand ?: strings.notAvailable)
                        InfoRow("Device", info.device ?: strings.notAvailable)
                        InfoRow("Prodotto", info.product ?: strings.notAvailable)
                        InfoRow("IMEI", info.imei ?: strings.notAvailable)
                        InfoRow("Serial", info.serial ?: strings.notAvailable)

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
                                "Sistema",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        InfoRow("Android", info.androidVersion ?: strings.notAvailable)
                        InfoRow("Patch", info.securityPatch ?: strings.notAvailable)
                        InfoRow("Build ID", info.buildId ?: strings.notAvailable)
                        InfoRow("Display", info.buildDisplay ?: strings.notAvailable)
                        InfoRow("Fingerprint", info.buildFingerprint ?: strings.notAvailable)
                        InfoRow("Radio", info.radio ?: strings.notAvailable)
                        InfoRow("Kernel", info.kernelVersion ?: strings.notAvailable)
                        InfoRow(strings.hardware, info.hardware ?: strings.notAvailable)
                        InfoRow("Bootloader", info.bootloader ?: strings.notAvailable)

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
                                "Connessione e debug",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        InfoRow("Rete", info.networkType ?: strings.notAvailable)
                        InfoRow("USB Debug", if (info.adbEnabled) "Attivo" else "Disattivo")
                        InfoRow(
                            "Opzioni sviluppatore",
                            if (info.developerOptions) "Attive" else "Spente"
                        )
                        InfoRow("App debuggable", if (info.appDebuggable) "Sì" else "No")
                        InfoRow(
                            "Root",
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
                                "Display e memoria",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        InfoRow("Schermo", info.screenResolution ?: strings.notAvailable)
                        InfoRow("Densità", info.densityDpi?.let { "${it} dpi" } ?: strings.notAvailable)
                        InfoRow(
                            "RAM totale",
                            info.totalMemory?.let { "${it / 1024 / 1024} MB" } ?: strings.notAvailable)
                        InfoRow(
                            "RAM libera",
                            info.freeMemory?.let { "${it / 1024 / 1024} MB" } ?: strings.notAvailable)
                        InfoRow("Batteria", info.batteryLevel?.let { "$it%" } ?: strings.notAvailable)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                info.systemPropDump?.takeIf { it.isNotBlank() }?.let { dump ->
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppText(
                                "System properties (SDK 24+)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            InfoRow("Sorgente", info.systemPropSource ?: strings.notAvailable)
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
