package com.crackmydroid.shared.ui.activities

import com.crackmydroid.shared.domain.model.ActivityEntry
import com.crackmydroid.shared.domain.model.ActivityIntentRiskAnalyzer
import com.crackmydroid.shared.domain.model.ActivityIntentRiskLevel
import com.crackmydroid.shared.domain.model.ActivityIntentRiskReport
import com.crackmydroid.shared.domain.usecase.GetActivitiesUseCase
import com.crackmydroid.shared.domain.usecase.LaunchActivityUseCase
import com.crackmydroid.shared.domain.usecase.GetExportFormatUseCase
import com.crackmydroid.shared.domain.usecase.GetActivityFavoritesUseCase
import com.crackmydroid.shared.domain.usecase.SetActivityFavoritesUseCase
import com.crackmydroid.shared.domain.usecase.GetActivityPackageFavoritesUseCase
import com.crackmydroid.shared.domain.usecase.SetActivityPackageFavoritesUseCase
import com.crackmydroid.shared.presentation.BaseViewModel
import com.crackmydroid.shared.presentation.operations.OperationLogStore
import com.crackmydroid.shared.util.saveTextFile
import com.crackmydroid.shared.util.shareTextFile
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ActivityListViewModel(
    private val getActivities: GetActivitiesUseCase,
    private val launchActivity: LaunchActivityUseCase,
    private val getExportFormat: GetExportFormatUseCase
    , private val getFavorites: GetActivityFavoritesUseCase? = null
    , private val setFavorites: SetActivityFavoritesUseCase? = null
    , private val getPackageFavorites: GetActivityPackageFavoritesUseCase? = null
    , private val setPackageFavorites: SetActivityPackageFavoritesUseCase? = null
) : BaseViewModel() {
    private val json = Json { prettyPrint = true }
    private val _state = MutableStateFlow(ActivityListState())
    val state: StateFlow<ActivityListState> = _state
    private val queryCount = mutableMapOf<String, Int>()
    private var recomputeJob: Job? = null
    private val multiSpaceRegex = Regex("\\s+")

    fun refresh() {
        scope.launch {
            runCatching { getFavorites?.invoke().orEmpty() to getPackageFavorites?.invoke().orEmpty() }.onSuccess { (favorites, packages) ->
                _state.update { it.copy(favorites = favorites, favoritePackages = packages) }
            }
            _state.update { it.copy(loading = true, error = null) }
            runCatching { getActivities() }
                .onSuccess { list ->
                    recomputeAsync(_state.value.copy(activities = list, loading = false))
                    OperationLogStore.record("Caricate ${list.size} activity", success = true)
                }
                .onFailure { err ->
                    _state.update { it.copy(error = err.message ?: "Errore", loading = false) }
                    OperationLogStore.record("Caricamento activity", success = false, details = err.message)
                }
        }
    }

    fun setFilter(value: String, trackUsage: Boolean = false) {
        val top = if (trackUsage) {
            val normalized = normalizeQuery(value)
            if (normalized.length >= 2) {
                val count = (queryCount[normalized] ?: 0) + 1
                queryCount[normalized] = count
                queryCount.maxByOrNull { it.value }?.key
            } else {
                _state.value.topQuery
            }
        } else {
            _state.value.topQuery
        }
        recomputeAsync(_state.value.copy(topQuery = top, filter = value))
    }

    fun launch(entry: ActivityEntry) {
        launchInternal(entry)
    }

    fun launchWithIntent(
        entry: ActivityEntry,
        action: String?,
        dataUri: String?,
        mimeType: String?
    ) {
        launchInternal(entry, action, dataUri, mimeType)
    }

    private fun launchInternal(
        entry: ActivityEntry,
        action: String? = null,
        dataUri: String? = null,
        mimeType: String? = null
    ) {
        val risk = ActivityIntentRiskAnalyzer.analyze(entry)
        val normalizedAction = action?.trim().orEmpty().ifBlank { null }
        val normalizedData = dataUri?.trim().orEmpty().ifBlank { null }
        val normalizedMimeType = mimeType?.trim().orEmpty().ifBlank { null }
        val hasGuidedIntent = normalizedAction != null || normalizedData != null || normalizedMimeType != null
        if (!entry.launchableViaShell && !hasGuidedIntent) {
            val msg = entry.launchabilityReason ?: "Activity non avviabile via am start -n"
            _state.update { it.copy(error = msg) }
            OperationLogStore.record(
                "Lancio ${entry.activityName}",
                success = false,
                details = "$msg • rischio ${ActivityIntentRiskAnalyzer.formatLevel(risk.level)}"
            )
            return
        }
        scope.launch {
            val result = launchActivity(entry, normalizedAction, normalizedData, normalizedMimeType)
            result.onFailure { err ->
                val msg = err.message ?: "Impossibile avviare ${entry.activityName}"
                _state.update { it.copy(error = msg) }
                OperationLogStore.record(
                    "Lancio ${entry.activityName}",
                    success = false,
                    details = "$msg • rischio ${ActivityIntentRiskAnalyzer.formatLevel(risk.level)}"
                )
            }.onSuccess {
                OperationLogStore.record(
                    "Lancio ${entry.activityName}",
                    success = true,
                    details = "Rischio ${ActivityIntentRiskAnalyzer.formatLevel(risk.level)} • ${risk.summary}"
                )
            }
        }
    }

    fun groups(): List<ActivityGroup> = _state.value.groups

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun toggleFavorite(entry: ActivityEntry) {
        val key = activityKey(entry)
        val favs = _state.value.favorites.toMutableSet()
        if (favs.contains(key)) favs.remove(key) else favs.add(key)
        recomputeAsync(_state.value.copy(favorites = favs))
        scope.launch { setFavorites?.invoke(favs) }
    }

    fun toggleFavoritePackage(packageName: String) {
        val favs = _state.value.favoritePackages.toMutableSet()
        if (favs.contains(packageName)) favs.remove(packageName) else favs.add(packageName)
        recomputeAsync(_state.value.copy(favoritePackages = favs))
        scope.launch { setPackageFavorites?.invoke(favs) }
    }

    fun toggleExpanded(packageName: String) {
        val expanded = _state.value.expandedPackages.toMutableSet()
        if (expanded.contains(packageName)) expanded.remove(packageName) else expanded.add(packageName)
        _state.update { it.copy(expandedPackages = expanded) }
    }

    fun exportAll(): Result<String> {
        val content = buildExportContent(_state.value.activities)
        val result = saveTextFile("activities${extForCurrentFormat()}", content)
        result.onSuccess { path ->
            OperationLogStore.record("Export attività", success = true, details = path)
        }.onFailure { err ->
            _state.update { it.copy(error = err.message) }
            OperationLogStore.record("Export attività", success = false, details = err.message)
        }
        return result
    }

    fun exportGroup(packageName: String) {
        val acts = _state.value.activities.filter { it.packageName == packageName }
        if (acts.isEmpty()) {
            _state.update { it.copy(error = "Nessuna activity per $packageName") }
            OperationLogStore.record("Export attività $packageName", success = false, details = "Nessuna activity")
            return
        }
        val content = buildExportContent(acts)
        val baseName = "activities_${packageName.replace('.', '_')}"
        val result = saveTextFile(baseName + extForCurrentFormat(), content)
        result.onSuccess { path ->
            shareTextFile(path).onFailure { err -> _state.update { it.copy(error = err.message) } }
            OperationLogStore.record("Export attività $packageName", success = true, details = path)
        }.onFailure { err ->
            _state.update { it.copy(error = err.message) }
            OperationLogStore.record("Export attività $packageName", success = false, details = err.message)
        }
    }

    fun groupContent(packageName: String): String? {
        val acts = _state.value.activities.filter { it.packageName == packageName }
        if (acts.isEmpty()) return null
        return buildExportContent(acts)
    }

    fun buildReport(packageName: String): ActivityAppReport? {
        val activities = _state.value.activities.filter { it.packageName == packageName }
        if (activities.isEmpty()) return null

        val favorites = _state.value.favorites
        val riskReports = activities.map(ActivityIntentRiskAnalyzer::analyze)
        val shellLaunchableCount = activities.count { it.launchableViaShell }
        val contextualLaunchCount = activities.count { it.hasGuidedLaunchMetadata() }
        val guidedLaunchCount = activities.count { !it.launchableViaShell && it.hasGuidedLaunchMetadata() }
        val dumpOnlyCount = activities.count { !it.launchableViaShell && !it.hasGuidedLaunchMetadata() }
        val riskyActivitiesCount = riskReports.count { it.level != ActivityIntentRiskLevel.Low }
        val favoriteActivitiesCount = activities.count { favorites.contains(activityKey(it)) }
        val highestRiskLevel = ActivityIntentRiskAnalyzer.summarize(riskReports)
        val findings = buildList {
            if (dumpOnlyCount > 0) {
                add("$dumpOnlyCount activity compaiono solo nel dump del package e richiedono verifica manuale.")
            }
            if (guidedLaunchCount > 0) {
                add("$guidedLaunchCount activity richiedono un intent contestualizzato per essere testate.")
            }
            if (contextualLaunchCount > 0) {
                add("$contextualLaunchCount activity espongono hint utili per un lancio guidato o per test deeplink.")
            }
            if (_state.value.favoritePackages.contains(packageName)) {
                add("L'app e stata marcata come preferita nella sezione Activity.")
            }
            if (favoriteActivitiesCount > 0) {
                add("$favoriteActivitiesCount activity sono state segnate come preferite per analisi ripetute.")
            }
            if (riskyActivitiesCount > 0) {
                add("$riskyActivitiesCount activity espongono superfici intent/deeplink con rischio ${ActivityIntentRiskAnalyzer.formatLevel(highestRiskLevel)}.")
            }
            if (isEmpty()) {
                add("Tutte le activity rilevate risultano avviabili via shell senza metadati aggiuntivi.")
            }
        }

        val recommendations = riskReports
            .filter { it.level != ActivityIntentRiskLevel.Low }
            .sortedByDescending { it.score }
            .map { it.recommendation }
            .distinct()
            .take(4)

        val highlights = riskReports.sortedWith(
            compareByDescending<ActivityIntentRiskReport> { favorites.contains(activityKey(it.entry)) }
                .thenByDescending { it.score }
                .thenBy { it.entry.activityName.lowercase() }
        ).take(10)

        return ActivityAppReport(
            packageName = packageName,
            appLabel = activities.firstOrNull()?.appLabel ?: packageName,
            totalActivities = activities.size,
            shellLaunchableCount = shellLaunchableCount,
            contextualLaunchCount = contextualLaunchCount,
            guidedLaunchCount = guidedLaunchCount,
            dumpOnlyCount = dumpOnlyCount,
            riskyActivitiesCount = riskyActivitiesCount,
            favoriteActivitiesCount = favoriteActivitiesCount,
            packageFavorite = _state.value.favoritePackages.contains(packageName),
            highestRiskLevel = highestRiskLevel,
            recommendations = recommendations,
            findings = findings,
            highlights = highlights
        )
    }

    fun exportVisualReport(packageName: String): Result<String> {
        val report = buildReport(packageName)
            ?: return Result.failure(IllegalArgumentException("Nessuna activity per $packageName"))
        val baseName = "activity_report_${packageName.replace('.', '_')}.html"
        val content = buildHtmlReport(report)
        val result = saveTextFile(baseName, content)
        result.onSuccess { path ->
            shareTextFile(path).onFailure { err -> _state.update { it.copy(error = err.message) } }
            OperationLogStore.record("Report activity $packageName", success = true, details = path)
        }.onFailure { err ->
            _state.update { it.copy(error = err.message) }
            OperationLogStore.record("Report activity $packageName", success = false, details = err.message)
        }
        return result
    }

    private fun buildExportContent(list: List<ActivityEntry>): String {
        val format = runBlocking { getExportFormat() }
        return if (format == com.crackmydroid.shared.domain.model.ExportFormat.JSON) {
            val payload = list.map { entry -> entry.toExportPayload() }
            json.encodeToString(payload)
        } else {
            buildString {
                list.forEach { act ->
                    val risk = ActivityIntentRiskAnalyzer.analyze(act)
                    appendLine("${act.label} (${act.packageName})")
                    appendLine("Activity: ${act.activityName}")
                    appendLine("Launch mode: ${risk.launchMode}")
                    appendLine("Rischio: ${ActivityIntentRiskAnalyzer.formatLevel(risk.level)}")
                    act.launchIntentAction?.takeIf { it.isNotBlank() }?.let { appendLine("Action: $it") }
                    act.launchIntentData?.takeIf { it.isNotBlank() }?.let { appendLine("Data: $it") }
                    act.launchIntentMimeType?.takeIf { it.isNotBlank() }?.let { appendLine("MIME: $it") }
                    act.launchContextHint?.takeIf { it.isNotBlank() }?.let { appendLine("Hint: $it") }
                    act.launchabilityReason?.takeIf { it.isNotBlank() }?.let { appendLine("Motivo: $it") }
                    if (risk.findings.isNotEmpty()) {
                        appendLine("Finding:")
                        risk.findings.forEach { finding ->
                            appendLine("- ${ActivityIntentRiskAnalyzer.formatLevel(finding.level)} • ${finding.title}: ${finding.detail}")
                        }
                    }
                    appendLine("Raccomandazione: ${risk.recommendation}")
                    appendLine()
                }
            }
        }
    }

    private fun recompute(state: ActivityListState): ActivityListState {
        val filtered = if (state.filter.isBlank()) {
            state.activities
        } else {
            state.activities.filter {
                it.label.contains(state.filter, ignoreCase = true) ||
                    it.appLabel.contains(state.filter, ignoreCase = true) ||
                    it.activityName.contains(state.filter, ignoreCase = true) ||
                    it.packageName.contains(state.filter, ignoreCase = true)
            }
        }
        val sorted = filtered.sortedWith(
            compareBy<ActivityEntry> {
                if (state.favoritePackages.contains(it.packageName) ||
                    state.favorites.contains(activityKey(it))
                ) 0 else 1
            }
                .thenBy { it.packageName }
                .thenBy { it.activityName }
        )
        val grouped = sorted.groupBy { it.packageName }
            .map { (pkg, acts) ->
                ActivityGroup(
                    packageName = pkg,
                    label = acts.firstOrNull()?.appLabel,
                    activities = acts
                )
            }
            .sortedBy { it.packageName }
        return state.copy(groups = grouped)
    }

    private fun recomputeAsync(state: ActivityListState) {
        _state.value = state
        recomputeJob?.cancel()
        recomputeJob = scope.launch {
            val newState = recompute(state)
            _state.value = newState
        }
    }

    private fun extForCurrentFormat(): String =
        if (runBlocking { getExportFormat() } == com.crackmydroid.shared.domain.model.ExportFormat.JSON) ".json" else ".txt"

    private fun normalizeQuery(value: String): String = value
        .trim()
        .replace(multiSpaceRegex, " ")
        .lowercase()

    private fun activityKey(entry: ActivityEntry): String = "${entry.packageName}/${entry.activityName}"

    private fun buildHtmlReport(report: ActivityAppReport): String = buildString {
        appendLine("<!DOCTYPE html>")
        appendLine("<html lang=\"it\">")
        appendLine("<head>")
        appendLine("  <meta charset=\"utf-8\">")
        appendLine("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">")
        appendLine("  <title>CrackMyDroid Activity Report - ${report.appLabel.htmlEscape()}</title>")
        appendLine("  <style>")
        appendLine("    :root { color-scheme: light; }")
        appendLine("    * { box-sizing: border-box; }")
        appendLine("    body { margin: 0; font-family: \"Segoe UI\", sans-serif; background: #f3f6fb; color: #162033; }")
        appendLine("    .wrap { max-width: 1180px; margin: 0 auto; padding: 32px 20px 40px; }")
        appendLine("    .hero { background: linear-gradient(135deg, #0f2747, #1a5fb4); color: #fff; padding: 28px; border-radius: 24px; box-shadow: 0 18px 42px rgba(15, 39, 71, 0.22); }")
        appendLine("    .eyebrow { text-transform: uppercase; letter-spacing: 0.12em; font-size: 12px; opacity: 0.78; }")
        appendLine("    h1 { margin: 10px 0 6px; font-size: 32px; }")
        appendLine("    .hero p { margin: 6px 0; font-size: 15px; opacity: 0.92; }")
        appendLine("    .stats { display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: 14px; margin: 22px 0; }")
        appendLine("    .stat { background: #fff; border-radius: 18px; padding: 18px; box-shadow: 0 12px 26px rgba(22, 32, 51, 0.08); }")
        appendLine("    .stat strong { display: block; font-size: 30px; color: #0f2747; }")
        appendLine("    .stat span { color: #5b667a; font-size: 13px; }")
        appendLine("    .grid { display: grid; grid-template-columns: minmax(0, 1fr); gap: 18px; }")
        appendLine("    .card { background: #fff; border-radius: 22px; padding: 22px; box-shadow: 0 12px 30px rgba(22, 32, 51, 0.08); }")
        appendLine("    .card h2 { margin: 0 0 14px; font-size: 20px; color: #0f2747; }")
        appendLine("    ul { margin: 0; padding-left: 20px; }")
        appendLine("    li { margin: 8px 0; line-height: 1.45; }")
        appendLine("    table { width: 100%; border-collapse: collapse; font-size: 14px; }")
        appendLine("    th, td { text-align: left; padding: 12px 10px; border-bottom: 1px solid #e4e8f0; vertical-align: top; }")
        appendLine("    th { color: #536078; font-size: 12px; text-transform: uppercase; letter-spacing: 0.08em; }")
        appendLine("    .tag { display: inline-block; padding: 5px 10px; border-radius: 999px; font-size: 12px; font-weight: 600; }")
        appendLine("    .tag-shell { background: #e8f7ee; color: #1f7a3e; }")
        appendLine("    .tag-guided { background: #fff1d8; color: #9a5b00; }")
        appendLine("    .tag-dump { background: #fde5e7; color: #a1283d; }")
        appendLine("    .muted { color: #637089; }")
        appendLine("    code { font-family: \"SFMono-Regular\", monospace; font-size: 12px; background: #f4f7fb; padding: 2px 6px; border-radius: 6px; }")
        appendLine("  </style>")
        appendLine("</head>")
        appendLine("<body>")
        appendLine("  <div class=\"wrap\">")
        appendLine("    <section class=\"hero\">")
        appendLine("      <div class=\"eyebrow\">CrackMyDroid desktop report</div>")
        appendLine("      <h1>${report.appLabel.htmlEscape()}</h1>")
        appendLine("      <p>Package analizzato: <code>${report.packageName.htmlEscape()}</code></p>")
        appendLine("      <p>Report generato dalla sezione Activity con dettaglio intent e indicazioni PenTest.</p>")
        appendLine("    </section>")
        appendLine("    <section class=\"stats\">")
        appendLine("      <div class=\"stat\"><strong>${report.totalActivities}</strong><span>Activity totali</span></div>")
        appendLine("      <div class=\"stat\"><strong>${report.shellLaunchableCount}</strong><span>Avviabili via shell</span></div>")
        appendLine("      <div class=\"stat\"><strong>${report.guidedLaunchCount}</strong><span>Guided launch</span></div>")
        appendLine("      <div class=\"stat\"><strong>${report.dumpOnlyCount}</strong><span>Solo dump</span></div>")
        appendLine("      <div class=\"stat\"><strong>${report.riskyActivitiesCount}</strong><span>Con rischio intent</span></div>")
        appendLine("    </section>")
        appendLine("    <div class=\"grid\">")
        appendLine("      <section class=\"card\">")
        appendLine("        <h2>Osservazioni chiave</h2>")
        appendLine("        <ul>")
        report.findings.forEach { finding ->
            appendLine("          <li>${finding.htmlEscape()}</li>")
        }
        appendLine("        </ul>")
        appendLine("      </section>")
        appendLine("      <section class=\"card\">")
        appendLine("        <h2>Profilo vulnerabilita</h2>")
        appendLine("        <p>Severita massima rilevata: <strong>${ActivityIntentRiskAnalyzer.formatLevel(report.highestRiskLevel)}</strong></p>")
        if (report.recommendations.isNotEmpty()) {
            appendLine("        <ul>")
            report.recommendations.forEach { recommendation ->
                appendLine("          <li>${recommendation.htmlEscape()}</li>")
            }
            appendLine("        </ul>")
        } else {
            appendLine("        <p class=\"muted\">Nessuna raccomandazione aggiuntiva oltre ai test di reachability.</p>")
        }
        appendLine("      </section>")
        appendLine("      <section class=\"card\">")
        appendLine("        <h2>Activity in evidenza</h2>")
        appendLine("        <table>")
        appendLine("          <thead><tr><th>Label</th><th>Component</th><th>Launch</th><th>Rischio</th><th>Hint</th></tr></thead>")
        appendLine("          <tbody>")
        report.highlights.forEach { activity ->
            appendLine("            <tr>")
            appendLine("              <td>${activity.entry.label.htmlEscape()}</td>")
            appendLine("              <td><code>${activity.entry.activityName.htmlEscape()}</code></td>")
            appendLine("              <td>${activity.launchModeBadge()}</td>")
            appendLine("              <td>${activity.riskBadge()}</td>")
            appendLine("              <td class=\"muted\">${activity.reportHint().htmlEscape()}</td>")
            appendLine("            </tr>")
        }
        appendLine("          </tbody>")
        appendLine("        </table>")
        appendLine("      </section>")
        appendLine("      <section class=\"card\">")
        appendLine("        <h2>Inventario completo</h2>")
        appendLine("        <table>")
        appendLine("          <thead><tr><th>#</th><th>Label</th><th>Activity</th><th>Modalita</th><th>Rischio</th><th>Dettagli</th></tr></thead>")
        appendLine("          <tbody>")
        _state.value.activities
            .filter { it.packageName == report.packageName }
            .map(ActivityIntentRiskAnalyzer::analyze)
            .sortedByDescending { it.score }
            .forEachIndexed { index, activity ->
                appendLine("            <tr>")
                appendLine("              <td>${index + 1}</td>")
                appendLine("              <td>${activity.entry.label.htmlEscape()}</td>")
                appendLine("              <td><code>${activity.entry.activityName.htmlEscape()}</code></td>")
                appendLine("              <td>${activity.launchModeBadge()}</td>")
                appendLine("              <td>${activity.riskBadge()}</td>")
                appendLine("              <td class=\"muted\">${activity.reportHint().htmlEscape()}</td>")
                appendLine("            </tr>")
            }
        appendLine("          </tbody>")
        appendLine("        </table>")
        appendLine("      </section>")
        appendLine("    </div>")
        appendLine("  </div>")
        appendLine("</body>")
        appendLine("</html>")
    }

    private fun ActivityEntry.hasGuidedLaunchMetadata(): Boolean =
        !launchContextHint.isNullOrBlank() ||
            !launchIntentAction.isNullOrBlank() ||
            !launchIntentData.isNullOrBlank() ||
            !launchIntentMimeType.isNullOrBlank()

    private fun ActivityEntry.reportHint(): String =
        launchabilityReason
            ?: launchContextHint
            ?: buildList {
                launchIntentAction?.takeIf { it.isNotBlank() }?.let { add("action=$it") }
                launchIntentData?.takeIf { it.isNotBlank() }?.let { add("data=$it") }
                launchIntentMimeType?.takeIf { it.isNotBlank() }?.let { add("mime=$it") }
            }.joinToString(" • ")
                .ifBlank { "Nessun dettaglio aggiuntivo" }

    private fun ActivityEntry.launchModeBadge(): String = when {
        !launchableViaShell && hasGuidedLaunchMetadata() -> "<span class=\"tag tag-guided\">Guided launch</span>"
        !launchableViaShell -> "<span class=\"tag tag-dump\">Solo dump</span>"
        hasGuidedLaunchMetadata() -> "<span class=\"tag tag-guided\">Shell + hint</span>"
        else -> "<span class=\"tag tag-shell\">Shell</span>"
    }

    private fun ActivityIntentRiskReport.launchModeBadge(): String = entry.launchModeBadge()

    private fun ActivityIntentRiskReport.reportHint(): String = buildList {
        if (findings.isNotEmpty()) {
            add(findings.first().detail)
        }
        entry.launchContextHint?.takeIf { it.isNotBlank() }?.let { add(it) }
        entry.launchabilityReason?.takeIf { it.isNotBlank() }?.let { add(it) }
        entry.launchIntentAction?.takeIf { it.isNotBlank() }?.let { add("action=$it") }
        entry.launchIntentData?.takeIf { it.isNotBlank() }?.let { add("data=$it") }
        entry.launchIntentMimeType?.takeIf { it.isNotBlank() }?.let { add("mime=$it") }
    }.joinToString(" • ").ifBlank { "Nessun dettaglio aggiuntivo" }

    private fun ActivityIntentRiskReport.riskBadge(): String = when (level) {
        ActivityIntentRiskLevel.Critical -> "<span class=\"tag tag-dump\">Critica</span>"
        ActivityIntentRiskLevel.High -> "<span class=\"tag tag-guided\">Alta</span>"
        ActivityIntentRiskLevel.Medium -> "<span class=\"tag tag-shell\">Media</span>"
        ActivityIntentRiskLevel.Low -> "<span class=\"tag tag-shell\">Bassa</span>"
    }

    private fun ActivityEntry.toExportPayload(): ActivityExportPayload {
        val risk = ActivityIntentRiskAnalyzer.analyze(this)
        return ActivityExportPayload(
            label = label,
            appLabel = appLabel,
            packageName = packageName,
            activityName = activityName,
            launchableViaShell = launchableViaShell,
            launchabilityReason = launchabilityReason,
            launchContextHint = launchContextHint,
            launchIntentAction = launchIntentAction,
            launchIntentData = launchIntentData,
            launchIntentMimeType = launchIntentMimeType,
            launchMode = risk.launchMode,
            riskLevel = ActivityIntentRiskAnalyzer.formatLevel(risk.level),
            riskScore = risk.score,
            recommendation = risk.recommendation,
            findings = risk.findings.map { finding ->
                ActivityRiskFindingPayload(
                    level = ActivityIntentRiskAnalyzer.formatLevel(finding.level),
                    title = finding.title,
                    detail = finding.detail
                )
            }
        )
    }

    private fun String.htmlEscape(): String = buildString(length) {
        this@htmlEscape.forEach { char ->
            append(
                when (char) {
                    '&' -> "&amp;"
                    '<' -> "&lt;"
                    '>' -> "&gt;"
                    '"' -> "&quot;"
                    '\'' -> "&#39;"
                    else -> char.toString()
                }
            )
        }
    }

    @Serializable
    private data class ActivityExportPayload(
        val label: String,
        val appLabel: String,
        val packageName: String,
        val activityName: String,
        val launchableViaShell: Boolean,
        val launchabilityReason: String?,
        val launchContextHint: String?,
        val launchIntentAction: String?,
        val launchIntentData: String?,
        val launchIntentMimeType: String?,
        val launchMode: String,
        val riskLevel: String,
        val riskScore: Int,
        val recommendation: String,
        val findings: List<ActivityRiskFindingPayload>
    )

    @Serializable
    private data class ActivityRiskFindingPayload(
        val level: String,
        val title: String,
        val detail: String
    )
}
