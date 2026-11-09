package com.crackmydroid.shared.i18n

import com.crackmydroid.shared.domain.model.PenTestActivityRisk
import com.crackmydroid.shared.domain.model.PenTestMessage
import com.crackmydroid.shared.domain.model.PenTestSurface
import com.crackmydroid.shared.domain.model.PermissionFindingKind
import com.crackmydroid.shared.domain.model.PermissionRiskFinding
import com.crackmydroid.shared.domain.model.PermissionRiskLevel
import com.crackmydroid.shared.domain.model.ReportLabel
import com.crackmydroid.shared.domain.model.UseCaseReportText
import com.crackmydroid.shared.domain.model.activity.ActivityIntentRiskLevel

class UseCaseTexts(languageCode: String) : UseCaseReportText {
    val languageCode: String = languageCode.lowercase().take(2).let {
        if (it in SUPPORTED_LANGUAGES) it else "en"
    }

    override fun label(label: ReportLabel): String = when (label) {
        ReportLabel.DEVICE_INFO_TITLE -> t("Device information", "Informazioni dispositivo", "Geräteinformationen", "Información del dispositivo", "Informations de l’appareil")
        ReportLabel.MANUFACTURER -> t("Manufacturer", "Produttore", "Hersteller", "Fabricante", "Fabricant")
        ReportLabel.MODEL -> t("Model", "Modello", "Modell", "Modelo", "Modèle")
        ReportLabel.BRAND -> t("Brand", "Brand", "Marke", "Marca", "Marque")
        ReportLabel.DEVICE -> t("Device", "Dispositivo", "Gerät", "Dispositivo", "Appareil")
        ReportLabel.PRODUCT -> t("Product", "Prodotto", "Produkt", "Producto", "Produit")
        ReportLabel.ANDROID -> t("Android", "Android", "Android", "Android", "Android")
        ReportLabel.SECURITY_PATCH -> t("Security patch", "Patch di sicurezza", "Sicherheitspatch", "Parche de seguridad", "Correctif de sécurité")
        ReportLabel.BUILD_ID -> t("Build ID", "Build ID", "Build-ID", "Build ID", "ID de build")
        ReportLabel.BUILD_DISPLAY -> t("Build display", "Build display", "Build-Anzeige", "Build display", "Affichage de build")
        ReportLabel.FINGERPRINT -> t("Fingerprint", "Fingerprint", "Fingerprint", "Fingerprint", "Fingerprint")
        ReportLabel.RADIO -> t("Radio", "Radio", "Funkmodul", "Radio", "Radio")
        ReportLabel.KERNEL -> t("Kernel", "Kernel", "Kernel", "Kernel", "Kernel")
        ReportLabel.HARDWARE -> t("Hardware", "Hardware", "Hardware", "Hardware", "Matériel")
        ReportLabel.BOOTLOADER -> t("Bootloader", "Bootloader", "Bootloader", "Bootloader", "Bootloader")
        ReportLabel.BATTERY -> t("Battery", "Batteria", "Akku", "Batería", "Batterie")
        ReportLabel.NETWORK -> t("Network", "Rete", "Netzwerk", "Red", "Réseau")
        ReportLabel.TOTAL_RAM -> t("Total RAM", "RAM totale", "Gesamter RAM", "RAM total", "RAM totale")
        ReportLabel.FREE_RAM -> t("Free RAM", "RAM libera", "Freier RAM", "RAM libre", "RAM libre")
        ReportLabel.SCREEN -> t("Screen", "Schermo", "Bildschirm", "Pantalla", "Écran")
        ReportLabel.ADB_ENABLED -> t("ADB enabled", "ADB attivo", "ADB aktiviert", "ADB habilitado", "ADB activé")
        ReportLabel.DEVELOPER_OPTIONS -> t("Developer options", "Opzioni sviluppatore", "Entwickleroptionen", "Opciones de desarrollador", "Options pour les développeurs")
        ReportLabel.APP_DEBUGGABLE -> t("App debuggable", "App debuggabile", "App debuggable", "App depurable", "App débogable")
        ReportLabel.ROOTED -> t("Rooted", "Root rilevato", "Root erkannt", "Root detectado", "Root détecté")
        ReportLabel.ROOT_DETAILS -> t("Root details", "Dettagli root", "Root-Details", "Detalles de root", "Détails root")
        ReportLabel.IMEI -> t("IMEI", "IMEI", "IMEI", "IMEI", "IMEI")
        ReportLabel.SERIAL -> t("Serial", "Seriale", "Seriennummer", "Número de serie", "Numéro de série")
        ReportLabel.SYSTEM_PROPERTIES_SOURCE -> t("System properties source", "Origine delle system properties", "Quelle der Systemeigenschaften", "Origen de las propiedades del sistema", "Source des propriétés système")
        ReportLabel.SYSTEM_PROPERTIES_DUMP -> t("System properties dump", "Dump delle system properties", "Dump der Systemeigenschaften", "Volcado de propiedades del sistema", "Dump des propriétés système")
        ReportLabel.PERMISSIONS_REPORT_TITLE -> t("INSTALLED APP PERMISSIONS REPORT", "REPORT PERMESSI APP INSTALLATE", "BERECHTIGUNGSBERICHT INSTALLIERTER APPS", "INFORME DE PERMISOS DE APPS INSTALADAS", "RAPPORT DES AUTORISATIONS DES APPS INSTALLÉES")
        ReportLabel.SINGLE_PERMISSIONS_REPORT_TITLE -> t("APP PERMISSIONS REPORT", "REPORT PERMESSI APP", "APP-BERECHTIGUNGSBERICHT", "INFORME DE PERMISOS DE LA APP", "RAPPORT DES AUTORISATIONS DE L’APP")
        ReportLabel.APP -> t("App", "App", "App", "App", "App")
        ReportLabel.PACKAGE -> t("Package", "Package", "Paket", "Paquete", "Package")
        ReportLabel.PERMISSIONS_ANALYZED -> t("Apps analyzed", "App analizzate", "Analysierte Apps", "Apps analizadas", "Apps analysées")
        ReportLabel.PERMISSIONS_FOUND -> t("Permissions found", "Permessi trovati", "Gefundene Berechtigungen", "Permisos encontrados", "Autorisations trouvées")
        ReportLabel.APPS_WITH_RISKS -> t("Apps with detected risks", "App con rischi rilevati", "Apps mit erkannten Risiken", "Apps con riesgos detectados", "Apps présentant des risques détectés")
        ReportLabel.RISK_COUNTS -> t("Risks", "Rischi", "Risiken", "Riesgos", "Risques")
        ReportLabel.SEVERITY_LEGEND -> t("Severity", "Severità", "Schweregrad", "Gravedad", "Niveau de risque")
        ReportLabel.CAUTION_NOTE -> t("This report highlights risk indicators; it does not confirm malware.", "Il report evidenzia indicatori di rischio, non conferme definitive di malware.", "Dieser Bericht weist auf Risikofaktoren hin und bestätigt keine Malware.", "El informe señala indicadores de riesgo; no confirma la presencia de malware.", "Ce rapport signale des indicateurs de risque, sans confirmer la présence de malware.")
        ReportLabel.NO_DATA_AVAILABLE -> t("No data available.", "Nessun dato disponibile.", "Keine Daten verfügbar.", "No hay datos disponibles.", "Aucune donnée disponible.")
        ReportLabel.DECLARED_PERMISSIONS -> t("Declared permissions", "Permessi dichiarati", "Deklarierte Berechtigungen", "Permisos declarados", "Autorisations déclarées")
        ReportLabel.RISK_ANALYSIS -> t("Risk analysis", "Analisi rischio", "Risikoanalyse", "Análisis de riesgos", "Analyse des risques")
        ReportLabel.SCORE -> t("Score", "Score", "Score", "Score", "Score")
        ReportLabel.FINDINGS -> t("Findings", "Finding", "Findings", "Findings", "Findings")
        ReportLabel.FINDING_DETAILS -> t("Finding details", "Dettaglio finding", "Finding-Details", "Detalles de findings", "Détails des findings")
        ReportLabel.PERMISSIONS -> t("Permissions", "Permessi", "Berechtigungen", "Permisos", "Autorisations")
        ReportLabel.NO_SENSITIVE_BEHAVIOR -> noSensitiveBehavior()
        ReportLabel.NO_RISKY_PERMISSIONS -> noRiskyPermissions()
        ReportLabel.NO_PERMISSION_DECLARED -> noDeclaredPermissions()
    }

    override fun permissionTitle(finding: PermissionRiskFinding): String = when (finding.kind) {
        PermissionFindingKind.ACCESSIBILITY_SERVICE,
        PermissionFindingKind.OVERLAY_ACCESS,
        PermissionFindingKind.SMS_MMS_ACCESS,
        PermissionFindingKind.APK_INSTALLATION,
        PermissionFindingKind.BROAD_STORAGE_ACCESS,
        PermissionFindingKind.INSTALLED_APP_INVENTORY,
        PermissionFindingKind.PHONE_AND_CONTACTS_ACCESS,
        PermissionFindingKind.LOCATION_ACCESS,
        PermissionFindingKind.MICROPHONE_CAMERA_ACCESS,
        PermissionFindingKind.SENSITIVE_DATA_ACCESS -> finding.key
        PermissionFindingKind.OVERLAY_ACCESSIBILITY_PATTERN ->
            t("Suspicious pattern: overlay + accessibility", "Pattern sospetto: overlay + accessibility", "Verdächtiges Muster: Overlay + Accessibility", "Patrón sospechoso: overlay + accessibility", "Schéma suspect : overlay + accessibility")
        PermissionFindingKind.INSTALL_QUERY_PATTERN ->
            t("Suspicious pattern: installation + app inventory", "Pattern sospetto: installazione + inventario app", "Verdächtiges Muster: Installation + App-Inventar", "Patrón sospechoso: instalación + inventario de apps", "Schéma suspect : installation + inventaire des apps")
        PermissionFindingKind.BOOT_SMS_PATTERN ->
            t("Suspicious pattern: persistence + SMS", "Pattern sospetto: persistenza + SMS", "Verdächtiges Muster: Persistenz + SMS", "Patrón sospechoso: persistencia + SMS", "Schéma suspect : persistance + SMS")
        PermissionFindingKind.BACKGROUND_LOCATION_CAPTURE_PATTERN ->
            t("Suspicious pattern: background location + capture", "Pattern sospetto: background location + acquisizione", "Verdächtiges Muster: Hintergrundstandort + Aufzeichnung", "Patrón sospechoso: ubicación en segundo plano + captura", "Schéma suspect : localisation en arrière-plan + capture")
    }

    override fun permissionReason(finding: PermissionRiskFinding): String = when (finding.kind) {
        PermissionFindingKind.ACCESSIBILITY_SERVICE ->
            t("Can read on-screen content and simulate user interactions.", "Può leggere i contenuti sullo schermo e simulare le interazioni dell’utente.", "Kann Bildschirminhalte lesen und Nutzerinteraktionen simulieren.", "Puede leer el contenido de la pantalla y simular interacciones del usuario.", "Peut lire le contenu à l’écran et simuler les interactions de l’utilisateur.")
        PermissionFindingKind.OVERLAY_ACCESS ->
            t("Allows overlays above other apps, with phishing or tapjacking risk.", "Consente overlay sopra altre app, con rischio di phishing o tapjacking.", "Ermöglicht Overlays über anderen Apps und birgt Phishing- oder Tapjacking-Risiken.", "Permite mostrar overlays sobre otras apps, con riesgo de phishing o tapjacking.", "Permet d’afficher des overlays au-dessus d’autres apps, avec un risque de phishing ou de tapjacking.")
        PermissionFindingKind.SMS_MMS_ACCESS ->
            t("Access to SMS/MMS channels may enable data exfiltration or fraud.", "L’accesso ai canali SMS/MMS può consentire esfiltrazione di dati o frodi.", "Der Zugriff auf SMS/MMS-Kanäle kann Datenabfluss oder Betrug ermöglichen.", "El acceso a canales SMS/MMS puede facilitar la exfiltración de datos o el fraude.", "L’accès aux canaux SMS/MMS peut permettre l’exfiltration de données ou la fraude.")
        PermissionFindingKind.APK_INSTALLATION ->
            t("Can request installation of APKs outside the Play Store.", "Può richiedere l’installazione di APK al di fuori del Play Store.", "Kann die Installation von APKs außerhalb des Play Store anfordern.", "Puede solicitar la instalación de APK fuera de Play Store.", "Peut demander l’installation d’APK en dehors du Play Store.")
        PermissionFindingKind.BROAD_STORAGE_ACCESS ->
            t("Provides broad access to device storage.", "Fornisce un accesso esteso allo storage del dispositivo.", "Ermöglicht weitreichenden Zugriff auf den Gerätespeicher.", "Proporciona acceso amplio al almacenamiento del dispositivo.", "Accorde un accès étendu au stockage de l’appareil.")
        PermissionFindingKind.INSTALLED_APP_INVENTORY ->
            t("Can inventory all installed apps.", "Può creare un inventario completo delle app installate.", "Kann alle installierten Apps erfassen.", "Puede inventariar todas las apps instaladas.", "Peut établir l’inventaire de toutes les apps installées.")
        PermissionFindingKind.PHONE_AND_CONTACTS_ACCESS ->
            t("Access to sensitive phone data or contacts.", "Accesso a dati telefonici o rubriche sensibili.", "Zugriff auf sensible Telefondaten oder Kontakte.", "Acceso a datos telefónicos o contactos sensibles.", "Accès à des données téléphoniques ou contacts sensibles.")
        PermissionFindingKind.LOCATION_ACCESS ->
            t("Precise location tracking, including in the background.", "Tracciamento preciso della posizione, anche in background.", "Präzise Standortverfolgung, auch im Hintergrund.", "Seguimiento preciso de la ubicación, incluso en segundo plano.", "Suivi précis de la localisation, y compris en arrière-plan.")
        PermissionFindingKind.MICROPHONE_CAMERA_ACCESS ->
            t("Microphone or camera access may capture sensitive information.", "L’accesso a microfono o camera può acquisire informazioni sensibili.", "Der Zugriff auf Mikrofon oder Kamera kann sensible Informationen erfassen.", "El acceso al micrófono o la cámara puede capturar información sensible.", "L’accès au microphone ou à la caméra peut recueillir des informations sensibles.")
        PermissionFindingKind.SENSITIVE_DATA_ACCESS ->
            t("Sensitive permission to review in the app’s functional context.", "Permesso sensibile da verificare nel contesto funzionale dell’app.", "Sensible Berechtigung, die im funktionalen Kontext der App geprüft werden sollte.", "Permiso sensible que debe revisarse en el contexto funcional de la app.", "Autorisation sensible à vérifier dans le contexte fonctionnel de l’app.")
        PermissionFindingKind.OVERLAY_ACCESSIBILITY_PATTERN ->
            t("This combination can overlay UI and intercept user interactions.", "La combinazione può sovrapporre UI e intercettare le interazioni dell’utente.", "Diese Kombination kann UI überlagern und Nutzerinteraktionen abfangen.", "Esta combinación puede superponer UI e interceptar interacciones del usuario.", "Cette combinaison peut superposer l’UI et intercepter les interactions de l’utilisateur.")
        PermissionFindingKind.INSTALL_QUERY_PATTERN ->
            t("May manage APKs and map apps present on the device.", "Può gestire APK e mappare le app presenti sul dispositivo.", "Kann APKs verwalten und installierte Apps auf dem Gerät erfassen.", "Puede gestionar APK y detectar las apps presentes en el dispositivo.", "Peut gérer des APK et répertorier les apps présentes sur l’appareil.")
        PermissionFindingKind.BOOT_SMS_PATTERN ->
            t("May restart at boot and interact with sensitive SMS channels.", "Può riattivarsi al boot e interagire con canali SMS sensibili.", "Kann beim Booten erneut starten und mit sensiblen SMS-Kanälen interagieren.", "Puede reactivarse al iniciar el dispositivo e interactuar con canales SMS sensibles.", "Peut se réactiver au démarrage et interagir avec des canaux SMS sensibles.")
        PermissionFindingKind.BACKGROUND_LOCATION_CAPTURE_PATTERN ->
            t("Continuous location access is combined with audio or video sensors.", "L’accesso continuo alla posizione è combinato con sensori audio o video.", "Kontinuierlicher Standortzugriff wird mit Audio- oder Videosensoren kombiniert.", "El acceso continuo a la ubicación se combina con sensores de audio o vídeo.", "L’accès continu à la localisation est associé à des capteurs audio ou vidéo.")
    }

    override fun riskLevel(level: PermissionRiskLevel): String = when (level) {
        PermissionRiskLevel.Critical -> t("CRITICAL", "CRITICO", "KRITISCH", "CRÍTICO", "CRITIQUE")
        PermissionRiskLevel.High -> t("HIGH", "ALTO", "HOCH", "ALTO", "ÉLEVÉ")
        PermissionRiskLevel.Medium -> t("MEDIUM", "MEDIO", "MITTEL", "MEDIO", "MOYEN")
    }

    override fun activityRiskLevel(level: ActivityIntentRiskLevel): String = when (level) {
        ActivityIntentRiskLevel.Low -> t("LOW", "BASSO", "NIEDRIG", "BAJO", "FAIBLE")
        ActivityIntentRiskLevel.Medium -> t("MEDIUM", "MEDIO", "MITTEL", "MEDIO", "MOYEN")
        ActivityIntentRiskLevel.High -> t("HIGH", "ALTO", "HOCH", "ALTO", "ÉLEVÉ")
        ActivityIntentRiskLevel.Critical -> t("CRITICAL", "CRITICO", "KRITISCH", "CRÍTICO", "CRITIQUE")
    }

    override fun penTest(message: PenTestMessage): String = when (message) {
        is PenTestMessage.PermissionRiskSummary ->
            t("Permission risk: ", "Rischio permessi: ", "Berechtigungsrisiko: ", "Riesgo de permisos: ", "Risque lié aux autorisations : ") +
                    riskSummary(message.score, message.criticalCount, message.highCount, message.mediumCount)
        is PenTestMessage.MissingPermissionData -> when (message.surface) {
            PenTestSurface.MANIFEST -> t(
                "Manifest flags could not be analyzed: permission data is unavailable.",
                "Impossibile analizzare i flag manifest: i dati dei permessi non sono disponibili.",
                "Manifest-Flags konnten nicht analysiert werden: Berechtigungsdaten sind nicht verfügbar.",
                "No se pudieron analizar los flags del manifest: los datos de permisos no están disponibles.",
                "Impossible d’analyser les flags du manifest : les données d’autorisations ne sont pas disponibles."
            )
            PenTestSurface.NETWORK -> t(
                "Network surface could not be analyzed: permission data is unavailable.",
                "Impossibile analizzare la superficie di rete: i dati dei permessi non sono disponibili.",
                "Netzwerkoberfläche konnte nicht analysiert werden: Berechtigungsdaten sind nicht verfügbar.",
                "No se pudo analizar la superficie de red: los datos de permisos no están disponibles.",
                "Impossible d’analyser la surface réseau : les données d’autorisations ne sont pas disponibles."
            )
        }
        is PenTestMessage.ManifestSurface -> {
            val parts = buildList {
                if (message.permissions.isNotEmpty()) {
                    add(
                        t("sensitive permissions ", "permessi sensibili ", "sensible Berechtigungen ", "permisos sensibles ", "autorisations sensibles ") +
                                message.permissions.take(3).joinToString(", ")
                    )
                }
                if (message.activities.isNotEmpty()) {
                    add(activityListLabel(message.activities.take(2)))
                }
            }
            t("Manifest/intent surface: ", "Superficie manifest/intent: ", "Manifest-/Intent-Oberfläche: ", "Superficie de manifest/intent: ", "Surface manifest/intent : ") +
                    parts.joinToString(" • ") + "."
        }
        PenTestMessage.RootUnavailable -> t(
            "File scout is limited: root was not detected, so the sandbox cannot be fully inspected.",
            "File scout limitato: root non rilevato, impossibile ispezionare completamente la sandbox.",
            "File Scout eingeschränkt: Kein Root erkannt, die Sandbox kann nicht vollständig untersucht werden.",
            "File scout limitado: no se detectó root y no se puede inspeccionar por completo la sandbox.",
            "File scout limité : root non détecté, inspection complète de la sandbox impossible."
        )
        is PenTestMessage.FileExposure ->
            t("Possible file exposure: broad storage permissions (", "Possibile esposizione file: permessi storage estesi (", "Mögliche Offenlegung von Dateien: weitreichende Speicherberechtigungen (", "Posible exposición de archivos: permisos de almacenamiento amplios (", "Exposition possible de fichiers : autorisations de stockage étendues (") +
                    message.permissions.joinToString(", ") + ")."
        is PenTestMessage.NetworkSurface ->
            t("Extended network surface: INTERNET + ", "Superficie di rete estesa: INTERNET + ", "Erweiterte Netzwerkoberfläche: INTERNET + ", "Superficie de red ampliada: INTERNET + ", "Surface réseau étendue : INTERNET + ") +
                    message.permissions.take(3).joinToString(", ") + "."
        is PenTestMessage.DependencyRiskSummary ->
            t("High priority for external dependency scan: ", "Priorità alta per dependency scan esterno: ", "Hohe Priorität für externen Dependency-Scan: ", "Prioridad alta para un dependency scan externo: ", "Priorité élevée pour un dependency scan externe : ") +
                    riskSummary(message.score, message.criticalCount, message.highCount, message.mediumCount)
        PenTestMessage.InstallPackagesDependencyReview -> t(
            "Package installation requested: review dependencies and supply-chain.",
            "Installazione pacchetti richiesta: verificare dipendenze e supply-chain.",
            "Paketinstallation angefordert: Abhängigkeiten und Supply-Chain prüfen.",
            "Se solicita instalar paquetes: revisar dependencias y supply-chain.",
            "Installation de packages demandée : vérifier les dépendances et la supply-chain."
        )
        is PenTestMessage.SecurityAnomaly ->
            t("Security anomaly: ", "Anomalia di sicurezza: ", "Sicherheitsanomalie: ", "Anomalía de seguridad: ", "Anomalie de sécurité : ") +
                    riskSummary(message.score, message.criticalCount, message.highCount, message.mediumCount) + "."
        is PenTestMessage.CriticalActivityEntryPoints -> {
            val activityDetails = if (message.activities.isEmpty()) "" else " " + activityListLabel(message.activities)
            t(
                "Critical activity entry points: review intent/deeplink before manual testing.",
                "Entry-point activity critici: verificare intent/deeplink prima del test manuale.",
                "Kritische Activity-Einstiegspunkte: Intent/Deeplink vor manuellen Tests prüfen.",
                "Entry points de activity críticos: revisar intent/deeplink antes de las pruebas manuales.",
                "Points d’entrée d’activity critiques : vérifier intent/deeplink avant les tests manuels."
            ) + activityDetails
        }
    }

    override fun boolean(value: Boolean): String = if (value) {
        t("Yes", "Sì", "Ja", "Sí", "Oui")
    } else {
        t("No", "No", "Nein", "No", "Non")
    }

    override fun permissionOverview(
        appCount: Int,
        permissionCount: Int,
        appsWithRisksCount: Int
    ): String {
        val analyzedApps = when (languageCode) {
            "it" -> counted(appCount, "app analizzata", "app analizzate")
            "de" -> counted(appCount, "App analysiert", "Apps analysiert")
            "es" -> counted(appCount, "app analizada", "apps analizadas")
            "fr" -> counted(appCount, "app analysée", "apps analysées")
            else -> counted(appCount, "app analyzed", "apps analyzed")
        }
        val permissions = when (languageCode) {
            "it" -> counted(permissionCount, "permesso trovato", "permessi trovati")
            "de" -> counted(permissionCount, "Berechtigung gefunden", "Berechtigungen gefunden")
            "es" -> counted(permissionCount, "permiso encontrado", "permisos encontrados")
            "fr" -> counted(permissionCount, "autorisation trouvée", "autorisations trouvées")
            else -> counted(permissionCount, "permission found", "permissions found")
        }
        val appsWithRisks = when (languageCode) {
            "it" -> counted(appsWithRisksCount, "app con rischi rilevati", "app con rischi rilevati")
            "de" -> counted(appsWithRisksCount, "App mit erkannten Risiken", "Apps mit erkannten Risiken")
            "es" -> counted(appsWithRisksCount, "app con riesgos detectados", "apps con riesgos detectados")
            "fr" -> counted(appsWithRisksCount, "app présentant des risques", "apps présentant des risques")
            else -> counted(appsWithRisksCount, "app with detected risks", "apps with detected risks")
        }
        return "$analyzedApps • $permissions • $appsWithRisks"
    }

    override fun permissionRiskCounts(criticalCount: Int, highCount: Int, mediumCount: Int): String =
        t("Risks", "Rischi", "Risiken", "Riesgos", "Risques") + " → " +
                riskLevel(PermissionRiskLevel.Critical) + ": $criticalCount | " +
                riskLevel(PermissionRiskLevel.High) + ": $highCount | " +
                riskLevel(PermissionRiskLevel.Medium) + ": $mediumCount"

    override fun permissionAppHeading(index: Int, appLabel: String): String = "$index) $appLabel"

    override fun permissionRiskOverview(
        score: Int,
        findingCount: Int,
        criticalCount: Int,
        highCount: Int,
        mediumCount: Int
    ): String = when (languageCode) {
        "it" -> "score $score • $findingCount ${plural(findingCount, "finding", "finding")} " +
                "(${riskLevel(PermissionRiskLevel.Critical)}: $criticalCount, ${riskLevel(PermissionRiskLevel.High)}: $highCount, ${riskLevel(PermissionRiskLevel.Medium)}: $mediumCount)"
        "de" -> "Score $score • $findingCount ${plural(findingCount, "Finding", "Findings")} " +
                "(${riskLevel(PermissionRiskLevel.Critical)}: $criticalCount, ${riskLevel(PermissionRiskLevel.High)}: $highCount, ${riskLevel(PermissionRiskLevel.Medium)}: $mediumCount)"
        "es" -> "score $score • $findingCount ${plural(findingCount, "finding", "findings")} " +
                "(${riskLevel(PermissionRiskLevel.Critical)}: $criticalCount, ${riskLevel(PermissionRiskLevel.High)}: $highCount, ${riskLevel(PermissionRiskLevel.Medium)}: $mediumCount)"
        "fr" -> "score $score • $findingCount ${plural(findingCount, "finding", "findings")} " +
                "(${riskLevel(PermissionRiskLevel.Critical)} : $criticalCount, ${riskLevel(PermissionRiskLevel.High)} : $highCount, ${riskLevel(PermissionRiskLevel.Medium)} : $mediumCount)"
        else -> "score $score • $findingCount ${plural(findingCount, "finding", "findings")} " +
                "(${riskLevel(PermissionRiskLevel.Critical)}: $criticalCount, ${riskLevel(PermissionRiskLevel.High)}: $highCount, ${riskLevel(PermissionRiskLevel.Medium)}: $mediumCount)"
    }

    override fun declaredPermissionCount(count: Int): String = when (languageCode) {
        "it" -> "Permessi dichiarati: $count"
        "de" -> "Deklarierte Berechtigungen: $count"
        "es" -> "Permisos declarados: $count"
        "fr" -> "Autorisations déclarées : $count"
        else -> "Declared permissions: $count"
    }

    override fun permissionFindingHeading(index: Int, finding: PermissionRiskFinding): String =
        "$index. [${riskLevel(finding.level)}] ${permissionTitle(finding)}"

    override fun noSensitiveBehavior(): String = t(
        "no sensitive behavior detected.",
        "nessun comportamento sensibile rilevato.",
        "kein sensibles Verhalten erkannt.",
        "no se detectó ningún comportamiento sensible.",
        "aucun comportement sensible détecté."
    )

    override fun noRiskyPermissions(): String = t(
        "No risky or malicious permissions detected by the current rules.",
        "Nessun permesso pericoloso o malevolo rilevato con le regole correnti.",
        "Mit den aktuellen Regeln wurden keine riskanten oder bösartigen Berechtigungen erkannt.",
        "Las reglas actuales no detectaron permisos peligrosos o maliciosos.",
        "Aucune autorisation dangereuse ou malveillante détectée selon les règles actuelles."
    )

    override fun noDataAvailable(): String =
        label(ReportLabel.NO_DATA_AVAILABLE)

    override fun noDeclaredPermissions(): String = t(
        "No permissions declared",
        "Nessun permesso dichiarato",
        "Keine Berechtigungen deklariert",
        "No hay permisos declarados",
        "Aucune autorisation déclarée"
    )

    private fun riskSummary(score: Int, critical: Int, high: Int, medium: Int): String =
        "score $score (" +
                t("critical", "critici", "kritisch", "críticos", "critiques") + ": $critical, " +
                t("high", "alti", "hoch", "altos", "élevés") + ": $high, " +
                t("medium", "medi", "mittel", "medios", "moyens") + ": $medium)"

    private fun activityListLabel(activities: List<PenTestActivityRisk>): String =
        t("intent/deeplink ", "intent/deeplink ", "Intent/Deeplink ", "intent/deeplink ", "intent/deeplink ") +
                activities.joinToString("; ") { "${it.label}=${activityRiskLevel(it.level)}" }

    private fun plural(count: Int, singular: String, plural: String): String =
        if (count == 1) singular else plural

    private fun counted(count: Int, singular: String, pluralForm: String): String =
        "$count ${plural(count, singular, pluralForm)}"

    private fun t(en: String, it: String, de: String, es: String, fr: String): String = when (languageCode) {
        "it" -> it
        "de" -> de
        "es" -> es
        "fr" -> fr
        else -> en
    }

    private companion object {
        val SUPPORTED_LANGUAGES = setOf("en", "it", "de", "es", "fr")
    }
}
