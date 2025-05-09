package com.example.coursework.data.remote.model

import com.google.gson.annotations.SerializedName

data class DownloadResponse(
    @SerializedName("href") val href: String
)