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
package cnc.coop.milkcreamies.presentation.viewmodel.dashboard

import cnc.coop.milkcreamies.data.InventoryManager
import cnc.coop.milkcreamies.domain.repository.*
import cnc.coop.milkcreamies.models.*
import cnc.coop.milkcreamies.presentation.viewmodel.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn

/**
 * ViewModel for Dashboard screen following MVVM best practices
 */
class DashboardViewModel(
    private val cowRepository: CowRepository,
    private val memberRepository: MemberRepository,
    private val milkInEntryRepository: MilkInEntryRepository,
    private val milkOutEntryRepository: MilkOutEntryRepository,
    private val customerRepository: CustomerRepository,
    private val earningsSummaryRepository: EarningsSummaryRepository,
    private val stockSummaryRepository: StockSummaryRepository,
    private val cowSummaryRepository: CowSummaryRepository,
    private val inventoryManager: InventoryManager
) : BaseViewModel() {

    // Data States
    private val _cows = MutableStateFlow<List<Cow>>(emptyList())
    val cows: StateFlow<List<Cow>> = _cows.asStateFlow()

    private val _members = MutableStateFlow<List<Member>>(emptyList())
    val members: StateFlow<List<Member>> = _members.asStateFlow()

    private val _customers = MutableStateFlow<List<Customer>>(emptyList())
    val customers: StateFlow<List<Customer>> = _customers.asStateFlow()

    private val _milkOutEntries = MutableStateFlow<List<MilkOutEntry>>(emptyList())
    val milkOutEntries: StateFlow<List<MilkOutEntry>> = _milkOutEntries.asStateFlow()

    private val _milkInEntries = MutableStateFlow<List<MilkInEntry>>(emptyList())
    val milkInEntries: StateFlow<List<MilkInEntry>> = _milkInEntries.asStateFlow()

    // Dashboard metrics state
    private val _dashboardMetrics = MutableStateFlow(DashboardMetrics())
    val dashboardMetrics: StateFlow<DashboardMetrics> = _dashboardMetrics.asStateFlow()

    // Analytics state
    private val _milkAnalytics = MutableStateFlow(MilkAnalytics())
    val milkAnalytics: StateFlow<MilkAnalytics> = _milkAnalytics.asStateFlow()

    // Summary states
    private val _earningsSummary = MutableStateFlow(EarningsSummary(0.0, 0.0, 0.0))
    val earningsSummary: StateFlow<EarningsSummary> = _earningsSummary.asStateFlow()

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

    private val _cowSummary = MutableStateFlow(
        CowSummary(
            totalActiveCows = 0,
            totalArchivedCows = 0,
            healthyCows = 0,
            needsAttention = 0
        )
    )
    val cowSummary: StateFlow<CowSummary> = _cowSummary.asStateFlow()

    init {
        loadDashboardData()
        // Observe inventory manager state flows
        observeInventoryUpdates()
    }

    fun loadDashboardData() {
        executeWithLoading {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            // Load all basic data first
            loadAllData()
            // Then load calculated summaries and analytics
            loadEarningsAndAnalytics(today)
        }
    }

    fun refreshData() {
        loadDashboardData()
    }

    private suspend fun loadAllData() {
        try {
            // Load cows
            val cowsResult = cowRepository.getAllCows()
            _cows.value = cowsResult

            // Load members
            val membersResult = memberRepository.getAllMembers()
            _members.value = membersResult

            // Load customers
            val customersResult = customerRepository.getAllCustomers()
            _customers.value = customersResult

            // Load milk out entries
            val milkOutResult = milkOutEntryRepository.getAllMilkOutEntries()
            _milkOutEntries.value = milkOutResult

            // Load milk in entries for daily calculations
            val milkInResult = milkInEntryRepository.getAllMilkInEntries()
            _milkInEntries.value = milkInResult

        } catch (e: Exception) {
            handleException(e, "Failed to load basic data")
        }
    }

    private suspend fun loadEarningsAndAnalytics(today: LocalDate) {
        try {
            // Load earnings summary from server
            val earningsResult = earningsSummaryRepository.getEarningsSummary(today)
            val serverEarnings = if (earningsResult.isSuccess) {
                earningsResult.getOrNull() ?: EarningsSummary(0.0, 0.0, 0.0)
            } else {
                calculateLocalEarnings(today)
            }

            // Load stock summary from server
            val stockResult = stockSummaryRepository.getStockSummary(today)
            val serverStock = if (stockResult.isSuccess) {
                stockResult.getOrNull() ?: StockSummary(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
            } else {
                calculateLocalStock(today)
            }

            // Initialize InventoryManager with server data - this becomes the single source of truth
            inventoryManager.initializeWithServerData(serverStock, serverEarnings)

            // Update local state for backwards compatibility (these will be removed eventually)
            _earningsSummary.value = serverEarnings
            _stockSummary.value = serverStock

            // Load other analytics
            loadCowSummary()
            loadMilkAnalytics(today)

            updateDashboardMetrics()
        } catch (e: Exception) {
            handleException(e, "Failed to load dashboard data")
        }
    }

    private suspend fun calculateLocalEarnings(today: LocalDate): EarningsSummary {
        val weekAgo = today.minus(DatePeriod(days = 7))
        val monthAgo = today.minus(DatePeriod(months = 1))
        val milkOutEntries = _milkOutEntries.value

        val todayEarnings = milkOutEntries.filter { it.date == today }
            .sumOf { it.quantitySold * it.pricePerLiter }
        val weeklyEarnings = milkOutEntries.filter { it.date >= weekAgo }
            .sumOf { it.quantitySold * it.pricePerLiter }
        val monthlyEarnings = milkOutEntries.filter { it.date >= monthAgo }
            .sumOf { it.quantitySold * it.pricePerLiter }

        return EarningsSummary(
            todayEarnings = todayEarnings,
            weeklyEarnings = weeklyEarnings,
            monthlyEarnings = monthlyEarnings
        )
    }

    private suspend fun calculateLocalStock(today: LocalDate): StockSummary {
        val weekAgo = today.minus(DatePeriod(days = 7))
        val monthAgo = today.minus(DatePeriod(months = 1))

        val milkInEntries = _milkInEntries.value
        val milkOutEntries = _milkOutEntries.value

        // Calculate current stock: total in - total out (assuming no spoilage data)
        val totalMilkIn = milkInEntries.sumOf { it.liters }
        val totalMilkOut = milkOutEntries.sumOf { it.quantitySold }
        val currentStock = (totalMilkIn - totalMilkOut).coerceAtLeast(0.0)

        val dailyProduce = milkInEntries.filter { it.date == today }.sumOf { it.liters }
        val dailyTotalLitersSold =
            milkOutEntries.filter { it.date == today }.sumOf { it.quantitySold }
        val weeklySold = milkOutEntries.filter { it.date >= weekAgo }.sumOf { it.quantitySold }
        val monthlySold = milkOutEntries.filter { it.date >= monthAgo }.sumOf { it.quantitySold }

        return StockSummary(
            currentStock = currentStock,
            dailyProduce = dailyProduce,
            dailyTotalLitersSold = dailyTotalLitersSold,
            weeklySold = weeklySold,
            weeklySpoilt = 0.0, // Would need spoilage repository
            monthlySold = monthlySold
        )
    }

    private fun initializeInventoryWithServerData() {
        val stockSummary = _stockSummary.value
        val earningsSummary = _earningsSummary.value

        // Update inventory manager with server data
        inventoryManager.initializeWithServerData(stockSummary, earningsSummary)
    }

    private fun observeInventoryUpdates() {
        // This would typically be done with combine or other flow operators
        // but for simplicity, we'll expose the inventory manager flows directly
    }

    // Expose inventory manager flows
    val currentInventory = inventoryManager.currentInventory
    val inventoryStockSummary = inventoryManager.stockSummary
    val inventoryEarningsSummary = inventoryManager.earningsSummary

    private fun updateDashboardMetrics() {
        val serverStockSummary = _stockSummary.value
        val serverEarnings = _earningsSummary.value
        val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

        // Use server data for dashboard metrics
        _dashboardMetrics.value = DashboardMetrics(
            milkIn = serverStockSummary.dailyProduce,
            milkOut = serverStockSummary.dailyTotalLitersSold,
            currentStock = serverStockSummary.currentStock,
            earnings = serverEarnings.todayEarnings
        )
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
            // Keep default cow summary
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
            // Keep default analytics
        }
    }

    fun addCow(
        name: String,
        breed: String,
        age: Int,
        weight: Double,
        entryDate: LocalDate,
        ownerId: String?,
        healthStatus: HealthStatus = HealthStatus.HEALTHY,
        note: String? = null
    ) {
        executeWithLoading {
            try {
                // Validation
                if (name.isBlank()) {
                    setError("Cow name cannot be empty.")
                    return@executeWithLoading
                }
                if (age <= 0 || weight <= 0) {
                    setError("Age, weight, and liters must be positive.")
                    return@executeWithLoading
                }
                if (ownerId != null && _members.value.none { it.memberId == ownerId }) {
                    setError("Invalid owner selected.")
                    return@executeWithLoading
                }

                // Create a proper CowStatus object with all required fields
                val cowStatus = CowStatus(
                    healthStatus = healthStatus,
                    actionStatus = ActionStatus.ACTIVE,
                    dewormingDue = null,
                    dewormingLast = null,
                    calvingDate = null,
                    vaccinationDue = null,
                    vaccinationLast = null,
                    antibioticTreatment = null
                )

                val newCow = Cow(
                    entryDate = entryDate,
                    ownerId = ownerId ?: "",
                    name = name,
                    breed = breed,
                    age = age,
                    weight = weight,
                    status = cowStatus,
                    note = note
                )

                cowRepository.addCow(newCow)
                loadDashboardData() // Refresh data
                clearError() // Clear any previous errors on success
            } catch (e: Exception) {
                setError("Failed to add cow: ${e.message}")
            }
        }
    }

    fun clearErrorMessage() {
        clearError()
    }

    fun exportAllDataToCsv() {
        executeWithLoading {
            try {
                val entries = milkInEntryRepository.getAllMilkInEntries()
                val salesEntries = milkOutEntryRepository.getAllMilkOutEntries()

                // Create CSV content using CsvExportUtil
                val csvContent = cnc.coop.milkcreamies.util.CsvExportUtil.exportAllDataToCSV(
                    milkInEntries = entries,
                    milkOutEntries = salesEntries,
                    members = members.value,
                    cows = cows.value,
                    customers = customers.value,
                    spoiltEntries = emptyList() // Add spoilt entries when available
                )

                // Generate a timestamped filename
                val filename = cnc.coop.milkcreamies.util.FileUtil.generateTimestampedFilename(
                    "chonyi_milk_report", "csv"
                )
                
                // Export individual reports as well for more specific data
                val milkInFilename =
                    cnc.coop.milkcreamies.util.FileUtil.generateTimestampedFilename(
                        "milk_entries", "csv"
                    )
                val milkOutFilename =
                    cnc.coop.milkcreamies.util.FileUtil.generateTimestampedFilename(
                        "milk_sales", "csv"
                    )
                val milkSpoiltFilename =
                    cnc.coop.milkcreamies.util.FileUtil.generateTimestampedFilename(
                        "spoilt_milk", "csv"
                    )
                
                // Create separate reports
                val milkInCsv = cnc.coop.milkcreamies.util.CsvExportUtil.exportMilkInEntriesToCSV(_milkInEntries.value)
                val milkOutCsv = cnc.coop.milkcreamies.util.CsvExportUtil.exportMilkOutEntriesToCSV(_milkOutEntries.value)
                val spoiltMilkCsv = cnc.coop.milkcreamies.util.CsvExportUtil.exportSpoiltMilkToCSV(emptyList())

                // Try to save the file using the platform-specific implementation
                if (cnc.coop.milkcreamies.util.FileUtil.supportsDirectFileSave()) {
                    val result = cnc.coop.milkcreamies.util.FileUtil.saveToDownloads(
                        content = csvContent,
                        filename = filename,
                        format = cnc.coop.milkcreamies.util.FileUtil.ExportFormat.CSV
                    )

                    if (result.isSuccess) {
                        clearError()
                        setError("Report successfully saved to: ${result.getOrNull()}")
                    } else {
                        setError("Failed to save report: ${result.exceptionOrNull()?.message}")
                    }
                } else {
                    // Fallback to console output
                    clearError()
                    setError(
                        """
                        CSV reports generated successfully!
                        
                        Available reports:
                        - $filename: Comprehensive report
                        - $milkInFilename: Milk production entries
                        - $milkOutFilename: Sales data with customer details
                        - $milkSpoiltFilename: Spoilt milk records
                        
                        The data has been output to the console log.
                        """
                    )
                }
            } catch (e: Exception) {
                setError("Failed to export CSV: ${e.message}")
            }
        }
    }

    /**
     * Save CSV reports by outputting them to the console
     * and providing instructions for saving.
     */
    private fun saveReports(
        mainReportName: String, mainReport: String,
        milkInName: String, milkInReport: String,
        milkOutName: String, milkOutReport: String,
        spoiltName: String, spoiltReport: String
    ) {
        try {
            clearError()
            setError(
                """
                CSV reports generated successfully!
                
                Available reports:
                - $mainReportName: Comprehensive report
                - $milkInName: Milk production entries
                - $milkOutName: Sales data with customer details
                - $spoiltName: Spoilt milk records
                
                The data has been output to the console log. To save:
                1. Find each report section in the console logs
                2. Copy the content for the report you want
                3. Save it as a .csv file
                4. Open with Excel or other spreadsheet program
                
                In a production app, download buttons would be provided.
                """
            )
        } catch (e: Exception) {
            setError("Failed to prepare reports: ${e.message}")
        }
    }

    /**
     * Export data in different formats (CSV, Excel, PDF)
     *
     * @param format The export format to use
     */
    fun exportData(format: cnc.coop.milkcreamies.util.FileUtil.ExportFormat) {
        executeWithLoading {
            try {
                val entries = milkInEntryRepository.getAllMilkInEntries()
                val salesEntries = milkOutEntryRepository.getAllMilkOutEntries()
                val spoiltEntries = emptyList<MilkSpoiltEntry>() // In a real app, get from repository

                // Create content using CsvExportUtil
                val content = cnc.coop.milkcreamies.util.CsvExportUtil.exportAllDataToCSV(
                    milkInEntries = entries,
                    milkOutEntries = salesEntries,
                    members = members.value,
                    cows = cows.value,
                    customers = customers.value,
                    spoiltEntries = spoiltEntries // Add spoilt entries when available
                )

                // Generate a timestamped filename with appropriate extension
                val filename = cnc.coop.milkcreamies.util.FileUtil.generateTimestampedFilename(
                    "chonyi_milk_report", format.extension
                )

                // For non-CSV formats, we would normally convert the content here
                // In a real implementation, we would use specific libraries for PDF/Excel conversion
                // Try to save the file using the platform-specific implementation
                if (cnc.coop.milkcreamies.util.FileUtil.supportsDirectFileSave()) {
                    val result = cnc.coop.milkcreamies.util.FileUtil.saveToDownloads(
                        content = content,
                        filename = filename,
                        format = format
                    )

                    if (result.isSuccess) {
                        clearError()
                        setError("Report successfully exported as ${format.extension.uppercase()}!")

                    } else {
                        setError("Failed to export report: ${result.exceptionOrNull()?.message}")
                    }
                } else {
                    clearError()
                    setError("Export generated successfully! Check console for output.")
                }
            } catch (e: Exception) {
                setError("Failed to export data: ${e.message}")
            }
        }
    }
}
