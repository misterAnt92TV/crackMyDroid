package com.crackmydroid.shared.presentation.components.card

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.crackmydroid.shared.i18n.LocalStrings
import com.crackmydroid.shared.presentation.components.text.AppText
import com.crackmydroid.shared.presentation.preview.AppComponentPreviews
import com.crackmydroid.shared.presentation.preview.ComponentPreviewContainer

@Composable
fun ExportDetailsCard(
    path: String,
    modifier: Modifier = Modifier,
    title: String = "Dettagli export",
    summary: String? = null,
    details: List<String> = emptyList()
) {
    val normalizedPath = path.trim()
    val fileName = normalizedPath.substringAfterLast('/').substringAfterLast('\\')
    val extension = fileName.substringAfterLast('.', "").uppercase().ifBlank { "TXT" }
    val directory = when {
        normalizedPath.isBlank() -> "-"
        fileName.isBlank() -> normalizedPath
        else -> normalizedPath.removeSuffix(fileName).trimEnd('/', '\\').ifBlank { "-" }
    }

    val strings = LocalStrings.current
    AppCard(modifier = modifier.fillMaxWidth()) {
        AppText(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        summary?.takeIf { it.isNotBlank() }?.let {
            AppText(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        AppText(
            text = strings.exportFile.format(fileName),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        AppText(
            text = strings.exportFormat.format(extension),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
        AppText(
            text = strings.exportPath.format(normalizedPath),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        AppText(
            text = strings.exportDirectory.format(directory),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        details.forEach { line ->
            AppText(
                text = "• $line",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@AppComponentPreviews
@Composable
private fun ExportDetailsCardPreview() = ComponentPreviewContainer {
    ExportDetailsCard(
        path = "/storage/emulated/0/Download/crackmydroid-report.txt",
        title = "Dettagli export",
        summary = "Report PenTest generato correttamente",
        details = listOf("12 controlli eseguiti", "2 avvisi da verificare")
    )
}
