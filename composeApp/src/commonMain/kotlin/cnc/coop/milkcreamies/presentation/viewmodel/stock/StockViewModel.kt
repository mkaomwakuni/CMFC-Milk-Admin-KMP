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
package cnc.coop.milkcreamies.presentation.viewmodel.stock

import cnc.coop.milkcreamies.data.InventoryManager
import cnc.coop.milkcreamies.domain.repository.*
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
 * ViewModel for Stock screen following MVVM best practices
 */
class StockViewModel(
    private val stockSummaryRepository: StockSummaryRepository,
    private val milkInEntryRepository: MilkInEntryRepository,
    private val milkOutEntryRepository: MilkOutEntryRepository,
    private val milkSpoiltEntryRepository: MilkSpoiltEntryRepository,
    private val cowRepository: CowRepository,
    private val cowSummaryRepository: CowSummaryRepository,
    private val inventoryManager: InventoryManager
) : BaseViewModel() {

    // Stock summary state - now using InventoryManager
    private val _stockSummary = MutableStateFlow(
        StockSummary(
            currentStock = 0.0,
            dailyProduce = 0.0,
            dailyTotalLitersSold = 0.0,
            weeklySold = 0.0,
            weeklySpoilt = 0.0,
            monthlySold = 0.0
        )
    )
    val stockSummary: StateFlow<StockSummary> = _stockSummary.asStateFlow()

    // Milk analytics state
    private val _milkAnalytics = MutableStateFlow(MilkAnalytics())
    val milkAnalytics: StateFlow<MilkAnalytics> = _milkAnalytics.asStateFlow()

    // Cow summary state - updated to match new model
    private val _cowSummary = MutableStateFlow(
        CowSummary(
            totalActiveCows = 0,
            totalArchivedCows = 0,
            healthyCows = 0,
            needsAttention = 0
        )
    )
    val cowSummary: StateFlow<CowSummary> = _cowSummary.asStateFlow()

    // Real-time inventory data from InventoryManager
    val currentInventory = inventoryManager.currentInventory
    val inventoryStockSummary = inventoryManager.stockSummary
    val inventoryEarningsSummary = inventoryManager.earningsSummary

    // Weekly trends
    private val _weeklyTrends = MutableStateFlow<List<DailyStockData>>(emptyList())
    val weeklyTrends: StateFlow<List<DailyStockData>> = _weeklyTrends.asStateFlow()

    // Monthly summary
    private val _monthlyData = MutableStateFlow(MonthlyStockSummary())
    val monthlyData: StateFlow<MonthlyStockSummary> = _monthlyData.asStateFlow()

    init {
        loadStockData()
    }

    fun loadStockData() {
        executeWithLoading {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            loadStockSummary(today)
            loadMilkAnalytics(today)
            loadCowSummary()
            loadWeeklyTrends(today)
            calculateMonthlyData(today)
        }
    }

    private suspend fun loadStockSummary(today: LocalDate) {
        try {
            val stockResult = stockSummaryRepository.getStockSummary(today)
            if (stockResult.isSuccess) {
                stockResult.getOrNull()?.let { stock ->
                    _stockSummary.value = stock
                }
            } else {
                // Fallback to local calculation
                calculateLocalStockSummary(today)
            }
        } catch (e: Exception) {
            calculateLocalStockSummary(today)
        }
    }

    private suspend fun loadMilkAnalytics(today: LocalDate) {
        try {
            val analyticsResult = cowRepository.getMilkAnalytics(today)
            if (analyticsResult.isSuccess) {
                analyticsResult.getOrNull()?.let { analytics ->
                    _milkAnalytics.value = analytics
                }
            }
        } catch (e: Exception) {
            // Keep default empty analytics
        }
    }

    private suspend fun loadCowSummary() {
        try {
            val cowSummaryResult = cowSummaryRepository.getCowSummary()
            if (cowSummaryResult.isSuccess) {
                cowSummaryResult.getOrNull()?.let { cowSummary ->
                    _cowSummary.value = cowSummary
                }
            }
        } catch (e: Exception) {
            // Keep default empty cow summary
        }
    }

    private suspend fun calculateLocalStockSummary(today: LocalDate) {
        val weekAgo = today.minus(DatePeriod(days = 7))
        val monthAgo = today.minus(DatePeriod(months = 1))

        val milkInEntries = milkInEntryRepository.getAllMilkInEntries()
        val milkOutEntries = milkOutEntryRepository.getAllMilkOutEntries()
        val milkSpoiltEntries = milkSpoiltEntryRepository.getAllMilkSpoiltEntries()

        val dailyProduce = milkInEntries.filter { it.date == today }.sumOf { it.liters }
        val dailyTotalLitersSold =
            milkOutEntries.filter { it.date == today }.sumOf { it.quantitySold }
        val weeklySold = milkOutEntries.filter { it.date >= weekAgo }.sumOf { it.quantitySold }
        val weeklySpoilt = milkSpoiltEntries.filter { it.date >= weekAgo }.sumOf { it.amountSpoilt }
        val monthlySold = milkOutEntries.filter { it.date >= monthAgo }.sumOf { it.quantitySold }

        // Calculate current stock
        val totalIn = milkInEntries.filter { it.date == today }.sumOf { it.liters }
        val totalOut = milkOutEntries.filter { it.date == today }.sumOf { it.quantitySold }
        val totalSpoilt = milkSpoiltEntries.filter { it.date == today }.sumOf { it.amountSpoilt }
        val currentStock = (totalIn - totalOut - totalSpoilt).coerceAtLeast(0.0)

        _stockSummary.value = StockSummary(
            currentStock = currentStock,
            dailyProduce = dailyProduce,
            dailyTotalLitersSold = dailyTotalLitersSold,
            weeklySold = weeklySold,
            weeklySpoilt = weeklySpoilt,
            monthlySold = monthlySold
        )
    }

    private suspend fun loadWeeklyTrends(today: LocalDate) {
        val weeklyData = mutableListOf<DailyStockData>()

        for (i in 6 downTo 0) {
            val date = today.minus(DatePeriod(days = i))
            val milkInEntries = milkInEntryRepository.getAllMilkInEntries()
            val milkOutEntries = milkOutEntryRepository.getAllMilkOutEntries()

            val dailyIn = milkInEntries.filter { it.date == date }.sumOf { it.liters }
            val dailyOut = milkOutEntries.filter { it.date == date }.sumOf { it.quantitySold }

            weeklyData.add(
                DailyStockData(
                    date = date,
                    milkIn = dailyIn,
                    milkOut = dailyOut,
                    remainingStock = (dailyIn - dailyOut).coerceAtLeast(0.0)
                )
            )
        }

        _weeklyTrends.value = weeklyData
    }

    private suspend fun calculateMonthlyData(today: LocalDate) {
        val monthAgo = today.minus(DatePeriod(months = 1))
        val milkInEntries = milkInEntryRepository.getAllMilkInEntries()
        val milkOutEntries = milkOutEntryRepository.getAllMilkOutEntries()
        val milkSpoiltEntries = milkSpoiltEntryRepository.getAllMilkSpoiltEntries()

        val monthlyIn = milkInEntries.filter { it.date >= monthAgo }.sumOf { it.liters }
        val monthlyOut = milkOutEntries.filter { it.date >= monthAgo }.sumOf { it.quantitySold }
        val monthlySpoilt =
            milkSpoiltEntries.filter { it.date >= monthAgo }.sumOf { it.amountSpoilt }

        _monthlyData.value = MonthlyStockSummary(
            totalProduced = monthlyIn,
            totalSold = monthlyOut,
            totalSpoilt = monthlySpoilt,
            averageDailyProduction = monthlyIn / 30,
            averageDailySales = monthlyOut / 30
        )
    }

    fun refreshData() {
        loadStockData()
    }

    // Method to update inventory when milk operations occur
    fun onMilkInAdded(entry: MilkInEntry) {
        inventoryManager.onMilkInAdded(entry)
        refreshData()
    }

    fun onMilkOutSold(entry: MilkOutEntry) {
        inventoryManager.onMilkOutSold(entry)
        refreshData()
    }

    fun onMilkSpoiled(entry: MilkSpoiltEntry) {
        inventoryManager.onMilkSpoiled(entry)
        refreshData()
    }
}
