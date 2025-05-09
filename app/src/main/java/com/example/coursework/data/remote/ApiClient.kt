package com.example.coursework.data.remote

import android.content.*
import com.example.coursework.data.storage.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.*
import okhttp3.logging.*
import retrofit2.*
import retrofit2.converter.gson.*

object ApiClient {
    fun create(context: Context): YandexDiskApiService {
        val auth = Interceptor { chain ->
            val token = runBlocking { TokenDataStore(context).selectedToken.first() }
            val rq = chain.request().newBuilder()
                .addHeader("Authorization", "OAuth $token").build()
            chain.proceed(rq)
        }

        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(auth)
            .addInterceptor(logger)
            .build()

        return Retrofit.Builder()
            .baseUrl("https://cloud-api.yandex.net/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(YandexDiskApiService::class.java)
    }

    fun createUserInfoApi(): YandexUserInfoApi =
        Retrofit.Builder()
            .baseUrl("https://login.yandex.ru/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(YandexUserInfoApi::class.java)
}