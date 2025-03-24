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
package cnc.coop.milkcreamies.presentation.ui.screens.milk


import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdfScanner
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.automirrored.filled.Input
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Input

// Removed duplicate import
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cnc.coop.milkcreamies.models.Cow
import cnc.coop.milkcreamies.models.CowProduction
import cnc.coop.milkcreamies.models.CowHealthDetailsResponse
import cnc.coop.milkcreamies.models.DailyProduction
import cnc.coop.milkcreamies.models.HealthStatus
import cnc.coop.milkcreamies.models.Member
import cnc.coop.milkcreamies.models.MilkInEntry
import cnc.coop.milkcreamies.models.MilkingType
import cnc.coop.milkcreamies.models.MilkSpoiltEntry
import cnc.coop.milkcreamies.presentation.ui.common.components.ErrorDialog
import cnc.coop.milkcreamies.presentation.ui.common.components.ExportFormatDialog
import cnc.coop.milkcreamies.presentation.ui.common.components.LoadingDialog
import cnc.coop.milkcreamies.presentation.ui.common.components.SummaryCard
import cnc.coop.milkcreamies.presentation.ui.common.components.TopHeader
import cnc.coop.milkcreamies.presentation.ui.common.components.ChartWithLabels
import cnc.coop.milkcreamies.presentation.ui.screens.milkout.AddSpoiltMilkDialog
import cnc.coop.milkcreamies.presentation.viewmodel.milk.MilkInViewModel
import cnc.coop.milkcreamies.presentation.viewmodel.milk.MilkSpoiltViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

// Tab items for milk management
enum class MilkTab {
    IN, SPOILT
}

// Serializable data class for spoilt milk records
@Serializable
data class MilkSpoilageRequest(
    val amountSpoilt: Double,
    val date: LocalDate,
    val lossAmount: Double,
    val cause: String? = null
)

@Composable
fun MilkInScreen(
    viewModel: MilkInViewModel = koinInject()
) {
    var selectedTab by remember { mutableStateOf(MilkTab.IN) }
    var showAddMilkInDialog by remember { mutableStateOf(false) }
    var showAddSpoiltDialog by remember { mutableStateOf(false) }
    
    // Export dialog state
    var showExportDialog by remember { mutableStateOf(false) }
    
    // Error and loading state
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopHeader(
            onAddSale = {
                when (selectedTab) {
                    MilkTab.IN -> showAddMilkInDialog = true
                    MilkTab.SPOILT -> showAddSpoiltDialog = true
                }
            },
            currentTitle = "Milk Management",
            subTitle = if (selectedTab == MilkTab.IN)
                "Track and analyze milk production data"
            else "Manage spoilage records and track losses",
            onExport = { showExportDialog = true }
        )

        // Show export format dialog when requested
        if (showExportDialog) {
            ExportFormatDialog(
                onDismissRequest = { showExportDialog = false },
                onFormatSelected = { format ->
                    viewModel.exportMilkData(format)
                    showExportDialog = false
                }
            )
        }

        // Show error dialog if there's an error message
        errorMessage?.let {
            ErrorDialog(
                message = it,
                onDismiss = { viewModel.clearErrorMessage() }
            )
        }

        // Show loading dialog when needed
        if (isLoading) {
            LoadingDialog(message = "Processing...")
        }

        // Tab row to switch between Milk In and Milk Spoilt views
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
        ) {
            Tab(
                selected = selectedTab == MilkTab.IN,
                onClick = {
                    selectedTab = MilkTab.IN
                    viewModel.updateTab(MilkTab.IN)
                },
                text = { Text("Milk In") },
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Input,
                        contentDescription = "Milk In"
                    )
                }
            )
            Tab(
                selected = selectedTab == MilkTab.SPOILT,
                onClick = {
                    selectedTab = MilkTab.SPOILT
                    viewModel.updateTab(MilkTab.SPOILT)
                },
                text = { Text("Spoilt Milk") },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "Spoilt Milk"
                    )
                }
            )
        }

        // Display the selected tab content
        when (selectedTab) {
            MilkTab.IN -> MilkInContent(
                viewModel,
                showInternalDialog = false
            ) // Pass false to prevent showing internal dialog
            MilkTab.SPOILT -> MilkSpoiltContent()
        }
    }

    // Add Milk In Dialog
    if (showAddMilkInDialog) {
        AddMilkEntryDialog(
            onDismiss = { showAddMilkInDialog = false },
            onSave = { milkEntry ->
                viewModel.addMilkInEntry(
                    cowId = milkEntry.cowId,
                    ownerId = milkEntry.ownerId,
                    quantityLiters = milkEntry.liters,
                    date = milkEntry.date,
                    milkingType = milkEntry.milkingType
                )
                showAddMilkInDialog = false
            },
            viewModel = viewModel
        )
    }

    // Add Spoilt Milk Dialog
    if (showAddSpoiltDialog) {
        val spoiltViewModel: MilkSpoiltViewModel = koinInject()
        val stockViewModel: cnc.coop.milkcreamies.presentation.viewmodel.stock.StockViewModel = koinInject()
        val currentStock by stockViewModel.stockSummary.collectAsState()
        
        AddSpoiltMilkDialog(
            onDismiss = { showAddSpoiltDialog = false },
            onAddSpoiltEntry = { entry ->
                spoiltViewModel.addSpoiltEntry(
                    date = entry.date,
                    amountSpoilt = entry.amountSpoilt,
                    lossAmount = entry.lossAmount,
                    cause = entry.cause
                )
                showAddSpoiltDialog = false
            },
            currentStock = currentStock.currentStock
        )
    }
}



/**
 * A wrapper component for DailyProductionChart that adds axis labels
 */
@Composable
fun DailyProductionChartWithLabels(
    data: List<DailyProduction>,
    modifier: Modifier = Modifier
) {
    ChartWithLabels(
        data = data,
        modifier = modifier,
        content = {
            DailyProductionChart(
                data = data,
                modifier = Modifier.fillMaxSize()
            )
        }
    )
}

@Composable
fun MilkInContent(
    viewModel: MilkInViewModel = koinInject(),
    showInternalDialog: Boolean = true
) {
    var showAddDialog by remember { mutableStateOf(false) }
    val milkAnalytics by viewModel.milkAnalytics.collectAsState()
    val milkInEntries by viewModel.milkInEntries.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Real-time inventory data
    val currentInventory by viewModel.currentInventory.collectAsState()
    val stockSummary by viewModel.inventoryStockSummary.collectAsState()

    // Call loadAllData and check if we need to generate mock data
    LaunchedEffect(Unit) {
        viewModel.loadAllData()

        // If no entries are loaded, we can add some mock data
        if (milkInEntries.isEmpty()) {
            viewModel.loadAllData() // Reload data
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                errorMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Analytics Cards Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCard(
                        title = "Current Stock",
                        value = "${currentInventory.currentStock.toInt()} L",
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Inventory,
                        iconColor = Color(0xFF7C3AED),
                        backgroundColor = Color(0xFFEDE9FE),
                        subtitle = "Milk Available in Litres"
                    )
                    SummaryCard(
                        title = "Daily Production",
                        value = "${stockSummary.dailyProduce.toInt()} L",
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.AdfScanner,
                        subtitle = "Today Entry",
                        iconColor = Color(0xFFEA580C),
                        backgroundColor = Color(0xFFFED7AA),
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCard(
                        title = "Total Quantity",
                        value = "${milkAnalytics.totalQuantity.toInt()} L",
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.WaterDrop,
                        subtitle = "Overall Production This Month",
                        iconColor = Color(0xFF6D28D9),
                        backgroundColor = Color(0xFFE9D5FF)

                    )
                    SummaryCard(
                        title = "Active Cows",
                        value = "${milkAnalytics.uniqueCows}",
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Pets,
                        subtitle = "Currently Active",
                        iconColor = Color(0xFF1E40AF),
                        backgroundColor = Color(0xFFDBEAFE)
                    )
                }
            }

            // Daily Production Trend Chart
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Daily Production Trend",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        DailyProductionChartWithLabels(
                            data = milkAnalytics.dailyData,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Production by Cow Chart
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Production by Cow",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        CowProductionChart(
                            data = milkAnalytics.cowData,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Milk Entries List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Recent Milk Entries",
                        fontSize = 14.sp,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Milk Entries List
            items(milkInEntries.size) { index ->
                val entry = milkInEntries[index]
                MilkEntryCard(
                    entry = entry
                )
            }
        }
    }

    // Add Milk Entry Dialog
    if (showAddDialog) {
        AddMilkEntryDialog(
            onDismiss = { showAddDialog = false },
            onSave = { milkEntry ->
                viewModel.addMilkInEntry(
                    cowId = milkEntry.cowId,
                    ownerId = milkEntry.ownerId,
                    quantityLiters = milkEntry.liters,
                    date = milkEntry.date,
                    milkingType = milkEntry.milkingType
                )
                showAddDialog = false
            },
            viewModel = viewModel
        )
    }
}

//@Composable
//fun SummaryCard(
//    title: String,
//    value: String,
//    color: Color,
//    modifier: Modifier = Modifier,
//    icon: ImageVector
//) {
//
//
//    Card(
//        modifier = modifier
//            .height(120.dp)
//            .clickable { isHovered = !isHovered },
//        colors = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colorScheme.surface
//        ),
//        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
//        shape = RoundedCornerShape(12.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(16.dp),
//            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            // Icon with animation
//            Icon(
//                imageVector = icon,
//                contentDescription = title,
//                tint = color,
//                modifier = Modifier
//                    .size(28.dp)
//                    .graphicsLayer {
//                        scaleX = iconScale
//                        scaleY = iconScale
//                    }
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            // Title
//            Text(
//                text = title,
//                style = MaterialTheme.typography.bodyMedium,
//                color = MaterialTheme.colorScheme.onSurfaceVariant
//            )
//
//            // Value
//            Text(
//                text = value,
//                style = MaterialTheme.typography.headlineSmall.copy(
//                    fontWeight = FontWeight.Bold
//                ),
//                color = color,
//                modifier = Modifier.padding(top = 4.dp)
//            )
//        }
//    }
//}

@Composable
fun DailyProductionChart(
    data: List<DailyProduction>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No data available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val maxQuantity = data.maxOfOrNull { it.quantity } ?: 1.0
    val minQuantity = data.minOfOrNull { it.quantity } ?: 0.0
    val quantityRange = maxQuantity - minQuantity

    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    // Animation values
    val animationProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = 2000,
            easing = FastOutSlowInEasing
        )
    )

    val pointAnimationProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = 2500,
            delayMillis = 500,
            easing = FastOutSlowInEasing
        )
    )

    // Day labels
    val dayLabels = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(600.dp)
            .padding(24.dp)
    ) {
        val chartWidth = size.width - 120.dp.toPx() // Space for Y-axis labels
        val chartHeight = size.height - 80.dp.toPx() // Space for X-axis labels
        val chartStartX = 80.dp.toPx()
        val chartStartY = 20.dp.toPx()

        val pointsCount = data.size
        if (pointsCount < 2) return@Canvas

        // Draw grid lines
        val gridColor = surfaceVariant.copy(alpha = 0.4f)

        // Horizontal grid lines (Y-axis)
        for (i in 0..5) {
            val y = chartStartY + (chartHeight / 5) * i
            drawLine(
                color = gridColor,
                start = Offset(chartStartX, y),
                end = Offset(chartStartX + chartWidth, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
            )
        }

        // Vertical grid lines (X-axis)
        for (i in 0 until pointsCount) {
            val x = chartStartX + (chartWidth / (pointsCount - 1).coerceAtLeast(1)) * i
            drawLine(
                color = gridColor,
                start = Offset(x, chartStartY),
                end = Offset(x, chartStartY + chartHeight),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
            )
        }

        // Calculate smooth curve points
        val stepX = chartWidth / (pointsCount - 1).coerceAtLeast(1)
        val points = data.mapIndexed { index, dailyData ->
            val x = chartStartX + index * stepX
            val normalizedValue = if (quantityRange > 0) {
                (dailyData.quantity - minQuantity) / quantityRange
            } else 0.5
            val y = chartStartY + chartHeight - (normalizedValue * chartHeight).toFloat()
            Offset(x, y)
        }

        // Create smooth curve path using cubic bezier
        val path = Path()
        val animatedPoints = points.take((points.size * animationProgress).toInt().coerceAtLeast(1))

        if (animatedPoints.isNotEmpty()) {
            path.moveTo(animatedPoints[0].x, animatedPoints[0].y)

            for (i in 1 until animatedPoints.size) {
                val current = animatedPoints[i]
                val previous = animatedPoints[i - 1]
                val next = if (i + 1 < animatedPoints.size) animatedPoints[i + 1] else current

                // Calculate control points for smooth curve
                val controlPoint1X = previous.x + (current.x - previous.x) * 0.5f
                val controlPoint1Y = previous.y

                val controlPoint2X = current.x - (next.x - current.x) * 0.3f
                val controlPoint2Y = current.y

                path.cubicTo(
                    controlPoint1X, controlPoint1Y,
                    controlPoint2X, controlPoint2Y,
                    current.x, current.y
                )
            }
        }

        // Draw gradient fill under the curve
        val gradientPath = Path().apply {
            addPath(path)
            if (animatedPoints.isNotEmpty()) {
                lineTo(animatedPoints.last().x, chartStartY + chartHeight)
                lineTo(chartStartX, chartStartY + chartHeight)
                close()
            }
        }

        val gradient = Brush.verticalGradient(
            colors = listOf(
                primary.copy(alpha = 0.4f),
                primary.copy(alpha = 0.2f),
                primary.copy(alpha = 0.05f),
                Color.Transparent
            ),
            startY = chartStartY,
            endY = chartStartY + chartHeight
        )

        drawPath(
            path = gradientPath,
            brush = gradient
        )

        // Draw the main curve with rounded line caps
        drawPath(
            path = path,
            color = primary,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Draw animated points with beautiful effects
        animatedPoints.forEachIndexed { index, point ->
            val pointScale = if (index < (points.size * pointAnimationProgress).toInt()) {
                1f
            } else {
                0f
            }

            // Outer glow circle
            drawCircle(
                color = primary.copy(alpha = 0.2f),
                radius = 16.dp.toPx() * pointScale,
                center = point
            )

            // Middle circle
            drawCircle(
                color = primary.copy(alpha = 0.6f),
                radius = 10.dp.toPx() * pointScale,
                center = point
            )

            // Inner circle
            drawCircle(
                color = primary,
                radius = 6.dp.toPx() * pointScale,
                center = point
            )

            // Center highlight
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx() * pointScale,
                center = point
            )
        }
    }
}



@Composable
fun CowProductionChart(
    data: List<CowProduction>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No data available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    // Limit to top 10 cows for better visibility
    val topCows = data.sortedByDescending { it.quantity }.take(10)
    val maxQuantity = topCows.maxOfOrNull { it.quantity } ?: 1.0

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Chart area with bars
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Y-axis scale indicator (positioned on the left side)
            Column(
                modifier = Modifier
                    .height(180.dp)
                    .width(50.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Scale markers from top to bottom
                listOf(
                    maxQuantity,
                    maxQuantity * 0.75,
                    maxQuantity * 0.5,
                    maxQuantity * 0.25,
                    0.0
                ).forEach { value ->
                    Text(
                        text = "${value.toInt()}L",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Bars area
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(180.dp)
                    .padding(end = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                topCows.forEachIndexed { index, cowData ->
                    // Calculate bar height (max 140dp for chart area, leaving space for labels)
                    val barHeight = ((cowData.quantity / maxQuantity * 180).coerceAtMost(140.0)).dp

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Production value at top of bar
                        Text(
                            text = "${cowData.quantity.toInt()}L",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Bar
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(barHeight)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(
                                        topStart = 4.dp,
                                        topEnd = 4.dp
                                    )
                                )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Cow name at bottom (X-axis label)
                        Text(
                            text = cowData.cowName ?: cowData.cowId,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            modifier = Modifier.width(40.dp),
                            textAlign = TextAlign.Center
                        )

                        // Entry count
                        Text(
                            text = "${cowData.entries}",
                            fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MilkEntryCard(
    entry: MilkInEntry,
    viewModel: MilkInViewModel = koinInject()
) {
    var showHealthDetails by remember { mutableStateOf(false) }
    var cowHealthDetails by remember { mutableStateOf<CowHealthDetailsResponse?>(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (entry.cowId != null) {
                    viewModel.getCowHealthDetails(entry.cowId) { healthDetails ->
                        cowHealthDetails = healthDetails
                        showHealthDetails = true
                    }
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon section with more appealing design
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        color = when (entry.milkingType) {
                            MilkingType.MORNING -> MaterialTheme.colorScheme.primaryContainer
                            MilkingType.EVENING -> MaterialTheme.colorScheme.secondaryContainer
                        },
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (entry.milkingType) {
                        MilkingType.MORNING -> Icons.Filled.Input
                        MilkingType.EVENING -> Icons.Filled.Input
                    },
                    contentDescription = "Milk Entry",
                    tint = when (entry.milkingType) {
                        MilkingType.MORNING -> MaterialTheme.colorScheme.primary
                        MilkingType.EVENING -> MaterialTheme.colorScheme.secondary
                    },
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Details section
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cow ID: ${entry.cowId ?: "Unknown"}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                text = "${entry.liters}L",
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            labelColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Owner: ${entry.ownerId}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = entry.date.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Milking type badge
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = entry.milkingType.name.lowercase()
                                .replaceFirstChar { it.uppercase() },
                            fontSize = 12.sp
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (entry.milkingType == MilkingType.MORNING)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.secondaryContainer,
                        labelColor = if (entry.milkingType == MilkingType.MORNING)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    modifier = Modifier.height(28.dp)
                )
            }
        }
    }

    // Show health details dialog when clicked
    if (showHealthDetails && cowHealthDetails != null) {
        CowHealthDetailsDialog(
            healthDetails = cowHealthDetails!!,
            onDismiss = { 
                showHealthDetails = false
                cowHealthDetails = null
            }
        )
    }
}

@Composable
fun AddMilkEntryDialog(
    onDismiss: () -> Unit,
    onSave: (MilkInEntry) -> Unit,
    viewModel: MilkInViewModel = koinInject()
) {
    val cows by viewModel.cows.collectAsState()
    val members by viewModel.members.collectAsState()

    var selectedCow by remember { mutableStateOf<Cow?>(null) }
    var selectedMember by remember { mutableStateOf<Member?>(null) }
    var type by remember { mutableStateOf(MilkingType.MORNING) }
    var liters by remember { mutableStateOf("") }
    var selectedDate by remember {
        mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date)
    }
    var showCowDropdown by remember { mutableStateOf(false) }
    var showMilkingTypeDropdown by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showHealthDetails by remember { mutableStateOf(false) }
    var selectedCowHealthDetails by remember { mutableStateOf<CowHealthDetailsResponse?>(null) }
    var eligibilityError by remember { mutableStateOf<String?>(null) }
    var isCheckingEligibility by remember { mutableStateOf(false) }

    // Function to check cow eligibility
    fun checkCowEligibility(cow: Cow) {
        isCheckingEligibility = true
        eligibilityError = null
        
        viewModel.checkCowEligibility(cow.cowId ?: "") { isEligible, reason ->
            isCheckingEligibility = false
            if (!isEligible) {
                eligibilityError = reason
                selectedCow = null // Deselect the cow
            } else {
                selectedCow = cow
                selectedMember = members.find { it.memberId == cow.ownerId }
                eligibilityError = null
            }
        }
    }

    // Function to get cow health details
    fun getCowHealthDetails(cow: Cow) {
        viewModel.getCowHealthDetails(cow.cowId ?: "") { healthDetails ->
            selectedCowHealthDetails = healthDetails
            showHealthDetails = true
        }
    }

    AlertDialog(
        modifier = Modifier
            .fillMaxWidth()
            .background(  color = Color.Transparent,  shape = RoundedCornerShape(4.dp))
            .padding(16.dp),
        shape = RoundedCornerShape(4.dp),
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Add Milk Entry",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Show eligibility error if any
                eligibilityError?.let { error ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Warning",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // Cow Selection Dropdown
                Box {
                    OutlinedTextField(
                        value = selectedCow?.let {
                            val ownerName =
                                members.find { it.memberId == selectedCow?.ownerId }?.name
                                    ?: "Unknown"
                            "${it.name} - $ownerName"
                        } ?: "",
                        onValueChange = { },
                        label = { Text("Select Cow") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            Row {
                                if (isCheckingEligibility) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Checking...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                TextButton(onClick = { showCowDropdown = true }) {
                                    Text("▼")
                                }
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = showCowDropdown,
                        onDismissRequest = { showCowDropdown = false }
                    ) {
                        if (cows.isEmpty()) {
                            DropdownMenuItem(
                                onClick = { },
                                text = { Text("No cows available") },
                                enabled = false
                            )
                        } else {
                            cows.forEach { cow ->
                                val ownerName =
                                    members.find { it.memberId == cow.ownerId }?.name ?: "Unknown"

                                // Evaluate health status for this cow
                                val (healthColor, healthWarning, isBlocked) = when (cow.status.healthStatus) {
                                    HealthStatus.HEALTHY -> Triple(
                                        Color(0xFF4ADE80),
                                        null,
                                        false
                                    )

                                    HealthStatus.NEEDS_ATTENTION -> Triple(
                                        Color(0xFFFBBF24), 
                                        "Needs attention",
                                        false
                                    )

                                    HealthStatus.UNDER_TREATMENT -> Triple(
                                        Color(0xFFF87171), 
                                        "Under treatment - Cannot collect milk",
                                        true
                                    )

                                    HealthStatus.GESTATION -> Triple(
                                        Color(0xFF9C27B0),
                                        "Gestation period",
                                        false
                                    )

                                    HealthStatus.VACCINATED -> Triple(
                                        Color(0xFF2196F3),
                                        "Vaccinated - Cannot collect milk for 48h",
                                        true
                                    )

                                    HealthStatus.SICK -> Triple(
                                        Color(0xFFF87171),
                                        "Sick - Cannot collect milk",
                                        true
                                    )

                                    HealthStatus.ANTIBIOTICS -> Triple(
                                        Color(0xFFE91E63),
                                        "Antibiotics - Cannot collect milk for 72h",
                                        true
                                    )
                                }

                                DropdownMenuItem(
                                    onClick = {
                                        showCowDropdown = false
                                        // For debugging, show what we're checking


                                        // Check eligibility before selecting
                                        checkCowEligibility(cow)
                                    },
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = cow.name,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isBlocked) 
                                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                    else 
                                                        MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Owner: $ownerName • ${cow.breed}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = if (isBlocked)
                                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                    else
                                                        MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(top = 4.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(8.dp)
                                                            .background(
                                                                healthColor,
                                                                shape = RoundedCornerShape(50)
                                                            )
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = cow.status.healthStatus.name.replace(
                                                            "_",
                                                            " "
                                                        ),
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = healthColor
                                                    )
                                                }
                                                healthWarning?.let {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.padding(top = 2.dp)
                                                    ) {
                                                        if (isBlocked) {
                                                            Icon(
                                                                imageVector = Icons.Default.Warning,
                                                                contentDescription = "Blocked",
                                                                tint = MaterialTheme.colorScheme.error,
                                                                modifier = Modifier.size(12.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                        }
                                                        Text(
                                                            text = it,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = if (isBlocked) 
                                                                MaterialTheme.colorScheme.error 
                                                            else 
                                                                MaterialTheme.colorScheme.onSurfaceVariant,
                                                            fontWeight = if (isBlocked) FontWeight.Bold else FontWeight.Normal
                                                        )
                                                    }
                                                }
                                            }
                                            
                                            // Health details button
                                            IconButton(
                                                onClick = {
                                                    showCowDropdown = false
                                                    getCowHealthDetails(cow)
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Info,
                                                    contentDescription = "Health Details",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Milking Type Dropdown
                Box {
                    OutlinedTextField(
                        value = type.name,
                        onValueChange = { },
                        label = { Text("Milking Type") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            TextButton(onClick = { showMilkingTypeDropdown = true }) {
                                Text("▼")
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = showMilkingTypeDropdown,
                        onDismissRequest = { showMilkingTypeDropdown = false }
                    ) {
                        MilkingType.entries.forEach { milkType ->
                            DropdownMenuItem(
                                onClick = {
                                    type = milkType
                                    showMilkingTypeDropdown = false
                                },
                                text = { Text(milkType.name) }
                            )
                        }
                    }
                }

                // Quantity Input
                OutlinedTextField(
                    value = liters,
                    onValueChange = { liters = it.filter { it.isDigit() || it == '.' } },
                    label = { Text("Quantity (liters)") },
                    placeholder = { Text("e.g., 15.5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                // Date Selection
                OutlinedTextField(
                    value = selectedDate.toString(),
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Date") },
                    trailingIcon = {
                        TextButton(onClick = { showDatePicker = true }) {
                            Text("📅")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedCow != null && selectedMember != null &&
                        liters.isNotBlank()
                    ) {
                        val quantity = liters.toDoubleOrNull() ?: 0.0
                        if (quantity > 0) {
                            onSave(
                                MilkInEntry(
                                    entryId = null,  // Explicitly null - server will generate the ID
                                    cowId = selectedCow!!.cowId ?: "",
                                    ownerId = selectedCow!!.ownerId,  // Use the cow's ownerId which is already validated
                                    liters = quantity,
                                    date = selectedDate,
                                    milkingType = type
                                )
                            )
                        }
                    }
                },
                enabled = selectedCow != null && selectedMember != null &&
                        liters.isNotBlank() && liters.toDoubleOrNull()?.let { it > 0 } == true,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Save Entry")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    // Cow Health Details Dialog
    if (showHealthDetails && selectedCowHealthDetails != null) {
        CowHealthDetailsDialog(
            healthDetails = selectedCowHealthDetails!!,
            onDismiss = { 
                showHealthDetails = false
                selectedCowHealthDetails = null
            }
        )
    }

    // Enhanced Date Picker Dialog
    if (showDatePicker) {
        EnhancedDatePickerDialog(
            selectedDate = selectedDate,
            onDateSelected = { date ->
                selectedDate = date
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
fun CowHealthDetailsDialog(
    healthDetails: CowHealthDetailsResponse,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Health Details",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cow Health Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Cow Info Header
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = healthDetails.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "ID: ${healthDetails.cowId}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Health Status
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (healthDetails.canCollectMilk) 
                            MaterialTheme.colorScheme.secondaryContainer 
                        else 
                            MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (healthDetails.canCollectMilk) 
                                Icons.Default.Check 
                            else 
                                Icons.Default.Warning,
                            contentDescription = "Status",
                            tint = if (healthDetails.canCollectMilk) 
                                MaterialTheme.colorScheme.onSecondaryContainer 
                            else 
                                MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Current Status: ${healthDetails.healthStatus}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (healthDetails.canCollectMilk) 
                                    MaterialTheme.colorScheme.onSecondaryContainer 
                                else 
                                    MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = if (healthDetails.canCollectMilk) 
                                    "✅ Eligible for milk collection" 
                                else 
                                    "❌ Not eligible for milk collection",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (healthDetails.canCollectMilk) 
                                    MaterialTheme.colorScheme.onSecondaryContainer 
                                else 
                                    MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // Blocked Reason (if any)
                healthDetails.blockedReason?.let { reason ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "⚠️ Blocking Reason",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = reason,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // Vaccination Details
                if (healthDetails.vaccinationLast != null || healthDetails.vaccinationWaitingPeriodEnd != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "💉 Vaccination Information",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            healthDetails.vaccinationLast?.let {
                                Text(
                                    text = "Last Vaccination: $it",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                            
                            healthDetails.vaccinationWaitingPeriodEnd?.let {
                                Text(
                                    text = "Waiting Period Ends: $it",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "⏰ Cannot collect milk until this date",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }

                // Antibiotic Treatment Details
                if (healthDetails.antibioticTreatment != null || healthDetails.antibioticWaitingPeriodEnd != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "💊 Antibiotic Treatment",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            healthDetails.antibioticTreatment?.let {
                                Text(
                                    text = "Treatment Date: $it",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            
                            healthDetails.antibioticWaitingPeriodEnd?.let {
                                Text(
                                    text = "Waiting Period Ends: $it",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "⏰ Cannot collect milk until this date (72h waiting period)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Close")
            }
        }
    )
}

@Composable
fun EnhancedDatePickerDialog(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    var displayMonth by remember { mutableStateOf(selectedDate.month) }
    var displayYear by remember { mutableStateOf(selectedDate.year) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .background(  color = Color.Transparent,  shape = RoundedCornerShape(4.dp))
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select Date",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Month/Year Navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if (displayMonth.ordinal == 0) {
                                displayMonth = Month.DECEMBER
                                displayYear -= 1
                            } else {
                                displayMonth = Month.entries.toTypedArray()[displayMonth.ordinal - 1]
                            }
                        },
                        enabled = displayYear > currentDate.year - 2,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("◀")
                    }

                    Text(
                        text = "${displayMonth.name} $displayYear",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = {
                            if (displayMonth.ordinal == 11) {
                                displayMonth = Month.JANUARY
                                displayYear += 1
                            } else {
                                displayMonth = Month.entries.toTypedArray()[displayMonth.ordinal + 1]
                            }
                        },
                        enabled = displayYear < currentDate.year ||
                                (displayYear == currentDate.year && displayMonth <= currentDate.month),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("▶")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Day headers
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier
                                .weight(1f)
                                .padding(4.dp),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Calendar Grid
                val daysInMonth = when (displayMonth) {
                    Month.FEBRUARY -> if (displayYear % 4 == 0) 29 else 28
                    Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30
                    else -> 31
                }

                // First day of month
                val firstDayOfMonth = LocalDate(displayYear, displayMonth, 1)
                val startDayOfWeek = firstDayOfMonth.dayOfWeek.ordinal % 7 // 0 = Sunday

                for (week in 0..5) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (dayOfWeek in 0..6) {
                            val dayOfMonth = week * 7 + dayOfWeek - startDayOfWeek + 1

                            if (dayOfMonth in 1..daysInMonth) {
                                val dateToCheck = LocalDate(displayYear, displayMonth, dayOfMonth)
                                val isToday = dateToCheck == currentDate
                                val isPastDate = dateToCheck < currentDate
                                val isSelected = dateToCheck == selectedDate

                                Button(
                                    onClick = {
                                        if (!isPastDate) {
                                            onDateSelected(dateToCheck)
                                        }
                                    },
                                    enabled = !isPastDate,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = when {
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            isToday -> MaterialTheme.colorScheme.secondary
                                            else -> MaterialTheme.colorScheme.surface
                                        },
                                        contentColor = when {
                                            isSelected -> MaterialTheme.colorScheme.onPrimary
                                            isToday -> MaterialTheme.colorScheme.onSecondary
                                            isPastDate -> MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                alpha = 0.4f
                                            )

                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(2.dp)
                                        .height(36.dp)
                                ) {
                                    Text(
                                        text = dayOfMonth.toString(),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            } else {
                                Spacer(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp)
                                        .padding(2.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onDismiss, shape = RoundedCornerShape(4.dp)) {
                        Text("Done")
                    }
                }
            }
        }
    }
}

@Composable
fun MilkSpoiltContent(
    viewModel: MilkSpoiltViewModel = koinInject()
) {
    val spoiltMilkEntries by viewModel.milkSpoiltEntries.collectAsState()
    val totalSpoiltLiters by viewModel.totalSpoiltLiters.collectAsState()
    val totalIncidents by viewModel.totalIncidents.collectAsState()
    val totalLoss by viewModel.totalLoss.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var searchTerm by remember { mutableStateOf("") }

    // Filter spoilt milk based on search
    val filteredSpoilt = spoiltMilkEntries.filter { item ->
        val matchesSearch =
            item.cause?.toString()?.contains(searchTerm, ignoreCase = true) ?: false ||
                    item.date.toString().contains(searchTerm, ignoreCase = true)

        matchesSearch
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Error message
        errorMessage?.let {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
        // Summary Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    SpoiltSummaryCard(
                        title = "Total Spoilt",
                        value = "${totalSpoiltLiters.toInt()}L",
                        icon = Icons.Filled.Warning,
                        color = MaterialTheme.colorScheme.error,
                        subtitle = "Milk spoilt"
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    SpoiltSummaryCard(
                        title = "Total Incidents",
                        value = totalIncidents.toString(),
                        icon = Icons.AutoMirrored.Filled.List,
                        color = MaterialTheme.colorScheme.tertiary,
                        subtitle = "Reported cases"
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    SpoiltSummaryCard(
                        title = "Total Loss",
                        value = "KES ${totalLoss.toInt()}",
                        icon = Icons.Filled.Money,
                        color = MaterialTheme.colorScheme.error,
                        subtitle = "Financial impact"
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Search
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = searchTerm,
                        onValueChange = { searchTerm = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Search by cause or date") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                        singleLine = true
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Spoilt Milk Records Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(4.dp)
            ) {
                Column {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Spoilt Milk Records",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "Showing ${filteredSpoilt.size} of ${spoiltMilkEntries.size} incidents",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Records List
        if (filteredSpoilt.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = "No records",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No spoilt milk records found",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Try adjusting your search criteria.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(filteredSpoilt.size) { index ->
                Column {
                    SpoiltMilkCard(entry = filteredSpoilt[index])
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun SpoiltMilkCard(entry: MilkSpoiltEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with cause
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "Spoilt Milk",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Spoilage: ${entry.date}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Cause badge
                entry.cause?.let { cause ->
                    val (backgroundColor, textColor) = when (cause.toString()) {
                        "EXPIRED" -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
                        "CONTAMINATION" -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
                        "TEMPERATURE" -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
                        "SOUR" -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    AssistChip(
                        onClick = { },
                        label = { Text(cause.toString()) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = backgroundColor,
                            labelColor = textColor
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Amount Spoilt",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.LocalDrink,
                            contentDescription = "Amount",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${entry.amountSpoilt.toInt()}L",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Financial Loss",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "KES ${entry.lossAmount.toInt()}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun SpoiltSummaryCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    subtitle: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
