package com.crackmydroid.shared.presentation.components.text

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.crackmydroid.shared.presentation.components.AccessibilityPrefs
import com.crackmydroid.shared.presentation.components.LocalAccessibilityPrefs
import com.crackmydroid.shared.presentation.preview.AppComponentPreviews
import com.crackmydroid.shared.presentation.preview.ComponentPreviewContainer

@Composable
fun AppText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    fontWeight: FontWeight? = null,
    color: androidx.compose.ui.graphics.Color? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    val prefs = LocalAccessibilityPrefs.current
    val withWeight = if (fontWeight != null) style.copy(fontWeight = fontWeight) else style
    val sized =
        if (prefs.largeText) withWeight.copy(fontSize = withWeight.fontSize * 1.1f) else withWeight
    val finalColor = color
        ?: if (prefs.highContrast) MaterialTheme.colorScheme.onBackground else androidx.compose.material3.LocalContentColor.current
    Text(
        text,
        modifier = modifier,
        style = sized,
        color = finalColor,
        maxLines = maxLines,
        overflow = overflow,
    )
}

@AppComponentPreviews
@Composable
private fun AppTextPreview() = ComponentPreviewContainer {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AppText("Titolo della sezione", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        AppText("Descrizione leggibile del risultato dell'analisi.")
        AppText("Avviso che richiede attenzione", color = MaterialTheme.colorScheme.error)
    }
}

@AppComponentPreviews
@Composable
private fun AppTextAccessibilityPreview() = ComponentPreviewContainer(
    accessibilityPrefs = AccessibilityPrefs(highContrast = true, largeText = true),
) {
    AppText("Testo accessibile ad alto contrasto e dimensione aumentata.")
}
