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
package cnc.coop.milkcreamies.presentation.ui.screens.milkout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cnc.coop.milkcreamies.models.MilkSpoiltEntry
import cnc.coop.milkcreamies.models.SpoilageCause
import cnc.coop.milkcreamies.presentation.ui.common.components.TopHeader
import cnc.coop.milkcreamies.presentation.viewmodel.stock.StockViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

// Fixed price per liter for milk
const val MILK_PRICE_PER_LITER = 100

// Tab items
enum class MilkTab {
    OUT, SPOILT
}

@Serializable
data class MilkSpoilageRequest(
    val amountSpoilt: Double,
    val date: LocalDate,
    val lossAmount: Double,
    val cause: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilkOutTabsScreen(onBackClick: () -> Unit = {}) {
    var selectedTab by remember { mutableStateOf(MilkTab.SPOILT) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Milk Management") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Tab(
                    selected = selectedTab == MilkTab.OUT,
                    onClick = { selectedTab = MilkTab.OUT },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Output, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Milk Out")
                        }
                    }
                )

                Tab(
                    selected = selectedTab == MilkTab.SPOILT,
                    onClick = { selectedTab = MilkTab.SPOILT },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Spoilt Milk")
                        }
                    }
                )
            }

            // Content based on selected tab
            when (selectedTab) {
                MilkTab.OUT -> {
                    Text(
                        text = "Milk Out Screen - To be implemented",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                MilkTab.SPOILT -> MilkSpoiltScreen()
            }
        }
    }
}

@Composable
fun MilkSpoiltScreen(viewModel: StockViewModel = koinInject()) {
    val monthlyData by viewModel.monthlyData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var showAddSpoiltDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopHeader(
            onAddSale = { showAddSpoiltDialog = true },
            currentTitle = "Spoilt Milk Management",
            subTitle = "Track and report spoilt milk entries",
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
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 14.sp
                        )
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
                        text = "Loading spoilt milk data...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Monthly Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Monthly Spoilage Summary",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Total Spoilt: ${monthlyData.totalSpoilt.toInt()}L",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "Impact: ${((monthlyData.totalSpoilt / monthlyData.totalProduced) * 100).toInt()}% of production",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Add Spoilt Entry Button
            Button(
                onClick = { showAddSpoiltDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Report Spoilt Milk")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Placeholder for spoilt entries list
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Recent Spoilt Entries",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Spoilt entries list will be displayed here",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Add Spoilt Milk Dialog
        if (showAddSpoiltDialog) {
            AddSpoiltMilkDialog(
                onDismiss = { showAddSpoiltDialog = false },
                onAddSpoiltEntry = { entry ->
                    viewModel.onMilkSpoiled(entry)
                    showAddSpoiltDialog = false
                },
                currentStock = viewModel.stockSummary.value.currentStock
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSpoiltMilkDialog(
    onDismiss: () -> Unit,
    onAddSpoiltEntry: (MilkSpoiltEntry) -> Unit,
    currentStock: Double = 0.0 // Add parameter for current stock
) {
    var amountSpoilt by remember { mutableStateOf("") }
    var lossAmount by remember { mutableStateOf("") }
    var selectedCause by remember { mutableStateOf<SpoilageCause?>(null) }
    var showCauseDropdown by remember { mutableStateOf(false) }
    var dateString by remember {
        mutableStateOf(
            Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
        )
    }

    var amountError by remember { mutableStateOf(false) }
    var lossAmountError by remember { mutableStateOf(false) }
    var stockError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Report Spoilt Milk",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "Available Stock: ${currentStock.toInt()}L",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (currentStock <= 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Show warning if no stock available
                if (currentStock <= 0) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
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
                                text = "No milk stock available to spoil",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountSpoilt,
                    onValueChange = {
                        amountSpoilt = it.filter { char -> char.isDigit() || char == '.' }
                        amountError = false
                        stockError = false
                        // Auto-calculate loss amount based on fixed price per liter
                        val amount = amountSpoilt.toDoubleOrNull()
                        if (amount != null) {
                            lossAmount = (amount * MILK_PRICE_PER_LITER).toString()
                            lossAmountError = false
                        }
                    },
                    label = { Text("Amount Spoilt (L)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = amountError || stockError,
                    enabled = currentStock > 0, // Disable if no stock
                    supportingText = when {
                        stockError -> {
                            {
                                Text(
                                    "Amount exceeds available stock (${currentStock.toInt()}L)",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        amountError -> {
                            {
                                Text(
                                    "Please enter a valid amount",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        else -> {
                            {
                                Text(
                                    "Maximum available: ${currentStock.toInt()}L",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                )

                OutlinedTextField(
                    value = lossAmount,
                    onValueChange = { /* Readonly, calculated automatically */ },
                    label = { Text("Loss Amount (KES) - Fixed at $MILK_PRICE_PER_LITER KES/L") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = lossAmountError,
                    readOnly = true,
                    enabled = false,
                    supportingText = if (lossAmountError) {
                        {
                            Text(
                                "Please enter a valid amount",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    } else null,
                    leadingIcon = {
                        Icon(
                            Icons.Default.Money,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )

                ExposedDropdownMenuBox(
                    expanded = showCauseDropdown,
                    onExpandedChange = { showCauseDropdown = !showCauseDropdown },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCause?.name?.replace("_", " ")?.lowercase()
                            ?.replaceFirstChar { it.uppercase() } ?: "Select cause (optional)",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Cause of Spoilage") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCauseDropdown) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        enabled = currentStock > 0, // Disable if no stock
                        leadingIcon = {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = showCauseDropdown,
                        onDismissRequest = { showCauseDropdown = false }
                    ) {
                        SpoilageCause.values().forEach { cause ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        cause.name.replace("_", " ").lowercase()
                                            .replaceFirstChar { it.uppercase() })
                                },
                                onClick = {
                                    selectedCause = cause
                                    showCauseDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountSpoilt.toDoubleOrNull()
                    val loss = lossAmount.toDoubleOrNull()

                    when {
                        currentStock <= 0 -> {
                            // Should not happen as button should be disabled, but extra safety
                            return@Button
                        }
                        amount == null || amount <= 0 -> amountError = true
                        amount > currentStock -> stockError = true
                        loss == null || loss <= 0 -> lossAmountError = true
                        else -> {
                            val spoiltEntry = MilkSpoiltEntry(
                                date = Clock.System.todayIn(TimeZone.currentSystemDefault()),
                                amountSpoilt = amount,
                                lossAmount = amount * MILK_PRICE_PER_LITER,
                                cause = selectedCause
                            )
                            onAddSpoiltEntry(spoiltEntry)
                        }
                    }
                },
                enabled = currentStock > 0, // Disable button if no stock
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(if (currentStock > 0) "Report" else "No Stock Available")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
