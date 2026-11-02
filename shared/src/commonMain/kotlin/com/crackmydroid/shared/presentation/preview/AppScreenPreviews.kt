package com.crackmydroid.shared.presentation.preview

import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_TYPE_NORMAL

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
@Preview(
    name = "Phone Light",
    group = "Phone",
    device = Devices.PHONE,
    showBackground = true,
    showSystemUi = true
)
@Preview(
    name = "Phone Dark",
    group = "Phone",
    device = Devices.PHONE,
    uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL,
    showBackground = true,
    showSystemUi = true
)
@Preview(
    name = "Tablet Light",
    group = "Tablet",
    device = Devices.TABLET,
    showBackground = true,
    showSystemUi = true
)
@Preview(
    name = "Tablet Dark",
    group = "Tablet",
    device = Devices.TABLET,
    uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL,
    showBackground = true,
    showSystemUi = true
)
annotation class AppScreenPreviews
