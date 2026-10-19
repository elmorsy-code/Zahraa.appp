package com.example.data

import android.content.Context
import android.util.Log
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
import com.example.model.Supplier
import com.example.network.supabase.SupabaseCashAccountDto
import com.example.network.supabase.SupabaseClientProvider
import com.example.network.supabase.SupabaseConfigManager
import com.example.network.supabase.SupabaseConnectionState
import com.example.network.supabase.SupabaseSessionManager
import com.example.network.supabase.SupabaseCustomerDto
import com.example.network.supabase.SupabaseCustomerLedgerEntryDto
import com.example.network.supabase.SupabaseExpenseDto
import com.example.network.supabase.SupabaseOldLedgerDto
import com.example.network.supabase.SupabaseProductDto
import com.example.network.supabase.SupabaseStatusInfo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import retrofit2.HttpException

class AlZahraaRepository(
    private val context: Context? = null
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val configManager: SupabaseConfigManager? = context?.let { SupabaseConfigManager.getInstance(it) }

    private val sessionManager: SupabaseSessionManager? =
        context?.let { SupabaseSessionManager.getInstance(it) }

    /** هل المستخدم مسجل دخول على Supabase؟ (من غيره القاعدة بترجع بيانات فاضية بسبب RLS) */
    val isSignedIn: StateFlow<Boolean> =
        sessionManager?.signedIn ?: MutableStateFlow(false).asStateFlow()

    // كل البيانات بتيجي من قاعدة البيانات. بنبدأ فاضيين لحد ما أول مزامنة تخلص.
    private val _customers = MutableStateFlow<List<Customer>>(emptyList())
    val customers: StateFlow<List<Customer>> = _customers.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _suppliers = MutableStateFlow<List<Supplier>>(emptyList())
    val suppliers: StateFlow<List<Supplier>> = _suppliers.asStateFlow()

    private val _cashAccounts = MutableStateFlow<List<CashAccount>>(emptyList())
    val cashAccounts: StateFlow<List<CashAccount>> = _cashAccounts.asStateFlow()

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    private val _oldLedgers = MutableStateFlow<List<OldLedgerEntry>>(emptyList())
    val oldLedgers: StateFlow<List<OldLedgerEntry>> = _oldLedgers.asStateFlow()

    private val _purchaseInvoices = MutableStateFlow<List<PurchaseInvoice>>(emptyList())
    val purchaseInvoices: StateFlow<List<PurchaseInvoice>> = _purchaseInvoices.asStateFlow()

    private val _customerLedgers = MutableStateFlow<Map<String, List<CustomerLedgerEntry>>>(emptyMap())
    val customerLedgers: StateFlow<Map<String, List<CustomerLedgerEntry>>> = _customerLedgers.asStateFlow()

    private val _cashTransactions = MutableStateFlow<List<CashTransaction>>(emptyList())
    val cashTransactions: StateFlow<List<CashTransaction>> = _cashTransactions.asStateFlow()

    val supabaseStatus: StateFlow<SupabaseStatusInfo>? = configManager?.status

    init {
        SupabaseClientProvider.sessionManager = sessionManager
        if (configManager?.isConfigured() == true && sessionManager?.hasSession() == true) {
            scope.launch {
                syncFromSupabase()
            }
        }
    }

    private fun ensureUuid(rawId: String): String {
        return try {
            UUID.fromString(rawId).toString()
        } catch (_: Exception) {
            UUID.nameUUIDFromBytes(rawId.toByteArray()).toString()
        }
    }

    private fun getSupabaseApi() = configManager?.let {
        SupabaseClientProvider.sessionManager = sessionManager
        val url = it.getSupabaseUrl()
        val key = it.getSupabaseAnonKey()
        if (it.isConfigured(url, key)) {
            SupabaseClientProvider.getApiService(url, key)
        } else null
    }

    // --- Core Supabase Sync Operations ---

    /** تسجيل الدخول بنفس إيميل وكلمة مرور الموقع، وبعدها سحب كل البيانات. */
    suspend fun signIn(email: String, password: String): Result<String> {
        val cfg = configManager ?: return Result.failure(Exception("الإعدادات غير متاحة"))
        val session = sessionManager ?: return Result.failure(Exception("الجلسة غير متاحة"))
        if (!cfg.isConfigured()) {
            return Result.failure(Exception("لم يتم ضبط رابط Supabase ومفتاح Anon بعد."))
        }
        val result = session.signIn(cfg.getSupabaseUrl(), cfg.getSupabaseAnonKey(), email, password)
        if (result.isFailure) {
            return Result.failure(result.exceptionOrNull() ?: Exception("فشل تسجيل الدخول"))
        }
        return syncFromSupabase()
    }

    fun signOut() {
        sessionManager?.signOut()
        clearLocalData()
    }

    private fun clearLocalData() {
        _customers.value = emptyList()
        _products.value = emptyList()
        _suppliers.value = emptyList()
        _cashAccounts.value = emptyList()
        _expenses.value = emptyList()
        _oldLedgers.value = emptyList()
        _purchaseInvoices.value = emptyList()
        _customerLedgers.value = emptyMap()
        _cashTransactions.value = emptyList()
    }

    private suspend fun <T> fetchAllPages(
        pageSize: Int = 1000,
        fetch: suspend (limit: Int, offset: Int) -> List<T>
    ): List<T> {
        val all = mutableListOf<T>()
        var offset = 0
        while (true) {
            val page = fetch(pageSize, offset)
            all.addAll(page)
            if (page.size < pageSize) break
            offset += pageSize
        }
        return all
    }

    /** بيرجع null ويسجل سبب الفشل بدل ما يوقف المزامنة كلها. الـ 401 بس بيوقفها (الجلسة خلصت). */
    private suspend fun <T> fetchOrNull(
        failures: MutableList<String>,
        label: String,
        block: suspend () -> T
    ): T? {
        return try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            if (e.code() == 401) throw e
            failures.add("$label (خطأ ${e.code()})")
            null
        } catch (e: Exception) {
            failures.add("$label: ${e.localizedMessage}")
            null
        }
    }

    suspend fun syncFromSupabase(): Result<String> = withContext(Dispatchers.IO) {
        val api = getSupabaseApi()
            ?: return@withContext Result.failure(Exception("Supabase غير مهيأ بعد. يرجى إدخال الرابط والمفتاح."))
        if (sessionManager?.hasSession() != true) {
            return@withContext Result.failure(Exception("سجّل الدخول الأول عشان تشوف بيانات المؤسسة."))
        }

        configManager?.updateStatus(SupabaseConnectionState.CONNECTING, "جاري سحب البيانات من قاعدة البيانات...")
        val failures = mutableListOf<String>()
        try {
            // 1. العملاء
            val remoteCustomers = fetchOrNull(failures, "العملاء") {
                fetchAllPages { limit, offset -> api.getCustomers(limit = limit, offset = offset) }
            }
            if (remoteCustomers != null) _customers.value = remoteCustomers.map { it.toDomain() }

            // 2. الأصناف مع أرصدة المحل والمخزن
            val warehouses = fetchOrNull(failures, "المخازن") { api.getWarehouses() }
            val balances = fetchOrNull(failures, "أرصدة المخزون") {
                fetchAllPages { limit, offset -> api.getStockBalances(limit = limit, offset = offset) }
            }
            val remoteProducts = fetchOrNull(failures, "الأصناف") {
                fetchAllPages { limit, offset -> api.getProducts(limit = limit, offset = offset) }
            }
            if (remoteProducts != null) {
                val shopIds = warehouses.orEmpty()
                    .filter { it.locationType == "shop" }
                    .map { it.id }
                    .toSet()
                val balancesByProduct = balances.orEmpty().groupBy { it.productId }
                _products.value = remoteProducts.map { dto ->
                    val rows = balancesByProduct[dto.id].orEmpty()
                    val shop = rows.filter { it.warehouseId in shopIds }.sumOf { it.qty ?: 0.0 }.toInt()
                    val warehouse = rows.filter { it.warehouseId !in shopIds }.sumOf { it.qty ?: 0.0 }.toInt()
                    dto.toDomain().copy(stockShop = shop, stockWarehouse = warehouse, totalStock = shop + warehouse)
                }
            }

            // 3. كشوف حساب العملاء
            val remoteLedger = fetchOrNull(failures, "كشوف الحساب") {
                fetchAllPages { limit, offset -> api.getCustomerLedgerEntries(limit = limit, offset = offset) }
            }
            if (remoteLedger != null) {
                _customerLedgers.value = remoteLedger.groupBy({ it.customerId }, { it.toDomain() })
            }

            // 4. الموردين
            fetchOrNull(failures, "الموردين") { api.getSuppliers() }
                ?.let { list -> _suppliers.value = list.map { it.toDomain() } }

            // 5. الخزائن وحركاتها والمصروفات
            fetchOrNull(failures, "الخزائن") { api.getCashAccounts() }
                ?.let { list -> _cashAccounts.value = list.map { it.toDomain() } }
            fetchOrNull(failures, "حركات الخزينة") { api.getCashTransactions(limit = 500) }
                ?.let { list -> _cashTransactions.value = list.map { it.toDomain() } }
            fetchOrNull(failures, "المصروفات") { api.getExpenses(limit = 500) }
                ?.let { list -> _expenses.value = list.map { it.toDomain() } }

            // 6. الدفاتر القديمة
            fetchOrNull(failures, "الدفاتر القديمة") { api.getOldLedgers() }
                ?.let { list -> _oldLedgers.value = list.map { it.toDomain() } }

            // 7. فواتير الشراء
            fetchOrNull(failures, "فواتير الشراء") { api.getPurchaseInvoicesWithItems() }
                ?.let { list -> _purchaseInvoices.value = list.map { it.toDomain() } }

            val nowStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            val summary = "تم جلب ${_customers.value.size} عميل و ${_products.value.size} صنف."
            val message = if (failures.isEmpty()) {
                "تمت المزامنة بنجاح مع قاعدة البيانات"
            } else {
                "تمت المزامنة مع تعذر جلب: " + failures.joinToString("، ")
            }
            configManager?.updateStatus(
                state = SupabaseConnectionState.CONNECTED,
                message = message,
                customersCount = _customers.value.size,
                productsCount = _products.value.size,
                syncTime = nowStr
            )
            Result.success(if (failures.isEmpty()) summary else "$summary ($message)")
        } catch (e: HttpException) {
            if (e.code() == 401) {
                // التوكن مرفوض: نطلع المستخدم لشاشة الدخول
                signOut()
                configManager?.updateStatus(SupabaseConnectionState.CONNECTED, "انتهت الجلسة، سجّل الدخول من جديد")
                Result.failure(Exception("انتهت الجلسة، سجّل الدخول من جديد"))
            } else {
                Log.e("AlZahraaRepository", "Supabase sync error", e)
                configManager?.updateStatus(SupabaseConnectionState.ERROR, "فشل الاتصال بقاعدة البيانات (خطأ ${e.code()})")
                Result.failure(e)
            }
        } catch (e: Exception) {
            Log.e("AlZahraaRepository", "Supabase sync error", e)
            configManager?.updateStatus(
                state = SupabaseConnectionState.ERROR,
                message = "فشل الاتصال بقاعدة البيانات: ${e.localizedMessage}"
            )
            Result.failure(e)
        }
    }

    /** اتوقف عن قصد: التطبيق بقى بيشتغل على قاعدة البيانات الحقيقية ومينفعش نرفعلها بيانات تجريبية. */
    suspend fun pushSeedDataToSupabase(): Result<String> = withContext(Dispatchers.IO) {
        Result.failure(Exception("رفع البيانات التجريبية اتوقف: التطبيق بيقرأ من قاعدة بيانات المؤسسة الحقيقية."))
    }

    suspend fun testSupabaseConnection(url: String, key: String): Result<String> = withContext(Dispatchers.IO) {
        if (!configManager!!.isConfigured(url, key)) {
            return@withContext Result.failure(Exception("يرجى إدخال رابط يبدأ بـ https:// ومفتاح صالح"))
        }
        val api = SupabaseClientProvider.getApiService(url, key)
            ?: return@withContext Result.failure(Exception("تعذر إنشاء اتصال بالرابط المحدد"))

        try {
            // Ping by selecting customers limit 1
            api.getCustomers(select = "id", order = "name.asc")
            Result.success("الاتصال بقاعدة بيانات Supabase ناجح 100%!")
        } catch (e: Exception) {
            Result.failure(Exception("فشل الاتصال: تأكد من صحة الرابط ومفتاح Anon Key وصحة إنشاء جدول customers. التفاصيل: ${e.message}"))
        }
    }

    fun getCustomerById(id: String): Customer? = _customers.value.find { it.id == id }

    fun getLedgerForCustomer(customerId: String): List<CustomerLedgerEntry> {
        return _customerLedgers.value[customerId] ?: emptyList()
    }

    // Core Business Function: Add a new debt to a customer
    fun addCustomerDebt(
        customerId: String,
        invoiceNumber: String?,
        items: List<DebtItemDraft>,
        purchaseDate: String,
        notes: String?
    ): Boolean {
        val customer = getCustomerById(customerId) ?: return false
        val totalDebt = items.sumOf { it.totalPrice }
        if (totalDebt <= 0) return false

        val docNum = if (!invoiceNumber.isNullOrBlank()) invoiceNumber.trim() else "INV-${1000 + (1..8999).random()}"

        val descParts = items.map { item ->
            val name = item.itemName.trim()
            if (item.isSubQuantity) {
                "$name [كمية فرعية: ${item.subQuantity} ${item.subUnit} من أصل ${item.fullCapacity} ${item.subUnit} (${item.packageType}) بسعر ${item.totalPrice} ج.م]"
            } else {
                "$name [كمية: ${item.quantity} ${item.unit} بسعر ${if (item.unitPrice > 0) item.unitPrice else item.totalPrice} ج.م]"
            }
        }
        val fullDesc = descParts.joinToString(" + ") + (if (!notes.isNullOrBlank()) " ($notes)" else "")

        val newBalance = customer.currentBalance + totalDebt
        val entryId = ensureUuid("le_" + UUID.randomUUID().toString())
        val customerUuid = ensureUuid(customerId)

        val newEntry = CustomerLedgerEntry(
            id = entryId,
            customerId = customerId,
            entryDate = purchaseDate,
            entryType = "invoice",
            documentNumber = docNum,
            description = fullDesc,
            debit = totalDebt,
            credit = 0.0,
            runningBalance = newBalance
        )

        // Update In-Memory UI state
        val currentMap = _customerLedgers.value.toMutableMap()
        val customerEntries = (currentMap[customerId] ?: emptyList()) + newEntry
        currentMap[customerId] = customerEntries
        _customerLedgers.value = currentMap

        _customers.value = _customers.value.map {
            if (it.id == customerId) it.copy(currentBalance = newBalance) else it
        }

        // Push to Supabase asynchronously
        scope.launch {
            val api = getSupabaseApi() ?: return@launch
            runCatching {
                api.insertLedgerEntry(
                    SupabaseCustomerLedgerEntryDto(
                        id = entryId,
                        customerId = customerUuid,
                        entryDate = purchaseDate,
                        entryType = "invoice",
                        documentNumber = docNum,
                        description = fullDesc,
                        debit = totalDebt,
                        credit = 0.0,
                        runningBalance = newBalance
                    )
                )
                api.updateCustomer(
                    idFilter = "eq.$customerUuid",
                    customer = mapOf("current_balance" to newBalance)
                )
            }.onFailure { Log.e("AlZahraaRepository", "Failed to push debt to Supabase", it) }
        }

        return true
    }

    // Receive payment from customer
    fun receiveCustomerPayment(
        customerId: String,
        amount: Double,
        cashAccountId: String,
        paymentMethod: String,
        notes: String?
    ): Boolean {
        val customer = getCustomerById(customerId) ?: return false
        if (amount <= 0) return false

        val cashAcc = _cashAccounts.value.find { it.id == cashAccountId } ?: _cashAccounts.value.first()
        val paymentNum = "RCP-${100 + (1..899).random()}"
        val nowStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

        val newCustomerBalance = customer.currentBalance - amount
        val entryId = ensureUuid("le_" + UUID.randomUUID().toString())
        val customerUuid = ensureUuid(customerId)

        val newEntry = CustomerLedgerEntry(
            id = entryId,
            customerId = customerId,
            entryDate = nowStr,
            entryType = "payment",
            documentNumber = paymentNum,
            description = "سند قبض وتحصيل نقدية (${when(paymentMethod) { "bank" -> "تحويل بنكي"; "wallet" -> "محفظة كاش"; else -> "كاش نقداً" }})" + (if (!notes.isNullOrBlank()) " - $notes" else ""),
            debit = 0.0,
            credit = amount,
            runningBalance = newCustomerBalance
        )

        // Update Local State
        val currentMap = _customerLedgers.value.toMutableMap()
        currentMap[customerId] = (currentMap[customerId] ?: emptyList()) + newEntry
        _customerLedgers.value = currentMap

        _customers.value = _customers.value.map {
            if (it.id == customerId) it.copy(currentBalance = newCustomerBalance) else it
        }

        val newCashBalance = cashAcc.currentBalance + amount
        _cashAccounts.value = _cashAccounts.value.map {
            if (it.id == cashAcc.id) it.copy(currentBalance = newCashBalance) else it
        }

        val txId = ensureUuid("tx_" + UUID.randomUUID().toString())
        val tx = CashTransaction(
            id = txId,
            transactionNumber = "CSH-${1000 + (1..8999).random()}",
            cashAccountName = cashAcc.name,
            transactionDate = nowStr,
            transactionType = "customer_receipt",
            amount = amount,
            direction = "in",
            balanceAfter = newCashBalance,
            partyName = customer.name,
            description = "تحصيل نقدي بموجب سند قبض رقم $paymentNum"
        )
        _cashTransactions.value = listOf(tx) + _cashTransactions.value

        // Push to Supabase asynchronously
        scope.launch {
            val api = getSupabaseApi() ?: return@launch
            runCatching {
                api.insertLedgerEntry(
                    SupabaseCustomerLedgerEntryDto(
                        id = entryId,
                        customerId = customerUuid,
                        entryDate = nowStr,
                        entryType = "payment",
                        documentNumber = paymentNum,
                        description = newEntry.description,
                        debit = 0.0,
                        credit = amount,
                        runningBalance = newCustomerBalance
                    )
                )
                api.updateCustomer(
                    idFilter = "eq.$customerUuid",
                    customer = mapOf("current_balance" to newCustomerBalance)
                )
            }.onFailure { Log.e("AlZahraaRepository", "Failed to push payment to Supabase", it) }
        }

        return true
    }

    // Add or Edit Customer
    fun saveCustomer(
        id: String?,
        name: String,
        partnerName: String?,
        phone: String?,
        village: String?,
        openingBalance: Double = 0.0
    ): Customer {
        if (!id.isNullOrBlank()) {
            val existing = getCustomerById(id)
            if (existing != null) {
                val updated = existing.copy(
                    name = name.trim(),
                    partnerName = partnerName?.trim()?.ifBlank { null },
                    phone = phone?.trim()?.ifBlank { null },
                    villageOrAddress = village?.trim()?.ifBlank { null }
                )
                _customers.value = _customers.value.map { if (it.id == id) updated else it }

                scope.launch {
                    val api = getSupabaseApi() ?: return@launch
                    val customerUuid = ensureUuid(existing.id)
                    runCatching {
                        api.updateCustomer(
                            idFilter = "eq.$customerUuid",
                            customer = mapOf(
                                "name" to updated.name,
                                "partner_name" to updated.partnerName,
                                "phone" to updated.phone,
                                "village_or_address" to updated.villageOrAddress
                            )
                        )
                    }
                }
                return updated
            }
        }
        val nextIndex = _customers.value.size + 1
        val code = "C" + String.format(Locale.US, "%03d", nextIndex)
        val customerUuid = UUID.randomUUID().toString()
        val newCust = Customer(
            id = customerUuid,
            code = code,
            name = name.trim(),
            partnerName = partnerName?.trim()?.ifBlank { null },
            phone = phone?.trim()?.ifBlank { null },
            villageOrAddress = village?.trim()?.ifBlank { null },
            currentBalance = openingBalance,
            openingBalance = openingBalance
        )
        _customers.value = listOf(newCust) + _customers.value

        scope.launch {
            val api = getSupabaseApi() ?: return@launch
            runCatching {
                api.insertCustomer(SupabaseCustomerDto.fromDomain(newCust))
            }.onFailure { Log.e("AlZahraaRepository", "Failed to insert customer to Supabase", it) }
        }

        return newCust
    }

    fun deleteCustomer(id: String) {
        _customers.value = _customers.value.filter { it.id != id }
        scope.launch {
            val api = getSupabaseApi() ?: return@launch
            val customerUuid = ensureUuid(id)
            runCatching {
                api.deleteCustomer(idFilter = "eq.$customerUuid")
            }
        }
    }

    // Add or Edit Medicine Product
    fun saveProduct(
        id: String?,
        name: String,
        commercialName: String?,
        activeIngredient: String?,
        categoryName: String,
        weightOrSize: String?,
        salePrice: Double,
        purchasePrice: Double = 0.0,
        imageUrl: String?,
        description: String?
    ): Product {
        if (!id.isNullOrBlank()) {
            val existing = _products.value.find { it.id == id }
            if (existing != null) {
                val updated = existing.copy(
                    name = name.trim(),
                    commercialName = commercialName?.trim()?.ifBlank { null },
                    activeIngredient = activeIngredient?.trim()?.ifBlank { null },
                    categoryName = categoryName.trim(),
                    weightOrSize = weightOrSize?.trim()?.ifBlank { null },
                    defaultSalePrice = salePrice,
                    defaultPurchasePrice = if (purchasePrice > 0) purchasePrice else existing.defaultPurchasePrice,
                    imageUrl = imageUrl?.trim()?.ifBlank { null },
                    description = description?.trim()?.ifBlank { null }
                )
                _products.value = _products.value.map { if (it.id == id) updated else it }

                scope.launch {
                    val api = getSupabaseApi() ?: return@launch
                    val prodUuid = ensureUuid(existing.id)
                    runCatching {
                        api.updateProduct(
                            idFilter = "eq.$prodUuid",
                            product = mapOf(
                                "name" to updated.name,
                                "commercial_name" to updated.commercialName,
                                "active_ingredient" to updated.activeIngredient,
                                "default_sale_price" to updated.defaultSalePrice,
                                "default_purchase_price" to updated.defaultPurchasePrice,
                                "image_url" to updated.imageUrl,
                                "description" to updated.description
                            )
                        )
                    }
                }
                return updated
            }
        }
        val nextIndex = _products.value.size + 101
        val prodUuid = UUID.randomUUID().toString()
        val newProd = Product(
            id = prodUuid,
            code = "PRD-$nextIndex",
            name = name.trim(),
            commercialName = commercialName?.trim()?.ifBlank { null },
            activeIngredient = activeIngredient?.trim()?.ifBlank { null },
            categoryName = categoryName.trim(),
            weightOrSize = weightOrSize?.trim()?.ifBlank { null },
            defaultPurchasePrice = purchasePrice,
            defaultSalePrice = salePrice,
            stockShop = 10,
            stockWarehouse = 20,
            totalStock = 30,
            imageUrl = imageUrl?.trim()?.ifBlank { null },
            description = description?.trim()?.ifBlank { null }
        )
        _products.value = listOf(newProd) + _products.value

        scope.launch {
            val api = getSupabaseApi() ?: return@launch
            runCatching {
                api.insertProduct(SupabaseProductDto.fromDomain(newProd))
            }.onFailure { Log.e("AlZahraaRepository", "Failed to insert product to Supabase", it) }
        }

        return newProd
    }

    fun deleteProduct(id: String) {
        _products.value = _products.value.filter { it.id != id }
        scope.launch {
            val api = getSupabaseApi() ?: return@launch
            val prodUuid = ensureUuid(id)
            runCatching {
                api.deleteProduct(idFilter = "eq.$prodUuid")
            }
        }
    }

    // Add Purchase Invoice from supplier
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
    ): Boolean {
        if (items.isEmpty()) return false
        val subtotal = items.sumOf { it.totalCost }
        val total = (subtotal - discountAmount).coerceAtLeast(0.0)
        val paid = when(paymentType) {
            "cash" -> total
            "credit" -> 0.0
            else -> paidAmount.coerceIn(0.0, total)
        }
        val remaining = (total - paid).coerceAtLeast(0.0)

        var supp = _suppliers.value.find { it.name.trim().equals(supplierName.trim(), ignoreCase = true) }
        if (supp == null) {
            val nextIdx = _suppliers.value.size + 1
            supp = Supplier(
                id = ensureUuid("su_" + UUID.randomUUID().toString()),
                code = "SUPP-$nextIdx",
                name = supplierName.trim(),
                phone = supplierPhone?.trim()?.ifBlank { null },
                currentBalance = remaining
            )
            _suppliers.value = _suppliers.value + supp
        } else {
            val newSuppBalance = supp.currentBalance + remaining
            _suppliers.value = _suppliers.value.map { if (it.id == supp?.id) it.copy(currentBalance = newSuppBalance) else it }
        }

        val invNum = if (!invoiceNumber.isNullOrBlank()) invoiceNumber.trim() else "PUR-${1000 + (1..8999).random()}"
        val inv = PurchaseInvoice(
            id = ensureUuid("pur_" + UUID.randomUUID().toString()),
            invoiceNumber = invNum,
            supplierName = supplierName.trim(),
            invoiceDate = invoiceDate,
            paymentType = paymentType,
            subtotal = subtotal,
            discountAmount = discountAmount,
            totalAmount = total,
            paidAmount = paid,
            remainingAmount = remaining,
            notes = notes,
            items = items
        )
        _purchaseInvoices.value = listOf(inv) + _purchaseInvoices.value

        if (paid > 0) {
            val cashAcc = _cashAccounts.value.first()
            val newCash = cashAcc.currentBalance - paid
            _cashAccounts.value = _cashAccounts.value.map { if (it.id == cashAcc.id) it.copy(currentBalance = newCash) else it }

            val tx = CashTransaction(
                id = ensureUuid("tx_" + UUID.randomUUID().toString()),
                transactionNumber = "CSH-${1000 + (1..8999).random()}",
                cashAccountName = cashAcc.name,
                transactionDate = invoiceDate,
                transactionType = "supplier_payment",
                amount = paid,
                direction = "out",
                balanceAfter = newCash,
                partyName = supplierName,
                description = "سداد دفعة مشتريات فاتورة رقم $invNum"
            )
            _cashTransactions.value = listOf(tx) + _cashTransactions.value
        }

        return true
    }

    // Record operational expense
    fun recordExpense(
        category: String,
        amount: Double,
        cashAccountId: String,
        paidTo: String?,
        notes: String?
    ): Boolean {
        if (amount <= 0) return false
        val cashAcc = _cashAccounts.value.find { it.id == cashAccountId } ?: _cashAccounts.value.first()
        val nowStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val expNum = "EXP-${100 + (1..899).random()}"

        val newCash = cashAcc.currentBalance - amount
        _cashAccounts.value = _cashAccounts.value.map { if (it.id == cashAcc.id) it.copy(currentBalance = newCash) else it }

        val expId = ensureUuid("exp_" + UUID.randomUUID().toString())
        val exp = Expense(
            id = expId,
            expenseNumber = expNum,
            category = category,
            amount = amount,
            expenseDate = nowStr,
            cashAccountName = cashAcc.name,
            paidTo = paidTo?.trim()?.ifBlank { null },
            notes = notes?.trim()?.ifBlank { null }
        )
        _expenses.value = listOf(exp) + _expenses.value

        val tx = CashTransaction(
            id = ensureUuid("tx_" + UUID.randomUUID().toString()),
            transactionNumber = "CSH-${1000 + (1..8999).random()}",
            cashAccountName = cashAcc.name,
            transactionDate = nowStr,
            transactionType = "expense",
            amount = amount,
            direction = "out",
            balanceAfter = newCash,
            partyName = paidTo ?: category,
            description = "مصروف: $category" + (if (!notes.isNullOrBlank()) " ($notes)" else "")
        )
        _cashTransactions.value = listOf(tx) + _cashTransactions.value

        scope.launch {
            val api = getSupabaseApi() ?: return@launch
            runCatching {
                api.insertExpense(SupabaseExpenseDto.fromDomain(exp))
            }.onFailure { Log.e("AlZahraaRepository", "Failed to insert expense to Supabase", it) }
        }

        return true
    }

    // Migrate paper book entry into customer ledger
    fun migrateOldLedger(entryId: String): Boolean {
        val entry = _oldLedgers.value.find { it.id == entryId } ?: return false
        if (entry.isTransferred) return false

        val remaining = entry.debitAmount - entry.settledAmount
        if (remaining <= 0) return false

        var customer = _customers.value.find { it.name.trim().equals(entry.partyName.trim(), ignoreCase = true) }
        if (customer == null) {
            customer = saveCustomer(null, entry.partyName, null, entry.phone, entry.village, remaining)
        } else {
            val newBal = customer.currentBalance + remaining
            _customers.value = _customers.value.map { if (it.id == customer?.id) it.copy(currentBalance = newBal) else it }
        }

        val nowStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val ledgerEntry = CustomerLedgerEntry(
            id = ensureUuid("le_" + UUID.randomUUID().toString()),
            customerId = customer.id,
            entryDate = nowStr,
            entryType = "old_ledger_migration",
            documentNumber = "MIG-${entry.pageNumber ?: "OLD"}",
            description = "مرحل من ${entry.bookName} (${entry.pageNumber ?: "-"}): ${entry.notes ?: "رصيد دفتري قديم"}",
            debit = remaining,
            credit = 0.0,
            runningBalance = customer.currentBalance + remaining
        )

        val currentMap = _customerLedgers.value.toMutableMap()
        currentMap[customer.id] = (currentMap[customer.id] ?: emptyList()) + ledgerEntry
        _customerLedgers.value = currentMap

        _oldLedgers.value = _oldLedgers.value.map {
            if (it.id == entryId) it.copy(isTransferred = true) else it
        }

        return true
    }
}
