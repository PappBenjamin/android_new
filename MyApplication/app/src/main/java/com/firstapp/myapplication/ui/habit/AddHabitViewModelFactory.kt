package com.firstapp.myapplication.ui.habit

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AddHabitViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AddHabitViewModel::class.java) -> {
                AddHabitViewModel(context) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

