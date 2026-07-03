package com.crackmydroid.shared.ui.activities

import com.crackmydroid.shared.domain.model.ActivityEntry
import com.crackmydroid.shared.domain.usecase.GetActivitiesUseCase
import com.crackmydroid.shared.domain.usecase.LaunchActivityUseCase
import com.crackmydroid.shared.domain.usecase.GetExportFormatUseCase
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

class ActivityListViewModel(
    private val getActivities: GetActivitiesUseCase,
    private val launchActivity: LaunchActivityUseCase,
    private val getExportFormat: GetExportFormatUseCase
) : BaseViewModel() {
    private val _state = MutableStateFlow(ActivityListState())
    val state: StateFlow<ActivityListState> = _state
    private val queryCount = mutableMapOf<String, Int>()
    private var recomputeJob: Job? = null
    private val multiSpaceRegex = Regex("\\s+")

    fun refresh() {
        scope.launch {
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
        val normalizedAction = action?.trim().orEmpty().ifBlank { null }
        val normalizedData = dataUri?.trim().orEmpty().ifBlank { null }
        val normalizedMimeType = mimeType?.trim().orEmpty().ifBlank { null }
        val hasGuidedIntent = normalizedAction != null || normalizedData != null || normalizedMimeType != null
        if (!entry.launchableViaShell && !hasGuidedIntent) {
            val msg = entry.launchabilityReason ?: "Activity non avviabile via am start -n"
            _state.update { it.copy(error = msg) }
            OperationLogStore.record("Lancio ${entry.activityName}", success = false, details = msg)
            return
        }
        scope.launch {
            val result = launchActivity(entry, normalizedAction, normalizedData, normalizedMimeType)
            result.onFailure { err ->
                val msg = err.message ?: "Impossibile avviare ${entry.activityName}"
                _state.update { it.copy(error = msg) }
                OperationLogStore.record("Lancio ${entry.activityName}", success = false, details = msg)
            }.onSuccess {
                OperationLogStore.record("Lancio ${entry.activityName}", success = true)
            }
        }
    }

    fun groups(): List<ActivityGroup> = _state.value.groups

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun toggleFavorite(entry: ActivityEntry) {
        val key = entry.packageName + entry.activityName
        val favs = _state.value.favorites.toMutableSet()
        if (favs.contains(key)) favs.remove(key) else favs.add(key)
        recomputeAsync(_state.value.copy(favorites = favs))
    }

    fun toggleFavoritePackage(packageName: String) {
        val favs = _state.value.favoritePackages.toMutableSet()
        if (favs.contains(packageName)) favs.remove(packageName) else favs.add(packageName)
        recomputeAsync(_state.value.copy(favoritePackages = favs))
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

    private fun buildExportContent(list: List<ActivityEntry>): String {
        val format = runBlocking { getExportFormat() }
        return if (format == com.crackmydroid.shared.domain.model.ExportFormat.JSON) {
            buildString {
                appendLine("[")
                list.forEachIndexed { idx, act ->
                    append("  {\"label\":\"${act.label}\",\"package\":\"${act.packageName}\",\"activity\":\"${act.activityName}\"}")
                    if (idx != list.lastIndex) append(",")
                    appendLine()
                }
                append("]")
            }
        } else {
            buildString {
                list.forEach { act ->
                    appendLine("${act.label} (${act.packageName})")
                    appendLine(act.activityName)
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
                    state.favorites.contains(it.packageName + it.activityName)
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
}
