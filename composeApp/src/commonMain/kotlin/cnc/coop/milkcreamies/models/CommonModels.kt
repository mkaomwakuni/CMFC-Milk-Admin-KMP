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
package cnc.coop.milkcreamies.models

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.datetime.LocalDate

// Common model for overview cards used in dashboard and other screens
data class OverviewCardData(
    val title: String,
    val value: String,
    val icon: ImageVector,
    val color: Color
)

// Common model for stats cards
data class StatsCardData(
    val title: String,
    val value: String,
    val icon: ImageVector,
    val color: Color,
    val bgColor: Color,
    val subtitle: String? = null,
    val isSelected: Boolean = false,
    val onClick: (() -> Unit)? = null
)

/**
 * Data class for daily stock information
 */
data class DailyStockData(
    val date: LocalDate,
    val milkIn: Double,
    val milkOut: Double,
    val remainingStock: Double
)

/**
 * Data class for monthly stock summary
 */
data class MonthlyStockSummary(
    val totalProduced: Double = 0.0,
    val totalSold: Double = 0.0,
    val totalSpoilt: Double = 0.0,
    val averageDailyProduction: Double = 0.0,
    val averageDailySales: Double = 0.0
)

// Data classes
data class SettingsGroup(
    val title: String,
    val icon: ImageVector,
    val settings: List<SettingItem>
)

data class SettingItem(
    val title: String,
    val description: String? = null,
    val icon: ImageVector,
    val type: SettingType,
    val value: Any? = null,
    val onValueChange: ((Any) -> Unit)? = null,
    val options: List<String>? = null
)

enum class SettingType {
    TOGGLE,
    SELECT,
    NAVIGATION
}
