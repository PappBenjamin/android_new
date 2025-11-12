package com.firstapp.myapplication.ui.habit

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.firstapp.myapplication.auth.TokenManager
import com.firstapp.myapplication.network.dto.HabitCategoryDto
import com.firstapp.myapplication.repository.ProfileRepository
import kotlinx.coroutines.launch

class AddHabitViewModel(context: Context) : ViewModel() {
    private val tokenManager = TokenManager(context)
    private val profileRepository = ProfileRepository(tokenManager)

    // UI State
    private val _categories = MutableLiveData<List<HabitCategoryDto>>()
    val categories: LiveData<List<HabitCategoryDto>> = _categories

    private val _selectedCategoryId = MutableLiveData<Int?>()
    val selectedCategoryId: LiveData<Int?> = _selectedCategoryId

    // Loading/Error state
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage

    private val _navigateBack = MutableLiveData<Boolean>()
    val navigateBack: LiveData<Boolean> = _navigateBack

    fun loadHabitCategories() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = profileRepository.getHabitCategories()
                result.onSuccess { categoryList ->
                    _categories.value = categoryList
                    _isLoading.value = false
                }
                result.onFailure { error ->
                    _toastMessage.value = "Failed to load categories: ${error.message}"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _toastMessage.value = "Error loading categories: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun setSelectedCategory(categoryId: Int) {
        _selectedCategoryId.value = categoryId
    }

    fun createHabit(name: String, description: String?, goal: String?) {
        val trimmedName = name.trim()

        if (trimmedName.isEmpty()) {
            _toastMessage.value = "Please enter a habit name"
            return
        }

        if (_selectedCategoryId.value == null) {
            _toastMessage.value = "Please select a category"
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = profileRepository.createHabit(
                    name = trimmedName,
                    description = description?.trim()?.ifEmpty { null },
                    goal = goal?.trim()?.ifEmpty { null },
                    categoryId = _selectedCategoryId.value!!
                )

                result.onSuccess { habit ->
                    _toastMessage.value = "Habit '${habit.name}' created successfully!"
                    _navigateBack.value = true
                    _isLoading.value = false
                }

                result.onFailure { error ->
                    _toastMessage.value = "Failed to create habit: ${error.message}"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _toastMessage.value = "Error creating habit: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun clearNavigateBack() {
        _navigateBack.value = false
    }

    fun clearToastMessage() {
        _toastMessage.value = null
    }
}
