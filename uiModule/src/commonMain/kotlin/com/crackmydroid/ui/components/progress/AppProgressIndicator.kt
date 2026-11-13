package com.crackmydroid.ui.components.progress

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.crackmydroid.ui.preview.AppComponentPreviews
import com.crackmydroid.ui.preview.ComponentPreviewContainer

/**
 * Backward-compatible wrapper that delegates to [AppProgressBar].
 */
@Composable
fun AppProgressIndicator(label: String, modifier: Modifier = Modifier) {
    AppProgressBar(
        label = label,
        modifier = modifier,
    )
}

@AppComponentPreviews
@Composable
private fun AppProgressIndicatorPreview() = ComponentPreviewContainer {
    AppProgressIndicator("Analisi in corso: controllo permessi e configurazione applicazioni")
}
