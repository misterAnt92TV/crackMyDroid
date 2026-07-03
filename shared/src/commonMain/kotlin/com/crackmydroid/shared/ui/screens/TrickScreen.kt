package com.crackmydroid.shared.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.crackmydroid.shared.domain.model.ShellCommand
import com.crackmydroid.shared.domain.model.ShellCommandResult
import com.crackmydroid.shared.i18n.LocalStrings
import com.crackmydroid.shared.presentation.components.AppButton
import com.crackmydroid.shared.presentation.components.AppText
import com.crackmydroid.shared.presentation.components.ButtonVariant
import com.crackmydroid.shared.presentation.components.ErrorDialog
import com.crackmydroid.shared.presentation.components.SectionHeader
import com.crackmydroid.shared.presentation.components.ScrollToTopButton
import com.crackmydroid.shared.ui.trick.TrickViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TrickScreen(
    viewModel: TrickViewModel,
    padding: PaddingValues,
    headerHelp: String,
    onHelp: (String) -> Unit,
    onBack: (() -> Unit)? = null,
    onMenu: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    var pending by remember { mutableStateOf<ShellCommand?>(null) }
    var resultDialog by remember { mutableStateOf<ShellCommandResult?>(null) }
    val strings = LocalStrings.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val showScrollTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 1 || listState.firstVisibleItemScrollOffset > 240 }
    }

    LaunchedEffect(state.lastResult) {
        state.lastResult?.let { resultDialog = it }
    }
    LaunchedEffect(state.interactionHint) {
        if (state.interactionHint != null) {
            delay(1800)
            viewModel.clearInteractionHint()
        }
    }

    fun handleCommandTap(command: ShellCommand) {
        if (state.running) {
            viewModel.notifyBusyInteraction(command)
            return
        }
        if (!command.supported) {
            viewModel.notifyUnsupportedCommand(command)
            return
        }
        if (command.confirmation != null) pending = command else viewModel.execute(command)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 28.dp)
        ) {
            item {
                SectionHeader(
                    title = strings.navTrick,
                    help = headerHelp,
                    onHelp = onHelp,
                    onBack = onBack,
                    onMenu = onMenu
                )
            }
            state.interactionHint?.let { hint ->
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            AppText(
                                text = hint,
                                modifier = Modifier.padding(start = 8.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
            itemsIndexed(
                items = state.commands,
                key = { index, command -> "${command.command}:$index" }
            ) { _, command ->
                val isRunningThis = state.runningCommand?.command == command.command
                TrickCommandCard(
                    command = command,
                    running = state.running,
                    runningThis = isRunningThis,
                    onExecute = { handleCommandTap(command) }
                )
            }
            state.lastResult?.let { res ->
                item {
                    TrickInlineResultSummary(
                        commandTitle = state.lastCommandTitle,
                        result = res
                    )
                }
            }
            state.error?.let { err ->
                item {
                    AppText(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        ScrollToTopButton(
            visible = showScrollTop,
            onClick = { scope.launch { listState.animateScrollToItem(0) } },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
        )
    }

    pending?.let { command ->
        AlertDialog(
            onDismissRequest = { pending = null },
            title = { AppText("Conferma comando") },
            text = {
                AppText(
                    text = command.confirmation ?: "Eseguire: ${command.command}",
                    style = MaterialTheme.typography.bodySmall
                )
            },
            confirmButton = {
                AppButton(
                    text = if (state.running) "In esecuzione..." else "Esegui",
                    enabled = !state.running,
                    fullWidth = false,
                    onClick = {
                        viewModel.execute(command)
                        pending = null
                    }
                )
            },
            dismissButton = {
                AppButton(
                    text = "Annulla",
                    fullWidth = false,
                    variant = ButtonVariant.Secondary,
                    onClick = { pending = null }
                )
            }
        )
    }

    state.error?.let { ErrorDialog(it, viewModel::clearError) }

    resultDialog?.let { res ->
        AlertDialog(
            onDismissRequest = { resultDialog = null },
            title = {
                AppText(
                    text = if (res.success) strings.commandSuccess else strings.commandError,
                    color = if (res.success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                TrickResultDialogBody(
                    result = res,
                    commandTitle = state.lastCommandTitle
                )
            },
            confirmButton = {
                AppButton(
                    text = strings.close,
                    fullWidth = false,
                    onClick = { resultDialog = null }
                )
            }
        )
    }
}

@Composable
private fun TrickCommandCard(
    command: ShellCommand,
    running: Boolean,
    runningThis: Boolean,
    onExecute: () -> Unit
) {
    val disabledByOther = running && !runningThis
    val unsupported = !command.supported
    val cardContainer = when {
        runningThis -> MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        unsupported -> MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
        disabledByOther -> MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        else -> MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp)
    }
    val cardModifier = if (disabledByOther || unsupported) Modifier.alpha(0.72f) else Modifier

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(cardModifier),
        colors = CardDefaults.elevatedCardColors(containerColor = cardContainer),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
        onClick = {
            if (!unsupported && !disabledByOther && !runningThis) {
                onExecute()
            }
        }
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val icon = if (command.requiresRoot) Icons.Filled.Bolt else Icons.Filled.Code
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (command.requiresRoot) {
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f)
                            } else {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (command.requiresRoot) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }
                Column(
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .weight(1f)
                ) {
                    AppText(
                        text = command.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    AppText(
                        text = when {
                            unsupported -> command.unsupportedReason ?: "Comando non disponibile"
                            command.requiresRoot -> "Comando privilegiato"
                            else -> "Comando standard"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (runningThis) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CommandTag(
                    text = if (command.requiresRoot) "ROOT" else "SAFE",
                    tint = if (command.requiresRoot) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                )
                if (unsupported) {
                    CommandTag(
                        text = "NON DISP.",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                if (command.confirmation != null) {
                    CommandTag(
                        text = "CONFERMA",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                AppText(
                    text = command.command,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            AppButton(
                text = when {
                    runningThis -> "In esecuzione..."
                    unsupported -> "Non disponibile"
                    disabledByOther -> "Attendi comando corrente"
                    else -> "Esegui comando"
                },
                onClick = onExecute,
                enabled = !runningThis && !unsupported,
                fullWidth = false,
                variant = if (command.requiresRoot) ButtonVariant.Secondary else ButtonVariant.Tonal,
                modifier = Modifier.align(Alignment.End)
            )

            if (runningThis) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun CommandTag(text: String, tint: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(tint.copy(alpha = 0.16f))
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        AppText(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = tint,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun TrickInlineResultSummary(
    commandTitle: String?,
    result: ShellCommandResult
) {
    val title = commandTitle ?: "Comando"
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (result.success) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.11f)
            } else {
                MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
            }
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (result.success) Icons.Filled.CheckCircle else Icons.Filled.Error,
                contentDescription = null,
                tint = if (result.success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            Column(modifier = Modifier.padding(start = 8.dp)) {
                AppText(
                    text = "$title - exit ${result.code}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                val subtitle = when {
                    result.stderr.isNotBlank() -> result.stderr.lineSequence().first().trim()
                    result.stdout.isNotBlank() -> result.stdout.lineSequence().first().trim()
                    else -> "Nessun output testuale"
                }
                AppText(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun TrickResultDialogBody(
    result: ShellCommandResult,
    commandTitle: String?
) {
    val stdoutLines = remember(result.stdout) { parseNonBlankLines(result.stdout) }
    val stderrLines = remember(result.stderr) { parseNonBlankLines(result.stderr) }
    val packageLines = remember(stdoutLines) { parsePackageLines(stdoutLines) }
    val showPackageList = packageLines.isNotEmpty() && packageLines.size == stdoutLines.size

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 420.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    commandTitle?.let {
                        AppText(
                            text = it,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    AppText(
                        text = "Exit code: ${result.code}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (showPackageList) {
            item {
                AppText(
                    text = "Pacchetti trovati: ${packageLines.size}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            items(packageLines) { pkg ->
                TrickResultLine(
                    text = pkg,
                    icon = Icons.Filled.Code,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        } else if (stdoutLines.isNotEmpty()) {
            item {
                AppText(
                    text = "Output",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            items(stdoutLines) { line ->
                TrickResultLine(
                    text = line,
                    icon = Icons.Filled.CheckCircle,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (stderrLines.isNotEmpty()) {
            item {
                AppText(
                    text = "Error output",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.error
                )
            }
            items(stderrLines) { line ->
                TrickResultLine(
                    text = line,
                    icon = Icons.Filled.Error,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        if (stdoutLines.isEmpty() && stderrLines.isEmpty()) {
            item {
                TrickResultLine(
                    text = "Nessun output testuale",
                    icon = Icons.Filled.Schedule,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TrickResultLine(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier
                .size(15.dp)
                .padding(top = 2.dp)
        )
        AppText(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

private fun parseNonBlankLines(raw: String): List<String> {
    return raw.lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .toList()
}

private fun parsePackageLines(lines: List<String>): List<String> {
    return lines.mapNotNull { line ->
        if (line.startsWith("package:")) line.removePrefix("package:").trim() else null
    }
}
