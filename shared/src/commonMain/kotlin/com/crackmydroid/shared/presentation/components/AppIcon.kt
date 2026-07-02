package com.crackmydroid.shared.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
expect fun rememberPackageIcon(packageName: String): Painter?

@Composable
fun PackageIcon(
    packageName: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    backgroundColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.secondaryContainer,
    contentPadding: Dp = 8.dp
) {
    val painter = rememberPackageIcon(packageName)
    val tint = MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = modifier
            .size(size)
            .background(backgroundColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (painter != null) {
            Image(
                painter = painter,
                contentDescription = packageName,
                modifier = Modifier
                    .padding(contentPadding)
                    .size(size - contentPadding * 2)
            )
        } else {
            androidx.compose.material3.Icon(
                imageVector = Icons.Filled.Android,
                contentDescription = packageName,
                tint = tint,
                modifier = Modifier
                    .padding(contentPadding)
                    .size(size - contentPadding * 2)
            )
        }
    }
}
