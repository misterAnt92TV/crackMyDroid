package com.crackmydroid.shared.domain.model

import com.crackmydroid.shared.domain.model.activity.ActivityIntentRiskLevel

enum class ReportLabel {
    DEVICE_INFO_TITLE,
    MANUFACTURER,
    MODEL,
    BRAND,
    DEVICE,
    PRODUCT,
    ANDROID,
    SECURITY_PATCH,
    BUILD_ID,
    BUILD_DISPLAY,
    FINGERPRINT,
    RADIO,
    KERNEL,
    HARDWARE,
    BOOTLOADER,
    BATTERY,
    NETWORK,
    TOTAL_RAM,
    FREE_RAM,
    SCREEN,
    ADB_ENABLED,
    DEVELOPER_OPTIONS,
    APP_DEBUGGABLE,
    ROOTED,
    ROOT_DETAILS,
    IMEI,
    SERIAL,
    SYSTEM_PROPERTIES_SOURCE,
    SYSTEM_PROPERTIES_DUMP,
    PERMISSIONS_REPORT_TITLE,
    SINGLE_PERMISSIONS_REPORT_TITLE,
    APP,
    PACKAGE,
    PERMISSIONS_ANALYZED,
    PERMISSIONS_FOUND,
    APPS_WITH_RISKS,
    RISK_COUNTS,
    SEVERITY_LEGEND,
    CAUTION_NOTE,
    NO_DATA_AVAILABLE,
    DECLARED_PERMISSIONS,
    RISK_ANALYSIS,
    SCORE,
    FINDINGS,
    FINDING_DETAILS,
    PERMISSIONS,
    NO_SENSITIVE_BEHAVIOR,
    NO_RISKY_PERMISSIONS,
    NO_PERMISSION_DECLARED
}

interface UseCaseReportText {
    fun label(label: ReportLabel): String
    fun permissionTitle(finding: PermissionRiskFinding): String
    fun permissionReason(finding: PermissionRiskFinding): String
    fun riskLevel(level: PermissionRiskLevel): String
    fun activityRiskLevel(level: ActivityIntentRiskLevel): String
    fun penTest(message: PenTestMessage): String
    fun boolean(value: Boolean): String
    fun permissionOverview(appCount: Int, permissionCount: Int, appsWithRisksCount: Int): String
    fun permissionRiskCounts(criticalCount: Int, highCount: Int, mediumCount: Int): String
    fun permissionAppHeading(index: Int, appLabel: String): String
    fun permissionRiskOverview(
        score: Int,
        findingCount: Int,
        criticalCount: Int,
        highCount: Int,
        mediumCount: Int
    ): String
    fun declaredPermissionCount(count: Int): String
    fun permissionFindingHeading(index: Int, finding: PermissionRiskFinding): String
    fun noSensitiveBehavior(): String
    fun noRiskyPermissions(): String
    fun noDataAvailable(): String
    fun noDeclaredPermissions(): String
}
