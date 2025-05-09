package com.example.coursework.data.local.dao

import androidx.room.*
import com.example.coursework.data.local.model.DiskFileEntity

@Dao
interface DiskFileDao {

    @Query("SELECT * FROM disk_files")
    suspend fun getAll(): List<DiskFileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(files: List<DiskFileEntity>)

    @Query("DELETE FROM disk_files")
    suspend fun clearAll()
}