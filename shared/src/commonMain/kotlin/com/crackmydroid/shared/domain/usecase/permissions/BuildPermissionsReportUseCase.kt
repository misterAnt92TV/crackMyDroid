package com.crackmydroid.shared.domain.usecase.permissions

import com.crackmydroid.shared.domain.model.AppPermissionEntry
import com.crackmydroid.shared.domain.model.PermissionRiskReport

class BuildPermissionsReportUseCase(
    private val analyzePermissionRisks: AnalyzePermissionRisksUseCase
) {
    operator fun invoke(
        apps: List<AppPermissionEntry>,
        riskReports: List<PermissionRiskReport>
    ): String = buildString {
        val riskByPackage = riskReports.associateBy { it.packageName }
        val criticalTotal = riskReports.sumOf { it.criticalCount }
        val highTotal = riskReports.sumOf { it.highCount }
        val mediumTotal = riskReports.sumOf { it.mediumCount }
        val sortedApps = apps.sortedWith(
            compareByDescending<AppPermissionEntry> { riskByPackage[it.packageName]?.score ?: 0 }
                .thenBy { it.appLabel.lowercase() }
        )

        appendLine("REPORT PERMESSI APP INSTALLATE")
        appendLine("==============================================")
        appendLine("Totale app analizzate: ${apps.size}")
        appendLine("Totale permessi trovati: ${apps.sumOf { it.permissions.size }}")
        appendLine("App con rischi rilevati: ${riskReports.size}")
        appendLine("Rischi -> Critici: $criticalTotal | Alti: $highTotal | Medi: $mediumTotal")
        appendLine("Legenda severita: CRITICO > ALTO > MEDIO")
        appendLine("Nota: il report evidenzia indicatori di rischio, non conferme definitive di malware.")
        appendLine()

        if (apps.isEmpty()) {
            appendLine("Nessun dato disponibile.")
            return@buildString
        }

        sortedApps.forEachIndexed { index, app ->
            appendLine("--------------------------------------------------")
            appendLine("${index + 1}) ${app.appLabel}")
            appendLine("Package: ${app.packageName}")
            appendLine("Permessi dichiarati: ${app.permissions.size}")
            val risk = riskByPackage[app.packageName]
            if (risk == null) {
                appendLine("Analisi rischio: nessun comportamento sensibile rilevato.")
            } else {
                appendLine(
                    "Analisi rischio: score ${risk.score} • ${risk.findings.size} finding " +
                        "(Critici ${risk.criticalCount}, Alti ${risk.highCount}, Medi ${risk.mediumCount})"
                )
                appendLine("Dettaglio finding:")
                risk.findings.forEach { finding ->
                    appendLine("- [${analyzePermissionRisks.riskLabel(finding.level)}] ${finding.title}")
                    appendLine("  ${finding.reason}")
                }
            }
            appendLine("Permessi:")
            if (app.permissions.isEmpty()) {
                appendLine("- Nessun permesso dichiarato")
            } else {
                app.permissions.sorted().forEach { perm ->
                    appendLine("- $perm")
                }
            }
            appendLine()
        }
    }

    fun single(
        app: AppPermissionEntry,
        riskReport: PermissionRiskReport?
    ): String = buildString {
        appendLine("REPORT PERMESSI APP")
        appendLine("==============================================")
        appendLine("App: ${app.appLabel}")
        appendLine("Package: ${app.packageName}")
        appendLine("Totale permessi: ${app.permissions.size}")
        appendLine("Legenda severita: CRITICO > ALTO > MEDIO")
        appendLine()
        appendLine("ANALISI RISCHIO")
        appendLine("----------------------------------------------")
        if (riskReport == null) {
            appendLine("Nessun permesso pericoloso/malevolo rilevato con le regole correnti.")
        } else {
            appendLine("Score rischio: ${riskReport.score}")
            appendLine(
                "Finding: ${riskReport.findings.size} " +
                    "(Critici ${riskReport.criticalCount}, Alti ${riskReport.highCount}, Medi ${riskReport.mediumCount})"
            )
            riskReport.findings.forEachIndexed { index, finding ->
                appendLine("${index + 1}. [${analyzePermissionRisks.riskLabel(finding.level)}] ${finding.title}")
                appendLine("   ${finding.reason}")
            }
        }
        appendLine()
        appendLine("ELENCO PERMESSI")
        appendLine("----------------------------------------------")
        if (app.permissions.isEmpty()) {
            appendLine("Nessun permesso dichiarato.")
        } else {
            app.permissions.sorted().forEachIndexed { index, permission ->
                appendLine("${index + 1}. $permission")
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
