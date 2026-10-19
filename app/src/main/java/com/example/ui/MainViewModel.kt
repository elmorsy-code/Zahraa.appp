package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.SimsimAssistant
import com.example.data.AlZahraaRepository
import com.example.model.CashAccount
import com.example.model.CashTransaction
import com.example.model.Customer
import com.example.model.CustomerLedgerEntry
import com.example.model.DebtItemDraft
import com.example.model.Expense
import com.example.model.OldLedgerEntry
import com.example.model.Product
import com.example.model.PurchaseInvoice
import com.example.model.PurchaseInvoiceItem
import com.example.model.SimsimMessage
import com.example.model.Supplier
import com.example.network.supabase.SupabaseConfigManager
import com.example.network.supabase.SupabaseStatusInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class ZahraaTab(val titleAr: String) {
    DASHBOARD("الرئيسية"),
    CUSTOMERS("العملاء"),
    DEBTS("المديونيات"),
    PRODUCTS("الأدوية"),
    INVOICES("الفواتير"),
    TREASURY("الخزينة"),
    OLD_LEDGERS("الدفاتر القديمة"),
    SIMSIM("سمسم الذكي")
}

class MainViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository: AlZahraaRepository = AlZahraaRepository(application)
    private val configManager: SupabaseConfigManager = SupabaseConfigManager.getInstance(application)

    private val _currentTab = MutableStateFlow(ZahraaTab.DASHBOARD)
    val currentTab: StateFlow<ZahraaTab> = _currentTab.asStateFlow()

    // Supabase Cloud State
    val supabaseStatus: StateFlow<SupabaseStatusInfo> = configManager.status
    val showSupabaseConfigDialog = MutableStateFlow(false)
    val isSupabaseSyncing = MutableStateFlow(false)

    // تسجيل الدخول (نفس إيميل وكلمة مرور الموقع)
    val isSignedIn: StateFlow<Boolean> = repository.isSignedIn
    val isAuthLoading = MutableStateFlow(false)

    // Data streams from repository
    val customers: StateFlow<List<Customer>> = repository.customers
    val products: StateFlow<List<Product>> = repository.products
    val suppliers: StateFlow<List<Supplier>> = repository.suppliers
    val cashAccounts: StateFlow<List<CashAccount>> = repository.cashAccounts
    val expenses: StateFlow<List<Expense>> = repository.expenses
    val oldLedgers: StateFlow<List<OldLedgerEntry>> = repository.oldLedgers
    val purchaseInvoices: StateFlow<List<PurchaseInvoice>> = repository.purchaseInvoices
    val cashTransactions: StateFlow<List<CashTransaction>> = repository.cashTransactions

    // Dialog & Modal States
    val showAddDebtDialog = MutableStateFlow(false)
    val preselectedCustomerForDebt = MutableStateFlow<Customer?>(null)

    val showPaymentDialogForCustomer = MutableStateFlow<Customer?>(null)
    val showLedgerDialogForCustomer = MutableStateFlow<Customer?>(null)

    val showAddProductDialog = MutableStateFlow(false)
    val productToEdit = MutableStateFlow<Product?>(null)

    val showAddPurchaseDialog = MutableStateFlow(false)
    val showAddExpenseDialog = MutableStateFlow(false)

    // Searches & Filters
    val customerSearch = MutableStateFlow("")
    val filterDebtorsOnly = MutableStateFlow(false)

    val productSearch = MutableStateFlow("")
    val selectedCategoryFilter = MutableStateFlow("الكل")

    val debtSearch = MutableStateFlow("")

    // Simsim Messages Flow
    private val _simsimMessages = MutableStateFlow<List<SimsimMessage>>(
        listOf(
            SimsimMessage(
                id = "welcome",
                sender = "simsim",
                text = "أهلاً بك يا فندم! أنا \"سمسم\" 🤖 المساعد الذكي لصرح الزهراء للأدوية والمبيدات الزراعية.\n\nأقدر أفتح لك أي شاشة، أراجع الخزينة والسيولة، كشوف حسابات وديون المزارعين، أسجل مديونيات أو أصناف ومصروفات، أو أجاوبك على أي استشارة زراعية وأدوية ومبيدات. اطلب مني أي حاجة وأنا في خدمتك فوراً!",
                timestamp = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            )
        )
    )
    val simsimMessages: StateFlow<List<SimsimMessage>> = _simsimMessages.asStateFlow()

    val isSimsimTyping = MutableStateFlow(false)

    fun selectTab(tab: ZahraaTab) {
        _currentTab.value = tab
    }

    // Filtered Customers
    val filteredCustomers: StateFlow<List<Customer>> = combine(
        customers,
        customerSearch,
        filterDebtorsOnly
    ) { list, query, debtorsOnly ->
        list.filter { c ->
            val matchesQuery = query.isBlank() ||
                    c.name.contains(query, ignoreCase = true) ||
                    (c.partnerName != null && c.partnerName.contains(query, ignoreCase = true)) ||
                    c.code.contains(query, ignoreCase = true) ||
                    (c.phone != null && c.phone.contains(query)) ||
                    (c.villageOrAddress != null && c.villageOrAddress.contains(query, ignoreCase = true))

            val matchesDebt = !debtorsOnly || c.currentBalance > 0.01
            matchesQuery && matchesDebt
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Products
    val filteredProducts: StateFlow<List<Product>> = combine(
        products,
        productSearch,
        selectedCategoryFilter
    ) { list, query, cat ->
        list.filter { p ->
            val matchesQuery = query.isBlank() ||
                    p.name.contains(query, ignoreCase = true) ||
                    (p.activeIngredient != null && p.activeIngredient.contains(query, ignoreCase = true)) ||
                    p.categoryName.contains(query, ignoreCase = true) ||
                    (p.weightOrSize != null && p.weightOrSize.contains(query, ignoreCase = true)) ||
                    p.code.contains(query, ignoreCase = true)

            val matchesCat = cat == "الكل" || p.categoryName == cat
            matchesQuery && matchesCat
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Computed Financial KPI Totals
    val totalCash: StateFlow<Double> = cashAccounts.combine(MutableStateFlow(Unit)) { accs, _ ->
        accs.sumOf { it.currentBalance }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalCustomerDebt: StateFlow<Double> = customers.combine(MutableStateFlow(Unit)) { custs, _ ->
        custs.sumOf { maxOf(0.0, it.currentBalance) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalSupplierDebt: StateFlow<Double> = suppliers.combine(MutableStateFlow(Unit)) { supps, _ ->
        supps.sumOf { maxOf(0.0, it.currentBalance) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun getCustomerLedger(customerId: String): List<CustomerLedgerEntry> {
        return repository.getLedgerForCustomer(customerId)
    }

    fun addCustomerDebt(
        customerId: String,
        invoiceNumber: String?,
        items: List<DebtItemDraft>,
        purchaseDate: String,
        notes: String?
    ) {
        viewModelScope.launch {
            repository.addCustomerDebt(customerId, invoiceNumber, items, purchaseDate, notes)
            showAddDebtDialog.value = false
            preselectedCustomerForDebt.value = null
        }
    }

    fun receiveCustomerPayment(
        customerId: String,
        amount: Double,
        cashAccountId: String,
        paymentMethod: String,
        notes: String?
    ) {
        viewModelScope.launch {
            repository.receiveCustomerPayment(customerId, amount, cashAccountId, paymentMethod, notes)
            showPaymentDialogForCustomer.value = null
        }
    }

    fun saveCustomer(
        id: String?,
        name: String,
        partnerName: String?,
        phone: String?,
        village: String?,
        openingBalance: Double
    ) {
        viewModelScope.launch {
            repository.saveCustomer(id, name, partnerName, phone, village, openingBalance)
        }
    }

    fun deleteCustomer(id: String) {
        viewModelScope.launch {
            repository.deleteCustomer(id)
        }
    }

    fun saveProduct(
        id: String?,
        name: String,
        commercialName: String?,
        activeIngredient: String?,
        categoryName: String,
        weightOrSize: String?,
        salePrice: Double,
        purchasePrice: Double,
        imageUrl: String?,
        description: String?
    ) {
        viewModelScope.launch {
            repository.saveProduct(
                id, name, commercialName, activeIngredient, categoryName,
                weightOrSize, salePrice, purchasePrice, imageUrl, description
            )
            showAddProductDialog.value = false
            productToEdit.value = null
        }
    }

    fun deleteProduct(id: String) {
        viewModelScope.launch {
            repository.deleteProduct(id)
        }
    }

    fun savePurchaseInvoice(
        supplierName: String,
        supplierPhone: String?,
        invoiceNumber: String?,
        invoiceDate: String,
        paymentType: String,
        paidAmount: Double,
        discountAmount: Double,
        items: List<PurchaseInvoiceItem>,
        notes: String?
    ) {
        viewModelScope.launch {
            repository.savePurchaseInvoice(
                supplierName, supplierPhone, invoiceNumber, invoiceDate,
                paymentType, paidAmount, discountAmount, items, notes
            )
            showAddPurchaseDialog.value = false
        }
    }

    fun recordExpense(
        category: String,
        amount: Double,
        cashAccountId: String,
        paidTo: String?,
        notes: String?
    ) {
        viewModelScope.launch {
            repository.recordExpense(category, amount, cashAccountId, paidTo, notes)
            showAddExpenseDialog.value = false
        }
    }

    fun migrateOldLedger(entryId: String) {
        viewModelScope.launch {
            repository.migrateOldLedger(entryId)
        }
    }

    // Send Message to Simsim Assistant
    fun sendSimsimMessage(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isBlank()) return

        val nowStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        val userMsg = SimsimMessage(
            id = "user_" + UUID.randomUUID().toString().take(8),
            sender = "user",
            text = trimmed,
            timestamp = nowStr
        )
        _simsimMessages.value = _simsimMessages.value + userMsg

        viewModelScope.launch {
            isSimsimTyping.value = true
            kotlinx.coroutines.delay(500)

            val context = SimsimAssistant.LiveContext(
                totalProducts = products.value.size,
                totalCash = totalCash.value,
                totalCustomerDebt = totalCustomerDebt.value,
                totalSupplierDebt = totalSupplierDebt.value,
                lowStockCount = products.value.count { it.totalStock <= it.minStockAlert }
            )

            val response = SimsimAssistant.resolveSmartResponse(trimmed, context)
            _simsimMessages.value = _simsimMessages.value + response
            isSimsimTyping.value = false

            // Check if response has navigation action
            when (response.actionTarget) {
                "dashboard" -> selectTab(ZahraaTab.DASHBOARD)
                "customers" -> selectTab(ZahraaTab.CUSTOMERS)
                "debts" -> selectTab(ZahraaTab.DEBTS)
                "products" -> selectTab(ZahraaTab.PRODUCTS)
                "invoices" -> selectTab(ZahraaTab.INVOICES)
                "treasury" -> selectTab(ZahraaTab.TREASURY)
                "old_ledgers" -> selectTab(ZahraaTab.OLD_LEDGERS)
                "open_add_debt" -> showAddDebtDialog.value = true
                "open_add_product" -> showAddProductDialog.value = true
                "open_add_expense" -> showAddExpenseDialog.value = true
            }
        }
    }

    // --- Supabase Cloud Operations ---

    fun openSupabaseConfig() {
        showSupabaseConfigDialog.value = true
    }

    fun closeSupabaseConfig() {
        showSupabaseConfigDialog.value = false
    }

    fun isSupabaseConfigured(): Boolean = configManager.isConfigured()

    fun signIn(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            isAuthLoading.value = true
            val result = repository.signIn(email, password)
            isAuthLoading.value = false
            result.onSuccess { msg -> onResult(true, msg) }
                .onFailure { err -> onResult(false, err.localizedMessage ?: "فشل تسجيل الدخول") }
        }
    }

    fun signOut() {
        repository.signOut()
    }

    fun syncSupabase(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            isSupabaseSyncing.value = true
            val result = repository.syncFromSupabase()
            isSupabaseSyncing.value = false
            result.onSuccess { msg -> onResult(true, msg) }
                .onFailure { err -> onResult(false, err.localizedMessage ?: "فشلت المزامنة") }
        }
    }

    fun pushInitialSeedToSupabase(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            isSupabaseSyncing.value = true
            val result = repository.pushSeedDataToSupabase()
            isSupabaseSyncing.value = false
            result.onSuccess { msg -> onResult(true, msg) }
                .onFailure { err -> onResult(false, err.localizedMessage ?: "فشل الرفع إلى Supabase") }
        }
    }

    fun testAndSaveSupabaseConfig(
        url: String,
        key: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            isSupabaseSyncing.value = true
            val testRes = repository.testSupabaseConnection(url, key)
            if (testRes.isSuccess) {
                configManager.saveConfig(url, key)
                // Automatically trigger sync right after saving
                repository.syncFromSupabase()
                isSupabaseSyncing.value = false
                onResult(true, "تم الاتصال وحفظ الإعدادات والمزامنة بنجاح!")
            } else {
                isSupabaseSyncing.value = false
                onResult(false, testRes.exceptionOrNull()?.message ?: "فشل الاتصال بـ Supabase")
            }
        }
    }

    fun disconnectSupabase() {
        repository.signOut()
        configManager.clearConfig()
    }
}
