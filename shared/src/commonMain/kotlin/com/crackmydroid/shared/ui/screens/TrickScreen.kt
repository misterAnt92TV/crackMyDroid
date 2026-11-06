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
import com.crackmydroid.shared.domain.model.shell.ShellCommand
import com.crackmydroid.shared.domain.model.shell.ShellCommandResult
import com.crackmydroid.shared.i18n.LocalStrings
import com.crackmydroid.shared.i18n.ScreenUiStrings
import com.crackmydroid.shared.presentation.components.AppButton
import com.crackmydroid.shared.presentation.components.ButtonVariant
import com.crackmydroid.shared.presentation.components.ErrorDialogComponent
import com.crackmydroid.shared.presentation.components.ScrollToTopButton
import com.crackmydroid.shared.presentation.components.SectionHeader
import com.crackmydroid.shared.presentation.components.text.AppText
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
    val strings = LocalStrings.current
    val screenStrings = ScreenUiStrings.from(strings)
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val showScrollTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 1 || listState.firstVisibleItemScrollOffset > 240 }
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
        viewModel.requestExecution(command)
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = screenContentPadding(padding, horizontal = 16.dp, vertical = 10.dp)
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

    state.pendingCommand?.let { command ->
        AlertDialog(
            onDismissRequest = viewModel::dismissPendingCommand,
            title = { AppText(screenStrings.confirmCommand) },
            text = {
                AppText(
                    text = command.confirmation ?: "${screenStrings.execute}: ${command.command}",
                    style = MaterialTheme.typography.bodySmall
                )
            },
            confirmButton = {
                AppButton(
                    text = if (state.running) screenStrings.executing else screenStrings.execute,
                    enabled = !state.running,
                    fullWidth = false,
                    onClick = {
                        viewModel.confirmPendingExecution()
                    }
                )
            },
            dismissButton = {
                AppButton(
                    text = screenStrings.cancel,
                    fullWidth = false,
                    variant = ButtonVariant.Secondary,
                    onClick = viewModel::dismissPendingCommand
                )
            }
        )
    }

    state.error?.let { ErrorDialogComponent(it, viewModel::clearError) }

    state.resultToDisplay?.let { res ->
        AlertDialog(
            onDismissRequest = viewModel::dismissResult,
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
                    commandTitle = state.lastCommandTitle,
                    stdoutLines = state.stdoutLines,
                    stderrLines = state.stderrLines,
                    packageLines = state.packageLines,
                    showPackageList = state.stdoutIsPackageList
                )
            },
            confirmButton = {
                AppButton(
                    text = strings.close,
                    fullWidth = false,
                    onClick = viewModel::dismissResult
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
    val screenStrings = ScreenUiStrings.from(LocalStrings.current)
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
                            unsupported -> command.unsupportedReason ?: screenStrings.commandUnavailable
                            command.requiresRoot -> screenStrings.privilegedCommand
                            else -> screenStrings.standardCommand
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
                    text = if (command.requiresRoot) screenStrings.rootTag else screenStrings.safeTag,
                    tint = if (command.requiresRoot) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                )
                if (unsupported) {
                    CommandTag(
                        text = screenStrings.unavailableTag,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                if (command.confirmation != null) {
                    CommandTag(
                        text = screenStrings.confirmationTag,
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
                    runningThis -> screenStrings.executing
                    unsupported -> LocalStrings.current.commandError
                    disabledByOther -> screenStrings.waitForCommand
                    else -> screenStrings.executeCommand
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
    val screenStrings = ScreenUiStrings.from(LocalStrings.current)
    val title = commandTitle ?: screenStrings.command
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
                    text = "${screenStrings.exitCode.format(result.code)} - $title",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                val subtitle = when {
                    result.stderr.isNotBlank() -> result.stderr.lineSequence().first().trim()
                    result.stdout.isNotBlank() -> result.stdout.lineSequence().first().trim()
                    else -> screenStrings.noTextOutput
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
    commandTitle: String?,
    stdoutLines: List<String>,
    stderrLines: List<String>,
    packageLines: List<String>,
    showPackageList: Boolean
) {
    val screenStrings = ScreenUiStrings.from(LocalStrings.current)

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
                        text = screenStrings.exitCode.format(result.code),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (showPackageList) {
            item {
                AppText(
                    text = screenStrings.packagesFound.format(packageLines.size),
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
                    text = screenStrings.output,
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
                    text = screenStrings.errorOutput,
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
                    text = screenStrings.noTextOutput,
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
