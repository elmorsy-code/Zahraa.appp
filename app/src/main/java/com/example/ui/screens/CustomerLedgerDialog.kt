package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.Customer
import com.example.model.CustomerLedgerEntry
import com.example.ui.MainViewModel
import java.util.Locale

@Composable
fun CustomerLedgerDialog(
    customer: Customer,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val ledgerEntries = remember(customer.id) {
        viewModel.getCustomerLedger(customer.id)
    }

    val totalDebit = ledgerEntries.sumOf { it.debit }
    val totalCredit = ledgerEntries.sumOf { it.credit }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .clip(RoundedCornerShape(24.dp)),
            color = Color.White
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A))
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "كشف حساب: ${customer.name}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            if (!customer.partnerName.isNullOrBlank()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(شريك: ${customer.partnerName})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFFBBF24),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "كود العميل: ${customer.code} · ${customer.villageOrAddress ?: "كفر الحجر"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White)
                    }
                }

                // Balance Summary Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Total Debits
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("إجمالي المسحوبات (مدين)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(
                                "${String.format(Locale.US, "%,.2f", totalDebit)} ج.م",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Total Payments
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("إجمالي السدادات (دائن)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(
                                "${String.format(Locale.US, "%,.2f", totalCredit)} ج.م",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Net Balance
                    Box(
                        modifier = Modifier
                            .weight(1.1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (customer.currentBalance > 0) Color(0xFFFEF3C7) else Color(0xFFD1FAE5))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                if (customer.currentBalance > 0) "الرصيد المستحق (مدين)" else "حالة الحساب",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (customer.currentBalance > 0) Color(0xFFB45309) else Color(0xFF047857),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${String.format(Locale.US, "%,.2f", customer.currentBalance)} ج.م",
                                fontWeight = FontWeight.Black,
                                color = if (customer.currentBalance > 0) Color(0xFFB45309) else Color(0xFF047857),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Table Entries
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (ledgerEntries.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("لا توجد حركات مسجلة في كشف حساب العميل حتى الآن", color = Color.Gray)
                            }
                        }
                    } else {
                        items(ledgerEntries) { entry ->
                            LedgerEntryRowCard(entry = entry)
                        }
                    }
                }

                // Bottom Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            onDismiss()
                            viewModel.preselectedCustomerForDebt.value = customer
                            viewModel.showAddDebtDialog.value = true
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تسجيل شراء جديد")
                    }

                    Button(
                        onClick = {
                            onDismiss()
                            viewModel.showPaymentDialogForCustomer.value = customer
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857))
                    ) {
                        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تحصيل وقبض نقدية")
                    }
                }
            }
        }
    }
}

@Composable
fun LedgerEntryRowCard(entry: CustomerLedgerEntry) {
    val isInvoice = entry.entryType == "invoice"
    val isPayment = entry.entryType == "payment"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isInvoice) Color(0xFFFFFBEB) else Color(0xFFF0FDF4)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isInvoice) Color(0xFFFEF3C7) else Color(0xFFD1FAE5))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isInvoice) "فاتورة مبيعات" else if (isPayment) "سند قبض" else "تسوية",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isInvoice) Color(0xFFB45309) else Color(0xFF047857),
                            fontSize = 10.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = entry.documentNumber,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = entry.entryDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = entry.description,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1E293B),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (entry.debit > 0) {
                        Text(
                            text = "مدين: ${String.format(Locale.US, "%,.2f", entry.debit)} ج.م",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                    if (entry.credit > 0) {
                        Text(
                            text = "سداد: ${String.format(Locale.US, "%,.2f", entry.credit)} ج.م",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF047857),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                }

                Text(
                    text = "الرصيد بعدها: ${String.format(Locale.US, "%,.2f", entry.runningBalance)} ج.م",
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            }
        }
    }
}
