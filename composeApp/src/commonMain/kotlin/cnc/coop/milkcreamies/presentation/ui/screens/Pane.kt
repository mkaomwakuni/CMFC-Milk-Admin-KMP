/*
 * Copyright 2025  MkaoCodes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * you may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cnc.coop.milkcreamies.presentation.ui.screens

import AddSaleDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cnc.coop.milkcreamies.navigation.Screen
import cnc.coop.milkcreamies.navigation.rememberNavigationState
import cnc.coop.milkcreamies.presentation.ui.common.components.Sidebar
import cnc.coop.milkcreamies.presentation.ui.common.components.TopBar
import cnc.coop.milkcreamies.presentation.ui.navigation.NavigationContent
import cnc.coop.milkcreamies.presentation.ui.screens.milk.AddMilkEntryDialog
import cnc.coop.milkcreamies.presentation.ui.screens.milkout.AddSpoiltMilkDialog
import cnc.coop.milkcreamies.presentation.viewmodel.auth.AuthViewModel
import cnc.coop.milkcreamies.presentation.viewmodel.cows.CowsViewModel
import cnc.coop.milkcreamies.presentation.viewmodel.members.MembersViewModel
import cnc.coop.milkcreamies.presentation.viewmodel.milk.MilkInViewModel
import cnc.coop.milkcreamies.presentation.viewmodel.milk.MilkOutViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

@Composable
fun PaneHeader(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onMilkIn: () -> Unit,
    onMilkOut: () -> Unit
) {
    TopBar(
        onMilkIn = {},
        onMilkOut = {}
    )
}

@Composable
fun Pane(
    milkOut: MilkOutViewModel = koinInject(),
    milkIn: MilkInViewModel = koinInject(),
    cowsViewModel: CowsViewModel = koinInject(),
    membersViewModel: MembersViewModel = koinInject(),
    authViewModel: AuthViewModel = koinInject()
) {
    val navigationState = rememberNavigationState()
    val currentScreen by navigationState.currentScreen
    val currentStock by milkOut.currentStock.collectAsState()

    var showMilkOutDialog by remember { mutableStateOf(false) }
    var showMilkInDialog by remember { mutableStateOf(false) }
    var showSpoiltDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Update search in viewmodels when query changes
    cowsViewModel.searchCows(searchQuery)
    membersViewModel.searchMembers(searchQuery)

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Sidebar(
            selectedTab = navigationState.getCurrentTabIndex(),
            onTabSelected = { tabIndex ->
                navigationState.navigateToTab(tabIndex)
            },
            onLogout = {
                authViewModel.logout()
            }
        )

        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(
                currentScreen = currentScreen,
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                searchPlaceholder = when (currentScreen) {
                    Screen.COWS -> "Search cows by name, breed, or ID..."
                    Screen.MEMBERS -> "Search members by name..."
                    Screen.MILK_IN -> "Search milk entries..."
                    Screen.MILK_OUT -> "Search sales..."
                    Screen.STOCK -> "Search stock items..."
                    else -> "Search anything..."
                },
                onMilkIn = { showMilkInDialog = true },
                onMilkOut = { showMilkOutDialog = true },
                onMilkSpoilt = { showSpoiltDialog = true }
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                NavigationContent(currentScreen)
            }
        }
    }

    if (showMilkInDialog) {
        AddMilkEntryDialog(
            onDismiss = { showMilkInDialog = false },
            onSave = { milkEntry ->
                milkIn.addMilkInEntry(
                    cowId = milkEntry.cowId,
                    ownerId = milkEntry.ownerId,
                    quantityLiters = milkEntry.liters,
                    date = milkEntry.date,
                    milkingType = milkEntry.milkingType
                )
                showMilkInDialog = false
            },
            viewModel = milkIn
        )
    }

    if (showMilkOutDialog) {
        AddSaleDialog(
            viewModel = milkOut,
            availableStock = currentStock.currentStock,
            onDismiss = { showMilkOutDialog = false },
            onAddSale = { customerName, quantity, pricePerLiter, paymentMode ->
                milkOut.addMilkOutEntry(
                    customerName = customerName,
                    quantitySold = quantity,
                    pricePerLiter = pricePerLiter,
                    paymentMode = paymentMode,
                    date = Clock.System.todayIn(TimeZone.currentSystemDefault())
                )
                showMilkOutDialog = false
            }
        )
    }

    if (showSpoiltDialog) {
        AddSpoiltMilkDialog(
            onDismiss = { showSpoiltDialog = false },
            onAddSpoiltEntry = { entry ->
                // Handle spoilt milk entry
                showSpoiltDialog = false
            },
            currentStock = currentStock.currentStock
        )
    }
}

@Preview
@Composable
fun PanePreview() {
    Pane()
}
