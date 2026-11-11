package com.crackmydroid.shared.ui.activities.model

import com.crackmydroid.shared.domain.model.activity.ActivityIntentRiskLevel
import com.crackmydroid.shared.i18n.ActivityUiStrings

fun ActivityIntentRiskLevel.uiLabel(strings: ActivityUiStrings): String = when (this) {
    ActivityIntentRiskLevel.Low -> strings.riskLow
    ActivityIntentRiskLevel.Medium -> strings.riskMedium
    ActivityIntentRiskLevel.High -> strings.riskHigh
    ActivityIntentRiskLevel.Critical -> strings.riskCritical
}
