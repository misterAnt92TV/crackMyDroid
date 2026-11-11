package com.crackmydroid.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.crackmydroid.ui.components.text.AppText
import com.crackmydroid.ui.preview.AppComponentPreviews
import com.crackmydroid.ui.preview.ComponentPreviewContainer

@Composable
fun AppProgressIndicator(label: String, modifier: Modifier = Modifier) {
    if (LocalAccessibilityPrefs.current.reduceMotion) {
        AppText(label, modifier = modifier)
    } else {
        LinearProgressIndicator(modifier = modifier.fillMaxWidth())
    }
}

@AppComponentPreviews
@Composable
private fun AppProgressIndicatorPreview() = ComponentPreviewContainer {
    AppProgressIndicator("Analisi in corso: controllo permessi e configurazione applicazioni")
}
