package com.crackmydroid.shared.domain.usecase.permissions

import co.touchlab.kermit.Logger
import com.crackmydroid.shared.AppLogger
import com.crackmydroid.shared.domain.model.PermissionFindingKind
import com.crackmydroid.shared.domain.model.PermissionRiskFinding
import com.crackmydroid.shared.domain.model.PermissionRiskLevel
import com.crackmydroid.shared.domain.model.PermissionRiskReport
import com.crackmydroid.shared.domain.model.app.AppPermissionEntry
import com.crackmydroid.shared.domain.usecase.BaseUseCase
import com.crackmydroid.shared.util.AppDispatchers
import com.crackmydroid.shared.util.DefaultAppDispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

class AnalyzePermissionRisksUseCase(
    dispatchers: AppDispatchers = DefaultAppDispatchers,
    logger: Logger = AppLogger.logger,
) : BaseUseCase(TAG, dispatcher = dispatchers.default, logger = logger) {
    private companion object {
        private const val TAG = "AnalyzePermissionRisksUseCase"
        private const val PATTERN_OVERLAY_ACCESSIBILITY = "pattern_overlay_accessibility"
        private const val PATTERN_INSTALL_QUERY = "pattern_install_query"
        private const val PATTERN_BOOT_SMS = "pattern_boot_sms"
        private const val PATTERN_BACKGROUND_CAPTURE = "pattern_background_capture"
    }

    suspend operator fun invoke(app: AppPermissionEntry): Result<PermissionRiskReport?> = execute {
        if (app.permissions.isEmpty()) return@execute null
        val context = currentCoroutineContext()
        val permissions = app.permissions.map {
            context.ensureActive()
            it.trim()
        }.filter { it.isNotBlank() }.distinct()
        if (permissions.isEmpty()) return@execute null

        val findingsByKey = linkedMapOf<String, PermissionRiskFinding>()
        permissions.forEach { permission ->
            context.ensureActive()
            detectPermissionRisk(permission)?.let { finding ->
                registerFinding(findingsByKey, finding)
            }
        }

        val hasOverlay = containsPermission(permissions, "SYSTEM_ALERT_WINDOW")
        val hasAccessibility = containsPermission(permissions, "BIND_ACCESSIBILITY_SERVICE")
        if (hasOverlay && hasAccessibility) {
            registerFinding(
                findingsByKey,
                PermissionRiskFinding(
                    key = PATTERN_OVERLAY_ACCESSIBILITY,
                    kind = PermissionFindingKind.OVERLAY_ACCESSIBILITY_PATTERN,
                    level = PermissionRiskLevel.Critical,
                    isPattern = true,
                ),
            )
        }

        val hasInstallPackages = containsPermission(permissions, "REQUEST_INSTALL_PACKAGES")
        val hasQueryAllPackages = containsPermission(permissions, "QUERY_ALL_PACKAGES")
        if (hasInstallPackages && hasQueryAllPackages) {
            registerFinding(
                findingsByKey,
                PermissionRiskFinding(
                    key = PATTERN_INSTALL_QUERY,
                    kind = PermissionFindingKind.INSTALL_QUERY_PATTERN,
                    level = PermissionRiskLevel.High,
                    isPattern = true,
                ),
            )
        }

        val hasBootCompleted = containsPermission(permissions, "RECEIVE_BOOT_COMPLETED")
        val hasSmsFamily = containsAnyPermission(
            permissions,
            listOf("READ_SMS", "SEND_SMS", "RECEIVE_SMS", "RECEIVE_MMS", "RECEIVE_WAP_PUSH"),
        )
        if (hasBootCompleted && hasSmsFamily) {
            registerFinding(
                findingsByKey,
                PermissionRiskFinding(
                    key = PATTERN_BOOT_SMS,
                    kind = PermissionFindingKind.BOOT_SMS_PATTERN,
                    level = PermissionRiskLevel.Critical,
                    isPattern = true,
                ),
            )
        }

        val hasBackgroundLocation = containsPermission(permissions, "ACCESS_BACKGROUND_LOCATION")
        val hasMicOrCamera = containsAnyPermission(permissions, listOf("RECORD_AUDIO", "CAMERA"))
        if (hasBackgroundLocation && hasMicOrCamera) {
            registerFinding(
                findingsByKey,
                PermissionRiskFinding(
                    key = PATTERN_BACKGROUND_CAPTURE,
                    kind = PermissionFindingKind.BACKGROUND_LOCATION_CAPTURE_PATTERN,
                    level = PermissionRiskLevel.High,
                    isPattern = true,
                ),
            )
        }

        val findings = findingsByKey.values.sortedWith(
            compareByDescending<PermissionRiskFinding> {
                context.ensureActive()
                riskWeight(it.level)
            }
                .thenByDescending { it.isPattern }
                .thenBy { findingSortKey(it) },
        )
        if (findings.isEmpty()) return@execute null

        val criticalCount = findings.count { it.level == PermissionRiskLevel.Critical }
        val highCount = findings.count { it.level == PermissionRiskLevel.High }
        val mediumCount = findings.count { it.level == PermissionRiskLevel.Medium }
        val score = findings.sumOf { riskWeight(it.level) }

        PermissionRiskReport(
            appLabel = app.appLabel,
            packageName = app.packageName,
            findings = findings,
            score = score,
            criticalCount = criticalCount,
            highCount = highCount,
            mediumCount = mediumCount,
        )
    }

    fun sortReports(reports: List<PermissionRiskReport>): List<PermissionRiskReport> =
        reports.sortedWith(
            compareByDescending<PermissionRiskReport> { it.score }
                .thenByDescending { it.criticalCount }
                .thenBy { it.appLabel.lowercase() },
        )

    private fun detectPermissionRisk(permission: String): PermissionRiskFinding? {
        val key = permission.uppercase()
        return when {
            "BIND_ACCESSIBILITY_SERVICE" in key -> PermissionRiskFinding(
                key = permission,
                kind = PermissionFindingKind.ACCESSIBILITY_SERVICE,
                level = PermissionRiskLevel.Critical,
            )

            "SYSTEM_ALERT_WINDOW" in key -> PermissionRiskFinding(
                key = permission,
                kind = PermissionFindingKind.OVERLAY_ACCESS,
                level = PermissionRiskLevel.Critical,
            )

            "SEND_SMS" in key || "READ_SMS" in key || "RECEIVE_SMS" in key ||
                "RECEIVE_MMS" in key || "RECEIVE_WAP_PUSH" in key -> PermissionRiskFinding(
                key = permission,
                kind = PermissionFindingKind.SMS_MMS_ACCESS,
                level = PermissionRiskLevel.Critical,
            )

            "REQUEST_INSTALL_PACKAGES" in key -> PermissionRiskFinding(
                key = permission,
                kind = PermissionFindingKind.APK_INSTALLATION,
                level = PermissionRiskLevel.High,
            )

            "MANAGE_EXTERNAL_STORAGE" in key -> PermissionRiskFinding(
                key = permission,
                kind = PermissionFindingKind.BROAD_STORAGE_ACCESS,
                level = PermissionRiskLevel.High,
            )

            "QUERY_ALL_PACKAGES" in key -> PermissionRiskFinding(
                key = permission,
                kind = PermissionFindingKind.INSTALLED_APP_INVENTORY,
                level = PermissionRiskLevel.High,
            )

            "READ_CALL_LOG" in key || "WRITE_CALL_LOG" in key || "PROCESS_OUTGOING_CALLS" in key ||
                "READ_CONTACTS" in key || "WRITE_CONTACTS" in key ||
                "READ_PHONE_STATE" in key || "CALL_PHONE" in key || "ANSWER_PHONE_CALLS" in key -> PermissionRiskFinding(
                key = permission,
                kind = PermissionFindingKind.PHONE_AND_CONTACTS_ACCESS,
                level = PermissionRiskLevel.High,
            )

            "ACCESS_BACKGROUND_LOCATION" in key || "ACCESS_FINE_LOCATION" in key -> PermissionRiskFinding(
                key = permission,
                kind = PermissionFindingKind.LOCATION_ACCESS,
                level = PermissionRiskLevel.High,
            )

            "RECORD_AUDIO" in key || "CAMERA" in key -> PermissionRiskFinding(
                key = permission,
                kind = PermissionFindingKind.MICROPHONE_CAMERA_ACCESS,
                level = PermissionRiskLevel.High,
            )

            "READ_EXTERNAL_STORAGE" in key || "WRITE_EXTERNAL_STORAGE" in key ||
                "READ_MEDIA_" in key || "READ_CALENDAR" in key || "WRITE_CALENDAR" in key ||
                "BODY_SENSORS" in key || "USE_BIOMETRIC" in key || "USE_FINGERPRINT" in key -> PermissionRiskFinding(
                key = permission,
                kind = PermissionFindingKind.SENSITIVE_DATA_ACCESS,
                level = PermissionRiskLevel.Medium,
            )

            else -> null
        }
    }

    private suspend fun containsPermission(permissions: List<String>, token: String): Boolean =
        permissions.any {
            currentCoroutineContext().ensureActive()
            it.uppercase().contains(token)
        }

    private suspend fun containsAnyPermission(permissions: List<String>, tokens: List<String>): Boolean =
        tokens.any { token -> containsPermission(permissions, token) }

    private fun registerFinding(
        findingsByKey: MutableMap<String, PermissionRiskFinding>,
        finding: PermissionRiskFinding,
    ) {
        val existing = findingsByKey[finding.key]
        if (existing == null || riskWeight(finding.level) > riskWeight(existing.level)) {
            findingsByKey[finding.key] = finding
        }
    }

    private fun riskWeight(level: PermissionRiskLevel): Int = when (level) {
        PermissionRiskLevel.Critical -> 5
        PermissionRiskLevel.High -> 3
        PermissionRiskLevel.Medium -> 1
    }

    private fun findingSortKey(finding: PermissionRiskFinding): String = when (finding.kind) {
        PermissionFindingKind.BACKGROUND_LOCATION_CAPTURE_PATTERN -> "00"
        PermissionFindingKind.INSTALL_QUERY_PATTERN -> "01"
        PermissionFindingKind.BOOT_SMS_PATTERN -> "02"
        PermissionFindingKind.OVERLAY_ACCESSIBILITY_PATTERN -> "03"
        else -> finding.key
    }
}
