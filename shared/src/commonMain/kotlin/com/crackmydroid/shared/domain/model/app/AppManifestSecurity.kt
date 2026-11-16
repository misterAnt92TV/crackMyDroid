package com.crackmydroid.shared.domain.model.app

import kotlinx.serialization.Serializable

@Serializable
enum class AppComponentType {
    ACTIVITY,
    SERVICE,
    RECEIVER,
    PROVIDER,
}

@Serializable
data class ExposedComponentInfo(
    val type: AppComponentType,
    val name: String,
    val exported: Boolean,
    val permission: String? = null,
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
    val supportsComponentAnalysis: Boolean = true,
) {
    val unprotectedExportedComponents: List<ExposedComponentInfo>
        get() = exposedComponents.filter { it.exported && it.permission.isNullOrBlank() }
}

@Serializable
enum class ManifestRiskLevel {
    Low,
    Medium,
    High,
    Critical,
}

@Serializable
data class ManifestSecurityFinding(
    val level: ManifestRiskLevel,
    val title: String,
    val detail: String,
    val evidence: String? = null,
    val remediation: String? = null,
)

@Serializable
data class ManifestSecurityReport(
    val info: AppManifestSecurity,
    val level: ManifestRiskLevel,
    val score: Int,
    val findings: List<ManifestSecurityFinding>,
)

object ManifestSecurityAnalyzer {
    fun analyze(info: AppManifestSecurity): ManifestSecurityReport {
        val findings = mutableListOf<ManifestSecurityFinding>()

        if (info.debuggable) {
            findings += ManifestSecurityFinding(
                level = ManifestRiskLevel.Critical,
                title = "App debuggable in produzione",
                detail = "android:debuggable=true consente di attaccare un debugger, " +
                    "dumpare memoria ed eseguire codice nel processo dell'app.",
                evidence = "android:debuggable=\"true\"",
                remediation = "Imposta android:debuggable=\"false\" nei build di release e usa BuildConfig.DEBUG per i soli log diagnostici di sviluppo.",
            )
        }

        if (info.testOnly) {
            findings += ManifestSecurityFinding(
                level = ManifestRiskLevel.High,
                title = "Package testOnly",
                detail = "L'app e' marcata testOnly: verifica che non sia una build di sviluppo esposta per errore.",
                evidence = "android:testOnly=\"true\"",
                remediation = "Rimuovi android:testOnly=\"true\" dal manifest o dal build.gradle prima della distribuzione.",
            )
        }

        if (info.allowBackup) {
            if (info.hasCustomBackupAgent) {
                findings += ManifestSecurityFinding(
                    level = ManifestRiskLevel.Medium,
                    title = "Backup abilitato con BackupAgent custom",
                    detail = "allowBackup=true con BackupAgent dedicato: " +
                        "verifica comunque quali dati vengono inclusi nel backup cloud e D2D.",
                    evidence = "android:allowBackup=\"true\", backupAgent=\"custom\"",
                    remediation = "Definisci regole esplicite in dataExtractionRules (Android 12+) e fullBackupContent per escludere file privati.",
                )
            } else {
                val detailText = if (info.debuggable) {
                    "allowBackup=true con app debuggable: dati privati estraibili via 'adb backup' anche senza root e inclusi nei backup cloud/D2D."
                } else {
                    "allowBackup=true senza BackupAgent custom: dati privati inclusi nei backup cloud e migrazioni D2D (disabilitato da 'adb backup' su Android 12+ per app non debuggable)."
                }
                findings += ManifestSecurityFinding(
                    level = ManifestRiskLevel.High,
                    title = "Backup dati abilitato (allowBackup)",
                    detail = detailText,
                    evidence = "android:allowBackup=\"true\" (targetSdk=${info.targetSdkVersion ?: "n/d"})",
                    remediation = "Configura android:dataExtractionRules (API 31+) e android:fullBackupContent per escludere SharedPreferences, database e token, oppure imposta android:allowBackup=\"false\".",
                )
            }
        }

        info.targetSdkVersion?.let { targetSdk ->
            if (targetSdk < 28) {
                findings += ManifestSecurityFinding(
                    level = ManifestRiskLevel.High,
                    title = "Target SDK obsoleto ($targetSdk < 28)",
                    detail = "L'app punta a un target SDK inferiore ad Android 9 (API 28): il traffico HTTP in chiaro (cleartext) e' consentito per impostazione predefinita della piattaforma.",
                    evidence = "android:targetSdkVersion=\"$targetSdk\"",
                    remediation = "Aggiorna targetSdkVersion ad almeno API 34/35 per abilitare le protezioni di sicurezza moderne di Android.",
                )
            } else if (targetSdk < 31) {
                findings += ManifestSecurityFinding(
                    level = ManifestRiskLevel.Medium,
                    title = "Target SDK inferiore ad Android 12 ($targetSdk < 31)",
                    detail = "Sotto API 31 le componenti con intent-filter non richiedevano android:exported esplicito, aumentando il rischio di esposizione involontaria.",
                    evidence = "android:targetSdkVersion=\"$targetSdk\"",
                    remediation = "Aggiorna targetSdkVersion ad API 31+ e dichiara esplicitamente android:exported su ogni componente.",
                )
            }
        }

        if (info.usesCleartextTraffic) {
            findings += ManifestSecurityFinding(
                level = ManifestRiskLevel.Medium,
                title = "Traffico cleartext consentito",
                detail = "usesCleartextTraffic=true: possibile downgrade HTTP e MITM su reti non trusted.",
                evidence = "android:usesCleartextTraffic=\"true\"",
                remediation = "Imposta android:usesCleartextTraffic=\"false\" e adotta Network Security Config per imporre connessioni TLS.",
            )
        }

        if (info.debugCertSigned) {
            findings += ManifestSecurityFinding(
                level = ManifestRiskLevel.High,
                title = "Firmata con certificato di debug",
                detail = "L'APK risulta firmato con il certificato di debug standard: " +
                    "build non da rilasciare in produzione.",
                evidence = "CN=Android Debug",
                remediation = "Genera un keystore di produzione protetto e firma l'APK con chiave privata dedicata.",
            )
        }

        val unprotected = info.unprotectedExportedComponents
        if (unprotected.isNotEmpty()) {
            val providers = unprotected.count { it.type == AppComponentType.PROVIDER }
            val activities = unprotected.count { it.type == AppComponentType.ACTIVITY }
            val services = unprotected.count { it.type == AppComponentType.SERVICE }
            val receivers = unprotected.count { it.type == AppComponentType.RECEIVER }
            if (providers > 0) {
                findings += ManifestSecurityFinding(
                    level = ManifestRiskLevel.Critical,
                    title = "Content provider esportati senza permesso ($providers)",
                    detail = "Provider esportati e non protetti da permission possono esporre dati o SQL injection: ${
                        unprotected.filter { it.type == AppComponentType.PROVIDER }.take(3)
                            .joinToString(", ") { it.name }
                    }.",
                    evidence = "$providers provider esportati non protetti",
                    remediation = "Aggiungi android:permission, android:readPermission/writePermission o imposta android:exported=\"false\".",
                )
            }
            if (activities > 0) {
                findings += ManifestSecurityFinding(
                    level = ManifestRiskLevel.High,
                    title = "Activity esportate senza permesso ($activities)",
                    detail = "Activity esportate e avviabili da qualsiasi app esterna: ${
                        unprotected.filter { it.type == AppComponentType.ACTIVITY }.take(3)
                            .joinToString(", ") { it.name }
                    }.",
                    evidence = "$activities activity esportate non protette",
                    remediation = "Imposta android:exported=\"false\" per le activity interne oppure proteggile con android:permission.",
                )
            }
            if (services > 0) {
                findings += ManifestSecurityFinding(
                    level = ManifestRiskLevel.High,
                    title = "Service esportati senza permesso ($services)",
                    detail = "Service raggiungibili da altre app senza controllo permessi: ${
                        unprotected.filter { it.type == AppComponentType.SERVICE }.take(3)
                            .joinToString(", ") { it.name }
                    }.",
                    evidence = "$services service esportati non protetti",
                    remediation = "Imposta android:exported=\"false\" per i service privati o proteggili con un permesso a livello signature.",
                )
            }
            if (receivers > 0) {
                findings += ManifestSecurityFinding(
                    level = ManifestRiskLevel.Medium,
                    title = "BroadcastReceiver esportati senza permesso ($receivers)",
                    detail = "Receiver che accettano broadcast esterni senza permission: ${
                        unprotected.filter { it.type == AppComponentType.RECEIVER }.take(3)
                            .joinToString(", ") { it.name }
                    }.",
                    evidence = "$receivers receiver esportati non protetti",
                    remediation = "Imposta android:exported=\"false\" se il broadcast e' interno o proteggi con android:permission.",
                )
            }
        } else if (!info.supportsComponentAnalysis) {
            findings += ManifestSecurityFinding(
                level = ManifestRiskLevel.Low,
                title = "Analisi componenti non disponibile",
                detail = "Superficie service/receiver/provider non analizzabile in questa modalita' (richiede scansione diretta sul device Android).",
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
