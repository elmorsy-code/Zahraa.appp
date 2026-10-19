package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Customer
import com.example.ui.MainViewModel
import com.example.ui.ZahraaTab
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var activeDashboardView by remember { mutableIntStateOf(0) }
    val totalCash by viewModel.totalCash.collectAsState()
    val totalCustomerDebt by viewModel.totalCustomerDebt.collectAsState()
    val products by viewModel.products.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val invoices by viewModel.purchaseInvoices.collectAsState()
    val supabaseStatus by viewModel.supabaseStatus.collectAsState()

    val topDebtors = customers.filter { it.currentBalance > 0 }
        .sortedByDescending { it.currentBalance }
        .take(4)

    if (activeDashboardView == 1) {
        Column(modifier = modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE2E8F0).copy(alpha = 0.7f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Transparent)
                        .clickable { activeDashboardView = 0 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لوحة المؤشرات والعمليات",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF475569)
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF047857))
                        .clickable { activeDashboardView = 1 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "المعرض والواجهة التعريفية",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
            WebHomeScreen(viewModel = viewModel, modifier = Modifier.weight(1f))
        }
    } else {
        Box(modifier = modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // View Mode Switcher
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFE2E8F0).copy(alpha = 0.7f))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF047857))
                                .clickable { activeDashboardView = 0 }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "لوحة المؤشرات والعمليات",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Transparent)
                                .clickable { activeDashboardView = 1 }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "المعرض والواجهة التعريفية",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF475569)
                            )
                        }
                    }
                }
            // 1. Executive Master Header Banner with Logo & Live Sync Tag
            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("executive_hero_banner"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = Color(0xFF0B1F18)
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Logo Box
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White)
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.app_logo),
                                    contentDescription = "الزهراء",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            val isSupabaseConnected = supabaseStatus.state == com.example.network.supabase.SupabaseConnectionState.CONNECTED
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSupabaseConnected) Color(0xFF047857).copy(alpha = 0.4f) else Color(0xFFD97706).copy(alpha = 0.3f))
                                    .clickable { viewModel.openSupabaseConfig() }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (isSupabaseConnected) Color(0xFF34D399) else Color(0xFFFBBF24))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isSupabaseConnected) "Supabase سحابي نشط 🟢" else "ربط Supabase السحابي 🟡",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSupabaseConnected) Color(0xFFA7F3D0) else Color(0xFFFDE68A),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "صرح الزهراء للأدوية والمبيدات الزراعية",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "منظومة إدارة الصيدلية الزراعية، حسابات المزارعين والديون، الخزينة، والمشتريات",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8),
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // 2. 4 Main Interactive KPI Cards (Treasury, Debts, Products, Invoices)
            item {
                Text(
                    text = "المؤشرات التشغيلية والمالية الحية",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Treasury Cash Card
                    KpiDashboardCard(
                        title = "السيولة والخزينة",
                        value = "${String.format(Locale.US, "%,.0f", totalCash)} ج.م",
                        subtitle = "خزينة المحل والبنك",
                        icon = Icons.Default.MonetizationOn,
                        accentColor = Color(0xFF047857),
                        onClick = { viewModel.selectTab(ZahraaTab.TREASURY) },
                        modifier = Modifier.weight(1f)
                    )

                    // Customer Debts Card
                    KpiDashboardCard(
                        title = "مديونيات المزارعين",
                        value = "${String.format(Locale.US, "%,.0f", totalCustomerDebt)} ج.م",
                        subtitle = "${customers.count { it.currentBalance > 0 }} مزارع مدين",
                        icon = Icons.Default.People,
                        accentColor = Color(0xFFD97706),
                        onClick = { viewModel.selectTab(ZahraaTab.DEBTS) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Supabase Cloud Central Hub Card
            item {
                val isConnected = supabaseStatus.state == com.example.network.supabase.SupabaseConnectionState.CONNECTED
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.openSupabaseConfig() },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isConnected) Color(0xFF064E3B).copy(alpha = 0.12f) else Color(0xFFFEF3C7)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isConnected) Color(0xFF10B981) else Color(0xFFF59E0B)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isConnected) Color(0xFF059669) else Color(0xFFD97706)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isConnected) Icons.Default.CloudDone else Icons.Default.Cloud,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isConnected) "قاعدة بيانات Supabase السحابية (مباشرة 100%)" else "قاعدة بيانات Supabase (إعداد الربط السحابي)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isConnected) Color(0xFF065F46) else Color(0xFF92400E)
                                )
                                Text(
                                    text = if (isConnected) "البيانات مسحوبة وتُحفظ سحابياً لحظياً بدون Local Storage" else "اضغط هنا لإدخال رابط مشروعك ومفتاح الوصول لجلب وحفظ كل شيء من Supabase",
                                    fontSize = 10.5.sp,
                                    color = if (isConnected) Color(0xFF047857) else Color(0xFFB45309)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.openSupabaseConfig() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isConnected) Color(0xFF059669) else Color(0xFFD97706)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = if (isConnected) "إدارة" else "ربط الآن",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Products Catalog Card
                    KpiDashboardCard(
                        title = "دليل الأدوية",
                        value = "${products.size} دواء",
                        subtitle = "أصناف ومبيدات مرخصة",
                        icon = Icons.Default.Medication,
                        accentColor = Color(0xFF0D9488),
                        onClick = { viewModel.selectTab(ZahraaTab.PRODUCTS) },
                        modifier = Modifier.weight(1f)
                    )

                    // Purchases & Invoices Card
                    KpiDashboardCard(
                        title = "فواتير المشتريات",
                        value = "${invoices.size} فواتير",
                        subtitle = "حسابات الموردين",
                        icon = Icons.Default.ReceiptLong,
                        accentColor = Color(0xFF7C3AED),
                        onClick = { viewModel.selectTab(ZahraaTab.INVOICES) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 3. Quick Action Buttons Bar (+ مديونية، + سند قبض، + دواء، + مصروف)
            item {
                Text(
                    text = "العمليات والإجراءات السريعة",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionButton(
                        title = "تسجيل شراء",
                        icon = Icons.Default.Add,
                        containerColor = Color(0xFFD97706),
                        onClick = { viewModel.showAddDebtDialog.value = true },
                        modifier = Modifier.weight(1f)
                    )

                    QuickActionButton(
                        title = "سند قبض",
                        icon = Icons.Default.Payments,
                        containerColor = Color(0xFF047857),
                        onClick = {
                            val debtor = customers.find { it.currentBalance > 0 } ?: customers.firstOrNull()
                            viewModel.showPaymentDialogForCustomer.value = debtor
                        },
                        modifier = Modifier.weight(1f)
                    )

                    QuickActionButton(
                        title = "إضافة دواء",
                        icon = Icons.Default.Medication,
                        containerColor = Color(0xFF0D9488),
                        onClick = { viewModel.showAddProductDialog.value = true },
                        modifier = Modifier.weight(1f)
                    )

                    QuickActionButton(
                        title = "صرف مصروف",
                        icon = Icons.Default.Receipt,
                        containerColor = Color(0xFFBE123C),
                        onClick = { viewModel.showAddExpenseDialog.value = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 4. Top Debtors Farmers (كشف مديونيات المزارعين الأبرز)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "أبرز مديونيات المزارعين المطلوبة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "عرض الكل",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { viewModel.selectTab(ZahraaTab.DEBTS) }
                    )
                }
            }

            items(topDebtors) { customer ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.showLedgerDialogForCustomer.value = customer },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFFEF3C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = customer.code,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = customer.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = customer.villageOrAddress ?: "كفر الحجر",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${String.format(Locale.US, "%,.0f", customer.currentBalance)} ج.م",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFB45309),
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "انقر لكشف الحساب",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Floating Action Button to launch Simsim AI Assistant
        FloatingActionButton(
            onClick = { viewModel.selectTab(ZahraaTab.SIMSIM) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("simsim_fab"),
            containerColor = Color(0xFF047857),
            contentColor = Color.White
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "المساعد الذكي سمسم",
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "سمسم الذكي",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}
}

@Composable
fun KpiDashboardCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun QuickActionButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor
        ),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(20.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
