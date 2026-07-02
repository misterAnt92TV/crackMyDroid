package com.crackmydroid.shared.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import com.crackmydroid.shared.i18n.LocalStrings
import com.crackmydroid.shared.ui.root.RootViewModel
import com.crackmydroid.shared.presentation.components.AppButton
import com.crackmydroid.shared.presentation.components.AppText
import com.crackmydroid.shared.presentation.components.SectionHeader
import com.crackmydroid.shared.presentation.components.ErrorDialog
import com.crackmydroid.shared.presentation.components.ScrollToTopButton
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun RootScreen(
    viewModel: RootViewModel,
    padding: androidx.compose.foundation.layout.PaddingValues,
    headerHelp: String,
    onHelp: (String) -> Unit,
    onBack: (() -> Unit)? = null,
    onMenu: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    var nonceInput by remember { mutableStateOf("sample-nonce") }
    val strings = LocalStrings.current
    val resultBringIntoView = remember { BringIntoViewRequester() }

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
                title = strings.navRoot,
                help = headerHelp,
                onHelp = onHelp,
                onBack = onBack,
                onMenu = onMenu
            )

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text(strings.verifyRoot, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        IconButton(onClick = { viewModel.verify() }) {
                            Icon(Icons.Filled.Refresh, contentDescription = strings.verifyRoot, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    AppButton(text = strings.verifyRoot, onClick = { viewModel.verify() }, fullWidth = true)

                    OutlinedTextField(
                        value = nonceInput,
                        onValueChange = { nonceInput = it },
                        label = { androidx.compose.material3.Text(strings.nonceLabel) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )
                    AppButton(
                        text = strings.verifyPlayIntegrity,
                        onClick = { viewModel.verifyPlayIntegrity(nonceInput.ifBlank { "sample-nonce" }) },
                        fullWidth = true
                    )
                }
            }

            if (state.loading) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 12.dp))
            }

            if (state.status != null || state.playIntegrity != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(resultBringIntoView),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    state.status?.let { status ->
                        val bg = if (status.isRooted) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
                        val onBg = if (status.isRooted) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = bg),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (status.isRooted) Icons.Filled.Warning else Icons.Filled.CheckCircle,
                                            contentDescription = null,
                                            tint = onBg
                                        )
                                        Text(
                                            text = if (status.isRooted) strings.rootDetected else strings.rootNotDetected,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = onBg,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    IconButton(onClick = { viewModel.shareReport(strings) }) {
                                        Icon(
                                            imageVector = Icons.Filled.Share,
                                            contentDescription = strings.pentestShare,
                                            tint = onBg
                                        )
                                    }
                                }
                                AppText(
                                    status.details,
                                    modifier = Modifier.fillMaxWidth(),
                                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                    color = onBg
                                )
                            }
                        }
                    }

                    state.playIntegrity?.let { pi ->
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                ) {
                                    Text(
                                        strings.playIntegrityTitle,
                                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    IconButton(onClick = { viewModel.shareReport(strings) }) {
                                        Icon(
                                            imageVector = Icons.Filled.Share,
                                            contentDescription = strings.pentestShare,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                AppText("${strings.playIntegrityBasic}: ${pi.basicIntegrity ?: "n/d"}")
                                AppText("${strings.playIntegrityDevice}: ${pi.deviceIntegrity ?: "n/d"}")
                                SelectionContainer {
                                    AppText(
                                        "${strings.playIntegrityDetails}: ${pi.details}",
                                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }

            state.error?.let { err ->
                AppText(err, modifier = Modifier.padding(top = 8.dp), style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
            }
        }

        ScrollToTopButton(
            visible = showScrollTop,
            onClick = { scope.launch { scrollState.animateScrollTo(0) } },
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.BottomEnd)
                .padding(12.dp)
        )
    }

    LaunchedEffect(Unit) {
        viewModel.verify()
    }

    LaunchedEffect(state.status, state.playIntegrity) {
        if (state.status != null || state.playIntegrity != null) {
            resultBringIntoView.bringIntoView()
        }
    }

    state.error?.let { ErrorDialog(it, viewModel::clearError) }
}
