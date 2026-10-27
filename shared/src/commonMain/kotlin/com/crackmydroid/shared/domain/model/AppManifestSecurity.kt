package com.crackmydroid.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class AppComponentType {
    SERVICE,
    RECEIVER,
    PROVIDER
}

@Serializable
data class ExposedComponentInfo(
    val type: AppComponentType,
    val name: String,
    val exported: Boolean,
    val permission: String? = null
)

@Serializable
data class AppManifestSecurity(
    val appLabel: String,
    val packageName: String,
    val debuggable: Boolean,
    val allowBackup: Boolean,
    val hasCustomBackupAgent: Boolean = false,
    val usesCleartextTraffic: Boolean = false,
    val testOnly: Boolean = false,
    val targetSdkVersion: Int? = null,
    val minSdkVersion: Int? = null,
    val debugCertSigned: Boolean = false,
    val exposedComponents: List<ExposedComponentInfo> = emptyList(),
    val supportsComponentAnalysis: Boolean = true
) {
    val unprotectedExportedComponents: List<ExposedComponentInfo>
        get() = exposedComponents.filter { it.exported && it.permission.isNullOrBlank() }
}

@Serializable
enum class ManifestRiskLevel {
    Low,
    Medium,
    High,
    Critical
}

@Serializable
data class ManifestSecurityFinding(
    val level: ManifestRiskLevel,
    val title: String,
    val detail: String
)

@Serializable
data class ManifestSecurityReport(
    val info: AppManifestSecurity,
    val level: ManifestRiskLevel,
    val score: Int,
    val findings: List<ManifestSecurityFinding>
)

object ManifestSecurityAnalyzer {
    fun analyze(info: AppManifestSecurity): ManifestSecurityReport {
        val findings = mutableListOf<ManifestSecurityFinding>()

        if (info.debuggable) {
            findings += ManifestSecurityFinding(
                level = ManifestRiskLevel.Critical,
                title = "App debuggable in produzione",
                detail = "android:debuggable=true consente di attaccare un debugger, dumpare memoria ed eseguire codice nel processo dell'app."
            )
        }

        if (info.testOnly) {
            findings += ManifestSecurityFinding(
                level = ManifestRiskLevel.High,
                title = "Package testOnly",
                detail = "L'app e' marcata testOnly: verifica che non sia una build di sviluppo esposta per errore."
            )
        }

        if (info.allowBackup) {
            if (info.hasCustomBackupAgent) {
                findings += ManifestSecurityFinding(
                    level = ManifestRiskLevel.Medium,
                    title = "Backup abilitato con BackupAgent custom",
                    detail = "allowBackup=true con BackupAgent dedicato: verifica comunque quali dati vengono incluso nel backup."
                )
            } else {
                findings += ManifestSecurityFinding(
                    level = ManifestRiskLevel.High,
                    title = "Backup dati abilitato (allowBackup)",
                    detail = "allowBackup=true senza BackupAgent custom: dati privati estraibili via 'adb backup' anche senza root."
                )
            }
        }

        if (info.usesCleartextTraffic) {
            findings += ManifestSecurityFinding(
                level = ManifestRiskLevel.Medium,
                title = "Traffico cleartext consentito",
                detail = "usesCleartextTraffic=true: possibile downgrade HTTP e MITM su reti non trusted."
            )
        }

        if (info.debugCertSigned) {
            findings += ManifestSecurityFinding(
                level = ManifestRiskLevel.High,
                title = "Firmata con certificato di debug",
                detail = "L'APK risulta firmato con il certificato di debug standard: build non da rilasciare in produzione."
            )
        }

        val unprotected = info.unprotectedExportedComponents
        if (unprotected.isNotEmpty()) {
            val providers = unprotected.count { it.type == AppComponentType.PROVIDER }
            val services = unprotected.count { it.type == AppComponentType.SERVICE }
            val receivers = unprotected.count { it.type == AppComponentType.RECEIVER }
            if (providers > 0) {
                findings += ManifestSecurityFinding(
                    level = ManifestRiskLevel.Critical,
                    title = "Content provider esportati senza permesso ($providers)",
                    detail = "Provider esportati e non protetti da permission possono esporre dati o SQL injection: ${unprotected.filter { it.type == AppComponentType.PROVIDER }.take(3).joinToString(", ") { it.name }}."
                )
            }
            if (services > 0) {
                findings += ManifestSecurityFinding(
                    level = ManifestRiskLevel.High,
                    title = "Service esportati senza permesso ($services)",
                    detail = "Service raggiungibili da altre app senza controllo permessi: ${unprotected.filter { it.type == AppComponentType.SERVICE }.take(3).joinToString(", ") { it.name }}."
                )
            }
            if (receivers > 0) {
                findings += ManifestSecurityFinding(
                    level = ManifestRiskLevel.Medium,
                    title = "BroadcastReceiver esportati senza permesso ($receivers)",
                    detail = "Receiver che accettano broadcast esterni senza permission: ${unprotected.filter { it.type == AppComponentType.RECEIVER }.take(3).joinToString(", ") { it.name }}."
                )
            }
        } else if (!info.supportsComponentAnalysis) {
            findings += ManifestSecurityFinding(
                level = ManifestRiskLevel.Low,
                title = "Analisi componenti non disponibile",
                detail = "Superficie service/receiver/provider non analizzabile in questa modalita' (richiede scansione diretta sul device Android)."
            )
        }

        val sorted = findings.sortedByDescending { weight(it.level) }
        val score = sorted.sumOf { weight(it.level) }
        val level = sorted.maxByOrNull { weight(it.level) }?.level ?: ManifestRiskLevel.Low
        return ManifestSecurityReport(info = info, level = level, score = score, findings = sorted)
    }

    fun formatLevel(level: ManifestRiskLevel): String = when (level) {
        ManifestRiskLevel.Low -> "BASSA"
        ManifestRiskLevel.Medium -> "MEDIA"
        ManifestRiskLevel.High -> "ALTA"
        ManifestRiskLevel.Critical -> "CRITICA"
    }

    private fun weight(level: ManifestRiskLevel): Int = when (level) {
        ManifestRiskLevel.Low -> 0
        ManifestRiskLevel.Medium -> 1
        ManifestRiskLevel.High -> 3
        ManifestRiskLevel.Critical -> 5
    }
}
