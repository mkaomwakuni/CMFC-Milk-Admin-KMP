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

/**
 * Represents a navigation route in the application
 */
sealed class Route(val route: String) {
    // Authentication routes
    object Login : Route("login")

    // Main screens
    object Dashboard : Route("dashboard")
    object Members : Route("members")
    object MemberDetail : Route("members/{memberId}") {
        fun createRoute(memberId: String) = "members/$memberId"
    }

    object Cows : Route("cows")
    object CowDetail : Route("cows/{cowId}") {
        fun createRoute(cowId: String) = "cows/$cowId"
    }

    // Milk management routes
    object MilkIn : Route("milk/in")
    object MilkOut : Route("milk/out")
    object MilkSpoilt : Route("milk/spoilt")

    // Stock and financial routes
    object Stock : Route("stock")
    object Earnings : Route("earnings")

    // Settings
    object Settings : Route("settings")
}

/**
 * Centralizes navigation logic
 */
interface Navigator {
    fun navigateTo(route: Route)
    fun navigateBack(): Boolean
    fun navigateToMemberDetail(memberId: String)
    fun navigateToCowDetail(cowId: String)
}