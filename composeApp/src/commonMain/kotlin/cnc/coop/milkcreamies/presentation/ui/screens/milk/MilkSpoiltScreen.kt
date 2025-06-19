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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.singleWindowApplication
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

enum class MilkTab {
    OUT, SPOILT
}

@Composable
fun MilkOutTabsScreen() {
    var selectedTab by remember { mutableStateOf(MilkTab.OUT) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Milk Management") },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.onPrimary
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.onPrimary
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
                    // Display MilkOutScreen content
                    Text("Milk Out Screen - To be implemented")
                    // In real implementation, call MilkOutScreen() here
                }

                MilkTab.SPOILT -> MilkSpoiltScreen()
            }
        }
    }
}

@Serializable
data class MilkSpoilageRequest(
    val amountSpoilt: Double,
    val date: LocalDate,
    val lossAmount: Double,
    val cause: String? = null
)

@Composable
fun MilkSpoiltScreen() {
    val spoiltMilkData = remember {
        listOf(
            MilkSpoilageRequest(
                amountSpoilt = 25.5,
                date = LocalDate(2024, 6, 1),
                lossAmount = 892.50,
                cause = "Expired"
            ),
            MilkSpoilageRequest(
                amountSpoilt = 15.0,
                date = LocalDate(2024, 6, 2),
                lossAmount = 525.00,
                cause = "Contamination"
            ),
            MilkSpoilageRequest(
                amountSpoilt = 40.2,
                date = LocalDate(2024, 6, 3),
                lossAmount = 1407.00,
                cause = "Temperature Failure"
            ),
                amountSpoilt = 8.5,
                date = LocalDate(2024, 6, 3),
                lossAmount = 297.50,
                cause = "Sour Taste"
            )
        )
    }

    var searchTerm by remember { mutableStateOf("") }

    // Calculate summary statistics
    val totalSpoiltLiters = spoiltMilkData.sumOf { it.amountSpoilt }
    val totalIncidents = spoiltMilkData.size
    val totalLoss = spoiltMilkData.sumOf { it.lossAmount }

    // Filter spoilt milk based on search
    val filteredSpoilt = spoiltMilkData.filter { item ->
        val matchesSearch =
            item.cause?.contains(searchTerm, ignoreCase = true) ?: false ||
                    item.date.toString().contains(searchTerm, ignoreCase = true)

        matchesSearch
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Summary Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SummaryCard(
                title = "Total Spoilt",
                value = "${"%.1f".format(totalSpoiltLiters)}L",
                icon = Icons.Default.Warning,
                color = Color.Red,
                subtitle = "Milk spoilt"
            )

            SummaryCard(
                title = "Total Incidents",
                value = totalIncidents.toString(),
                icon = Icons.Default.List,
                color = Color(0xFFFFA726), // Orange
                subtitle = "Reported cases"
            )

            SummaryCard(
                title = "Total Loss",
                value = "KES ${"%,.2f".format(totalLoss)}",
                icon = Icons.Default.Money,
                color = Color(0xFFE53935), // Darker red
                subtitle = "Financial impact"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Search
                    OutlinedTextField(
                        value = searchTerm,
                        onValueChange = { searchTerm = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Search by cause or date") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        singleLine = true
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Spoilt Milk Records
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 2.dp
        ) {
            Column {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Spoilt Milk Records",
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Showing ${filteredSpoilt.size} of ${spoiltMilkData.size} incidents",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }

                // Records List
                if (filteredSpoilt.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "No records",
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No spoilt milk records found",
                            style = MaterialTheme.typography.subtitle1
                        )
                        Text(
                            text = "Try adjusting your search criteria.",
                            style = MaterialTheme.typography.caption,
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredSpoilt) { item ->
                            SpoiltMilkCard(item = item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SpoiltMilkCard(item: MilkSpoilageRequest) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 1.dp,
        backgroundColor = MaterialTheme.colors.surface
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
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Spoilt Milk",
                        tint = Color.Red,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Spoilage: ${item.date}",
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Cause badge
                item.cause?.let { cause ->
                    val (backgroundColor, textColor) = when (cause) {
                        "Expired" -> Color(0xFFFFF3E0) to Color(0xFFEF6C00)
                        "Contamination" -> Color(0xFFFCE4EC) to Color(0xFFC2185B)
                        "Temperature Failure" -> Color(0xFFF3E5F5) to Color(0xFF7B1FA2)
                        "Sour Taste" -> Color(0xFFFFFDE7) to Color(0xFFF9A825)
                        else -> Color(0xFFE0E0E0) to Color(0xFF424242)
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .background(backgroundColor, MaterialTheme.shapes.small)
                    ) {
                        Text(
                            text = cause,
                            color = textColor,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
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
                        style = MaterialTheme.typography.caption,
                        color = Color.Gray
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalDrink,
                            contentDescription = "Amount",
                            tint = Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${"%.1f".format(item.amountSpoilt)}L",
                            style = MaterialTheme.typography.body1,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Financial Loss",
                        style = MaterialTheme.typography.caption,
                        color = Color.Gray
                    )
                    Text(
                        text = "KES ${"%,.2f".format(item.lossAmount)}",
                        style = MaterialTheme.typography.body1,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    subtitle: String
) {
    Card(
        modifier = Modifier.weight(1f),
        backgroundColor = color.copy(alpha = 0.1f),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
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
                style = MaterialTheme.typography.subtitle2,
                color = MaterialTheme.colors.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.h6,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

// Main function for desktop preview
fun main() = singleWindowApplication(title = "CMFC Milk Admin") {
    MaterialTheme {
        MilkOutTabsScreen()
    }
}
