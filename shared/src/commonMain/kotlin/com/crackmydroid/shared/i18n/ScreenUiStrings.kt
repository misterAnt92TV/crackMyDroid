package com.crackmydroid.shared.i18n

class ScreenUiStrings(private val language: String) {
    private fun text(it: String, en: String, de: String, es: String, fr: String): String = when (language) {
        "it" -> it
        "de" -> de
        "es" -> es
        "fr" -> fr
        else -> en
    }

    val searchFilters
        get() = text(
            "Ricerca e filtri",
            "Search and filters",
            "Suche und Filter",
            "Búsqueda y filtros",
            "Recherche et filtres",
        )
    val suggestionsCleared
        get() = text(
            "Suggerimenti svuotati",
            "Suggestions cleared",
            "Vorschläge gelöscht",
            "Sugerencias borradas",
            "Suggestions effacées",
        )
    val frequentSearch
        get() = text(
            "Ricerca frequente: %s",
            "Frequent search: %s",
            "Häufige Suche: %s",
            "Búsqueda frecuente: %s",
            "Recherche fréquente : %s",
        )
    val removedFromFavorites
        get() = text(
            "Rimosso dai preferiti",
            "Removed from favorites",
            "Aus Favoriten entfernt",
            "Eliminado de favoritos",
            "Supprimé des favoris",
        )
    val exportReady
        get() = text(
            "Stato: pronto per invio.",
            "Status: ready to send.",
            "Status: bereit zum Senden.",
            "Estado: listo para enviar.",
            "État : prêt à envoyer.",
        )
    val resultsSummary
        get() = text(
            "Risultati: %d / %d",
            "Results: %d / %d",
            "Ergebnisse: %d / %d",
            "Resultados: %d / %d",
            "Résultats : %d / %d",
        )
    val noApps
        get() = text(
            "Nessuna app disponibile al momento.",
            "No apps available right now.",
            "Derzeit keine Apps verfügbar.",
            "No hay apps disponibles.",
            "Aucune application disponible pour le moment.",
        )
    val noResultsFor
        get() = text(
            "Nessun risultato per \"%s\".",
            "No results for \"%s\".",
            "Keine Ergebnisse für \"%s\".",
            "Sin resultados para \"%s\".",
            "Aucun résultat pour \"%s\".",
        )
    val changeFilter
        get() = text(
            "Rimuovi o cambia filtro per vedere altre app.",
            "Remove or change the filter to see more apps.",
            "Entferne oder ändere den Filter, um weitere Apps zu sehen.",
            "Quita o cambia el filtro para ver más apps.",
            "Supprimez ou modifiez le filtre pour voir d’autres applications.",
        )
    val refreshAppsHint
        get() = text(
            "Prova ad aggiornare la lista per rileggere le app installate.",
            "Refresh the list to read installed apps again.",
            "Aktualisiere die Liste, um installierte Apps erneut zu laden.",
            "Actualiza la lista para volver a leer las apps instaladas.",
            "Actualisez la liste pour relire les applications installées.",
        )
    val resetFilter
        get() = text(
            "Reset filtro",
            "Reset filter",
            "Filter zurücksetzen",
            "Restablecer filtro",
            "Réinitialiser le filtre",
        )
    val refresh get() = text("Aggiorna", "Refresh", "Aktualisieren", "Actualizar", "Actualiser")
    val logWrapLines
        get() = text(
            "Avvolgi righe",
            "Wrap lines",
            "Zeilen umbrechen",
            "Ajustar líneas",
            "Renvoyer les lignes",
        )
    val logColorByLevel
        get() = text(
            "Colora per livello",
            "Color by level",
            "Nach Ebene einfärben",
            "Colorear por nivel",
            "Colorer par niveau",
        )
    val logTextSize get() = text("Dimensione testo", "Text size", "Textgröße", "Tamaño del texto", "Taille du texte")
    val logTrimmed
        get() = text(
            "Mostro solo le ultime 1500 righe del log per prestazioni.",
            "Showing only the latest 1500 log lines for performance.",
            "Aus Leistungsgründen werden nur die letzten 1500 Logzeilen angezeigt.",
            "Solo se muestran las últimas 1500 líneas del registro por rendimiento.",
            "Seules les 1500 dernières lignes du journal sont affichées pour les performances.",
        )
    val confirmCommand
        get() = text(
            "Conferma comando",
            "Confirm command",
            "Befehl bestätigen",
            "Confirmar comando",
            "Confirmer la commande",
        )
    val execute get() = text("Esegui", "Run", "Ausführen", "Ejecutar", "Exécuter")
    val executing
        get() = text(
            "In esecuzione...",
            "Running...",
            "Wird ausgeführt...",
            "En ejecución...",
            "Exécution...",
        )
    val cancel get() = text("Annulla", "Cancel", "Abbrechen", "Cancelar", "Annuler")
    val privilegedCommand
        get() = text(
            "Comando privilegiato",
            "Privileged command",
            "Privilegierter Befehl",
            "Comando privilegiado",
            "Commande privilégiée",
        )
    val standardCommand
        get() = text(
            "Comando standard",
            "Standard command",
            "Standardbefehl",
            "Comando estándar",
            "Commande standard",
        )
    val waitForCommand
        get() = text(
            "Attendi comando corrente",
            "Wait for the current command",
            "Warte auf den aktuellen Befehl",
            "Espera al comando actual",
            "Attendez la commande en cours",
        )
    val executeCommand
        get() = text(
            "Esegui comando",
            "Run command",
            "Befehl ausführen",
            "Ejecutar comando",
            "Exécuter la commande",
        )
    val command get() = text("Comando", "Command", "Befehl", "Comando", "Commande")
    val noTextOutput
        get() = text(
            "Nessun output testuale",
            "No text output",
            "Keine Textausgabe",
            "Sin salida de texto",
            "Aucune sortie texte",
        )
    val output get() = text("Output", "Output", "Ausgabe", "Salida", "Sortie")
    val errorOutput get() = text("Error output", "Error output", "Fehlerausgabe", "Salida de error", "Sortie d’erreur")
    val exitCode
        get() = text(
            "Exit code: %d",
            "Exit code: %d",
            "Exit-Code: %d",
            "Código de salida: %d",
            "Code de sortie : %d",
        )
    val packagesFound
        get() = text(
            "Pacchetti trovati: %d",
            "Packages found: %d",
            "Gefundene Pakete: %d",
            "Paquetes encontrados: %d",
            "Paquets trouvés : %d",
        )
    val riskScanTitle
        get() = text(
            "Scansione automatica permessi rischiosi",
            "Automatic risky-permission scan",
            "Automatischer Scan riskanter Berechtigungen",
            "Escaneo automático de permisos de riesgo",
            "Analyse automatique des permissions à risque",
        )
    val riskScanDescription
        get() = text(
            "Analizza tutte le app installate e segnala permessi pericolosi o pattern potenzialmente malevoli.",
            "Analyze all installed apps and flag dangerous permissions or potentially malicious patterns.",
            "Analysiert alle installierten Apps und meldet gefährliche Berechtigungen oder potenziell schädliche Muster.",
            "Analiza todas las apps instaladas y señala permisos peligrosos o patrones potencialmente maliciosos.",
            "Analyse toutes les applications installées et signale les permissions dangereuses ou les motifs potentiellement malveillants.",
        )
    val startScan get() = text("Avvia scansione", "Start scan", "Scan starten", "Iniciar escaneo", "Lancer l’analyse")
    val stopScan get() = text("Interrompi", "Stop", "Stopp", "Detener", "Arrêter")
    val scanProgress
        get() = text(
            "Scansione in corso: %d/%d app",
            "Scanning: %d/%d apps",
            "Scan läuft: %d/%d Apps",
            "Escaneando: %d/%d apps",
            "Analyse en cours : %d/%d applications",
        )
    val analysis get() = text("Analisi: %s", "Analysis: %s", "Analyse: %s", "Análisis: %s", "Analyse : %s")
    val riskOnly
        get() = text(
            "Solo app a rischio",
            "Risky apps only",
            "Nur riskante Apps",
            "Solo apps de riesgo",
            "Applications à risque uniquement",
        )
    val allApps get() = text("Mostra tutte", "Show all", "Alle anzeigen", "Mostrar todas", "Tout afficher")
    val additionalFindings
        get() = text(
            "+%d finding aggiuntivi nel report condivisibile",
            "+%d additional findings in the shareable report",
            "+%d zusätzliche Findings im teilbaren Bericht",
            "+%d hallazgos adicionales en el informe compartible",
            "+%d findings supplémentaires dans le rapport partageable",
        )
    val riskSummary
        get() = text(
            "Riepilogo: %d app a rischio • Critici %d • Alti %d • Medi %d",
            "Summary: %d risky apps • Critical %d • High %d • Medium %d",
            "Zusammenfassung: %d riskante Apps • Kritisch %d • Hoch %d • Mittel %d",
            "Resumen: %d apps de riesgo • Críticas %d • Altas %d • Medias %d",
            "Résumé : %d applications à risque • Critiques %d • Élevées %d • Moyennes %d",
        )
    val noRiskResults
        get() = text(
            "Nessun risultato con i filtri correnti.",
            "No results with the current filters.",
            "Keine Ergebnisse mit den aktuellen Filtern.",
            "No hay resultados con los filtros actuales.",
            "Aucun résultat avec les filtres actuels.",
        )
    val noPermissions
        get() = text(
            "Nessun permesso disponibile.",
            "No permissions available.",
            "Keine Berechtigungen verfügbar.",
            "No hay permisos disponibles.",
            "Aucune permission disponible.",
        )
    val riskFilterHint
        get() = text(
            "Prova a disattivare \"Solo app a rischio\" o a cambiare ricerca.",
            "Disable \"Risky apps only\" or change the search.",
            "Deaktiviere \"Nur riskante Apps\" oder ändere die Suche.",
            "Desactiva \"Solo apps de riesgo\" o cambia la búsqueda.",
            "Désactivez \"Applications à risque uniquement\" ou modifiez la recherche.",
        )
    val refreshPermissionsHint
        get() = text(
            "Aggiorna i dati permessi per iniziare l'analisi.",
            "Refresh permissions data to start the analysis.",
            "Aktualisiere die Berechtigungsdaten, um die Analyse zu starten.",
            "Actualiza los datos de permisos para iniciar el análisis.",
            "Actualisez les données de permissions pour commencer l’analyse.",
        )
    val permissionsExportSummary
        get() = text(
            "Report permessi esportato in formato testuale, già pronto alla condivisione.",
            "Permissions report exported as text, ready to share.",
            "Berechtigungsbericht als Text exportiert und bereit zum Teilen.",
            "Informe de permisos exportado en texto, listo para compartir.",
            "Rapport de permissions exporté en texte, prêt à être partagé.",
        )
    val permissionsExportDetails
        get() = text(
            "Contiene riepilogo rischi (critici/alti/medi) e lista permessi per app.",
            "Contains risk summary (critical/high/medium) and permissions per app.",
            "Enthält eine Risikoübersicht (kritisch/hoch/mittel) und Berechtigungen pro App.",
            "Contiene resumen de riesgos (críticos/altos/medios) y permisos por app.",
            "Contient un résumé des risques (critiques/élevés/moyens) et les permissions par application.",
        )
    val readableFormat
        get() = text(
            "Formato leggibile: blocchi separati per app e finding.",
            "Readable format: separate blocks for each app and finding.",
            "Lesbares Format: getrennte Blöcke für Apps und Findings.",
            "Formato legible: bloques separados por app y hallazgo.",
            "Format lisible : blocs séparés par application et finding.",
        )
    val appsExportSummary
        get() = text(
            "Esportazione completata. Puoi usare il file per backup o condivisione tecnica.",
            "Export completed. You can use the file for backup or technical sharing.",
            "Export abgeschlossen. Du kannst die Datei für Backup oder technisches Teilen verwenden.",
            "Exportación completada. Puedes usar el archivo para copia o uso técnico.",
            "Exportation terminée. Vous pouvez utiliser le fichier pour sauvegarde ou partage technique.",
        )
    val appsExportContent
        get() = text(
            "Contenuto: lista applicazioni installate (label, package, path APK).",
            "Content: installed application list (label, package, APK path).",
            "Inhalt: Liste installierter Anwendungen (Label, Package, APK-Pfad).",
            "Contenido: lista de aplicaciones instaladas (etiqueta, package, ruta APK).",
            "Contenu : liste des applications installées (libellé, package, chemin APK).",
        )
    val selected get() = text("✔︎ %s", "✔︎ %s", "✔︎ %s", "✔︎ %s", "✔︎ %s")
    val bullet get() = text("• %s", "• %s", "• %s", "• %s", "• %s")
    val missingValue get() = text("?", "?", "?", "?", "?")
    val stepIndicator get() = text("%d/4 • %s", "%d/4 • %s", "%d/4 • %s", "%d/4 • %s", "%d/4 • %s")
    val summaryBullet get() = text("• %s: %d/%d", "• %s: %d/%d", "• %s: %d/%d", "• %s: %d/%d", "• %s : %d/%d")
    val additionalCount get() = text("+%d", "+%d", "+%d", "+%d", "+%d")
    val rootEvidence get() = text("%s: %s — %s", "%s: %s — %s", "%s: %s — %s", "%s: %s — %s", "%s : %s — %s")
    val commandUnavailable
        get() = text(
            "Comando non disponibile",
            "Command unavailable",
            "Befehl nicht verfügbar",
            "Comando no disponible",
            "Commande indisponible",
        )
    val rootTag get() = text("ROOT", "ROOT", "ROOT", "ROOT", "ROOT")
    val safeTag get() = text("SAFE", "SAFE", "SICHER", "SEGURO", "SÛR")
    val unavailableTag get() = text("NON DISP.", "UNAVAILABLE", "NICHT VERF.", "NO DISP.", "INDISP.")
    val confirmationTag get() = text("CONFERMA", "CONFIRM", "BESTÄTIGUNG", "CONFIRMACIÓN", "CONFIRMATION")
    val anotherCommand
        get() = text(
            "un altro comando",
            "another command",
            "ein anderer Befehl",
            "otro comando",
            "une autre commande",
        )
    val waitCommandRunning
        get() = text(
            "Attendi: \"%s\" è ancora in esecuzione.",
            "Wait: \"%s\" is still running.",
            "Warte: \"%s\" läuft noch.",
            "Espera: \"%s\" aún se está ejecutando.",
            "Attendez : \"%s\" est encore en cours d’exécution.",
        )
    val commandNotStartedUntilCurrentEnds
        get() = text(
            "\"%s\" non avviato: prima termina \"%s\".",
            "\"%s\" not started: first finish \"%s\".",
            "\"%s\" nicht gestartet: zuerst \"%s\" beenden.",
            "\"%s\" no iniciado: primero termina \"%s\".",
            "\"%s\" non démarrée : terminez d’abord \"%s\".",
        )
    val commandNotAvailableOnSelectedDevice
        get() = text(
            "\"%s\" non disponibile sul device selezionato.",
            "\"%s\" is not available on the selected device.",
            "\"%s\" ist auf dem ausgewählten Gerät nicht verfügbar.",
            "\"%s\" no está disponible en el dispositivo seleccionado.",
            "\"%s\" n’est pas disponible sur l’appareil sélectionné.",
        )
    val installedAppsReportTitle
        get() = text(
            "REPORT APP INSTALLATE",
            "INSTALLED APPS REPORT",
            "BERICHT INSTALLIERTE APPS",
            "INFORME DE APPS INSTALADAS",
            "RAPPORT APPLIS INSTALLÉES",
        )
    val installedAppsTotal
        get() = text(
            "Totale app: %d",
            "Total apps: %d",
            "Apps gesamt: %d",
            "Total de apps: %d",
            "Total d’applications : %d",
        )
    val readableTextFormat
        get() = text(
            "Formato: testo leggibile",
            "Format: readable text",
            "Format: lesbarer Text",
            "Formato: texto legible",
            "Format : texte lisible",
        )
    val packageLabel get() = text("Package", "Package", "Paket", "Paquete", "Package")
    val apkPathLabel get() = text("Path APK", "APK Path", "APK-Pfad", "Ruta APK", "Chemin APK")
    val installSourceLabel
        get() = text(
            "Origine installazione",
            "Install source",
            "Installationsquelle",
            "Origen de instalación",
            "Source d’installation",
        )
    val installerLabel get() = text("Installer", "Installer", "Installer", "Instalador", "Installeur")
    val queuedTarget get() = text("%s %s", "%s %s", "%s %s", "%s %s", "%s %s")
    val errorDetail get() = text("%s: %s", "%s: %s", "%s: %s", "%s: %s", "%s : %s")
    val noTargetValue get() = text("-", "-", "-", "-", "-")
    val permissionsCount get() = text("%d perm", "%d permissions", "%d Berechtigungen", "%d permisos", "%d permissions")
    val permissionsRiskCount
        get() = text(
            "%d perm • rischi %d",
            "%d permissions • risks %d",
            "%d Berechtigungen • Risiken %d",
            "%d permisos • riesgos %d",
            "%d permissions • risques %d",
        )
    val risksDetected
        get() = text(
            "Rischi rilevati: %d (Critici %d, Alti %d, Medi %d)",
            "Risks detected: %d (Critical %d, High %d, Medium %d)",
            "Risiken: %d (Kritisch %d, Hoch %d, Mittel %d)",
            "Riesgos detectados: %d (Críticos %d, Altos %d, Medios %d)",
            "Risques détectés : %d (Critiques %d, Élevés %d, Moyens %d)",
        )
    val findingAdditional
        get() = text(
            "+%d finding aggiuntivi nel report condivisibile",
            "+%d additional findings in the shareable report",
            "+%d zusätzliche Findings im teilbaren Bericht",
            "+%d hallazgos adicionales en el informe compartible",
            "+%d findings supplémentaires dans le rapport partageable",
        )
    val riskCritical get() = text("CRITICO", "CRITICAL", "KRITISCH", "CRÍTICO", "CRITIQUE")
    val riskHigh get() = text("ALTO", "HIGH", "HOCH", "ALTO", "ÉLEVÉ")
    val riskMedium get() = text("MEDIO", "MEDIUM", "MITTEL", "MEDIO", "MOYEN")
    val appsLoaded get() = text(
        "Caricate %d app installate",
        "%d installed apps loaded",
        "%d installierte Apps geladen",
        "%d apps instaladas cargadas",
        "%d applications installées chargées",
    )
    val loadingInstalledApps get() = text(
        "Caricamento app installate",
        "Loading installed apps",
        "Installierte Apps werden geladen",
        "Cargando apps instaladas",
        "Chargement des applications installées",
    )
    val exportApk get() = text("Export APK", "APK export", "APK-Export", "Exportación APK", "Export APK")
    val exportApps get() = text(
        "Export lista app",
        "App list export",
        "Export der App-Liste",
        "Exportación de lista de apps",
        "Export de la liste des applications",
    )
    val shareApps get() = text(
        "Share lista app",
        "Share app list",
        "App-Liste teilen",
        "Compartir lista de apps",
        "Partager la liste des applications",
    )
    val shareApk get() = text("Share APK", "Share APK", "APK teilen", "Compartir APK", "Partager l’APK")
    val saveSettingFailed get() = text(
        "Impossibile salvare l'impostazione",
        "Unable to save the setting",
        "Einstellung konnte nicht gespeichert werden",
        "No se pudo guardar el ajuste",
        "Impossible d’enregistrer le réglage",
    )
    val exitConfirmTitle
        get() = text(
            "Uscita dall'app",
            "Exit app",
            "App beenden",
            "Salir de la aplicación",
            "Quitter l'application",
        )
    val exitConfirmMessage
        get() = text(
            "Sei sicuro di voler uscire da CrackMyDroid?",
            "Are you sure you want to exit CrackMyDroid?",
            "Möchten Sie CrackMyDroid wirklich beenden?",
            "¿Seguro que deseas salir de CrackMyDroid?",
            "Voulez-vous vraiment quitter CrackMyDroid ?",
        )
    val exitConfirmButton
        get() = text(
            "Esci",
            "Exit",
            "Beenden",
            "Salir",
            "Quitter",
        )
    val exitCancelButton
        get() = text(
            "Annulla",
            "Cancel",
            "Abbrechen",
            "Cancelar",
            "Annuler",
        )

    val apkContentStep1Title
        get() = text(
            "Fase 1 di 3 — Seleziona un'app installata",
            "Phase 1 of 3 — Select an installed app",
            "Phase 1 von 3 — Installierte App auswählen",
            "Fase 1 de 3 — Selecciona una app instalada",
            "Phase 1 sur 3 — Sélectionner une application installée",
        )
    val apkContentStep1Desc
        get() = text(
            "Scegli un'applicazione dal dispositivo per ispezionare l'archivio APK e le librerie.",
            "Choose an application from the device to inspect its APK archive and libraries.",
            "Wähle eine App vom Gerät, um das APK-Archiv und die Bibliotheken zu prüfen.",
            "Elige una aplicación del dispositivo para inspeccionar su archivo APK y librerías.",
            "Choisissez une application de l'appareil pour inspecter son archive APK et ses bibliothèques.",
        )
    val apkContentStep2Title
        get() = text(
            "Fase 2 di 3 — Conferma APK e ispezione archivio ZIP",
            "Phase 2 of 3 — Confirm APK and inspect ZIP contents",
            "Phase 2 von 3 — APK bestätigen und ZIP-Inhalt prüfen",
            "Fase 2 de 3 — Confirmar APK e inspeccionar contenido ZIP",
            "Phase 2 sur 3 — Confirmer l'APK et inspecter le contenu ZIP",
        )
    val apkContentStep2Desc
        get() = text(
            "Pronto a estrarre struttura APK, binari nativi ed elementi dei certificati.",
            "Ready to extract APK structure, native binaries, and certificate entries.",
            "Bereit zur Extraktion von APK-Struktur, nativen Binärdateien und Zertifikaten.",
            "Listo para extraer la estructura del APK, binarios nativos y certificados.",
            "Prêt à extraire la structure de l'APK, les binaires natifs et les certificats.",
        )
    val apkContentStep3Title
        get() = text(
            "Fase 3 di 3 — Report di analisi statica offline",
            "Phase 3 of 3 — Offline static-analysis report",
            "Phase 3 von 3 — Offline-Bericht zur statischen Analyse",
            "Fase 3 de 3 — Informe de análisis estático offline",
            "Phase 3 sur 3 — Rapport d'analyse statique hors ligne",
        )
    val apkContentStep3Desc
        get() = text(
            "Evidenze euristiche, librerie rilevate e riepilogo integrità dell'archivio.",
            "Heuristic findings, detected libraries, and archive integrity summary.",
            "Heuristische Befunde, erkannte Bibliotheken und Archiv-Integritätsübersicht.",
            "Hallazgos heurísticos, librerías detectadas y resumen de integridad del archivo.",
            "Résultats heuristiques, bibliothèques détectées et résumé de l'intégrité de l'archive.",
        )
    val apkContentSelect
        get() = text(
            "Seleziona",
            "Select",
            "Auswählen",
            "Seleccionar",
            "Sélectionner",
        )
    val apkContentAnalyzingProgress
        get() = text(
            "Analisi dell'archivio APK, dei file DEX e delle librerie native in corso…",
            "Analyzing APK archive, DEX files, and native libraries…",
            "APK-Archiv, DEX-Dateien und native Bibliotheken werden analysiert…",
            "Analizando archivo APK, archivos DEX y librerías nativas…",
            "Analyse de l'archive APK, des fichiers DEX et des bibliothèques natives en cours…",
        )
    val apkContentChangeApp
        get() = text(
            "Cambia app",
            "Change app",
            "App wechseln",
            "Cambiar app",
            "Changer d'application",
        )
    val apkContentAnalyze
        get() = text(
            "Analizza contenuto",
            "Analyze content",
            "Inhalt analysieren",
            "Analizar contenido",
            "Analyser le contenu",
        )
    val apkContentAnalyzing
        get() = text(
            "Analisi in corso…",
            "Analyzing…",
            "Analyse läuft…",
            "Analizando…",
            "Analyse en cours…",
        )
    val apkContentExportReport
        get() = text(
            "Esporta report",
            "Export report",
            "Bericht exportieren",
            "Exportar informe",
            "Exporter le rapport",
        )
    val apkContentShareReport
        get() = text(
            "Condividi report",
            "Share report",
            "Bericht teilen",
            "Compartir informe",
            "Partager le rapport",
        )
    val apkContentAnalyzeAnother
        get() = text(
            "Analizza un'altra app",
            "Analyze another app",
            "Andere App analysieren",
            "Analizar otra app",
            "Analyser une autre application",
        )
    val apkContentReportTitle
        get() = text(
            "Report di analisi statica",
            "Static Analysis Report",
            "Bericht zur statischen Analyse",
            "Informe de análisis estático",
            "Rapport d'analyse statique",
        )
    val apkContentRiskBadge
        get() = text(
            "RISCHIO %s",
            "%s RISK",
            "%s-RISIKO",
            "RIESGO %s",
            "RISQUE %s",
        )
    val apkContentMetricsTitle
        get() = text(
            "Metriche archivio",
            "Archive Metrics",
            "Archiv-Metriken",
            "Métricas del archivo",
            "Métriques de l'archive",
        )
    val apkContentMetricArchiveSize
        get() = text(
            "Dimensione archivio",
            "Archive size",
            "Archivgröße",
            "Tamaño del archivo",
            "Taille de l'archive",
        )
    val apkContentMetricUncompressed
        get() = text(
            "Non compresso",
            "Uncompressed",
            "Unkomprimiert",
            "Descomprimido",
            "Non compressé",
        )
    val apkContentMetricTotalEntries
        get() = text(
            "Voci totali",
            "Total entries",
            "Einträge gesamt",
            "Entradas totales",
            "Entrées totales",
        )
    val apkContentMetricDexFiles
        get() = text(
            "File DEX",
            "DEX files",
            "DEX-Dateien",
            "Archivos DEX",
            "Fichiers DEX",
        )
    val apkContentMetricNativeLibs
        get() = text(
            "Librerie native",
            "Native libraries",
            "Native Bibliotheken",
            "Librerías nativas",
            "Bibliothèques natives",
        )
    val apkContentMetricCerts
        get() = text(
            "Certificati",
            "Certificates",
            "Zertifikate",
            "Certificados",
            "Certificats",
        )
    val apkContentChecksumSha256
        get() = text(
            "Checksum SHA-256",
            "SHA-256 Checksum",
            "SHA-256-Prüfsumme",
            "Suma de comprobación SHA-256",
            "Somme de contrôle SHA-256",
        )
    val apkContentLibrariesTitle
        get() = text(
            "Librerie rilevate (%d)",
            "Detected Libraries (%d)",
            "Erkannte Bibliotheken (%d)",
            "Librerías detectadas (%d)",
            "Bibliothèques détectées (%d)",
        )
    val apkContentNoLibraries
        get() = text(
            "Nessuna libreria di terze parti nota rilevata.",
            "No known third-party libraries detected.",
            "Keine bekannten Drittanbieter-Bibliotheken erkannt.",
            "No se detectaron librerías de terceros conocidas.",
            "Aucune bibliothèque tierce connue détectée.",
        )
    val apkContentFindingsTitle
        get() = text(
            "Riscontri di sicurezza e integrità (%d)",
            "Security & Integrity Findings (%d)",
            "Sicherheits- & Integritätsbefunde (%d)",
            "Hallazgos de seguridad e integridad (%d)",
            "Résultats de sécurité et d'intégrité (%d)",
        )
    val apkContentNoFindings
        get() = text(
            "Nessun problema di integrità o sicurezza rilevato.",
            "No integrity or security issues detected.",
            "Keine Integritäts- oder Sicherheitsprobleme erkannt.",
            "No se detectaron problemas de integridad o seguridad.",
            "Aucun problème d'intégrité ou de sécurité détecté.",
        )
    val apkContentRemediationPrefix
        get() = text(
            "Rimedio: %s",
            "Remediation: %s",
            "Behebung: %s",
            "Solución: %s",
            "Remédiation : %s",
        )
    val apkContentDisclaimer
        get() = text(
            "L'analisi statica offline fornisce indicatori euristici, non un verdetto antivirus.",
            "Offline static analysis provides heuristic indicators, not an antivirus verdict.",
            "Die statische Offline-Analyse liefert heuristische Indikatoren, kein Antivirus-Urteil.",
            "El análisis estático offline proporciona indicadores heurísticos, no un veredicto antivirus.",
            "L'analyse statique hors ligne fournit des indicateurs heuristiques, pas un verdict antivirus.",
        )
    val apkContentExportedTo
        get() = text(
            "Report esportato in: %s",
            "Report exported to: %s",
            "Bericht exportiert nach: %s",
            "Informe exportado a: %s",
            "Rapport exporté vers : %s",
        )
    val navAnalyzeApkContent
        get() = text(
            "Analisi APK",
            "Analyze APK Content",
            "APK-Inhalt analysieren",
            "Analizar contenido APK",
            "Analyser le contenu APK",
        )
    val helpAnalyzeApkContent
        get() = text(
            "Analisi offline dell'archivio APK, librerie e indicatori euristici.",
            "Offline APK archive, library and heuristic analysis.",
            "Offline-Analyse des APK-Archivs, der Bibliotheken und Heuristiken.",
            "Análisis offline de archivos APK, bibliotecas e indicadores heurísticos.",
            "Analyse hors ligne de l'archive APK, des bibliothèques et des indicateurs heuristiques.",
        )
    val apkContentPlaintextTitle
        get() = text(
            "Elementi e testi in chiaro (%d)",
            "Readable & Plaintext Elements (%d)",
            "Klartext- & lesbare Elemente (%d)",
            "Elementos y texto en claro (%d)",
            "Éléments et textes en clair (%d)",
        )
    val apkContentPlaintextFilterAll
        get() = text(
            "Tutti",
            "All",
            "Alle",
            "Todos",
            "Tous",
        )
    val apkContentPlaintextFilterEndpoints
        get() = text(
            "URL ed endpoint",
            "URLs & Endpoints",
            "URLs & Endpunkte",
            "URLs y endpoints",
            "URLs et points de terminaison",
        )
    val apkContentPlaintextFilterConfig
        get() = text(
            "Chiavi e configurazioni",
            "Keys & Configurations",
            "Schlüssel & Konfiguration",
            "Claves y configuración",
            "Clés et configurations",
        )
    val apkContentPlaintextFilterStrings
        get() = text(
            "Stringhe e nomi",
            "Strings & Names",
            "Zeichenketten & Namen",
            "Cadenas y nombres",
            "Chaînes et noms",
        )
    val apkContentNoPlaintext
        get() = text(
            "Nessun testo o variabile in chiaro rilevante estratto.",
            "No notable readable text or plaintext variables extracted.",
            "Keine bemerkenswerten Klartext-Variablen oder Texte extrahiert.",
            "No se extrajo texto legible ni variables en claro destacables.",
            "Aucun texte lisible ou variable en clair notable extrait.",
        )
    val apkContentSourceLabel
        get() = text(
            "Sorgente: %s",
            "Source: %s",
            "Quelle: %s",
            "Origen: %s",
            "Source : %s",
        )
    val apkContentOpenAnalyzedArchive
        get() = text(
            "Apri ZIP analizzato",
            "Open analyzed ZIP",
            "Analysierte ZIP öffnen",
            "Abrir ZIP analizado",
            "Ouvrir le ZIP analysé",
        )
    val apkContentPreparingArchive
        get() = text(
            "Preparazione ZIP...",
            "Preparing ZIP...",
            "ZIP wird vorbereitet...",
            "Preparando ZIP...",
            "Préparation du ZIP...",
        )
    val apkContentPlaintextPreview
        get() = text(
            "Mostra altri %d elementi",
            "Show %d more items",
            "%d weitere Elemente anzeigen",
            "Mostrar %d elementos más",
            "Afficher %d éléments supplémentaires",
        )

    companion object {
        fun from(strings: Strings): ScreenUiStrings = ScreenUiStrings(strings.activityUi.languageCode)
    }
}
