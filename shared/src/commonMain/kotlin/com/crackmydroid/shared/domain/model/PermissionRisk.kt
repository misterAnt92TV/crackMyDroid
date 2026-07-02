package com.crackmydroid.shared.domain.model

enum class PermissionRiskLevel {
    Medium,
    High,
    Critical
}

data class PermissionRiskFinding(
    val key: String,
    val title: String,
    val level: PermissionRiskLevel,
    val reason: String,
    val isPattern: Boolean = false
)

data class PermissionRiskReport(
    val appLabel: String,
    val packageName: String,
    val findings: List<PermissionRiskFinding>,
    val score: Int,
    val criticalCount: Int,
    val highCount: Int,
    val mediumCount: Int
)
