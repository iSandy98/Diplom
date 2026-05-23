package com.example.diplom.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private const val BASE_URL =  "https://yave4en.pythonanywhere.com/"
        //"http://192.168.0.166:8000/"
        //"http://10.0.2.2:8000/"

    fun create(context: Context): ApiService {
        val tokenManager = TokenManager(context)

        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val urlPath = originalRequest.url.encodedPath

            val shouldSkipAuthHeader =
                urlPath.contains("/api/auth/register/") ||
                        urlPath.contains("/api/auth/token/") ||
                        urlPath.contains("/api/auth/token/refresh/") ||
                        urlPath.contains("/api/auth/guest/")

            val accessToken = tokenManager.getAccessToken()

            val newRequest = if (!shouldSkipAuthHeader && !accessToken.isNullOrBlank()) {
                originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer $accessToken")
                    .build()
            } else {
                originalRequest
            }

            chain.proceed(newRequest)
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}