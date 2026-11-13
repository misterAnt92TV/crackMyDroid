package com.crackmydroid.shared.ui.home.view

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import com.crackmydroid.shared.domain.model.operation.OperationEntry
import com.crackmydroid.shared.i18n.stringsFor
import com.crackmydroid.shared.presentation.preview.AppPreviewEnvironment
import com.crackmydroid.shared.presentation.preview.AppScreenPreviews
import com.crackmydroid.shared.ui.home.model.HomeState
import com.crackmydroid.shared.ui.home.model.HomeUiStateMapper

@AppScreenPreviews
@Composable
private fun HomeScreenPreview() {
    val strings = stringsFor("it")
    AppPreviewEnvironment {
        HomeContent(
            state = HomeUiStateMapper().map(
                HomeState(
                    recentOperations = listOf(
                        OperationEntry("Scansione APK completata", true, "42 applicazioni", 0L),
                        OperationEntry("Avvio activity", false, "Activity non esportata", 0L),
                    ),
                ),
                strings,
                "it",
            ),
            padding = PaddingValues(),
            onRemove = {},
        )
    }
}

@AppScreenPreviews
@Composable
private fun HomeScreenEmptyHistoryPreview() {
    val strings = stringsFor("it")
    AppPreviewEnvironment {
        HomeContent(
            state = HomeUiStateMapper().map(HomeState(), strings, "it"),
            padding = PaddingValues(),
            onRemove = {},
            initiallyExpandedHistory = true,
        )
    }
}
