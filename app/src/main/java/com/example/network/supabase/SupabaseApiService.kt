package com.example.network.supabase

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface SupabaseApiService {

    // --- Customers ---
    @GET("rest/v1/customers")
    suspend fun getCustomers(
        @Query("select") select: String = "*",
        @Query("order") order: String = "name.asc"
    ): List<SupabaseCustomerDto>

    @Headers("Prefer: return=representation")
    @POST("rest/v1/customers")
    suspend fun insertCustomer(@Body customer: SupabaseCustomerDto): List<SupabaseCustomerDto>

    @Headers("Prefer: resolution=merge-duplicates,return=representation")
    @POST("rest/v1/customers")
    suspend fun upsertCustomerBatch(@Body customers: List<SupabaseCustomerDto>): List<SupabaseCustomerDto>

    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/customers")
    suspend fun updateCustomer(
        @Query("id") idFilter: String, // e.g. "eq.<uuid>"
        @Body customer: Map<String, Any?>
    ): List<SupabaseCustomerDto>

    @DELETE("rest/v1/customers")
    suspend fun deleteCustomer(@Query("id") idFilter: String)

    // --- Products ---
    @GET("rest/v1/products")
    suspend fun getProducts(
        @Query("select") select: String = "*",
        @Query("order") order: String = "created_at.desc"
    ): List<SupabaseProductDto>

    @Headers("Prefer: return=representation")
    @POST("rest/v1/products")
    suspend fun insertProduct(@Body product: SupabaseProductDto): List<SupabaseProductDto>

    @Headers("Prefer: resolution=merge-duplicates,return=representation")
    @POST("rest/v1/products")
    suspend fun upsertProductBatch(@Body products: List<SupabaseProductDto>): List<SupabaseProductDto>

    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/products")
    suspend fun updateProduct(
        @Query("id") idFilter: String,
        @Body product: Map<String, Any?>
    ): List<SupabaseProductDto>

    @DELETE("rest/v1/products")
    suspend fun deleteProduct(@Query("id") idFilter: String)

    // --- Customer Ledger Entries (Debts & Payments) ---
    @GET("rest/v1/customer_ledger_entries")
    suspend fun getCustomerLedgerEntries(
        @Query("select") select: String = "*",
        @Query("order") order: String = "entry_date.asc"
    ): List<SupabaseCustomerLedgerEntryDto>

    @Headers("Prefer: return=representation")
    @POST("rest/v1/customer_ledger_entries")
    suspend fun insertLedgerEntry(@Body entry: SupabaseCustomerLedgerEntryDto): List<SupabaseCustomerLedgerEntryDto>

    @Headers("Prefer: resolution=merge-duplicates,return=representation")
    @POST("rest/v1/customer_ledger_entries")
    suspend fun insertLedgerEntriesBatch(@Body entries: List<SupabaseCustomerLedgerEntryDto>): List<SupabaseCustomerLedgerEntryDto>

    // --- Cash Accounts ---
    @GET("rest/v1/cash_accounts")
    suspend fun getCashAccounts(
        @Query("select") select: String = "*",
        @Query("order") order: String = "is_primary.desc"
    ): List<SupabaseCashAccountDto>

    @Headers("Prefer: resolution=merge-duplicates,return=representation")
    @POST("rest/v1/cash_accounts")
    suspend fun upsertCashAccountsBatch(@Body accounts: List<SupabaseCashAccountDto>): List<SupabaseCashAccountDto>

    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/cash_accounts")
    suspend fun updateCashAccount(
        @Query("id") idFilter: String,
        @Body account: Map<String, Any?>
    ): List<SupabaseCashAccountDto>

    // --- Cash Transactions ---
    @GET("rest/v1/cash_transactions")
    suspend fun getCashTransactions(
        @Query("select") select: String = "*",
        @Query("order") order: String = "transaction_date.desc",
        @Query("limit") limit: Int = 100
    ): List<SupabaseCashTransactionDto>

    @Headers("Prefer: return=representation")
    @POST("rest/v1/cash_transactions")
    suspend fun insertCashTransaction(@Body tx: SupabaseCashTransactionDto): List<SupabaseCashTransactionDto>

    // --- Expenses ---
    @GET("rest/v1/expenses")
    suspend fun getExpenses(
        @Query("select") select: String = "*",
        @Query("order") order: String = "expense_date.desc",
        @Query("limit") limit: Int = 100
    ): List<SupabaseExpenseDto>

    @Headers("Prefer: return=representation")
    @POST("rest/v1/expenses")
    suspend fun insertExpense(@Body expense: SupabaseExpenseDto): List<SupabaseExpenseDto>

    // --- Old Ledgers (الدفاتر القديمة) ---
    @GET("rest/v1/old_ledgers")
    suspend fun getOldLedgers(
        @Query("select") select: String = "*",
        @Query("order") order: String = "entry_date.desc"
    ): List<SupabaseOldLedgerDto>

    @Headers("Prefer: return=representation")
    @POST("rest/v1/old_ledgers")
    suspend fun insertOldLedger(@Body entry: SupabaseOldLedgerDto): List<SupabaseOldLedgerDto>

    @Headers("Prefer: resolution=merge-duplicates,return=representation")
    @POST("rest/v1/old_ledgers")
    suspend fun upsertOldLedgersBatch(@Body entries: List<SupabaseOldLedgerDto>): List<SupabaseOldLedgerDto>

    // --- Purchase Invoices ---
    @GET("rest/v1/purchase_invoices")
    suspend fun getPurchaseInvoices(
        @Query("select") select: String = "*",
        @Query("order") order: String = "invoice_date.desc",
        @Query("limit") limit: Int = 100
    ): List<SupabasePurchaseInvoiceDto>

    @Headers("Prefer: return=representation")
    @POST("rest/v1/purchase_invoices")
    suspend fun insertPurchaseInvoice(@Body invoice: SupabasePurchaseInvoiceDto): List<SupabasePurchaseInvoiceDto>
}

object SupabaseClientProvider {

    private var currentUrl: String = ""
    private var currentKey: String = ""
    private var serviceInstance: SupabaseApiService? = null

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Synchronized
    fun getApiService(baseUrl: String, anonKey: String): SupabaseApiService? {
        val cleanUrl = if (baseUrl.trim().endsWith("/")) baseUrl.trim() else "${baseUrl.trim()}/"
        val cleanKey = anonKey.trim()

        if (cleanUrl.isBlank() || cleanKey.isBlank() || cleanUrl.contains("your-project")) {
            return null
        }

        if (serviceInstance != null && currentUrl == cleanUrl && currentKey == cleanKey) {
            return serviceInstance
        }

        currentUrl = cleanUrl
        currentKey = cleanKey

        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .header("apikey", cleanKey)
                .header("Authorization", "Bearer $cleanKey")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
            chain.proceed(requestBuilder.build())
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()

        serviceInstance = Retrofit.Builder()
            .baseUrl(cleanUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SupabaseApiService::class.java)

        return serviceInstance
    }
}
