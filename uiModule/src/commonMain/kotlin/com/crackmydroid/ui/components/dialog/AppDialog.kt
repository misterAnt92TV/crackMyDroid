package com.crackmydroid.ui.components.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.crackmydroid.ui.components.button.AppButton
import com.crackmydroid.ui.components.button.ButtonVariant
import com.crackmydroid.ui.components.text.AppText
import com.crackmydroid.ui.preview.AppComponentPreviews
import com.crackmydroid.ui.preview.ComponentPreviewContainer


@Composable
fun AppDialog(
    title: String,
    onDismiss: () -> Unit,
    confirmLabel: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    dismissLabel: String? = null,
    onDismissAction: (() -> Unit)? = null,
    confirmEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = {
            AppText(
                title,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.semantics { heading() },
            )
        },
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
        } else {
            null
        },
    )
}

@AppComponentPreviews
@Composable
private fun AppDialogPreview() = ComponentPreviewContainer {
    AppDialog(
        title = "Conferma scansione sicurezza",
        confirmLabel = "Avvia scansione",
        dismissLabel = "Annulla",
        onDismiss = {},
        onConfirm = {},
        onDismissAction = {},
    ) {
        AppText("La scansione analizzerà i controlli disponibili sul dispositivo selezionato.")
    }
}
