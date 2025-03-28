package cnc.coop.milkcreamies.models

import androidx.compose.ui.graphics.Color

// Data classes for charts
data class ProductionData(
    val date: String,
    val production: Float,
    val sales: Float,
    val spoilage: Float,
    val stock: Float
)

data class WeeklyTrend(
    val week: String,
    val avgProduction: Float,
    val efficiency: Float,
    val spoilageRate: Float
)

data class CowHealthData(
    val name: String,
    val value: Int,
    val color: Color,
    val percentage: Float
)