package com.example.network.supabase

import com.example.model.CashAccount
import com.example.model.CashTransaction
import com.example.model.Customer
import com.example.model.CustomerLedgerEntry
import com.example.model.Expense
import com.example.model.OldLedgerEntry
import com.example.model.Product
import com.example.model.PurchaseInvoice
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SupabaseCustomerDto(
    @Json(name = "id") val id: String,
    @Json(name = "code") val code: String,
    @Json(name = "name") val name: String,
    @Json(name = "partner_name") val partnerName: String? = null,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "village_or_address") val villageOrAddress: String? = null,
    @Json(name = "current_balance") val currentBalance: Double = 0.0,
    @Json(name = "opening_balance") val openingBalance: Double = 0.0,
    @Json(name = "credit_limit") val creditLimit: Double = 50000.0,
    @Json(name = "notes") val notes: String? = null,
    @Json(name = "is_active") val isActive: Boolean = true
) {
    fun toDomain(): Customer = Customer(
        id = id,
        code = code,
        name = name,
        partnerName = partnerName,
        phone = phone,
        villageOrAddress = villageOrAddress,
        currentBalance = currentBalance,
        openingBalance = openingBalance,
        creditLimit = creditLimit,
        notes = notes,
        isActive = isActive
    )

    companion object {
        fun fromDomain(c: Customer): SupabaseCustomerDto = SupabaseCustomerDto(
            id = c.id,
            code = c.code,
            name = c.name,
            partnerName = c.partnerName,
            phone = c.phone,
            villageOrAddress = c.villageOrAddress,
            currentBalance = c.currentBalance,
            openingBalance = c.openingBalance,
            creditLimit = c.creditLimit,
            notes = c.notes,
            isActive = c.isActive
        )
    }
}

@JsonClass(generateAdapter = true)
data class SupabaseProductDto(
    @Json(name = "id") val id: String,
    @Json(name = "code") val code: String,
    @Json(name = "barcode") val barcode: String? = null,
    @Json(name = "name") val name: String,
    @Json(name = "commercial_name") val commercialName: String? = null,
    @Json(name = "active_ingredient") val activeIngredient: String? = null,
    @Json(name = "manufacturer") val manufacturer: String? = null,
    @Json(name = "base_unit_name") val baseUnitName: String = "عبوة",
    @Json(name = "default_purchase_price") val defaultPurchasePrice: Double = 0.0,
    @Json(name = "default_sale_price") val defaultSalePrice: Double = 0.0,
    @Json(name = "min_stock_alert") val minStockAlert: Double = 10.0,
    @Json(name = "is_pesticide") val isPesticide: Boolean = false,
    @Json(name = "description") val description: String? = null,
    @Json(name = "image_url") val imageUrl: String? = null
) {
    fun toDomain(): Product = Product(
        id = id,
        code = code,
        barcode = barcode,
        name = name,
        commercialName = commercialName,
        activeIngredient = activeIngredient,
        manufacturer = manufacturer,
        baseUnitName = baseUnitName,
        defaultPurchasePrice = defaultPurchasePrice,
        defaultSalePrice = defaultSalePrice,
        minStockAlert = minStockAlert.toInt(),
        isPesticide = isPesticide,
        description = description,
        imageUrl = imageUrl
    )

    companion object {
        fun fromDomain(p: Product): SupabaseProductDto = SupabaseProductDto(
            id = p.id,
            code = p.code,
            barcode = p.barcode,
            name = p.name,
            commercialName = p.commercialName,
            activeIngredient = p.activeIngredient,
            manufacturer = p.manufacturer,
            baseUnitName = p.baseUnitName,
            defaultPurchasePrice = p.defaultPurchasePrice,
            defaultSalePrice = p.defaultSalePrice,
            minStockAlert = p.minStockAlert.toDouble(),
            isPesticide = p.isPesticide,
            description = p.description,
            imageUrl = p.imageUrl
        )
    }
}

@JsonClass(generateAdapter = true)
data class SupabaseCustomerLedgerEntryDto(
    @Json(name = "id") val id: String,
    @Json(name = "customer_id") val customerId: String,
    @Json(name = "entry_date") val entryDate: String,
    @Json(name = "entry_type") val entryType: String,
    @Json(name = "document_number") val documentNumber: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "debit") val debit: Double = 0.0,
    @Json(name = "credit") val credit: Double = 0.0,
    @Json(name = "running_balance") val runningBalance: Double = 0.0
) {
    fun toDomain(): CustomerLedgerEntry = CustomerLedgerEntry(
        id = id,
        customerId = customerId,
        entryDate = entryDate,
        entryType = entryType,
        documentNumber = documentNumber ?: "",
        description = description ?: "",
        debit = debit,
        credit = credit,
        runningBalance = runningBalance
    )

    companion object {
        fun fromDomain(e: CustomerLedgerEntry): SupabaseCustomerLedgerEntryDto = SupabaseCustomerLedgerEntryDto(
            id = e.id,
            customerId = e.customerId,
            entryDate = e.entryDate,
            entryType = e.entryType,
            documentNumber = e.documentNumber,
            description = e.description,
            debit = e.debit,
            credit = e.credit,
            runningBalance = e.runningBalance
        )
    }
}

@JsonClass(generateAdapter = true)
data class SupabaseCashAccountDto(
    @Json(name = "id") val id: String,
    @Json(name = "code") val code: String,
    @Json(name = "name") val name: String,
    @Json(name = "account_type") val accountType: String,
    @Json(name = "current_balance") val currentBalance: Double = 0.0,
    @Json(name = "is_primary") val isPrimary: Boolean = false
) {
    fun toDomain(): CashAccount = CashAccount(
        id = id,
        code = code,
        name = name,
        accountType = accountType,
        currentBalance = currentBalance,
        isPrimary = isPrimary
    )

    companion object {
        fun fromDomain(a: CashAccount): SupabaseCashAccountDto = SupabaseCashAccountDto(
            id = a.id,
            code = a.code,
            name = a.name,
            accountType = a.accountType,
            currentBalance = a.currentBalance,
            isPrimary = a.isPrimary
        )
    }
}

@JsonClass(generateAdapter = true)
data class SupabaseCashTransactionDto(
    @Json(name = "id") val id: String,
    @Json(name = "transaction_number") val transactionNumber: String,
    @Json(name = "party_name") val partyName: String? = null,
    @Json(name = "transaction_date") val transactionDate: String,
    @Json(name = "transaction_type") val transactionType: String,
    @Json(name = "amount") val amount: Double,
    @Json(name = "direction") val direction: String,
    @Json(name = "balance_after") val balanceAfter: Double = 0.0,
    @Json(name = "description") val description: String? = null
) {
    fun toDomain(): CashTransaction = CashTransaction(
        id = id,
        transactionNumber = transactionNumber,
        cashAccountName = "الخزينة النقدية الرئيسية - المحل",
        transactionDate = transactionDate,
        transactionType = transactionType,
        amount = amount,
        direction = direction,
        balanceAfter = balanceAfter,
        partyName = partyName,
        description = description ?: ""
    )

    companion object {
        fun fromDomain(t: CashTransaction): SupabaseCashTransactionDto = SupabaseCashTransactionDto(
            id = t.id,
            transactionNumber = t.transactionNumber,
            partyName = t.partyName,
            transactionDate = t.transactionDate,
            transactionType = t.transactionType,
            amount = t.amount,
            direction = t.direction,
            balanceAfter = t.balanceAfter,
            description = t.description
        )
    }
}

@JsonClass(generateAdapter = true)
data class SupabaseExpenseDto(
    @Json(name = "id") val id: String,
    @Json(name = "expense_number") val expenseNumber: String,
    @Json(name = "category") val category: String,
    @Json(name = "amount") val amount: Double,
    @Json(name = "expense_date") val expenseDate: String,
    @Json(name = "paid_to") val paidTo: String? = null,
    @Json(name = "notes") val notes: String? = null
) {
    fun toDomain(): Expense = Expense(
        id = id,
        expenseNumber = expenseNumber,
        category = category,
        amount = amount,
        expenseDate = expenseDate,
        cashAccountName = "الخزينة النقدية الرئيسية - المحل",
        paidTo = paidTo,
        notes = notes
    )

    companion object {
        fun fromDomain(e: Expense): SupabaseExpenseDto = SupabaseExpenseDto(
            id = e.id,
            expenseNumber = e.expenseNumber,
            category = e.category,
            amount = e.amount,
            expenseDate = e.expenseDate,
            paidTo = e.paidTo,
            notes = e.notes
        )
    }
}

@JsonClass(generateAdapter = true)
data class SupabaseOldLedgerDto(
    @Json(name = "id") val id: String,
    @Json(name = "book_name") val bookName: String,
    @Json(name = "page_number") val pageNumber: String? = null,
    @Json(name = "entry_date") val entryDate: String,
    @Json(name = "party_name") val partyName: String,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "village") val village: String? = null,
    @Json(name = "debit_amount") val debitAmount: Double = 0.0,
    @Json(name = "settled_amount") val settledAmount: Double = 0.0,
    @Json(name = "is_transferred_to_system") val isTransferred: Boolean = false,
    @Json(name = "notes") val notes: String? = null
) {
    fun toDomain(): OldLedgerEntry = OldLedgerEntry(
        id = id,
        bookName = bookName,
        pageNumber = pageNumber,
        entryDate = entryDate,
        partyName = partyName,
        phone = phone,
        village = village,
        debitAmount = debitAmount,
        settledAmount = settledAmount,
        isTransferred = isTransferred,
        notes = notes
    )

    companion object {
        fun fromDomain(o: OldLedgerEntry): SupabaseOldLedgerDto = SupabaseOldLedgerDto(
            id = o.id,
            bookName = o.bookName,
            pageNumber = o.pageNumber,
            entryDate = o.entryDate,
            partyName = o.partyName,
            phone = o.phone,
            village = o.village,
            debitAmount = o.debitAmount,
            settledAmount = o.settledAmount,
            isTransferred = o.isTransferred,
            notes = o.notes
        )
    }
}

@JsonClass(generateAdapter = true)
data class SupabasePurchaseInvoiceDto(
    @Json(name = "id") val id: String,
    @Json(name = "invoice_number") val invoiceNumber: String,
    @Json(name = "supplier_name") val supplierName: String,
    @Json(name = "invoice_date") val invoiceDate: String,
    @Json(name = "payment_type") val paymentType: String,
    @Json(name = "subtotal") val subtotal: Double = 0.0,
    @Json(name = "discount_amount") val discountAmount: Double = 0.0,
    @Json(name = "total_amount") val totalAmount: Double = 0.0,
    @Json(name = "paid_amount") val paidAmount: Double = 0.0,
    @Json(name = "remaining_amount") val remainingAmount: Double = 0.0,
    @Json(name = "notes") val notes: String? = null
) {
    fun toDomain(): PurchaseInvoice = PurchaseInvoice(
        id = id,
        invoiceNumber = invoiceNumber,
        supplierName = supplierName,
        invoiceDate = invoiceDate,
        paymentType = paymentType,
        subtotal = subtotal,
        discountAmount = discountAmount,
        totalAmount = totalAmount,
        paidAmount = paidAmount,
        remainingAmount = remainingAmount,
        notes = notes
    )

    companion object {
        fun fromDomain(p: PurchaseInvoice): SupabasePurchaseInvoiceDto = SupabasePurchaseInvoiceDto(
            id = p.id,
            invoiceNumber = p.invoiceNumber,
            supplierName = p.supplierName,
            invoiceDate = p.invoiceDate,
            paymentType = p.paymentType,
            subtotal = p.subtotal,
            discountAmount = p.discountAmount,
            totalAmount = p.totalAmount,
            paidAmount = p.paidAmount,
            remainingAmount = p.remainingAmount,
            notes = p.notes
        )
    }
}
