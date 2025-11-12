package com.firstapp.myapplication.ui.schedule

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.firstapp.myapplication.auth.TokenManager
import com.firstapp.myapplication.repository.ScheduleRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddProgressViewModel(context: Context) : ViewModel() {
    private val tokenManager = TokenManager(context)
    private val scheduleRepository = ScheduleRepository(tokenManager)

    // UI State
    private val _todayDate = MutableLiveData<String?>()
    val todayDate: LiveData<String?> = _todayDate

    private val _loggedTime = MutableLiveData<String?>()
    val loggedTime: LiveData<String?> = _loggedTime

    private val _notes = MutableLiveData<String?>()
    val notes: LiveData<String?> = _notes

    private val _isCompleted = MutableLiveData<Boolean>(false)
    val isCompleted: LiveData<Boolean> = _isCompleted

    // Loading/Error state
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage

    private val _navigateBack = MutableLiveData<Boolean>()
    val navigateBack: LiveData<Boolean> = _navigateBack

    init {
        setTodayDate()
    }

    private fun setTodayDate() {
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        _todayDate.value = todayDate
    }

    fun setLoggedTime(time: String) {
        _loggedTime.value = time
    }

    fun setNotes(newNotes: String) {
        _notes.value = newNotes
    }

    fun setIsCompleted(completed: Boolean) {
        _isCompleted.value = completed
    }

    fun addProgress(scheduleId: Int, date: String) {
        val trimmedDate = date.trim()
        val loggedTimeStr = _loggedTime.value?.trim() ?: ""
        val notesValue = _notes.value?.trim() ?: ""
        val completed = _isCompleted.value ?: false

        if (trimmedDate.isEmpty()) {
            _toastMessage.value = "Please enter a date"
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = scheduleRepository.createProgress(
                    scheduleId = scheduleId,
                    date = trimmedDate,
                    loggedTime = loggedTimeStr.toIntOrNull(),
                    notes = notesValue.ifEmpty { null },
                    isCompleted = completed
                )

                result.onSuccess {
                    _toastMessage.value = "Progress added successfully"
                    _navigateBack.value = true
                    _isLoading.value = false
                }

                result.onFailure { error ->
                    _toastMessage.value = "Failed to add progress: ${error.message}"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _toastMessage.value = "Error: ${e.message}"
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
