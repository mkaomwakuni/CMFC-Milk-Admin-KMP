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

import cnc.coop.milkcreamies.data.InventoryManager
import cnc.coop.milkcreamies.domain.repository.MilkInEntryRepository
import cnc.coop.milkcreamies.domain.repository.CowRepository
import cnc.coop.milkcreamies.domain.repository.MemberRepository
import cnc.coop.milkcreamies.models.*
import cnc.coop.milkcreamies.presentation.ui.screens.milk.MilkTab
import cnc.coop.milkcreamies.presentation.viewmodel.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * ViewModel for Milk In screen following MVVM best practices
 */
class MilkInViewModel(
    private val milkInEntryRepository: MilkInEntryRepository,
    private val cowRepository: CowRepository,
    private val memberRepository: MemberRepository,
    private val inventoryManager: InventoryManager
) : BaseViewModel() {

    // Milk In entries state
    private val _milkInEntries = MutableStateFlow<List<MilkInEntry>>(emptyList())
    val milkInEntries: StateFlow<List<MilkInEntry>> = _milkInEntries.asStateFlow()

    // Available cows state
    private val _cows = MutableStateFlow<List<Cow>>(emptyList())
    val cows: StateFlow<List<Cow>> = _cows.asStateFlow()

    // Available members state
    private val _members = MutableStateFlow<List<Member>>(emptyList())
    val members: StateFlow<List<Member>> = _members.asStateFlow()

    // Analytics state
    private val _milkAnalytics = MutableStateFlow(MilkAnalytics())
    val milkAnalytics: StateFlow<MilkAnalytics> = _milkAnalytics.asStateFlow()

    // Real-time inventory data from InventoryManager
    val currentInventory = inventoryManager.currentInventory
    val inventoryStockSummary = inventoryManager.stockSummary
    val inventoryEarningsSummary = inventoryManager.earningsSummary

    // State for current tab (IN or SPOILT)
    private val _milkTab = MutableStateFlow(MilkTab.IN)
    val milkTab: StateFlow<MilkTab> = _milkTab.asStateFlow()

    init {
        loadAllData()
    }

    fun loadAllData() {
        executeWithLoading {
            loadMilkInEntries()
            loadCows()
            loadMembers()
            loadMilkAnalytics()
        }
    }

    private suspend fun loadMilkInEntries() {
        val entries = milkInEntryRepository.getAllMilkInEntries()
        _milkInEntries.value = entries
    }

    private suspend fun loadCows() {
        val cowsList = cowRepository.getAllCows()
        _cows.value = cowsList
    }

    private suspend fun loadMembers() {
        val membersList = memberRepository.getAllMembers()
        _members.value = membersList
    }

    private suspend fun loadMilkAnalytics() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val analyticsResult = cowRepository.getMilkAnalytics(today)
        if (analyticsResult.isSuccess) {
            analyticsResult.getOrNull()?.let { analytics ->
                _milkAnalytics.value = analytics
            }
        } else {
            // If the server request fails, calculate analytics locally as a fallback
            calculateLocalAnalytics()
        }
    }

    private fun calculateLocalAnalytics() {
        val entries = _milkInEntries.value
        if (entries.isEmpty()) return

        val totalQuantity = entries.sumOf { it.liters }
        val totalEntries = entries.size
        val uniqueCowIds = entries.mapNotNull { it.cowId }.toSet()
        val uniqueCows = uniqueCowIds.size
        val avgQuantityPerEntry = if (totalEntries > 0) totalQuantity / totalEntries else 0.0

        // Group by date to create daily data
        val dailyData = entries.groupBy { it.date.toString() }
            .map { (date, entriesForDate) ->
                DailyProduction(
                    date = date,
                    quantity = entriesForDate.sumOf { it.liters },
                    entries = entriesForDate.size
                )
            }

        // Group by cow to create cow production data
        val cowData = entries.filter { it.cowId != null }
            .groupBy { it.cowId }
            .map { (cowId, entriesForCow) ->
                CowProduction(
                    cowId = cowId ?: "Unknown",
                    quantity = entriesForCow.sumOf { it.liters },
                    entries = entriesForCow.size
                )
            }

        _milkAnalytics.value = MilkAnalytics(
            totalQuantity = totalQuantity,
            totalEntries = totalEntries,
            uniqueCows = uniqueCows,
            avgQuantityPerEntry = avgQuantityPerEntry,
            dailyData = dailyData,
            cowData = cowData
        )
    }

    fun addMilkInEntry(
        cowId: String?,
        ownerId: String,
        quantityLiters: Double,
        date: LocalDate,
        milkingType: MilkingType = MilkingType.MORNING
    ) {
        executeWithLoading {
            try {
                // Validation
                when {
                    quantityLiters <= 0 -> {
                        setError("Milk quantity must be positive.")
                        return@executeWithLoading
                    }

                    cowId == null || cowId.isEmpty() || _cows.value.none { it.cowId == cowId } -> {
                        setError("Invalid cow selected.")
                        return@executeWithLoading
                    }

                    ownerId.isBlank() -> {
                        setError("Owner ID cannot be empty.")
                        return@executeWithLoading
                    }
                }

                // Create MilkInEntry object - repository will handle the conversion to MilkInRequest
                val newEntry = MilkInEntry(
                    entryId = null, // Server will generate ID
                    cowId = cowId,
                    ownerId = ownerId,
                    liters = quantityLiters,
                    date = date,
                    milkingType = milkingType
                )

                val createdEntry = milkInEntryRepository.addMilkInEntry(newEntry)
                
                // Update inventory manager with the new milk entry
                inventoryManager.onMilkInAdded(createdEntry)
                
                refreshData() // Refresh all data including analytics
                clearError()

            } catch (e: MilkCollectionException) {
                // Handle specific milk collection error with detailed information
                val errorMessage = buildString {
                    append("❌ Cannot collect milk from ${e.cowName ?: "cow"}")
                    appendLine()
                    append("🔸 Health Status: ${e.healthStatus}")
                    appendLine()
                    append("🔸 Reason: ${e.message}")
                    
                    if (e.blockedUntil?.isNotEmpty() == true) {
                        appendLine()
                        append("⏰ Blocked until: ${e.blockedUntil}")
                    }
                    
                    if (e.suggestions.isNotEmpty()) {
                        appendLine()
                        append("💡 Suggestions:")
                        e.suggestions.forEach { suggestion ->
                            appendLine()
                            append("  • $suggestion")
                        }
                    }
                }
                setError(errorMessage)
            } catch (e: Exception) {
                // Handle general errors and check for nested MilkCollectionException
                val cause = e.cause
                if (cause is cnc.coop.milkcreamies.models.MilkCollectionException) {
                    // Handle nested milk collection error
                    val errorMessage = buildString {
                        append("❌ Cannot collect milk from ${cause.cowName ?: "cow"}")
                        appendLine()
                        append("🔸 Health Status: ${cause.healthStatus}")
                        appendLine()
                        append("🔸 Reason: ${cause.message}")
                        
                        if (cause.blockedUntil?.isNotEmpty() == true) {
                            appendLine()
                            append("⏰ Blocked until: ${cause.blockedUntil}")
                        }
                        
                        if (cause.suggestions.isNotEmpty()) {
                            appendLine()
                            append("💡 Suggestions:")
                            cause.suggestions.forEach { suggestion ->
                                appendLine()
                                append("  • $suggestion")
                            }
                        }
                    }
                    setError(errorMessage)
                } else {
                    // Handle general errors
                    setError("Failed to add milk entry: ${e.message}")
                }
            }
        }
    }

    fun refreshData() {
        loadAllData()
    }

    /**
     * Check if a cow is eligible for milk collection before attempting to add an entry
     */
    fun checkCowEligibility(cowId: String, onResult: (Boolean, String?) -> Unit) {
        executeWithLoading(showLoading = false) {
            try {
                // First, try to get cow eligibility from server
                val result = cowRepository.getCowEligibility(cowId)

                if (result.isSuccess) {
                    val eligibility = result.getOrThrow()
                    onResult(eligibility.isEligible, eligibility.reason)
                } else {
                    val exception = result.exceptionOrNull()
                    // Fallback: Check cow health status locally
                    val cow = _cows.value.find { it.cowId == cowId }
                    if (cow != null) {
                        val (isEligible, reason) = checkCowHealthLocally(cow)
                        onResult(isEligible, reason)
                    } else {
                        onResult(false, "Cow not found")
                    }
                }
            } catch (e: Exception) {
                // Fallback: Check cow health status locally
                val cow = _cows.value.find { it.cowId == cowId }
                if (cow != null) {
                    val (isEligible, reason) = checkCowHealthLocally(cow)
                    onResult(isEligible, reason)
                } else {
                    onResult(false, "Unable to check cow eligibility: ${e.message}")
                }
            }
        }
    }

    /**
     * Get detailed health information for a cow including waiting periods
     */
    fun getCowHealthDetails(cowId: String, onResult: (CowHealthDetailsResponse?) -> Unit) {
        executeWithLoading(showLoading = false) {
            try {
                val result = cowRepository.getCowHealthDetails(cowId)
                if (result.isSuccess) {
                    onResult(result.getOrThrow())
                } else {
                    onResult(null)
                }
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }

    /**
     * Get bulk eligibility for multiple cows (useful for member-specific views)
     */
    fun getBulkCowEligibility(
        ownerId: String? = null,
        activeOnly: Boolean = true,
        onResult: (BulkEligibilityResponse?) -> Unit
    ) {
        executeWithLoading(showLoading = false) {
            try {
                val result = cowRepository.getBulkCowEligibility(ownerId, activeOnly)
                if (result.isSuccess) {
                    onResult(result.getOrThrow())
                } else {
                    onResult(null)
                }
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }

    /**
     * Local fallback method to check cow health status
     */
    private fun checkCowHealthLocally(cow: Cow): Pair<Boolean, String?> {
        return when (cow.status.healthStatus) {
            HealthStatus.HEALTHY,
            HealthStatus.NEEDS_ATTENTION,
            HealthStatus.GESTATION -> {
                Pair(true, null)
            }

            HealthStatus.UNDER_TREATMENT,
            HealthStatus.SICK -> {
                Pair(
                    false,
                    "Cannot collect milk: Cow is ${
                        cow.status.healthStatus.name.lowercase().replace("_", " ")
                    }"
                )
            }

            HealthStatus.VACCINATED -> {
                Pair(
                    false,
                    "Cannot collect milk: Cow was recently vaccinated. Wait 48 hours after vaccination."
                )
            }

            HealthStatus.ANTIBIOTICS -> {
                Pair(
                    false,
                    "Cannot collect milk: Cow is under antibiotic treatment. Wait 72 hours after treatment ends."
                )
            }
        }
    }

    /**
     * Export milk production data to the specified format
     *
     * @param format The format to export in (CSV, Excel, PDF)
     */
    fun exportMilkData(format: cnc.coop.milkcreamies.util.FileUtil.ExportFormat) {
        executeWithLoading {
            try {
                val milkInEntries = milkInEntryRepository.getAllMilkInEntries()
                val cowsData = cowRepository.getAllCows()
                val membersData = memberRepository.getAllMembers()

                // Generate a timestamped filename
                val filename = cnc.coop.milkcreamies.util.FileUtil.generateTimestampedFilename(
                    "milk_production", format.extension
                )

                // Create the data content based on the current tab
                val csvContent = if (milkTab.value == MilkTab.IN) {
                    // Generate milk production report
                    cnc.coop.milkcreamies.util.CsvExportUtil.exportMilkInEntriesToCSV(milkInEntries)
                } else {
                    // Generate spoilt milk report - in a real app we would load spoilt milk entries from repository
                    // For now, we're using an empty list as a placeholder
                    cnc.coop.milkcreamies.util.CsvExportUtil.exportSpoiltMilkToCSV(emptyList())
                }

                val reportTitle =
                    if (milkTab.value == MilkTab.IN) "Milk Production Report" else "Spoilt Milk Report"

                // Convert to the selected format if needed
                val finalContent = when (format) {
                    cnc.coop.milkcreamies.util.FileUtil.ExportFormat.PDF -> {
                        // Use PdfExportUtil for PDF format
                        cnc.coop.milkcreamies.util.PdfExportUtil.convertCsvToPdfText(
                            csvContent = csvContent,
                            title = reportTitle
                        )
                    }

                    cnc.coop.milkcreamies.util.FileUtil.ExportFormat.EXCEL -> {
                        // In a real app, we would convert CSV to Excel here
                        // For demo, just add a header
                        "EXCEL EXPORT - $reportTitle\n\n$csvContent"
                    }

                    else -> csvContent
                }

                // Try to save the file using the platform-specific implementation
                if (cnc.coop.milkcreamies.util.FileUtil.supportsDirectFileSave()) {
                    val result = cnc.coop.milkcreamies.util.FileUtil.saveToDownloads(
                        content = finalContent,
                        filename = filename,
                        format = format
                    )

                    if (result.isSuccess) {
                        clearError()
                        setError("$reportTitle exported as ${format.extension.uppercase()} successfully!")
                    } else {
                        setError("Failed to export data: ${result.exceptionOrNull()?.message}")
                    }
                } else {
                    // Fallback to console output
                    val contentToDisplay =
                        "===== $reportTitle EXPORT: $filename =====\n$finalContent\n===== END $reportTitle EXPORT ====="

                    clearError()
                    setError("Data exported to console. Check logs to copy the data.")
                }
            } catch (e: Exception) {
                setError("Export failed: ${e.message}")
            }
        }
    }

    /**
     * Clear the current error message
     */
    fun clearErrorMessage() {
        clearError()
    }

    /**
     * Update the current tab
     */
    fun updateTab(tab: MilkTab) {
        _milkTab.value = tab
    }
}
