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
package cnc.coop.milkcreamies.presentation.ui.common.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

// Color Palette
object AppColors {
    val Primary = Color(0xFF2E7D32)
    val PrimaryVariant = Color(0xFF1B5E20)
    val Secondary = Color(0xFF4CAF50)
    val SecondaryVariant = Color(0xFF388E3C)
    val Background = Color(0xFFF1F8E9)
    val Surface = Color(0xFFFFFFFF)
    val Error = Color(0xFFD32F2F)
    val OnPrimary = Color(0xFFFFFFFF)
    val OnSecondary = Color(0xFFFFFFFF)
    val OnBackground = Color(0xFF1B5E20)
    val OnSurface = Color(0xFF1B5E20)
    val OnError = Color(0xFFFFFFFF)

    // Dark theme colors
    val DarkBackground = Color(0xFF121212)
    val DarkSurface = Color(0xFF1E1E1E)
    val DarkOnBackground = Color(0xFFE8F5E8)
    val DarkOnSurface = Color(0xFFE8F5E8)
}

private val LightColorScheme = lightColorScheme(
    primary = AppColors.Primary,
    secondary = AppColors.Secondary,
    background = AppColors.Background,
    surface = AppColors.Surface,
    error = AppColors.Error,
    onPrimary = AppColors.OnPrimary,
    onSecondary = AppColors.OnSecondary,
    onBackground = AppColors.OnBackground,
    onSurface = AppColors.OnSurface,
    onError = AppColors.OnError
)

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.Secondary,
    secondary = AppColors.Primary,
    background = AppColors.DarkBackground,
    surface = AppColors.DarkSurface,
    error = AppColors.Error,
    onPrimary = Color(0xFF000000),
    onSecondary = Color(0xFF000000),
    onBackground = AppColors.DarkOnBackground,
    onSurface = AppColors.DarkOnSurface,
    onError = AppColors.OnError
)

// Theme Manager
class ThemeManager {
    private var _isDarkMode by mutableStateOf(false)

    val isDarkMode: Boolean get() = _isDarkMode

    fun toggleTheme() {
        _isDarkMode = !_isDarkMode
    }

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode = enabled
    }
}

val LocalThemeManager = compositionLocalOf { ThemeManager() }

@Composable
fun MilkCooperativeTheme(
    themeManager: ThemeManager = ThemeManager(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (themeManager.isDarkMode) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalThemeManager provides themeManager) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
