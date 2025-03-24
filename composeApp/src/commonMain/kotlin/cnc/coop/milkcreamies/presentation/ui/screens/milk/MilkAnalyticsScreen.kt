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
package cnc.coop.milkcreamies.presentation.ui.screens.milk

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cnc.coop.milkcreamies.presentation.ui.common.components.MilkAnalyticsView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilkAnalyticsScreen() {
    var selectedView by remember { mutableStateOf("production") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    onClick = { selectedView = "production" },
                    label = { Text("Daily Production") },
                    selected = selectedView == "production",
                    leadingIcon = if (selectedView == "production") {
                        { Icon(Icons.Default.Check, contentDescription = "Selected") }
                    } else null
                )
                FilterChip(
                    onClick = { selectedView = "charts" },
                    label = { Text("Charts & Insights") },
                    selected = selectedView == "charts",
                    leadingIcon = if (selectedView == "charts") {
                        { Icon(Icons.Default.Check, contentDescription = "Selected") }
                    } else null
                )
            }
        }

        when (selectedView) {
            "production" -> item { MilkAnalyticsView() }
            "charts" -> {
                }
            }
        }
    }
