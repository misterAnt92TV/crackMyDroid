package com.crackmydroid.ui.preview

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.crackmydroid.ui.components.AccessibilityPrefs
import com.crackmydroid.ui.components.LocalAccessibilityPrefs
import com.crackmydroid.ui.strings.LocalUiStrings
import com.crackmydroid.ui.theme.CrackMyDroidTheme

@Composable
fun ComponentPreviewContainer(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accessibilityPrefs: AccessibilityPrefs = AccessibilityPrefs(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalAccessibilityPrefs provides accessibilityPrefs) {
        CrackMyDroidTheme(darkTheme = darkTheme) {
            Surface(
                color = MaterialTheme.colorScheme.background,
                shape = MaterialTheme.shapes.large,
            ) {
                Column(modifier = Modifier.padding(16.dp), content = { content() })
            }
        }
    }
}

