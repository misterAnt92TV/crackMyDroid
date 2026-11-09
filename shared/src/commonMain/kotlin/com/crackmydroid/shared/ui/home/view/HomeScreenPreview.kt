package com.crackmydroid.shared.ui.home.view

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.crackmydroid.shared.domain.model.operation.OperationEntry
import com.crackmydroid.shared.i18n.LocalStrings
import com.crackmydroid.shared.i18n.stringsFor
import com.crackmydroid.shared.presentation.preview.AppScreenPreviews
import com.crackmydroid.shared.presentation.theme.CrackMyDroidTheme
import com.crackmydroid.shared.ui.home.model.HomeState
import com.crackmydroid.shared.ui.home.model.HomeUiStateMapper

@AppScreenPreviews
@Composable
private fun HomeScreenPreview() {
    CrackMyDroidTheme {
        CompositionLocalProvider(LocalStrings provides stringsFor("it")) {
            HomeContent(
                state = HomeUiStateMapper().map(
                    HomeState(
                        recentOperations = listOf(
                            OperationEntry("Scansione APK completata", true, "42 applicazioni", 0L),
                            OperationEntry("Avvio activity", false, "Activity non esportata", 0L)
                        )
                    ),
                    stringsFor("it"),
                    "it"
                ),
                padding = PaddingValues(),
                headerHelp = "Panoramica delle funzioni principali.",
                onHelp = {},
                onRemove = {},
            )
        }
    }
}

@AppScreenPreviews
@Composable
private fun HomeScreenEmptyHistoryPreview() {
    CrackMyDroidTheme {
        CompositionLocalProvider(LocalStrings provides stringsFor("it")) {
            HomeContent(
                state = HomeUiStateMapper().map(HomeState(), stringsFor("it"), "it"),
                padding = PaddingValues(),
                headerHelp = "Panoramica delle funzioni principali.",
                onHelp = {},
                onRemove = {},
                initiallyExpandedHistory = true
            )
        }
    }
}
