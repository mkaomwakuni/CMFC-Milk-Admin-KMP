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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cnc.coop.milkcreamies.util.FileUtil.ExportFormat

/**
 * Dialog for selecting export format
 */
@Composable
fun ExportFormatDialog(
    onDismissRequest: () -> Unit,
    onFormatSelected: (ExportFormat) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = "Export Report",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Choose a format to export your data:",
                    style = MaterialTheme.typography.bodyLarge
                )

                ExportFormatOption(
                    format = ExportFormat.CSV,
                    icon = Icons.Default.TableChart,
                    title = "CSV File",
                    description = "Comma-separated values file that can be opened with Excel or other spreadsheet software.",
                    onClick = { onFormatSelected(ExportFormat.CSV) }
                )

                ExportFormatOption(
                    format = ExportFormat.EXCEL,
                    icon = Icons.Default.TableChart,
                    title = "Excel File",
                    description = "Microsoft Excel format with proper formatting and multiple sheets.",
                    onClick = { onFormatSelected(ExportFormat.EXCEL) }
                )

                ExportFormatOption(
                    format = ExportFormat.PDF,
                    icon = Icons.Default.PictureAsPdf,
                    title = "PDF Document",
                    description = "Portable Document Format for easy viewing and sharing.",
                    onClick = { onFormatSelected(ExportFormat.PDF) }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismissRequest,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("Cancel")
            }
        },
        dismissButton = null,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
private fun ExportFormatOption(
    format: ExportFormat,
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(4.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = "Download as ${format.name}",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Dialog shown when an export is in progress or completed
 */
@Composable
fun ExportProgressDialog(
    exportState: ExportState,
    onDismiss: () -> Unit
) {
    val progress = if (exportState is ExportState.Exporting) exportState.progress else 1f

    AlertDialog(
        onDismissRequest = {
            // Only allow dismissing if export is complete or failed
            if (exportState !is ExportState.Exporting) {
                onDismiss()
            }
        },
        icon = {
            when (exportState) {
                is ExportState.Exporting -> CircularProgressIndicator(progress = { progress })
                is ExportState.Success -> Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )

                is ExportState.Failed -> Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(36.dp)
                )
                ExportState.Initial -> {
                    // Initial state shows nothing
                }
            }
        },
        title = {
            Text(
                text = when (exportState) {
                    is ExportState.Exporting -> "Exporting Data"
                    is ExportState.Success -> "Export Complete"
                    is ExportState.Failed -> "Export Failed"
                    ExportState.Initial -> "Prepare Export"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = when (exportState) {
                    is ExportState.Exporting -> "Please wait while your data is being prepared..."
                    is ExportState.Success -> "Your file has been saved to:\n${exportState.filePath}"
                    is ExportState.Failed -> "Failed to export: ${exportState.error}"
                    ExportState.Initial -> "Select a format to export your data."
                },
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                enabled = exportState !is ExportState.Exporting,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("OK")
            }
        },
        dismissButton = if (exportState is ExportState.Exporting) {
            null
        } else {
            {
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        },
        shape = RoundedCornerShape(4.dp)
    )
}

/**
 * Represents the state of an export operation
 */
sealed class ExportState {
    object Initial : ExportState()
    data class Exporting(val progress: Float = 0f) : ExportState()
    data class Success(val filePath: String) : ExportState()
    data class Failed(val error: String) : ExportState()
}
