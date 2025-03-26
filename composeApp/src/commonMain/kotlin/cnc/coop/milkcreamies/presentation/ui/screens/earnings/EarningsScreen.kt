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
package cnc.coop.milkcreamies.presentation.ui.screens.earnings

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cnc.coop.milkcreamies.presentation.viewmodel.earnings.EarningsViewModel
import cnc.coop.milkcreamies.models.MilkOutEntry
import cnc.coop.milkcreamies.presentation.ui.common.components.TopHeader
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.koin.compose.koinInject
import kotlin.math.max

data class DailyEarningsData(
    val day: String,
    val income: Int,
    val target: Int = 100000
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarningsScreen(viewModel: EarningsViewModel = koinInject()) {
    val earningsSummary by viewModel.earningsSummary.collectAsState()
    val recentTransactions by viewModel.recentTransactions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val currentInventory by viewModel.currentInventory.collectAsState()
    val stockSummary by viewModel.inventoryStockSummary.collectAsState()

    // Filter state for chart
    var selectedPeriod by remember { mutableStateOf("Last 7 Days") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val periods = listOf("Last 7 Days", "Last 14 Days", "Last 28 Days")

    // Calculate chart data based on recent transactions
    val chartData = remember(recentTransactions, selectedPeriod) {
        val days = when (selectedPeriod) {
            "Last 7 Days" -> 7
            "Last 14 Days" -> 14
            "Last 28 Days" -> 28
            else -> 7
        }

        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val dateToEarningsByDay = mutableMapOf<kotlinx.datetime.DayOfWeek, Int>()
        val dateMap = mutableMapOf<kotlinx.datetime.DayOfWeek, kotlinx.datetime.LocalDate>()

        // Initialize all days of the week with 0 earnings
        for (dayOffset in days-1 downTo 0) {
            val date = kotlinx.datetime.LocalDate.fromEpochDays(today.toEpochDays() - dayOffset)
            dateToEarningsByDay[date.dayOfWeek] = 0
            dateMap[date.dayOfWeek] = date
        }

        // Group transactions by date and sum earnings
        recentTransactions.forEach { transaction ->
            val daysDiff = today.toEpochDays() - transaction.date.toEpochDays()
            if (daysDiff < days) {
                val earnings = (transaction.quantitySold * transaction.pricePerLiter).toInt()
                dateToEarningsByDay[transaction.date.dayOfWeek] = 
                    (dateToEarningsByDay[transaction.date.dayOfWeek] ?: 0) + earnings
            }
        }

        // Create a consistent list from Monday to Sunday
        val orderedDays = listOf(
            kotlinx.datetime.DayOfWeek.MONDAY,
            kotlinx.datetime.DayOfWeek.TUESDAY,
            kotlinx.datetime.DayOfWeek.WEDNESDAY,
            kotlinx.datetime.DayOfWeek.THURSDAY,
            kotlinx.datetime.DayOfWeek.FRIDAY,
            kotlinx.datetime.DayOfWeek.SATURDAY,
            kotlinx.datetime.DayOfWeek.SUNDAY
        )
        
        // When showing 14 days, we take 2 weeks of data
        val daysToShow = if (selectedPeriod == "Last 14 Days") 14 else 7
        
        // For 14 days, we'll repeat the days list
        val finalDays = if (selectedPeriod == "Last 14 Days") {
            orderedDays + orderedDays
        } else {
            orderedDays
        }
        
        finalDays.take(daysToShow).map { dayOfWeek ->
            val dayAbbreviation = when (dayOfWeek) {
                kotlinx.datetime.DayOfWeek.MONDAY -> "Mon"
                kotlinx.datetime.DayOfWeek.TUESDAY -> "Tue"
                kotlinx.datetime.DayOfWeek.WEDNESDAY -> "Wed"
                kotlinx.datetime.DayOfWeek.THURSDAY -> "Thu"
                kotlinx.datetime.DayOfWeek.FRIDAY -> "Fri"
                kotlinx.datetime.DayOfWeek.SATURDAY -> "Sat"
                kotlinx.datetime.DayOfWeek.SUNDAY -> "Sun"
                else -> "???" // This should never happen, but needed for exhaustive when
            }
            
            DailyEarningsData(
                day = dayAbbreviation,
                income = dateToEarningsByDay[dayOfWeek] ?: 0,
                target = 200000 / daysToShow // Adjust target based on days shown
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Header
        TopHeader(
            onAddSale = { viewModel.refreshEarnings() },
            currentTitle = "Milk Intake",
            subTitle = "Track and analyze your daily milk production data",
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {

            // Error Message
            errorMessage?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Server Connection Issue",
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 14.sp
                            )
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Loading State
            if (isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Loading earnings data...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Earnings Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EarningsCard(
                    title = "Today's Earnings",
                    amount = "Ksh ${earningsSummary.todayEarnings.toInt()}",
                    subtitle = "Real-time tracking",
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.primary
                )
                EarningsCard(
                    title = "Weekly Earnings",
                    amount = "Ksh ${earningsSummary.weeklyEarnings.toInt()}",
                    subtitle = "last 7 days",
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.tertiary
                )
                EarningsCard(
                    title = "Monthly Earnings",
                    amount = "Ksh ${earningsSummary.monthlyEarnings.toInt()}",
                    subtitle = "last 30 days",
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Animated Revenue Chart
            AnimatedRevenueChart(
                data = chartData,
                selectedPeriod = selectedPeriod,
                periods = periods,
                isDropdownExpanded = isDropdownExpanded,
                onDropdownExpandedChange = { isDropdownExpanded = it },
                onPeriodSelected = { selectedPeriod = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Real-time Stock Information
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Live Stock Status",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Current Stock: ${currentInventory.currentStock.toInt()}L",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                text = "Daily Production: ${stockSummary.dailyProduce.toInt()}L",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        Column {
                            Text(
                                text = "Daily Sales: ${stockSummary.dailyTotalLitersSold.toInt()}L",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                text = "Weekly Spoilt: ${stockSummary.weeklySpoilt.toInt()}L",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Data Source: Real-time Inventory Manager",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recent Transactions
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Transactions",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${recentTransactions.size} transactions",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (recentTransactions.isNotEmpty()) {
                        recentTransactions.take(10).forEach { transaction ->
                            TransactionItem(transaction = transaction)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    } else {
                        Text(
                            text = "No recent transactions",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 20.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimatedRevenueChart(
    data: List<DailyEarningsData>,
    selectedPeriod: String,
    periods: List<String>,
    isDropdownExpanded: Boolean,
    onDropdownExpandedChange: (Boolean) -> Unit,
    onPeriodSelected: (String) -> Unit
) {
    val maxValue = 200000 // Fixed target of 200K

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(32.dp)
        ) {
            // Header Section with Filter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Revenue",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ksh ${data.sumOf { it.income }}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = selectedPeriod,
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }

                // Filter Dropdown
                ExposedDropdownMenuBox(
                    expanded = isDropdownExpanded,
                    onExpandedChange = onDropdownExpandedChange
                ) {
                    OutlinedTextField(
                        value = selectedPeriod,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color(0xFF6B7280)
                            )
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .width(140.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6B7280),
                            unfocusedBorderColor = Color(0xFF6B7280)
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { onDropdownExpandedChange(false) }
                    ) {
                        periods.forEach { period ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = period,
                                        fontSize = 14.sp
                                    )
                                },
                                onClick = {
                                    onPeriodSelected(period)
                                    onDropdownExpandedChange(false)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Chart Section
            ChartSection(data = data, maxValue = maxValue)

            Spacer(modifier = Modifier.height(24.dp))

            // Legend Section
            LegendSection()
        }
    }
}

@Composable
private fun ChartSection(data: List<DailyEarningsData>, maxValue: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
    ) {
        // Y-axis labels
        YAxisLabels()

        Spacer(modifier = Modifier.width(16.dp))

        // Chart area
        ChartArea(data = data, maxValue = maxValue)
    }
}

@Composable
private fun YAxisLabels() {
    Column(
        modifier = Modifier
            .width(64.dp)
            .height(280.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        listOf("200K", "150K", "100K", "50K", "0K").forEach { label ->
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color(0xFF9CA3AF)
            )
        }
    }
}

@Composable
private fun ChartArea(data: List<DailyEarningsData>, maxValue: Int) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Grid lines
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val chartHeight = 280.dp.toPx()
            val chartWidth = size.width

            // Draw horizontal grid lines
            for (i in 0..4) {
                val y = chartHeight - (i * chartHeight / 4)
                drawLine(
                    color = Color(0xFFF9FAFB),
                    start = Offset(0f, y),
                    end = Offset(chartWidth, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Draw vertical line (left border)
            drawLine(
                color = Color(0xFFF3F4F6),
                start = Offset(0f, 0f),
                end = Offset(0f, chartHeight),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Chart bars
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, top = 20.dp, bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            data.forEachIndexed { index, item ->
                ChartBar(
                    data = item,
                    maxValue = maxValue,
                    animationDelay = index * 100
                )
            }
        }
    }
}

@Composable
private fun ChartBar(
    data: DailyEarningsData,
    maxValue: Int,
    animationDelay: Int
) {
    var startAnimation by remember { mutableStateOf(false) }

    val animatedHeight by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 700,
            delayMillis = animationDelay,
            easing = FastOutSlowInEasing
        ),
        label = "bar_animation"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(64.dp)
    ) {
        Box(
            modifier = Modifier.height(240.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            val totalHeight = (data.target.toFloat() / maxValue) * 500.dp.value
            val incomeHeight = (data.income.toFloat() / maxValue) * 500.dp.value
            val gapHeight = totalHeight - incomeHeight

            Canvas(
                modifier = Modifier
                    .width(50.dp)
                    .height((totalHeight * animatedHeight).dp)
            ) {
                val barWidth = size.width
                val currentTotalHeight = size.height
                val currentIncomeHeight = (incomeHeight / totalHeight) * currentTotalHeight
                val currentGapHeight = currentTotalHeight - currentIncomeHeight

                // Draw gap section (yellow gradient - top)
                if (currentGapHeight > 0) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFEF3C7),
                                Color(0xFFFCD34D),
                                Color(0xFFF59E0B)
                            ),
                            startY = 0f,
                            endY = currentGapHeight
                        ),
                        topLeft = Offset(0f, 0f),
                        size = Size(barWidth, currentGapHeight)
                    )
                }

                // Draw income section (green gradient - bottom)
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF34D399),
                            Color(0xFF10B981),
                            Color(0xFF059669)
                        ),
                        startY = currentGapHeight,
                        endY = currentTotalHeight
                    ),
                    topLeft = Offset(0f, currentGapHeight),
                    size = Size(barWidth, currentIncomeHeight)
                )

                // Add highlight effect at top
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.8f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = 32.dp.toPx()
                    ),
                    topLeft = Offset(0f, 0f),
                    size = Size(barWidth, minOf(32.dp.toPx(), currentTotalHeight))
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Day label (Mon, Tue, etc.)
        Text(
            text = data.day,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF6B7280)
        )
        
        // Add value below day name
        if (data.income > 0) {
            Text(
                text = "${data.income/1000}K",
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF6B7280).copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun LegendSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        LegendItem(
            color = Color(0xFF10B981),
            label = "Income"
        )

        LegendItem(
            color = Color(0xFFF59E0B),
            label = "Gap to Target (200K)"
        )
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(color)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF4B5563)
        )
    }
}

@Composable
fun EarningsCard(
    title: String,
    amount: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = amount,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun TransactionItem(transaction: MilkOutEntry) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Ksh ${(transaction.quantitySold * transaction.pricePerLiter).toInt()}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Qty: ${transaction.quantitySold.toInt()}L • Price: Ksh ${transaction.pricePerLiter.toInt()}/L",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = "Payment: ${transaction.paymentMode.name}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = transaction.date.toString(),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = "ID: ${transaction.saleId?.take(8) ?: "N/A"}",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}
