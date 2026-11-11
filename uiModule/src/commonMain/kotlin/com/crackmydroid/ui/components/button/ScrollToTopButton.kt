package com.crackmydroid.ui.components.button
import com.crackmydroid.ui.icons.AppIcons

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.crackmydroid.ui.components.LocalAccessibilityPrefs
import com.crackmydroid.ui.strings.LocalUiStrings
import com.crackmydroid.ui.preview.AppComponentPreviews
import com.crackmydroid.ui.preview.ComponentPreviewContainer

@Composable
fun ScrollToTopButton(
    visible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    val reduceMotion = LocalAccessibilityPrefs.current.reduceMotion
    val strings = LocalUiStrings.current
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = if (reduceMotion) fadeIn() else fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut(),
    ) {
        FloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Icon(
                imageVector = AppIcons.Filled.KeyboardArrowUp,
                contentDescription = contentDescription ?: strings.scrollToTop,
            )
        }
    }
}

@AppComponentPreviews
@Composable
private fun ScrollToTopButtonPreview() = ComponentPreviewContainer {
    ScrollToTopButton(visible = true, onClick = {})
}
