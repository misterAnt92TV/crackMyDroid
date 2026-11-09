package com.crackmydroid.shared.data.reports

import com.crackmydroid.shared.domain.model.ExportDocument
import com.crackmydroid.shared.domain.model.ExportFormat
import com.crackmydroid.shared.util.TextPdfWriter
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper

class ReportFileRepositoryDesktopTest {
    @Test
    fun textPdfWriterCreatesValidPagesAndPreservesItalianAccents() {
        val content = buildString {
            appendLine("Attività già pronte")
            repeat(60) { appendLine("Riga di verifica $it") }
            append("Fine del report")
        }

        Loader.loadPDF(TextPdfWriter.write(content)).use { pdf ->
            assertTrue(pdf.numberOfPages >= 2)
            val extracted = PDFTextStripper().getText(pdf)
            assertTrue(extracted.contains("Attività già pronte"))
            assertTrue(extracted.contains("Fine del report"))
        }
    }

    @Test
    fun savePdfProducesAValidPaginatedDocument() = runTest {
        val fileName = "pdf_validation_${System.nanoTime()}"
        val content = buildString {
            appendLine("PDF export header")
            repeat(120) { appendLine("Report row $it contains printable content.") }
            append("PDF export footer")
        }
        val document = ExportDocument.textOnly(fileName, "validation", content, ExportFormat.PDF)
        var output: File? = null

        try {
            output = File(ReportFileRepositoryDesktop().save(document).getOrThrow())
            assertEquals("$fileName.pdf", output.name)
            Loader.loadPDF(output).use { pdf ->
                assertTrue(pdf.numberOfPages >= 2)
                val extracted = PDFTextStripper().getText(pdf)
                assertTrue(extracted.contains("PDF export header"))
                assertTrue(extracted.contains("PDF export footer"))
            }
        } finally {
            output?.delete()
        }
    }

    @Test
    fun saveJsonUsesTheDocumentExtensionAndEnvelope() = runTest {
        val fileName = "json_validation_${System.nanoTime()}"
        val document = ExportDocument.textOnly(fileName, "validation", "report body", ExportFormat.JSON)
        var output: File? = null

        try {
            output = File(ReportFileRepositoryDesktop().save(document).getOrThrow())
            assertEquals("$fileName.json", output.name)
            assertTrue(output.readText().contains("\"reportType\":\"validation\""))
            assertTrue(output.readText().contains("\"content\":\"report body\""))
        } finally {
            output?.delete()
        }
    }
}
