package com.example.model

data class Customer(
    val id: String,
    val code: String,
    val name: String,
    val partnerName: String? = null,
    val phone: String? = null,
    val villageOrAddress: String? = null,
    val currentBalance: Double = 0.0,
    val openingBalance: Double = 0.0,
    val creditLimit: Double = 50000.0,
    val notes: String? = null,
    val isActive: Boolean = true
)

data class CustomerLedgerEntry(
    val id: String,
    val customerId: String,
    val entryDate: String,
    val entryType: String, // "invoice", "payment", "return", "opening_balance", "old_ledger_migration"
    val documentNumber: String,
    val description: String,
    val debit: Double = 0.0,   // مدين (على العميل)
    val credit: Double = 0.0,  // دائن (سدد العميل)
    val runningBalance: Double = 0.0
)

data class Product(
    val id: String,
    val code: String,
    val barcode: String? = null,
    val name: String,
    val commercialName: String? = null,
    val activeIngredient: String? = null,
    val manufacturer: String? = null,
    val categoryName: String = "أدوية ومبيدات فطرية وحشرية",
    val baseUnitName: String = "عبوة",
    val weightOrSize: String? = null,
    val defaultPurchasePrice: Double = 0.0,
    val defaultSalePrice: Double = 0.0,
    val minStockAlert: Int = 10,
    val stockShop: Int = 0,
    val stockWarehouse: Int = 0,
    val totalStock: Int = 0,
    val isPesticide: Boolean = false,
    val description: String? = null,
    val imageUrl: String? = null
)

data class Supplier(
    val id: String,
    val code: String,
    val name: String,
    val companyName: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val taxNumber: String? = null,
    val currentBalance: Double = 0.0,
    val notes: String? = null
)

data class PurchaseInvoiceItem(
    val id: String,
    val productName: String,
    val qty: Double,
    val unit: String,
    val costPrice: Double,
    val totalCost: Double
)

data class PurchaseInvoice(
    val id: String,
    val invoiceNumber: String,
    val supplierName: String,
    val invoiceDate: String,
    val paymentType: String, // "cash", "credit", "partial"
    val subtotal: Double,
    val discountAmount: Double = 0.0,
    val totalAmount: Double,
    val paidAmount: Double,
    val remainingAmount: Double,
    val notes: String? = null,
    val items: List<PurchaseInvoiceItem> = emptyList()
)

data class CashAccount(
    val id: String,
    val code: String,
    val name: String,
    val accountType: String, // "cash", "bank", "wallet"
    val currentBalance: Double = 0.0,
    val isPrimary: Boolean = false
)

data class CashTransaction(
    val id: String,
    val transactionNumber: String,
    val cashAccountName: String,
    val transactionDate: String,
    val transactionType: String,
    val amount: Double,
    val direction: String, // "in" or "out"
    val balanceAfter: Double,
    val partyName: String? = null,
    val description: String
)

data class Expense(
    val id: String,
    val expenseNumber: String,
    val category: String,
    val amount: Double,
    val expenseDate: String,
    val cashAccountName: String,
    val paidTo: String? = null,
    val notes: String? = null
)

data class OldLedgerEntry(
    val id: String,
    val bookName: String,
    val pageNumber: String? = null,
    val entryDate: String,
    val partyName: String,
    val phone: String? = null,
    val village: String? = null,
    val debitAmount: Double,
    val settledAmount: Double = 0.0,
    val isTransferred: Boolean = false,
    val notes: String? = null
)

data class DebtItemDraft(
    val id: String,
    val itemName: String,
    val isSubQuantity: Boolean = false,
    // Standard quantity
    val quantity: Double = 1.0,
    val unit: String = "زجاجة",
    val unitPrice: Double = 0.0,
    // Sub-quantity (تجزئة / كسر عبوة)
    val subQuantity: Double = 0.0,
    val subUnit: String = "مل",
    val fullCapacity: Double = 100.0,
    val packageType: String = "زجاجة",
    val packagePrice: Double = 0.0,
    val totalPrice: Double = 0.0
)

data class SimsimMessage(
    val id: String,
    val sender: String, // "user" or "simsim"
    val text: String,
    val timestamp: String,
    val actionTarget: String? = null,
    val actionLabel: String? = null
)
