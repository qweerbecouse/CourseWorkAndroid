package com.example.coursework.data.remote

import com.example.coursework.data.remote.model.*
import retrofit2.*
import retrofit2.http.*

interface YandexDiskApiService {
    @GET("v1/disk/resources")
    suspend fun getDiskResources(
        @Query("path") path: String = "disk:/"
    ): DiskResourceResponse

    @GET("v1/disk/resources/download")
    suspend fun getDownloadLink(
        @Query("path") path: String
    ): DownloadResponse

    @GET("v1/disk/resources/upload")
    suspend fun getUploadLink(
        @Query("path", encoded = true) path: String,
        @Query("overwrite") overwrite: Boolean = true
    ): UploadHrefResponse

    @PUT("v1/disk/resources")
    suspend fun createFolder(
        @Query(value = "path", encoded = true) path: String
    ): Response<Void>

    @POST("v1/disk/resources/move")
    suspend fun move(
        @Query(value = "from", encoded = true) fromPath: String,
        @Query(value = "path", encoded = true) toPath: String,
        @Query("overwrite") overwrite: Boolean = true
    ): Response<Void>

    @DELETE("v1/disk/resources")
    suspend fun deleteResource(
        @Query(value = "path", encoded = true) path: String,
        @Query("permanently") permanently: Boolean = true
    ): Response<Void>
}