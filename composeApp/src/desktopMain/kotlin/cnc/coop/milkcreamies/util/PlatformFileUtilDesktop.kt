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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * Desktop implementation of PlatformFileUtil for Windows
 * This is essentially identical to the JVM implementation but needed for the desktop target
 */
actual object PlatformFileUtil {
    /**
     * Save content to a file on the user's device using Java's file APIs
     * This will show a file save dialog to allow user to select location
     */
    actual suspend fun saveToDevice(
        content: String,
        filename: String,
        mimeType: String
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Determine file extension from mimetype
                val extension = when (mimeType) {
                    "text/csv" -> "csv"
                    "application/pdf" -> "pdf"
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> "xlsx"
                    else -> "txt"
                }

                // Try to use AWT FileDialog first (more native look on Windows)
                val fileDialog = FileDialog(null as Frame?, "Save Report", FileDialog.SAVE)
                fileDialog.file = filename
                fileDialog.setFilenameFilter { _, name -> name.endsWith(".$extension") }
                fileDialog.isVisible = true

                // If user cancels, fall back to JFileChooser
                val selectedFile = if (fileDialog.file != null) {
                    val path = if (fileDialog.directory != null) {
                        fileDialog.directory + fileDialog.file
                    } else {
                        fileDialog.file
                    }
                    File(path)
                } else {
                    val fileChooser = JFileChooser()
                    fileChooser.dialogTitle = "Save Report"
                    fileChooser.fileFilter = FileNameExtensionFilter(
                        "Report files (*.$extension)", extension
                    )
                    getDefaultDownloadPath()?.let {
                        fileChooser.selectedFile =
                            File(it + File.separator + filename)
                    }

                    val result = fileChooser.showSaveDialog(null)
                    if (result == JFileChooser.APPROVE_OPTION) {
                        fileChooser.selectedFile
                    } else {
                        return@withContext Result.failure(Exception("File save cancelled"))
                    }
                }

                // Check if filename has extension, add if not
                val finalFile = if (!selectedFile.name.contains(".")) {
                    File(selectedFile.parentFile, "${selectedFile.name}.$extension")
                } else {
                    selectedFile
                }

                // Write the content to the file
                finalFile.writeText(content)
                Result.success(finalFile.absolutePath)

            } catch (e: Exception) {
                // If GUI dialogs fail, try direct save to Downloads folder
                try {
                    getDefaultDownloadPath()?.let { path ->
                        val downloadsFile = File(path, filename)
                        downloadsFile.writeText(content)
                        Result.success(downloadsFile.absolutePath)
                    }
                        ?: return@withContext Result.failure(Exception("Could not determine download path"))
                } catch (e2: Exception) {
                    Result.failure(Exception("Failed to save file: ${e2.message}", e2))
                }
            }
        }
    }

    /**
     * Check if platform supports direct file saving - always true for desktop
     */
    actual fun supportsDirectFileSave(): Boolean = true

    /**
     * Get the default downloads folder path for Windows
     */
    actual fun getDefaultDownloadPath(): String? {
        // Try to get Windows Downloads folder from registry/environment
        val userHome = System.getProperty("user.home")
        val windowsDownloads = Paths.get(userHome, "Downloads")

        // Check if Downloads directory exists, create if not
        if (!Files.exists(windowsDownloads)) {
            try {
                Files.createDirectories(windowsDownloads)
            } catch (e: Exception) {
                // If not, use user.home as fallback
                return userHome
            }
        }

        return windowsDownloads.toString()
    }
}
