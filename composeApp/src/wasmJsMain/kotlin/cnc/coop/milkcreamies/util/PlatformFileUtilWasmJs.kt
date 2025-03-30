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

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob

@JsName("Object")
external class JsUtils {
    val browserSupportsFileDownload: Boolean
    fun createBlob(content: String, type: String): Blob
    fun logError(msg: String)
}

private val jsUtils: JsUtils = js(
    """
{
    browserSupportsFileDownload: typeof window !== 'undefined' && 'Blob' in window && 'URL' in window && 'createObjectURL' in window.URL && 'download' in document.createElement('a'),
    createBlob: function(content, type) { return new Blob([content], {type: type}); },
    logError: function(msg) { console.error(msg); }
}
""".trimIndent()
) as JsUtils

/**
 * Implementation of PlatformFileUtil for WASM/JS target
 * Uses browser DOM APIs to download files
 */
actual object PlatformFileUtil {
    /**
     * Saves content to a file by triggering a browser download
     * Uses the Blob and URL APIs to create a downloadable resource
     */
    actual suspend fun saveToDevice(
        content: String,
        filename: String,
        mimeType: String
    ): Result<String> {
        return try {
            if (!jsUtils.browserSupportsFileDownload) {
                return Result.failure(RuntimeException("This browser doesn't support file downloads"))
            }

            // Create blob and URL
            val blob = jsUtils.createBlob(content, mimeType)
            val url = URL.createObjectURL(blob)

            // Create and configure download link
            val downloadLink = (document.createElement("a") as HTMLAnchorElement).apply {
                href = url
                setAttribute("download", filename)
                style.display = "none"
            }

            // Add link to document, click it to trigger download, then clean up
            document.body?.appendChild(downloadLink)
            downloadLink.click()
            document.body?.removeChild(downloadLink)
            URL.revokeObjectURL(url)

            Result.success("File saved as $filename")
        } catch (e: Throwable) {
            jsUtils.logError("Error saving file: ${e.message}")
            Result.failure(RuntimeException("Failed to save file: ${e.message}"))
        }
    }

    /**
     * Check if the current browser supports file downloads
     */
    actual fun supportsDirectFileSave(): Boolean = jsUtils.browserSupportsFileDownload

    /**
     * Web browsers don't have standard download directories
     */
    actual fun getDefaultDownloadPath(): String? {
        // No direct access to file system in browser
        return null
    }
}
