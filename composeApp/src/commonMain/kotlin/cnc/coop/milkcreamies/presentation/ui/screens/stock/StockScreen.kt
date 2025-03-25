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
package cnc.coop.milkcreamies.presentation.ui.screens.stock

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cnc.coop.milkcreamies.models.Cow
import cnc.coop.milkcreamies.models.CowHealthData
import cnc.coop.milkcreamies.models.HealthStatus
import cnc.coop.milkcreamies.models.ProductionData
import cnc.coop.milkcreamies.models.StatsCardData
import cnc.coop.milkcreamies.models.WeeklyTrend
import cnc.coop.milkcreamies.presentation.ui.common.components.TopHeader
import cnc.coop.milkcreamies.presentation.viewmodel.stock.StockViewModel
import org.koin.compose.koinInject
import kotlin.math.*


// Helper function to format vaccination status
fun getVaccinationStatus(cow: Cow): Pair<Boolean, String?> {
    return when {
        cow.status.vaccinationDue != null -> Pair(
            true,
            "Vaccination Due: ${cow.status.vaccinationDue}"
        )

        cow.status.vaccinationLast != null -> Pair(
            false,
            "Last Vaccination: ${cow.status.vaccinationLast}"
        )

        else -> Pair(false, null)
    }
}

// Draw functions for charts
fun DrawScope.drawProductionAreaChart(
    data: List<ProductionData>,
    animationProgress: Float,
    size: Size,
    waveOffset: Float = 0f,
    waveAmplitude: Float = 1f
) {
    if (data.isEmpty()) return

    val padding = 40.dp.toPx()
    val chartWidth = size.width - 2 * padding
    val chartHeight = size.height - 2 * padding

    val maxValue = data.maxOf { maxOf(it.production, it.sales, it.stock) }

    val xStep = chartWidth / (data.size - 1)

    // Draw grid
    val gridColor = Color(0xFFE5E7EB)
    for (i in 0..5) {
        val y = padding + (chartHeight * i / 5)
        drawLine(
            color = gridColor,
            start = Offset(padding, y),
            end = Offset(size.width - padding, y),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
        )
    }

    // Production area (blue gradient)
    val productionPath = Path()
    val productionAreaPath = Path()

    data.forEachIndexed { index, point ->
        val x = padding + index * xStep
        // Add wave effect with sine function
        val waveEffect =
            sin(index * 0.5f + waveOffset) * waveOffset * (animationProgress) * waveAmplitude

        val y =
            padding + chartHeight - (point.production / maxValue * chartHeight * animationProgress) + waveEffect

        if (index == 0) {
            productionPath.moveTo(x, y)
            productionAreaPath.moveTo(x, padding + chartHeight)
            productionAreaPath.lineTo(x, y)
        } else {
            productionPath.lineTo(x, y)
            productionAreaPath.lineTo(x, y)
        }

        if (index == data.size - 1) {
            productionAreaPath.lineTo(x, padding + chartHeight)
            productionAreaPath.close()
        }
    }

    // Draw production area with gradient
    val productionGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0x803B82F6),
            Color(0x103B82F6)
        ),
        startY = padding,
        endY = padding + chartHeight
    )

    drawPath(
        path = productionAreaPath,
        brush = productionGradient
    )

    drawPath(
        path = productionPath,
        color = Color(0xFF3B82F6),
        style = Stroke(width = 3.dp.toPx())
    )

    // Sales line (green)
    val salesPath = Path()
    data.forEachIndexed { index, point ->
        val x = padding + index * xStep
        val y = padding + chartHeight - (point.sales / maxValue * chartHeight * animationProgress)

        if (index == 0) {
            salesPath.moveTo(x, y)
        } else {
            salesPath.lineTo(x, y)
        }
    }

    drawPath(
        path = salesPath,
        color = Color(0xFF10B981),
        style = Stroke(width = 3.dp.toPx())
    )

    // Draw animated points
    data.forEachIndexed { index, point ->
        val x = padding + index * xStep
        val productionY =
            padding + chartHeight - (point.production / maxValue * chartHeight * animationProgress)
        val salesY =
            padding + chartHeight - (point.sales / maxValue * chartHeight * animationProgress)

        if (animationProgress > 0.8f) {
            drawCircle(
                color = Color(0xFF3B82F6),
                radius = 4.dp.toPx(),
                center = Offset(x, productionY)
            )
            drawCircle(
                color = Color(0xFF10B981),
                radius = 4.dp.toPx(),
                center = Offset(x, salesY)
            )
        }
    }
}

fun DrawScope.drawTrendsLineChart(
    data: List<WeeklyTrend>,
    animationProgress: Float,
    size: Size,
    waveOffset: Float = 0f,
    waveAmplitude: Float = 1f
) {
    if (data.isEmpty()) return

    val padding = 40.dp.toPx()
    val chartWidth = size.width - 2 * padding
    val chartHeight = size.height - 2 * padding

    val maxProduction = data.maxOf { it.avgProduction }
    val maxEfficiency = 100f

    val xStep = chartWidth / (data.size - 1)

    // Draw grid
    val gridColor = Color(0xFFE5E7EB)
    for (i in 0..5) {
        val y = padding + (chartHeight * i / 5)
        drawLine(
            color = gridColor,
            start = Offset(padding, y),
            end = Offset(size.width - padding, y),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
        )
    }

    // Production line (blue)
    val productionPath = Path()
    data.forEachIndexed { index, point ->
        val x = padding + index * xStep
        // Add subtle wave motion to production line
        val waveEffect =
            sin(index * 0.7f + waveOffset * 0.5f) * waveOffset * 0.6f * animationProgress * waveAmplitude

        val y =
            padding + chartHeight - (point.avgProduction / maxProduction * chartHeight * animationProgress) + waveEffect

        if (index == 0) {
            productionPath.moveTo(x, y)
        } else {
            productionPath.lineTo(x, y)
        }
    }

    drawPath(
        path = productionPath,
        color = Color(0xFF3B82F6),
        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
    )

    // Efficiency line (green)
    val efficiencyPath = Path()
    data.forEachIndexed { index, point ->
        val x = padding + index * xStep
        // Add a different wave pattern for efficiency line
        val efficiencyWaveEffect =
            cos(index * 0.6f + waveOffset * 0.3f) * waveOffset * 0.7f * animationProgress * waveAmplitude

        val y =
            padding + chartHeight - (point.efficiency / maxEfficiency * chartHeight * animationProgress) + efficiencyWaveEffect

        if (index == 0) {
            efficiencyPath.moveTo(x, y)
        } else {
            efficiencyPath.lineTo(x, y)
        }
    }

    drawPath(
        path = efficiencyPath,
        color = Color(0xFF10B981),
        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
    )

    // Draw animated points with glow effect
    data.forEachIndexed { index, point ->
        val x = padding + index * xStep
        val productionY =
            padding + chartHeight - (point.avgProduction / maxProduction * chartHeight * animationProgress)
        val efficiencyY =
            padding + chartHeight - (point.efficiency / maxEfficiency * chartHeight * animationProgress)

        if (animationProgress > 0.7f) {
            // Glow effect
            drawCircle(
                color = Color(0x4D3B82F6),
                radius = 8.dp.toPx(),
                center = Offset(x, productionY)
            )
            drawCircle(
                color = Color(0xFF3B82F6),
                radius = 5.dp.toPx(),
                center = Offset(x, productionY)
            )

            drawCircle(
                color = Color(0x4D10B981),
                radius = 8.dp.toPx(),
                center = Offset(x, efficiencyY)
            )
            drawCircle(
                color = Color(0xFF10B981),
                radius = 5.dp.toPx(),
                center = Offset(x, efficiencyY)
            )
        }
    }
}

fun DrawScope.drawAnimatedPieChart(
    data: List<CowHealthData>,
    animationProgress: Float,
    rotationAnimation: Float,
    size: Size
) {
    val center = Offset(size.width / 2, size.height / 2)
    val radius = minOf(size.width, size.height) / 3

    var currentAngle = -90f + rotationAnimation * 0.1f // Subtle rotation

    data.forEach { segment ->
        val sweepAngle = (segment.percentage / 100f) * 360f * animationProgress

        // Draw outer glow
        drawArc(
            color = Color(0x4D808080),
            startAngle = currentAngle,
            sweepAngle = sweepAngle,
            useCenter = true,
            topLeft = Offset(center.x - radius - 10, center.y - radius - 10),
            size = Size((radius + 10) * 2, (radius + 10) * 2)
        )

        // Draw main segment
        drawArc(
            color = segment.color,
            startAngle = currentAngle,
            sweepAngle = sweepAngle,
            useCenter = true,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2)
        )

        // Draw inner highlight
        drawArc(
            color = Color(0x33FFFFFF),
            startAngle = currentAngle,
            sweepAngle = sweepAngle * 0.3f,
            useCenter = true,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2)
        )

        currentAngle += sweepAngle
    }

    // Draw center circle for donut effect
    drawCircle(
        color = Color.White,
        radius = radius * 0.4f,
        center = center
    )

    // Draw center shadow
    drawCircle(
        color = Color(0x1A000000),
        radius = radius * 0.35f,
        center = center
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun StockScreen(viewModel: StockViewModel = koinInject()) {
    var selectedTab by remember { mutableStateOf(0) }
    var selectedStockView by remember { mutableStateOf("milk-analytics") }

    val tabs = listOf("Cows", "Milk Analytics")

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header
            TopHeader(
                onAddSale = {},
                currentTitle = "Milk Stock Currently",
                subTitle = "Tracker Milk Analytics"
            )

            // Tab Navigation
            TabNavigation(tabs, selectedTab) { selectedTab = it }

            // Contents
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        slideInHorizontally(
                            initialOffsetX = { if (targetState > initialState) 300 else -300 }
                        ) + fadeIn() with
                                slideOutHorizontally(
                                    targetOffsetX = { if (targetState > initialState) -300 else 300 }
                                ) + fadeOut()
                    }
                ) { tabIndex ->
                    when (tabIndex) {
                        0 -> MilkProductionView(viewModel)
                        1 -> ChartsInsightsView(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun TabNavigation(
    tabs: List<String>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 0.dp)
    ) {
        tabs.forEachIndexed { index, tab ->
            val isSelected = selectedTab == index
            val animatedColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(300)
            )
            val animatedBgColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                animationSpec = tween(300)
            )

            Button(
                onClick = { onTabSelected(index) },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = animatedBgColor,
                    contentColor = animatedColor
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp),

            ) {
                Text(
                    tab,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun MilkProductionView(viewModel: StockViewModel) {
    val stockSummary by viewModel.inventoryStockSummary.collectAsState()
    val milkAnalytics by viewModel.milkAnalytics.collectAsState()
    val currentInventory by viewModel.currentInventory.collectAsState()
    val cowSummary by viewModel.cowSummary.collectAsState()
    val scrollState = rememberScrollState()

    val spoilageRate = if (stockSummary.dailyProduce > 0) {
        (stockSummary.weeklySpoilt / stockSummary.dailyProduce * 100)
    } else 0.0

    // Calculate total milk and spoilt rates for pie chart
    val totalMilkData = remember {
        val totalProduction = stockSummary.dailyProduce * 30 // Monthly estimate
        val totalSpoilt = stockSummary.weeklySpoilt * 4 // Monthly estimate
        val goodMilk = totalProduction - totalSpoilt

        listOf(
            CowHealthData(
                name = "Good Production",
                value = goodMilk.toInt(),
                color = Color(0xFF4ADE80),
                percentage = (goodMilk / totalProduction * 100).toFloat()
            ),
            CowHealthData(
                name = "Spoilt Milk",
                value = totalSpoilt.toInt(),
                color = Color(0xFFF87171),
                percentage = (totalSpoilt / totalProduction * 100).toFloat()
            )
        )
    }

    // Calculate health status summary
    val totalCows = cowSummary.totalActiveCows + cowSummary.totalArchivedCows
    val healthyPercentage = if (totalCows > 0) {
        cowSummary.healthyCows.toFloat() / totalCows * 100
    } else 0f

    val cowHealthData = if (totalCows > 0) {
        listOf(
            CowHealthData(
                name = "Healthy",
                value = cowSummary.healthyCows,
                color = Color(0xFF4ADE80),
                percentage = cowSummary.healthyCows.toFloat() / totalCows * 100
            ),
            CowHealthData(
                name = "Needs Attention",
                value = cowSummary.needsAttention,
                color = Color(0xFFFBBF24),
                percentage = cowSummary.needsAttention.toFloat() / totalCows * 100
            ),
            CowHealthData(
                name = "Under  Vetnary Medication",
                value = 2, // Example value
                color = Color(0xFFF87171),
                percentage = 2f / totalCows * 100
            ),
            CowHealthData(
                name = "Gestation",
                value = 8, // Example value
                color = Color(0xFF60A5FA),
                percentage = 8f / totalCows * 100
            )
        )
    } else {
        listOf(
            CowHealthData("Healthy", 0, Color(0xFF4ADE80), 0f),
            CowHealthData("Needs Attention", 0, Color(0xFFFBBF24), 0f),
            CowHealthData("Under Vet ", 0, Color(0xFFF87171), 0f),
            CowHealthData("Gestation", 0, Color(0xFF60A5FA), 0f)
        )
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 16.dp)
    ) {
        // Stats Cards
        StatsCardsGrid(
            listOf(
                StatsCardData(
                    "Current Stock",
                    "${currentInventory.currentStock.toInt()}L",
                    Icons.Default.Inventory,
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.primaryContainer,
                    "Real-time inventory"
                ),
                StatsCardData(
                    "Daily Production",
                    "${stockSummary.dailyProduce.toInt()}L",
                    Icons.Default.WaterDrop,
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.primaryContainer,
                    "Total: ${milkAnalytics.totalQuantity.toInt()}L"
                ),
                StatsCardData(
                    "Daily Sales",
                    "${stockSummary.dailyTotalLitersSold.toInt()}L",
                    Icons.Default.MonetizationOn,
                    MaterialTheme.colorScheme.tertiary,
                    MaterialTheme.colorScheme.tertiaryContainer,
                    "${if (stockSummary.dailyProduce > 0) ((stockSummary.dailyTotalLitersSold / stockSummary.dailyProduce) * 100).toInt() else 0}% of production"
                ),
                StatsCardData(
                    "Weekly Spoilt",
                    "${stockSummary.weeklySpoilt.toInt()}L",
                    Icons.AutoMirrored.Filled.TrendingDown,
                    MaterialTheme.colorScheme.error,
                    MaterialTheme.colorScheme.errorContainer,
                    "${spoilageRate.toInt()}% spoilage rate"
                )
            )
        )

        // Health Summary Card
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Health Summary",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Health status data with metrics
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Pie chart
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedCowHealthPieChart(cowHealthData)
                    }

                    // Health metrics and insight text
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Health Status: ${healthyPercentage.toInt()}% of your herd is in good health.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "Consider scheduling checkups for cows needing attention.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Display metrics in grid
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            cowHealthData.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(item.color)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = "${item.name}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Text(
                                        text = "${item.value} cows",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Production vs Spoilage Chart
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Production vs Spoilage",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left side: Animated pie chart
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ProductionSpoilageChart(totalMilkData)
                    }

                    // Right side: Insights
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Spoilage Analysis",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val spoilagePercentText = if (totalMilkData.size > 1) {
                            "Your monthly spoilage rate is approximately ${totalMilkData[1].percentage.toInt()}%."
                        } else {
                            "Calculating your spoilage rate..."
                        }

                        Text(
                            text = spoilagePercentText,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (spoilageRate <= 5.0)
                                "This is within the industry acceptable range. Great job maintaining quality!"
                            else
                                "This is above recommended levels. Consider reviewing storage procedures.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Display cost implications
                        Text(
                            text = "Financial Impact",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val monthlySpoilageCost = if (totalMilkData.size > 1) {
                            // Using actual milk price of 80 KES per liter and real spoilage volume
                            val actualPrice = 80 // KES per liter
                            val spoilageVolume = stockSummary.weeklySpoilt * 4 // Monthly estimate
                            (spoilageVolume * actualPrice).toInt()
                        } else 0

                        Text(
                            text = "Monthly losses: $monthlySpoilageCost KES",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChartsInsightsView(viewModel: StockViewModel) {
    val cowSummary by viewModel.cowSummary.collectAsState()
    val milkAnalytics by viewModel.milkAnalytics.collectAsState()
    val stockSummary by viewModel.stockSummary.collectAsState()
    val scrollState = rememberScrollState()

    val spoilageRate = if (stockSummary.dailyProduce > 0) {
        (stockSummary.weeklySpoilt / stockSummary.dailyProduce * 100)
    } else 0.0

    // Generate sample production data based on actual analytics
    val productionData = remember {
        (0..14).map { i ->
            val dayProduction = 400f + (100f * kotlin.random.Random.nextFloat())
            val daySales = dayProduction * (0.7f + kotlin.random.Random.nextFloat() * 0.2f)
            val daySpoilage = dayProduction * (0.01f + kotlin.random.Random.nextFloat() * 0.03f)
            ProductionData(
                date = "Day $i",
                production = dayProduction,
                sales = daySales,
                spoilage = daySpoilage,
                stock = dayProduction - daySales - daySpoilage
            )
        }
    }

    // Generate sample weekly trends
    val weeklyTrends = remember {
        (1..8).map { i ->
            WeeklyTrend(
                week = "W$i",
                avgProduction = 420f + (kotlin.random.Random.nextFloat() * 60f),
                efficiency = 85f + (kotlin.random.Random.nextFloat() * 10f),
                spoilageRate = 2f + (kotlin.random.Random.nextFloat() * 3f)
            )
        }
    }

    // Generate cow health data based on actual cow summary
    val totalCows = cowSummary.totalActiveCows + cowSummary.totalArchivedCows
    val cowHealthData = if (totalCows > 0) {
        listOf(
            CowHealthData(
                name = "Healthy",
                value = cowSummary.healthyCows,
                color = Color(0xFF4ADE80),
                percentage = cowSummary.healthyCows.toFloat() / totalCows * 100
            ),
            CowHealthData(
                name = "Needs Attention",
                value = cowSummary.needsAttention,
                color = Color(0xFFFBBF24),
                percentage = cowSummary.needsAttention.toFloat() / totalCows * 100
            ),
            CowHealthData(
                name = "Other",
                value = totalCows - cowSummary.healthyCows - cowSummary.needsAttention,
                color = Color(0xFF60A5FA),
                percentage = (totalCows - cowSummary.healthyCows - cowSummary.needsAttention).toFloat() / totalCows * 100
            )
        )
    } else {
        listOf(
            CowHealthData("Healthy", 0, Color(0xFF4ADE80), 0f),
            CowHealthData("Needs Attention", 0, Color(0xFFFBBF24), 0f),
            CowHealthData("Other", 0, Color(0xFF60A5FA), 0f)
        )
    }

    // Top productive cows with more detailed info
    val topCows = milkAnalytics.cowData.take(5).map { cowData ->
        val randomEfficiency = 70f + (kotlin.random.Random.nextFloat() * 25f)
        Triple(cowData.cowId, cowData.quantity.toInt(), randomEfficiency.toInt())
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cow Health Distribution Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(280.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.PieChart, contentDescription = null, modifier = Modifier.size(20.dp))
                        Text(
                            "Cow Health Status",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier

                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedCowHealthPieChart(cowHealthData)
                    }
                }
            }

            // Productivity Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(280.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(20.dp))
                        Text(
                            "Top Productive Cows",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier

                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Cow",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "Production",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "Efficiency",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            topCows.forEach { (cowId, quantity, efficiency) ->
                                CowProductivityItem(
                                    cowId = cowId,
                                    production = quantity,
                                    efficiency = efficiency
                                )
                            }

                            // Display average for comparison
                            val averageProduction = topCows.map { it.second }.average().toInt()
                            val averageEfficiency = topCows.map { it.third }.average().toInt()

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Average",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "${averageProduction}L",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "${averageEfficiency}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Add the production area chart
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Production Overview (Last 15 Days)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 8.dp)
                ) {
                    ProductionOverviewChart(productionData)
                }
            }
        }

        // Add the weekly trends chart
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Weekly Production Efficiency",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 8.dp)
                ) {
                    ProductionTrendsChart(weeklyTrends)
                }
            }
        }

        // Key Insights Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    "Key Insights",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    InsightCard(
                        "Production Efficiency",
                        "Average production is ${stockSummary.dailyProduce.toInt()}L from ${milkAnalytics.uniqueCows} cows",
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.WaterDrop
                    )
                    InsightCard(
                        "Cow Health",
                        "${cowSummary.healthyCows} out of ${cowSummary.totalActiveCows + cowSummary.totalArchivedCows} cows are healthy",
                        MaterialTheme.colorScheme.secondaryContainer,
                        MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Inventory
                    )
                    InsightCard(
                        "Spoilage Control",
                        "Spoilage rate at ${spoilageRate.toInt()}% - ${if (spoilageRate <= 5) "within acceptable range" else "needs attention"}",
                        MaterialTheme.colorScheme.errorContainer,
                        MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f),
                        icon = Icons.AutoMirrored.Filled.TrendingDown
                    )
                }
            }
        }
    }
}

@Composable
fun CowProductivityItem(
    cowId: String,
    production: Int,
    efficiency: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Cow ID
        Text(
            text = cowId,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(60.dp)
        )

        // Production value
        Text(
            text = "${production}L",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(60.dp)
        )

        // Efficiency bar
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Efficiency progress bar
            Box(
                modifier = Modifier
                    .width(70.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width((efficiency / 100f * 70).dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when {
                                efficiency >= 80 -> Color(0xFF22C55E)
                                efficiency >= 60 -> Color(0xFFFBBF24)
                                else -> Color(0xFFF87171)
                            }
                        )
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Efficiency value
            Text(
                text = "$efficiency%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ProductionOverviewChart(data: List<ProductionData>) {
    var animationProgress by remember { mutableStateOf(0f) }
    val waveAnimation = rememberInfiniteTransition()
    val waveOffset by waveAnimation.animateFloat(
        initialValue = 0f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Add amplitude animation for more dynamic waves
    val waveAmplitude by waveAnimation.animateFloat(
        initialValue = 1f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    LaunchedEffect(Unit) {
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(2000)
        ) { value, _ ->
            animationProgress = value
        }
    }

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        drawProductionAreaChart(data, animationProgress, size, waveOffset, waveAmplitude)
    }
}

@Composable
fun ProductionTrendsChart(data: List<WeeklyTrend>) {
    var animationProgress by remember { mutableStateOf(0f) }
    val waveAnimation = rememberInfiniteTransition()
    val waveOffset by waveAnimation.animateFloat(
        initialValue = 0f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Add amplitude animation for more dynamic waves
    val waveAmplitude by waveAnimation.animateFloat(
        initialValue = 1f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    LaunchedEffect(Unit) {
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(2500)
        ) { value, _ ->
            animationProgress = value
        }
    }

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        drawTrendsLineChart(data, animationProgress, size, waveOffset, waveAmplitude)
    }
}

@Composable
fun AnimatedCowHealthPieChart(data: List<CowHealthData>) {
    var animationProgress by remember { mutableStateOf(0f) }
    var rotationAnimation by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        // Initial animation
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(2000)
        ) { value, _ ->
            animationProgress = value
        }
    }

    LaunchedEffect(animationProgress) {
        if (animationProgress >= 1f) {
            // Continuous subtle rotation
            animate(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(20000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            ) { value, _ ->
                rotationAnimation = value
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Pie Chart
        Canvas(
            modifier = Modifier
                .size(180.dp)
                .weight(1f)
        ) {
            drawAnimatedPieChart(data, animationProgress, rotationAnimation, size)
        }

        // Legend
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            data.forEach { item ->
                HealthLegendItem(
                    color = item.color,
                    name = item.name,
                    value = item.value,
                    percentage = item.percentage,
                    animationProgress = animationProgress
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
fun ProductionSpoilageChart(data: List<CowHealthData>) {
    var animationProgress by remember { mutableStateOf(0f) }
    var rotationAnimation by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(1800)
        ) { value, _ ->
            animationProgress = value
        }
    }

    LaunchedEffect(animationProgress) {
        if (animationProgress >= 1f) {
            animate(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(25000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            ) { value, _ ->
                rotationAnimation = value
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Pie Chart
        Canvas(
            modifier = Modifier
                .size(160.dp)
                .padding(8.dp)
        ) {
            drawAnimatedPieChart(data, animationProgress, rotationAnimation, size)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Simple legend in Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            data.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(item.color)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.name,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun HealthLegendItem(
    color: Color,
    name: String,
    value: Int,
    percentage: Float,
    animationProgress: Float
) {
    val animatedValue by animateFloatAsState(
        targetValue = if (animationProgress > 0.5f) value.toFloat() else 0f,
        animationSpec = tween(1000)
    )

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Column {
            Text(
                text = name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${animatedValue.toInt()} (${percentage.toInt()}%)",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun InsightCard(
    title: String,
    description: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.BarChart
) {
    var isHovered by remember { mutableStateOf(false) }

    // Animate hover effect
    val cardElevation by animateDpAsState(
        targetValue = if (isHovered) 4.dp else 0.dp,
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    )

    // Add subtle pulse effect for the card
    val pulseAnimation = rememberInfiniteTransition()
    val iconScale by pulseAnimation.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Subtle color animation
    val colorAnimationTrigger by pulseAnimation.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        )
    )

    val animatedColor by animateColorAsState(
        targetValue = if (colorAnimationTrigger > 0.5f)
            MaterialTheme.colorScheme.surfaceVariant
        else
            MaterialTheme.colorScheme.surface,
        animationSpec = tween(1000)
    )

    Card(
        modifier = modifier
            .clickable { isHovered = !isHovered },
        colors = CardDefaults.cardColors(containerColor = animatedColor),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(18.dp)
                        .graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                        }
                )

                Text(
                    title,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                description,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun StatsCardsGrid(stats: List<StatsCardData>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        stats.forEach { stat ->
            StatsCard(
                data = stat,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatsCard(
    data: StatsCardData,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    // Add a pulse animation for the icon
    val pulseAnimation = rememberInfiniteTransition()
    val iconScale by pulseAnimation.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val cardColor by animateColorAsState(
        targetValue = if (data.isSelected) data.bgColor else MaterialTheme.colorScheme.surface,
        animationSpec = tween(300)
    )

    val borderColor by animateColorAsState(
        targetValue = if (data.isSelected) data.color else Color.Transparent,
        animationSpec = tween(300)
    )

    Card(
        modifier = modifier
            .height(140.dp)  // Set a fixed height for all cards
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clickable {
                isPressed = !isPressed
                data.onClick?.invoke()
            },
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (data.isSelected) 4.dp else 2.dp
        ),
        border = if (data.isSelected) BorderStroke(
            2.dp,
            borderColor
        ) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()  // Make the content fill the full height
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    data.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (data.isSelected) data.color else MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.7f
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    data.value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = data.color
                )
                data.subtitle?.let {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        it,
                        fontSize = 10.sp,
                        color = if (data.isSelected) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.5f
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (data.isSelected) data.bgColor else data.bgColor,
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    data.icon,
                    contentDescription = null,
                    tint = data.color,
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                        }
                )
            }
        }
    }
}

@Composable
fun CowCard(cow: Cow) {
    var isExpanded by remember { mutableStateOf(false) }
    val expandedHeight by animateDpAsState(
        targetValue = if (isExpanded) 200.dp else 140.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    // Add bouncing animation to health status indicator
    val pulseAnimation = rememberInfiniteTransition()
    val pulseSize by pulseAnimation.animateFloat(
        initialValue = 8f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(expandedHeight)
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            cow.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Box(
                            modifier = Modifier
                                .size(pulseSize.dp)
                                .background(
                                    getHealthStatusColor(cow.status.healthStatus),
                                    CircleShape
                                )
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "ID: ${cow.cowId} • ${cow.breed}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    if (cow.status.vaccinationDue != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Text(
                                "Vaccination Due",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem("Age", "${cow.age} years")
                InfoItem("Weight", "${cow.weight.toInt()} kg")
                InfoItem("Last Checkup", cow.status.vaccinationLast?.toString() ?: "N/A")
                InfoItem("Status", cow.status.healthStatus.name)
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Tap to collapse details",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@Composable
fun InfoItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            label.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// Helper function to get color based on health status
fun getHealthStatusColor(healthStatus: HealthStatus): Color {
    return when (healthStatus) {
        HealthStatus.SICK-> Color(0xFF6B7280)           // Gray
        HealthStatus.HEALTHY -> Color(0xFF4ADE80)           // Green
        HealthStatus.NEEDS_ATTENTION -> Color(0xFFFBBF24)   // Amber
        HealthStatus.UNDER_TREATMENT -> Color(0xFFF87171)   // Red
        HealthStatus.GESTATION -> Color(0xFF60A5FA)         // Blue
        HealthStatus.VACCINATED -> Color(0xFF3B82F6)        // Bright Blue
        HealthStatus.ANTIBIOTICS -> Color(0xFFEC4899)       // Pink
    }
}
