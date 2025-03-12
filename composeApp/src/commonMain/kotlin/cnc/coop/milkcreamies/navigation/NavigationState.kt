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
package cnc.coop.milkcreamies.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

enum class Screen {
    DASHBOARD,
    MILK_IN,
    MILK_OUT,
    STOCK,
    EARNINGS,
    COWS,
    MEMBERS,
    SETTINGS,
    LOGIN
}

class NavigationState {
    private val _currentScreen = mutableStateOf(Screen.DASHBOARD)
    val currentScreen: State<Screen> = _currentScreen

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun navigateToTab(tabIndex: Int) {
        val screen = when (tabIndex) {
            0 -> Screen.DASHBOARD
            1 -> Screen.MILK_IN
            2 -> Screen.MILK_OUT
            3 -> Screen.STOCK
            4 -> Screen.EARNINGS
            5 -> Screen.COWS
            6 -> Screen.MEMBERS
            7 -> Screen.SETTINGS
            8 -> Screen.LOGIN
            else -> Screen.DASHBOARD
        }
        navigateTo(screen)
    }

    fun getCurrentTabIndex(): Int {
        return when (_currentScreen.value) {
            Screen.DASHBOARD -> 0
            Screen.MILK_IN -> 1
            Screen.MILK_OUT -> 2
            Screen.STOCK -> 3
            Screen.EARNINGS -> 4
            Screen.COWS -> 5
            Screen.MEMBERS -> 6
            Screen.SETTINGS -> 7
            Screen.LOGIN -> 8
        }
    }
}

@Composable
fun rememberNavigationState(): NavigationState {
    return remember { NavigationState() }
}
