package com.crackmydroid.shared.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

private const val MAX_HISTORY = 8
private const val MAX_VISIBLE_SUGGESTIONS = 6

object SearchHistoryStore {
    private val histories = mutableMapOf<String, ArrayDeque<String>>()
    private var enabled: Boolean = true
    private val multiSpaceRegex = Regex("\\s+")

    fun setEnabled(value: Boolean) {
        enabled = value
    }

    fun add(key: String, query: String) {
        if (!enabled) return
        val normalized = normalizeQuery(query)
        if (normalized.isEmpty()) return
        val deque = histories.getOrPut(key) { ArrayDeque() }
        val existing = deque.firstOrNull { it.equals(normalized, ignoreCase = true) }
        if (existing != null) {
            deque.remove(existing)
        }
        deque.addFirst(normalized)
        while (deque.size > MAX_HISTORY) deque.removeLast()
    }

    fun remove(key: String, query: String) {
        val normalized = normalizeQuery(query)
        histories[key]?.removeAll { it.equals(normalized, ignoreCase = true) }
    }

    fun clear(key: String) {
        histories[key]?.clear()
    }

    fun suggestions(key: String, prefix: String = ""): List<String> {
        if (!enabled) return emptyList()
        val deque = histories[key] ?: return emptyList()
        val normalizedPrefix = normalizeQuery(prefix)
        if (normalizedPrefix.isBlank()) {
            return deque.take(MAX_VISIBLE_SUGGESTIONS)
        }
        return deque.asSequence()
            .filter { it.contains(normalizedPrefix, ignoreCase = true) }
            .filterNot { it.equals(normalizedPrefix, ignoreCase = true) }
            .sortedBy { if (it.startsWith(normalizedPrefix, ignoreCase = true)) 0 else 1 }
            .take(MAX_VISIBLE_SUGGESTIONS)
            .toList()
    }

    fun clearAll() {
        histories.clear()
    }

    private fun normalizeQuery(query: String): String = query
        .trim()
        .replace(multiSpaceRegex, " ")
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchFieldWithHistory(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: (String) -> Unit,
    label: String,
    historyKey: String,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    showClearAction: Boolean = false,
    onCleared: (() -> Unit)? = null
) {
    var historyVersion by remember { mutableStateOf(0) }
    var hasFocus by remember { mutableStateOf(false) }
    var showSuggestions by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val suggestions = remember(value, historyKey, historyVersion) {
        SearchHistoryStore.suggestions(historyKey, value)
    }
    val suggestionsVisible = showSuggestions && suggestions.isNotEmpty()

    LaunchedEffect(hasFocus) {
        if (hasFocus) {
            showSuggestions = true
        } else {
            // Keep chips visible for a very short time so chip taps are not lost on blur.
            delay(120)
            showSuggestions = false
        }
    }

    fun addToHistoryAndRefresh(query: String) {
        SearchHistoryStore.add(historyKey, query)
        historyVersion++
    }

    fun removeFromHistoryAndRefresh(query: String) {
        SearchHistoryStore.remove(historyKey, query)
        historyVersion++
    }

    fun clearHistoryAndRefresh() {
        SearchHistoryStore.clear(historyKey)
        historyVersion++
        onCleared?.invoke()
    }

    Column(modifier = modifier) {
        TextField(
            value = value,
            onValueChange = {
                onValueChange(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { state ->
                    hasFocus = state.isFocused
                },
            label = { Text(label) },
            shape = shape,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            trailingIcon = {
                if (value.isNotBlank()) {
                    IconButton(onClick = {
                        onValueChange("")
                        showSuggestions = hasFocus
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            keyboardActions = KeyboardActions(
                onSearch = {
                    addToHistoryAndRefresh(value)
                    onSubmit(value)
                    focusManager.clearFocus()
                    showSuggestions = false
                }
            )
        )

        AnimatedVisibility(
            visible = suggestionsVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(suggestions) { suggestion ->
                        val chipColors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        )
                        FilterChip(
                            selected = value.equals(suggestion, ignoreCase = true),
                            onClick = {},
                            modifier = Modifier.combinedClickable(
                                onClick = {
                                    addToHistoryAndRefresh(suggestion)
                                    onValueChange(suggestion)
                                    onSubmit(suggestion)
                                    focusManager.clearFocus()
                                    showSuggestions = false
                                },
                                onLongClick = {
                                    removeFromHistoryAndRefresh(suggestion)
                                }
                            ),
                            label = { Text(suggestion) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            colors = chipColors
                        )
                    }
                }
                if (showClearAction) {
                    Text(
                        text = "Svuota suggerimenti",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .combinedClickable(
                                onClick = { clearHistoryAndRefresh() },
                                onLongClick = { clearHistoryAndRefresh() }
                            )
                    )
                }
            }
        }
    }
}
