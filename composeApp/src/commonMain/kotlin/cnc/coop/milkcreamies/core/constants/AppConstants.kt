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
package cnc.coop.milkcreamies.core.constants

/**
 * Application-wide constants following MVVM best practices
 */
object AppConstants {

    // API Configuration
    object Api {
        const val BASE_URL = "http://localhost:8081"
        const val TIMEOUT_SECONDS = 30L
        const val API_KEY = "dairy-app-secret-key-12345"

        // Helper function to get base URL
        fun getBaseUrl(): String = BASE_URL

        // Helper function to get API endpoint
        fun getEndpoint(path: String): String = "$BASE_URL/$path"
    }

    // Database Configuration
    object Database {
        const val NAME = "milk_cooperative.db"
        const val VERSION = 1
    }

    // UI Constants
    object UI {
        const val ANIMATION_DURATION_SHORT = 150
        const val ANIMATION_DURATION_MEDIUM = 300
        const val ANIMATION_DURATION_LONG = 500

        const val DEFAULT_PADDING = 16
        const val SMALL_PADDING = 8
        const val LARGE_PADDING = 24

        const val CARD_ELEVATION = 4
        const val DIALOG_ELEVATION = 8
    }

    // Business Logic Constants
    object Business {
        const val MIN_COW_AGE = 1
        const val MAX_COW_AGE = 20
        const val MIN_MILK_QUANTITY = 0.1
        const val MAX_MILK_QUANTITY = 100.0
        const val DEFAULT_MILK_PRICE = 50.0
    }

    // Error Messages
    object ErrorMessages {
        const val NETWORK_ERROR = "Failed to connect to server. Please check your connection."
        const val TIMEOUT_ERROR = "Request timed out. Please try again."
        const val GENERIC_ERROR = "An unexpected error occurred"
        const val VALIDATION_ERROR = "Please check your input and try again"
    }

    // Success Messages
    object SuccessMessages {
        const val COW_ADDED = "Cow added successfully"
        const val MEMBER_ADDED = "Member added successfully"
        const val MILK_ENTRY_ADDED = "Milk entry added successfully"
        const val DATA_UPDATED = "Data updated successfully"
    }

    // Navigation
    object Navigation {
        const val DASHBOARD = "dashboard"
        const val MILK_IN = "milk_in"
        const val MILK_OUT = "milk_out"
        const val STOCK = "stock"
        const val EARNINGS = "earnings"
        const val COWS = "cows"
        const val MEMBERS = "members"
    }

    // Preferences Keys
    object PreferenceKeys {
        const val USER_PREFERENCES = "user_preferences"
        const val THEME_MODE = "theme_mode"
        const val LAST_SYNC_TIME = "last_sync_time"
    }
}
