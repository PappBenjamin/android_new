package com.firstapp.myapplication.ui.home

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.firstapp.myapplication.auth.TokenManager
import com.firstapp.myapplication.network.dto.ScheduleResponseDto
import com.firstapp.myapplication.repository.ScheduleRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.time.LocalDate

class HomeViewModel(context: Context) : ViewModel() {
    private val tokenManager = TokenManager(context)
    private val scheduleRepository = ScheduleRepository(tokenManager)

    // UI State
    private val _currentDate = MutableLiveData<String?>()
    val currentDate: LiveData<String?> = _currentDate

    private val _schedules = MutableLiveData<List<ScheduleResponseDto>>()
    val schedules: LiveData<List<ScheduleResponseDto>> = _schedules

    // Loading/Error state
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage

    private val _navigateToAddSchedule = MutableLiveData<Boolean>()
    val navigateToAddSchedule: LiveData<Boolean> = _navigateToAddSchedule

    private val _navigateToScheduleDetails = MutableLiveData<Int?>()
    val navigateToScheduleDetails: LiveData<Int?> = _navigateToScheduleDetails

    private var currentDateIndex = 0 // Start from tomorrow (1 day ahead)

    init {
        setCurrentDate()
    }

    private fun setCurrentDate() {
        val displayDate = LocalDate.now().plusDays(currentDateIndex.toLong())
        val formatter = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.set(displayDate.year, displayDate.monthValue - 1, displayDate.dayOfMonth)
        _currentDate.value = formatter.format(calendar.time)
    }

    fun loadScheduleData() {
        val date = LocalDate.now().plusDays(currentDateIndex.toLong()).toString()
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = scheduleRepository.getSchedulesByDay(date)
                result.onSuccess { scheduleList ->
                    _schedules.value = scheduleList
                    if (scheduleList.isEmpty()) {
                        _toastMessage.value = "No schedules for this day"
                    }
                    _isLoading.value = false
                }
                result.onFailure { error ->
                    _toastMessage.value = "Failed to load schedules: ${error.message}"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _toastMessage.value = "Error: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun nextDay() {
        currentDateIndex++
        setCurrentDate()
        loadScheduleData()
    }

    fun previousDay() {
        if (currentDateIndex > 0) {
            currentDateIndex--
            setCurrentDate()
            loadScheduleData()
        }
    }

    fun onScheduleItemClick(scheduleId: Int) {
        _navigateToScheduleDetails.value = scheduleId
    }

    fun navigateToAddSchedule() {
        _navigateToAddSchedule.value = true
    }

    fun clearNavigationFlags() {
        _navigateToAddSchedule.value = false
        _navigateToScheduleDetails.value = null
    }

    fun clearToastMessage() {
        _toastMessage.value = null
    }
}
