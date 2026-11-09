package com.crackmydroid.shared.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.crackmydroid.shared.presentation.components.text.AppText

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    singleLine: Boolean = false,
    error: String? = null
) {
    val prefs = LocalAccessibilityPrefs.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { AppText(label) },
        enabled = enabled,
        singleLine = singleLine,
        isError = error != null,
        supportingText = error?.let { { AppText(it) } },
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = if (prefs.largeText) 64.dp else 56.dp)
            .semantics { error?.let { this.error(it) } }
    )
}
