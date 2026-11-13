package com.crackmydroid.shared.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.crackmydroid.ui.components.AppAccordion
import com.crackmydroid.ui.preview.AppComponentPreviews
import com.crackmydroid.ui.preview.ComponentPreviewContainer

@Composable
fun SearchPanelComponent(
    title: String,
    subtitle: String? = null,
    meta: String? = null,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
    onSubmit: (String) -> Unit,
    label: String,
    historyKey: String,
    focusRequestId: Long = 0,
    showClearAction: Boolean = false,
    onCleared: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {},
) {
    AppAccordion(
        title = title,
        subtitle = subtitle,
        meta = meta,
        expanded = expanded,
        onToggle = { onExpandedChange(!expanded) },
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SearchFieldWithHistoryComponent(
                value = query,
                onValueChange = onQueryChange,
                onSubmit = onSubmit,
                label = label,
                historyKey = historyKey,
                showClearAction = showClearAction,
                onCleared = onCleared,
                focusRequestId = focusRequestId,
            )
            content()
        }
    }
}

@AppComponentPreviews
@Composable
private fun SearchPanelComponentPreview() = ComponentPreviewContainer {
    SearchPanelComponent(
        title = "Search",
        subtitle = "Search and filters",
        expanded = true,
        onExpandedChange = {},
        query = "crack",
        onQueryChange = {},
        onSubmit = {},
        label = "Search",
        historyKey = "search-panel-preview",
    )
}
