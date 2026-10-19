package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Science
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
import com.example.model.Customer
import com.example.model.DebtItemDraft
import com.example.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomerDebtDialog(
    preselectedCustomer: Customer?,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val customers by viewModel.customers.collectAsState()
    val products by viewModel.products.collectAsState()

    var selectedCustomer by remember { mutableStateOf(preselectedCustomer ?: customers.firstOrNull()) }
    var customerDropdownExpanded by remember { mutableStateOf(false) }

    var invoiceNumber by remember { mutableStateOf("") }
    val currentDateStr = remember {
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
    }
    var purchaseDate by remember { mutableStateOf(currentDateStr) }
    var notes by remember { mutableStateOf("") }

    // List of added items in this debt
    val itemsList = remember { androidx.compose.runtime.mutableStateListOf<DebtItemDraft>() }

    // Current builder item
    var currentItemName by remember { mutableStateOf("") }
    var isSubQuantityMode by remember { mutableStateOf(false) }

    // Standard quantity inputs
    var standardQty by remember { mutableStateOf("1") }
    var standardUnit by remember { mutableStateOf("زجاجة") }
    var standardUnitPrice by remember { mutableStateOf("") }

    // Sub-quantity inputs (تجزئة كسر عبوة)
    var fullCapacity by remember { mutableStateOf("100") }
    var subUnit by remember { mutableStateOf("مل") }
    var subQuantity by remember { mutableStateOf("20") }
    var packagePrice by remember { mutableStateOf("100") }

    val calculatedCurrentItemPrice: Double = remember(
        isSubQuantityMode, standardQty, standardUnitPrice,
        fullCapacity, subQuantity, packagePrice
    ) {
        if (isSubQuantityMode) {
            val subQ = subQuantity.toDoubleOrNull() ?: 0.0
            val cap = fullCapacity.toDoubleOrNull() ?: 1.0
            val pkgP = packagePrice.toDoubleOrNull() ?: 0.0
            if (cap > 0 && subQ > 0 && pkgP > 0) {
                (subQ / cap) * pkgP
            } else 0.0
        } else {
            val q = standardQty.toDoubleOrNull() ?: 0.0
            val p = standardUnitPrice.toDoubleOrNull() ?: 0.0
            q * p
        }
    }

    val grandTotalDebt = remember(itemsList.toList(), calculatedCurrentItemPrice, currentItemName) {
        val listTotal = itemsList.sumOf { it.totalPrice }
        if (itemsList.isEmpty() && currentItemName.isNotBlank() && calculatedCurrentItemPrice > 0) {
            calculatedCurrentItemPrice
        } else {
            listTotal
        }
    }

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
                        .background(Color(0xFFB45309))
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "تسجيل مديونية شراء جديدة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "إضافة أدوية ومبيدات · دعم الكمية الفرعية (تجزئة العبوة)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFEF3C7)
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
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Customer Selector
                    item {
                        Text(
                            text = "1. اختيار المزارع / العميل *",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        ExposedDropdownMenuBox(
                            expanded = customerDropdownExpanded,
                            onExpandedChange = { customerDropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedCustomer?.name ?: "اختر العميل...",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            ExposedDropdownMenu(
                                expanded = customerDropdownExpanded,
                                onDismissRequest = { customerDropdownExpanded = false }
                            ) {
                                customers.forEach { cust ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(cust.name, fontWeight = FontWeight.Bold)
                                                Text(
                                                    "${cust.code} (${String.format(Locale.US, "%,.0f", cust.currentBalance)} ج.م)",
                                                    color = Color.Gray,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedCustomer = cust
                                            customerDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 2. Invoice Number & Purchase Date
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = invoiceNumber,
                                onValueChange = { invoiceNumber = it },
                                label = { Text("رقم الفاتورة (اختياري)") },
                                placeholder = { Text("INV-1025") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = purchaseDate,
                                onValueChange = { purchaseDate = it },
                                label = { Text("تاريخ الشراء") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // 3. List of Added Medicines (if more than one)
                    if (itemsList.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7).copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "الأدوية المضافة للمديونية (${itemsList.size} صنف):",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color(0xFFB45309)
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
                                                    text = "${index + 1}. ${itm.itemName}",
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                                Text(
                                                    text = if (itm.isSubQuantity)
                                                        "كمية فرعية: ${itm.subQuantity} ${itm.subUnit} من أصل ${itm.fullCapacity} ${itm.subUnit} (${itm.packageType})"
                                                    else
                                                        "كمية: ${itm.quantity} ${itm.unit} بسعر ${itm.unitPrice} ج.م",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.Gray
                                                )
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "${String.format(Locale.US, "%,.2f", itm.totalPrice)} ج.م",
                                                    fontWeight = FontWeight.Black,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = Color(0xFFB45309)
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

                    // 4. Medicine Builder Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = if (itemsList.isEmpty()) "2. تفاصيل الدواء وسعره *" else "إضافة دواء آخر لهذه المديونية:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Mode Toggle (عبوة كاملة vs كمية فرعية)
                                TabRow(
                                    selectedTabIndex = if (isSubQuantityMode) 1 else 0,
                                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                                ) {
                                    Tab(
                                        selected = !isSubQuantityMode,
                                        onClick = { isSubQuantityMode = false },
                                        text = { Text("عبوة كاملة / كمية أساسية", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                    )
                                    Tab(
                                        selected = isSubQuantityMode,
                                        onClick = { isSubQuantityMode = true },
                                        text = { Text("كمية فرعية (تجزئة كسر عبوة)", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Medicine Name
                                OutlinedTextField(
                                    value = currentItemName,
                                    onValueChange = { currentItemName = it },
                                    label = { Text("اسم الدواء / المبيد *") },
                                    placeholder = { Text("مثال: أباكتين 5% سوبر، كوراجين، رادوميل...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // MODE A: Standard Quantity Inputs
                                if (!isSubQuantityMode) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = standardQty,
                                            onValueChange = { standardQty = it },
                                            label = { Text("الكمية") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp)
                                        )

                                        OutlinedTextField(
                                            value = standardUnit,
                                            onValueChange = { standardUnit = it },
                                            label = { Text("الوحدة") },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp)
                                        )

                                        OutlinedTextField(
                                            value = standardUnitPrice,
                                            onValueChange = { standardUnitPrice = it },
                                            label = { Text("سعر الوحدة") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            modifier = Modifier.weight(1.2f),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }
                                } else {
                                    // MODE B: Sub-Quantity Inputs (تجزئة كسر عبوة)
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFE0E7FF))
                                                .padding(8.dp)
                                        ) {
                                            Text(
                                                text = "مثال: زجاجة 100 مل أخذ منها 20 مل بسعر 100 ج.م للزجاجة كلها.",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF3730A3),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = subQuantity,
                                                onValueChange = { subQuantity = it },
                                                label = { Text("الكمية المأخوذة") },
                                                placeholder = { Text("20") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(10.dp)
                                            )

                                            OutlinedTextField(
                                                value = subUnit,
                                                onValueChange = { subUnit = it },
                                                label = { Text("الوحدة (مل/جم)") },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = fullCapacity,
                                                onValueChange = { fullCapacity = it },
                                                label = { Text("سعة العبوة كاملة") },
                                                placeholder = { Text("100") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(10.dp)
                                            )

                                            OutlinedTextField(
                                                value = packagePrice,
                                                onValueChange = { packagePrice = it },
                                                label = { Text("سعر العبوة كاملة (ج.م)") },
                                                placeholder = { Text("100") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Item Calculated Price & Add Button
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("سعر هذا الصنف:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        Text(
                                            text = "${String.format(Locale.US, "%,.2f", calculatedCurrentItemPrice)} ج.م",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color(0xFFB45309)
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            if (currentItemName.isNotBlank() && calculatedCurrentItemPrice > 0) {
                                                itemsList.add(
                                                    DebtItemDraft(
                                                        id = UUID.randomUUID().toString(),
                                                        itemName = currentItemName.trim(),
                                                        isSubQuantity = isSubQuantityMode,
                                                        quantity = standardQty.toDoubleOrNull() ?: 1.0,
                                                        unit = standardUnit,
                                                        unitPrice = standardUnitPrice.toDoubleOrNull() ?: 0.0,
                                                        subQuantity = subQuantity.toDoubleOrNull() ?: 0.0,
                                                        subUnit = subUnit,
                                                        fullCapacity = fullCapacity.toDoubleOrNull() ?: 100.0,
                                                        packagePrice = packagePrice.toDoubleOrNull() ?: 0.0,
                                                        totalPrice = calculatedCurrentItemPrice
                                                    )
                                                )
                                                // Reset builder
                                                currentItemName = ""
                                                standardUnitPrice = ""
                                            }
                                        },
                                        enabled = currentItemName.isNotBlank() && calculatedCurrentItemPrice > 0,
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("+ إضافة هذا الدواء للقائمة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // 5. Notes
                    item {
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("ملاحظات إضافية (اختياري)") },
                            placeholder = { Text("سداد بعد بيع المحصول، تسليم شيكارة سلفات...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // 6. Grand Total Banner
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFFEF3C7))
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("إجمالي المديونية المستحقة على العميل:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFF78350F))
                                    Text(
                                        if (itemsList.isNotEmpty()) "مجموع ${itemsList.size + if (currentItemName.isNotBlank() && calculatedCurrentItemPrice > 0) 1 else 0} أصناف"
                                        else "صنف واحد",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF92400E)
                                    )
                                }

                                Text(
                                    text = "${String.format(Locale.US, "%,.2f", grandTotalDebt)} ج.م",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF78350F)
                                )
                            }
                        }
                    }
                }

                // Dialog Buttons
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
                            val cust = selectedCustomer ?: return@Button
                            val finalList = itemsList.toMutableList()

                            // If user typed an item without pressing "+ إضافة الدواء", add it automatically
                            if (currentItemName.isNotBlank() && calculatedCurrentItemPrice > 0) {
                                finalList.add(
                                    DebtItemDraft(
                                        id = UUID.randomUUID().toString(),
                                        itemName = currentItemName.trim(),
                                        isSubQuantity = isSubQuantityMode,
                                        quantity = standardQty.toDoubleOrNull() ?: 1.0,
                                        unit = standardUnit,
                                        unitPrice = standardUnitPrice.toDoubleOrNull() ?: 0.0,
                                        subQuantity = subQuantity.toDoubleOrNull() ?: 0.0,
                                        subUnit = subUnit,
                                        fullCapacity = fullCapacity.toDoubleOrNull() ?: 100.0,
                                        packagePrice = packagePrice.toDoubleOrNull() ?: 0.0,
                                        totalPrice = calculatedCurrentItemPrice
                                    )
                                )
                            }

                            if (finalList.isNotEmpty()) {
                                viewModel.addCustomerDebt(
                                    customerId = cust.id,
                                    invoiceNumber = invoiceNumber.ifBlank { null },
                                    items = finalList,
                                    purchaseDate = purchaseDate,
                                    notes = notes.ifBlank { null }
                                )
                            }
                        },
                        enabled = selectedCustomer != null && grandTotalDebt > 0,
                        modifier = Modifier.weight(1.8f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                    ) {
                        Text("حفظ وتسجيل المديونية", fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}
