package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Customer
import com.example.ui.MainViewModel
import java.util.Locale

private val AVATAR_PALETTE = listOf(
    Color(0xFFA855F7), // purple
    Color(0xFF22C55E), // green
    Color(0xFF3B82F6), // blue
    Color(0xFFEC4899), // pink
    Color(0xFF8B5CF6), // violet
    Color(0xFF14B8A6), // teal
    Color(0xFFF97316), // orange
    Color(0xFF0EA5E9), // sky
    Color(0xFF6366F1), // indigo
    Color(0xFF10B981)  // emerald
)

@Composable
fun CustomersScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val customers by viewModel.customers.collectAsState()
    val filteredCustomers by viewModel.filteredCustomers.collectAsState()
    val searchQuery by viewModel.customerSearch.collectAsState()
    val filterDebtorsOnly by viewModel.filterDebtorsOnly.collectAsState()
    val totalCustomerDebt by viewModel.totalCustomerDebt.collectAsState()
    val supabaseStatus by viewModel.supabaseStatus.collectAsState()

    var showCreateCustomerDialog by remember { mutableStateOf(false) }
    var customerToEdit by remember { mutableStateOf<Customer?>(null) }
    var deleteCandidateCustomer by remember { mutableStateOf<Customer?>(null) }

    // Pagination (matching website's 8 items per page)
    var currentPage by remember { mutableIntStateOf(1) }
    val pageSize = 8

    val debtorCount = customers.count { it.currentBalance > 0 }
    val totalPages = (filteredCustomers.size + pageSize - 1) / pageSize.coerceAtLeast(1)
    val safePage = currentPage.coerceIn(1, totalPages.coerceAtLeast(1))
    val paginatedList = remember(filteredCustomers, safePage) {
        val start = (safePage - 1) * pageSize
        filteredCustomers.drop(start).take(pageSize)
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. EXACT TOP SUMMARY METRIC CARDS (Matching uploaded image and CustomersList.tsx)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Card 1: عدد العملاء المدينين (Amber #fef8ea / #f59e0b)
                    ExactMetricCard(
                        title = "عدد العملاء المدينين",
                        value = "$debtorCount",
                        bgColor = Color(0xFFFEF8EA),
                        borderColor = Color(0xFFFEF0CD),
                        iconBgColor = Color(0xFFF59E0B),
                        textColor = Color(0xFFD97706),
                        labelColor = Color(0xFFB45309),
                        icon = Icons.Default.People,
                        modifier = Modifier.weight(1f)
                    )

                    // Card 2: إجمالي المديونيات (Rose #fff1f2 / #f43f5e)
                    ExactMetricCard(
                        title = "إجمالي المديونيات",
                        value = "${String.format(Locale.US, "%,.0f", totalCustomerDebt)} ج.م",
                        bgColor = Color(0xFFFFF1F2),
                        borderColor = Color(0xFFFFE4E6),
                        iconBgColor = Color(0xFFF43F5E),
                        textColor = Color(0xFFE11D48),
                        labelColor = Color(0xFFBE123C),
                        icon = Icons.Default.MonetizationOn,
                        modifier = Modifier.weight(1.3f)
                    )

                    // Card 3: إجمالي العملاء المسجلين (Mint #f0fdf4 / emerald-teal)
                    ExactMetricCard(
                        title = "إجمالي العملاء المسجلين",
                        value = "${customers.size}",
                        bgColor = Color(0xFFF0FDF4),
                        borderColor = Color(0xFFDCFCE7),
                        iconBgColor = Color(0xFF059669),
                        textColor = Color(0xFF059669),
                        labelColor = Color(0xFF065F46),
                        icon = Icons.Default.Person,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 2. SEARCH & FILTER ACTION BAR (Matching website toolbar)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                viewModel.customerSearch.value = it
                                currentPage = 1
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("customer_search_input"),
                            placeholder = { Text("ابحث باسم العميل أو رقم التليفون أو القرية أو الكود...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.customerSearch.value = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "مسح")
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // "المدينين فقط" Switch
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "المدينين فقط",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF334155)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Switch(
                                    checked = filterDebtorsOnly,
                                    onCheckedChange = {
                                        viewModel.filterDebtorsOnly.value = it
                                        currentPage = 1
                                    }
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val isConnected = supabaseStatus.state == com.example.network.supabase.SupabaseConnectionState.CONNECTED
                                OutlinedButton(
                                    onClick = { viewModel.openSupabaseConfig() },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = if (isConnected) Color(0xFF059669) else Color(0xFFD97706)
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isConnected) Color(0xFF10B981) else Color(0xFFF59E0B)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isConnected) Icons.Default.CloudDone else Icons.Default.Cloud,
                                        contentDescription = "Supabase",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isConnected) "Supabase" else "ربط سحابي",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
                                    )
                                }

                                // Dark Navy "+ إضافة عميل" Button (Matching #0F2744)
                                Button(
                                    onClick = {
                                        customerToEdit = null
                                        showCreateCustomerDialog = true
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F2744)),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("إضافة عميل", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // 3. TABLE OF CUSTOMERS (Matching website with Ice Blue Header #EDF4FA)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Ice Blue Table Header (#EDF4FA)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFEDF4FA))
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("#", fontWeight = FontWeight.Black, color = Color(0xFF334155), fontSize = 11.sp, modifier = Modifier.width(24.dp))
                                Text("اسم العميل", fontWeight = FontWeight.Black, color = Color(0xFF334155), fontSize = 12.sp, modifier = Modifier.weight(1.8f))
                                Text("القرية", fontWeight = FontWeight.Black, color = Color(0xFF334155), fontSize = 11.sp, modifier = Modifier.weight(1f))
                                Text("المديونية", fontWeight = FontWeight.Black, color = Color(0xFF334155), fontSize = 12.sp, modifier = Modifier.weight(1.2f))
                                Text("الحالة", fontWeight = FontWeight.Black, color = Color(0xFF334155), fontSize = 11.sp, modifier = Modifier.width(55.dp))
                                Text("الإجراءات", fontWeight = FontWeight.Black, color = Color(0xFF334155), fontSize = 11.sp, modifier = Modifier.width(110.dp))
                            }
                        }

                        // Table Rows
                        if (paginatedList.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("لا يوجد عملاء يطابقون محددات البحث", color = Color.Gray)
                            }
                        } else {
                            paginatedList.forEachIndexed { index, customer ->
                                val rowNumber = (safePage - 1) * pageSize + index + 1
                                val avatarColor = AVATAR_PALETTE[index % AVATAR_PALETTE.size]

                                WebCustomerTableRow(
                                    rowNumber = rowNumber,
                                    customer = customer,
                                    avatarColor = avatarColor,
                                    onViewLedger = { viewModel.showLedgerDialogForCustomer.value = customer },
                                    onEdit = {
                                        customerToEdit = customer
                                        showCreateCustomerDialog = true
                                    },
                                    onDelete = { deleteCandidateCustomer = customer },
                                    onPayment = { viewModel.showPaymentDialogForCustomer.value = customer },
                                    onAddDebt = {
                                        viewModel.preselectedCustomerForDebt.value = customer
                                        viewModel.showAddDebtDialog.value = true
                                    }
                                )
                            }
                        }

                        // Table Footer / Pagination Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8FAFC))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "عرض ${if (filteredCustomers.isEmpty()) 0 else (safePage - 1) * pageSize + 1} - ${minOf(safePage * pageSize, filteredCustomers.size)} من ${filteredCustomers.size} عميل",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )

                                // Prev / Next Buttons
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(
                                        onClick = { if (safePage > 1) currentPage-- },
                                        enabled = safePage > 1,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.ChevronRight, contentDescription = "السابق")
                                    }

                                    Text(
                                        text = "$safePage / $totalPages",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )

                                    IconButton(
                                        onClick = { if (safePage < totalPages) currentPage++ },
                                        enabled = safePage < totalPages,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.ChevronLeft, contentDescription = "التالي")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Add or Edit Customer
    if (showCreateCustomerDialog) {
        AddCustomerDialog(
            customer = customerToEdit,
            onDismiss = {
                showCreateCustomerDialog = false
                customerToEdit = null
            },
            onConfirm = { name: String, partner: String?, phone: String, village: String, balance: Double ->
                viewModel.saveCustomer(customerToEdit?.id, name, partner, phone, village, balance)
                showCreateCustomerDialog = false
                customerToEdit = null
            }
        )
    }

    // Modal: Confirm Delete
    if (deleteCandidateCustomer != null) {
        AlertDialog(
            onDismissRequest = { deleteCandidateCustomer = null },
            title = { Text("حذف العميل نهائياً") },
            text = { Text("هل أنت متأكد من حذف العميل \"${deleteCandidateCustomer!!.name}\"؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCustomer(deleteCandidateCustomer!!.id)
                        deleteCandidateCustomer = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                ) {
                    Text("نعم، احذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteCandidateCustomer = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun ExactMetricCard(
    title: String,
    value: String,
    bgColor: Color,
    borderColor: Color,
    iconBgColor: Color,
    textColor: Color,
    labelColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = textColor,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun WebCustomerTableRow(
    rowNumber: Int,
    customer: Customer,
    avatarColor: Color,
    onViewLedger: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPayment: () -> Unit,
    onAddDebt: () -> Unit
) {
    val hasDebt = customer.currentBalance > 0
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // #
        Text(
            text = "$rowNumber",
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            fontSize = 11.sp,
            modifier = Modifier.width(24.dp)
        )

        // Customer Name + Avatar + Partner
        Row(
            modifier = Modifier.weight(1.8f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(avatarColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = customer.name.take(1),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = customer.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF0F172A)
                )
                if (!customer.partnerName.isNullOrBlank()) {
                    Text(
                        text = "[شريك: ${customer.partnerName}]",
                        fontSize = 10.sp,
                        color = Color(0xFF047857),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Village
        Text(
            text = customer.villageOrAddress ?: "-",
            fontSize = 11.sp,
            color = Color(0xFF475569),
            modifier = Modifier.weight(1f)
        )

        // Debt Amount
        Text(
            text = "${String.format(Locale.US, "%,.0f", customer.currentBalance)} ج.م",
            fontWeight = FontWeight.Black,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF0F172A),
            modifier = Modifier.weight(1.2f)
        )

        // Status Badge (مدين بالوردي / سليم بالأخضر)
        Box(
            modifier = Modifier
                .width(55.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (hasDebt) Color(0xFFFFE4E6) else Color(0xFFDCFCE7))
                .padding(vertical = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (hasDebt) "مدين" else "سليم",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = if (hasDebt) Color(0xFFE11D48) else Color(0xFF16A34A)
            )
        }

        // 4 Action Buttons: Delete (Rose), Edit (Sky), View (Solid Blue #1D4ED8), 3-dots
        Row(
            modifier = Modifier.width(110.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Delete (Rose)
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFFFE4E6))
                    .clickable(onClick = onDelete),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFE11D48), modifier = Modifier.size(14.dp))
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Edit (Sky #E0F2FE)
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFE0F2FE))
                    .clickable(onClick = onEdit),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = Color(0xFF0284C7), modifier = Modifier.size(14.dp))
            }

            Spacer(modifier = Modifier.width(4.dp))

            // View Ledger (Solid Blue #1D4ED8)
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF1D4ED8))
                    .clickable(onClick = onViewLedger),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Visibility, contentDescription = "كشف حساب", tint = Color.White, modifier = Modifier.size(14.dp))
            }

            Spacer(modifier = Modifier.width(4.dp))

            // 3-dots menu
            Box {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { menuExpanded = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "المزيد", tint = Color.Gray, modifier = Modifier.size(16.dp))
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("سند قبض / تحصيل نقدية", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF047857)) },
                        onClick = {
                            menuExpanded = false
                            onPayment()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("تسجيل مديونية شراء", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706)) },
                        onClick = {
                            menuExpanded = false
                            onAddDebt()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("كشف الحساب المفصل", fontSize = 11.sp) },
                        onClick = {
                            menuExpanded = false
                            onViewLedger()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AddCustomerDialog(
    customer: Customer?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, partner: String?, phone: String, village: String, balance: Double) -> Unit
) {
    var name by remember { mutableStateOf(customer?.name ?: "") }
    var partnerName by remember { mutableStateOf(customer?.partnerName ?: "") }
    var phone by remember { mutableStateOf(customer?.phone ?: "") }
    var village by remember { mutableStateOf(customer?.villageOrAddress ?: "") }
    var balanceText by remember { mutableStateOf(if (customer != null) customer.openingBalance.toString() else "0") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (customer == null) "إضافة عميل / مزارع جديد" else "تعديل بيانات العميل",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        isError = false
                    },
                    label = { Text("اسم المزارع / العميل *") },
                    isError = isError && name.isBlank(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = partnerName,
                    onValueChange = { partnerName = it },
                    label = { Text("الشريك أو الكفيل (اختياري)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الموبايل / الواتساب") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = village,
                    onValueChange = { village = it },
                    label = { Text("القرية / المركز (مثل: المنصورة، ميت غمر...)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (customer == null) {
                    OutlinedTextField(
                        value = balanceText,
                        onValueChange = { balanceText = it },
                        label = { Text("الرصيد الافتتاحي أو المديونية السابقة (ج.م)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        isError = true
                    } else {
                        val bal = balanceText.toDoubleOrNull() ?: 0.0
                        onConfirm(name.trim(), partnerName.trim().ifEmpty { null }, phone.trim(), village.trim(), bal)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857))
            ) {
                Text(if (customer == null) "إضافة العميل" else "حفظ التعديلات", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = Color.Gray)
            }
        }
    )
}

