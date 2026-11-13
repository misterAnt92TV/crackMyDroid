package com.crackmydroid.shared.domain.model

enum class PermissionRiskLevel {
    Medium,
    High,
    Critical,
}

enum class PermissionFindingKind {
    ACCESSIBILITY_SERVICE,
    OVERLAY_ACCESS,
    SMS_MMS_ACCESS,
    APK_INSTALLATION,
    BROAD_STORAGE_ACCESS,
    INSTALLED_APP_INVENTORY,
    PHONE_AND_CONTACTS_ACCESS,
    LOCATION_ACCESS,
    MICROPHONE_CAMERA_ACCESS,
    SENSITIVE_DATA_ACCESS,
    OVERLAY_ACCESSIBILITY_PATTERN,
    INSTALL_QUERY_PATTERN,
    BOOT_SMS_PATTERN,
    BACKGROUND_LOCATION_CAPTURE_PATTERN,
}

data class PermissionRiskFinding(
    val key: String,
    val kind: PermissionFindingKind,
    val level: PermissionRiskLevel,
    val isPattern: Boolean = false,
)

data class PermissionRiskReport(
    val appLabel: String,
    val packageName: String,
    val findings: List<PermissionRiskFinding>,
    val score: Int,
    val criticalCount: Int,
    val highCount: Int,
    val mediumCount: Int,
)
