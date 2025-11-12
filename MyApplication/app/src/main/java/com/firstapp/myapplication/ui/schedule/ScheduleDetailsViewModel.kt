package com.firstapp.myapplication.ui.schedule

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

class ScheduleDetailsViewModel(context: Context) : ViewModel() {
    private val tokenManager = TokenManager(context)
    private val scheduleRepository = ScheduleRepository(tokenManager)

    // UI State
    private val _scheduleDetails = MutableLiveData<ScheduleResponseDto?>()
    val scheduleDetails: LiveData<ScheduleResponseDto?> = _scheduleDetails

    // Loading/Error state
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage

    private val _navigateToEditSchedule = MutableLiveData<Boolean>()
    val navigateToEditSchedule: LiveData<Boolean> = _navigateToEditSchedule

    private val _navigateToAddProgress = MutableLiveData<Boolean>()
    val navigateToAddProgress: LiveData<Boolean> = _navigateToAddProgress

    private val _navigateBack = MutableLiveData<Boolean>()
    val navigateBack: LiveData<Boolean> = _navigateBack

    fun loadScheduleDetails(scheduleId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = scheduleRepository.getScheduleById(scheduleId)
                result.onSuccess { schedule ->
                    _scheduleDetails.value = schedule
                    _isLoading.value = false
                }
                result.onFailure { error ->
                    _toastMessage.value = "Failed to load schedule: ${error.message}"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _toastMessage.value = "Error: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun deleteSchedule(scheduleId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = scheduleRepository.deleteSchedule(scheduleId)
                result.onSuccess {
                    _toastMessage.value = "Schedule deleted successfully"
                    _navigateBack.value = true
                    _isLoading.value = false
                }
                result.onFailure { error ->
                    _toastMessage.value = "Failed to delete schedule: ${error.message}"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _toastMessage.value = "Error: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun navigateToEditSchedule() {
        _navigateToEditSchedule.value = true
    }

    fun navigateToAddProgress() {
        _navigateToAddProgress.value = true
    }

    fun formatDate(dateString: String): String {
        return try {
            val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString) ?: Date()
            dateFormat.format(date)
        } catch (e: Exception) {
            dateString
        }
    }

    fun getStatusColor(status: String): Int {
        return when (status) {
            "Completed" -> android.graphics.Color.GREEN
            "Planned" -> android.graphics.Color.YELLOW
            "Skipped" -> android.graphics.Color.RED
            else -> android.graphics.Color.BLACK
        }
    }

    fun clearNavigationFlags() {
        _navigateToEditSchedule.value = false
        _navigateToAddProgress.value = false
        _navigateBack.value = false
    }

    fun updateScheduleStatus(scheduleId: Int, newStatus: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val currentSchedule = _scheduleDetails.value ?: return@launch
                val result = scheduleRepository.updateSchedule(
                    scheduleId = scheduleId,
                    status = newStatus,
                    startTime = currentSchedule.startTime,
                    endTime = currentSchedule.endTime,
                    durationMinutes = currentSchedule.durationMinutes,
                    date = currentSchedule.date
                )
                result.onSuccess {
                    _scheduleDetails.value = it
                    _toastMessage.value = "Status updated to $newStatus"
                    _isLoading.value = false
                }
                result.onFailure { error ->
                    _toastMessage.value = "Failed to update status: ${error.message}"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _toastMessage.value = "Error: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun clearToastMessage() {
        _toastMessage.value = null
    }
}
