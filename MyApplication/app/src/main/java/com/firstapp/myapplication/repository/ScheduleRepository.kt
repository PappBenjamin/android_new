package com.firstapp.myapplication.repository

import com.firstapp.myapplication.auth.TokenManager
import com.firstapp.myapplication.network.ApiClient
import com.firstapp.myapplication.network.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ScheduleRepository(private val tokenManager: TokenManager) {

    suspend fun getSchedulesByDay(date: String? = null): Result<List<ScheduleResponseDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.scheduleService.getSchedulesByDay(date)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Failed to fetch schedules: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getScheduleById(scheduleId: Int): Result<ScheduleResponseDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.scheduleService.getScheduleById(scheduleId)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Failed to fetch schedule: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun createCustomSchedule(
        habitId: Int,
        date: String,
        startTime: String,
        endTime: String? = null,
        durationMinutes: Int? = null,
        participantIds: List<Int>? = null,
        notes: String? = null
    ): Result<ScheduleResponseDto> {
        return withContext(Dispatchers.IO) {
            try {
                val request = CreateCustomScheduleRequest(
                    habitId = habitId,
                    date = date,
                    startTime = startTime,
                    endTime = endTime,
                    durationMinutes = durationMinutes,
                    isCustom = true,
                    participantIds = participantIds,
                    notes = notes
                )
                val response = ApiClient.scheduleService.createCustomSchedule(request)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Failed to create schedule: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun createRecurringSchedule(
        habitId: Int,
        startTime: String,
        repeatPattern: String,
        endTime: String? = null,
        durationMinutes: Int? = null,
        repeatDays: Int = 30,
        participantIds: List<Int>? = null,
        notes: String? = null
    ): Result<List<ScheduleResponseDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val request = CreateRecurringScheduleRequest(
                    habitId = habitId,
                    startTime = startTime,
                    repeatPattern = repeatPattern,
                    isCustom = true,
                    endTime = endTime,
                    durationMinutes = durationMinutes,
                    repeatDays = repeatDays,
                    participantIds = participantIds,
                    notes = notes
                )
                val response = ApiClient.scheduleService.createRecurringSchedule(request)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Failed to create recurring schedule: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun createWeekdaySchedule(
        habitId: Int,
        startTime: String,
        daysOfWeek: List<Int>,
        numberOfWeeks: Int = 4,
        durationMinutes: Int? = null,
        endTime: String? = null,
        participantIds: List<Int>? = null,
        notes: String? = null
    ): Result<List<ScheduleResponseDto>> {
        return withContext(Dispatchers.IO) {
            try {
                val request = CreateWeekdayScheduleRequest(
                    habitId = habitId,
                    startTime = startTime,
                    daysOfWeek = daysOfWeek,
                    numberOfWeeks = numberOfWeeks,
                    durationMinutes = durationMinutes,
                    endTime = endTime,
                    participantIds = participantIds,
                    notes = notes
                )
                val response = ApiClient.scheduleService.createWeekdaySchedule(request)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Failed to create weekday schedule: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun updateSchedule(
        scheduleId: Int,
        startTime: String? = null,
        endTime: String? = null,
        durationMinutes: Int? = null,
        status: String? = null,
        date: String? = null,
        participantIds: List<Int>? = null,
        notes: String? = null
    ): Result<ScheduleResponseDto> {
        return withContext(Dispatchers.IO) {
            try {
                val request = UpdateScheduleRequest(
                    startTime = startTime,
                    endTime = endTime,
                    durationMinutes = durationMinutes,
                    status = status,
                    date = date,
                    isCustom = null,
                    participantIds = participantIds,
                    notes = notes
                )
                val response = ApiClient.scheduleService.updateSchedule(scheduleId, request)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Failed to update schedule: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteSchedule(scheduleId: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.scheduleService.deleteSchedule(scheduleId)
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to delete schedule: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun createProgress(
        scheduleId: Int,
        date: String,
        loggedTime: Int? = null,
        notes: String? = null,
        isCompleted: Boolean? = null
    ): Result<ProgressResponseDto> {
        return withContext(Dispatchers.IO) {
            try {
                val request = CreateProgressRequest(
                    scheduleId = scheduleId,
                    date = date,
                    loggedTime = loggedTime,
                    notes = notes,
                    isCompleted = isCompleted
                )
                val response = ApiClient.scheduleService.createProgress(request)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Failed to create progress: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

