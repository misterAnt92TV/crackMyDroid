package com.crackmydroid.shared.ui.activities

import com.crackmydroid.shared.domain.model.activity.ActivityIntentRiskLevel

fun ActivityIntentRiskLevel.uiLabel(): String = when (this) {
    ActivityIntentRiskLevel.Low -> "Basso"
    ActivityIntentRiskLevel.Medium -> "Medio"
    ActivityIntentRiskLevel.High -> "Alto"
    ActivityIntentRiskLevel.Critical -> "Critico"
}
