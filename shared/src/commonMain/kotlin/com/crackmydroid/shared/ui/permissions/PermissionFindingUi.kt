package com.crackmydroid.shared.ui.permissions

import com.crackmydroid.shared.domain.model.PermissionRiskFinding
import com.crackmydroid.shared.domain.model.PermissionRiskLevel
import com.crackmydroid.shared.domain.model.UseCaseReportText

data class PermissionFindingUi(
    val key: String,
    val title: String,
    val reason: String,
    val level: PermissionRiskLevel,
    val levelLabel: String,
    val isPattern: Boolean
)

internal fun PermissionRiskFinding.toUi(text: UseCaseReportText): PermissionFindingUi =
    PermissionFindingUi(key, text.permissionTitle(this), text.permissionReason(this), level, text.riskLevel(level), isPattern)
