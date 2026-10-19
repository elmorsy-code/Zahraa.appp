package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val cashAccounts by viewModel.cashAccounts.collectAsState()

    val expenseCategories = listOf(
        "نقل وشحن بضائع",
        "كهرباء ومياه وغاز",
        "إيجار المحل أو المخزن",
        "أجور وعمالة يومية",
        "ضيافة وبوفيه",
        "أدوات مكتبية ومطبوعات",
        "صيانة وتجهيزات",
        "مصروفات نثرية وأخرى"
    )

    var category by remember { mutableStateOf(expenseCategories.first()) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    var amount by remember { mutableStateOf("") }
    var selectedCashAccount by remember { mutableStateOf(cashAccounts.firstOrNull()) }
    var cashDropdownExpanded by remember { mutableStateOf(false) }

    var paidTo by remember { mutableStateOf("") }
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
                        .background(Color(0xFFBE123C))
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "تسجيل مصروف تشغيلي جديد",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "صرف مبلغ من الخزينة وتسجيله في الحسابات",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFFE4E6)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إلغاء", tint = Color.White)
                    }
                }

                // Form Inputs
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Category Selector
                    ExposedDropdownMenuBox(
                        expanded = categoryDropdownExpanded,
                        onExpandedChange = { categoryDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("بند المصروف *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false }
                        ) {
                            expenseCategories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        category = cat
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Amount
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("المبلغ المصروف (ج.م) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Cash Account
                    ExposedDropdownMenuBox(
                        expanded = cashDropdownExpanded,
                        onExpandedChange = { cashDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedCashAccount?.name ?: "الخزينة النقدية الرئيسية",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("الخزينة المسحوب منها *") },
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
                                    text = { Text("${acc.name} (${acc.currentBalance} ج.م)") },
                                    onClick = {
                                        selectedCashAccount = acc
                                        cashDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Paid To
                    OutlinedTextField(
                        value = paidTo,
                        onValueChange = { paidTo = it },
                        label = { Text("صُرف إلى / المستلم") },
                        placeholder = { Text("مثال: شركة الكهرباء، سائق النقل...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("ملاحظات إضافية") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("إلغاء", color = Color.Gray, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val amt = amount.toDoubleOrNull() ?: 0.0
                            if (amt > 0) {
                                viewModel.recordExpense(
                                    category = category,
                                    amount = amt,
                                    cashAccountId = selectedCashAccount?.id ?: "b01",
                                    paidTo = paidTo.ifBlank { null },
                                    notes = notes.ifBlank { null }
                                )
                            }
                        },
                        enabled = (amount.toDoubleOrNull() ?: 0.0) > 0,
                        modifier = Modifier.weight(1.6f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBE123C))
                    ) {
                        Text("صرف وتسجيل في الخزينة", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
