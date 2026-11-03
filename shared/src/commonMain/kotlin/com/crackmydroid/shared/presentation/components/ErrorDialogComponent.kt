package com.crackmydroid.shared.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.crackmydroid.shared.i18n.LocalStrings

@Composable
fun ErrorDialogComponent(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss) { Text(LocalStrings.current.confirm) }
        },
        text = { Text(message) }
    )
}
