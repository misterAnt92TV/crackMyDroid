package com.crackmydroid.shared.presentation.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.crackmydroid.shared.presentation.components.text.AppText
import com.crackmydroid.shared.presentation.preview.AppComponentPreviews
import com.crackmydroid.shared.presentation.preview.ComponentPreviewContainer


@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: ButtonVariant = ButtonVariant.Primary,
    leadingIcon: ImageVector? = null,
    fullWidth: Boolean = true,
) {
    val prefs = LocalAccessibilityPrefs.current
    val baseStyle = MaterialTheme.typography.labelLarge
    val finalStyle =
        if (prefs.largeText) baseStyle.copy(fontSize = baseStyle.fontSize * 1.1f) else baseStyle
    val content = @Composable {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
        }
        AppText(
            text,
            style = finalStyle,
            maxLines = if (prefs.largeText) 2 else 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
    val widthModifier = if (fullWidth) Modifier.fillMaxWidth() else Modifier
    val buttonModifier = modifier
        .then(widthModifier)
        .padding(vertical = 4.dp)
        .defaultMinSize(minHeight = if (prefs.largeText) 58.dp else 0.dp)
    when (variant) {
        ButtonVariant.Primary -> Button(
            onClick = onClick,
            enabled = enabled,
            modifier = buttonModifier,
            contentPadding = PaddingValues(
                horizontal = 18.dp,
                vertical = if (prefs.largeText) 16.dp else 14.dp,
            ),
        ) { content() }

        ButtonVariant.Secondary -> OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            modifier = buttonModifier,
            contentPadding = PaddingValues(
                horizontal = 18.dp,
                vertical = if (prefs.largeText) 16.dp else 14.dp,
            ),
        ) { content() }

        ButtonVariant.Tonal -> FilledTonalButton(
            onClick = onClick,
            enabled = enabled,
            modifier = buttonModifier,
            contentPadding = PaddingValues(
                horizontal = 18.dp,
                vertical = if (prefs.largeText) 16.dp else 14.dp,
            ),
        ) { content() }
    }
}

@AppComponentPreviews
@Composable
private fun AppButtonPreview() = ComponentPreviewContainer {
    androidx.compose.foundation.layout.Column(
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
    ) {
        AppButton("Analizza dispositivo", onClick = {}, leadingIcon = Icons.Filled.Info)
        AppButton("Azione secondaria", onClick = {}, variant = ButtonVariant.Secondary)
        AppButton("Azione tonale", onClick = {}, variant = ButtonVariant.Tonal, fullWidth = false)
        AppButton("Azione non disponibile", onClick = {}, enabled = false)
    }
}
