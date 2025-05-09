package com.example.coursework.data.remote

import com.example.coursework.data.remote.model.*
import retrofit2.http.*

interface YandexUserInfoApi {
    @GET("info")
    suspend fun getUserInfo(
        @Header("Authorization") authHeader: String
    ): YandexUserInfo
}