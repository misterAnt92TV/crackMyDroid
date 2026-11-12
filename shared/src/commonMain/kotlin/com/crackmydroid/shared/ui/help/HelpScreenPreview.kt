package com.crackmydroid.shared.ui.help

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import com.crackmydroid.shared.presentation.preview.AppPreviewEnvironment
import com.crackmydroid.shared.presentation.preview.AppScreenPreviews

@AppScreenPreviews
@Composable
private fun HelpScreenPreview() {
    AppPreviewEnvironment {
        HelpScreen(
            padding = PaddingValues(),
            onNavigate = {},
        )
    }
}
