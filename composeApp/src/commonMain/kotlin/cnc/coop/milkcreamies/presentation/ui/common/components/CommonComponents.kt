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
package cnc.coop.milkcreamies.presentation.ui.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cnc.coop.milkcreamies.models.Cow
import cnc.coop.milkcreamies.models.ActionStatus
import cnc.coop.milkcreamies.models.HealthStatus

@Composable
fun CowCard(
    cow: Cow,
    onArchiveSold: (Cow) -> Unit = {},
    onArchiveDeceased: (Cow) -> Unit = {},
    onDelete: (String) -> Unit = {},
    onClick: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = cow.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(getHealthStatusColor(cow.status.healthStatus))
                        )
                    }
                    Text(
                        text = "ID: ${cow.cowId} • ${cow.breed}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                                            Icons.Default.MonetizationOn,
                                            contentDescription = "Sold",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Mark as Sold")
                                    }
                                },
                                onClick = {
                                    onArchiveSold(cow)
                                    showMenu = false
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.SentimentDissatisfied,
                                            contentDescription = "Deceased",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Mark as Deceased")
                                    }
                                },
                                onClick = {
                                    onArchiveDeceased(cow)
                                    showMenu = false
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Delete")
                                    }
                                },
                                onClick = {
                                    cow.cowId?.let { onDelete(it) }
                                    showMenu = false
                                  }
                            )
                    }
                }
            }
         }
     }

            if (cow.status.vaccinationDue != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3E0)
                    ),
                    modifier = Modifier.padding(top = 4.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Vaccination Due",
                        fontSize = 10.sp,
                        color = Color(0xFFE65100),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Display archived status if cow is not active
            if (cow.status.actionStatus != ActionStatus.ACTIVE) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when (cow.status.actionStatus) {
                            ActionStatus.SOLD -> Color(0xFFE0F2F1)
                            ActionStatus.DECEASED -> Color(0xFFFFEBEE)
                            else -> Color(0xFFF5F5F5)
                        }
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = cow.status.actionStatus.name,
                        fontSize = 10.sp,
                        color = when (cow.status.actionStatus) {
                            ActionStatus.SOLD -> Color(0xFF00897B)
                            ActionStatus.DECEASED -> Color(0xFFD32F2F)
                            else -> Color.Gray
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem1("Age", "${cow.age} years")
                InfoItem1("Weight", "${cow.weight} kg")
                InfoItem1("Last Checkup", cow.status.vaccinationLast?.toString() ?: "N/A")
                InfoItem1("Status", cow.status.healthStatus.name)
            }
    }
}

@Composable
fun MilkAnalyticsView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Milk Analytics View",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    }
}

@Composable
fun InfoItem1(label: String, value: String) {
    Column {
        Text(
            text = label.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}


// Helper function to get color based on health status
fun getHealthStatusColor(healthStatus: HealthStatus): Color {
    return when (healthStatus) {
        HealthStatus.SICK -> Color.Gray
        HealthStatus.HEALTHY -> Color(0xFF4CAF50)           // Green
        HealthStatus.NEEDS_ATTENTION -> Color(0xFFFF9800)   // Orange
        HealthStatus.UNDER_TREATMENT -> Color(0xFFF44336)   // Red
        HealthStatus.GESTATION -> Color(0xFF9C27B0)         // Purple
        HealthStatus.VACCINATED -> Color(0xFF2196F3)        // Blue
        HealthStatus.ANTIBIOTICS -> Color(0xFFE91E63)       // Pink
    }
}
