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
package cnc.coop.milkcreamies

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.url.URL

private val fileSavingFeatures = js("typeof window !== 'undefined' && 'Blob' in window && 'URL' in window && 'createObjectURL' in URL && 'download' in document.createElement('a')") as Boolean

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"

    fun supportsFileSaving(): Boolean = fileSavingFeatures

    /**
     * Detects the browser environment
     *
     * @return The browser name and version if detectable
     */
    fun detectBrowser(): String {
        val userAgent = window.navigator.userAgent
        return when {
            userAgent.contains("Chrome") && !userAgent.contains("Edg") -> "Chrome"
            userAgent.contains("Firefox") -> "Firefox"
            userAgent.contains("Safari") && !userAgent.contains("Chrome") -> "Safari"
            userAgent.contains("Edg") -> "Edge"
            userAgent.contains("MSIE") || userAgent.contains("Trident") -> "Internet Explorer"
            else -> "Unknown Browser"
        }
    }

    /**
     * Gets device info including platform and screen size
     */
    fun getDeviceInfo(): String {
        return try {
            val platform = window.navigator.platform
            val width = window.screen.width
            val height = window.screen.height
            "Platform: $platform, Screen: ${width}x${height}"
        } catch (e: Throwable) {
            "Unknown device"
        }
    }
}

actual fun getPlatform(): Platform = WasmPlatform()
