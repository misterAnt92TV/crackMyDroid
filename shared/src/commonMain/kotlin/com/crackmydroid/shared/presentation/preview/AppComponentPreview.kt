package com.crackmydroid.shared.presentation.preview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import com.crackmydroid.shared.i18n.toUiStrings
import com.crackmydroid.ui.components.AccessibilityPrefs
import com.crackmydroid.ui.components.LocalAccessibilityPrefs
import com.crackmydroid.ui.strings.LocalUiStrings
import com.crackmydroid.ui.theme.CrackMyDroidTheme

@Composable
internal fun AppPreviewEnvironment(
    languageCode: String = "it",
    accessibilityPrefs: AccessibilityPrefs = AccessibilityPrefs(),
    wrapInSurface: Boolean = true,
    content: @Composable () -> Unit,
) {
    val strings = stringsFor(languageCode)
    CrackMyDroidTheme {
        CompositionLocalProvider(
            LocalStrings provides strings,
            LocalUiStrings provides strings.toUiStrings(),
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
                .padding(16.dp),
            content = { content() },
        )
    }
}
