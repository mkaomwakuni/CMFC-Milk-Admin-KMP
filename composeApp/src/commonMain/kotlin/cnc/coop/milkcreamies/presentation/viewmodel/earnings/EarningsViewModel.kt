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
package cnc.coop.milkcreamies.presentation.viewmodel.earnings

import cnc.coop.milkcreamies.data.InventoryManager
import cnc.coop.milkcreamies.domain.repository.EarningsSummaryRepository
import cnc.coop.milkcreamies.domain.repository.MilkOutEntryRepository
import cnc.coop.milkcreamies.models.*
import cnc.coop.milkcreamies.presentation.viewmodel.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.LocalDate
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.minus

/**
 * ViewModel for Earnings screen following MVVM best practices
 */
class EarningsViewModel(
    private val earningsSummaryRepository: EarningsSummaryRepository,
    private val milkOutEntryRepository: MilkOutEntryRepository,
    private val inventoryManager: InventoryManager
) : BaseViewModel() {

    // Earnings summary state
    private val _earningsSummary = MutableStateFlow(EarningsSummary(0.0, 0.0, 0.0))
    val earningsSummary: StateFlow<EarningsSummary> = _earningsSummary.asStateFlow()

    // Real-time earnings data from InventoryManager
    val inventoryEarningsSummary = inventoryManager.earningsSummary
    val inventoryStockSummary = inventoryManager.stockSummary
    val currentInventory = inventoryManager.currentInventory

    // Recent transactions
    private val _recentTransactions = MutableStateFlow<List<MilkOutEntry>>(emptyList())
    val recentTransactions: StateFlow<List<MilkOutEntry>> = _recentTransactions.asStateFlow()

    // Weekly earnings breakdown
    private val _weeklyEarnings = MutableStateFlow<List<DailyEarnings>>(emptyList())
    val weeklyEarnings: StateFlow<List<DailyEarnings>> = _weeklyEarnings.asStateFlow()

    // Monthly comparison
    private val _monthlyComparison = MutableStateFlow<List<MonthlyEarnings>>(emptyList())
    val monthlyComparison: StateFlow<List<MonthlyEarnings>> = _monthlyComparison.asStateFlow()

    init {
        loadEarningsData()
    }

    fun loadEarningsData() {
        executeWithLoading {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            loadEarningsSummary(today)
            loadRecentTransactions()
            calculateWeeklyEarnings(today)
            calculateMonthlyComparison(today)
        }
    }

    private suspend fun loadEarningsSummary(today: LocalDate) {
        try {
            val earningsResult = earningsSummaryRepository.getEarningsSummary(today)
            if (earningsResult.isSuccess) {
                earningsResult.getOrNull()?.let { earnings ->
                    _earningsSummary.value = earnings
                }
            } else {
                // Fallback to local calculation
                calculateLocalEarnings(today)
            }
        } catch (e: Exception) {
            calculateLocalEarnings(today)
        }
    }

    private suspend fun calculateLocalEarnings(today: LocalDate) {
        val weekAgo = today.minus(DatePeriod(days = 7))
        val monthAgo = today.minus(DatePeriod(months = 1))
        val milkOutEntries = milkOutEntryRepository.getAllMilkOutEntries()

        val todayEarnings = milkOutEntries.filter { it.date == today }
            .sumOf { it.quantitySold * it.pricePerLiter }
        val weeklyEarnings = milkOutEntries.filter { it.date >= weekAgo }
            .sumOf { it.quantitySold * it.pricePerLiter }
        val monthlyEarnings = milkOutEntries.filter { it.date >= monthAgo }
            .sumOf { it.quantitySold * it.pricePerLiter }

        _earningsSummary.value = EarningsSummary(
            todayEarnings = todayEarnings,
            weeklyEarnings = weeklyEarnings,
            monthlyEarnings = monthlyEarnings
        )
    }

    private suspend fun loadRecentTransactions() {
        val allEntries = milkOutEntryRepository.getAllMilkOutEntries()
        _recentTransactions.value = allEntries.sortedByDescending { it.date }.take(10)
    }

    private suspend fun calculateWeeklyEarnings(today: LocalDate) {
        val weeklyData = mutableListOf<DailyEarnings>()
        val milkOutEntries = milkOutEntryRepository.getAllMilkOutEntries()

        for (i in 6 downTo 0) {
            val date = today.minus(DatePeriod(days = i))
            val dailyEntries = milkOutEntries.filter { it.date == date }
            val dailyEarnings = dailyEntries.sumOf { it.quantitySold * it.pricePerLiter }
            val transactionCount = dailyEntries.size

            weeklyData.add(
                DailyEarnings(
                    date = date,
                    earnings = dailyEarnings,
                    transactionCount = transactionCount,
                    averagePerTransaction = if (transactionCount > 0) dailyEarnings / transactionCount else 0.0
                )
            )
        }

        _weeklyEarnings.value = weeklyData
    }

    private suspend fun calculateMonthlyComparison(today: LocalDate) {
        val monthlyData = mutableListOf<MonthlyEarnings>()
        val milkOutEntries = milkOutEntryRepository.getAllMilkOutEntries()

        for (i in 0..5) { // Last 6 months
            val monthStart = today.minus(DatePeriod(months = i))
            val monthEnd = monthStart.minus(DatePeriod(days = 1))
            val previousMonthStart = monthStart.minus(DatePeriod(months = 1))

            val currentMonthEntries = milkOutEntries.filter {
                it.date >= previousMonthStart && it.date < monthStart
            }
            val monthlyEarnings = currentMonthEntries.sumOf { it.quantitySold * it.pricePerLiter }
            val monthlyTransactions = currentMonthEntries.size

            monthlyData.add(
                MonthlyEarnings(
                    month = monthStart,
                    earnings = monthlyEarnings,
                    transactionCount = monthlyTransactions,
                    averagePerDay = monthlyEarnings / 30
                )
            )
        }

        _monthlyComparison.value = monthlyData.reversed() // Show chronologically
    }

    fun refreshEarnings() {
        loadEarningsData()
    }
}


