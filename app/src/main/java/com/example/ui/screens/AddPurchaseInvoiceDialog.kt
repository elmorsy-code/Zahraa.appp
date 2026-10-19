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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.window.DialogProperties
import com.example.model.PurchaseInvoiceItem
import com.example.model.Supplier
import com.example.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPurchaseInvoiceDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val suppliers by viewModel.suppliers.collectAsState()

    var supplierName by remember { mutableStateOf(suppliers.firstOrNull()?.name ?: "") }
    var supplierPhone by remember { mutableStateOf("") }
    var invoiceNumber by remember { mutableStateOf("") }

    val currentDateStr = remember {
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
    }
    var invoiceDate by remember { mutableStateOf(currentDateStr) }

    // Payment Type: 0 = آجل (credit), 1 = نقدي (cash), 2 = سداد جزئي (partial)
    var paymentTypeIndex by remember { mutableIntStateOf(0) }
    var paidAmount by remember { mutableStateOf("0") }
    var discountAmount by remember { mutableStateOf("0") }
    var notes by remember { mutableStateOf("") }

    // Items List in Invoice
    val itemsList = remember { mutableStateListOf<PurchaseInvoiceItem>() }

    // Current builder item
    var currentItemName by remember { mutableStateOf("") }
    var currentItemQty by remember { mutableStateOf("1") }
    var currentItemUnit by remember { mutableStateOf("عبوة") }
    var currentItemCost by remember { mutableStateOf("") }

    val subtotal = itemsList.sumOf { it.totalCost }
    val discount = discountAmount.toDoubleOrNull() ?: 0.0
    val totalAmount = (subtotal - discount).coerceAtLeast(0.0)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp)),
            color = Color.White
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF7C3AED))
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "إضافة فاتورة مشتريات أدوية جديدة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "تسجيل مشتريات من الموردين وحسابات السداد الآجلة والنقدية",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFF5F3FF)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إلغاء", tint = Color.White)
                    }
                }

                // Scrollable Form Body
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Supplier Name & Phone
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = supplierName,
                                onValueChange = { supplierName = it },
                                label = { Text("اسم المورد أو الشركة *") },
                                placeholder = { Text("مثال: شركة سنجنتا مصر") },
                                modifier = Modifier.weight(1.3f),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = supplierPhone,
                                onValueChange = { supplierPhone = it },
                                label = { Text("الهاتف (اختياري)") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // 2. Invoice Number & Date
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = invoiceNumber,
                                onValueChange = { invoiceNumber = it },
                                label = { Text("رقم الفاتورة الورقية") },
                                placeholder = { Text("INV-9901") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = invoiceDate,
                                onValueChange = { invoiceDate = it },
                                label = { Text("تاريخ الفاتورة") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // 3. Items list if added
                    if (itemsList.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F3FF))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "الأدوية المضافة للفاتورة (${itemsList.size} صنف):",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color(0xFF6D28D9)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    itemsList.forEachIndexed { index, itm ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "${index + 1}. ${itm.productName}",
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                                Text(
                                                    text = "${itm.qty} ${itm.unit} × ${itm.costPrice} ج.م",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.Gray
                                                )
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "${String.format(Locale.US, "%,.2f", itm.totalCost)} ج.م",
                                                    fontWeight = FontWeight.Black,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = Color(0xFF6D28D9)
                                                )
                                                IconButton(
                                                    onClick = { itemsList.removeAt(index) },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 4. Medicine Item Builder
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = if (itemsList.isEmpty()) "إضافة دواء إلى الفاتورة *" else "إضافة دواء آخر للفاتورة:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = currentItemName,
                                    onValueChange = { currentItemName = it },
                                    label = { Text("اسم الدواء / المبيد *") },
                                    placeholder = { Text("مثال: رادوميل جولد، أباكتين...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = currentItemQty,
                                        onValueChange = { currentItemQty = it },
                                        label = { Text("الكمية") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    OutlinedTextField(
                                        value = currentItemUnit,
                                        onValueChange = { currentItemUnit = it },
                                        label = { Text("الوحدة") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    OutlinedTextField(
                                        value = currentItemCost,
                                        onValueChange = { currentItemCost = it },
                                        label = { Text("سعر الشراء") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(1.2f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        val q = currentItemQty.toDoubleOrNull() ?: 1.0
                                        val c = currentItemCost.toDoubleOrNull() ?: 0.0
                                        if (currentItemName.isNotBlank() && c > 0) {
                                            itemsList.add(
                                                PurchaseInvoiceItem(
                                                    id = UUID.randomUUID().toString(),
                                                    productName = currentItemName.trim(),
                                                    qty = q,
                                                    unit = currentItemUnit.trim(),
                                                    costPrice = c,
                                                    totalCost = q * c
                                                )
                                            )
                                            currentItemName = ""
                                            currentItemCost = ""
                                        }
                                    },
                                    enabled = currentItemName.isNotBlank() && (currentItemCost.toDoubleOrNull() ?: 0.0) > 0,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+ إضافة الدواء للفاتورة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // 5. Payment Type & Discount
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("طريقة السداد للمورد *", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)

                            TabRow(
                                selectedTabIndex = paymentTypeIndex,
                                modifier = Modifier.clip(RoundedCornerShape(10.dp))
                            ) {
                                Tab(
                                    selected = paymentTypeIndex == 0,
                                    onClick = { paymentTypeIndex = 0 },
                                    text = { Text("آجل (على الحساب)", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                )
                                Tab(
                                    selected = paymentTypeIndex == 1,
                                    onClick = { paymentTypeIndex = 1 },
                                    text = { Text("نقدي (كاش)", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                )
                                Tab(
                                    selected = paymentTypeIndex == 2,
                                    onClick = { paymentTypeIndex = 2 },
                                    text = { Text("سداد جزئي", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                )
                            }

                            if (paymentTypeIndex == 2) {
                                OutlinedTextField(
                                    value = paidAmount,
                                    onValueChange = { paidAmount = it },
                                    label = { Text("المبلغ المدفوع نقداً الآن (ج.م)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }

                    // 6. Grand Total Banner
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFEDE9FE))
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("إجمالي الفاتورة المطلوب:", fontWeight = FontWeight.Bold, color = Color(0xFF5B21B6))
                                Text(
                                    "${String.format(Locale.US, "%,.2f", totalAmount)} ج.م",
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 18.sp,
                                    color = Color(0xFF5B21B6)
                                )
                            }
                        }
                    }
                }

                // Footer Actions
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
                            val finalList = itemsList.toMutableList()
                            val c = currentItemCost.toDoubleOrNull() ?: 0.0
                            val q = currentItemQty.toDoubleOrNull() ?: 1.0

                            // If user typed item without pressing "+ إضافة الدواء", add it automatically
                            if (currentItemName.isNotBlank() && c > 0) {
                                finalList.add(
                                    PurchaseInvoiceItem(
                                        id = UUID.randomUUID().toString(),
                                        productName = currentItemName.trim(),
                                        qty = q,
                                        unit = currentItemUnit.trim(),
                                        costPrice = c,
                                        totalCost = q * c
                                    )
                                )
                            }

                            if (finalList.isNotEmpty() && supplierName.isNotBlank()) {
                                val pType = when(paymentTypeIndex) {
                                    1 -> "cash"
                                    2 -> "partial"
                                    else -> "credit"
                                }
                                val paid = if (pType == "cash") totalAmount else (paidAmount.toDoubleOrNull() ?: 0.0)

                                viewModel.savePurchaseInvoice(
                                    supplierName = supplierName.trim(),
                                    supplierPhone = supplierPhone.ifBlank { null },
                                    invoiceNumber = invoiceNumber.ifBlank { null },
                                    invoiceDate = invoiceDate,
                                    paymentType = pType,
                                    paidAmount = paid,
                                    discountAmount = discount,
                                    items = finalList,
                                    notes = notes.ifBlank { null }
                                )
                            }
                        },
                        enabled = supplierName.isNotBlank() && (itemsList.isNotEmpty() || (currentItemName.isNotBlank() && (currentItemCost.toDoubleOrNull() ?: 0.0) > 0)),
                        modifier = Modifier.weight(1.8f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                    ) {
                        Text("حفظ الفاتورة وتحديث الأرصدة", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
