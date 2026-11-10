package com.crackmydroid.shared.i18n

class UseCasePresentationTexts(languageCode: String) {
    private val language = UseCaseTexts(languageCode).languageCode
    private fun text(it: String, en: String, de: String, es: String, fr: String): String = when (language) {
        "it" -> it
        "de" -> de
        "es" -> es
        "fr" -> fr
        else -> en
    }

    val automaticScan
        get() = text(
            "Scansione automatica",
            "Automatic scan",
            "Automatischer Scan",
            "Análisis automático",
            "Analyse automatique",
        )
    val singleTarget
        get() = text(
            "Target singolo",
            "Single target",
            "Einzelnes Target",
            "Target único",
            "Target unique",
        )
    val allInstalledApps
        get() = text(
            "Tutte le app installate",
            "All installed apps",
            "Alle installierten Apps",
            "Todas las apps instaladas",
            "Toutes les apps installées",
        )
    val mode get() = text("Modalità", "Mode", "Modus", "Modo", "Mode")
    val analyzedApps
        get() = text(
            "App analizzate",
            "Analyzed apps",
            "Analysierte Apps",
            "Apps analizadas",
            "Apps analysées",
        )
    val totalChecks
        get() = text(
            "Totale test",
            "Total checks",
            "Tests insgesamt",
            "Total de pruebas",
            "Total des tests",
        )
    val errors get() = text("Errori", "Errors", "Fehler", "Errores", "Erreurs")
    val error get() = text("Errore", "Error", "Fehler", "Error", "Erreur")
    val queued get() = text("In coda", "Queued", "In Warteschlange", "En cola", "En attente")
    val description get() = text("Descrizione", "Description", "Beschreibung", "Descripción", "Description")
    val outcome get() = text("Esito", "Outcome", "Ergebnis", "Resultado", "Résultat")
    val checkTarget get() = text("Target check", "Check target", "Test-Target", "Target de la prueba", "Target du test")
    val details get() = text("Dettagli", "Details", "Details", "Detalles", "Détails")
    val noChecks
        get() = text(
            "Nessun test eseguito.",
            "No checks performed.",
            "Keine Tests durchgeführt.",
            "No se han realizado pruebas.",
            "Aucun test effectué.",
        )
    val selectCheck
        get() = text(
            "Seleziona almeno un test per avviare la scansione automatica.",
            "Select at least one check to start the automatic scan.",
            "Wähle mindestens einen Test für den automatischen Scan.",
            "Selecciona al menos una prueba para iniciar el análisis automático.",
            "Sélectionnez au moins un test pour lancer l’analyse automatique.",
        )
    val noInstalledApps
        get() = text(
            "Nessuna app installata disponibile per la scansione automatica.",
            "No installed apps available for the automatic scan.",
            "Keine installierten Apps für den automatischen Scan verfügbar.",
            "No hay apps instaladas para el análisis automático.",
            "Aucune app installée disponible pour l’analyse automatique.",
        )
    val noPermissionApps
        get() = text(
            "Nessuna app disponibile. Aggiorna prima la lista permessi.",
            "No apps available. Refresh the permissions list first.",
            "Keine Apps verfügbar. Aktualisiere zuerst die Berechtigungsliste.",
            "No hay apps disponibles. Actualiza primero la lista de permisos.",
            "Aucune app disponible. Actualisez d’abord la liste des permissions.",
        )
    val noPackageData
        get() = text(
            "Nessun dato disponibile per il package selezionato.",
            "No data available for the selected package.",
            "Keine Daten für das ausgewählte Package verfügbar.",
            "No hay datos para el package seleccionado.",
            "Aucune donnée disponible pour le package sélectionné.",
        )
    val reportFailed
        get() = text(
            "Impossibile generare il report",
            "Unable to generate the report",
            "Bericht konnte nicht erstellt werden",
            "No se pudo generar el informe",
            "Impossible de générer le rapport",
        )
    val permissionScanFailed
        get() = text(
            "Scansione permessi non riuscita",
            "Permissions scan failed",
            "Berechtigungsscan fehlgeschlagen",
            "Error al analizar los permisos",
            "Échec de l’analyse des permissions",
        )
    val checkFailed
        get() = text(
            "Test non riuscito",
            "Check failed",
            "Test fehlgeschlagen",
            "Prueba fallida",
            "Échec du test",
        )
    val checksFailed
        get() = text(
            "Esecuzione test non riuscita",
            "Check execution failed",
            "Testausführung fehlgeschlagen",
            "Error al ejecutar las pruebas",
            "Échec de l’exécution des tests",
        )
    val scanFailed
        get() = text(
            "Scansione automatica non riuscita",
            "Automatic scan failed",
            "Automatischer Scan fehlgeschlagen",
            "Error en el análisis automático",
            "Échec de l’analyse automatique",
        )
    val saveSettingFailed
        get() = text(
            "Impossibile salvare l'impostazione",
            "Unable to save the setting",
            "Einstellung konnte nicht gespeichert werden",
            "No se pudo guardar el ajuste",
            "Impossible d’enregistrer le réglage",
        )

    val exportFormatUseCaseRequired
        get() = text(
            "GetExportFormatUseCase è richiesto per esportare un report nel formato selezionato",
            "GetExportFormatUseCase is required to export a report in the selected format",
            "GetExportFormatUseCase ist erforderlich, um einen Bericht im ausgewählten Format zu exportieren",
            "GetExportFormatUseCase es obligatorio para exportar un informe en el formato seleccionado",
            "GetExportFormatUseCase est requis pour exporter un rapport dans le format sélectionné",
        )

    fun activitySurface(level: String, count: Int): String = text(
        "Superficie intent/activity: $level ($count ${if (count == 1) "componente da verificare" else "componenti da verificare"})",
        "intent/activity surface: $level ($count ${if (count == 1) "component to review" else "components to review"})",
        "intent/activity-Oberfläche: $level ($count ${if (count == 1) "Komponente zu prüfen" else "Komponenten zu prüfen"})",
        "Superficie intent/activity: $level ($count ${if (count == 1) "componente por verificar" else "componentes por verificar"})",
        "Surface intent/activity : $level ($count ${if (count == 1) "composant à vérifier" else "composants à vérifier"})",
    )
}
