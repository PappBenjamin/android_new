package com.firstapp.myapplication.ui.schedule

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.firstapp.myapplication.auth.TokenManager
import com.firstapp.myapplication.network.dto.HabitResponseDto
import com.firstapp.myapplication.repository.ProfileRepository
import com.firstapp.myapplication.repository.ScheduleRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddScheduleViewModel(context: Context) : ViewModel() {
    private val tokenManager = TokenManager(context)
    private val profileRepository = ProfileRepository(tokenManager)
    private val scheduleRepository = ScheduleRepository(tokenManager)

    // UI State
    private val _habits = MutableLiveData<List<HabitResponseDto>>()
    val habits: LiveData<List<HabitResponseDto>> = _habits

    private val _selectedHabitId = MutableLiveData<Int?>()
    val selectedHabitId: LiveData<Int?> = _selectedHabitId

    private val _repeatPattern = MutableLiveData<String>("none")
    val repeatPattern: LiveData<String> = _repeatPattern

    private val _todayDate = MutableLiveData<String?>()
    val todayDate: LiveData<String?> = _todayDate

    private val _calculatedDuration = MutableLiveData<Int?>(null)
    val calculatedDuration: LiveData<Int?> = _calculatedDuration

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

    fun loadHabits() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = profileRepository.getHabits()
                result.onSuccess { habitList ->
                    _habits.value = habitList
                    _isLoading.value = false
                }
                result.onFailure { error ->
                    _toastMessage.value = "Failed to load habits: ${error.message}"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _toastMessage.value = "Error: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun setSelectedHabit(habitId: Int) {
        _selectedHabitId.value = habitId
    }

    fun setRepeatPattern(pattern: String) {
        _repeatPattern.value = pattern
    }

    fun calculateDuration(startTime: String, endTime: String) {
        if (startTime.isEmpty() || endTime.isEmpty()) {
            _calculatedDuration.value = null
            return
        }

        try {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val startDate = timeFormat.parse(startTime)
            val endDate = timeFormat.parse(endTime)

            if (startDate != null && endDate != null) {
                var durationMs = endDate.time - startDate.time

                // Handle case where end time is next day
                if (durationMs < 0) {
                    durationMs += 24 * 60 * 60 * 1000
                }

                val durationMinutes = (durationMs / (1000 * 60)).toInt()
                if (durationMinutes > 0) {
                    _calculatedDuration.value = durationMinutes
                }
            }
        } catch (_: Exception) {
            // If parsing fails, don't update duration
        }
    }

    fun createSchedule(
        startTime: String,
        endTime: String,
        date: String,
        notes: String
    ) {
        val trimmedStartTime = startTime.trim()
        val trimmedEndTime = endTime.trim()
        val trimmedDate = date.trim()
        val trimmedNotes = notes.trim()
        val pattern = _repeatPattern.value ?: "none"

        if (_selectedHabitId.value == null) {
            _toastMessage.value = "Please select a habit"
            return
        }

        if (trimmedStartTime.isEmpty()) {
            _toastMessage.value = "Please enter start time"
            return
        }

        if (pattern == "none" && trimmedDate.isEmpty()) {
            _toastMessage.value = "Please enter a date"
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val endTimeOrNull = trimmedEndTime.ifEmpty { null }
                val notesOrNull = trimmedNotes.ifEmpty { null }

                if (pattern == "none") {
                    // Create custom schedule
                    val result = scheduleRepository.createCustomSchedule(
                        habitId = _selectedHabitId.value!!,
                        date = trimmedDate,
                        startTime = trimmedStartTime,
                        endTime = endTimeOrNull,
                        notes = notesOrNull
                    )

                    result.onSuccess {
                        _toastMessage.value = "Schedule created successfully"
                        _navigateBack.value = true
                        _isLoading.value = false
                    }

                    result.onFailure { error ->
                        _toastMessage.value = "Failed to create schedule: ${error.message}"
                        _isLoading.value = false
                    }
                } else {
                    // Create recurring schedule
                    val result = scheduleRepository.createRecurringSchedule(
                        habitId = _selectedHabitId.value!!,
                        startTime = trimmedStartTime,
                        repeatPattern = pattern,
                        endTime = endTimeOrNull,
                        notes = notesOrNull
                    )

                    result.onSuccess {
                        _toastMessage.value = "Recurring schedule created successfully"
                        _navigateBack.value = true
                        _isLoading.value = false
                    }

                    result.onFailure { error ->
                        _toastMessage.value = "Failed to create recurring schedule: ${error.message}"
                        _isLoading.value = false
                    }
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
