package com.crackmydroid.shared.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.crackmydroid.shared.i18n.LocalStrings
import com.crackmydroid.shared.presentation.components.text.AppText
import com.crackmydroid.shared.presentation.preview.AppComponentPreviews
import com.crackmydroid.shared.presentation.preview.ComponentPreviewContainer

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchFieldWithHistoryComponent(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: (String) -> Unit,
    label: String,
    historyKey: String,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(14.dp),
    showClearAction: Boolean = false,
    onCleared: (() -> Unit)? = null
) {
    var historyVersion by remember { mutableStateOf(0) }
    var hasFocus by remember { mutableStateOf(false) }
    var showSuggestions by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val strings = LocalStrings.current
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
            label = { AppText(label) },
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
                            contentDescription = strings.clearSearchContentDescription,
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

        val preferences = LocalAccessibilityPrefs.current
        val suggestionsContent: @Composable () -> Unit = {
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
                                },
                                onLongClickLabel = strings.removeSearchHistoryContentDescription.format(suggestion)
                            ),
                            label = { AppText(suggestion) },
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
                    AppText(
                        text = strings.clearSearchHistoryContentDescription,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .semantics {
                                role = androidx.compose.ui.semantics.Role.Button
                                contentDescription = strings.clearSearchHistoryContentDescription
                            }
                            .combinedClickable(
                                onClick = { clearHistoryAndRefresh() },
                                onLongClick = { clearHistoryAndRefresh() }
                            )
                    )
                }
            }
        }
        if (preferences.reduceMotion) {
            if (suggestionsVisible) suggestionsContent()
        } else {
            AnimatedVisibility(
                visible = suggestionsVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) { suggestionsContent() }
        }
    }
}

@AppComponentPreviews
@Composable
private fun SearchFieldWithHistoryComponentPreview() = ComponentPreviewContainer {
    val strings = LocalStrings.current
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SearchFieldWithHistoryComponent(
            value = "",
            onValueChange = {},
            onSubmit = {},
            label = strings.permissionsSearch,
            historyKey = "component-preview-empty"
        )
        SearchFieldWithHistoryComponent(
            value = "crack",
            onValueChange = {},
            onSubmit = {},
            label = strings.permissionsSearch,
            historyKey = "component-preview-filled",
            showClearAction = true,
            onCleared = {}
        )
    }
}
