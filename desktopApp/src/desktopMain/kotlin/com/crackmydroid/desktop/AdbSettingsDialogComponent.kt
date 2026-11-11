package com.crackmydroid.desktop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.crackmydroid.shared.i18n.LocalStrings
import com.crackmydroid.ui.components.dialog.AppDialog
import com.crackmydroid.ui.components.text.AppTextField
import com.crackmydroid.ui.components.text.AppText

@Composable
internal fun AdbSettingsDialogComponent(
    adbPath: String,
    onAdbPathChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    val strings = LocalStrings.current
    AppDialog(
        title = strings.desktopAdbSettings,
        onDismiss = onDismiss,
        confirmLabel = strings.desktopSave,
        onConfirm = onSave,
        confirmEnabled = adbPath.isNotBlank(),
        dismissLabel = strings.desktopCancel,
        onDismissAction = onDismiss,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AppText(
                strings.desktopAdbSettingsDescription,
                style = MaterialTheme.typography.bodyMedium,
            )
            AppTextField(
                value = adbPath,
                onValueChange = onAdbPathChange,
                label = strings.desktopAdbPathLabel,
                singleLine = true,
            )
        }
    }
}
