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
package cnc.coop.milkcreamies.presentation.viewmodel.cows

import cnc.coop.milkcreamies.domain.repository.CowRepository
import cnc.coop.milkcreamies.domain.repository.MilkInEntryRepository
import cnc.coop.milkcreamies.models.Cow
import cnc.coop.milkcreamies.models.HealthStatus
import cnc.coop.milkcreamies.models.ActionStatus
import cnc.coop.milkcreamies.models.MilkInEntry
import cnc.coop.milkcreamies.presentation.viewmodel.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * ViewModel for Cows screen following MVVM best practices
 */
class CowsViewModel(
    private val cowRepository: CowRepository,
    private val milkInEntryRepository: MilkInEntryRepository
) : BaseViewModel() {

    // Cows state
    private val _cows = MutableStateFlow<List<Cow>>(emptyList())
    val cows: StateFlow<List<Cow>> = _cows.asStateFlow()

    // Selected cow state
    private val _selectedCow = MutableStateFlow<Cow?>(null)
    val selectedCow: StateFlow<Cow?> = _selectedCow.asStateFlow()

    // Cow milk entries for detailed view
    private val _cowMilkEntries = MutableStateFlow<List<MilkInEntry>>(emptyList())
    val cowMilkEntries: StateFlow<List<MilkInEntry>> = _cowMilkEntries.asStateFlow()

    // Search functionality
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filter states
    private val _healthFilter = MutableStateFlow<HealthStatus?>(null)
    val healthFilter: StateFlow<HealthStatus?> = _healthFilter.asStateFlow()

    // Stats filter for interactive cards
    private val _statsFilter = MutableStateFlow<String?>(null)
    val statsFilter: StateFlow<String?> = _statsFilter.asStateFlow()

    // Active status filter (by default show only active cows)
    private val _showActiveOnly = MutableStateFlow(true)
    val showActiveOnly: StateFlow<Boolean> = _showActiveOnly.asStateFlow()

    // Filtered cows based on current filter
    private val _filteredCows = MutableStateFlow<List<Cow>>(emptyList())
    val filteredCows: StateFlow<List<Cow>> = _filteredCows.asStateFlow()

    init {
        loadCows()
    }

    fun loadCows() {
        executeWithLoading {
            val cowsList = cowRepository.getAllCows()
            if (cowsList.isNotEmpty()) {
                _cows.value = cowsList
                applyFilter()
            } else {
                setError("No cows found. Please ensure the server is running and has data.")
            }
        }
    }

    fun selectCow(cow: Cow) {
        _selectedCow.value = cow
    }

    fun clearSelectedCow() {
        _selectedCow.value = null
    }

    fun setHealthFilter(healthStatus: HealthStatus?) {
        _healthFilter.value = healthStatus
        applyFilter()
    }

    fun setStatsFilter(filter: String?) {
        _statsFilter.value = filter
        applyFilter()
    }

    fun clearFilters() {
        _healthFilter.value = null
        _statsFilter.value = null
        applyFilter()
    }

    fun toggleActiveStatusFilter(showActiveOnly: Boolean) {
        _showActiveOnly.value = showActiveOnly
        applyFilter()
    }

    fun refreshCows() {
        loadCows()
    }

    fun searchCows(query: String) {
        _searchQuery.value = query
        applyFilter()
    }

    fun updateSearchQuery(query: String) {
        searchCows(query)
    }

    private fun applyFilter() {
        val healthFilter = _healthFilter.value
        val showActiveOnly = _showActiveOnly.value
        val query = _searchQuery.value.lowercase()
        val statsFilter = _statsFilter.value

        _filteredCows.value = _cows.value.filter { cow ->
            // Apply search filter
            (query.isEmpty() ||
                    cow.name.lowercase().contains(query) ||
                    cow.breed.lowercase().contains(query) ||
                    cow.cowId?.lowercase()?.contains(query) == true) &&
            // Apply health filter if set
            (healthFilter == null || cow.status.healthStatus == healthFilter) &&
                    // Apply stats filter if set
                    (statsFilter == null || when (statsFilter) {
                        "total" -> true // Show all cows
                        "healthy" -> cow.status.healthStatus == HealthStatus.HEALTHY
                        "attention" -> cow.status.healthStatus != HealthStatus.HEALTHY
                        else -> true
                    }) &&
                    // Filter by active status
                    (!showActiveOnly || cow.isActive)
        }
    }

    fun archiveCow(cow: Cow, archiveReason: ActionStatus) {
        executeWithLoading {
            try {
                val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

                // Using the repository archiveCow method
                val reason = when (archiveReason) {
                    ActionStatus.SOLD -> "Sold"
                    ActionStatus.DECEASED -> "Deceased"
                    else -> "Archived"
                }

                // Safely access owner info
                val ownerId = cow.ownerId
                val ownerDesc = if (ownerId.isBlank()) "unknown owner" else "member $ownerId"

                val success = cow.cowId?.let { cowId ->
                    cowRepository.archiveCow(cowId, reason, currentDate)
                } ?: false

                if (success) {
                    loadCows() // Refresh the list after update
                    clearError()
                } else {
                    setError("Failed to archive cow for $ownerDesc")
                }
            } catch (e: Exception) {
                setError("Error archiving cow: ${e.message}")
            }
        }
    }

    fun deleteCow(cowId: String) {
        executeWithLoading {
            try {
                val success = cowRepository.deleteCow(cowId)
                if (success) {
                    loadCows() // Refresh the list after deletion
                    clearError()
                } else {
                    setError("Failed to delete cow")
                }
            } catch (e: Exception) {
                setError("Error deleting cow: ${e.message}")
            }
        }
    }

    suspend fun loadCowDetails(cowId: String) {
        executeWithLoading {
            try {
                // Load selected cow
                val cow = cowRepository.getAllCows().find { it.cowId == cowId }
                _selectedCow.value = cow

                if (cow != null) {
                    // Load cow's milk entries
                    val allMilkEntries = milkInEntryRepository.getAllMilkInEntries()
                    val cowMilkEntries = allMilkEntries.filter { it.cowId == cowId }
                    _cowMilkEntries.value = cowMilkEntries
                }
            } catch (e: Exception) {
                setError("Failed to load cow details: ${e.message}")
            }
        }
    }
}
