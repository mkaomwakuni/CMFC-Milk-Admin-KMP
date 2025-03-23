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
package cnc.coop.milkcreamies.presentation.ui.screens.cows

import cnc.coop.milkcreamies.models.ActionStatus
import cnc.coop.milkcreamies.models.Cow
import cnc.coop.milkcreamies.models.HealthStatus
import cnc.coop.milkcreamies.models.PaymentMode
import cnc.coop.milkcreamies.models.StatsCardData
import cnc.coop.milkcreamies.presentation.ui.common.components.CowCard
import cnc.coop.milkcreamies.presentation.ui.screens.dashboard.AddCowDialog
import cnc.coop.milkcreamies.presentation.ui.screens.milk.AddMilkEntryDialog
import cnc.coop.milkcreamies.presentation.ui.screens.milk.AddSaleDialog
import cnc.coop.milkcreamies.presentation.ui.screens.stock.StatsCardsGrid
import cnc.coop.milkcreamies.presentation.ui.screens.cows.CowDetailsScreen
import cnc.coop.milkcreamies.presentation.ui.common.components.TopHeader
import cnc.coop.milkcreamies.presentation.viewmodel.cows.CowsViewModel
import cnc.coop.milkcreamies.presentation.viewmodel.dashboard.DashboardViewModel
import cnc.coop.milkcreamies.presentation.viewmodel.milk.MilkInViewModel
import cnc.coop.milkcreamies.presentation.viewmodel.milk.MilkOutViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.Output
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.koin.compose.koinInject

@Composable
fun CowsScreen(
    viewModel: CowsViewModel = koinInject(),
    dashboard: DashboardViewModel = koinInject(),
    milkInViewModel: MilkInViewModel = koinInject(),
    milkOutViewModel: MilkOutViewModel = koinInject()
) {
    val cows by viewModel.cows.collectAsState()
    val filteredCows by viewModel.filteredCows.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val members by dashboard.members.collectAsState()
    val showActiveOnly by viewModel.showActiveOnly.collectAsState()
    val currentStock by milkOutViewModel.currentStock.collectAsState()

    var showAddCow by remember { mutableStateOf(false) }
    var showCowDetails by remember { mutableStateOf(false) }
    var selectedCowId by remember { mutableStateOf<String?>(null) }
    var showMilkInDialog by remember { mutableStateOf(false) }
    var showMilkOutDialog by remember { mutableStateOf(false) }
    var showSpoiltMilkDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error message if any
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(message = it)
        }
    }

    if (showAddCow) {
        AddCowDialog(
            members = members,
            onDismiss = { showAddCow = false },
            onAddCow = { name, breed, age, weight, ownerId, healthStatus ->
                dashboard.addCow(
                    name = name,
                    breed = breed.displayName,
                    age = age,
                    weight = weight,
                    entryDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
                    ownerId = ownerId,
                    healthStatus = healthStatus
                )
                showAddCow = false
            }
        )
    }

    // Milk In Dialog
    if (showMilkInDialog) {
        AddMilkEntryDialog(
            onDismiss = { showMilkInDialog = false },
            onSave = { milkEntry ->
                milkInViewModel.addMilkInEntry(
                    cowId = milkEntry.cowId,
                    ownerId = milkEntry.ownerId,
                    quantityLiters = milkEntry.liters,
                    date = milkEntry.date,
                    milkingType = milkEntry.milkingType
                )
                showMilkInDialog = false
            },
            viewModel = milkInViewModel
        )
    }

    // Milk Out Dialog
    if (showMilkOutDialog) {
        AddSaleDialog(
            viewModel = milkOutViewModel,
            availableStock = currentStock.currentStock,
            onDismiss = { showMilkOutDialog = false },
            onAddSale = { customerName, quantity, pricePerLiter, paymentMode ->
                milkOutViewModel.addMilkOutEntry(
                    customerName = customerName,
                    quantitySold = quantity,
                    pricePerLiter = pricePerLiter,
                    paymentMode = paymentMode,
                    date = Clock.System.todayIn(TimeZone.currentSystemDefault())
                )
                showMilkOutDialog = false
            }
        )
    }

    // Spoilt Milk Dialog
    if (showSpoiltMilkDialog) {
        AlertDialog(
            onDismissRequest = { showSpoiltMilkDialog = false },
            title = { Text("Report Spoilt Milk") },
            text = {
                if (currentStock.currentStock <= 0) {
                    Text("No milk stock available. Cannot report spoilt milk.")
                } else {
                    Column {
                        Text("Enter the quantity of milk that has been spoilt.")
                        Text(
                            "Available stock: ${currentStock.currentStock.toInt()} liters",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // logic to report spoilt milk
                        showSpoiltMilkDialog = false
                    },
                    enabled = currentStock.currentStock > 0
                ) {
                    Text("Report")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSpoiltMilkDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Show CowDetailsScreen as a separate screen, not nested in LazyColumn
    if (showCowDetails && selectedCowId != null) {
        CowDetailsScreen(
            cowId = selectedCowId!!,
            onNavigateBack = {
                showCowDetails = false
                selectedCowId = null
            }
        )
    } else {
        // Main cows list screen
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { viewModel.refreshCows() },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else if (cows.isEmpty() && errorMessage == null) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No cows found. Pull to refresh or add new cows.",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        TopHeader(
                            onAddSale = { showAddCow = true },
                            currentTitle = "Cows Overview",
                            subTitle = "Analytics",
                        )

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            item {
                                var searchQuery by remember { mutableStateOf("") }

                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = {
                                        searchQuery = it
                                        viewModel.updateSearchQuery(it)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("Search cows") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Search,
                                            contentDescription = null
                                        )
                                    }
                                )

                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Showing ${if (showActiveOnly) "active" else "all"} cows",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Show archived cows")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Switch(
                                            checked = !showActiveOnly,
                                            onCheckedChange = { checked ->
                                                viewModel.toggleActiveStatusFilter(!checked)
                                            }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            item {

                                val statsFilter by viewModel.statsFilter.collectAsState()

                                StatsCardsGrid(
                                    listOf(
                                        StatsCardData(
                                            "Total Cows",
                                            cows.size.toString(),
                                            Icons.Default.Groups,
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.primaryContainer,
                                            isSelected = statsFilter == "total",
                                            onClick = {
                                                if (statsFilter == "total") {
                                                    viewModel.clearFilters()
                                                } else {
                                                    viewModel.setStatsFilter("total")
                                                }
                                            }
                                        ),
                                        StatsCardData(
                                            "Healthy",
                                            cows.count { it.status.healthStatus == HealthStatus.HEALTHY }
                                                .toString(),
                                            Icons.Default.Favorite,
                                            MaterialTheme.colorScheme.tertiary,
                                            MaterialTheme.colorScheme.tertiaryContainer,
                                            isSelected = statsFilter == "healthy",
                                            onClick = {
                                                if (statsFilter == "healthy") {
                                                    viewModel.clearFilters()
                                                } else {
                                                    viewModel.setStatsFilter("healthy")
                                                }
                                            }
                                        ),
                                        StatsCardData(
                                            "Need Attention",
                                            cows.count { it.status.healthStatus != HealthStatus.HEALTHY }
                                                .toString(),
                                            Icons.Default.Warning,
                                            MaterialTheme.colorScheme.error,
                                            MaterialTheme.colorScheme.errorContainer,
                                            isSelected = statsFilter == "attention",
                                            onClick = {
                                                if (statsFilter == "attention") {
                                                    viewModel.clearFilters()
                                                } else {
                                                    viewModel.setStatsFilter("attention")
                                                }
                                            }
                                        )
                                    )
                                )
                            }

                            items(filteredCows) { cow ->
                                var showDeleteConfirmation by remember { mutableStateOf(false) }

                                if (showDeleteConfirmation) {
                                    AlertDialog(
                                        onDismissRequest = { showDeleteConfirmation = false },
                                        title = { Text("Confirm Deletion") },
                                        text = { Text("Are you sure you want to delete ${cow.name}? This action cannot be undone.") },
                                        confirmButton = {
                                            Button(
                                                onClick = {
                                                    cow.cowId?.let { viewModel.deleteCow(it) }
                                                    showDeleteConfirmation = false
                                                }
                                            ) {
                                                Text("Delete")
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(
                                                onClick = { showDeleteConfirmation = false }
                                            ) {
                                                Text("Cancel")
                                            }
                                        }
                                    )
                                }

                                CowCard(
                                    cow = cow,
                                    onArchiveSold = { viewModel.archiveCow(it, ActionStatus.SOLD) },
                                    onArchiveDeceased = {
                                        viewModel.archiveCow(
                                            it,
                                            ActionStatus.DECEASED
                                        )
                                    },
                                    onDelete = { showDeleteConfirmation = true },
                                    onClick = {
                                        selectedCowId = cow.cowId
                                        showCowDetails = true
                                    }
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            item {
                                // Add bottom padding
                                Spacer(modifier = Modifier.height(80.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
