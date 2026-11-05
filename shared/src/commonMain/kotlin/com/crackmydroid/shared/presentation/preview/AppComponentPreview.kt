package com.crackmydroid.shared.presentation.preview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.crackmydroid.shared.i18n.LocalStrings
import com.crackmydroid.shared.i18n.stringsFor
import com.crackmydroid.shared.presentation.components.AccessibilityPrefs
import com.crackmydroid.shared.presentation.components.LocalAccessibilityPrefs
import com.crackmydroid.shared.presentation.theme.CrackMyDroidTheme

@Composable
internal fun ComponentPreviewContainer(
    accessibilityPrefs: AccessibilityPrefs = AccessibilityPrefs(),
    content: @Composable () -> Unit
) {
    CrackMyDroidTheme {
        CompositionLocalProvider(
            LocalStrings provides stringsFor("it"),
            LocalAccessibilityPrefs provides accessibilityPrefs
        ) {
            Surface(color = MaterialTheme.colorScheme.background) {
                Column(modifier = Modifier.padding(16.dp), content = { content() })
            }
        }
    }
}
