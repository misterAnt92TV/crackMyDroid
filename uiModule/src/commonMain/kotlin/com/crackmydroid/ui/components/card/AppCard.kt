package com.crackmydroid.ui.components.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.crackmydroid.ui.components.text.AppText
import com.crackmydroid.ui.preview.AppComponentPreviews
import com.crackmydroid.ui.preview.ComponentPreviewContainer

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    if (onClick != null) {
        ElevatedCard(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier,
        ) {
            Column(modifier = Modifier.padding(contentPadding), content = content)
        }
    } else {
        ElevatedCard(
            modifier = modifier,
        ) {
            Column(modifier = Modifier.padding(contentPadding), content = content)
        }
    }
}

@AppComponentPreviews
@Composable
private fun AppCardPreview() = ComponentPreviewContainer {
    AppCard(modifier = Modifier.fillMaxWidth(), onClick = {}) {
        AppText("Risultato dell'analisi", style = MaterialTheme.typography.titleMedium)
        AppText("Tocca la card per visualizzare i dettagli.")
    }
}
