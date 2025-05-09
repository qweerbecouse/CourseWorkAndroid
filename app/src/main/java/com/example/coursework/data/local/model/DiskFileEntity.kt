package com.example.coursework.data.local.model

import androidx.room.*

@Entity(tableName = "disk_files")
data class DiskFileEntity(
    @PrimaryKey val name: String,
    val size: Long,
    val type: String,
    val modified: Long
)