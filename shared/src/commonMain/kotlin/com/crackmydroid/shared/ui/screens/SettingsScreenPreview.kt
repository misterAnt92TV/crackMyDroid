package com.crackmydroid.shared.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.crackmydroid.shared.domain.model.AppLanguage
import com.crackmydroid.shared.domain.model.AppTheme
import com.crackmydroid.shared.i18n.LocalStrings
import com.crackmydroid.shared.i18n.stringsFor
import com.crackmydroid.shared.presentation.preview.AppScreenPreviews
import com.crackmydroid.shared.presentation.theme.CrackMyDroidTheme
import com.crackmydroid.shared.ui.settings.SettingsActions
import com.crackmydroid.shared.ui.settings.SettingsAppInfo
import com.crackmydroid.shared.ui.settings.SettingsState

@AppScreenPreviews
@Composable
private fun SettingsScreenPreview() {
    CrackMyDroidTheme {
        CompositionLocalProvider(LocalStrings provides stringsFor("it")) {
            SettingsContent(
                state = SettingsState(
                    language = AppLanguage.ITALIAN,
                    theme = AppTheme.SYSTEM,
                    suggestionsEnabled = true,
                    featureHintsEnabled = true
                ),
                padding = PaddingValues(),
                headerHelp = "Configura tema, lingua, accessibilità ed esportazione.",
                appInfo = SettingsAppInfo(
                    versionName = "1.2",
                    packageName = "com.crackmydroid.android",
                    minSdk = 24,
                    targetSdk = 35,
                    requestedPermissions = listOf("android.permission.QUERY_ALL_PACKAGES")
                ),
                actions = SettingsActions()
            )
        }
    }
}
