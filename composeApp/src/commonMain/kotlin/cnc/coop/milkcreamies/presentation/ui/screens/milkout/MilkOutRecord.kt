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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cnc.coop.milkcreamies.models.MilkOutEntry
import cnc.coop.milkcreamies.models.PaymentMode
import cnc.coop.milkcreamies.presentation.ui.common.components.ErrorDialog
import cnc.coop.milkcreamies.presentation.ui.common.components.ExportFormatDialog
import cnc.coop.milkcreamies.presentation.ui.common.components.LoadingDialog
import cnc.coop.milkcreamies.presentation.ui.common.components.SummaryCard
import cnc.coop.milkcreamies.presentation.ui.common.components.TopHeader
import cnc.coop.milkcreamies.presentation.viewmodel.milk.MilkOutViewModel
import cnc.coop.milkcreamies.presentation.viewmodel.milk.ValidationResult
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.koin.compose.koinInject
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilkSalesScreen(viewModel: MilkOutViewModel = koinInject()) {
    val milkOutEntries by viewModel.milkOutEntries.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val currentInventory by viewModel.currentStock.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var searchTerm by remember { mutableStateOf("") }
    var filterPayment by remember { mutableStateOf("All") } // "All", "Cash", "Mpesa"
    var showAddSaleDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Get current available stock
    val availableStock = currentInventory.currentStock

    // Filter sales based on search and filters
    val filteredSales = milkOutEntries.filter { entry ->
        val matchesSearch = entry.customerName.contains(searchTerm, ignoreCase = true)
        val matchesPayment = filterPayment == "All" ||
                (filterPayment == "Cash" && entry.paymentMode == PaymentMode.CASH) ||
                (filterPayment == "Mpesa" && entry.paymentMode == PaymentMode.MPESA)

        matchesSearch && matchesPayment
    }

    // Calculate summary statistics
    val totalLitersSold = milkOutEntries.sumOf { it.quantitySold }
    val totalSales = milkOutEntries.size
    val averageSaleAmount = if (totalSales > 0) totalLitersSold / totalSales else 0.0
    val cashSales = milkOutEntries.count { it.paymentMode == PaymentMode.CASH }
    val todayTotalLitres = milkOutEntries
        .filter { it.date == Clock.System.todayIn(TimeZone.currentSystemDefault()) }
        .sumOf { it.quantitySold }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopHeader(
            onAddSale = { showAddSaleDialog = true },
            currentTitle = "Milk Sales", 
            subTitle = "Track and manage milk sales data",
            onExport = { showExportDialog = true }
        )


        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Error Message
            errorMessage?.let { errorMsgString ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMsgString,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            // Summary Cards
            item {
                SummaryCardsRow(
                    totalLitersSold = totalLitersSold,
                    totalSales = totalSales,
                    averageSaleAmount = averageSaleAmount,
                    todayTotalLitres = todayTotalLitres
                )
            }

            // Search and Filters
            item {
                SearchAndFilters(
                    searchTerm = searchTerm,
                    onSearchChange = { searchTerm = it },
                    filterPayment = filterPayment,
                    onFilterChange = { filterPayment = it }
                )
            }

            // Sales Table Header
            item {
                SalesTableHeader(filteredCount = filteredSales.size, totalCount = milkOutEntries.size)
            }

            // Sales Items
            items(filteredSales) { entry ->
                SaleCard(
                    entry = entry,
                    customerName = entry.customerName
                )
            }

            // Empty State
            if (filteredSales.isEmpty()) {
                item {
                    EmptyState()
                }
            }
        }
    }

    // Show loading dialog if data is being loaded
    if (isLoading) {
        LoadingDialog(message = "Processing...")
    }

    // Show export format dialog when requested
    if (showExportDialog) {
        ExportFormatDialog(
            onDismissRequest = { showExportDialog = false },
            onFormatSelected = { format ->
                viewModel.exportSalesData(format)
                showExportDialog = false
            }
        )
    }

    // Add Sale Dialog
    if (showAddSaleDialog) {
        AddSaleDialog(
            viewModel = viewModel,
            availableStock = availableStock,
            onDismiss = { showAddSaleDialog = false },
            onAddSale = { customerName, quantity, pricePerLiter, paymentMode ->
                viewModel.addMilkOutEntry(
                    customerName = customerName,
                    quantitySold = quantity,
                    pricePerLiter = pricePerLiter,
                    paymentMode = paymentMode,
                    date = Clock.System.todayIn(TimeZone.currentSystemDefault())
                )
                showAddSaleDialog = false
            }
        )
    }

    // Show error dialog if there's an error message
    errorMessage?.let {
        ErrorDialog(
            message = it,
            onDismiss = { viewModel.clearErrorMessage() }
        )
    }
}

@Composable
fun SummaryCardsRow(
    totalLitersSold: Double,
    totalSales: Int,
    averageSaleAmount: Double,
    todayTotalLitres: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryCard(
            title = "Total Volume",
            value = "${(totalLitersSold * 10).roundToInt() / 10.0}L",
            subtitle = "Milk sold",
            icon = Icons.Default.Water,
            iconColor = Color(0xFF3B82F6),
            backgroundColor = Color(0xFFDBEAFE),
            modifier = Modifier.weight(1f)
        )

        SummaryCard(
            title = "Total Sales",
            value = totalSales.toString(),
            subtitle = "Transactions",
            icon = Icons.Default.People,
            iconColor = Color(0xFF059669),
            backgroundColor = Color(0xFFD1FAE5),
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryCard(
            title = "Avg Sale",
            value = "${(averageSaleAmount * 10).roundToInt() / 10.0}L",
            subtitle = "Per transaction",
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            iconColor = Color(0xFF7C3AED),
            backgroundColor = Color(0xFFEDE9FE),
            modifier = Modifier.weight(1f)
        )

        SummaryCard(
            title = "Today's Volume",
            value = "${(todayTotalLitres * 10).roundToInt() / 10.0}L",
            subtitle = "Today's sales",
            icon = Icons.Default.Money,
            iconColor = Color(0xFFEA580C),
            backgroundColor = Color(0xFFFED7AA),
            modifier = Modifier.weight(1f)
        )
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchAndFilters(
    searchTerm: String,
    onSearchChange: (String) -> Unit,
    filterPayment: String,
    onFilterChange: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // Search Field and Filter Button in the same row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Search Field
            OutlinedTextField(
                value = searchTerm,
                onValueChange = onSearchChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search customers...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                singleLine = true
            )

            // Filter Dropdown - Smaller button size
            var expandedPayment by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = expandedPayment,
                onExpandedChange = { expandedPayment = !expandedPayment }
            ) {
                Button(
                    onClick = { expandedPayment = true },
                    modifier = Modifier
                        .menuAnchor()
                        .height(56.dp)
                        .widthIn(min = 120.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = when (filterPayment) {
                            "All" -> "All Payments"
                            "Cash" -> "Cash"
                            "Mpesa" -> "Mpesa"
                            else -> "All Payments"
                        },
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (expandedPayment) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Filter",
                        modifier = Modifier.size(16.dp)
                    )
                }

                ExposedDropdownMenu(
                    expanded = expandedPayment,
                    onDismissRequest = { expandedPayment = false }
                ) {
                    listOf("All", "Cash", "Mpesa").forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(when(option) {
                                    "All" -> "All Payments"
                                    else -> option
                                })
                            },
                            onClick = {
                                onFilterChange(option)
                                expandedPayment = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SalesTableHeader(filteredCount: Int, totalCount: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Sales Records",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111827)
            )
            Text(
                text = "Showing $filteredCount of $totalCount sales",
                fontSize = 14.sp,
                color = Color(0xFF6B7280)
            )
        }
    }
}

@Composable
fun SaleCard(
    entry: MilkOutEntry,
    customerName: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle sale item click */ },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Customer and Date Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = customerName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = "ID: ${entry.saleId}",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF9CA3AF)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${entry.date.dayOfMonth}/${entry.date.monthNumber}/${entry.date.year}",
                        fontSize = 14.sp,
                        color = Color(0xFF111827)
                    )
                }
            }

            // Amount and Price Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem(
                    label = "Amount",
                    value = "${(entry.quantitySold * 10).roundToInt() / 10.0}L",
                    icon = Icons.Default.Water,
                    iconTint = Color(0xFF3B82F6)
                )

                InfoItem(
                    label = "Price/Liter",
                    value = "KES ${(entry.pricePerLiter * 100).roundToInt() / 100.0}",
                    icon = Icons.Default.Money,
                    iconTint = Color(0xFF059669)
                )

                InfoItem(
                    label = "Total",
                    value = "KES ${(entry.quantitySold * entry.pricePerLiter).roundToInt()}",
                    icon = Icons.Default.AccountBalance,
                    iconTint = Color(0xFF059669)
                )
            }

            // Payment Method Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                PaymentMethodBadge(paymentMode = entry.paymentMode)
            }
        }
    }
}

@Composable
fun InfoItem(
    label: String,
    value: String,
    icon: ImageVector,
    iconTint: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = iconTint
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color(0xFF6B7280),
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF111827)
        )
    }
}

@Composable
fun PaymentMethodBadge(paymentMode: PaymentMode) {
    val (backgroundColor, textColor, text) = when (paymentMode) {
        PaymentMode.CASH -> Triple(
            Color(0xFFD1FAE5),
            Color(0xFF065F46),
            "Cash"
        )
        PaymentMode.MPESA -> Triple(
            Color(0xFFDBEAFE),
            Color(0xFF1E40AF),
            "Mpesa"
        )
    }

    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.People,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = Color(0xFF9CA3AF)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No sales found",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF111827)
        )
        Text(
            text = "Try adjusting your search or filter criteria.",
            fontSize = 14.sp,
            color = Color(0xFF6B7280),
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSaleDialog(
    viewModel: MilkOutViewModel,
    availableStock: Double,
    onDismiss: () -> Unit,
    onAddSale: (customerName: String, quantity: Double, pricePerLiter: Double, paymentMode: PaymentMode) -> Unit
) {
    val customers by viewModel.customers.collectAsState()

    var customerName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var pricePerLiter by remember { mutableStateOf("80.0") } // Fixed price at 80 bob
    var paymentMode by remember { mutableStateOf(PaymentMode.CASH) }
    var showPaymentDropdown by remember { mutableStateOf(false) }

    // Real-time validation
    val quantityValue = quantity.toDoubleOrNull() ?: 0.0
    val priceValue = pricePerLiter.toDoubleOrNull() ?: 80.0
    val validationResult = viewModel.validateSaleQuantity(quantityValue)
    val isValidQuantity = validationResult is ValidationResult.Valid
    val quantityError =
        if (validationResult is ValidationResult.Invalid) validationResult.message else null

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background, shape = RoundedCornerShape(4.dp))
                .padding(16.dp),
            shape = RoundedCornerShape(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Add New Sale",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Stock Information Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (availableStock > 0) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.errorContainer
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Inventory,
                                contentDescription = "Stock",
                                tint = if (availableStock > 0) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onErrorContainer
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Available Stock",
                                    fontSize = 12.sp,
                                    color = if (availableStock > 0) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onErrorContainer
                                    }
                                )
                                Text(
                                    text = "${availableStock.toInt()} Liters",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (availableStock > 0) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onErrorContainer
                                    }
                                )
                            }
                        }

                        if (availableStock <= 0) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "No Stock",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // Customer Name Input
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Customer Name") },
                    placeholder = { Text("Enter customer name") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = "Customer")
                    }
                )

                // Quantity Input with Real-time Validation
                OutlinedTextField(
                    value = quantity,
                    onValueChange = {
                        val newValue = it.filter { char -> char.isDigit() || char == '.' }
                        quantity = newValue
                    },
                    label = { Text("Quantity (Liters)") },
                    placeholder = { Text("Enter quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    isError = !isValidQuantity && quantity.isNotEmpty(),
                    supportingText = if (!isValidQuantity && quantity.isNotEmpty()) {
                        {
                            Text(
                                text = quantityError ?: "Invalid quantity",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    } else null,
                    leadingIcon = {
                        Icon(
                            Icons.Default.Water,
                            contentDescription = "Quantity",
                            tint = if (isValidQuantity || quantity.isEmpty()) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    }
                )

                // Price Input (Read-only)
                OutlinedTextField(
                    value = pricePerLiter,
                    onValueChange = { },
                    label = { Text("Price per Liter (KES)") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    enabled = false,
                    leadingIcon = {
                        Icon(Icons.Default.Money, contentDescription = "Price")
                    }
                )

                // Payment Mode Dropdown
                ExposedDropdownMenuBox(
                    expanded = showPaymentDropdown,
                    onExpandedChange = { showPaymentDropdown = !showPaymentDropdown },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = when (paymentMode) {
                            PaymentMode.CASH -> "Cash"
                            PaymentMode.MPESA -> "M-Pesa"
                        },
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Payment Mode") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showPaymentDropdown) },
                        leadingIcon = {
                            Icon(
                                imageVector = if (paymentMode == PaymentMode.CASH) Icons.Default.Money else Icons.Default.Phone,
                                contentDescription = "Payment"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = showPaymentDropdown,
                        onDismissRequest = { showPaymentDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Cash") },
                            onClick = {
                                paymentMode = PaymentMode.CASH
                                showPaymentDropdown = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Money, contentDescription = "Cash")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("M-Pesa") },
                            onClick = {
                                paymentMode = PaymentMode.MPESA
                                showPaymentDropdown = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = "M-Pesa")
                            }
                        )
                    }
                }

                // Total Calculation
                if (quantityValue > 0 && isValidQuantity) {
                    val total = quantityValue * priceValue
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Amount:",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "KES ${total.toInt()}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (customerName.isNotBlank() && isValidQuantity) {
                                onAddSale(
                                    customerName.trim(),
                                    quantityValue,
                                    priceValue,
                                    paymentMode
                                )
                            }
                        },
                        enabled = customerName.isNotBlank() &&
                                quantityValue > 0 &&
                                isValidQuantity &&
                                availableStock > 0,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Sale")
                    }
                }
            }
        }
    }
}
