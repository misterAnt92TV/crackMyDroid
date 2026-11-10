package com.crackmydroid.shared.i18n

class ActivityUiStrings(private val language: String) {
    val languageCode: String get() = language
    private fun text(it: String, en: String, de: String, es: String, fr: String): String = when (language) {
        "it" -> it
        "de" -> de
        "es" -> es
        "fr" -> fr
        else -> en
    }

    val searchToolsSubtitle
        get() = text(
            "Suggerimenti e filtri",
            "Suggestions and filters",
            "Vorschläge und Filter",
            "Sugerencias y filtros",
            "Suggestions et filtres",
        )
    val suggestionsCleared
        get() = text(
            "Suggerimenti svuotati",
            "Suggestions cleared",
            "Vorschläge gelöscht",
            "Sugerencias borradas",
            "Suggestions effacées",
        )
    val exportSuccess
        get() = text(
            "Report HTML esportato per %s",
            "HTML report exported for %s",
            "HTML-Bericht für %s exportiert",
            "Informe HTML exportado para %s",
            "Rapport HTML exporté pour %s",
        )
    val exportFailed
        get() = text(
            "Export report non riuscito",
            "Report export failed",
            "Berichtexport fehlgeschlagen",
            "Error al exportar el informe",
            "Échec de l’export du rapport",
        )
    val frequentSearch
        get() = text(
            "Ricerca frequente: %s",
            "Frequent search: %s",
            "Häufige Suche: %s",
            "Búsqueda frecuente: %s",
            "Recherche fréquente : %s",
        )
    val resultsSummary
        get() = text(
            "Risultati: %d / %d attività",
            "Results: %d / %d activities",
            "Ergebnisse: %d / %d Aktivitäten",
            "Resultados: %d / %d actividades",
            "Résultats : %d / %d activités",
        )
    val noResults
        get() = text(
            "Nessun risultato per \"%s\".",
            "No results for \"%s\".",
            "Keine Ergebnisse für \"%s\".",
            "Sin resultados para \"%s\".",
            "Aucun résultat pour \"%s\".",
        )
    val noActivities
        get() = text(
            "Nessuna activity disponibile.",
            "No activities available.",
            "Keine Aktivitäten verfügbar.",
            "No hay actividades disponibles.",
            "Aucune activité disponible.",
        )
    val searchHint
        get() = text(
            "Prova con package, nome app o nome activity.",
            "Try a package, app name or activity name.",
            "Versuche ein Package, einen App- oder Activity-Namen.",
            "Prueba con package, nombre de app o actividad.",
            "Essayez un package, un nom d’app ou d’activité.",
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
    val activityCount get() = text("%d attività", "%d activities", "%d Aktivitäten", "%d actividades", "%d activités")
    val intentRequired
        get() = text(
            "intent richiesto",
            "intent required",
            "Intent erforderlich",
            "intent requerido",
            "intent requis",
        )
    val dumpOnly get() = text("solo dump", "dump only", "nur Dump", "solo volcado", "dump uniquement")
    val shellWithAction
        get() = text(
            "am start + action",
            "am start + action",
            "am start + Aktion",
            "am start + acción",
            "am start + action",
        )
    val shellStart get() = text("am start", "am start", "am start", "am start", "am start")
    val guidedLauncherTitle
        get() = text(
            "Launcher guidato",
            "Guided launcher",
            "Geführter Launcher",
            "Lanzador guiado",
            "Lanceur guidé",
        )
    val actionLabel get() = "Action"
    val dataUriLabel get() = "Data URI"
    val mimeTypeLabel get() = "MIME type"
    val resolver get() = text("Resolver: %s", "Resolver: %s", "Resolver: %s", "Resolver: %s", "Résolveur : %s")
    val launch get() = text("Avvia", "Launch", "Starten", "Iniciar", "Lancer")
    val cancel get() = text("Annulla", "Cancel", "Abbrechen", "Cancelar", "Annuler")
    val reportTitle
        get() = text(
            "Report activity desktop",
            "Desktop activity report",
            "Desktop-Aktivitätsbericht",
            "Informe de actividades de escritorio",
            "Rapport d’activités bureau",
        )
    val close get() = text("Chiudi", "Close", "Schließen", "Cerrar", "Fermer")
    val totals
        get() = text(
            "Totale %d activity • shell %d • guided %d • dump only %d",
            "Total %d activities • shell %d • guided %d • dump only %d",
            "Gesamt %d Aktivitäten • Shell %d • geführt %d • nur Dump %d",
            "Total %d actividades • shell %d • guiado %d • solo volcado %d",
            "Total : %d activités • shell %d • guidées %d • dump uniquement %d",
        )
    val riskSummary
        get() = text(
            "Hint contestuali %d • preferite %d • rischio %s%s",
            "Contextual hints %d • favorites %d • risk %s%s",
            "Kontexthinweise %d • Favoriten %d • Risiko %s%s",
            "Pistas contextuales %d • favoritas %d • riesgo %s%s",
            "Indices contextuels %d • favorites %d • risque %s%s",
        )
    val favoriteAppSuffix
        get() = text(
            " • app preferita",
            " • favorite app",
            " • bevorzugte App",
            " • app favorita",
            " • app favorite",
        )
    val recommendations
        get() = text(
            "Raccomandazioni PenTest",
            "PenTest recommendations",
            "PenTest-Empfehlungen",
            "Recomendaciones PenTest",
            "Recommandations PenTest",
        )
    val highlights
        get() = text(
            "Activity in evidenza",
            "Highlighted activities",
            "Hervorgehobene Aktivitäten",
            "Actividades destacadas",
            "Activités mises en avant",
        )
    val noAdditionalHint
        get() = text(
            "Nessun hint aggiuntivo",
            "No additional hint",
            "Kein zusätzlicher Hinweis",
            "No hay pista adicional",
            "Aucun indice supplémentaire",
        )
    val activityNotLaunchable get() = text("Activity non avviabile via am start -n", "Activity cannot be launched via am start -n", "Activity kann nicht über am start -n gestartet werden", "La activity no se puede iniciar mediante am start -n", "L’activité ne peut pas être lancée via am start -n")
    val unableToLaunch get() = text("Impossibile avviare %s", "Unable to launch %s", "%s konnte nicht gestartet werden", "No se pudo iniciar %s", "Impossible de lancer %s")
    val activityExport get() = text("Export attività", "Activity export", "Activity-Export", "Exportación de actividades", "Export des activités")
    val noActivity get() = text("Nessuna activity", "No activity", "Keine Activity", "Ninguna activity", "Aucune activité")
    val activityReport get() = text("Report activity", "Activity report", "Activity-Bericht", "Informe de actividades", "Rapport d’activités")
    val riskLabel get() = text("Rischio", "Risk", "Risiko", "Riesgo", "Risque")
    val hintLabel get() = text("Hint", "Hint", "Hinweis", "Pista", "Indice")
    val noAdditionalDetails get() = text("Nessun dettaglio aggiuntivo", "No additional details", "Keine zusätzlichen Details", "Ningún detalle adicional", "Aucun détail supplémentaire")
    val guidedLaunch get() = text("Guided launch", "Guided launch", "Geführter Start", "Inicio guiado", "Lancement guidé")
    val activityDumpOnly get() = text("Solo dump", "Dump only", "Nur Dump", "Solo volcado", "Dump uniquement")
    val shellHint get() = text("Shell + hint", "Shell + hint", "Shell + Hinweis", "Shell + pista", "Shell + indice")
    val shell get() = text("Shell", "Shell", "Shell", "Shell", "Shell")
    val riskCriticalLabel get() = text("Critica", "Critical", "Kritisch", "Crítica", "Critique")
    val riskHighLabel get() = text("Alta", "High", "Hoch", "Alta", "Élevé")
    val riskMediumLabel get() = text("Media", "Medium", "Mittel", "Media", "Moyen")
    val riskLowLabel get() = text("Bassa", "Low", "Niedrig", "Baja", "Faible")
    val packageAnalyzed get() = text("Package analizzato", "Analyzed package", "Analysiertes Package", "Paquete analizado", "Package analysé")
    val generatedActivityReport get() = text("Report generato dalla sezione Activity con dettaglio intent e indicazioni PenTest.", "Report generated by the Activity section with intent details and PenTest guidance.", "Bericht aus dem Activity-Bereich mit Intent-Details und PenTest-Hinweisen.", "Informe generado por la sección Activity con detalles de intent y orientación PenTest.", "Rapport généré par la section Activity avec détails d’intent et conseils PenTest.")
    val totalActivities get() = text("Activity totali", "Total activities", "Activities insgesamt", "Actividades totales", "Activités totales")
    val shellLaunchable get() = text("Avviabili via shell", "Shell-launchable", "Über Shell startbar", "Iniciables por shell", "Lancées via shell")
    val activityDumpOnlyLabel get() = text("Solo dump", "Dump only", "Nur Dump", "Solo volcado", "Dump uniquement")
    val keyObservations get() = text("Osservazioni chiave", "Key observations", "Wichtige Beobachtungen", "Observaciones clave", "Observations clés")
    val vulnerabilityProfile get() = text("Profilo vulnerabilità", "Vulnerability profile", "Schwachstellenprofil", "Perfil de vulnerabilidad", "Profil de vulnérabilité")
    val noAdditionalRecommendation get() = text("Nessuna raccomandazione aggiuntiva oltre ai test di reachability.", "No additional recommendations beyond reachability tests.", "Keine zusätzlichen Empfehlungen über Reachability-Tests hinaus.", "No hay recomendaciones adicionales más allá de las pruebas de accesibilidad.", "Aucune recommandation supplémentaire au-delà des tests de joignabilité.")
    val riskColumn get() = text("Rischio", "Risk", "Risiko", "Riesgo", "Risque")
    val detailsColumn get() = text("Dettagli", "Details", "Details", "Detalles", "Détails")
    val exportHtml
        get() = text(
            "Esporta report HTML",
            "Export HTML report",
            "HTML-Bericht exportieren",
            "Exportar informe HTML",
            "Exporter le rapport HTML",
        )
    val closeReport
        get() = text(
            "Chiudi report",
            "Close report",
            "Bericht schließen",
            "Cerrar informe",
            "Fermer le rapport",
        )
    val exportedTitle
        get() = text(
            "Report activity esportato",
            "Activity report exported",
            "Aktivitätsbericht exportiert",
            "Informe de actividades exportado",
            "Rapport d’activités exporté",
        )
    val exportedSummary
        get() = text(
            "Report HTML pronto per consultazione desktop.",
            "HTML report ready for desktop viewing.",
            "HTML-Bericht zur Desktop-Anzeige bereit.",
            "Informe HTML listo para consultar en escritorio.",
            "Rapport HTML prêt pour la consultation sur bureau.",
        )
    val appDetail get() = text("App: %s", "App: %s", "App: %s", "App: %s", "App : %s")
    val packageDetail get() = text("Package: %s", "Package: %s", "Package: %s", "Package: %s", "Package : %s")
    val activitiesDetail
        get() = text(
            "Activity incluse: %d",
            "Activities included: %d",
            "Enthaltene Aktivitäten: %d",
            "Actividades incluidas: %d",
            "Activités incluses : %d",
        )
    val maximumRiskDetail
        get() = text(
            "Pericolosità massima: %s",
            "Maximum risk: %s",
            "Maximales Risiko: %s",
            "Riesgo máximo: %s",
            "Risque maximal : %s",
        )
    val riskLow get() = text("BASSA", "LOW", "NIEDRIG", "BAJO", "FAIBLE")
    val riskMedium get() = text("MEDIA", "MEDIUM", "MITTEL", "MEDIO", "MOYEN")
    val riskHigh get() = text("ALTA", "HIGH", "HOCH", "ALTO", "ÉLEVÉ")
    val riskCritical get() = text("CRITICA", "CRITICAL", "KRITISCH", "CRÍTICO", "CRITIQUE")
    val findingDumpOnly
        get() = text(
            "%d activity compaiono solo nel dump del package e richiedono verifica manuale.",
            "%d activities appear only in the package dump and require manual review.",
            "%d Aktivitäten erscheinen nur im Package-Dump und müssen manuell geprüft werden.",
            "%d actividades solo aparecen en el volcado del package y requieren revisión manual.",
            "%d activités apparaissent uniquement dans le dump du package et nécessitent une vérification manuelle.",
        )
    val findingGuided
        get() = text(
            "%d activity richiedono un intent contestualizzato per essere testate.",
            "%d activities require a contextual intent to be tested.",
            "%d Aktivitäten benötigen zum Testen einen Kontext-Intent.",
            "%d actividades requieren un intent contextual para probarse.",
            "%d activités nécessitent un intent contextuel pour être testées.",
        )
    val findingContextual
        get() = text(
            "%d activity espongono hint utili per un lancio guidato o per test deeplink.",
            "%d activities expose hints useful for guided launch or deeplink testing.",
            "%d Aktivitäten bieten Hinweise für geführte Starts oder Deeplink-Tests.",
            "%d actividades exponen pistas útiles para lanzamientos guiados o pruebas de deeplink.",
            "%d activités exposent des indices utiles pour un lancement guidé ou des tests de deeplink.",
        )
    val findingFavoritePackage
        get() = text(
            "L'app è stata marcata come preferita nella sezione Activity.",
            "The app was marked as a favorite in the Activity section.",
            "Die App wurde im Activity-Bereich als Favorit markiert.",
            "La app se marcó como favorita en la sección Actividades.",
            "L’app a été ajoutée aux favoris dans la section Activités.",
        )
    val findingFavoriteActivities
        get() = text(
            "%d activity sono state segnate come preferite per analisi ripetute.",
            "%d activities were marked as favorites for repeated analysis.",
            "%d Aktivitäten wurden für wiederholte Analysen favorisiert.",
            "%d actividades se marcaron como favoritas para análisis repetidos.",
            "%d activités ont été ajoutées aux favoris pour des analyses répétées.",
        )
    val findingRisky
        get() = text(
            "%d activity espongono superfici intent/deeplink con rischio %s.",
            "%d activities expose intent/deeplink surfaces with %s risk.",
            "%d Aktivitäten bieten Intent-/Deeplink-Oberflächen mit Risiko %s.",
            "%d actividades exponen superficies intent/deeplink con riesgo %s.",
            "%d activités exposent des surfaces intent/deeplink avec un risque %s.",
        )
    val findingAllShell
        get() = text(
            "Tutte le activity rilevate risultano avviabili via shell senza metadati aggiuntivi.",
            "All detected activities can be launched via shell without additional metadata.",
            "Alle erkannten Aktivitäten können ohne zusätzliche Metadaten über die Shell gestartet werden.",
            "Todas las actividades detectadas se pueden iniciar mediante shell sin metadatos adicionales.",
            "Toutes les activités détectées peuvent être lancées via shell sans métadonnées supplémentaires.",
        )
    val recommendationCritical
        get() = text(
            "Usa un device di test isolato, payload controllati e osserva gli effetti collaterali su file/provider/canali sensibili.",
            "Use an isolated test device, controlled payloads and observe side effects on sensitive files/providers/channels.",
            "Verwende ein isoliertes Testgerät, kontrollierte Payloads und beobachte Nebenwirkungen auf sensible Dateien/Provider/Kanäle.",
            "Usa un dispositivo de prueba aislado, payloads controlados y observa efectos secundarios en archivos/proveedores/canales sensibles.",
            "Utilisez un appareil de test isolé, des payloads contrôlés et observez les effets secondaires sur fichiers/fournisseurs/canaux sensibles.",
        )
    val recommendationHigh
        get() = text(
            "Avvia l'intent con dati innocui e aumenta gradualmente i payload per verificare sanitizzazione e routing.",
            "Launch the intent with harmless data, then increase payloads to verify sanitization and routing.",
            "Starte den Intent mit harmlosen Daten und erhöhe die Payloads schrittweise, um Sanitization und Routing zu prüfen.",
            "Inicia el intent con datos inocuos y aumenta gradualmente los payloads para verificar la sanitización y el enrutamiento.",
            "Lancez l’intent avec des données inoffensives, puis augmentez les payloads pour vérifier la sanitisation et le routage.",
        )
    val recommendationMedium
        get() = text(
            "Verifica host, parametri e comportamento con input malformati prima di test più invasivi.",
            "Check hosts, parameters and behavior with malformed input before more invasive tests.",
            "Prüfe Hosts, Parameter und Verhalten mit fehlerhaften Eingaben vor invasiveren Tests.",
            "Verifica hosts, parámetros y comportamiento con entradas malformadas antes de pruebas más invasivas.",
            "Vérifiez les hôtes, paramètres et comportements avec des entrées malformées avant des tests plus invasifs.",
        )
    val recommendationLow
        get() = text(
            "Nessuna pericolosità evidente: mantieni comunque test base di raggiungibilità.",
            "No obvious danger: still keep basic reachability tests.",
            "Keine offensichtliche Gefahr: behalte dennoch grundlegende Erreichbarkeitstests bei.",
            "No se observa peligro evidente: mantén pruebas básicas de accesibilidad.",
            "Aucun danger évident : maintenez toutefois des tests de joignabilité de base.",
        )
}

/** Localized copy shared by screen-level controls that are not domain data. */
