package com.crackmydroid.shared.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.crackmydroid.shared.i18n.LocalStrings
import com.crackmydroid.shared.presentation.preview.AppComponentPreviews
import com.crackmydroid.shared.presentation.preview.ComponentPreviewContainer
import com.crackmydroid.shared.presentation.components.text.AppText

@Composable
fun ErrorDialogComponent(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss) { AppText(LocalStrings.current.confirm) }
        },
        text = { AppText(message) }
    )
}

@AppComponentPreviews
@Composable
private fun ErrorDialogComponentPreview() = ComponentPreviewContainer {
    ErrorDialogComponent(
        message = "Non è stato possibile completare l'operazione. Riprova tra qualche istante.",
        onDismiss = {}
    )
}
