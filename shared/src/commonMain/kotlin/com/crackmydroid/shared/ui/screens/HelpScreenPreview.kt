package com.crackmydroid.shared.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.crackmydroid.shared.i18n.LocalStrings
import com.crackmydroid.shared.i18n.stringsFor
import com.crackmydroid.shared.presentation.preview.AppScreenPreviews
import com.crackmydroid.shared.presentation.theme.CrackMyDroidTheme

@AppScreenPreviews
@Composable
private fun HelpScreenPreview() {
    CrackMyDroidTheme {
        CompositionLocalProvider(LocalStrings provides stringsFor("it")) {
            HelpScreen(
                padding = PaddingValues(),
                onHelp = {},
                onNavigate = {}
            )
        }
    }
}
