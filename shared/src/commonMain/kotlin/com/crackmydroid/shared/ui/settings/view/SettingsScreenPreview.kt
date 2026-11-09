package com.crackmydroid.shared.ui.settings.view

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import com.crackmydroid.shared.domain.model.app.AppLanguage
import com.crackmydroid.shared.domain.model.app.AppTheme
import com.crackmydroid.shared.i18n.stringsFor
import com.crackmydroid.shared.presentation.preview.AppLocalePreviews
import com.crackmydroid.shared.presentation.preview.AppPreviewEnvironment
import com.crackmydroid.shared.presentation.preview.AppScreenPreviews
import com.crackmydroid.shared.ui.settings.SettingsState
import com.crackmydroid.shared.ui.settings.model.SettingsActions
import com.crackmydroid.shared.ui.settings.model.SettingsAppInfo

@AppScreenPreviews
@Composable
private fun SettingsScreenPreview() = SettingsLocalePreview(AppLanguage.ITALIAN)

@AppLocalePreviews
@Composable
private fun SettingsEnglishPreview() = SettingsLocalePreview(AppLanguage.ENGLISH)

@AppLocalePreviews
@Composable
private fun SettingsGermanPreview() = SettingsLocalePreview(AppLanguage.GERMAN)

@AppLocalePreviews
@Composable
private fun SettingsFrenchPreview() = SettingsLocalePreview(AppLanguage.FRENCH)

@AppLocalePreviews
@Composable
private fun SettingsSpanishPreview() = SettingsLocalePreview(AppLanguage.SPANISH)

@AppLocalePreviews
@Composable
private fun SettingsWithoutPermissionsPreview() = SettingsLocalePreview(
    language = AppLanguage.ENGLISH,
    appInfo = SettingsAppInfo("1.2", "com.example.preview", null, null, emptyList()),
)

@AppLocalePreviews
@Composable
private fun SettingsErrorPreview() = SettingsLocalePreview(
    language = AppLanguage.ENGLISH,
    error = "Synthetic storage error",
)

@Composable
private fun SettingsLocalePreview(
    language: AppLanguage,
    error: String? = null,
    appInfo: SettingsAppInfo = SettingsAppInfo(
        versionName = "1.2",
        packageName = "com.crackmydroid.android",
        minSdk = 24,
        targetSdk = 35,
        requestedPermissions = listOf("android.permission.QUERY_ALL_PACKAGES"),
    ),
) {
    val strings = stringsFor(language.code)
    AppPreviewEnvironment(languageCode = language.code) {
        SettingsContent(
            state = SettingsState(
                language = language,
                theme = AppTheme.SYSTEM,
                suggestionsEnabled = true,
                featureHintsEnabled = true,
                error = error,
            ),
            padding = PaddingValues(),
            headerHelp = strings.helpSettings,
            appInfo = appInfo,
            actions = SettingsActions(),
        )
    }
}
