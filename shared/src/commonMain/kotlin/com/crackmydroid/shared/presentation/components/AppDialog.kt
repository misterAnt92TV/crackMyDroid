package com.crackmydroid.shared.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.crackmydroid.shared.presentation.components.text.AppText

@Composable
fun AppDialog(
    title: String,
    onDismiss: () -> Unit,
    confirmLabel: String,
    onConfirm: () -> Unit,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    dismissLabel: String? = null,
    onDismissAction: (() -> Unit)? = null,
    confirmEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { AppText(title, fontWeight = FontWeight.SemiBold) },
        text = content,
        confirmButton = {
            AppButton(confirmLabel, onConfirm, enabled = confirmEnabled, fullWidth = false)
        },
        dismissButton = if (dismissLabel != null && onDismissAction != null) {
            {
                AppButton(
                    dismissLabel,
                    onDismissAction,
                    variant = ButtonVariant.Tonal,
                    fullWidth = false,
                )
            }
        } else null,
    )
}
