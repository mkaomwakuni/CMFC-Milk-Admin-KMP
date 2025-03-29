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
package cnc.coop.milkcreamies.presentation.ui.screens.milk

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cnc.coop.milkcreamies.models.PaymentMode
import cnc.coop.milkcreamies.presentation.viewmodel.milk.MilkOutViewModel
import cnc.coop.milkcreamies.presentation.viewmodel.milk.ValidationResult
import kotlin.math.max

@Composable
fun AddSaleDialog(
    viewModel: MilkOutViewModel,
    availableStock: Double,
    onDismiss: () -> Unit,
    onAddSale: (String, Double, Double, PaymentMode) -> Unit
) {
    var customerName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var pricePerLiter by remember { mutableStateOf("") }
    var selectedPaymentMode by remember { mutableStateOf(PaymentMode.CASH) }
    
    var quantityError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Milk Sale") },
        text = {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Available stock: ${availableStock.toInt()} liters", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { 
                        customerName = it
                        nameError = if (it.isBlank()) "Customer name is required" else null
                    },
                    label = { Text("Customer Name") },
                    isError = nameError != null,
                    supportingText = { nameError?.let { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { value ->
                        quantity = value
                        val parsedValue = value.toDoubleOrNull()
                        if (parsedValue != null) {
                            when (val result = viewModel.validateSaleQuantity(parsedValue)) {
                                is ValidationResult.Invalid -> quantityError = result.message
                                is ValidationResult.Valid -> quantityError = null
                            }
                        } else if (value.isNotEmpty()) {
                            quantityError = "Enter a valid number"
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("Quantity (liters)") },
                    isError = quantityError != null,
                    supportingText = { quantityError?.let { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = pricePerLiter,
                    onValueChange = { value ->
                        pricePerLiter = value
                        val parsedValue = value.toDoubleOrNull()
                        priceError = when {
                            value.isEmpty() -> "Price is required"
                            parsedValue == null -> "Enter a valid price"
                            parsedValue <= 0 -> "Price must be greater than 0"
                            else -> null
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    label = { Text("Price per liter") },
                    isError = priceError != null,
                    supportingText = { priceError?.let { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Column {
                    Text(
                        "Payment Mode",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedPaymentMode == PaymentMode.CASH,
                            onClick = { selectedPaymentMode = PaymentMode.CASH }
                        )
                        Text("Cash", modifier = Modifier.padding(start = 8.dp))
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        RadioButton(
                            selected = selectedPaymentMode == PaymentMode.MPESA,
                            onClick = { selectedPaymentMode = PaymentMode.MPESA }
                        )
                        Text("M-Pesa", modifier = Modifier.padding(start = 8.dp))
                    }
                }
                
                val total = try {
                    val qtyValue = quantity.toDoubleOrNull() ?: 0.0
                    val priceValue = pricePerLiter.toDoubleOrNull() ?: 0.0
                    qtyValue * priceValue
                } catch (e: NumberFormatException) {
                    0.0
                }
                
                Text(
                    text = "Total: KSh ${max(0.0, total).toString().take(8)}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val quantityValue = quantity.toDoubleOrNull() ?: 0.0
                    val priceValue = pricePerLiter.toDoubleOrNull() ?: 0.0
                    
                    onAddSale(
                        customerName.trim(),
                        quantityValue,
                        priceValue,
                        selectedPaymentMode
                    )
                },
                enabled = customerName.isNotBlank() && 
                        quantity.isNotBlank() && 
                        pricePerLiter.isNotBlank() && 
                        quantityError == null && 
                        priceError == null &&
                        nameError == null
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
