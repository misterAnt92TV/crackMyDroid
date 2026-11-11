package com.crackmydroid.ui.preview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
    accessibilityPrefs: AccessibilityPrefs = AccessibilityPrefs(),
    content: @Composable () -> Unit,
) {
    CrackMyDroidTheme {
        CompositionLocalProvider(LocalAccessibilityPrefs provides accessibilityPrefs) {
            Surface(color = MaterialTheme.colorScheme.background, shape = RoundedCornerShape(20.dp)) {
                Column(modifier = Modifier.padding(16.dp), content = { content() })
            }
        }
    }
}
