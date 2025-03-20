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
import cnc.coop.milkcreamies.domain.repository.MilkOutEntryRepository
import cnc.coop.milkcreamies.domain.repository.CustomerRepository
import cnc.coop.milkcreamies.models.*
import cnc.coop.milkcreamies.presentation.viewmodel.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * ViewModel for Milk Out screen following MVVM best practices
 * Now integrated with InventoryManager for proper stock validation
 */
class MilkOutViewModel(
    private val milkOutEntryRepository: MilkOutEntryRepository,
    private val customerRepository: CustomerRepository,
    private val inventoryManager: InventoryManager
) : BaseViewModel() {

    // Milk Out entries state
    private val _milkOutEntries = MutableStateFlow<List<MilkOutEntry>>(emptyList())
    val milkOutEntries: StateFlow<List<MilkOutEntry>> = _milkOutEntries.asStateFlow()

    // Available customers state (read-only from server)
    private val _customers = MutableStateFlow<List<Customer>>(emptyList())
    val customers: StateFlow<List<Customer>> = _customers.asStateFlow()

    // Current stock from InventoryManager (single source of truth)
    val currentStock = inventoryManager.currentInventory

    init {
        loadAllData()
    }

    fun loadAllData() {
        executeWithLoading {
            loadMilkOutEntries()
            loadCustomers()
        }
    }

    private suspend fun loadMilkOutEntries() {
        val entries = milkOutEntryRepository.getAllMilkOutEntries()
        _milkOutEntries.value = entries
    }

    private suspend fun loadCustomers() {
        val customersList = customerRepository.getAllCustomers()
        _customers.value = customersList
    }

    fun addMilkOutEntry(
        customerName: String,
        quantitySold: Double,
        pricePerLiter: Double,
        paymentMode: PaymentMode,
        date: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    ) {
        executeWithLoading {
            try {
                val availableStock = inventoryManager.getCurrentStock()

                // Comprehensive validation
                when {
                    customerName.isBlank() -> {
                        setError("Customer name cannot be empty.")
                        return@executeWithLoading
                    }

                    quantitySold <= 0 -> {
                        setError("Sale quantity must be positive.")
                        return@executeWithLoading
                    }

                    pricePerLiter <= 0 -> {
                        setError("Price per liter must be positive.")
                        return@executeWithLoading
                    }

                    quantitySold > availableStock -> {
                        setError("Insufficient stock! Available: ${availableStock.toInt()}L, Requested: ${quantitySold.toInt()}L")
                        return@executeWithLoading
                    }

                    // Additional business rule: prevent sales that would leave negative stock
                    availableStock - quantitySold < 0 -> {
                        setError("Sale would result in negative stock. Available: ${availableStock.toInt()}L")
                        return@executeWithLoading
                    }
                }

                val newEntry = MilkOutEntry(
                    saleId = null, // Let server generate in SL## format
                    customerId = null, // Let server generate based on customerName
                    customerName = customerName.trim(),
                    date = date,
                    quantitySold = quantitySold,
                    pricePerLiter = pricePerLiter,
                    paymentMode = paymentMode
                )

                // Add to repository first
                milkOutEntryRepository.addMilkOutEntry(newEntry)

                // Update inventory manager with the sale
                inventoryManager.onMilkOutSold(newEntry)

                // Refresh data
                loadMilkOutEntries()
                loadCustomers()
                clearError()

            } catch (e: Exception) {
                setError("Failed to add milk sale: ${e.message}")
            }
        }
    }

    fun refreshData() {
        loadAllData()
    }


    /**
     * Validate if a sale quantity is possible
     */
    fun validateSaleQuantity(quantity: Double): ValidationResult {
        val availableStock = inventoryManager.getCurrentStock()

        return when {
            quantity <= 0 -> ValidationResult.Invalid("Quantity must be greater than 0")
            quantity > availableStock -> ValidationResult.Invalid(
                "Insufficient stock! Available: ${availableStock.toInt()}L, Requested: ${quantity.toInt()}L"
            )

            else -> ValidationResult.Valid
        }
    }

    /**
     * Export sales data to the specified format
     *
     * @param format The format to export in (CSV, Excel, PDF)
     */
    fun exportSalesData(format: cnc.coop.milkcreamies.util.FileUtil.ExportFormat) {
        executeWithLoading {
            try {
                val entries = milkOutEntryRepository.getAllMilkOutEntries()

                // Generate a timestamped filename
                val filename = cnc.coop.milkcreamies.util.FileUtil.generateTimestampedFilename(
                    "milk_sales", format.extension
                )

                // Create the data content
                val csvContent =
                    cnc.coop.milkcreamies.util.CsvExportUtil.exportMilkOutEntriesToCSV(entries)

                // Convert to the selected format if needed
                val finalContent = when (format) {
                    cnc.coop.milkcreamies.util.FileUtil.ExportFormat.PDF -> {
                        // Use PdfExportUtil for PDF format
                        cnc.coop.milkcreamies.util.PdfExportUtil.convertCsvToPdfText(
                            csvContent = csvContent,
                            title = "Milk Sales Report"
                        )
                    }

                    cnc.coop.milkcreamies.util.FileUtil.ExportFormat.EXCEL -> {
                        // In a real app, we would convert CSV to Excel here
                        // For demo, just add a header
                        "EXCEL EXPORT - Milk Sales\n\n$csvContent"
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
                        setError("Sales data exported as ${format.extension.uppercase()} successfully!")
                    } else {
                        setError("Failed to export sales data: ${result.exceptionOrNull()?.message}")
                    }
                } else {
                    // Fallback to console output
                    clearError()
                    setError("Sales data exported to console. Check logs to copy the data.")
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
}

/**
 * Validation result for sale operations
 */
sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val message: String) : ValidationResult()
}
