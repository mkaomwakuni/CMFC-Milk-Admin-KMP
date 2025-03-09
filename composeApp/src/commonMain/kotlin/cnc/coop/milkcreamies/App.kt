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
package cnc.coop.milkcreamies

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cnc.coop.milkcreamies.di.appModule
import cnc.coop.milkcreamies.presentation.ui.common.theme.MilkCooperativeTheme
import cnc.coop.milkcreamies.presentation.ui.common.theme.ThemeManager
import cnc.coop.milkcreamies.presentation.ui.screens.Pane
import cnc.coop.milkcreamies.presentation.ui.screens.logins.LandingScreen
import cnc.coop.milkcreamies.presentation.viewmodel.auth.AuthViewModel
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

@Composable
fun App() {
    val themeManager = ThemeManager()

    KoinApplication(
        application = {
            modules(appModule)
        }
    ) {
        MilkCooperativeTheme(themeManager = themeManager) {
            AuthenticatedApp()
        }
    }
}

@Composable
fun AuthenticatedApp(
    authViewModel: AuthViewModel = koinInject()
) {
    val authState by authViewModel.uiState.collectAsState()

    if (authState.isLoggedIn) {
        // User is logged in, show the main app
        Pane()
    } else {
        // User is not logged in, show login screen
        LandingScreen(
            onLoginSuccess = {
                // Navigation will happen automatically when authState.isLoggedIn becomes true
            }
        )
    }
}
