package com.blockremote.data.network

import com.blockremote.data.local.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val DEFAULT_BASE_URL = "https://api.blockremote.io/"
    private const val CONNECT_TIMEOUT = 15L
    private const val READ_TIMEOUT = 30L

    private var retrofit: Retrofit? = null
    private var sessionManager: SessionManager? = null

    fun initialize(sessionManager: SessionManager, baseUrl: String = DEFAULT_BASE_URL) {
        this.sessionManager = sessionManager

        val authInterceptor = Interceptor { chain ->
            val token = sessionManager.getJwtToken()
            val request = if (token != null) {
                chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("X-Device-Id", sessionManager.getDeviceId())
                    .build()
            } else {
                chain.request()
            }
            chain.proceed(request)
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getApi(): BlockRemoteApi {
        return retrofit?.create(BlockRemoteApi::class.java)
            ?: throw IllegalStateException("ApiClient not initialized. Call initialize() first.")
    }

    fun createAuthenticatedOkHttpClient(): OkHttpClient {
        val sm = sessionManager
            ?: throw IllegalStateException("ApiClient not initialized. Call initialize() first.")

        val wsAuthInterceptor = Interceptor { chain ->
            val token = sm.getJwtToken()
            val request = chain.request().newBuilder()
                .addHeader("X-Device-Id", sm.getDeviceId())
                .apply {
                    if (token != null) {
                        addHeader("Authorization", "Bearer $token")
                    }
                }
                .build()
            chain.proceed(request)
        }

        return OkHttpClient.Builder()
            .addInterceptor(wsAuthInterceptor)
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS)
            .pingInterval(25, TimeUnit.SECONDS)
            .build()
    }

    fun getWebSocketUrl(): String {
        val base = retrofit?.baseUrl()?.toString() ?: DEFAULT_BASE_URL
        return base
            .replace("https://", "wss://")
            .replace("http://", "ws://") + "v1/signals"
    }

    fun getJwtToken(): String? = sessionManager?.getJwtToken()
}
