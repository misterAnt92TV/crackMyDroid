package com.crackmydroid.shared.presentation.components.text

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.crackmydroid.shared.presentation.components.LocalAccessibilityPrefs

@Composable
fun AppText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    fontWeight: FontWeight? = null,
    color: androidx.compose.ui.graphics.Color? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
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
        overflow = overflow
    )
}