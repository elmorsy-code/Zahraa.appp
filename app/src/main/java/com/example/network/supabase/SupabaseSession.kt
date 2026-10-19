package com.example.network.supabase

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * جلسة تسجيل الدخول على Supabase Auth (نفس الإيميل وكلمة المرور بتاعة موقع الزهراء).
 *
 * قواعد الأمان في قاعدة البيانات (RLS) بتسمح بقراءة البيانات للمستخدمين المسجلين بس،
 * فالتطبيق لازم يبعت توكن المستخدم (access token) مع كل طلب، مش مفتاح anon لوحده.
 */
class SupabaseSessionManager private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val http: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    private val _signedIn = MutableStateFlow(hasSession())
    val signedIn: StateFlow<Boolean> = _signedIn.asStateFlow()

    fun hasSession(): Boolean = !prefs.getString(KEY_REFRESH, null).isNullOrBlank()

    fun userEmail(): String? = prefs.getString(KEY_EMAIL, null)

    /** التوكن اللي بيتبعت في Authorization، أو null لو مفيش جلسة. */
    fun accessToken(): String? = prefs.getString(KEY_ACCESS, null)?.takeIf { it.isNotBlank() }

    suspend fun signIn(
        baseUrl: String,
        anonKey: String,
        email: String,
        password: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject()
                .put("email", email.trim())
                .put("password", password)
                .toString()
            val request = Request.Builder()
                .url(tokenUrl(baseUrl, "password"))
                .header("apikey", anonKey)
                .post(body.toRequestBody(JSON_MEDIA))
                .build()
            http.newCall(request).execute().use { response ->
                val text = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception(describeError(text)))
                }
                storeSession(JSONObject(text), email.trim())
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("تعذر الاتصال بالسيرفر: ${e.localizedMessage}"))
        }
    }

    /**
     * بيجدد التوكن لو فاضله أقل من دقيقة. بيتنادى من الـ interceptor (thread خلفي)،
     * فهو blocking عن قصد. لو النت مقطوع بنسيب الجلسة زي ما هي.
     */
    @Synchronized
    fun refreshIfNeededBlocking(baseUrl: String, anonKey: String) {
        val refreshToken = prefs.getString(KEY_REFRESH, null)
        if (refreshToken.isNullOrBlank()) return
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)
        if (System.currentTimeMillis() < expiresAt - 60_000L) return

        try {
            val body = JSONObject().put("refresh_token", refreshToken).toString()
            val request = Request.Builder()
                .url(tokenUrl(baseUrl, "refresh_token"))
                .header("apikey", anonKey)
                .post(body.toRequestBody(JSON_MEDIA))
                .build()
            http.newCall(request).execute().use { response ->
                val text = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    storeSession(JSONObject(text), userEmail())
                } else if (response.code in 400..403) {
                    // refresh token مرفوض (اتغيرت كلمة المرور أو اتعطل الحساب): لازم دخول من جديد
                    signOut()
                }
            }
        } catch (_: Exception) {
            // مفيش نت: هنحاول تاني في الطلب الجاي
        }
    }

    fun signOut() {
        prefs.edit().clear().apply()
        _signedIn.value = false
    }

    private fun storeSession(json: JSONObject, email: String?) {
        val expiresInSec = json.optLong("expires_in", 3600L)
        prefs.edit()
            .putString(KEY_ACCESS, json.getString("access_token"))
            .putString(KEY_REFRESH, json.getString("refresh_token"))
            .putLong(KEY_EXPIRES_AT, System.currentTimeMillis() + expiresInSec * 1000L)
            .putString(KEY_EMAIL, email)
            .apply()
        _signedIn.value = true
    }

    private fun tokenUrl(baseUrl: String, grantType: String): String =
        baseUrl.trim().trimEnd('/') + "/auth/v1/token?grant_type=" + grantType

    private fun describeError(raw: String): String {
        val message = try {
            val json = JSONObject(raw)
            json.optString("msg").ifBlank {
                json.optString("error_description").ifBlank { json.optString("message") }
            }
        } catch (_: Exception) {
            ""
        }
        return if (message.contains("invalid", ignoreCase = true) || message.isBlank()) {
            "الإيميل أو كلمة المرور غير صحيحة"
        } else {
            message
        }
    }

    companion object {
        private const val PREFS_NAME = "supabase_session_prefs"
        private const val KEY_ACCESS = "access_token"
        private const val KEY_REFRESH = "refresh_token"
        private const val KEY_EXPIRES_AT = "expires_at"
        private const val KEY_EMAIL = "email"
        private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()

        @Volatile
        private var INSTANCE: SupabaseSessionManager? = null

        fun getInstance(context: Context): SupabaseSessionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SupabaseSessionManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
