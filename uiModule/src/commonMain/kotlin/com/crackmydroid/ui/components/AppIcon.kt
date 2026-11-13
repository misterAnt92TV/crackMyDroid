package com.crackmydroid.ui.components
import com.crackmydroid.ui.icons.AppIcons

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.crackmydroid.ui.preview.AppComponentPreviews
import com.crackmydroid.ui.preview.ComponentPreviewContainer

@Composable
fun AppIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    val prefs = LocalAccessibilityPrefs.current
    val effectiveTint = if (prefs.highContrast && tint == LocalContentColor.current) {
        MaterialTheme.colorScheme.onBackground
    } else {
        tint
    }
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = effectiveTint,
    )
}

@Composable
fun AppIcon(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    val prefs = LocalAccessibilityPrefs.current
    val effectiveTint = if (prefs.highContrast && tint == LocalContentColor.current) {
        MaterialTheme.colorScheme.onBackground
    } else {
        tint
    }
    Icon(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = effectiveTint,
    )
}

@Composable
expect fun rememberPackageIcon(packageName: String): Painter?

@Composable
fun PackageIcon(
    packageName: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentPadding: Dp = 8.dp,
    contentDescription: String? = packageName,
) {
    val painter = rememberPackageIcon(packageName)
    val tint = MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = modifier
            .size(size)
            .background(backgroundColor, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (painter != null) {
            Image(
                painter = painter,
                contentDescription = contentDescription,
                modifier = Modifier
                    .padding(contentPadding)
                    .size(size - contentPadding * 2),
            )
        } else {
            AppIcon(
                imageVector = AppIcons.Filled.Android,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier
                    .padding(contentPadding)
                    .size(size - contentPadding * 2),
            )
        }
    }
}

@AppComponentPreviews
@Composable
private fun AppIconPreview() = ComponentPreviewContainer {
    AppIcon(imageVector = AppIcons.Filled.Android, contentDescription = "Android")
}

@AppComponentPreviews
@Composable
private fun PackageIconPreview() = ComponentPreviewContainer {
    PackageIcon(packageName = "com.crackmydroid.preview", size = 64.dp)
}
