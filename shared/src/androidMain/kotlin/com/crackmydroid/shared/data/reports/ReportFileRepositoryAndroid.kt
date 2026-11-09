package com.crackmydroid.shared.data.reports

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.crackmydroid.shared.domain.model.ExportDocument
import com.crackmydroid.shared.domain.model.ExportFormat
import com.crackmydroid.shared.domain.repository.ReportFileRepository
import com.crackmydroid.shared.util.exportDirectory
import com.crackmydroid.shared.util.saveTextFile
import com.crackmydroid.shared.util.shareTextFile
import java.io.File
import java.io.FileOutputStream

class ReportFileRepositoryAndroid : ReportFileRepository {
    override suspend fun save(fileName: String, content: String): Result<String> = saveTextFile(fileName, content)

    override suspend fun share(path: String): Result<Unit> = shareTextFile(path)

    override suspend fun save(document: ExportDocument): Result<String> = when (document.format) {
        ExportFormat.TXT -> saveTextFile(document.fileName, document.textContent)
        ExportFormat.JSON -> saveTextFile(document.fileName, document.jsonEnvelope().toString())
        ExportFormat.PDF -> savePdf(document)
    }

    private fun savePdf(document: ExportDocument): Result<String> = runCatching {
        val output = File(exportDirectory(), document.fileName)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 11f }
        val pageWidth = 595
        val pageHeight = 842
        val margin = 42f
        val lineHeight = 16f
        val pdf = PdfDocument()
        var activePage: PdfDocument.Page? = null
        try {
            var pageNumber = 1
            var page = pdf.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
            activePage = page
            var canvas = page.canvas
            var y = margin
            document.textContent.lineSequence().forEach { line ->
                wrap(line, paint, pageWidth - margin * 2).forEach { wrapped ->
                    if (y + lineHeight > pageHeight - margin) {
                        pdf.finishPage(page)
                        activePage = null
                        pageNumber += 1
                        page = pdf.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                        activePage = page
                        canvas = page.canvas
                        y = margin
                    }
                    canvas.drawText(wrapped, margin, y, paint)
                    y += lineHeight
                }
            }
            pdf.finishPage(page)
            activePage = null
            FileOutputStream(output).use { stream -> pdf.writeTo(stream) }
        } finally {
            try {
                activePage?.let(pdf::finishPage)
            } finally {
                pdf.close()
            }
        }
        output.absolutePath
    }

    private fun wrap(line: String, paint: Paint, maxWidth: Float): List<String> {
        if (line.isEmpty()) return listOf("")
        val lines = mutableListOf<String>()
        var remaining = line
        while (remaining.isNotEmpty()) {
            val count = paint.breakText(remaining, true, maxWidth, null)
            val split = if (count ==
                remaining.length
            ) {
                count
            } else {
                remaining.lastIndexOf(' ', count).takeIf { it > 0 } ?: count
            }
            lines += remaining.take(split).trimEnd()
            remaining = remaining.drop(split).trimStart()
        }
        return lines
    }
}
