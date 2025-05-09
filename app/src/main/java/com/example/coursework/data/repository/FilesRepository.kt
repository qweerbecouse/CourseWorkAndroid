package com.example.coursework.data.repository

import android.content.*
import android.net.*
import android.provider.*
import android.util.*
import com.example.coursework.data.local.*
import com.example.coursework.data.local.model.*
import com.example.coursework.data.remote.*
import com.example.coursework.data.remote.model.*
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.*
import java.nio.charset.*
import java.time.*
import java.time.format.*

class FilesRepository(private val context: Context) {

    private val dao = AppDatabase.getDatabase(context).diskFileDao()

    private fun DiskFile.toEntity(): DiskFileEntity =
        DiskFileEntity(
            name = name,
            size = size,
            type = type,
            modified = runCatching {
                OffsetDateTime.parse(modified).toInstant().toEpochMilli()
            }.getOrElse { 0L }
        )

    suspend fun getFiles(path: String): List<DiskFile> = withContext(Dispatchers.IO) {
        val api = ApiClient.create(context)
        val files = api.getDiskResources(path)._embedded?.items ?: emptyList()
        if (path == "disk:/") {
            dao.clearAll()
            dao.insertAll(files.map { it.toEntity() })
        }
        return@withContext files
    }

    suspend fun createFolder(fullPath: String) = withContext(Dispatchers.IO) {
        val safe = URLEncoder.encode(fullPath, StandardCharsets.UTF_8.name())

        val resp = ApiClient.create(context).createFolder(safe)

        Log.d("CreateFolder", "PUT $safe  →  ${resp.code()}")

        resp.errorBody()?.string()?.let { Log.d("CreateFolder", "errorBody = $it") }

        if (!resp.isSuccessful && resp.code() != 409)
            error("createFolder HTTP ${resp.code()}")
    }

    private suspend fun uploadBytes(fullPath: String, bytes: ByteArray) =
        withContext(Dispatchers.IO) {
            val api = ApiClient.create(context)
            val safe = URLEncoder.encode(fullPath, StandardCharsets.UTF_8.name())
            val href = api.getUploadLink(safe).href
            val req = Request.Builder().url(href).put(bytes.toRequestBody()).build()
            OkHttpClient().newCall(req).execute().use { r ->
                if (!r.isSuccessful) error("upload ${r.code}")
            }
        }

    suspend fun uploadTemplate(nameOnDisk: String, assetFile: String, cwd: String) {
        val bytes = context.assets.open("templates/$assetFile").readBytes()
        uploadBytes("${cwd.removeSuffix("/")}/$nameOnDisk", bytes)
    }

    suspend fun renameFile(from: String, to: String) = withContext(Dispatchers.IO) {
        val safeFrom = URLEncoder.encode(from, StandardCharsets.UTF_8.name())
        val safeTo = URLEncoder.encode(to, StandardCharsets.UTF_8.name())
        val resp = ApiClient.create(context).move(safeFrom, safeTo)
        if (!resp.isSuccessful) error("rename HTTP ${resp.code()}")
    }

    suspend fun deleteFile(fullPath: String, permanently: Boolean = true) =
        withContext(Dispatchers.IO) {
            val safe = URLEncoder.encode(fullPath, StandardCharsets.UTF_8.name())
            val resp = ApiClient.create(context).deleteResource(safe, permanently)
            if (!resp.isSuccessful) error("delete HTTP ${resp.code()}")
        }

    suspend fun uploadLocalFile(
        uri: Uri,
        cr: ContentResolver,
        cwd: String
    ) = withContext(Dispatchers.IO) {

        val name = cr.query(uri, null, null, null, null)?.use { c ->
            val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            c.moveToFirst(); c.getString(idx)
        } ?: "noname.bin"

        val bytes = cr.openInputStream(uri)?.readBytes()
            ?: error("Не удалось прочитать файл")

        val api = ApiClient.create(context)
        val href = api.getUploadLink("${cwd.removeSuffix("/")}/$name").href

        val req = okhttp3.Request.Builder()
            .url(href)
            .put(bytes.toRequestBody(null))
            .build()
        okhttp3.OkHttpClient().newCall(req).execute().use { r ->
            if (!r.isSuccessful) error("upload HTTP ${r.code}")
        }
    }
}