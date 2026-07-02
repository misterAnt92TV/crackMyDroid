package com.crackmydroid.shared.domain.usecase.permissions

import com.crackmydroid.shared.domain.model.AppPermissionEntry
import com.crackmydroid.shared.domain.model.PermissionRiskFinding
import com.crackmydroid.shared.domain.model.PermissionRiskLevel
import com.crackmydroid.shared.domain.model.PermissionRiskReport

class AnalyzePermissionRisksUseCase {
    private companion object {
        private const val PATTERN_OVERLAY_ACCESSIBILITY = "pattern_overlay_accessibility"
        private const val PATTERN_INSTALL_QUERY = "pattern_install_query"
        private const val PATTERN_BOOT_SMS = "pattern_boot_sms"
        private const val PATTERN_BACKGROUND_CAPTURE = "pattern_background_capture"
    }

    operator fun invoke(app: AppPermissionEntry): PermissionRiskReport? {
        if (app.permissions.isEmpty()) return null
        val permissions = app.permissions.map { it.trim() }.filter { it.isNotBlank() }.distinct()
        if (permissions.isEmpty()) return null

        val findingsByKey = linkedMapOf<String, PermissionRiskFinding>()
        permissions.forEach { permission ->
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
                    title = "Pattern sospetto: overlay + accessibility",
                    level = PermissionRiskLevel.Critical,
                    reason = "La combinazione puo sovrapporre UI e intercettare interazioni utente.",
                    isPattern = true
                )
            )
        }

        val hasInstallPackages = containsPermission(permissions, "REQUEST_INSTALL_PACKAGES")
        val hasQueryAllPackages = containsPermission(permissions, "QUERY_ALL_PACKAGES")
        if (hasInstallPackages && hasQueryAllPackages) {
            registerFinding(
                findingsByKey,
                PermissionRiskFinding(
                    key = PATTERN_INSTALL_QUERY,
                    title = "Pattern sospetto: installazione + inventario app",
                    level = PermissionRiskLevel.High,
                    reason = "Puo scaricare/gestire APK e mappare le app presenti nel dispositivo.",
                    isPattern = true
                )
            )
        }

        val hasBootCompleted = containsPermission(permissions, "RECEIVE_BOOT_COMPLETED")
        val hasSmsFamily = containsAnyPermission(
            permissions,
            listOf("READ_SMS", "SEND_SMS", "RECEIVE_SMS", "RECEIVE_MMS", "RECEIVE_WAP_PUSH")
        )
        if (hasBootCompleted && hasSmsFamily) {
            registerFinding(
                findingsByKey,
                PermissionRiskFinding(
                    key = PATTERN_BOOT_SMS,
                    title = "Pattern sospetto: persistenza + SMS",
                    level = PermissionRiskLevel.Critical,
                    reason = "Può riattivarsi al boot e interagire con canali SMS sensibili.",
                    isPattern = true
                )
            )
        }

        val hasBackgroundLocation = containsPermission(permissions, "ACCESS_BACKGROUND_LOCATION")
        val hasMicOrCamera = containsAnyPermission(permissions, listOf("RECORD_AUDIO", "CAMERA"))
        if (hasBackgroundLocation && hasMicOrCamera) {
            registerFinding(
                findingsByKey,
                PermissionRiskFinding(
                    key = PATTERN_BACKGROUND_CAPTURE,
                    title = "Pattern sospetto: background location + acquisizione",
                    level = PermissionRiskLevel.High,
                    reason = "Accesso continuo alla posizione unito a sensori audio/video.",
                    isPattern = true
                )
            )
        }

        val findings = findingsByKey.values.sortedWith(
            compareByDescending<PermissionRiskFinding> { riskWeight(it.level) }
                .thenByDescending { it.isPattern }
                .thenBy { it.title }
        )
        if (findings.isEmpty()) return null

        val criticalCount = findings.count { it.level == PermissionRiskLevel.Critical }
        val highCount = findings.count { it.level == PermissionRiskLevel.High }
        val mediumCount = findings.count { it.level == PermissionRiskLevel.Medium }
        val score = findings.sumOf { riskWeight(it.level) }

        return PermissionRiskReport(
            appLabel = app.appLabel,
            packageName = app.packageName,
            findings = findings,
            score = score,
            criticalCount = criticalCount,
            highCount = highCount,
            mediumCount = mediumCount
        )
    }

    fun sortReports(reports: List<PermissionRiskReport>): List<PermissionRiskReport> =
        reports.sortedWith(
            compareByDescending<PermissionRiskReport> { it.score }
                .thenByDescending { it.criticalCount }
                .thenBy { it.appLabel.lowercase() }
        )

    fun riskLabel(level: PermissionRiskLevel): String = when (level) {
        PermissionRiskLevel.Critical -> "CRITICO"
        PermissionRiskLevel.High -> "ALTO"
        PermissionRiskLevel.Medium -> "MEDIO"
    }

    private fun detectPermissionRisk(permission: String): PermissionRiskFinding? {
        val key = permission.uppercase()
        return when {
            "BIND_ACCESSIBILITY_SERVICE" in key -> PermissionRiskFinding(
                key = permission,
                title = permission,
                level = PermissionRiskLevel.Critical,
                reason = "Può leggere contenuti a schermo e simulare interazioni utente."
            )
            "SYSTEM_ALERT_WINDOW" in key -> PermissionRiskFinding(
                key = permission,
                title = permission,
                level = PermissionRiskLevel.Critical,
                reason = "Consente overlay sopra altre app (rischio phishing/tapjacking)."
            )
            "SEND_SMS" in key || "READ_SMS" in key || "RECEIVE_SMS" in key ||
                "RECEIVE_MMS" in key || "RECEIVE_WAP_PUSH" in key -> PermissionRiskFinding(
                key = permission,
                title = permission,
                level = PermissionRiskLevel.Critical,
                reason = "Accesso a canali SMS/MMS con possibile esfiltrazione o frode."
            )
            "REQUEST_INSTALL_PACKAGES" in key -> PermissionRiskFinding(
                key = permission,
                title = permission,
                level = PermissionRiskLevel.High,
                reason = "Può richiedere installazioni di APK fuori dal Play Store."
            )
            "MANAGE_EXTERNAL_STORAGE" in key -> PermissionRiskFinding(
                key = permission,
                title = permission,
                level = PermissionRiskLevel.High,
                reason = "Accesso esteso allo storage del dispositivo."
            )
            "QUERY_ALL_PACKAGES" in key -> PermissionRiskFinding(
                key = permission,
                title = permission,
                level = PermissionRiskLevel.High,
                reason = "Permette inventario completo delle app installate."
            )
            "READ_CALL_LOG" in key || "WRITE_CALL_LOG" in key || "PROCESS_OUTGOING_CALLS" in key ||
                "READ_CONTACTS" in key || "WRITE_CONTACTS" in key ||
                "READ_PHONE_STATE" in key || "CALL_PHONE" in key || "ANSWER_PHONE_CALLS" in key -> PermissionRiskFinding(
                key = permission,
                title = permission,
                level = PermissionRiskLevel.High,
                reason = "Accesso a dati telefonici o rubriche sensibili."
            )
            "ACCESS_BACKGROUND_LOCATION" in key || "ACCESS_FINE_LOCATION" in key -> PermissionRiskFinding(
                key = permission,
                title = permission,
                level = PermissionRiskLevel.High,
                reason = "Tracciamento posizione preciso, anche in background."
            )
            "RECORD_AUDIO" in key || "CAMERA" in key -> PermissionRiskFinding(
                key = permission,
                title = permission,
                level = PermissionRiskLevel.High,
                reason = "Accesso a microfono/camera con possibile acquisizione sensibile."
            )
            "READ_EXTERNAL_STORAGE" in key || "WRITE_EXTERNAL_STORAGE" in key ||
                "READ_MEDIA_" in key || "READ_CALENDAR" in key || "WRITE_CALENDAR" in key ||
                "BODY_SENSORS" in key || "USE_BIOMETRIC" in key || "USE_FINGERPRINT" in key -> PermissionRiskFinding(
                key = permission,
                title = permission,
                level = PermissionRiskLevel.Medium,
                reason = "Permesso sensibile da verificare nel contesto funzionale dell'app."
            )
            else -> null
        }
    }

    private fun containsPermission(permissions: List<String>, token: String): Boolean =
        permissions.any { it.uppercase().contains(token) }

    private fun containsAnyPermission(permissions: List<String>, tokens: List<String>): Boolean =
        tokens.any { token -> containsPermission(permissions, token) }

    private fun registerFinding(
        findingsByKey: MutableMap<String, PermissionRiskFinding>,
        finding: PermissionRiskFinding
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
}
