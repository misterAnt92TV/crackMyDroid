package com.crackmydroid.shared.domain.model

enum class ActivityIntentRiskLevel {
    Low,
    Medium,
    High,
    Critical
}

data class ActivityIntentRiskFinding(
    val level: ActivityIntentRiskLevel,
    val title: String,
    val detail: String
)

data class ActivityIntentRiskReport(
    val entry: ActivityEntry,
    val level: ActivityIntentRiskLevel,
    val score: Int,
    val launchMode: String,
    val recommendation: String,
    val findings: List<ActivityIntentRiskFinding>
) {
    val summary: String
        get() = findings.firstOrNull()?.detail ?: "Nessuna esposizione particolare rilevata."
}

object ActivityIntentRiskAnalyzer {
    fun analyze(entry: ActivityEntry): ActivityIntentRiskReport {
        val findings = linkedMapOf<String, ActivityIntentRiskFinding>()
        val action = entry.launchIntentAction?.trim().orEmpty()
        val normalizedAction = action.uppercase()
        val dataUri = entry.launchIntentData?.trim().orEmpty()
        val scheme = extractScheme(dataUri)
        val mimeType = entry.launchIntentMimeType?.trim().orEmpty()

        if (!entry.launchableViaShell && entry.hasLaunchMetadata()) {
            register(
                findings,
                "guided",
                ActivityIntentRiskFinding(
                    level = ActivityIntentRiskLevel.High,
                    title = "Guided launch richiesto",
                    detail = "L'activity richiede un intent contestualizzato e merita test dedicati su deeplink/input."
                )
            )
        } else if (!entry.launchableViaShell) {
            register(
                findings,
                "dump_only",
                ActivityIntentRiskFinding(
                    level = ActivityIntentRiskLevel.Medium,
                    title = "Componente solo dump",
                    detail = "La componente compare nel dump del package ma non risulta avviabile direttamente."
                )
            )
        }

        when {
            normalizedAction.contains("ACTION_CALL") || normalizedAction.contains("CALL_PRIVILEGED") ->
                register(
                    findings,
                    "call_action",
                    ActivityIntentRiskFinding(
                        level = ActivityIntentRiskLevel.Critical,
                        title = "Intent telefonico sensibile",
                        detail = "L'intent puo innescare chiamate o flussi telefonici con impatto immediato."
                    )
                )
            normalizedAction.contains("ACTION_SEND") || normalizedAction.contains("ACTION_SENDTO") ->
                register(
                    findings,
                    "send_action",
                    ActivityIntentRiskFinding(
                        level = ActivityIntentRiskLevel.High,
                        title = "Intent di condivisione",
                        detail = "L'activity accetta payload da share/send: verificare input injection ed esposizione dati."
                    )
                )
            normalizedAction.contains("ACTION_VIEW") && scheme.isNotBlank() ->
                register(
                    findings,
                    "view_action",
                    ActivityIntentRiskFinding(
                        level = when (scheme) {
                            "http", "https" -> ActivityIntentRiskLevel.Medium
                            "file", "content", "intent" -> ActivityIntentRiskLevel.Critical
                            else -> ActivityIntentRiskLevel.High
                        },
                        title = "Entry-point deeplink/view",
                        detail = "L'activity espone un endpoint VIEW su schema ${scheme.ifBlank { "non specificato" }}."
                    )
                )
            normalizedAction.contains("PROCESS_TEXT") || normalizedAction.contains("PICK") || normalizedAction.contains("EDIT") ->
                register(
                    findings,
                    "input_action",
                    ActivityIntentRiskFinding(
                        level = ActivityIntentRiskLevel.High,
                        title = "Intent orientato a input esterno",
                        detail = "Accetta input contestuale da altre app: utile per test injection/manipolazione contenuti."
                    )
                )
        }

        when (scheme) {
            "file", "content" ->
                register(
                    findings,
                    "file_scheme",
                    ActivityIntentRiskFinding(
                        level = ActivityIntentRiskLevel.Critical,
                        title = "URI file/content",
                        detail = "Gestisce URI locali/content provider: verificare path traversal, permessi e leakage."
                    )
                )
            "intent" ->
                register(
                    findings,
                    "intent_scheme",
                    ActivityIntentRiskFinding(
                        level = ActivityIntentRiskLevel.High,
                        title = "Intent URI annidato",
                        detail = "Supporta intent://: verificare sanitizzazione dei parametri e chaining tra componenti."
                    )
                )
            "tel", "sms", "smsto", "mms", "mmsto", "mailto" ->
                register(
                    findings,
                    "sensitive_scheme",
                    ActivityIntentRiskFinding(
                        level = ActivityIntentRiskLevel.High,
                        title = "Schema sensibile",
                        detail = "Lo schema $scheme puo attivare canali sensibili o azioni verso app di sistema."
                    )
                )
            "http", "https" ->
                register(
                    findings,
                    "web_scheme",
                    ActivityIntentRiskFinding(
                        level = ActivityIntentRiskLevel.Medium,
                        title = "Deeplink web",
                        detail = "Endpoint web da validare con open redirect, host confusion e parametri malevoli."
                    )
                )
            else -> if (scheme.isNotBlank()) {
                register(
                    findings,
                    "custom_scheme",
                    ActivityIntentRiskFinding(
                        level = ActivityIntentRiskLevel.High,
                        title = "Custom scheme",
                        detail = "Schema custom $scheme da verificare con payload arbitrari e collisioni di deeplink."
                    )
                )
            }
        }

        if (mimeType.isNotBlank()) {
            val level = if (mimeType.contains("*")) ActivityIntentRiskLevel.High else ActivityIntentRiskLevel.Medium
            register(
                findings,
                "mime_type",
                ActivityIntentRiskFinding(
                    level = level,
                    title = "Filtro MIME",
                    detail = "Accetta MIME type $mimeType: verificare parsing file e contenuti inattesi."
                )
            )
        }

        val orderedFindings = findings.values.sortedWith(
            compareByDescending<ActivityIntentRiskFinding> { weight(it.level) }
                .thenBy { it.title }
        )
        val score = orderedFindings.sumOf { weight(it.level) }
        val level = orderedFindings.maxByOrNull { weight(it.level) }?.level ?: ActivityIntentRiskLevel.Low
        val launchMode = when {
            !entry.launchableViaShell && entry.hasLaunchMetadata() -> "guided-intent"
            !entry.launchableViaShell -> "dump-only"
            entry.hasLaunchMetadata() -> "shell+intent"
            else -> "shell"
        }
        val recommendation = when (level) {
            ActivityIntentRiskLevel.Critical ->
                "Usa device di test isolato, payload controllati e osserva side-effect su file/provider/canali sensibili."
            ActivityIntentRiskLevel.High ->
                "Avvia l'intent con dati innocui e poi aumenta i payload per verificare sanitizzazione e routing."
            ActivityIntentRiskLevel.Medium ->
                "Verifica host, parametri e comportamento con input malformed prima di test piu invasivi."
            ActivityIntentRiskLevel.Low ->
                "Nessuna pericolosita evidente: mantieni comunque test base di reachability."
        }

        return ActivityIntentRiskReport(
            entry = entry,
            level = level,
            score = score,
            launchMode = launchMode,
            recommendation = recommendation,
            findings = orderedFindings
        )
    }

    fun summarize(reports: List<ActivityIntentRiskReport>): ActivityIntentRiskLevel =
        reports.maxByOrNull { weight(it.level) }?.level ?: ActivityIntentRiskLevel.Low

    fun formatLevel(level: ActivityIntentRiskLevel): String = when (level) {
        ActivityIntentRiskLevel.Low -> "BASSA"
        ActivityIntentRiskLevel.Medium -> "MEDIA"
        ActivityIntentRiskLevel.High -> "ALTA"
        ActivityIntentRiskLevel.Critical -> "CRITICA"
    }

    private fun register(
        target: MutableMap<String, ActivityIntentRiskFinding>,
        key: String,
        finding: ActivityIntentRiskFinding
    ) {
        val current = target[key]
        if (current == null || weight(finding.level) > weight(current.level)) {
            target[key] = finding
        }
    }

    private fun extractScheme(dataUri: String): String {
        val raw = dataUri.substringBefore(':', missingDelimiterValue = "").trim()
        return raw.lowercase()
    }

    private fun weight(level: ActivityIntentRiskLevel): Int = when (level) {
        ActivityIntentRiskLevel.Low -> 0
        ActivityIntentRiskLevel.Medium -> 1
        ActivityIntentRiskLevel.High -> 3
        ActivityIntentRiskLevel.Critical -> 5
    }

    private fun ActivityEntry.hasLaunchMetadata(): Boolean =
        !launchContextHint.isNullOrBlank() ||
            !launchIntentAction.isNullOrBlank() ||
            !launchIntentData.isNullOrBlank() ||
            !launchIntentMimeType.isNullOrBlank()
}
