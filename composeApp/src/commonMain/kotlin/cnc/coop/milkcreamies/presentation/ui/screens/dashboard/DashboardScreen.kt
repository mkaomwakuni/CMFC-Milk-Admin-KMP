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
package cnc.coop.milkcreamies.presentation.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cnc.coop.milkcreamies.models.*
import cnc.coop.milkcreamies.presentation.ui.common.components.*
import cnc.coop.milkcreamies.presentation.viewmodel.dashboard.DashboardViewModel
import kotlinx.datetime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
// Import the custom ExportState class here explicitly
import cnc.coop.milkcreamies.presentation.ui.common.components.ExportState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen( viewModel: DashboardViewModel = koinInject()) {
    val cows by viewModel.cows.collectAsState()
    val members by viewModel.members.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val dashboardMetrics by viewModel.dashboardMetrics.collectAsState()
    val milkInEntries by viewModel.milkInEntries.collectAsState()
    val milkOutEntries by viewModel.milkOutEntries.collectAsState()

    // Server data comes through InventoryManager (single source of truth)
    val currentInventory by viewModel.currentInventory.collectAsState()
    val stockSummary by viewModel.inventoryStockSummary.collectAsState()
    val earningsSummary by viewModel.inventoryEarningsSummary.collectAsState()
    
    val isLoading by viewModel.isLoading.collectAsState()
    
    // Use rememberCoroutineScope for launching coroutines from composition
    val scope = rememberCoroutineScope()

    var showAddCowDialog by remember { mutableStateOf(false) }







    // Filter state moved to screen level
    var selectedPeriod by remember { mutableStateOf("Last 7 Days") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val periods = listOf("Last 7 Days", "Last 14 Days", "Last Month", "This Month")
    
    // Export dialog state
    var showExportDialog by remember { mutableStateOf(false) }
    // Define explicit class since import doesn't seem to resolve properly with AS
    var exportState by remember { mutableStateOf<cnc.coop.milkcreamies.presentation.ui.common.components.ExportState>(
        cnc.coop.milkcreamies.presentation.ui.common.components.ExportState.Initial
    ) }

    // Refresh data when period changes
    LaunchedEffect(selectedPeriod) {
        viewModel.refreshData()
    }



    // Ensure data is loaded when screen appears
    LaunchedEffect(Unit) {
        viewModel.refreshData()
    }

    if (showAddCowDialog) {
        AddCowDialog(
            members = members,
            onDismiss = { showAddCowDialog = false },
            onAddCow = { name, breed, age, weight, ownerId, healthStatus ->
                viewModel.addCow(
                    name = name,
                    breed = breed.displayName,
                    age = age,
                    weight = weight,
                    entryDate = Clock.System.now()
                        .toLocalDateTime(TimeZone.currentSystemDefault()).date,
                    ownerId = ownerId,
                    healthStatus = healthStatus
                )
                showAddCowDialog = false
            }
        )
    }

    // Show export format dialog when requested
    if (showExportDialog) {
        ExportFormatDialog(
            onDismissRequest = { showExportDialog = false },
            onFormatSelected = { format ->
                showExportDialog = false
                // Show export progress dialog
                exportState = cnc.coop.milkcreamies.presentation.ui.common.components.ExportState.Exporting()
                
                // Start export in background
                viewModel.exportData(format)
                
                // In a real implementation, we would track the export progress
                // For this demo, we'll simulate success after a delay
                scope.launch {
                    delay(1500) // Simulate processing time
                    val defaultPath = "Downloads" // Simplified path for demonstration
                    exportState = cnc.coop.milkcreamies.presentation.ui.common.components.ExportState.Success(defaultPath)
                }
            }
        )
    }
    
    // Show export progress dialog when export is in progress
    if (exportState != cnc.coop.milkcreamies.presentation.ui.common.components.ExportState.Initial) {
        cnc.coop.milkcreamies.presentation.ui.common.components.ExportProgressDialog(
            exportState = exportState,
            onDismiss = {
                exportState = cnc.coop.milkcreamies.presentation.ui.common.components.ExportState.Initial
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        TopHeader(
            onAddSale = { showAddCowDialog = true },
            currentTitle = "Dashboard Panel Overview",
            subTitle = "Analytics and Reporting Dashboard",
            onExport = { showExportDialog = true }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp)) // Add spacing from top header

                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                    Button(
                        onClick = { viewModel.clearErrorMessage() },
                        modifier = Modifier.padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Clear Error")
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = spacedBy(16.dp)
                ) {
                    listOf(
                        OverviewCardData(
                            "Milk inL)",
                            dashboardMetrics.milkIn.toInt().toString(),
                            Icons.Default.Add,
                            MaterialTheme.colorScheme.primaryContainer
                        ),
                        OverviewCardData(
                            "Milk out(L)",
                            dashboardMetrics.milkOut.toInt().toString(),
                            Icons.Default.Remove,
                            MaterialTheme.colorScheme.errorContainer
                        ),
                        OverviewCardData(
                            "Current Stock(L)",
                            stockSummary.currentStock.toInt().toString(),
                            Icons.Default.Home,
                            MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        OverviewCardData(
                            "Today's Earnings(Kshs)",
                            earningsSummary.todayEarnings.toInt().toString(),
                            Icons.AutoMirrored.Filled.TrendingUp,
                            MaterialTheme.colorScheme.secondaryContainer
                        )
                    ).forEach { card ->
                        OverviewCard(
                            title = card.title,
                            value = card.value,
                            icon = card.icon,
                            color = card.color,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = spacedBy(24.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Left Column - Sales and Milk Report
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = spacedBy(24.dp)
                    ) {
                        // Sales Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = Color.Transparent,
                                    shape = RoundedCornerShape(4.dp)
                                ),
                            shape = RoundedCornerShape(4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column {
                                // Header
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp)
                                        .padding(bottom = 0.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Sales",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1F2937)
                                    )
                                    Text(
                                        text = "View all",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF6B7280),
                                        modifier = Modifier.clickable { /* Handle view all click */ }
                                    )
                                }

                                // Divider
                                androidx.compose.material3.HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 24.dp),
                                    thickness = 1.dp,
                                    color = Color(0xFFF3F4F6)
                                )

                                // Table Header
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF9FAFB))
                                        .padding(horizontal = 24.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    TableHeaderCell("S.No", modifier = Modifier.weight(0.8f))
                                    TableHeaderCell("Name", modifier = Modifier.weight(1.2f))
                                    TableHeaderCell("Date", modifier = Modifier.weight(1.5f))
                                    TableHeaderCell("Quantity(L)", modifier = Modifier.weight(1.2f))
                                    TableHeaderCell(
                                        "Total Amount",
                                        modifier = Modifier.weight(1.3f)
                                    )
                                    TableHeaderCell("Pay Mode", modifier = Modifier.weight(1.2f))
                                }

                                // Table Rows
                                LazyColumn(
                                    modifier = Modifier.height(250.dp)
                                ) {
                                    items(milkOutEntries) { entry ->
                                        val customer =
                                            customers.find { it.customerId == entry.customerId }
                                        val totalAmount = entry.quantitySold * entry.pricePerLiter
                                        val index = milkOutEntries.indexOf(entry)

                                        SalesTableRow(
                                            id = "${index + 1}".padStart(2, '0'),
                                            name = customer?.name ?: "Unknown",
                                            date = entry.date.toString(),
                                            quantity = entry.quantitySold.toInt(),
                                            totalAmount = "${totalAmount.toInt()} KSh",
                                            payMode = entry.paymentMode
                                        )

                                        if (entry != milkOutEntries.last()) {
                                            androidx.compose.material3.HorizontalDivider(
                                                modifier = Modifier.padding(horizontal = 24.dp),
                                                thickness = 1.dp,
                                                color = Color(0xFFF3F4F6)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Milk Report Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = spacedBy(16.dp)
                                    ) {
                                        Text(
                                            text = "Milk Report",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        // Legend
                                        Row(
                                            horizontalArrangement = spacedBy(16.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = spacedBy(4.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(12.dp)
                                                        .background(
                                                            Color(0xFF10B981), // Green for Milk In
                                                            CircleShape
                                                        )
                                                )
                                                Text("Milk In", fontSize = 12.sp)
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(12.dp)
                                                        .background(
                                                            Color(0xFF60A5FA), // Blue for Milk Out
                                                            CircleShape
                                                        )
                                                )
                                                Text("Milk Out", fontSize = 12.sp)
                                            }
                                        }
                                    }

                                    // Filter Dropdown
                                    ExposedDropdownMenuBox(
                                        expanded = isDropdownExpanded,
                                        onExpandedChange = {
                                            isDropdownExpanded = !isDropdownExpanded
                                        }
                                    ) {
                                        OutlinedTextField(
                                            value = selectedPeriod,
                                            onValueChange = {},
                                            readOnly = true,
                                            trailingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Add, // Using Add icon as dropdown arrow
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            },
                                            modifier = Modifier
                                                .menuAnchor()
                                                .width(130.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = MaterialTheme.colorScheme.outline,
                                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                            )
                                        )

                                        ExposedDropdownMenu(
                                            expanded = isDropdownExpanded,
                                            onDismissRequest = { isDropdownExpanded = false }
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
                                                        selectedPeriod = period
                                                        isDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Chart with Y-axis scale
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Y-axis scale (0-500L)
                                    Column(
                                        modifier = Modifier
                                            .width(50.dp)
                                            .height(200.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        listOf(500, 400, 300, 200, 100, 0).forEach { value ->
                                            Text(
                                                text = "${value}L",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }

                                    // Chart area
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(200.dp)
                                            .padding(end = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        // Get filtered data based on selected period
                                        val today =
                                            Clock.System.todayIn(TimeZone.currentSystemDefault())

                                        // Calculate date ranges for filtering
                                        val daysToSubtract = when (selectedPeriod) {
                                            "Last 7 Days" -> 7
                                            "Last 14 Days" -> 14
                                            "Last Month" -> 30
                                            else -> 7
                                        }

                                        val filteredMilkInEntries = when (selectedPeriod) {
                                            "This Month" -> milkInEntries.filter {
                                                it.date.year == today.year && it.date.monthNumber == today.monthNumber
                                            }

                                            else -> milkInEntries.filter { entry ->
                                                val daysDiff =
                                                    today.toEpochDays() - entry.date.toEpochDays()
                                                daysDiff <= daysToSubtract
                                            }
                                        }

                                        val filteredMilkOutEntries = when (selectedPeriod) {
                                            "This Month" -> milkOutEntries.filter {
                                                it.date.year == today.year && it.date.monthNumber == today.monthNumber
                                            }

                                            else -> milkOutEntries.filter { entry ->
                                                val daysDiff =
                                                    today.toEpochDays() - entry.date.toEpochDays()
                                                daysDiff <= daysToSubtract
                                            }
                                        }

                                        // Create days for chart display - show 7 or 14 days based on filter
                                        val daysToShow = if (selectedPeriod == "Last 14 Days") 14 else 7
                                        val days = (0 until daysToShow).map { dayIndex ->
                                            val epochDays = today.toEpochDays() - (daysToShow - 1 - dayIndex)
                                            kotlinx.datetime.LocalDate.fromEpochDays(epochDays)
                                        }

                                        days.forEach { date ->
                                            // Calculate daily totals from real data
                                            val dailyMilkIn = filteredMilkInEntries
                                                .filter { it.date == date }
                                                .sumOf { it.liters }

                                            val dailyMilkOut = filteredMilkOutEntries
                                                .filter { it.date == date }
                                                .sumOf { it.quantitySold }

                                            // Scale bars based on 500L maximum
                                            val maxScale = 500.0
                                            val milkInHeight =
                                                ((dailyMilkIn / maxScale * 170).coerceAtMost(170.0)).dp
                                            val milkOutHeight =
                                                ((dailyMilkOut / maxScale * 170).coerceAtMost(170.0)).dp

                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(horizontal = if (selectedPeriod == "Last 14 Days") 1.dp else 2.dp)
                                            ) {
                                                // Two bars side by side for each day
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                    verticalAlignment = Alignment.Bottom,
                                                    modifier = Modifier.height(170.dp)
                                                ) {
                                                    // Milk In Bar (Green) - Make sure it's visible
                                                    Box(
                                                        modifier = Modifier
                                                            .width(if (selectedPeriod == "Last 14 Days") 10.dp else 14.dp)
                                                            .height(if (milkInHeight < 2.dp) 2.dp else milkInHeight)
                                                            .background(
                                                                Color(0xFF10B981), // Green color
                                                                RoundedCornerShape(
                                                                    topStart = 4.dp,
                                                                    topEnd = 4.dp
                                                                )
                                                            )
                                                    )

                                                    // Milk Out Bar (Blue) - Make sure it's visible
                                                    Box(
                                                        modifier = Modifier
                                                            .width(if (selectedPeriod == "Last 14 Days") 8.dp else 12.dp)
                                                            .height(if (milkOutHeight < 2.dp) 2.dp else milkOutHeight)
                                                            .background(
                                                                Color(0xFF60A5FA), // Blue color
                                                                RoundedCornerShape(
                                                                    topStart = 4.dp,
                                                                    topEnd = 4.dp
                                                                )
                                                            )
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(8.dp))

                                                // Day label (show day of month or abbreviated day name)
                                                Text(
                                                    text = if (selectedPeriod.contains("Month"))
                                                        "${date.dayOfMonth}"
                                                    else
                                                        when (date.dayOfWeek) {
                                                            kotlinx.datetime.DayOfWeek.MONDAY -> "Mon"
                                                            kotlinx.datetime.DayOfWeek.TUESDAY -> "Tue"
                                                            kotlinx.datetime.DayOfWeek.WEDNESDAY -> "Wed"
                                                            kotlinx.datetime.DayOfWeek.THURSDAY -> "Thu"
                                                            kotlinx.datetime.DayOfWeek.FRIDAY -> "Fri"
                                                            kotlinx.datetime.DayOfWeek.SATURDAY -> "Sat"
                                                            kotlinx.datetime.DayOfWeek.SUNDAY -> "Sun"
                                                            else -> "---"
                                                        },
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontWeight = FontWeight.Medium
                                                )

                                                // Day number (not needed if in month view since the day is already shown)
                                                if (!selectedPeriod.contains("Month")) {
                                                    Text(
                                                        text = "${date.dayOfMonth}",
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                        fontWeight = FontWeight.Normal
                                                    )
                                                    
                                                    // Add a bit of extra space before the milk values
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                }

                                                // Values below day label
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text(
                                                        text = "${dailyMilkIn.toInt()}L",
                                                        fontSize = 10.sp,
                                                        color = Color(0xFF10B981), // Green for milk in values
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "${dailyMilkOut.toInt()}L",
                                                        fontSize = 10.sp,
                                                        color = Color(0xFF60A5FA), // Blue for milk out values
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Right Column - Cows Card
                    Column(modifier = Modifier.width(300.dp)) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(740.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                // Header Section
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column {
                                        Text(
                                            text = "Cows",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF374151)
                                        )
                                        Text(
                                            text = cows.size.toString(),
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF111827)
                                        )
                                    }

                                    Button(
                                        onClick = { showAddCowDialog = true },
                                        modifier = Modifier
                                            .height(40.dp)
                                            .widthIn(min = 100.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.White,
                                            contentColor = Color(0xFF374151)
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            Color(0xFFD1D5DB)
                                        ),
                                        shape = RoundedCornerShape(4.dp),
                                        contentPadding = PaddingValues(
                                            horizontal = 16.dp,
                                            vertical = 8.dp
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Add Cow",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // List Header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Cows list",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF111827)
                                    )

                                    TextButton(
                                        onClick = { /* Handle view all */ },
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(
                                            text = "View all",
                                            fontSize = 14.sp,
                                            color = Color(0xFF6B7280)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Table Header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    SortableHeader(
                                        text = "S.no.",
                                        modifier = Modifier.width(60.dp)
                                    )

                                    SortableHeader(
                                        text = "Cow Name",
                                        modifier = Modifier.weight(1f)
                                    )

                                    SortableHeader(
                                        text = "Health",
                                        modifier = Modifier.width(80.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Cows List
                                LazyColumn(
                                    verticalArrangement = spacedBy(12.dp),
                                    modifier = Modifier.heightIn(max = 600.dp)
                                ) {
                                    items(cows) { cow ->
                                        CowListItem(
                                            serialNumber = "${cows.indexOf(cow) + 1}".padStart(
                                                2,
                                                '0'
                                            ),
                                            cowName = cow.name,
                                            healthStatus = cow.status.healthStatus
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun TableHeaderCell(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(8.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF6B7280)
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "Sort",
            tint = Color(0xFF9CA3AF),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun SalesTableRow(
    id: String,
    name: String,
    date: String,
    quantity: Int,
    totalAmount: String,
    payMode: PaymentMode
) {
    var isHovered by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isHovered) Color(0xFFF9FAFB) else Color.Transparent)
            .clickable { isHovered = !isHovered }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // S.no
        Text(
            text = id,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1F2937),
            modifier = Modifier.weight(0.8f)
        )

        // Name
        Text(
            text = name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1F2937),
            modifier = Modifier.weight(1.2f)
        )

        // Date
        Text(
            text = date,
            fontSize = 14.sp,
            color = Color(0xFF6B7280),
            modifier = Modifier.weight(1.5f)
        )

        // Quantity
        Text(
            text = quantity.toString(),
            fontSize = 14.sp,
            color = Color(0xFF1F2937),
            modifier = Modifier.weight(1.2f)
        )

        // Total Amount
        Text(
            text = totalAmount,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1F2937),
            modifier = Modifier.weight(1.3f)
        )

        // Pay Mode Badge
        PaymentBadge(
            paymentMode = payMode,
            modifier = Modifier.weight(1.2f)
        )
    }
}

@Composable
fun PaymentBadge(
    paymentMode: PaymentMode,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, text) = when (paymentMode) {
        PaymentMode.CASH -> Triple(
            Color(0xFFDCFCE7), // green-100
            Color(0xFF059669), // green-600
            "Cash"
        )

        PaymentMode.MPESA -> Triple(
            Color(0xFFFED7AA), // orange-100
            Color(0xFFEA580C), // orange-600
            "Mpesa"
        )
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterStart
    ) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = backgroundColor,
            modifier = Modifier.wrapContentSize()
        ) {
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun SortableHeader(
    text: String,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    if (onClick != null) {
        TextButton(
            onClick = onClick,
            modifier = modifier,
            contentPadding = PaddingValues(0.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6B7280)
                )
                Icon(
                    imageVector = if (isSelected) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Sort",
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFF9CA3AF)
                )
            }
        }
    } else {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6B7280)
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Sort",
                modifier = Modifier.size(16.dp),
                tint = Color(0xFF9CA3AF)
            )
        }
    }
}

@Composable
fun CowListItem(
    serialNumber: String,
    cowName: String,
    healthStatus: HealthStatus
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Serial Number
        Text(
            text = serialNumber,
            fontSize = 14.sp,
            color = Color(0xFF374151),
            modifier = Modifier.width(60.dp)
        )

        // Cow Name with image
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Cow image placeholder (circular)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        getCowColor(serialNumber),
                        CircleShape
                    )
            )

            Text(
                text = cowName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF374151)
            )
        }

        // Health Status
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = when (healthStatus) {
                HealthStatus.HEALTHY -> Color(0xFFDCFCE7) // Green background
                HealthStatus.GESTATION -> Color(0xFFE0F2FE) // Blue background for gestation
                else -> Color(0xFFFED7AA) // Amber background for other states
            },
            modifier = Modifier.width(80.dp)
        ) {
            Text(
                text = when (healthStatus) {
                    HealthStatus.HEALTHY -> "Healthy"
                    HealthStatus.GESTATION -> "Gestation"
                    HealthStatus.SICK -> "Sick"
                    HealthStatus.UNDER_TREATMENT -> "Treatment"
                    HealthStatus.NEEDS_ATTENTION -> "Attention"
                    HealthStatus.VACCINATED -> "Vaccinated"
                    HealthStatus.ANTIBIOTICS -> "Antibiotics"
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = when (healthStatus) {
                    HealthStatus.HEALTHY -> Color(0xFF059669) // Green text
                    HealthStatus.GESTATION -> Color(0xFF0369A1) // Blue text
                    else -> Color(0xFFEA580C) // Amber text
                },
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

// Helper function to generate different colors for cow placeholders
fun getCowColor(serialNumber: String): Color {
    val colors = listOf(
        Color(0xFF8B5CF6), // Purple
        Color(0xFF06B6D4), // Cyan
        Color(0xFF10B981), // Emerald
        Color(0xFFF59E0B), // Amber
        Color(0xFFEF4444), // Red
        Color(0xFF3B82F6), // Blue
        Color(0xFFEC4899), // Pink
        Color(0xFF84CC16), // Lime
        Color(0xFF6366F1), // Indigo
        Color(0xFF14B8A6), // Teal
        Color(0xFFE11D48), // Rose
        Color(0xFF8B5CF6), // Purple
        Color(0xFF059669)  // Green
    )

    val index = serialNumber.toIntOrNull()?.minus(1) ?: 0
    return colors.getOrElse(index) { colors.first() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCowDialog(
    members: List<Member>,
    onDismiss: () -> Unit,
    onAddCow: (name: String, breed: CowBreed, age: Int, weight: Double, ownerId: String, healthStatus: HealthStatus) -> Unit
) {
        var name by remember { mutableStateOf("") }
        var selectedBreed by remember { mutableStateOf(CowBreed.HOLSTEIN) }
        var age by remember { mutableStateOf("") }
        var weight by remember { mutableStateOf("") }
        var selectedHealthStatus by remember { mutableStateOf(HealthStatus.HEALTHY) }
        var selectedOwner by remember { mutableStateOf<Member?>(null) }
        var showBreedDropdown by remember { mutableStateOf(false) }
        var showHealthStatusDropdown by remember { mutableStateOf(false) }
        var showOwnerDropdown by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(4.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Add New cows",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        ExposedDropdownMenuBox(
                            expanded = showBreedDropdown,
                            onExpandedChange = { showBreedDropdown = !showBreedDropdown },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextField(
                                value = selectedBreed.displayName,
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Breed") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showBreedDropdown) },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = showBreedDropdown,
                                onDismissRequest = { showBreedDropdown = false }
                            ) {
                                CowBreed.values().forEach { breedValue ->
                                    DropdownMenuItem(
                                        text = { Text(breedValue.displayName) },
                                        onClick = {
                                            selectedBreed = breedValue
                                            showBreedDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = age,
                            onValueChange = { age = it.filter { it.isDigit() } },
                            label = { Text("Age (years)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = weight,
                            onValueChange = { weight = it.filter { it.isDigit() || it == '.' } },
                            label = { Text("Weight (kg)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        ExposedDropdownMenuBox(
                            expanded = showOwnerDropdown,
                            onExpandedChange = { showOwnerDropdown = !showOwnerDropdown },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextField(
                                value = selectedOwner?.name ?: "Select Owner",
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Owner") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showOwnerDropdown) },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = showOwnerDropdown,
                                onDismissRequest = { showOwnerDropdown = false }
                            ) {
                                members.forEach { member ->
                                    DropdownMenuItem(
                                        text = { Text(member.name) },
                                        onClick = {
                                            selectedOwner = member
                                            showOwnerDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        ExposedDropdownMenuBox(
                            expanded = showHealthStatusDropdown,
                            onExpandedChange = {
                                showHealthStatusDropdown = !showHealthStatusDropdown
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextField(
                                value = selectedHealthStatus.name,
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Health Status") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showHealthStatusDropdown) },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = showHealthStatusDropdown,
                                onDismissRequest = { showHealthStatusDropdown = false }
                            ) {
                                HealthStatus.values().forEach { status ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                when (status) {
                                                    HealthStatus.SICK -> "Sick"
                                                    HealthStatus.HEALTHY -> "Healthy"
                                                    HealthStatus.NEEDS_ATTENTION -> "Needs Attention"
                                                    HealthStatus.UNDER_TREATMENT -> "Under Treatment"
                                                    HealthStatus.GESTATION -> "Gestation"
                                                    HealthStatus.VACCINATED -> "Vaccinated"
                                                    HealthStatus.ANTIBIOTICS -> "Antibiotics Treatment"
                                                }
                                            )
                                        },
                                        onClick = {
                                            selectedHealthStatus = status
                                            showHealthStatusDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { onDismiss() }) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val ageInt = age.toIntOrNull()

                                    if (ageInt != null && ageInt > 0 &&
                                        selectedOwner != null
                                    ) {
                                        onAddCow(
                                            name,
                                            selectedBreed,
                                            ageInt,
                                            weight.toDoubleOrNull() ?: 0.0,
                                            selectedOwner!!.memberId ?: "",
                                            selectedHealthStatus
                                        )
                                    }
                                },
                                enabled = name.isNotBlank() &&
                                        age.isNotBlank() && age.toIntOrNull()
                                    ?.let { it > 0 } == true &&
                                        weight.isNotBlank() && weight.toDoubleOrNull()
                                    ?.let { it > 0 } == true &&
                                        selectedOwner != null,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("Save")
                            }
                        }
                    }
                }
            }
        }
    }

