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
import com.example.network.supabase.SupabaseCustomerDto
import com.example.network.supabase.SupabaseCustomerLedgerEntryDto
import com.example.network.supabase.SupabaseExpenseDto
import com.example.network.supabase.SupabaseOldLedgerDto
import com.example.network.supabase.SupabaseProductDto
import com.example.network.supabase.SupabaseStatusInfo
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

class AlZahraaRepository(
    private val context: Context? = null
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val configManager: SupabaseConfigManager? = context?.let { SupabaseConfigManager.getInstance(it) }

    private val _customers = MutableStateFlow<List<Customer>>(AlZahraaSeedData.generateInitialCustomers())
    val customers: StateFlow<List<Customer>> = _customers.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(AlZahraaSeedData.INITIAL_PRODUCTS)
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _suppliers = MutableStateFlow<List<Supplier>>(AlZahraaSeedData.INITIAL_SUPPLIERS)
    val suppliers: StateFlow<List<Supplier>> = _suppliers.asStateFlow()

    private val _cashAccounts = MutableStateFlow<List<CashAccount>>(AlZahraaSeedData.INITIAL_CASH_ACCOUNTS)
    val cashAccounts: StateFlow<List<CashAccount>> = _cashAccounts.asStateFlow()

    private val _expenses = MutableStateFlow<List<Expense>>(AlZahraaSeedData.INITIAL_EXPENSES)
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    private val _oldLedgers = MutableStateFlow<List<OldLedgerEntry>>(AlZahraaSeedData.INITIAL_OLD_LEDGERS)
    val oldLedgers: StateFlow<List<OldLedgerEntry>> = _oldLedgers.asStateFlow()

    private val _purchaseInvoices = MutableStateFlow<List<PurchaseInvoice>>(AlZahraaSeedData.INITIAL_PURCHASE_INVOICES)
    val purchaseInvoices: StateFlow<List<PurchaseInvoice>> = _purchaseInvoices.asStateFlow()

    private val _customerLedgers = MutableStateFlow<Map<String, List<CustomerLedgerEntry>>>(
        AlZahraaSeedData.INITIAL_CUSTOMERS
            .filter { it.currentBalance > 0 }
            .associate { c ->
                val invNum = "INV-${1000 + (c.code.filter { ch -> ch.isDigit() }.toIntOrNull() ?: 1)}"
                val entryId = "le_" + UUID.nameUUIDFromBytes((c.id + "_initial").toByteArray()).toString().take(12)
                c.id to listOf(
                    CustomerLedgerEntry(
                        id = entryId,
                        customerId = c.id,
                        entryDate = "2026-10-14 10:30",
                        entryType = "invoice",
                        documentNumber = invNum,
                        description = "فاتورة مشتريات مبيدات وأدوية زراعية مسجلة (رصيد سابق)",
                        debit = c.currentBalance,
                        credit = 0.0,
                        runningBalance = c.currentBalance
                    )
                )
            }
    )
    val customerLedgers: StateFlow<Map<String, List<CustomerLedgerEntry>>> = _customerLedgers.asStateFlow()

    private val _cashTransactions = MutableStateFlow<List<CashTransaction>>(
        listOf(
            CashTransaction("tx01", "CSH-1001", "الخزينة النقدية الرئيسية - المحل", "2026-10-18 14:30", "expense", 1200.0, "out", 48500.0, "نقل وشحن", "مصروف شحن ونقل بضائع"),
            CashTransaction("tx02", "CSH-1002", "الخزينة النقدية الرئيسية - المحل", "2026-10-17 10:00", "expense", 650.0, "out", 49700.0, "كهرباء ومياه", "سداد فاتورة استهلاك كهرباء المحل")
        )
    )
    val cashTransactions: StateFlow<List<CashTransaction>> = _cashTransactions.asStateFlow()

    val supabaseStatus: StateFlow<SupabaseStatusInfo>? = configManager?.status

    init {
        if (configManager?.isConfigured() == true) {
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
        val url = it.getSupabaseUrl()
        val key = it.getSupabaseAnonKey()
        if (it.isConfigured(url, key)) {
            SupabaseClientProvider.getApiService(url, key)
        } else null
    }

    // --- Core Supabase Sync Operations ---
    suspend fun syncFromSupabase(): Result<String> = withContext(Dispatchers.IO) {
        val api = getSupabaseApi()
            ?: return@withContext Result.failure(Exception("Supabase غير مهيأ بعد. يرجى إدخال الرابط والمفتاح."))

        configManager?.updateStatus(SupabaseConnectionState.CONNECTING, "جاري سحب ومزامنة البيانات من Supabase...")
        try {
            // 1. Fetch Customers
            val remoteCustomers = api.getCustomers()
            if (remoteCustomers.isNotEmpty()) {
                _customers.value = remoteCustomers.map { it.toDomain() }
            }

            // 2. Fetch Products
            val remoteProducts = runCatching { api.getProducts() }.getOrNull()
            if (!remoteProducts.isNullOrEmpty()) {
                _products.value = remoteProducts.map { it.toDomain() }
            }

            // 3. Fetch Ledger Entries
            val remoteLedger = runCatching { api.getCustomerLedgerEntries() }.getOrNull()
            if (!remoteLedger.isNullOrEmpty()) {
                val grouped = remoteLedger.groupBy({ it.customerId }, { it.toDomain() })
                _customerLedgers.value = grouped
            }

            // 4. Fetch Expenses
            val remoteExpenses = runCatching { api.getExpenses() }.getOrNull()
            if (!remoteExpenses.isNullOrEmpty()) {
                _expenses.value = remoteExpenses.map { it.toDomain() }
            }

            // 5. Fetch Old Ledgers
            val remoteOld = runCatching { api.getOldLedgers() }.getOrNull()
            if (!remoteOld.isNullOrEmpty()) {
                _oldLedgers.value = remoteOld.map { it.toDomain() }
            }

            val nowStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            configManager?.updateStatus(
                state = SupabaseConnectionState.CONNECTED,
                message = "تمت المزامنة بنجاح مع Supabase",
                customersCount = _customers.value.size,
                productsCount = _products.value.size,
                syncTime = nowStr
            )
            Result.success("تم جلب البيانات بنجاح: ${_customers.value.size} عميل و ${_products.value.size} صنف.")
        } catch (e: Exception) {
            Log.e("AlZahraaRepository", "Supabase sync error", e)
            configManager?.updateStatus(
                state = SupabaseConnectionState.ERROR,
                message = "فشل الاتصال بـ Supabase: ${e.localizedMessage}"
            )
            Result.failure(e)
        }
    }

    suspend fun pushSeedDataToSupabase(): Result<String> = withContext(Dispatchers.IO) {
        val api = getSupabaseApi()
            ?: return@withContext Result.failure(Exception("Supabase غير مهيأ بعد."))

        configManager?.updateStatus(SupabaseConnectionState.CONNECTING, "جاري رفع البيانات الأولية إلى Supabase...")
        try {
            // 1. Push Customers
            val custDtos = _customers.value.map { c ->
                SupabaseCustomerDto.fromDomain(c.copy(id = ensureUuid(c.id)))
            }
            api.upsertCustomerBatch(custDtos)

            // 2. Push Products
            val prodDtos = _products.value.map { p ->
                SupabaseProductDto.fromDomain(p.copy(id = ensureUuid(p.id)))
            }
            api.upsertProductBatch(prodDtos)

            // 3. Push Old Ledgers
            val oldDtos = _oldLedgers.value.map { o ->
                SupabaseOldLedgerDto.fromDomain(o.copy(id = ensureUuid(o.id)))
            }
            runCatching { api.upsertOldLedgersBatch(oldDtos) }

            // 4. Push Cash Accounts
            val cashDtos = _cashAccounts.value.map { a: CashAccount ->
                SupabaseCashAccountDto.fromDomain(a.copy(id = ensureUuid(a.id)))
            }
            runCatching { api.upsertCashAccountsBatch(cashDtos) }

            // 5. Push Debt Ledgers
            val ledgerDtos = _customerLedgers.value.values.flatten().map { l ->
                SupabaseCustomerLedgerEntryDto.fromDomain(
                    l.copy(
                        id = ensureUuid(l.id),
                        customerId = ensureUuid(l.customerId)
                    )
                )
            }
            if (ledgerDtos.isNotEmpty()) {
                runCatching { api.insertLedgerEntriesBatch(ledgerDtos) }
            }

            val nowStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            configManager?.updateStatus(
                state = SupabaseConnectionState.CONNECTED,
                message = "تم رفع وتحديث كافة البيانات في Supabase بنجاح",
                customersCount = custDtos.size,
                productsCount = prodDtos.size,
                syncTime = nowStr
            )
            Result.success("تم رفع ${custDtos.size} عميل و ${prodDtos.size} صنف إلى Supabase بنجاح!")
        } catch (e: Exception) {
            Log.e("AlZahraaRepository", "Push seed error", e)
            configManager?.updateStatus(SupabaseConnectionState.ERROR, "فشل الرفع: ${e.localizedMessage}")
            Result.failure(e)
        }
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
