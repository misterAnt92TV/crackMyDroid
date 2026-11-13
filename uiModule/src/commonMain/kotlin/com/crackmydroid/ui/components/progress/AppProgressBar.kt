package com.crackmydroid.ui.components.progress

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.crackmydroid.ui.components.LocalAccessibilityPrefs
import com.crackmydroid.ui.components.text.AppText
import com.crackmydroid.ui.preview.AppComponentPreviews
import com.crackmydroid.ui.preview.ComponentPreviewContainer
import com.crackmydroid.ui.theme.CrackMyDroidTheme
import kotlin.math.roundToInt

private val DefaultBarHeight: Dp = 6.dp

/**
 * Determinate progress bar styled according to CrackMyDroid design tokens.
 */
@Composable
fun AppProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    variant: ProgressBarVariant = ProgressBarVariant.Primary,
    label: String? = null,
    showPercentage: Boolean = false,
    progressText: String? = null,
    height: Dp = DefaultBarHeight,
    color: Color? = null,
    trackColor: Color? = null,
) {
    AppProgressBar(
        progress = { progress },
        modifier = modifier.padding(4.dp),
        variant = variant,
        label = label,
        showPercentage = showPercentage,
        progressText = progressText,
        height = height,
        color = color,
        trackColor = trackColor,
    )
}

/**
 * Determinate progress bar accepting a progress lambda for deferred state reads.
 */
@Composable
fun AppProgressBar(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    variant: ProgressBarVariant = ProgressBarVariant.Primary,
    label: String? = null,
    showPercentage: Boolean = false,
    progressText: String? = null,
    height: Dp = DefaultBarHeight,
    color: Color? = null,
    trackColor: Color? = null,
) {
    val prefs = LocalAccessibilityPrefs.current
    val rawProgress = progress()
    val clampedProgress = rawProgress.coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = clampedProgress,
        animationSpec = if (prefs.reduceMotion) tween(durationMillis = 0) else tween(durationMillis = 250),
        label = "AppProgressBarAnimation",
    )

    val effectiveColor = resolveProgressBarColor(variant = variant, override = color)
    val effectiveTrackColor = resolveProgressBarTrackColor(override = trackColor)
    val shape = RoundedCornerShape(height / 2)

    val borderModifier = if (prefs.highContrast) {
        Modifier.border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), shape)
    } else {
        Modifier
    }

    val displayTrailingText = progressText ?: if (showPercentage) {
        "${(clampedProgress * 100).roundToInt()}%"
    } else {
        null
    }

    val semanticDescription = buildString {
        if (!label.isNullOrBlank()) append(label)
        if (displayTrailingText != null) {
            if (isNotEmpty()) append(": ")
            append(displayTrailingText)
        }
    }.ifEmpty { null }

    Column(modifier = modifier) {
        if (!label.isNullOrBlank() || displayTrailingText != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!label.isNullOrBlank()) {
                    AppText(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .padding(bottom = 4.dp, top = 4.dp)
                            .weight(1f, fill = false),
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
                if (displayTrailingText != null) {
                    AppText(
                        text = displayTrailingText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }

        LinearProgressIndicator(
            progress = { if (prefs.reduceMotion) clampedProgress else animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(shape)
                .then(borderModifier)
                .semantics {
                    progressBarRangeInfo = ProgressBarRangeInfo(clampedProgress, 0f..1f)
                    if (semanticDescription != null) {
                        contentDescription = semanticDescription
                    }
                },
            color = effectiveColor,
            trackColor = effectiveTrackColor,
            strokeCap = StrokeCap.Round,
        )
    }
}

/**
 * Indeterminate progress bar styled according to CrackMyDroid design tokens.
 */
@Composable
fun AppProgressBar(
    modifier: Modifier = Modifier,
    variant: ProgressBarVariant = ProgressBarVariant.Primary,
    label: String? = null,
    height: Dp = DefaultBarHeight,
    color: Color? = null,
    trackColor: Color? = null,
) {
    val prefs = LocalAccessibilityPrefs.current
    val effectiveColor = resolveProgressBarColor(variant = variant, override = color)
    val effectiveTrackColor = resolveProgressBarTrackColor(override = trackColor)
    val shape = RoundedCornerShape(height / 2)

    val borderModifier = if (prefs.highContrast) {
        Modifier.border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), shape)
    } else {
        Modifier
    }

    Column(modifier = modifier) {
        if (!label.isNullOrBlank()) {
            AppText(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }

        if (prefs.reduceMotion) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .clip(shape)
                    .background(effectiveColor)
                    .then(borderModifier)
                    .semantics {
                        progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
                        if (!label.isNullOrBlank()) {
                            contentDescription = label
                        }
                    },
            )
        } else {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .clip(shape)
                    .then(borderModifier)
                    .semantics {
                        progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
                        if (!label.isNullOrBlank()) {
                            contentDescription = label
                        }
                    },
                color = effectiveColor,
                trackColor = effectiveTrackColor,
                strokeCap = StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun resolveProgressBarColor(variant: ProgressBarVariant, override: Color?): Color {
    if (override != null) return override
    val statusColors = CrackMyDroidTheme.statusColors
    return when (variant) {
        ProgressBarVariant.Primary -> MaterialTheme.colorScheme.primary
        ProgressBarVariant.Success -> statusColors.success
        ProgressBarVariant.Error -> MaterialTheme.colorScheme.error
        ProgressBarVariant.Warning -> statusColors.warning
        ProgressBarVariant.Info -> statusColors.info
    }
}

@Composable
private fun resolveProgressBarTrackColor(override: Color?): Color = override ?: MaterialTheme.colorScheme.surfaceVariant

@AppComponentPreviews
@Composable
private fun AppProgressBarPreview() = ComponentPreviewContainer {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AppProgressBar(
            progress = 0.65f,
            label = "Scansione file in corso",
            showPercentage = true,
        )
        AppProgressBar(
            progress = 1.0f,
            variant = ProgressBarVariant.Success,
            label = "Verifica completata",
            progressText = "10/10",
        )
        AppProgressBar(
            progress = 0.40f,
            variant = ProgressBarVariant.Warning,
            label = "Avviso rischi rilevati",
            showPercentage = true,
        )
        AppProgressBar(
            progress = 0.25f,
            variant = ProgressBarVariant.Error,
            label = "Fallimento controlli",
            showPercentage = true,
        )
        AppProgressBar(
            label = "Caricamento indefinito in corso...",
        )
    }
}
