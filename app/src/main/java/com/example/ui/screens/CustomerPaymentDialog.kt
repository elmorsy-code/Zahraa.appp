package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.Customer
import com.example.ui.MainViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerPaymentDialog(
    customer: Customer,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val cashAccounts by viewModel.cashAccounts.collectAsState()

    var amount by remember {
        mutableStateOf(if (customer.currentBalance > 0) customer.currentBalance.toString() else "")
    }
    var selectedCashAccount by remember { mutableStateOf(cashAccounts.firstOrNull()) }
    var cashDropdownExpanded by remember { mutableStateOf(false) }

    var paymentMethod by remember { mutableStateOf("cash") }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
            color = Color.White
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF047857))
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "سند قبض وتحصيل نقدية",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "تحصيل دفعة وتحديث رصيد العميل والخزينة",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFD1FAE5)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إلغاء", tint = Color.White)
                    }
                }

                // Body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Customer preview card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFD1FAE5).copy(alpha = 0.5f))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = customer.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = customer.code,
                                    color = Color.Gray,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("المديونية الحالية على العميل:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text(
                                    "${String.format(Locale.US, "%,.2f", customer.currentBalance)} ج.م",
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }
                    }

                    // Amount input
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("المبلغ المحصل (ج.م) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Cash Account selector
                    ExposedDropdownMenuBox(
                        expanded = cashDropdownExpanded,
                        onExpandedChange = { cashDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedCashAccount?.name ?: "الخزينة النقدية الرئيسية",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("الخزينة المودع بها *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cashDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = cashDropdownExpanded,
                            onDismissRequest = { cashDropdownExpanded = false }
                        ) {
                            cashAccounts.forEach { acc ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(acc.name, fontWeight = FontWeight.Bold)
                                            Text("${String.format(Locale.US, "%,.0f", acc.currentBalance)} ج.م", color = Color.Gray)
                                        }
                                    },
                                    onClick = {
                                        selectedCashAccount = acc
                                        cashDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("البيان / ملاحظات السداد") },
                        placeholder = { Text("دفعة من حساب مبيدات عروة الشتاء...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Footer Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء", color = Color.Gray, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val parsed = amount.toDoubleOrNull() ?: 0.0
                            if (parsed > 0) {
                                viewModel.receiveCustomerPayment(
                                    customerId = customer.id,
                                    amount = parsed,
                                    cashAccountId = selectedCashAccount?.id ?: "b01",
                                    paymentMethod = paymentMethod,
                                    notes = notes.ifBlank { null }
                                )
                            }
                        },
                        enabled = (amount.toDoubleOrNull() ?: 0.0) > 0,
                        modifier = Modifier.weight(1.6f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857))
                    ) {
                        Text("تأكيد التحصيل وإصدار السند", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
