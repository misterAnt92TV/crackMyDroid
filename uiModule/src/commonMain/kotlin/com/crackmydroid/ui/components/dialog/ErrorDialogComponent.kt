package com.crackmydroid.ui.components.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import com.crackmydroid.ui.strings.LocalUiStrings
import com.crackmydroid.ui.components.text.AppText
import com.crackmydroid.ui.preview.AppComponentPreviews
import com.crackmydroid.ui.preview.ComponentPreviewContainer

@Composable
fun ErrorDialogComponent(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss) { AppText(LocalUiStrings.current.confirm) }
        },
        text = { AppText(message) },
    )
}

@AppComponentPreviews
@Composable
private fun ErrorDialogComponentPreview() = ComponentPreviewContainer {
    ErrorDialogComponent(
        message = "Non è stato possibile completare l'operazione. Riprova tra qualche istante.",
        onDismiss = {},
    )
}
