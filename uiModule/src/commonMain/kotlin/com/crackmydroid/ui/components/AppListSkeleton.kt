package com.crackmydroid.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.crackmydroid.ui.preview.AppComponentPreviews
import com.crackmydroid.ui.preview.ComponentPreviewContainer

private val SkeletonShape = RoundedCornerShape(8.dp)

@Composable
fun AppListSkeleton(
    itemCount: Int = 5,
    modifier: Modifier = Modifier,
    variant: AppListSkeletonVariant = AppListSkeletonVariant.TwoLine,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(itemCount.coerceAtLeast(0)) {
            AppListSkeletonItem(variant = variant)
        }
    }
}

@Composable
fun AppListSkeletonItem(
    modifier: Modifier = Modifier,
    variant: AppListSkeletonVariant = AppListSkeletonVariant.TwoLine,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(SkeletonShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(if (variant == AppListSkeletonVariant.Compact) 0.72f else 0.88f)
                    .height(16.dp)
                    .clip(SkeletonShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
            if (variant != AppListSkeletonVariant.Compact) {
                Box(
                    modifier = Modifier
                        .width(variant.subtitleWidth)
                        .height(12.dp)
                        .clip(SkeletonShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )
            }
        }
    }
}

enum class AppListSkeletonVariant(val subtitleWidth: androidx.compose.ui.unit.Dp) {
    Compact(96.dp),
    TwoLine(156.dp),
}

@AppComponentPreviews
@Composable
private fun AppListSkeletonPreview() = ComponentPreviewContainer {
    AppListSkeleton()
}
