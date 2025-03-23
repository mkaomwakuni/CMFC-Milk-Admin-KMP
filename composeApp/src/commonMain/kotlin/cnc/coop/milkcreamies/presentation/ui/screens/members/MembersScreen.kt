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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cnc.coop.milkcreamies.models.Member
import cnc.coop.milkcreamies.presentation.ui.common.components.TopHeader
import cnc.coop.milkcreamies.presentation.ui.screens.members.MemberStatsScreen
import cnc.coop.milkcreamies.presentation.viewmodel.members.MembersViewModel
import org.koin.compose.koinInject

@Composable
fun MembersScreen(viewModel: MembersViewModel = koinInject()) {
    val members by viewModel.members.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showAddMemberDialog by remember { mutableStateOf(false) }
    var showActiveOnly by remember { mutableStateOf(true) }
    var showMemberStats by remember { mutableStateOf(false) }
    var selectedMemberId by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }


    // Show MemberStatsScreen as a separate screen, not nested
    if (showMemberStats && selectedMemberId != null) {
        MemberStatsScreen(
            memberId = selectedMemberId!!,
            onNavigateBack = {
                showMemberStats = false
                selectedMemberId = null
            }
        )
    } else {
        // Main members list screen
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopHeader(
                onAddSale = { showAddMemberDialog = true },
                currentTitle = "Members Panel Overview",
                subTitle = "Manage Cooperative Add and Remove Members",
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(24.dp)
            ) {
                // Stats Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${members.count { it.isActive }}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Active Members",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${members.count { !it.isActive }}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "Archived Members",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${members.size}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                text = "Total Members",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Members List
                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "All Members",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Switch(
                                checked = showActiveOnly,
                                onCheckedChange = { showActiveOnly = it },
                                modifier = Modifier.padding(end = 16.dp),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }

                        if (isLoading) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        } else if (members.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No members found",
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Add your first member to get started",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            // Filter based on search results or active status
                            val displayMembers = if (searchQuery.isNotEmpty()) {
                                members.filter {
                                    it.name.contains(searchQuery, ignoreCase = true)
                                }
                            } else {
                                if (showActiveOnly) members.filter { it.isActive } else members
                            }

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(4),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(displayMembers) { member ->
                                    var showArchiveConfirmation by remember { mutableStateOf(false) }
                                    var selectedArchiveReason by remember { mutableStateOf("") }

                                    if (showArchiveConfirmation) {
                                        AlertDialog(
                                            onDismissRequest = { showArchiveConfirmation = false },
                                            title = { Text("Archive Member") },
                                            text = {
                                                Text("Are you sure you want to archive ${member.name} with reason: $selectedArchiveReason?")
                                            },
                                            confirmButton = {
                                                Button(
                                                    onClick = {
                                                        viewModel.archiveMember(
                                                            member,
                                                            selectedArchiveReason
                                                        )
                                                        showArchiveConfirmation = false
                                                    }
                                                ) {
                                                    Text("Archive")
                                                }
                                            },
                                            dismissButton = {
                                                TextButton(
                                                    onClick = { showArchiveConfirmation = false }
                                                ) {
                                                    Text("Cancel")
                                                }
                                            }
                                        )
                                    }

                                    MemberCard(
                                        member = member,
                                        onClick = {
                                            selectedMemberId = member.memberId
                                            showMemberStats = true
                                        },
                                        onArchive = { _, reason ->
                                            selectedArchiveReason = reason
                                            showArchiveConfirmation = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Error handling
            errorMessage?.let { error ->
                LaunchedEffect(error) {
                    // You can show a snackbar or toast here if needed
                }
            }

            // Add Member Dialog
            if (showAddMemberDialog) {
                AddMemberDialog(
                    onDismiss = { showAddMemberDialog = false },
                    onAddMember = { name ->
                        viewModel.addMember(name)
                        showAddMemberDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun MemberCard(
    member: Member,
    onClick: () -> Unit,
    onArchive: (Member, String) -> Unit = { _, _ -> }
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .size(250.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (member.isActive)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        if (member.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        RoundedCornerShape(4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.name.firstOrNull()?.uppercase() ?: "?",
                    color = if (member.isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = member.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (member.isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!member.isActive) {
                        Spacer(modifier = Modifier.width(8.dp))
                        val (chipColor, chipTextColor) = when (member.archiveReason?.lowercase()) {
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
                                    text = member.archiveReason ?: "Archived",
                                    fontSize = 10.sp
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
                            },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }
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

            // Archive menu for active members only
            if (member.isActive) {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Archive,
                                    contentDescription = "Left Cooperative",
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Member Left")
                            }
                        },
                        onClick = {
                            onArchive(member, "Left Cooperative")
                            showMenu = false
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Archive,
                                    contentDescription = "Suspended",
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Suspended")
                            }
                        },
                        onClick = {
                            onArchive(member, "Suspended")
                            showMenu = false
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Archive,
                                    contentDescription = "Relocated",
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Relocated")
                            }
                        },
                        onClick = {
                            onArchive(member, "Relocated")
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AddMemberDialog(
    onDismiss: () -> Unit,
    onAddMember: (String) -> Unit
) {
    var memberName by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add New Member",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Enter the member's name",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = memberName,
                    onValueChange = {
                        memberName = it
                        nameError = false
                    },
                    label = { Text("Member Name") },
                    isError = nameError,
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = if (nameError) {
                        {
                            Text(
                                "Name cannot be empty",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    } else null
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (memberName.isBlank()) {
                        nameError = true
                    } else {
                        onAddMember(memberName.trim())
                    }
                },
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("Add Member")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, shape = RoundedCornerShape(4.dp)) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(4.dp)
    )
}
