package com.example.network.supabase

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class SupabaseConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

data class SupabaseStatusInfo(
    val state: SupabaseConnectionState = SupabaseConnectionState.DISCONNECTED,
    val projectUrl: String = "",
    val message: String = "",
    val syncedCustomersCount: Int = 0,
    val syncedProductsCount: Int = 0,
    val lastSyncTime: String? = null
)

class SupabaseConfigManager private constructor(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _status = MutableStateFlow(SupabaseStatusInfo())
    val status: StateFlow<SupabaseStatusInfo> = _status.asStateFlow()

    init {
        val currentUrl = getSupabaseUrl()
        val currentKey = getSupabaseAnonKey()
        if (isConfigured(currentUrl, currentKey)) {
            _status.value = SupabaseStatusInfo(
                state = SupabaseConnectionState.CONNECTED,
                projectUrl = currentUrl,
                message = "متصل بمشروع Supabase السحابي"
            )
        } else {
            _status.value = SupabaseStatusInfo(
                state = SupabaseConnectionState.DISCONNECTED,
                projectUrl = currentUrl,
                message = "لم يتم ضبط مفاتيح Supabase بعد"
            )
        }
    }

    fun getSupabaseUrl(): String {
        val saved = prefs.getString(KEY_URL, null)
        if (!saved.isNullOrBlank()) return saved.trim()
        val buildVal = runCatching { BuildConfig.SUPABASE_URL }.getOrNull()
        return if (!buildVal.isNullOrBlank() && !buildVal.contains("your-project")) buildVal.trim() else ""
    }

    fun getSupabaseAnonKey(): String {
        val saved = prefs.getString(KEY_KEY, null)
        if (!saved.isNullOrBlank()) return saved.trim()
        val buildVal = runCatching { BuildConfig.SUPABASE_ANON_KEY }.getOrNull()
        return if (!buildVal.isNullOrBlank() && !buildVal.contains("your-anon-key")) buildVal.trim() else ""
    }

    fun isConfigured(): Boolean {
        return isConfigured(getSupabaseUrl(), getSupabaseAnonKey())
    }

    fun isConfigured(url: String, key: String): Boolean {
        val cleanUrl = url.trim()
        val cleanKey = key.trim()
        return cleanUrl.startsWith("https://") &&
                !cleanUrl.contains("your-project") &&
                cleanKey.length > 20 &&
                !cleanKey.contains("your-anon-key")
    }

    fun saveConfig(url: String, key: String) {
        val cleanUrl = if (url.trim().endsWith("/")) url.trim().dropLast(1) else url.trim()
        val cleanKey = key.trim()
        prefs.edit()
            .putString(KEY_URL, cleanUrl)
            .putString(KEY_KEY, cleanKey)
            .apply()

        if (isConfigured(cleanUrl, cleanKey)) {
            _status.value = _status.value.copy(
                state = SupabaseConnectionState.CONNECTED,
                projectUrl = cleanUrl,
                message = "تم حفظ وتفعيل الاتصال السحابي بـ Supabase"
            )
        } else {
            _status.value = _status.value.copy(
                state = SupabaseConnectionState.DISCONNECTED,
                projectUrl = cleanUrl,
                message = "بيانات الاعتماد غير مكتملة"
            )
        }
    }

    fun updateStatus(
        state: SupabaseConnectionState,
        message: String,
        customersCount: Int = _status.value.syncedCustomersCount,
        productsCount: Int = _status.value.syncedProductsCount,
        syncTime: String? = _status.value.lastSyncTime
    ) {
        _status.value = _status.value.copy(
            state = state,
            message = message,
            syncedCustomersCount = customersCount,
            syncedProductsCount = productsCount,
            lastSyncTime = syncTime
        )
    }

    fun clearConfig() {
        prefs.edit().clear().apply()
        _status.value = SupabaseStatusInfo(
            state = SupabaseConnectionState.DISCONNECTED,
            projectUrl = "",
            message = "تم فصل الاتصال بـ Supabase"
        )
    }

    companion object {
        private const val PREFS_NAME = "supabase_credentials_prefs"
        private const val KEY_URL = "supabase_url"
        private const val KEY_KEY = "supabase_anon_key"

        @Volatile
        private var INSTANCE: SupabaseConfigManager? = null

        fun getInstance(context: Context): SupabaseConfigManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SupabaseConfigManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
