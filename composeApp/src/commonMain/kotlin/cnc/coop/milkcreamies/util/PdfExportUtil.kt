/*
 * Copyright 2025  MkaoCodes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cnc.coop.milkcreamies.util

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Utility for PDF export formatting
 */
object PdfExportUtil {

    /**
     * Convert CSV content to PDF formatted text
     * In a real implementation, this would use a PDF library to generate an actual PDF,
     * but for this demo we're just formatting the content with PDF "markers"
     *
     * @param csvContent The CSV content to convert
     * @param title The title for the PDF document
     * @return String representation of what would be a PDF (in a real app, this would return ByteArray)
     */
    fun convertCsvToPdfText(csvContent: String, title: String): String {
        val sb = StringBuilder()

        // Timestamp for the PDF
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

        sb.append("%PDF-1.7\n") // PDF file header marker for demonstration
        sb.append("% Generated PDF Document for Milk Cooperative\n")
        sb.append("% Created: $now\n")
        sb.append("\n")
        sb.append("1 0 obj\n")
        sb.append("<<\n")
        sb.append("  /Type /Catalog\n")
        sb.append("  /Pages 2 0 R\n")
        sb.append(">>\n")
        sb.append("endobj\n")
        sb.append("\n")
        sb.append("2 0 obj\n")
        sb.append("<<\n")
        sb.append("  /Type /Pages\n")
        sb.append("  /Count 1\n")
        sb.append("  /Kids [3 0 R]\n")
        sb.append(">>\n")
        sb.append("endobj\n")
        sb.append("\n")
        sb.append("3 0 obj\n")
        sb.append("<<\n")
        sb.append("  /Type /Page\n")
        sb.append("  /Parent 2 0 R\n")
        sb.append("  /Resources <<\n")
        sb.append("    /Font <<\n")
        sb.append("      /F1 4 0 R\n")
        sb.append("      /F2 5 0 R\n")
        sb.append("    >>\n")
        sb.append("  >>\n")
        sb.append("  /Contents 6 0 R\n")
        sb.append(">>\n")
        sb.append("endobj\n")
        sb.append("\n")
        sb.append("4 0 obj\n")
        sb.append("<<\n")
        sb.append("  /Type /Font\n")
        sb.append("  /Subtype /Type1\n")
        sb.append("  /BaseFont /Helvetica\n")
        sb.append(">>\n")
        sb.append("endobj\n")
        sb.append("\n")
        sb.append("5 0 obj\n")
        sb.append("<<\n")
        sb.append("  /Type /Font\n")
        sb.append("  /Subtype /Type1\n")
        sb.append("  /BaseFont /Helvetica-Bold\n")
        sb.append(">>\n")
        sb.append("endobj\n")
        sb.append("\n")
        sb.append("6 0 obj\n")
        sb.append("<<\n")
        sb.append("  /Length 1000\n")
        sb.append(">>\n")
        sb.append("stream\n")
        sb.append("BT\n")

        // Document title
        sb.append("  /F2 24 Tf\n")
        sb.append("  36 800 Td\n")
        sb.append("  ($title) Tj\n")

        // Date and time
        sb.append("  /F1 12 Tf\n")
        sb.append("  0 -20 Td\n")
        sb.append("  (Generated: $now) Tj\n")
        sb.append("  0 -30 Td\n")

        // Check if the content has table markers
        if (csvContent.contains("====") || csvContent.contains("|----")) {
            // This is a table-formatted report, use our enhanced table rendering
            convertFormattedTablesToPdf(sb, csvContent)
        } else {
            // Default CSV rendering (line by line)
            convertRawCsvToPdf(sb, csvContent)
        }

        // Finish up the PDF content
        sb.append("ET\n")
        sb.append("endstream\n")
        sb.append("endobj\n")
        sb.append("\n")
        sb.append("xref\n")
        sb.append("0 6\n")
        sb.append("0000000000 65535 f\n")
        sb.append("0000000010 00000 n\n")
        sb.append("0000000089 00000 n\n")
        sb.append("0000000173 00000 n\n")
        sb.append("0000000301 00000 n\n")
        sb.append("0000000380 00000 n\n")
        sb.append("\n")
        sb.append("trailer\n")
        sb.append("<<\n")
        sb.append("  /Size 6\n")
        sb.append("  /Root 1 0 R\n")
        sb.append(">>\n")
        sb.append("startxref\n")
        sb.append("1000\n")
        sb.append("%%EOF\n")

        return sb.toString()
    }

    /**
     * Convert a formatted table (with table borders) to PDF commands
     */
    private fun convertFormattedTablesToPdf(sb: StringBuilder, content: String) {
        val lines = content.split("\n")
        var y = 750 // Start Y position for content
        var isTableHeader = false
        var currentSection = ""

        for (line in lines) {
            when {
                line.startsWith("====") -> {
                    // Section header
                    y -= 20  // Extra space before section
                    sb.append("  /F2 16 Tf\n") // Bold font, larger
                    sb.append("  36 $y Td\n")
                    val sectionName = line.replace("=", "").trim()
                    currentSection = sectionName
                    sb.append("  ($sectionName) Tj\n")
                    y -= 20
                    isTableHeader = true
                }

                line.startsWith("| ") -> {
                    // Table row
                    if (isTableHeader) {
                        // It's a header row, make it bold
                        sb.append("  /F2 12 Tf\n") // Bold font
                        isTableHeader = false
                    } else {
                        sb.append("  /F1 12 Tf\n") // Regular font
                    }

                    // Position for this row
                    sb.append("  36 $y Td\n")

                    // Clean up the line for PDF (remove table chars)
                    val rowText = line.replace("|", " ").trim()
                    sb.append("  ($rowText) Tj\n")

                    // Move down for next row
                    y -= 16

                    if (y < 50) {
                        y = 750
                        sb.append("  /F1 10 Tf\n")
                        sb.append("  36 $y Td\n")
                        sb.append("  (Continued on next page...) Tj\n")
                        y -= 20
                    }
                }

                line.startsWith("|--") -> {
                    // Table separator line - skip in PDF but keep the header flag
                    continue
                }

                line.isBlank() -> {
                    // Extra spacing for readability
                    y -= 10
                }

                else -> {
                    // Regular text line
                    sb.append("  /F1 12 Tf\n")
                    sb.append("  36 $y Td\n")
                    sb.append("  ($line) Tj\n")
                    y -= 16
                }
            }
        }

        // Add footer with page count
        sb.append("  /F1 10 Tf\n")
        sb.append("  36 30 Td\n")
        sb.append("  (CMFC Milk Admin - Page 1) Tj\n")
    }

    /**
     * Convert raw CSV to PDF lines (simpler version without table formatting)
     */
    private fun convertRawCsvToPdf(sb: StringBuilder, csvContent: String) {
        val lines = csvContent.split("\n")
        var yPosition = 700

        // Add each CSV row as text in the PDF
        lines.forEachIndexed { index, line ->
            if (index < 100) {
                sb.append("  0 -16 Td\n")

                // Replace commas with tabs for better readability
                val formattedLine = line.replace(",", "    ").replace("\"", "")
                sb.append("  ($formattedLine) Tj\n")
                yPosition -= 16
            }
        }

        // Add page numbers and note about truncation
        if (lines.size > 50) {
            sb.append("  0 -24 Td\n")
            sb.append("  (... Showing 50/${lines.size} rows. Download CSV for complete data.) Tj\n")
        }
    }
}
