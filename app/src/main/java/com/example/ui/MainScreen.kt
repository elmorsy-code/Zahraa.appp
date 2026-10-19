package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.AddCustomerDebtDialog
import com.example.ui.screens.AddExpenseDialog
import com.example.ui.screens.AddProductDialog
import com.example.ui.screens.AddPurchaseInvoiceDialog
import com.example.ui.screens.CustomerLedgerDialog
import com.example.ui.screens.CustomerPaymentDialog
import com.example.ui.screens.CustomersScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.InvoicesScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.OldLedgersScreen
import com.example.ui.screens.ProductsScreen
import com.example.ui.screens.SimsimScreen
import com.example.ui.screens.TreasuryScreen
import java.util.Locale

/**
 * لو الاتصال بـ Supabase مضبوط والمستخدم مش مسجل دخول، بنعرض شاشة الدخول.
 * من غير تسجيل دخول القاعدة بترجع بيانات فاضية (حماية RLS).
 */
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val signedIn by viewModel.isSignedIn.collectAsState()
    val status by viewModel.supabaseStatus.collectAsState() // بيعيد الحساب لما الإعدادات تتغير
    val needsLogin = viewModel.isSupabaseConfigured() && !signedIn &&
        status.state != com.example.network.supabase.SupabaseConnectionState.DISCONNECTED

    if (needsLogin) {
        LoginScreen(viewModel = viewModel, modifier = modifier)
    } else {
        MainContent(viewModel = viewModel, modifier = modifier)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainContent(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val totalCash by viewModel.totalCash.collectAsState()

    // Modals
    val showAddDebt by viewModel.showAddDebtDialog.collectAsState()
    val preselectedCustomerForDebt by viewModel.preselectedCustomerForDebt.collectAsState()

    val paymentCustomer by viewModel.showPaymentDialogForCustomer.collectAsState()
    val ledgerCustomer by viewModel.showLedgerDialogForCustomer.collectAsState()

    val showAddProduct by viewModel.showAddProductDialog.collectAsState()
    val productToEdit by viewModel.productToEdit.collectAsState()

    val showAddPurchase by viewModel.showAddPurchaseDialog.collectAsState()
    val showAddExpense by viewModel.showAddExpenseDialog.collectAsState()

    val showSupabaseConfig by viewModel.showSupabaseConfigDialog.collectAsState()
    val supabaseStatus by viewModel.supabaseStatus.collectAsState()

    // BackHandler: Navigate back to Dashboard when on a sub-screen
    BackHandler(enabled = currentTab != ZahraaTab.DASHBOARD) {
        viewModel.selectTab(ZahraaTab.DASHBOARD)
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        topBar = {
            com.example.ui.components.WebHeader(
                currentTitle = currentTab.titleAr,
                totalCash = totalCash,
                isSupabaseConnected = supabaseStatus.state == com.example.network.supabase.SupabaseConnectionState.CONNECTED,
                onSupabaseClick = { viewModel.openSupabaseConfig() },
                onLogoClick = { viewModel.selectTab(ZahraaTab.DASHBOARD) },
                onSimsimClick = { viewModel.selectTab(ZahraaTab.SIMSIM) },
                onTreasuryClick = { viewModel.selectTab(ZahraaTab.TREASURY) }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("zahraa_bottom_nav_bar"),
                containerColor = Color.White
            ) {
                // 1. الرئيسية
                NavigationBarItem(
                    selected = currentTab == ZahraaTab.DASHBOARD,
                    onClick = { viewModel.selectTab(ZahraaTab.DASHBOARD) },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "الرئيسية") },
                    label = { Text("الرئيسية", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                )

                // 2. العملاء
                NavigationBarItem(
                    selected = currentTab == ZahraaTab.CUSTOMERS || currentTab == ZahraaTab.DEBTS,
                    onClick = { viewModel.selectTab(ZahraaTab.CUSTOMERS) },
                    icon = { Icon(Icons.Filled.People, contentDescription = "العملاء") },
                    label = { Text("العملاء", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                )

                // 3. الأدوية
                NavigationBarItem(
                    selected = currentTab == ZahraaTab.PRODUCTS,
                    onClick = { viewModel.selectTab(ZahraaTab.PRODUCTS) },
                    icon = { Icon(Icons.Filled.Medication, contentDescription = "الأدوية") },
                    label = { Text("الأدوية", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                )

                // 4. الفواتير
                NavigationBarItem(
                    selected = currentTab == ZahraaTab.INVOICES,
                    onClick = { viewModel.selectTab(ZahraaTab.INVOICES) },
                    icon = { Icon(Icons.Filled.ReceiptLong, contentDescription = "الفواتير") },
                    label = { Text("الفواتير", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                )

                // 5. الخزينة
                NavigationBarItem(
                    selected = currentTab == ZahraaTab.TREASURY,
                    onClick = { viewModel.selectTab(ZahraaTab.TREASURY) },
                    icon = { Icon(Icons.Filled.AccountBalance, contentDescription = "الخزينة") },
                    label = { Text("الخزينة", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                )

                // 6. الدفاتر القديمة
                NavigationBarItem(
                    selected = currentTab == ZahraaTab.OLD_LEDGERS,
                    onClick = { viewModel.selectTab(ZahraaTab.OLD_LEDGERS) },
                    icon = { Icon(Icons.Filled.Book, contentDescription = "الدفاتر") },
                    label = { Text("الدفاتر", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                )

                // 7. سمسم الذكي
                NavigationBarItem(
                    selected = currentTab == ZahraaTab.SIMSIM,
                    onClick = { viewModel.selectTab(ZahraaTab.SIMSIM) },
                    icon = {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = "سمسم",
                            tint = if (currentTab == ZahraaTab.SIMSIM) Color(0xFFD97706) else Color.Gray
                        )
                    },
                    label = { Text("سمسم", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                ZahraaTab.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                ZahraaTab.CUSTOMERS -> CustomersScreen(viewModel = viewModel)
                ZahraaTab.DEBTS -> CustomersScreen(viewModel = viewModel)
                ZahraaTab.PRODUCTS -> ProductsScreen(viewModel = viewModel)
                ZahraaTab.INVOICES -> InvoicesScreen(viewModel = viewModel)
                ZahraaTab.TREASURY -> TreasuryScreen(viewModel = viewModel)
                ZahraaTab.OLD_LEDGERS -> OldLedgersScreen(viewModel = viewModel)
                ZahraaTab.SIMSIM -> SimsimScreen(viewModel = viewModel)
            }
        }
    }

    // Modal Dialogs Handling
    if (showAddDebt) {
        AddCustomerDebtDialog(
            preselectedCustomer = preselectedCustomerForDebt,
            viewModel = viewModel,
            onDismiss = {
                viewModel.showAddDebtDialog.value = false
                viewModel.preselectedCustomerForDebt.value = null
            }
        )
    }

    if (paymentCustomer != null) {
        CustomerPaymentDialog(
            customer = paymentCustomer!!,
            viewModel = viewModel,
            onDismiss = { viewModel.showPaymentDialogForCustomer.value = null }
        )
    }

    if (ledgerCustomer != null) {
        CustomerLedgerDialog(
            customer = ledgerCustomer!!,
            viewModel = viewModel,
            onDismiss = { viewModel.showLedgerDialogForCustomer.value = null }
        )
    }

    if (showAddProduct) {
        AddProductDialog(
            productToEdit = productToEdit,
            viewModel = viewModel,
            onDismiss = {
                viewModel.showAddProductDialog.value = false
                viewModel.productToEdit.value = null
            }
        )
    }

    if (showAddPurchase) {
        AddPurchaseInvoiceDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.showAddPurchaseDialog.value = false }
        )
    }

    if (showAddExpense) {
        AddExpenseDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.showAddExpenseDialog.value = false }
        )
    }

    if (showSupabaseConfig) {
        com.example.ui.screens.SupabaseConfigDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeSupabaseConfig() }
        )
    }
}
