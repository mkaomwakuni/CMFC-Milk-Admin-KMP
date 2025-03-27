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
package cnc.coop.milkcreamies.data

import cnc.coop.milkcreamies.models.*
import kotlinx.datetime.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Real-time inventory management system that tracks milk stock levels
 * and automatically updates based on milk entries, sales, and spoilage.
 * This serves as the single source of truth for all inventory-related data.
 */
class InventoryManager {

    private val _currentInventory = MutableStateFlow(
        MilkInventory(
            currentStock = 0.0,
            lastUpdated = LocalDate(2025, 6, 10)
        )
    )
    val currentInventory: StateFlow<MilkInventory> = _currentInventory.asStateFlow()

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

    private val _earningsSummary = MutableStateFlow(
        EarningsSummary(
            todayEarnings = 0.0,
            weeklyEarnings = 0.0,
            monthlyEarnings = 0.0
        )
    )
    val earningsSummary: StateFlow<EarningsSummary> = _earningsSummary.asStateFlow()

    private var _isInitialized = false

    /**
     * Initialize InventoryManager with fresh server data
     * This should be called on app start and periodically for refresh
     */
    fun initializeWithServerData(stockSummary: StockSummary, earningsSummary: EarningsSummary) {
        _currentInventory.value = _currentInventory.value.copy(
            currentStock = stockSummary.currentStock,
            lastUpdated = LocalDate(2025, 6, 10) // Should use actual server timestamp
        )

        _stockSummary.value = stockSummary
        _earningsSummary.value = earningsSummary
        _isInitialized = true
    }

    /**
     * Updates server data while preserving any real-time changes
     * This should be called when fresh server data is received
     */
    fun updateWithServerData(stockSummary: StockSummary, earningsSummary: EarningsSummary) {
        // Update base server data while preserving current stock level if it's been modified
        val currentStock =
            if (_isInitialized) _currentInventory.value.currentStock else stockSummary.currentStock

        _currentInventory.value = _currentInventory.value.copy(
            currentStock = currentStock,
            lastUpdated = LocalDate(2025, 6, 10)
        )

        _stockSummary.value = stockSummary.copy(currentStock = currentStock)
        _earningsSummary.value = earningsSummary
    }

    /**
     * Updates inventory when milk is added through milk-in entry
     */
    fun onMilkInAdded(entry: MilkInEntry) {
        val currentStock = _currentInventory.value.currentStock
        val newStock = currentStock + entry.liters

        // Update inventory
        _currentInventory.value = _currentInventory.value.copy(
            currentStock = newStock,
            lastUpdated = entry.date
        )

        // Update daily produce in stock summary
        val currentSummary = _stockSummary.value
        _stockSummary.value = currentSummary.copy(
            currentStock = newStock,
            dailyProduce = currentSummary.dailyProduce + entry.liters
        )
    }

    /**
     * Updates inventory when milk is sold through milk-out entry
     */
    fun onMilkOutSold(entry: MilkOutEntry) {
        val currentStock = _currentInventory.value.currentStock
        val newStock = (currentStock - entry.quantitySold).coerceAtLeast(0.0)

        // Calculate earnings
        val saleAmount = entry.quantitySold * entry.pricePerLiter

        // Update inventory
        _currentInventory.value = _currentInventory.value.copy(
            currentStock = newStock,
            lastUpdated = entry.date
        )

        // Update stock and earnings summaries
        val currentStockSummary = _stockSummary.value
        val currentEarningsSummary = _earningsSummary.value

        _stockSummary.value = currentStockSummary.copy(
            currentStock = newStock,
            dailyTotalLitersSold = currentStockSummary.dailyTotalLitersSold + entry.quantitySold
        )

        _earningsSummary.value = currentEarningsSummary.copy(
            todayEarnings = currentEarningsSummary.todayEarnings + saleAmount
        )
    }

    /**
     * Updates inventory when milk is spoiled
     */
    fun onMilkSpoiled(entry: MilkSpoiltEntry) {
        val currentStock = _currentInventory.value.currentStock
        val newStock = (currentStock - entry.amountSpoilt).coerceAtLeast(0.0)

        // Update inventory
        _currentInventory.value = _currentInventory.value.copy(
            currentStock = newStock,
            lastUpdated = entry.date
        )

        // Update spoilage in stock summary
        val currentSummary = _stockSummary.value
        _stockSummary.value = currentSummary.copy(
            currentStock = newStock,
            weeklySpoilt = currentSummary.weeklySpoilt + entry.amountSpoilt
        )
    }

    /**
     * Calculates cow's average daily milk production based on recent entries
     */
    fun calculateCowAverageProduction(
        cowId: String,
        milkEntries: List<MilkInEntry>,
        days: Int = 30
    ): Double {
        val recentEntries = milkEntries
            .filter { it.cowId == cowId }
            .takeLast(days * 2) // morning and evening for each day

        if (recentEntries.isEmpty()) return 0.0

        // Group by date and sum daily totals
        val dailyTotals = recentEntries
            .groupBy { it.date }
            .mapValues { (_, entries) -> entries.sumOf { it.liters } }

        return if (dailyTotals.isNotEmpty()) {
            dailyTotals.values.average()
        } else 0.0
    }

    /**
     * Calculates member's total daily production from all their active cows
     */
    fun calculateMemberDailyProduction(
        ownerId: String,
        milkEntries: List<MilkInEntry>,
        activeCows: List<Cow>,
        targetDate: LocalDate = LocalDate(2025, 6, 10)
    ): Double {
        val memberCowIds = activeCows
            .filter { it.ownerId == ownerId && it.isActive }
            .map { it.cowId }

        return milkEntries
            .filter { it.ownerId == ownerId && it.date == targetDate }
            .filter { it.cowId in memberCowIds }
            .sumOf { it.liters }
    }

    /**
     * Gets current stock level
     */
    fun getCurrentStock(): Double = _currentInventory.value.currentStock

    /**
     * Resets daily statistics (to be called at day change)
     */
    fun resetDailyStats() {
        _stockSummary.value = _stockSummary.value.copy(
            dailyProduce = 0.0,
            dailyTotalLitersSold = 0.0
        )

        _earningsSummary.value = _earningsSummary.value.copy(
            todayEarnings = 0.0
        )
    }

    /**
     * Updates weekly and monthly statistics
     */
    fun updateWeeklyMonthlyStats(
        weeklyMilkOut: List<MilkOutEntry>,
        monthlyMilkOut: List<MilkOutEntry>,
        weeklySpoilage: List<MilkSpoiltEntry>
    ) {
        val weeklySold = weeklyMilkOut.sumOf { it.quantitySold }
        val monthlySold = monthlyMilkOut.sumOf { it.quantitySold }
        val weeklySpoilt = weeklySpoilage.sumOf { it.amountSpoilt }

        val weeklyEarnings = weeklyMilkOut.sumOf { it.quantitySold * it.pricePerLiter }
        val monthlyEarnings = monthlyMilkOut.sumOf { it.quantitySold * it.pricePerLiter }

        _stockSummary.value = _stockSummary.value.copy(
            weeklySold = weeklySold,
            monthlySold = monthlySold,
            weeklySpoilt = weeklySpoilt
        )

        _earningsSummary.value = _earningsSummary.value.copy(
            weeklyEarnings = weeklyEarnings,
            monthlyEarnings = monthlyEarnings
        )
    }
}
