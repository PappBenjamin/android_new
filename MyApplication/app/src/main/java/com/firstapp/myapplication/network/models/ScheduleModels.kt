package com.firstapp.myapplication.network.models

import com.google.gson.annotations.SerializedName

// Response DTOs
data class ScheduleResponseDto(
    val id: Int,
    val habitId: Int,
    val habit: HabitResponseDto?,
    @SerializedName("start_time")
    val startTime: String,
    @SerializedName("end_time")
    val endTime: String?,
    val date: String,
    @SerializedName("duration_minutes")
    val durationMinutes: Int?,
    val status: String, // "Planned", "Completed", "Skipped"
    @SerializedName("is_custom")
    val isCustom: Boolean,
    val notes: String?,
    val progress: List<ProgressResponseDto>?,
    @SerializedName("participantIds")
    val participantIds: List<Int>?,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

data class ProgressResponseDto(
    val id: Int,
    val scheduleId: Int,
    val date: String,
    @SerializedName("logged_time")
    val loggedTime: Int?,
    val notes: String?,
    @SerializedName("is_completed")
    val isCompleted: Boolean,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

// Request DTOs for creating schedules
data class CreateCustomScheduleRequest(
    val habitId: Int,
    val date: String,
    @SerializedName("start_time")
    val startTime: String,
    @SerializedName("end_time")
    val endTime: String? = null,
    @SerializedName("duration_minutes")
    val durationMinutes: Int? = null,
    @SerializedName("is_custom")
    val isCustom: Boolean = true,
    @SerializedName("participantIds")
    val participantIds: List<Int>? = null,
    val notes: String? = null
)

data class CreateRecurringScheduleRequest(
    val habitId: Int,
    @SerializedName("start_time")
    val startTime: String,
    @SerializedName("repeat_pattern")
    val repeatPattern: String, // "none", "daily", "weekdays", "weekends"
    @SerializedName("is_custom")
    val isCustom: Boolean = true,
    @SerializedName("end_time")
    val endTime: String? = null,
    @SerializedName("duration_minutes")
    val durationMinutes: Int? = null,
    @SerializedName("repeat_days")
    val repeatDays: Int = 30,
    @SerializedName("participantIds")
    val participantIds: List<Int>? = null,
    val notes: String? = null
)

data class CreateWeekdayScheduleRequest(
    val habitId: Int,
    @SerializedName("start_time")
    val startTime: String,
    @SerializedName("days_of_week")
    val daysOfWeek: List<Int>, // 1=Monday ... 7=Sunday
    @SerializedName("number_of_weeks")
    val numberOfWeeks: Int = 4,
    @SerializedName("duration_minutes")
    val durationMinutes: Int? = null,
    @SerializedName("end_time")
    val endTime: String? = null,
    @SerializedName("participantIds")
    val participantIds: List<Int>? = null,
    val notes: String? = null
)

data class UpdateScheduleRequest(
    @SerializedName("start_time")
    val startTime: String? = null,
    @SerializedName("end_time")
    val endTime: String? = null,
    @SerializedName("duration_minutes")
    val durationMinutes: Int? = null,
    val status: String? = null, // "Planned", "Completed", "Skipped"
    val date: String? = null,
    @SerializedName("is_custom")
    val isCustom: Boolean? = null,
    @SerializedName("participantIds")
    val participantIds: List<Int>? = null,
    val notes: String? = null
)

data class CreateProgressRequest(
    val scheduleId: Int,
    val date: String,
    @SerializedName("logged_time")
    val loggedTime: Int? = null,
    val notes: String? = null,
    @SerializedName("is_completed")
    val isCompleted: Boolean? = null
)

enum class ScheduleStatus {
    PLANNED, // Tervezett
    COMPLETED, // Befejezett  
    SKIPPED // Kihagyott
}

enum class TimeOfDay {
    MORNING, // Reggel
    AFTERNOON, // Délután
    NIGHT // Este
}

// Sample data for testing
object SampleScheduleData {
    fun getSampleSchedules(): List<ScheduleResponseDto> = emptyList()
}