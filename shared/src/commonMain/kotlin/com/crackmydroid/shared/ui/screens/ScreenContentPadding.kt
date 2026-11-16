package com.crackmydroid.shared.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import com.crackmydroid.ui.theme.CrackMyDroidTheme
import com.crackmydroid.ui.theme.appScreenContentPadding

@Composable
internal fun screenContentPadding(
    systemPadding: PaddingValues,
    horizontal: Dp = CrackMyDroidTheme.spacing.screenHorizontal,
    top: Dp = CrackMyDroidTheme.spacing.screenVertical,
    bottom: Dp = CrackMyDroidTheme.spacing.screenBottomPadding,
): PaddingValues = appScreenContentPadding(
    systemPadding = systemPadding,
    horizontal = horizontal,
    top = top,
    bottom = bottom,
)

@Composable
internal fun screenContentPadding(
    systemPadding: PaddingValues,
    horizontal: Dp = CrackMyDroidTheme.spacing.screenHorizontal,
    vertical: Dp = CrackMyDroidTheme.spacing.screenVertical,
): PaddingValues = appScreenContentPadding(
    systemPadding = systemPadding,
    horizontal = horizontal,
    vertical = vertical,
)
