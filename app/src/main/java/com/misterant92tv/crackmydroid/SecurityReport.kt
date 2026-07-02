package com.misterant92tv.crackmydroid

/**
 * Represents the security analysis result for a single installed application.
 */
data class SecurityReport(
    val packageName: String,
    val appName: String,
    val versionName: String?,
    val versionCode: Long,
    val isDebuggable: Boolean,
    val allowsBackup: Boolean,
    val isTestOnly: Boolean,
    val isSystemApp: Boolean,
    val minSdkVersion: Int,
    val dangerousPermissions: List<String>,
    val exportedActivities: List<String>,
    val exportedServices: List<String>,
    val exportedReceivers: List<String>,
    val exportedProviders: List<String>,
    val networkSecurityConfigPresent: Boolean
) {
    enum class RiskLevel { LOW, MEDIUM, HIGH }

    /**
     * Computes an overall risk score (0–100) based on security findings.
     *
     * Scoring breakdown:
     *  - Debuggable flag:            +30 points
     *  - Backup allowed:             +10 points
     *  - Test-only flag:             +10 points
     *  - Min SDK < 23 (< Marshmallow): +10 points
     *  - Each dangerous permission:  +3  points (up to 15)
     *  - Each exported component:    +2  points (up to 10)
     *  - No network security config: +5  points
     */
    val riskScore: Int
        get() {
            var score = 0
            if (isDebuggable) score += 30
            if (allowsBackup) score += 10
            if (isTestOnly) score += 10
            if (minSdkVersion in 1 until 23) score += 10
            score += minOf(dangerousPermissions.size * 3, 15)
            val exportedCount = exportedActivities.size + exportedServices.size +
                exportedReceivers.size + exportedProviders.size
            score += minOf(exportedCount * 2, 10)
            if (!networkSecurityConfigPresent) score += 5
            return minOf(score, 100)
        }

    val riskLevel: RiskLevel
        get() = when {
            riskScore >= 40 -> RiskLevel.HIGH
            riskScore >= 20 -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }

    /** Human-readable list of all findings for display in the detail view. */
    fun findings(): List<String> {
        val items = mutableListOf<String>()
        if (isDebuggable) items.add("⚠️ App is debuggable")
        if (allowsBackup) items.add("⚠️ Backup is allowed (android:allowBackup=true)")
        if (isTestOnly) items.add("⚠️ App is marked as test-only")
        if (minSdkVersion in 1 until 23) {
            items.add("⚠️ Min SDK $minSdkVersion is below Android 6.0 (API 23)")
        }
        if (dangerousPermissions.isNotEmpty()) {
            items.add("⚠️ Requests ${dangerousPermissions.size} dangerous permission(s):")
            dangerousPermissions.forEach { items.add("   • $it") }
        }
        if (exportedActivities.isNotEmpty()) {
            items.add("⚠️ ${exportedActivities.size} exported Activity(ies):")
            exportedActivities.forEach { items.add("   • $it") }
        }
        if (exportedServices.isNotEmpty()) {
            items.add("⚠️ ${exportedServices.size} exported Service(s):")
            exportedServices.forEach { items.add("   • $it") }
        }
        if (exportedReceivers.isNotEmpty()) {
            items.add("⚠️ ${exportedReceivers.size} exported BroadcastReceiver(s):")
            exportedReceivers.forEach { items.add("   • $it") }
        }
        if (exportedProviders.isNotEmpty()) {
            items.add("⚠️ ${exportedProviders.size} exported ContentProvider(s):")
            exportedProviders.forEach { items.add("   • $it") }
        }
        if (!networkSecurityConfigPresent) {
            items.add("ℹ️ No custom Network Security Config defined")
        }
        if (items.isEmpty()) {
            items.add("✅ No obvious security issues found")
        }
        return items
    }
}
