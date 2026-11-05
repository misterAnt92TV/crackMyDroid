package com.crackmydroid.shared.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.crackmydroid.shared.domain.model.operation.OperationEntry
import com.crackmydroid.shared.i18n.LocalStrings
import com.crackmydroid.shared.i18n.stringsFor
import com.crackmydroid.shared.presentation.preview.AppScreenPreviews
import com.crackmydroid.shared.presentation.theme.CrackMyDroidTheme

@AppScreenPreviews
@Composable
private fun HomeScreenPreview() {
    CrackMyDroidTheme {
        CompositionLocalProvider(LocalStrings provides stringsFor("it")) {
            HomeContent(
                history = listOf(
                    OperationEntry("Scansione APK completata", true, "42 applicazioni", 0L),
                    OperationEntry("Avvio activity", false, "Activity non esportata", 0L)
                ),
                padding = PaddingValues(),
                headerHelp = "Panoramica delle funzioni principali.",
                onHelp = {},
                onRemove = {},
                languageCode = "it"
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
                history = emptyList(),
                padding = PaddingValues(),
                headerHelp = "Panoramica delle funzioni principali.",
                onHelp = {},
                onRemove = {},
                initiallyExpandedHistory = true,
                languageCode = "it"
            )
        }
    }
}
