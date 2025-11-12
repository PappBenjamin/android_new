package com.firstapp.myapplication.ui.schedule

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class EditScheduleViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(EditScheduleViewModel::class.java) -> {
                EditScheduleViewModel(context) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

