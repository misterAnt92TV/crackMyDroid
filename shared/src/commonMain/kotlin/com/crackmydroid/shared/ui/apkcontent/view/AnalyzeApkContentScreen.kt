package com.crackmydroid.shared.ui.apkcontent.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.crackmydroid.architecture.presentation.model.UiLoadState
import com.crackmydroid.shared.domain.model.InstalledAppEntry
import com.crackmydroid.shared.domain.model.apkcontent.ApkArchiveMetadata
import com.crackmydroid.shared.domain.model.apkcontent.ApkContentAnalysis
import com.crackmydroid.shared.domain.model.apkcontent.ApkContentFinding
import com.crackmydroid.shared.domain.model.apkcontent.ApkFindingSeverity
import com.crackmydroid.shared.domain.model.apkcontent.ApkLibrary
import com.crackmydroid.shared.domain.model.apkcontent.ApkLibraryAdvisory
import com.crackmydroid.shared.domain.model.apkcontent.ApkLibraryVulnerabilityReport
import com.crackmydroid.shared.domain.model.apkcontent.ApkLibraryVulnerabilityResult
import com.crackmydroid.shared.domain.model.apkcontent.ApkMavenLibrary
import com.crackmydroid.shared.domain.model.apkcontent.AdvisoryLookupAvailability
import com.crackmydroid.shared.domain.model.apkcontent.AdvisoryLookupFailureKind
import com.crackmydroid.shared.domain.model.apkcontent.AdvisorySeverity
import com.crackmydroid.shared.domain.model.apkcontent.MavenLibrarySource
import com.crackmydroid.shared.domain.model.apkcontent.ApkPlainTextCategory
import com.crackmydroid.shared.domain.model.apkcontent.ApkPlainTextItem
import com.crackmydroid.shared.domain.model.apkcontent.ApkPlaintextContent
import com.crackmydroid.shared.i18n.LocalStrings
import com.crackmydroid.shared.i18n.ScreenUiStrings
import com.crackmydroid.shared.i18n.format
import com.crackmydroid.shared.presentation.components.SearchPanelComponent
import com.crackmydroid.shared.ui.apkcontent.AnalyzeApkContentEvent
import com.crackmydroid.shared.ui.apkcontent.AnalyzeApkContentState
import com.crackmydroid.shared.ui.apkcontent.AnalyzeApkContentViewModel
import com.crackmydroid.shared.ui.screens.screenContentPadding
import com.crackmydroid.ui.components.AppIcon
import com.crackmydroid.ui.components.AppAccordion
import com.crackmydroid.ui.components.PackageIcon
import com.crackmydroid.ui.components.button.AppButton
import com.crackmydroid.ui.components.button.ButtonVariant
import com.crackmydroid.ui.components.card.AppCard
import com.crackmydroid.ui.components.dialog.ErrorDialogComponent
import com.crackmydroid.ui.components.progress.AppProgressBar
import com.crackmydroid.ui.components.progress.ProgressBarVariant
import com.crackmydroid.ui.components.text.AppText
import com.crackmydroid.ui.icons.AppIcons
import com.crackmydroid.ui.preview.AppComponentPreviews
import com.crackmydroid.ui.preview.ComponentPreviewContainer
import com.crackmydroid.ui.theme.CrackMyDroidTheme

@Composable
fun AnalyzeApkContentScreen(
    viewModel: AnalyzeApkContentViewModel,
    padding: PaddingValues,
    searchRequestId: Long = 0,
    searchVisible: Boolean = true,
    onSearchVisibilityChanged: (Boolean) -> Unit = {},
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        if (state.apps.isEmpty() && state.loadState is UiLoadState.Idle) {
            viewModel.handleEvent(AnalyzeApkContentEvent.Refresh())
        }
    }

    AnalyzeApkContentContent(
        state = state,
        padding = padding,
        searchRequestId = searchRequestId,
        searchVisible = searchVisible,
        onSearchVisibilityChanged = onSearchVisibilityChanged,
        onEvent = viewModel::handleEvent,
    )
}

@Composable
private fun AnalyzeApkContentContent(
    state: AnalyzeApkContentState,
    padding: PaddingValues,
    searchRequestId: Long,
    searchVisible: Boolean,
    onSearchVisibilityChanged: (Boolean) -> Unit,
    onEvent: (AnalyzeApkContentEvent) -> Unit,
) {
    val strings = LocalStrings.current
    val screenStrings = ScreenUiStrings.from(strings)
    val uriHandler = LocalUriHandler.current
    val listState = rememberLazyListState()
    val spacing = CrackMyDroidTheme.spacing
    val archiveButtonFocusRequester = remember { FocusRequester() }
    var restoreArchiveFocus by remember { mutableStateOf(false) }

    LaunchedEffect(restoreArchiveFocus) {
        if (restoreArchiveFocus) {
            archiveButtonFocusRequester.requestFocus()
            restoreArchiveFocus = false
        }
    }

    LaunchedEffect(searchRequestId) {
        if (searchRequestId > 0) {
            listState.animateScrollToItem(0)
        }
    }

    LazyColumn(
        state = listState,
        contentPadding = screenContentPadding(padding),
        verticalArrangement = Arrangement.spacedBy(spacing.itemSpacing),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            StepHeaderCard(step = state.step, screenStrings = screenStrings)
        }

        when (state.step) {
            1 -> {
                item {
                    SearchPanelComponent(
                        title = strings.apkSearch,
                        subtitle = screenStrings.searchFilters,
                        expanded = searchVisible,
                        onExpandedChange = onSearchVisibilityChanged,
                        query = state.query,
                        onQueryChange = { onEvent(AnalyzeApkContentEvent.QueryChanged(it)) },
                        onSubmit = { onEvent(AnalyzeApkContentEvent.QueryChanged(it)) },
                        label = strings.apkSearch,
                        historyKey = "analyze_apk_content",
                        focusRequestId = searchRequestId,
                        showClearAction = true,
                    )
                }

                if (state.loadState is UiLoadState.Loading) {
                    item {
                        AppProgressBar(
                            modifier = Modifier.fillMaxWidth(),
                            variant = ProgressBarVariant.Primary,
                        )
                    }
                }

                if (state.filteredApps.isEmpty() && state.loadState !is UiLoadState.Loading) {
                    item {
                        AppCard(modifier = Modifier.fillMaxWidth()) {
                            AppText(
                                text = if (state.query.isNotBlank()) {
                                    screenStrings.noResultsFor.format(state.query)
                                } else {
                                    screenStrings.noApps
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else {
                    items(
                        count = state.filteredApps.size,
                        key = { state.filteredApps[it].packageName },
                    ) { index ->
                        AppItemCard(
                            app = state.filteredApps[index],
                            isFavorite = state.filteredApps[index].packageName in state.favorites,
                            onToggleFavorite = { onEvent(AnalyzeApkContentEvent.FavoriteToggled(it)) },
                            screenStrings = screenStrings,
                            onSelect = { onEvent(AnalyzeApkContentEvent.PackageSelected(it)) },
                        )
                    }
                }
            }

            2 -> {
                item {
                    ConfirmAndAnalyzeCard(
                        app = state.selectedApp,
                        packageName = state.selectedPackage.orEmpty(),
                        analysisState = state.analysisState,
                        screenStrings = screenStrings,
                        onAnalyze = { onEvent(AnalyzeApkContentEvent.AnalysisRequested()) },
                        onChangeApp = { onEvent(AnalyzeApkContentEvent.ClearSelection()) },
                    )
                }
            }

            3 -> {
                state.analysis?.let { analysis ->
                    item {
                        AnalysisReportCard(
                            analysis = analysis,
                            exportPath = state.exportPath,
                            screenStrings = screenStrings,
                            onExport = { onEvent(AnalyzeApkContentEvent.ReportExportRequested()) },
                            onShare = { onEvent(AnalyzeApkContentEvent.ReportShareRequested()) },
                            onOpenAnalyzedArchive = { onEvent(AnalyzeApkContentEvent.ArchiveOpenRequested()) },
                            archiveNavigationState = state.archiveNavigationState,
                            archiveButtonFocusRequester = archiveButtonFocusRequester,
                            mavenLibraries = state.mavenLibraries,
                            advisoryState = state.advisoryState,
                            advisoryReport = state.advisoryReport,
                            onVulnerabilityLookup = { onEvent(AnalyzeApkContentEvent.VulnerabilityLookupRequested()) },
                            onOpenAdvisory = uriHandler::openUri,
                            onReset = { onEvent(AnalyzeApkContentEvent.Reset()) },
                        )
                    }
                }
            }
        }

    }

    state.error?.let { error ->
        ErrorDialogComponent(
            message = error,
            onDismiss = {
                restoreArchiveFocus = true
                onEvent(AnalyzeApkContentEvent.ErrorDismissed())
            },
        )
    }
}

@Composable
private fun StepHeaderCard(step: Int, screenStrings: ScreenUiStrings) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        val (title, description) = when (step) {
            1 ->
                screenStrings.apkContentStep1Title to screenStrings.apkContentStep1Desc

            2 ->
                screenStrings.apkContentStep2Title to screenStrings.apkContentStep2Desc

            else ->
                screenStrings.apkContentStep3Title to screenStrings.apkContentStep3Desc
        }
        AppText(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        AppText(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AppItemCard(
    app: InstalledAppEntry,
    isFavorite: Boolean,
    onToggleFavorite: (String) -> Unit,
    screenStrings: ScreenUiStrings,
    onSelect: (String) -> Unit,
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onSelect(app.packageName) },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CrackMyDroidTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PackageIcon(
                packageName = app.packageName,
                modifier = Modifier.size(36.dp),
                size = 36.dp,
                contentPadding = 4.dp,
                contentDescription = null,
            )
            IconButton(onClick = { onToggleFavorite(app.packageName) }) {
                AppIcon(
                    imageVector = if (isFavorite) AppIcons.Filled.Star else AppIcons.Filled.StarBorder,
                    contentDescription = LocalStrings.current.favoriteContentDescription,
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                AppText(
                    text = app.appLabel,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                AppText(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            AppButton(
                text = screenStrings.apkContentSelect,
                onClick = { onSelect(app.packageName) },
                variant = ButtonVariant.Secondary,
                fullWidth = false,
            )
        }
    }
}

@Composable
private fun ConfirmAndAnalyzeCard(
    app: InstalledAppEntry?,
    packageName: String,
    analysisState: UiLoadState,
    screenStrings: ScreenUiStrings,
    onAnalyze: () -> Unit,
    onChangeApp: () -> Unit,
) {
    val spacing = CrackMyDroidTheme.spacing
    val isLoading = analysisState is UiLoadState.Loading

    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            AppText(
                text = app?.appLabel ?: packageName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            AppText(
                text = packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = spacing.small),
                    verticalArrangement = Arrangement.spacedBy(spacing.extraSmall),
                ) {
                    AppProgressBar(
                        modifier = Modifier.fillMaxWidth(),
                        variant = ProgressBarVariant.Primary,
                    )
                    AppText(
                        text = screenStrings.apkContentAnalyzingProgress,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.small),
            ) {
                AppButton(
                    text = screenStrings.apkContentChangeApp,
                    onClick = onChangeApp,
                    variant = ButtonVariant.Secondary,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f),
                    leadingIcon = AppIcons.AutoMirrored.Filled.ArrowBack,
                )
                AppButton(
                    text = if (isLoading) screenStrings.apkContentAnalyzing else screenStrings.apkContentAnalyze,
                    onClick = onAnalyze,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f),
                    leadingIcon = AppIcons.Filled.PlayArrow,
                )
            }
        }
    }
}

@Composable
private fun AnalysisReportCard(
    analysis: ApkContentAnalysis,
    exportPath: String?,
    screenStrings: ScreenUiStrings,
    onExport: () -> Unit,
    onShare: () -> Unit,
    onOpenAnalyzedArchive: () -> Unit,
    archiveNavigationState: UiLoadState,
    archiveButtonFocusRequester: FocusRequester,
    mavenLibraries: List<ApkMavenLibrary>,
    advisoryState: UiLoadState,
    advisoryReport: ApkLibraryVulnerabilityReport?,
    onVulnerabilityLookup: () -> Unit,
    onOpenAdvisory: (String) -> Unit,
    onReset: () -> Unit,
) {
    val spacing = CrackMyDroidTheme.spacing

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.itemSpacing),
    ) {
        ReportSummaryHeader(analysis = analysis, screenStrings = screenStrings)

        ReportMetricsGrid(metadata = analysis.metadata, screenStrings = screenStrings)

        ReportLibrariesCard(libraries = analysis.libraries, screenStrings = screenStrings)

        if (mavenLibraries.isNotEmpty()) {
            LibraryAdvisoriesCard(
                libraries = mavenLibraries,
                lookupState = advisoryState,
                report = advisoryReport,
                screenStrings = screenStrings,
                onLookup = onVulnerabilityLookup,
                onOpenAdvisory = onOpenAdvisory,
            )
        }

        ReportFindingsCard(findings = analysis.findings, screenStrings = screenStrings)

        ReportPlaintextCard(plaintextContent = analysis.plaintextContent, screenStrings = screenStrings)

        ReportDisclaimerCard(screenStrings = screenStrings)

        exportPath?.let { path ->
            ExportNoticeCard(path = path, screenStrings = screenStrings)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            AppButton(
                text = screenStrings.apkContentExportReport,
                onClick = onExport,
                variant = ButtonVariant.Secondary,
                modifier = Modifier.weight(1f),
                leadingIcon = AppIcons.Filled.Description,
            )
            AppButton(
                text = screenStrings.apkContentShareReport,
                onClick = onShare,
                variant = ButtonVariant.Secondary,
                modifier = Modifier.weight(1f),
                leadingIcon = AppIcons.Filled.Share,
            )
        }

        val openingArchive = archiveNavigationState is UiLoadState.Loading
        if (openingArchive) {
            AppProgressBar(modifier = Modifier.fillMaxWidth(), variant = ProgressBarVariant.Primary)
        }
        AppButton(
            text = if (openingArchive) screenStrings.apkContentPreparingArchive else screenStrings.apkContentOpenAnalyzedArchive,
            onClick = onOpenAnalyzedArchive,
            enabled = !openingArchive,
            modifier = Modifier.focusRequester(archiveButtonFocusRequester),
            variant = ButtonVariant.Tonal,
            fullWidth = true,
            leadingIcon = AppIcons.Filled.PlayArrow,
        )

        AppButton(
            text = screenStrings.apkContentAnalyzeAnother,
            onClick = onReset,
            variant = ButtonVariant.Primary,
            fullWidth = true,
            leadingIcon = AppIcons.Filled.Refresh,
        )
    }
}

@Composable
private fun ReportSummaryHeader(analysis: ApkContentAnalysis, screenStrings: ScreenUiStrings) {
    val spacing = CrackMyDroidTheme.spacing
    val (statusIcon, containerColor, contentColor) = when (analysis.highestSeverity) {
        ApkFindingSeverity.Critical, ApkFindingSeverity.High ->
            Triple(
                AppIcons.Filled.Error,
                MaterialTheme.colorScheme.errorContainer,
                MaterialTheme.colorScheme.onErrorContainer,
            )

        ApkFindingSeverity.Medium ->
            Triple(
                AppIcons.Filled.Warning,
                MaterialTheme.colorScheme.tertiaryContainer,
                MaterialTheme.colorScheme.onTertiaryContainer,
            )

        ApkFindingSeverity.Info ->
            Triple(
                AppIcons.Filled.CheckCircle,
                MaterialTheme.colorScheme.primaryContainer,
                MaterialTheme.colorScheme.onPrimaryContainer,
            )
    }

    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(spacing.medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppIcon(
                    imageVector = statusIcon,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = contentColor,
                )
                Column {
                    AppText(
                        text = screenStrings.apkContentReportTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    AppText(
                        text = analysis.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(spacing.extraSmall))
                    .background(containerColor)
                    .padding(horizontal = spacing.medium, vertical = spacing.extraSmall),
            ) {
                AppText(
                    text = screenStrings.apkContentRiskBadge.format(analysis.highestSeverity.name.uppercase()),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                )
            }
        }
    }
}

@Composable
private fun ReportMetricsGrid(metadata: ApkArchiveMetadata, screenStrings: ScreenUiStrings) {
    val spacing = CrackMyDroidTheme.spacing

    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppIcon(
                    imageVector = AppIcons.Filled.SdStorage,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                AppText(
                    text = screenStrings.apkContentMetricsTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.medium),
            ) {
                MetricItem(
                    label = screenStrings.apkContentMetricArchiveSize,
                    value = formatSize(metadata.archiveBytes),
                    modifier = Modifier.weight(1f),
                )
                MetricItem(
                    label = screenStrings.apkContentMetricUncompressed,
                    value = formatSize(metadata.uncompressedBytes),
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.medium),
            ) {
                MetricItem(
                    label = screenStrings.apkContentMetricTotalEntries,
                    value = metadata.entryCount.toString(),
                    modifier = Modifier.weight(1f),
                )
                MetricItem(
                    label = screenStrings.apkContentMetricDexFiles,
                    value = metadata.dexCount.toString(),
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.medium),
            ) {
                MetricItem(
                    label = screenStrings.apkContentMetricNativeLibs,
                    value = metadata.nativeLibraries.size.toString(),
                    modifier = Modifier.weight(1f),
                )
                MetricItem(
                    label = screenStrings.apkContentMetricCerts,
                    value = metadata.certificateEntries.size.toString(),
                    modifier = Modifier.weight(1f),
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(spacing.extraSmall))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(spacing.small),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.extraSmall)) {
                    AppText(
                        text = screenStrings.apkContentChecksumSha256,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                    )
                    AppText(
                        text = metadata.sha256,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Normal,
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        AppText(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        AppText(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun ReportLibrariesCard(libraries: List<ApkLibrary>, screenStrings: ScreenUiStrings) {
    val spacing = CrackMyDroidTheme.spacing

    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppIcon(
                    imageVector = AppIcons.Filled.Code,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                AppText(
                    text = screenStrings.apkContentLibrariesTitle.format(libraries.size),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            if (libraries.isEmpty()) {
                AppText(
                    text = screenStrings.apkContentNoLibraries,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(spacing.extraSmall),
                ) {
                    libraries.forEach { lib ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(spacing.extraSmall))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = spacing.medium, vertical = spacing.small),
                        ) {
                            Column {
                                AppText(
                                    text = lib.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                                AppText(
                                    text = lib.evidence,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryAdvisoriesCard(
    libraries: List<ApkMavenLibrary>,
    lookupState: UiLoadState,
    report: ApkLibraryVulnerabilityReport?,
    screenStrings: ScreenUiStrings,
    onLookup: () -> Unit,
    onOpenAdvisory: (String) -> Unit,
) {
    val spacing = CrackMyDroidTheme.spacing
    val isLoading = lookupState is UiLoadState.Loading
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppIcon(
                    imageVector = AppIcons.Filled.Security,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                AppText(
                    text = screenStrings.apkContentAdvisoriesTitle.format(libraries.size),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            AppText(
                text = screenStrings.apkContentAdvisoriesPrivacy,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (isLoading) {
                AppProgressBar(modifier = Modifier.fillMaxWidth(), variant = ProgressBarVariant.Primary)
                AppText(
                    text = screenStrings.apkContentCheckingVulnerabilities,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            } else if (report == null) {
                AppButton(
                    text = screenStrings.apkContentCheckVulnerabilities,
                    onClick = onLookup,
                    variant = ButtonVariant.Tonal,
                    fullWidth = true,
                    leadingIcon = AppIcons.Filled.Security,
                )
            } else {
                AdvisoryReportContent(report, libraries, screenStrings, onOpenAdvisory)
            }
        }
    }
}

@Composable
private fun AdvisoryReportContent(
    report: ApkLibraryVulnerabilityReport,
    libraries: List<ApkMavenLibrary>,
    screenStrings: ScreenUiStrings,
    onOpenAdvisory: (String) -> Unit,
) {
    when {
        report.availability == AdvisoryLookupAvailability.NotConfigured -> AppText(
            text = screenStrings.apkContentAdvisoryNotConfigured,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        report.failure != null -> AppText(
            text = screenStrings.apkContentAdvisoryUnavailable,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )

        else -> Column(verticalArrangement = Arrangement.spacedBy(CrackMyDroidTheme.spacing.small)) {
            report.results.forEach { result ->
                val library = libraries.firstOrNull { it.packageUrl == result.packageUrl }
                AdvisoryLibrarySection(
                    title = library?.displayName ?: result.packageUrl,
                    version = result.version,
                    source = library?.source,
                    advisories = result.advisories,
                    screenStrings = screenStrings,
                    onOpenAdvisory = onOpenAdvisory,
                )
            }
        }
    }
}

@Composable
private fun AdvisoryLibrarySection(
    title: String,
    version: String,
    source: MavenLibrarySource?,
    advisories: List<ApkLibraryAdvisory>,
    screenStrings: ScreenUiStrings,
    onOpenAdvisory: (String) -> Unit,
) {
    var expanded by remember(title, version, advisories) { mutableStateOf(false) }
    val sourceLabel = when (source) {
        MavenLibrarySource.PomProperties -> screenStrings.apkContentMetadataPom
        MavenLibrarySource.VersionMetadata -> screenStrings.apkContentMetadataVersion
        null -> null
    }
    AppAccordion(
        title = title,
        subtitle = version,
        meta = if (advisories.isEmpty()) "0" else screenStrings.apkContentAdvisoriesFound.format(advisories.size),
        expanded = expanded,
        onToggle = { expanded = !expanded },
        accessibilityLabel = "$title $version",
        titleMaxLines = 3,
        subtitleMaxLines = 2,
        textOverflow = TextOverflow.Clip,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(CrackMyDroidTheme.spacing.small)) {
            sourceLabel?.let {
                AppText(
                    text = screenStrings.apkContentLibraryMetadataSource.format(it),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (advisories.isEmpty()) {
                AppText(
                    text = screenStrings.apkContentNoAdvisories,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                advisories.forEach { advisory ->
                    AdvisoryItem(advisory, screenStrings, onOpenAdvisory)
                }
            }
        }
    }
}

@Composable
private fun AdvisoryItem(
    advisory: ApkLibraryAdvisory,
    screenStrings: ScreenUiStrings,
    onOpenAdvisory: (String) -> Unit,
) {
    val spacing = CrackMyDroidTheme.spacing
    val severityColor = when (advisory.severity) {
        AdvisorySeverity.Critical, AdvisorySeverity.High -> MaterialTheme.colorScheme.error
        AdvisorySeverity.Medium -> MaterialTheme.colorScheme.tertiary
        AdvisorySeverity.Low, AdvisorySeverity.Unknown -> MaterialTheme.colorScheme.primary
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(spacing.extraSmall))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(spacing.small),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.extraSmall)) {
            AppText(
                text = "${advisory.source.name.uppercase()} · ${advisory.advisoryId} · ${advisory.severity.name.uppercase()}",
                style = MaterialTheme.typography.labelSmall,
                color = severityColor,
                fontWeight = FontWeight.Bold,
            )
            AppText(text = advisory.summary, style = MaterialTheme.typography.bodySmall)
            advisory.affectedVersions?.let {
                AppText(
                    text = screenStrings.apkContentAffectedVersions.format(it),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            advisory.fixedVersion?.let {
                AppText(
                    text = screenStrings.apkContentFixedVersion.format(it),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AppButton(
                text = screenStrings.apkContentOpenAdvisory,
                onClick = { onOpenAdvisory(advisory.url) },
                variant = ButtonVariant.Secondary,
                fullWidth = false,
            )
        }
    }
}

@Composable
private fun ReportFindingsCard(findings: List<ApkContentFinding>, screenStrings: ScreenUiStrings) {
    val spacing = CrackMyDroidTheme.spacing

    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppIcon(
                    imageVector = AppIcons.Filled.Security,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                AppText(
                    text = screenStrings.apkContentFindingsTitle.format(findings.size),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            if (findings.isEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.small),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppIcon(
                        imageVector = AppIcons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    AppText(
                        text = screenStrings.apkContentNoFindings,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(spacing.medium),
                ) {
                    findings.forEachIndexed { index, finding ->
                        FindingItem(finding = finding, screenStrings = screenStrings)
                        if (index < findings.lastIndex) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FindingItem(finding: ApkContentFinding, screenStrings: ScreenUiStrings) {
    val spacing = CrackMyDroidTheme.spacing
    val (pillBg, pillFg) = when (finding.severity) {
        ApkFindingSeverity.Critical, ApkFindingSeverity.High ->
            MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer

        ApkFindingSeverity.Medium ->
            MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer

        ApkFindingSeverity.Info ->
            MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.extraSmall),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppText(
                text = finding.category,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(spacing.extraSmall))
                    .background(pillBg)
                    .padding(horizontal = spacing.small, vertical = 2.dp),
            ) {
                AppText(
                    text = finding.severity.name.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = pillFg,
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(spacing.extraSmall))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(spacing.small),
        ) {
            AppText(
                text = finding.evidence,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall),
            verticalAlignment = Alignment.Top,
        ) {
            AppIcon(
                imageVector = AppIcons.Filled.TipsAndUpdates,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            AppText(
                text = screenStrings.apkContentRemediationPrefix.format(finding.remediation),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ReportPlaintextCard(plaintextContent: ApkPlaintextContent, screenStrings: ScreenUiStrings) {
    val spacing = CrackMyDroidTheme.spacing

    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppIcon(
                    imageVector = AppIcons.Filled.Description,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                AppText(
                    text = screenStrings.apkContentPlaintextTitle.format(plaintextContent.totalCount),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            if (plaintextContent.items.isEmpty()) {
                AppText(
                    text = screenStrings.apkContentNoPlaintext,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                PlaintextSection(
                    title = screenStrings.apkContentPlaintextFilterEndpoints,
                    items = plaintextContent.endpoints,
                    screenStrings = screenStrings,
                )
                PlaintextSection(
                    title = screenStrings.apkContentPlaintextFilterConfig,
                    items = plaintextContent.configKeys,
                    screenStrings = screenStrings,
                )
                PlaintextSection(
                    title = screenStrings.apkContentPlaintextFilterStrings,
                    items = plaintextContent.textStrings,
                    screenStrings = screenStrings,
                )
            }
        }
    }
}

@Composable
private fun PlaintextSection(title: String, items: List<ApkPlainTextItem>, screenStrings: ScreenUiStrings) {
    if (items.isEmpty()) return
    var expanded by remember(title, items) { mutableStateOf(false) }
    val visibleItems = if (expanded) items else items.take(3)
    AppAccordion(
        title = title,
        meta = items.size.toString(),
        expanded = expanded,
        onToggle = { expanded = !expanded },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(CrackMyDroidTheme.spacing.extraSmall)) {
            visibleItems.forEach { PlaintextItemRow(item = it, screenStrings = screenStrings) }
            if (!expanded && items.size > visibleItems.size) {
                AppText(
                    text = screenStrings.apkContentPlaintextPreview.format(items.size - visibleItems.size),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun PlaintextItemRow(item: ApkPlainTextItem, screenStrings: ScreenUiStrings) {
    val spacing = CrackMyDroidTheme.spacing
    val categoryLabel = when (item.category) {
        ApkPlainTextCategory.Endpoint -> "URL"
        ApkPlainTextCategory.ConfigurationKey -> "CONFIG"
        ApkPlainTextCategory.TextString -> "TEXT"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(spacing.extraSmall))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(spacing.small),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    AppText(
                        text = categoryLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                    )
                }

                AppText(
                    text = screenStrings.apkContentSourceLabel.format(item.source),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            AppText(
                text = item.value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun ReportDisclaimerCard(screenStrings: ScreenUiStrings) {
    val spacing = CrackMyDroidTheme.spacing

    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIcon(
                imageVector = AppIcons.Filled.Info,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AppText(
                text = screenStrings.apkContentDisclaimer,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ExportNoticeCard(path: String, screenStrings: ScreenUiStrings) {
    val spacing = CrackMyDroidTheme.spacing

    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIcon(
                imageVector = AppIcons.Filled.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            AppText(
                text = screenStrings.apkContentExportedTo.format(path),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

private fun formatSize(bytes: Long): String = when {
    bytes >= 1024 * 1024 -> "${bytes / (1024 * 1024)} MB ($bytes B)"
    bytes >= 1024 -> "${bytes / 1024} KB ($bytes B)"
    else -> "$bytes B"
}

@AppComponentPreviews
@Composable
private fun AppItemCardPreview() = ComponentPreviewContainer {
    AppItemCard(
        app = InstalledAppEntry(
            appLabel = "Example application",
            packageName = "com.example.application",
            sourcePath = "/data/app/com.example.application/base.apk",
        ),
        isFavorite = false,
        onToggleFavorite = {},
        screenStrings = ScreenUiStrings("en"),
        onSelect = {},
    )
}

@AppComponentPreviews
@Composable
private fun LibraryAdvisoriesCardPreview() = ComponentPreviewContainer {
    LibraryAdvisoriesCard(
        libraries = listOf(
            ApkMavenLibrary(
                displayName = "androidx.window:window",
                packageUrl = "pkg:maven/androidx.window/window@1.5.0",
                version = "1.5.0",
                source = MavenLibrarySource.VersionMetadata,
            ),
        ),
        lookupState = UiLoadState.Idle,
        report = null,
        screenStrings = ScreenUiStrings("en"),
        onLookup = {},
        onOpenAdvisory = {},
    )
}

@AppComponentPreviews
@Composable
private fun LibraryAdvisoriesNotConfiguredPreview() = ComponentPreviewContainer {
    LibraryAdvisoriesCard(
        libraries = previewAdvisoryLibraries(),
        lookupState = UiLoadState.Success,
        report = ApkLibraryVulnerabilityReport(AdvisoryLookupAvailability.NotConfigured),
        screenStrings = ScreenUiStrings("en"),
        onLookup = {},
        onOpenAdvisory = {},
    )
}

@AppComponentPreviews
@Composable
private fun LibraryAdvisoriesUnavailablePreview() = ComponentPreviewContainer {
    LibraryAdvisoriesCard(
        libraries = previewAdvisoryLibraries(),
        lookupState = UiLoadState.Success,
        report = ApkLibraryVulnerabilityReport(
            availability = AdvisoryLookupAvailability.Available,
            failure = AdvisoryLookupFailureKind.Network,
        ),
        screenStrings = ScreenUiStrings("en"),
        onLookup = {},
        onOpenAdvisory = {},
    )
}

@AppComponentPreviews
@Composable
private fun LibraryAdvisoriesFoundPreview() = ComponentPreviewContainer {
    val library = previewAdvisoryLibraries().single()
    LibraryAdvisoriesCard(
        libraries = listOf(library),
        lookupState = UiLoadState.Success,
        report = ApkLibraryVulnerabilityReport(
            availability = AdvisoryLookupAvailability.Available,
            results = listOf(
                ApkLibraryVulnerabilityResult(
                    packageUrl = library.packageUrl,
                    version = library.version,
                    advisories = listOf(
                        ApkLibraryAdvisory(
                            source = com.crackmydroid.shared.domain.model.apkcontent.AdvisorySource.Osv,
                            advisoryId = "GHSA-example",
                            severity = AdvisorySeverity.High,
                            summary = "Example dependency vulnerability",
                            fixedVersion = "1.5.1",
                            url = "https://osv.dev/",
                        ),
                    ),
                ),
            ),
        ),
        screenStrings = ScreenUiStrings("en"),
        onLookup = {},
        onOpenAdvisory = {},
    )
}

private fun previewAdvisoryLibraries() = listOf(
    ApkMavenLibrary(
        displayName = "androidx.window:window",
        packageUrl = "pkg:maven/androidx.window/window@1.5.0",
        version = "1.5.0",
        source = MavenLibrarySource.VersionMetadata,
    ),
)
