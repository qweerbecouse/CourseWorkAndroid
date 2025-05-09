package com.example.coursework.data.remote.model

data class DiskResourceResponse(
    val _embedded: Embedded?
)

data class Embedded(
    val items: List<DiskFile>
)

data class DiskFile(
    val name: String,
    val size: Long,
    val type: String,
    val modified: String
)