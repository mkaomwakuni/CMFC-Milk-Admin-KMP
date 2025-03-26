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
package cnc.coop.milkcreamies.presentation.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cnc.coop.milkcreamies.models.SettingItem
import cnc.coop.milkcreamies.models.SettingType
import cnc.coop.milkcreamies.models.SettingsGroup
import cnc.coop.milkcreamies.presentation.ui.common.theme.LocalThemeManager
import cnc.coop.milkcreamies.presentation.viewmodel.dashboard.DashboardViewModel
import org.koin.compose.koinInject

@Composable
fun SettingsScreen() {
    val themeManager = LocalThemeManager.current
    var pushNotifications by remember { mutableStateOf(true) }
    var language by remember { mutableStateOf("English") }
    var autoSync by remember { mutableStateOf(true) }
    var monthlyReportNotifications by remember { mutableStateOf(true) }
    val dashboardViewModel: DashboardViewModel = koinInject()
    var showExportSuccessMessage by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    val settingsGroups = listOf(
        SettingsGroup(
            title = "Appearance",
            icon = Icons.Outlined.Visibility,
            settings = listOf(
                SettingItem(
                    title = "Dark Mode",
                    description = "Switch between light and dark themes",
                    icon = if (themeManager.isDarkMode) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                    type = SettingType.TOGGLE,
                    value = themeManager.isDarkMode,
                    onValueChange = { themeManager.setDarkMode(it as Boolean) }
                ),
                SettingItem(
                    title = "Language",
                    description = "Choose your preferred language",
                    icon = Icons.Outlined.Language,
                    type = SettingType.SELECT,
                    value = language,
                    onValueChange = { language = it as String },
                    options = listOf("English", "Swahili", "French", "Spanish")
                )
            )
        ),
        SettingsGroup(
            title = "Notifications",
            icon = Icons.Default.Notifications,
            settings = listOf(
                SettingItem(
                    title = "Push Notifications",
                    description = "Receive push notifications on your device",
                    icon = Icons.Outlined.PhoneAndroid,
                    type = SettingType.TOGGLE,
                    value = pushNotifications,
                    onValueChange = { pushNotifications = it as Boolean }
                ),
                SettingItem(
                    title = "Monthly Report Notifications",
                    description = "Get a notification at the end of each month to generate reports",
                    icon = Icons.Default.Notifications,
                    type = SettingType.TOGGLE,
                    value = monthlyReportNotifications,
                    onValueChange = { monthlyReportNotifications = it as Boolean }
                )
            )
        ),
        SettingsGroup(
            title = "System",
            icon = Icons.Default.Settings,
            settings = listOf(
                SettingItem(
                    title = "Auto Sync",
                    description = "Automatically sync data when connected",
                    icon = Icons.Default.Settings,
                    type = SettingType.TOGGLE,
                    value = autoSync,
                    onValueChange = { autoSync = it as Boolean }
                )
            )
        ),
        SettingsGroup(
            title = "Account & Security",
            icon = Icons.Default.Security,
            settings = listOf(
                SettingItem(
                    title = "Edit Profile",
                    description = "Update your personal information",
                    icon = Icons.Default.Person,
                    type = SettingType.NAVIGATION
                ),
                SettingItem(
                    title = "Security Settings",
                    description = "Manage passwords and security options",
                    icon = Icons.Default.Security,
                    type = SettingType.NAVIGATION
                )
            )
        )
    )

    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) {
        Column {
            // Header
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { /* Handle back */ }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Content
            LazyColumn(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                settingsGroups.forEach { group ->
                    item {
                        SettingsGroup(group = group)
                    }
                }

                item {
                    AppInfoCard(
                        dashboardViewModel = dashboardViewModel,
                        showExportSuccessMessage = showExportSuccessMessage,
                        onExportSuccess = { showExportSuccessMessage = true },
                        onDismissSuccessMessage = { showExportSuccessMessage = false },
                        onShowAboutDialog = { showAboutDialog = true },
                        onShowHelpDialog = { showHelpDialog = true }
                    )
                }
            }
        }
    }

    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }

    if (showHelpDialog) {
        HelpSupportDialog(onDismiss = { showHelpDialog = false })
    }
}

@Composable
fun SettingsGroup(group: SettingsGroup) {
    Card(
        shape = RoundedCornerShape(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            // Group Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = group.icon,
                        contentDescription = group.title,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = group.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Divider(
                color = MaterialTheme.colorScheme.outline,
                thickness = 1.dp
            )

            // Settings Items
            Column {
                group.settings.forEachIndexed { index, setting ->
                    SettingItem(setting = setting)
                    if (index < group.settings.lastIndex) {
                        Divider(
                            modifier = Modifier.padding(start = 56.dp),
                            color = MaterialTheme.colorScheme.outline,
                            thickness = 1.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingItem(setting: SettingItem) {
    val modifier = if (setting.type == SettingType.NAVIGATION) {
        Modifier.clickable { /* Handle navigation */ }
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = setting.icon,
                    contentDescription = setting.title,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = setting.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (setting.description != null) {
                    Text(
                        text = setting.value.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        when (setting.type) {
            SettingType.TOGGLE -> {
                val value = (setting.value as? Boolean ?: false)
                val onValueChange = setting.onValueChange as? (Boolean) -> Unit
                Switch(
                    checked = value,
                    onCheckedChange = onValueChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
            SettingType.SELECT -> {
                // For simplicity, using a text representation
                // In a real app, you'd use a dropdown menu
                Text(
                    text = setting.value.toString(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            SettingType.NAVIGATION -> {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Navigate",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun AppInfoCard(
    dashboardViewModel: DashboardViewModel,
    showExportSuccessMessage: Boolean,
    onExportSuccess: () -> Unit,
    onDismissSuccessMessage: () -> Unit,
    onShowAboutDialog: () -> Unit,
    onShowHelpDialog: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "App Icon",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = "Milk Management System",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Version 1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "Generate Report",
                        modifier = Modifier
                            .clickable {
                                dashboardViewModel.exportAllDataToCsv()
                                onExportSuccess()
                            }
                            .padding(12.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (showExportSuccessMessage) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = "Report generated successfully! Check the application console to copy the CSV content. Click to dismiss.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onDismissSuccessMessage() }
                                .padding(12.dp),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "About",
                        modifier = Modifier
                            .clickable { onShowAboutDialog() }
                            .padding(12.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "Help & Support",
                        modifier = Modifier
                            .clickable { onShowHelpDialog() }
                            .padding(12.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo/icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "App Logo",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Chonyi Milk Cooperative Farmers",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Version 1.0.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    textAlign = TextAlign.Start
                )

                Text(
                    text = "The Chonyi Milk Cooperative Farmers application is a comprehensive milk management system designed for dairy cooperatives.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Key Features:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    textAlign = TextAlign.Start
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    BulletPoint("Milk production tracking and management")
                    BulletPoint("Cow health and productivity monitoring")
                    BulletPoint("Member and customer management")
                    BulletPoint("Sales and inventory tracking")
                    BulletPoint("Financial reporting and analytics")
                    BulletPoint("Spoilt milk management and loss calculation")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "© 2025 Chonyi Milk Cooperative",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun BulletPoint(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(end = 8.dp, top = 0.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun HelpSupportDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Help & Support",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                Text(
                    text = "Frequently Asked Questions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    textAlign = TextAlign.Start
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        FAQItem(
                            question = "How do I add a new milk entry?",
                            answer = "Navigate to the Milk In screen and click the 'Add Milk Entry' button. Fill in the required information and submit the form."
                        )
                    }

                    item {
                        FAQItem(
                            question = "How do I record spoilt milk?",
                            answer = "Go to the Milk Out section, select the Spoilt tab, and click on 'Report Spoilt Milk'. Enter the amount and the system will automatically calculate the financial loss based on the fixed price."
                        )
                    }

                    item {
                        FAQItem(
                            question = "How do I generate reports?",
                            answer = "Go to the Settings screen and click on the 'Generate Report' button. The system will generate a CSV report. Open the app's console output, copy the content between the 'CSV CONTENT START' and 'CSV CONTENT END' markers, paste it into a text file, and save with a .csv extension."
                        )
                    }

                    item {
                        FAQItem(
                            question = "How do I add or manage cows?",
                            answer = "From the Dashboard, click on 'Add Cow' button in the Cows panel, or navigate to the Cows screen for detailed management options."
                        )
                    }
                }

                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                Text(
                    text = "Contact Support",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    textAlign = TextAlign.Start
                )

                Text(
                    text = "Email: support@chonyimilk.co.ke\nPhone: +254 712 345 678\nHours: Mon-Fri, 8AM-5PM EAT",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun FAQItem(question: String, answer: String) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = question,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = answer,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
