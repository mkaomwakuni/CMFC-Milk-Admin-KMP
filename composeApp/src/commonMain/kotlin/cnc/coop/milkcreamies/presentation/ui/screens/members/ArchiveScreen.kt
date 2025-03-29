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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cnc.coop.milkcreamies.models.Member
import cnc.coop.milkcreamies.models.Cow
import cnc.coop.milkcreamies.presentation.ui.common.components.CowCard
import cnc.coop.milkcreamies.presentation.viewmodel.members.MembersViewModel
import cnc.coop.milkcreamies.presentation.viewmodel.cows.CowsViewModel
import org.koin.compose.koinInject

enum class ArchiveFilter {
    ALL, MEMBERS, COWS
}

enum class ArchiveReason {
    ALL, SOLD, DECEASED, SUSPENDED, LEFT_COOPERATIVE, RELOCATED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    onNavigateBack: () -> Unit,
    membersViewModel: MembersViewModel = koinInject(),
    cowsViewModel: CowsViewModel = koinInject()
) {
    val members by membersViewModel.members.collectAsState()
    val cows by cowsViewModel.cows.collectAsState()

    var selectedFilter by remember { mutableStateOf(ArchiveFilter.ALL) }
    var selectedReason by remember { mutableStateOf(ArchiveReason.ALL) }
    var showFilterDialog by remember { mutableStateOf(false) }

    // Filter archived items based on selected filters
    val archivedMembers = members.filter { !it.isActive }
    val archivedCows = cows.filter { !it.isActive }

    val filteredMembers = when (selectedReason) {
        ArchiveReason.SUSPENDED -> archivedMembers.filter {
            it.archiveReason?.contains("suspended", true) == true
        }

        ArchiveReason.LEFT_COOPERATIVE -> archivedMembers.filter {
            it.archiveReason?.contains("left", true) == true
        }

        ArchiveReason.RELOCATED -> archivedMembers.filter {
            it.archiveReason?.contains("relocated", true) == true
        }

        else -> archivedMembers
    }

    val filteredCows = when (selectedReason) {
        ArchiveReason.SOLD -> archivedCows.filter {
            it.archiveReason?.contains("sold", true) == true
        }

        ArchiveReason.DECEASED -> archivedCows.filter {
            it.archiveReason?.contains("deceased", true) == true
        }

        else -> archivedCows
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Archive,
                            contentDescription = "Archive",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Archive", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Summary Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.People,
                            contentDescription = "Members",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${archivedMembers.size}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Archived Members",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Pets,
                            contentDescription = "Cows",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "${archivedCows.size}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "Archived Cows",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    onClick = { selectedFilter = ArchiveFilter.ALL },
                    label = { Text("All") },
                    selected = selectedFilter == ArchiveFilter.ALL
                )
                FilterChip(
                    onClick = { selectedFilter = ArchiveFilter.MEMBERS },
                    label = { Text("Members") },
                    selected = selectedFilter == ArchiveFilter.MEMBERS
                )
                FilterChip(
                    onClick = { selectedFilter = ArchiveFilter.COWS },
                    label = { Text("Cows") },
                    selected = selectedFilter == ArchiveFilter.COWS
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Show members if filter allows
                if (selectedFilter == ArchiveFilter.ALL || selectedFilter == ArchiveFilter.MEMBERS) {
                    if (filteredMembers.isNotEmpty()) {
                        item {
                            Text(
                                text = "Archived Members (${filteredMembers.size})",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        items(filteredMembers) { member ->
                            ArchivedMemberCard(member = member)
                        }

                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }

                // Show cows if filter allows
                if (selectedFilter == ArchiveFilter.ALL || selectedFilter == ArchiveFilter.COWS) {
                    if (filteredCows.isNotEmpty()) {
                        item {
                            Text(
                                text = "Archived Cows (${filteredCows.size})",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        items(filteredCows) { cow ->
                            CowCard(cow = cow)
                        }
                    }
                }

                // Empty state
                if ((selectedFilter == ArchiveFilter.MEMBERS && filteredMembers.isEmpty()) ||
                    (selectedFilter == ArchiveFilter.COWS && filteredCows.isEmpty()) ||
                    (selectedFilter == ArchiveFilter.ALL && filteredMembers.isEmpty() && filteredCows.isEmpty())
                ) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Archive,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No archived items found",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Archived members and cows will appear here",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // Filter Dialog
    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text("Filter by Reason") },
            text = {
                Column {
                    Text("Select archive reason to filter:")
                    Spacer(modifier = Modifier.height(8.dp))

                    ArchiveReason.values().forEach { reason ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = selectedReason == reason,
                                onClick = { selectedReason = reason }
                            )
                            Text(
                                text = when (reason) {
                                    ArchiveReason.ALL -> "All Reasons"
                                    ArchiveReason.SOLD -> "Sold"
                                    ArchiveReason.DECEASED -> "Deceased"
                                    ArchiveReason.SUSPENDED -> "Suspended"
                                    ArchiveReason.LEFT_COOPERATIVE -> "Left Cooperative"
                                    ArchiveReason.RELOCATED -> "Relocated"
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFilterDialog = false }) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFilterDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ArchivedMemberCard(member: Member) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar placeholder
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.outline,
                        RoundedCornerShape(4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.name.firstOrNull()?.uppercase() ?: "?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "ID: ${member.memberId}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                member.archiveDate?.let { date ->
                    Text(
                        text = "Archived: $date",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            // Archive reason chip
            member.archiveReason?.let { reason ->
                val (chipColor, chipTextColor) = when (reason.lowercase()) {
                    "suspended" -> Pair(
                        MaterialTheme.colorScheme.errorContainer,
                        MaterialTheme.colorScheme.onErrorContainer
                    )

                    "left cooperative" -> Pair(
                        MaterialTheme.colorScheme.secondaryContainer,
                        MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    "relocated" -> Pair(
                        MaterialTheme.colorScheme.tertiaryContainer,
                        MaterialTheme.colorScheme.onTertiaryContainer
                    )

                    else -> Pair(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = reason,
                            fontSize = 12.sp
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = chipColor,
                        labelColor = chipTextColor
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Archive,
                            contentDescription = "Archived status",
                            modifier = Modifier.size(16.dp),
                            tint = chipTextColor
                        )
                    }
                )
            }
        }
    }
}