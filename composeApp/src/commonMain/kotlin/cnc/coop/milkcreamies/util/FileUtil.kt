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
import kotlinx.datetime.todayIn

/**
 * Platform-independent file utility interface
 * Actual implementations will be provided in platform-specific source sets
 */
expect object PlatformFileUtil {
    /**
     * Save content to a file on the user's device
     *
     * @param content The content to save
     * @param filename The suggested filename
     * @param mimeType The MIME type of the content
     * @return Success or Failure result
     */
    suspend fun saveToDevice(content: String, filename: String, mimeType: String): Result<String>

    /**
     * Check if the platform supports direct file saving
     *
     * @return True if direct file saving is supported
     */
    fun supportsDirectFileSave(): Boolean

    /**
     * Get the default download directory path
     *
     * @return The path to the default download directory or null if not applicable
     */
    fun getDefaultDownloadPath(): String?
}

/**
 * Utility for file operations including generating filenames and saving files to device
 */
object FileUtil {
    /**
     * File format options for exports
     */
    enum class ExportFormat(val extension: String, val mimeType: String) {
        CSV("csv", "text/csv"),
        PDF("pdf", "application/pdf"),
        EXCEL("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    }

    /**
     * Generate timestamped filename
     *
     * @param baseName Base name of the file
     * @param extension File extension (without dot)
     * @return Timestamped filename
     */
    fun generateTimestampedFilename(baseName: String, extension: String): String {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val timestamp = "${today.year}-${
            today.monthNumber.toString().padStart(2, '0')
        }-${today.dayOfMonth.toString().padStart(2, '0')}"
        return "${sanitizeFilename(baseName)}_${timestamp}.${extension}"
    }

    /**
     * Sanitize filename by removing invalid characters
     *
     * @param filename Raw filename
     * @return Sanitized filename
     */
    fun sanitizeFilename(filename: String): String {
        return filename.replace(Regex("[\\\\/:*?\"<>|]"), "_")
    }

    /**
     * Save content to downloads folder
     *
     * @param content Content to save
     * @param filename Suggested filename
     * @param format Export format
     * @return Result with success or failure
     */
    suspend fun saveToDownloads(
        content: String,
        filename: String,
        format: ExportFormat = ExportFormat.CSV
    ): Result<String> {
        val sanitizedFilename = sanitizeFilename(filename)
        return PlatformFileUtil.saveToDevice(content, sanitizedFilename, format.mimeType)
    }

    /**
     * Check if the platform supports direct file saving
     */
    fun supportsDirectFileSave(): Boolean {
        return PlatformFileUtil.supportsDirectFileSave()
    }

    /**
     * Generate content for console output as fallback
     *
     * @param content The content to save
     * @param filename The filename
     * @return Console output with instructions
     */
    fun generateConsoleOutput(content: String, filename: String): String {
        return """
            ===== EXPORT: $filename =====
            $content
            ===== END EXPORT =====
            
            To save this file:
            1. Copy all content between the markers
            2. Paste into a text editor
            3. Save as "$filename"
            4. Open with appropriate application
        """.trimIndent()
    }
}

/**
 * Implementations for specific platforms will be added in their respective source sets.
 * For example:
 * - composeApp/src/jvmMain/kotlin/cnc/coop/milkcreamies/util/PlatformFileUtilJvm.kt
 * - composeApp/src/jsMain/kotlin/cnc/coop/milkcreamies/util/PlatformFileUtilJs.kt
 */
