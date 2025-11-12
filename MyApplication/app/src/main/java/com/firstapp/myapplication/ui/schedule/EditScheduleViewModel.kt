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
import java.time.LocalTime
import java.time.Duration

class EditScheduleViewModel(context: Context) : ViewModel() {
    private val tokenManager = TokenManager(context)
    private val scheduleRepository = ScheduleRepository(tokenManager)

    // UI State
    private val _currentSchedule = MutableLiveData<ScheduleResponseDto?>()
    val currentSchedule: LiveData<ScheduleResponseDto?> = _currentSchedule

    private val _startTime = MutableLiveData<String?>()
    val startTime: LiveData<String?> = _startTime

    private val _endTime = MutableLiveData<String?>()
    val endTime: LiveData<String?> = _endTime

    private val _status = MutableLiveData<String?>()
    val status: LiveData<String?> = _status

    private val _notes = MutableLiveData<String?>()
    val notes: LiveData<String?> = _notes

    private val _participants = MutableLiveData<String?>()
    val participants: LiveData<String?> = _participants

    private val _calculatedDuration = MutableLiveData<Int?>(null)
    val calculatedDuration: LiveData<Int?> = _calculatedDuration

    // Loading/Error state
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage

    private val _navigateBack = MutableLiveData<Boolean>()
    val navigateBack: LiveData<Boolean> = _navigateBack

    fun loadScheduleData(scheduleId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = scheduleRepository.getScheduleById(scheduleId)
                result.onSuccess { schedule ->
                    _currentSchedule.value = schedule
                    _startTime.value = parseTimeFromIso(schedule.startTime) ?: ""
                    _endTime.value = parseTimeFromIso(schedule.endTime) ?: ""
                    _status.value = schedule.status
                    _notes.value = schedule.notes ?: ""
                    _participants.value = schedule.participantIds?.joinToString(", ") ?: ""
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

    fun updateStartTime(time: String) {
        _startTime.value = time
    }

    fun updateEndTime(time: String) {
        _endTime.value = time
    }

    fun updateStatus(newStatus: String) {
        _status.value = newStatus
    }

    fun updateNotes(newNotes: String) {
        _notes.value = newNotes
    }

    fun updateParticipants(newParticipants: String) {
        _participants.value = newParticipants
    }

    fun updateSchedule(scheduleId: Int) {
        val start = _startTime.value?.trim() ?: ""
        val end = _endTime.value?.trim() ?: ""
        val statusValue = _status.value ?: "Planned"
        val notesValue = _notes.value?.trim() ?: ""
        val participantsValue = _participants.value?.trim() ?: ""
        val schedule = _currentSchedule.value ?: return

        if (start.isEmpty() || end.isEmpty()) {
            _toastMessage.value = "Please enter start time and end time"
            return
        }

        val durationMinutes = calculateDuration(start, end)
        if (end.isNotEmpty() && durationMinutes == null) {
            _toastMessage.value = "Invalid time format. Please use HH:mm"
            return
        }

        val participantIds = if (participantsValue.isNotEmpty()) {
            participantsValue.split(",").mapNotNull { it.trim().toIntOrNull() }
        } else {
            null
        }

        val datePart = schedule.date.split("T")[0]
        val startIso = if (start.isNotEmpty() && datePart.isNotEmpty()) {
            "${datePart}T${start}:00.000Z"
        } else {
            start
        }
        val endIso = if (end.isNotEmpty() && datePart.isNotEmpty()) {
            "${datePart}T${end}:00.000Z"
        } else {
            end.ifEmpty { null }
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = scheduleRepository.updateSchedule(
                    scheduleId = scheduleId,
                    startTime = startIso,
                    endTime = endIso,
                    durationMinutes = durationMinutes,
                    status = statusValue,
                    date = schedule.date,
                    notes = notesValue,
                    participantIds = participantIds
                )

                result.onSuccess {
                    _toastMessage.value = "Schedule updated successfully"
                    _navigateBack.value = true
                    _isLoading.value = false
                }

                result.onFailure { error ->
                    _toastMessage.value = "Failed to update schedule: ${error.message}"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _toastMessage.value = "Error: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    private fun parseTimeFromIso(iso: String?): String? {
        return try {
            iso?.substring(11, 16)
        } catch (_: Exception) {
            null
        }
    }

    fun calculateDuration(startTime: String, endTime: String): Int? {
        return try {
            val start = LocalTime.parse(startTime)
            val end = LocalTime.parse(endTime)

            var duration = Duration.between(start, end)
            if (duration.isNegative) {
                duration = duration.plusHours(24)
            }

            duration.toMinutes().toInt()
        } catch (_: Exception) {
            null
        }
    }

    fun clearNavigateBack() {
        _navigateBack.value = false
    }

    fun clearToastMessage() {
        _toastMessage.value = null
    }
}
