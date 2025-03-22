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
package cnc.coop.milkcreamies.presentation.ui.navigation

import MilkSalesScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import cnc.coop.milkcreamies.navigation.Screen
import cnc.coop.milkcreamies.presentation.ui.screens.cows.CowsScreen
import cnc.coop.milkcreamies.presentation.ui.screens.dashboard.DashboardScreen
import cnc.coop.milkcreamies.presentation.ui.screens.earnings.EarningsScreen
import cnc.coop.milkcreamies.presentation.ui.screens.members.MembersScreen
import cnc.coop.milkcreamies.presentation.ui.screens.milk.MilkInScreen
import cnc.coop.milkcreamies.presentation.ui.screens.settings.SettingsScreen
import cnc.coop.milkcreamies.presentation.ui.screens.stock.StockScreen
import cnc.coop.milkcreamies.presentation.viewmodel.dashboard.DashboardViewModel
import cnc.coop.milkcreamies.presentation.viewmodel.milk.MilkInViewModel
import cnc.coop.milkcreamies.presentation.viewmodel.milk.MilkOutViewModel
import cnc.coop.milkcreamies.presentation.viewmodel.stock.StockViewModel
import org.koin.compose.koinInject

@Composable
fun NavigationContent(screen: Screen) {
    // Get all ViewModels that need auto-refresh when navigating between screens
    val milkInViewModel: MilkInViewModel = koinInject()
    val milkOutViewModel: MilkOutViewModel = koinInject()
    val dashboardViewModel: DashboardViewModel = koinInject()
    val stockViewModel: StockViewModel = koinInject()

    // Auto-refresh data when navigating to any screen
    LaunchedEffect(screen) {
        when (screen) {
            Screen.MILK_IN -> milkInViewModel.refreshData()
            Screen.MILK_OUT -> milkOutViewModel.refreshData()
            Screen.DASHBOARD -> dashboardViewModel.refreshData()
            Screen.STOCK -> stockViewModel.refreshData()
            else -> {}
        }
    }

    when (screen) {
        Screen.DASHBOARD -> {
            DashboardScreen()
        }
        Screen.MILK_IN -> {
            MilkInScreen()
        }
        Screen.MILK_OUT -> {
            MilkSalesScreen()
        }
        Screen.STOCK -> {
            StockScreen()
        }
        Screen.EARNINGS -> {
            EarningsScreen()
        }
        Screen.COWS -> {
            CowsScreen()
        }
        Screen.MEMBERS -> {
            MembersScreen()
        }
        Screen.SETTINGS -> {
            SettingsScreen()
        }
        else -> {
            DashboardScreen()
        }
    }
}
