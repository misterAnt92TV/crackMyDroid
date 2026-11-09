package com.crackmydroid.shared.data.reports

import com.crackmydroid.shared.domain.model.ExportDocument
import com.crackmydroid.shared.domain.model.ExportFormat
import com.crackmydroid.shared.domain.repository.ReportFileRepository
import com.crackmydroid.shared.util.DesktopPaths
import com.crackmydroid.shared.util.saveTextFile
import com.crackmydroid.shared.util.shareTextFile
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import java.io.File

class ReportFileRepositoryDesktop : ReportFileRepository {
    override suspend fun save(fileName: String, content: String): Result<String> = saveTextFile(fileName, content)

    override suspend fun share(path: String): Result<Unit> = shareTextFile(path)

    override suspend fun save(document: ExportDocument): Result<String> = when (document.format) {
        ExportFormat.TXT -> saveTextFile(document.fileName, document.textContent)
        ExportFormat.JSON -> saveTextFile(document.fileName, document.jsonEnvelope().toString())
        ExportFormat.PDF -> savePdf(document)
    }

    private fun savePdf(document: ExportDocument): Result<String> = runCatching {
        val output = File(DesktopPaths.exportsDir(), document.fileName)
        val font = PDType1Font(Standard14Fonts.FontName.HELVETICA)
        val margin = 48f
        val leading = 15f
        PDDocument().use { pdf ->
            var page = PDPage(PDRectangle.A4)
            pdf.addPage(page)
            var stream = PDPageContentStream(pdf, page)
            var y = page.mediaBox.upperRightY - margin
            fun nextPage() {
                stream.close()
                page = PDPage(PDRectangle.A4)
                pdf.addPage(page)
                stream = PDPageContentStream(pdf, page)
                y = page.mediaBox.upperRightY - margin
            }
            stream.beginText()
            stream.setFont(font, 10f)
            stream.newLineAtOffset(margin, y)
            document.textContent.lineSequence().forEach { line ->
                wrap(line, 92).forEach { wrapped ->
                    if (y - leading < margin) {
                        stream.endText()
                        nextPage()
                        stream.beginText()
                        stream.setFont(font, 10f)
                        stream.newLineAtOffset(margin, y)
                    }
                    stream.showText(wrapped.sanitizePdfText())
                    stream.newLineAtOffset(0f, -leading)
                    y -= leading
                }
            }
            stream.endText()
            stream.close()
            pdf.save(output)
        }
        output.absolutePath
    }

    private fun wrap(line: String, maximumCharacters: Int): List<String> =
        line.chunked(maximumCharacters).ifEmpty { listOf("") }

    private fun String.sanitizePdfText(): String = map { if (it.code in 32..255) it else '?' }.joinToString("")
}
