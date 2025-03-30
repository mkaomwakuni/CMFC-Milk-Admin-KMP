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

/**
 * Android implementation of PlatformFileUtil
 * This is a mock implementation that just logs the file operations
 * In a real app, you would use ContentResolver to create files
 */
actual object PlatformFileUtil {
    /**
     * Save content to a file using Android APIs
     * This is a mock implementation that just logs the file operations
     */
    actual suspend fun saveToDevice(
        content: String,
        filename: String,
        mimeType: String
    ): Result<String> {
        // In a real app, we would use ContentResolver to create a file
        return Result.success("File would be saved to Downloads/$filename")
    }

    /**
     * Android supports direct file saving through ContentResolver
     */
    actual fun supportsDirectFileSave(): Boolean {
        return false // Using false to trigger fallback mechanism
    }

    /**
     * Get the default download directory path on Android
     */
    actual fun getDefaultDownloadPath(): String? {
        // In a real app, we would return Environment.DIRECTORY_DOWNLOADS
        return "Download"
    }
}
