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
package cnc.coop.milkcreamies.presentation.ui.screens.members

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cnc.coop.milkcreamies.models.*
import cnc.coop.milkcreamies.presentation.ui.common.components.TopHeader
import cnc.coop.milkcreamies.presentation.viewmodel.members.MembersViewModel
import kotlinx.datetime.LocalDate
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberStatsScreen(
    memberId: String,
    onNavigateBack: () -> Unit,
    viewModel: MembersViewModel = koinInject()
) {
    val member by viewModel.selectedMember.collectAsState()
    val memberCows by viewModel.memberCows.collectAsState()
    val milkEntries by viewModel.memberMilkEntries.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedPeriod by remember { mutableStateOf(30) }

    LaunchedEffect(memberId) {
        viewModel.loadMemberStats(memberId)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Header with back navigation
        TopAppBar(
            title = {
                Text(
                    text = member?.name ?: "Member Stats",
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                // Member Overview Card
                item {
                    member?.let { memberData ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text(
                                    text = "Member Overview",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    StatItem(
                                        title = "Total Cows",
                                        value = memberCows.size.toString(),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    StatItem(
                                        title = "Active Cows",
                                        value = memberCows.count { it.isActive }.toString(),
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                    StatItem(
                                        title = "Avg Daily Production",
                                        value = "${
                                            calculateAverageProduction(
                                                milkEntries,
                                                selectedPeriod
                                            ).toInt()
                                        }L",
                                        color = MaterialTheme.colorScheme.secondary
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
                                text = "Analysis Period",
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

                // Member's Cows List
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Member's Cows",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            if (memberCows.isEmpty()) {
                                Text(
                                    text = "No cows found for this member",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )
                            } else {
                                Column {
                                    memberCows.forEach { cow ->
                                        CowStatCard(
                                            cow = cow,
                                            milkEntries = milkEntries.filter { it.cowId == cow.cowId },
                                            period = selectedPeriod,
                                            onClick = { /* Navigate to cow details */ }
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
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
                                text = "Recent Milk Entries (Last ${selectedPeriod} days)",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            val recentEntries = milkEntries.takeLast(20)

                            if (recentEntries.isEmpty()) {
                                Text(
                                    text = "No milk entries found",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )
                            } else {
                                Column {
                                    recentEntries.forEach { entry ->
                                        MilkEntryItem(
                                            entry = entry,
                                            cowName = memberCows.find { it.cowId == entry.cowId }?.name
                                                ?: "Unknown"
                                        )
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
fun StatItem(
    title: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 24.sp,
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
fun CowStatCard(
    cow: Cow,
    milkEntries: List<MilkInEntry>,
    period: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (cow.isActive)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Pets,
                contentDescription = null,
                tint = if (cow.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cow.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${cow.breed} • ${if (cow.isActive) "Active" else "Archived"}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${calculateCowProduction(milkEntries, period).toInt()}L",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Last $period days",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MilkEntryItem(
    entry: MilkInEntry,
    cowName: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = cowName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${entry.date} • ${
                    entry.milkingType.name.lowercase().replaceFirstChar { it.uppercase() }
                }",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = "${entry.liters}L",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

// Helper functions
private fun calculateAverageProduction(entries: List<MilkInEntry>, days: Int): Double {
    if (entries.isEmpty()) return 0.0
    val recentEntries = entries.takeLast(days * 2) // morning and evening
    val dailyTotals = recentEntries.groupBy { it.date }.mapValues { (_, dayEntries) ->
        dayEntries.sumOf { it.liters }
    }
    return if (dailyTotals.isNotEmpty()) dailyTotals.values.average() else 0.0
}

private fun calculateCowProduction(entries: List<MilkInEntry>, days: Int): Double {
    if (entries.isEmpty()) return 0.0
    return entries.takeLast(days * 2).sumOf { it.liters }
}
