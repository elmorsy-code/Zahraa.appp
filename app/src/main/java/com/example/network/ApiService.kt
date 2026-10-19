package com.example.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

data class AuthRequest(
    val email: String,
    val password: String
)

data class AuthApiResponse(
    val success: Boolean,
    val token: String?,
    val message: String?,
    val userId: String?,
    val userName: String?
)

data class ContentApiResponse(
    val items: List<ContentApiItem>,
    val total: Int
)

data class ContentApiItem(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val date: String
)

interface ApiService {
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: AuthRequest): AuthApiResponse

    @GET("api/v1/content")
    suspend fun getContent(): ContentApiResponse
}

object NetworkClient {
    private var currentBaseUrl = "https://api.example.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    fun getService(baseUrl: String = currentBaseUrl): ApiService {
        val formattedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        currentBaseUrl = formattedUrl
        return Retrofit.Builder()
            .baseUrl(formattedUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ApiService::class.java)
    }

    fun getBaseUrl(): String = currentBaseUrl
}
