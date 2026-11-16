package com.crackmydroid.shared.ui.history.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.crackmydroid.shared.i18n.LocalStrings
import com.crackmydroid.shared.i18n.stringsFor
import com.crackmydroid.shared.domain.model.operation.OperationEntry
import com.crackmydroid.shared.presentation.preview.AppPreviewEnvironment
import com.crackmydroid.shared.presentation.preview.AppScreenPreviews
import com.crackmydroid.shared.ui.home.HomeEvent
import com.crackmydroid.shared.ui.home.HomeUiState
import com.crackmydroid.shared.ui.home.HomeViewModel
import com.crackmydroid.shared.ui.home.model.HomeUiStateMapper
import com.crackmydroid.shared.ui.home.view.LastOperationItemComponent
import com.crackmydroid.shared.ui.screens.screenContentPadding
import com.crackmydroid.ui.components.card.AppCard
import com.crackmydroid.ui.components.text.AppText
import com.crackmydroid.ui.theme.CrackMyDroidTheme

@Composable
fun HistoryScreen(viewModel: HomeViewModel, padding: PaddingValues, languageCode: String) {
    val state by viewModel.state.collectAsState()
    val strings = LocalStrings.current
    HistoryContent(
        state = HomeUiStateMapper().map(state, strings, languageCode),
        padding = padding,
        onRemove = { viewModel.emitEvent(HomeEvent.Remove(it)) },
    )
}

@Composable
internal fun HistoryContent(state: HomeUiState, padding: PaddingValues, onRemove: (String) -> Unit) {
    val strings = LocalStrings.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = screenContentPadding(padding),
        verticalArrangement = Arrangement.spacedBy(CrackMyDroidTheme.spacing.itemSpacing),
    ) {
        if (state.operationHistory.isEmpty()) {
            item {
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    AppText(strings.homeLatestOperationsEmpty)
                }
            }
        } else {
            items(state.operationHistory, key = { it.operationId }) { operation ->
                LastOperationItemComponent(
                    item = operation,
                    actionContentDescription = strings.removeOperationContentDescription,
                    onAction = onRemove,
                )
            }
        }
    }
}

@AppScreenPreviews
@Composable
private fun HistoryScreenPreview() = AppPreviewEnvironment {
    HistoryContent(
        state = HomeUiStateMapper().map(
            com.crackmydroid.shared.ui.home.model.HomeState(
                operationHistory = listOf(OperationEntry("Scansione APK completata", true, "42 applicazioni", 0L)),
            ),
            stringsFor("it"),
            "it",
        ),
        padding = PaddingValues(),
        onRemove = {},
    )
}
