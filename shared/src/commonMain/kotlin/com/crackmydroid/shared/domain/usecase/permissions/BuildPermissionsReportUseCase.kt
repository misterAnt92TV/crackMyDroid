package com.crackmydroid.shared.domain.usecase.permissions

import co.touchlab.kermit.Logger
import com.crackmydroid.shared.AppLogger
import com.crackmydroid.shared.domain.model.PermissionRiskLevel
import com.crackmydroid.shared.domain.model.PermissionRiskReport
import com.crackmydroid.shared.domain.model.ReportLabel
import com.crackmydroid.shared.domain.model.UseCaseReportText
import com.crackmydroid.shared.domain.model.app.AppPermissionEntry
import com.crackmydroid.shared.domain.usecase.BaseUseCase
import com.crackmydroid.shared.util.AppDispatchers
import com.crackmydroid.shared.util.DefaultAppDispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

class BuildPermissionsReportUseCase(
    private val analyzePermissionRisks: AnalyzePermissionRisksUseCase,
    dispatchers: AppDispatchers = DefaultAppDispatchers,
    logger: Logger = AppLogger.logger,
) : BaseUseCase(TAG, dispatcher = dispatchers.default, logger = logger) {
    private companion object {
        private const val TAG = "BuildPermissionsReportUseCase"
    }

    suspend operator fun invoke(
        apps: List<AppPermissionEntry>,
        riskReports: List<PermissionRiskReport>,
        text: UseCaseReportText,
    ): Result<String> = execute {
        buildString {
            val context = currentCoroutineContext()
            val riskByPackage = riskReports.associateBy {
                context.ensureActive()
                it.packageName
            }
            val criticalTotal = riskReports.sumOf { context.ensureActive(); it.criticalCount }
            val highTotal = riskReports.sumOf { context.ensureActive(); it.highCount }
            val mediumTotal = riskReports.sumOf { context.ensureActive(); it.mediumCount }
            val sortedApps = apps.map { context.ensureActive(); it }.sortedWith(
                compareByDescending<AppPermissionEntry> {
                    context.ensureActive()
                    riskByPackage[it.packageName]?.score ?: 0
                }
                    .thenBy { it.appLabel.lowercase() },
            )

            appendLine(text.label(ReportLabel.PERMISSIONS_REPORT_TITLE))
            appendLine("==============================================")
            appendLine(
                text.permissionOverview(
                    appCount = apps.size,
                    permissionCount = apps.sumOf { context.ensureActive(); it.permissions.size },
                    appsWithRisksCount = riskReports.size,
                ),
            )
            appendLine(text.permissionRiskCounts(criticalTotal, highTotal, mediumTotal))
            appendLine(
                "${text.label(ReportLabel.SEVERITY_LEGEND)}: " +
                    "${text.riskLevel(PermissionRiskLevel.Critical)} > " +
                    "${text.riskLevel(PermissionRiskLevel.High)} > " +
                    text.riskLevel(PermissionRiskLevel.Medium),
            )
            appendLine(text.label(ReportLabel.CAUTION_NOTE))
            appendLine()

            if (apps.isEmpty()) {
                appendLine(text.noDataAvailable())
                return@buildString
            }

            sortedApps.forEachIndexed { index, app ->
                context.ensureActive()
                appendLine("--------------------------------------------------")
                appendLine(text.permissionAppHeading(index + 1, app.appLabel))
                appendLine("${text.label(ReportLabel.PACKAGE)}: ${app.packageName}")
                appendLine(text.declaredPermissionCount(app.permissions.size))
                val risk = riskByPackage[app.packageName]
                if (risk == null) {
                    appendLine("${text.label(ReportLabel.RISK_ANALYSIS)}: ${text.noSensitiveBehavior()}")
                } else {
                    appendLine(
                        "${text.label(ReportLabel.RISK_ANALYSIS)}: ${
                            text.permissionRiskOverview(
                                score = risk.score,
                                findingCount = risk.findings.size,
                                criticalCount = risk.criticalCount,
                                highCount = risk.highCount,
                                mediumCount = risk.mediumCount,
                            )
                        }",
                    )
                    appendLine(text.label(ReportLabel.FINDING_DETAILS) + ":")
                    risk.findings.forEach { finding ->
                        context.ensureActive()
                        appendLine("- [${text.riskLevel(finding.level)}] ${text.permissionTitle(finding)}")
                        appendLine("  ${text.permissionReason(finding)}")
                    }
                }
                appendLine("${text.label(ReportLabel.PERMISSIONS)}:")
                if (app.permissions.isEmpty()) {
                    appendLine("- ${text.noDeclaredPermissions()}")
                } else {
                    app.permissions.map { context.ensureActive(); it }.sorted().forEach { perm ->
                        context.ensureActive()
                        appendLine("- $perm")
                    }
                }
                appendLine()
            }
        }
    }

    suspend fun single(
        app: AppPermissionEntry,
        riskReport: PermissionRiskReport?,
        text: UseCaseReportText,
    ): Result<String> = execute("single") {
        buildString {
            val context = currentCoroutineContext()
            appendLine(text.label(ReportLabel.SINGLE_PERMISSIONS_REPORT_TITLE))
            appendLine("==============================================")
            appendLine("${text.label(ReportLabel.APP)}: ${app.appLabel}")
            appendLine("${text.label(ReportLabel.PACKAGE)}: ${app.packageName}")
            appendLine(text.declaredPermissionCount(app.permissions.size))
            appendLine(
                "${text.label(ReportLabel.SEVERITY_LEGEND)}: " +
                    "${text.riskLevel(PermissionRiskLevel.Critical)} > " +
                    "${text.riskLevel(PermissionRiskLevel.High)} > " +
                    text.riskLevel(PermissionRiskLevel.Medium),
            )
            appendLine()
            appendLine(text.label(ReportLabel.RISK_ANALYSIS).uppercase())
            appendLine("----------------------------------------------")
            if (riskReport == null) {
                appendLine(text.noRiskyPermissions())
            } else {
                appendLine("${text.label(ReportLabel.SCORE)}: ${riskReport.score}")
                appendLine(
                    text.permissionRiskOverview(
                        score = riskReport.score,
                        findingCount = riskReport.findings.size,
                        criticalCount = riskReport.criticalCount,
                        highCount = riskReport.highCount,
                        mediumCount = riskReport.mediumCount,
                    ),
                )
                riskReport.findings.forEachIndexed { index, finding ->
                    context.ensureActive()
                    appendLine(text.permissionFindingHeading(index + 1, finding))
                    appendLine("   ${text.permissionReason(finding)}")
                }
            }
            appendLine()
            appendLine(text.label(ReportLabel.PERMISSIONS).uppercase())
            appendLine("----------------------------------------------")
            if (app.permissions.isEmpty()) {
                appendLine(text.noDeclaredPermissions())
            } else {
                app.permissions.map { context.ensureActive(); it }.sorted().forEachIndexed { index, permission ->
                    context.ensureActive()
                    appendLine("${index + 1}. $permission")
                }
            }
        }
    }

    fun singleFileName(packageName: String): String {
        val safe = packageName
            .replace(Regex("[^A-Za-z0-9._-]"), "_")
            .replace('.', '_')
        return "permissions_$safe.txt"
    }
}
