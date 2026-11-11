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

    companion object {
        fun from(strings: Strings): ScreenUiStrings = ScreenUiStrings(strings.activityUi.languageCode)
    }
}
