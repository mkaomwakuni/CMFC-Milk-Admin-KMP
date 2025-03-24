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

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cnc.coop.milkcreamies.models.*
import cnc.coop.milkcreamies.presentation.viewmodel.cows.CowsViewModel
import kotlinx.datetime.LocalDate
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CowDetailsScreen(
    cowId: String,
    onNavigateBack: () -> Unit,
    viewModel: CowsViewModel = koinInject()
) {
    val cow by viewModel.selectedCow.collectAsState()
    val cowMilkEntries by viewModel.cowMilkEntries.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedPeriod by remember { mutableStateOf(30) }

    LaunchedEffect(cowId) {
        viewModel.loadCowDetails(cowId)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Header with back navigation
        TopAppBar(
            title = {
                Text(
                    text = cow?.name ?: "Cow Details",
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Cow Overview Card
                item {
                    cow?.let { cowData ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Pets,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = cowData.name,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${cowData.breed} • ${cowData.age} years old",
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    CowInfoItem(
                                        title = "Weight",
                                        value = "${cowData.weight.toInt()}kg",
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    CowInfoItem(
                                        title = "Health",
                                        value = cowData.status.healthStatus.name.lowercase()
                                            .replaceFirstChar { it.uppercase() },
                                        color = if (cowData.status.healthStatus == HealthStatus.HEALTHY)
                                            MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                                    )
                                    CowInfoItem(
                                        title = "Status",
                                        value = if (cowData.isActive) "Active" else "Archived",
                                        color = if (cowData.isActive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Period Selection
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Milk Production Analysis",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                FilterChip(
                                    onClick = { selectedPeriod = 7 },
                                    label = { Text("7 Days") },
                                    selected = selectedPeriod == 7,
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    onClick = { selectedPeriod = 30 },
                                    label = { Text("30 Days") },
                                    selected = selectedPeriod == 30,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Production Stats
                item {
                    val filteredEntries = cowMilkEntries.takeLast(selectedPeriod * 2)
                    val totalProduction = filteredEntries.sumOf { it.liters }
                    val averageDaily = if (filteredEntries.isNotEmpty()) {
                        val dailyTotals =
                            filteredEntries.groupBy { it.date }.mapValues { (_, entries) ->
                                entries.sumOf { it.liters }
                            }
                        if (dailyTotals.isNotEmpty()) dailyTotals.values.average() else 0.0
                    } else 0.0

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Production Summary (Last $selectedPeriod days)",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                CowInfoItem(
                                    title = "Total Production",
                                    value = "${totalProduction.toInt()}L",
                                    color = MaterialTheme.colorScheme.primary
                                )
                                CowInfoItem(
                                    title = "Daily Average",
                                    value = "${averageDaily.toInt()}L",
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                CowInfoItem(
                                    title = "Total Entries",
                                    value = filteredEntries.size.toString(),
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }

                // Recent Milk Entries
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Recent Milk Entries",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            val recentEntries = cowMilkEntries.takeLast(20)

                            if (recentEntries.isEmpty()) {
                                Text(
                                    text = "No milk entries found",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )
                            } else {
                                Column {
                                    recentEntries.forEach { entry ->
                                        CowMilkEntryItem(entry = entry)
                                        if (entry != recentEntries.last()) {
                                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CowInfoItem(
    title: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = title,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CowMilkEntryItem(
    entry: MilkInEntry
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CalendarToday,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = entry.date.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = entry.milkingType.name.lowercase().replaceFirstChar { it.uppercase() },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            text = "${entry.liters}L",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
