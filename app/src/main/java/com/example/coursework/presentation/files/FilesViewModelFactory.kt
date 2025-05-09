package com.example.coursework.presentation.files

import android.app.*
import androidx.lifecycle.*

class FilesViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FilesViewModel(application) as T
    }
}