package com.crackmydroid.shared.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import com.crackmydroid.shared.presentation.preview.AppPreviewEnvironment
import com.crackmydroid.shared.presentation.preview.AppScreenPreviews

@AppScreenPreviews
@Composable
private fun MoreScreenPreview() {
    AppPreviewEnvironment {
        MoreScreen(
            padding = PaddingValues(),
            onNavigate = {},
            headerHelp = "Apri rapidamente gli strumenti secondari.",
            onHelp = {},
        )
    }
}
