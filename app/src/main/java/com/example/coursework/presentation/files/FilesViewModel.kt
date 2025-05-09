package com.example.coursework.presentation.files

import android.app.*
import android.content.*
import android.net.*
import androidx.lifecycle.*
import com.example.coursework.data.remote.model.*
import com.example.coursework.data.repository.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

enum class SortMode(val label: String) {
    NAME("Название"),
    TYPE("Тип"),
    SIZE("Размер"),
    MODIFIED("Дата изменения")
}

class FilesViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = FilesRepository(app)

    private val _sort = MutableStateFlow(SortMode.NAME)
    val sortMode: StateFlow<SortMode> = _sort

    private val _path = MutableStateFlow("disk:/")
    val currentPath: StateFlow<String> = _path

    private val _busy = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _busy

    private val _files = MutableStateFlow<List<DiskFile>>(emptyList())
    val files: StateFlow<List<DiskFile>> = _files

    fun setSort(mode: SortMode) {
        _sort.value = mode
        _files.value = sort(_files.value)
    }

    private fun sort(list: List<DiskFile>): List<DiskFile> = when (_sort.value) {
        SortMode.NAME     -> list.sortedBy { it.name.lowercase() }
        SortMode.TYPE     -> list.sortedBy { it.type }
        SortMode.SIZE     -> list.sortedBy { it.size }
        SortMode.MODIFIED -> list.sortedByDescending { it.modified }
    }

    fun load(path: String = _path.value) = viewModelScope.launch {
        _busy.value = true
        _path.value = path
        _files.value = sort(repo.getFiles(path))
        _busy.value = false
    }

    fun createFolder(name: String) = viewModelScope.launch {
        repo.createFolder("${_path.value.removeSuffix("/")}/$name")
        load(_path.value)
    }

    fun createFromTemplate(baseName: String, extension: String, asset: String) = viewModelScope.launch {
        val fullName = "$baseName$extension"
        repo.uploadTemplate(fullName, asset, _path.value)
        load(_path.value)
    }

    fun rename(oldName: String, newName: String) = viewModelScope.launch {
        val cwd = currentPath.value.removeSuffix("/")
        repo.renameFile("$cwd/$oldName", "$cwd/$newName")
        load(currentPath.value)
    }

    fun delete(name: String) = viewModelScope.launch {
        val cwd = currentPath.value.removeSuffix("/")
        repo.deleteFile("$cwd/$name")
        load(currentPath.value)
    }

    fun uploadLocal(uri: Uri, cr: ContentResolver) = viewModelScope.launch {
        repo.uploadLocalFile(uri, cr, _path.value)
        load(_path.value)
    }

    fun move(srcName: String, dstDir: String) = viewModelScope.launch {
        val cwd = currentPath.value.removeSuffix("/")
        val parent = cwd.substringBeforeLast("/", "disk:")
        val fromPath = "$cwd/$srcName"
        val toPath = if (dstDir == "..") {
            "$parent/$srcName"
        } else {
            "$cwd/$dstDir/$srcName"
        }

        repo.renameFile(fromPath, toPath)
        load(currentPath.value)
    }
}