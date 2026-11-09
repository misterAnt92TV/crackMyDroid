package com.crackmydroid.shared.presentation.preview

import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
@Preview(
    name = "Locale",
    group = "Localization",
    device = Devices.PHONE,
    showBackground = true,
)
annotation class AppLocalePreviews
