package com.crackmydroid.shared.presentation.preview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
    content: @Composable () -> Unit,
) {
    AppPreviewEnvironment(accessibilityPrefs = accessibilityPrefs, wrapInSurface = false) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(20.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp), content = { content() })
        }
    }
}

@Composable
internal fun AppPreviewEnvironment(
    languageCode: String = "it",
    accessibilityPrefs: AccessibilityPrefs = AccessibilityPrefs(),
    wrapInSurface: Boolean = true,
    content: @Composable () -> Unit,
) {
    CrackMyDroidTheme {
        CompositionLocalProvider(
            LocalStrings provides stringsFor(languageCode),
            LocalAccessibilityPrefs provides accessibilityPrefs,
        ) {
            if (wrapInSurface) {
                PreviewSurface(content = content)
            } else {
                content()
            }
        }
    }
}

@Composable
internal fun PreviewSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            content = { content() },
        )
    }
}
