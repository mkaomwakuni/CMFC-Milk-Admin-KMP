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
package cnc.coop.milkcreamies.presentation.viewmodel.milk

import cnc.coop.milkcreamies.domain.repository.MilkSpoiltEntryRepository
import cnc.coop.milkcreamies.models.MilkSpoiltEntry
import cnc.coop.milkcreamies.models.SpoilageCause
import cnc.coop.milkcreamies.presentation.viewmodel.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDate

/**
 * ViewModel for Milk Spoilt screen following MVVM best practices
 */
class MilkSpoiltViewModel(
    private val milkSpoiltEntryRepository: MilkSpoiltEntryRepository
) : BaseViewModel() {

    // Milk Spoilt entries state
    private val _milkSpoiltEntries = MutableStateFlow<List<MilkSpoiltEntry>>(emptyList())
    val milkSpoiltEntries: StateFlow<List<MilkSpoiltEntry>> = _milkSpoiltEntries.asStateFlow()

    // Summary statistics
    private val _totalSpoiltLiters = MutableStateFlow(0.0)
    val totalSpoiltLiters: StateFlow<Double> = _totalSpoiltLiters.asStateFlow()

    private val _totalIncidents = MutableStateFlow(0)
    val totalIncidents: StateFlow<Int> = _totalIncidents.asStateFlow()

    private val _totalLoss = MutableStateFlow(0.0)
    val totalLoss: StateFlow<Double> = _totalLoss.asStateFlow()

    init {
        loadAllSpoiltEntries()
    }

    fun loadAllSpoiltEntries() {
        executeWithLoading {
            try {
                val entries = milkSpoiltEntryRepository.getAllMilkSpoiltEntries()
                _milkSpoiltEntries.value = entries
                calculateSummaryStats(entries)
                clearError()
            } catch (e: Exception) {
                setError("Failed to load spoilt milk entries: ${e.message}")
            }
        }
    }

    private fun calculateSummaryStats(entries: List<MilkSpoiltEntry>) {
        _totalSpoiltLiters.value = entries.sumOf { it.amountSpoilt }
        _totalIncidents.value = entries.size
        _totalLoss.value = entries.sumOf { it.lossAmount }
    }

    fun addSpoiltEntry(
        date: LocalDate,
        amountSpoilt: Double,
        lossAmount: Double,
        cause: SpoilageCause? = null
    ) {
        executeWithLoading {
            try {
                // Validation
                when {
                    amountSpoilt <= 0 -> {
                        setError("Spoilt amount must be positive.")
                        return@executeWithLoading
                    }
                    lossAmount < 0 -> {
                        setError("Loss amount cannot be negative.")
                        return@executeWithLoading
                    }
                }

                val newEntry = MilkSpoiltEntry(
                    spoiltId = null, // Server will generate ID
                    date = date,
                    amountSpoilt = amountSpoilt,
                    lossAmount = lossAmount,
                    cause = cause
                )

                milkSpoiltEntryRepository.addMilkSpoiltEntry(newEntry)
                loadAllSpoiltEntries() // Refresh data
                clearError()

            } catch (e: Exception) {
                setError("Failed to add spoilt milk entry: ${e.message}")
            }
        }
    }

    fun deleteSpoiltEntry(spoiltId: String) {
        executeWithLoading {
            try {
                val success = milkSpoiltEntryRepository.deleteMilkSpoiltEntry(spoiltId)
                if (success) {
                    loadAllSpoiltEntries() // Refresh data
                    clearError()
                } else {
                    setError("Failed to delete spoilt milk entry")
                }
            } catch (e: Exception) {
                setError("Failed to delete spoilt milk entry: ${e.message}")
            }
        }
    }

    fun getSpoiltEntryById(spoiltId: String) {
        executeWithLoading(showLoading = false) {
            try {
                val entry = milkSpoiltEntryRepository.getMilkSpoiltEntryById(spoiltId)
                // Handle the retrieved entry as needed
                clearError()
            } catch (e: Exception) {
                setError("Failed to get spoilt milk entry: ${e.message}")
            }
        }
    }

    fun refreshData() {
        loadAllSpoiltEntries()
    }
}