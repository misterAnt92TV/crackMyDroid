package com.crackmydroid.shared.presentation.components.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crackmydroid.shared.presentation.components.text.AppText
import com.crackmydroid.shared.presentation.preview.AppComponentPreviews
import com.crackmydroid.shared.presentation.preview.ComponentPreviewContainer

@Composable
fun CardBlock(
    title: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    content: @Composable () -> Unit,
) {
    AppCard(
        modifier = modifier.padding(vertical = 12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            leadingIcon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
            AppText(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@AppComponentPreviews
@Composable
private fun CardBlockPreview() = ComponentPreviewContainer {
    CardBlock(
        title = "Esportazione completata",
        leadingIcon = Icons.Filled.FileDownload,
        modifier = Modifier.fillMaxWidth(),
    ) {
        AppText("Il report è pronto per essere condiviso.")
    }
}
