package com.crackmydroid.shared.util

internal object TextPdfWriter {
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 42
    private const val LINE_HEIGHT = 16
    private const val MAX_LINE_LENGTH = 90
    private const val LINES_PER_PAGE = 47

    fun write(text: String): ByteArray {
        val lines = text.lineSequence().flatMap(::wrap).toList().ifEmpty { listOf("") }
        val pages = lines.chunked(LINES_PER_PAGE)
        val objects = mutableListOf<String>()
        fun addObject(content: String): Int {
            objects += content
            return objects.size
        }

        val catalogId = addObject("")
        val pagesId = addObject("")
        val fontId = addObject("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>")
        val pageIds = pages.map { addObject("") }
        val contentIds = pages.map { pageLines -> addObject(contentStream(pageLines)) }

        objects[catalogId - 1] = "<< /Type /Catalog /Pages $pagesId 0 R >>"
        objects[pagesId - 1] =
            "<< /Type /Pages /Kids [${pageIds.joinToString(" ") { "$it 0 R" }}] /Count ${pages.size} >>"
        pages.indices.forEach { index ->
            objects[pageIds[index] - 1] =
                "<< /Type /Page /Parent $pagesId 0 R /MediaBox [0 0 $PAGE_WIDTH $PAGE_HEIGHT] " +
                    "/Resources << /Font << /F1 $fontId 0 R >> >> /Contents ${contentIds[index]} 0 R >>"
        }

        val pdf = StringBuilder("%PDF-1.4\n")
        val offsets = mutableListOf<Int>()
        objects.forEachIndexed { index, content ->
            offsets += pdf.length
            pdf.append("${index + 1} 0 obj\n$content\nendobj\n")
        }
        val xrefOffset = pdf.length
        pdf.append("xref\n0 ${objects.size + 1}\n")
        pdf.append("0000000000 65535 f \n")
        offsets.forEach { offset -> pdf.append(offset.toString().padStart(10, '0')).append(" 00000 n \n") }
        pdf.append("trailer\n<< /Size ${objects.size + 1} /Root $catalogId 0 R >>\n")
        pdf.append("startxref\n$xrefOffset\n%%EOF\n")
        return pdf.toString().encodeToByteArray()
    }

    private fun contentStream(lines: List<String>): String = buildString {
        append("BT\n/F1 10 Tf\n$MARGIN ${PAGE_HEIGHT - MARGIN} Td\n")
        lines.forEach { line ->
            append('<')
            line.forEach { character -> append(character.toWinAnsiByte().toString(16).padStart(2, '0')) }
            append("> Tj\n0 -$LINE_HEIGHT Td\n")
        }
        append("ET")
    }.let { stream -> "<< /Length ${stream.encodeToByteArray().size} >>\nstream\n$stream\nendstream" }

    private fun wrap(line: String): List<String> {
        if (line.isEmpty()) return listOf("")
        val wrapped = mutableListOf<String>()
        var remaining = line
        while (remaining.length > MAX_LINE_LENGTH) {
            val split = remaining.lastIndexOf(' ', MAX_LINE_LENGTH).takeIf { it > 0 } ?: MAX_LINE_LENGTH
            wrapped += remaining.take(split).trimEnd()
            remaining = remaining.drop(split).trimStart()
        }
        wrapped += remaining
        return wrapped
    }

    private fun Char.toWinAnsiByte(): Int = when (code) {
        in 0x20..0x7E, in 0xA0..0xFF -> code
        0x20AC -> 0x80
        0x201A -> 0x82
        0x0192 -> 0x83
        0x201E -> 0x84
        0x2026 -> 0x85
        0x2020 -> 0x86
        0x2021 -> 0x87
        0x02C6 -> 0x88
        0x2030 -> 0x89
        0x0160 -> 0x8A
        0x2039 -> 0x8B
        0x0152 -> 0x8C
        0x017D -> 0x8E
        0x2018 -> 0x91
        0x2019 -> 0x92
        0x201C -> 0x93
        0x201D -> 0x94
        0x2022 -> 0x95
        0x2013 -> 0x96
        0x2014 -> 0x97
        0x02DC -> 0x98
        0x2122 -> 0x99
        0x0161 -> 0x9A
        0x203A -> 0x9B
        0x0153 -> 0x9C
        0x017E -> 0x9E
        0x0178 -> 0x9F
        else -> 0x3F
    }
}
