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

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cnc.coop.milkcreamies.models.DailyProduction
import kotlinx.datetime.LocalDate

/**
 * A component that renders Y-axis labels for charts
 */
@Composable
fun YAxisLabels(
    maxValue: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.height(280.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        val step = if (maxValue > 0) maxValue / 5 else 1
        for (i in 0..5) {
            val value = maxValue - (i * step)
            Text(
                text = "${value}L",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * A component that renders X-axis day labels for charts
 */
@Composable
fun XAxisDayLabels(
    data: List<DailyProduction>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        data.forEach { dailyData ->
            // Parse the date string to LocalDate for extraction
            val dateParts = dailyData.date.split("-")
            if (dateParts.size == 3) {
                val year = dateParts[0].toInt()
                val month = dateParts[1].toInt()
                val day = dateParts[2].toInt()
                
                // Extract dayOfWeek from the parsed date
                val localDate = LocalDate(year, month, day)
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = localDate.dayOfWeek.name.take(3), // First 3 chars of day name
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = day.toString(), // Day of month
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * A wrapper component that places the chart with axis labels
 */
@Composable
fun ChartWithLabels(
    data: List<DailyProduction>,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No data available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val maxQuantity = data.maxOfOrNull { it.quantity }?.toInt() ?: 0

    Box(modifier = modifier.padding(start = 8.dp, end = 8.dp, bottom = 32.dp)) {
        // Main chart content
        content()

        // Y-axis labels overlay
        YAxisLabels(
            maxValue = maxQuantity,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 8.dp)
        )

        // X-axis labels overlay
        XAxisDayLabels(
            data = data,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 80.dp, end = 24.dp)
        )
    }
}
