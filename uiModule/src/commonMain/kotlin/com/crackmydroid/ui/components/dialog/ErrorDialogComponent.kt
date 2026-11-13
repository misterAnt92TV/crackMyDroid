package com.crackmydroid.ui.components.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import com.crackmydroid.ui.components.button.AppButton
import com.crackmydroid.ui.components.text.AppText
import com.crackmydroid.ui.preview.AppComponentPreviews
import com.crackmydroid.ui.preview.ComponentPreviewContainer
import com.crackmydroid.ui.strings.LocalUiStrings

@Composable
fun ErrorDialogComponent(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        modifier = Modifier.semantics { paneTitle = "Error" },
        onDismissRequest = onDismiss,
        confirmButton = {
            AppButton(
                text = LocalUiStrings.current.confirm,
                onClick = onDismiss,
                fullWidth = false,
            )
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
