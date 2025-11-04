package com.firstapp.myapplication.network.models

data class ScheduleResponseDto(
    val id: String,
    val habitName: String,
    val habitIcon: String?,
    val scheduledTime: String, // "HH:mm" format
    val duration: Int?, // in minutes
    val status: ScheduleStatus,
    val notes: String?,
    val isCompleted: Boolean,
    val timeOfDay: TimeOfDay,
    val habitColor: String? = null
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
    fun getSampleSchedules(): List<ScheduleResponseDto> = listOf(
        // Morning
        ScheduleResponseDto(
            id = "1",
            habitName = "Drink rum",
            habitIcon = "🥃",
            scheduledTime = "06:00",
            duration = 5,
            status = ScheduleStatus.COMPLETED,
            notes = null,
            isCompleted = true,
            timeOfDay = TimeOfDay.MORNING,
            habitColor = "#8B5CF6"
        ),
        ScheduleResponseDto(
            id = "2", 
            habitName = "Walk the plank",
            habitIcon = "🏴‍☠️",
            scheduledTime = "11:00",
            duration = 30,
            status = ScheduleStatus.PLANNED,
            notes = null,
            isCompleted = false,
            timeOfDay = TimeOfDay.MORNING,
            habitColor = "#8B5CF6"
        ),
        
        // Afternoon
        ScheduleResponseDto(
            id = "3",
            habitName = "Run from Kraken",
            habitIcon = "🦑",
            scheduledTime = "14:00",
            duration = 45,
            status = ScheduleStatus.PLANNED,
            notes = null,
            isCompleted = false,
            timeOfDay = TimeOfDay.AFTERNOON,
            habitColor = "#8B5CF6"
        ),
        ScheduleResponseDto(
            id = "4",
            habitName = "Follow treasure map",
            habitIcon = "🗺️",
            scheduledTime = "16:00",
            duration = 60,
            status = ScheduleStatus.COMPLETED,
            notes = null,
            isCompleted = true,
            timeOfDay = TimeOfDay.AFTERNOON,
            habitColor = "#06B6D4"
        ),
        ScheduleResponseDto(
            id = "5",
            habitName = "Drink rum",
            habitIcon = "🥃",
            scheduledTime = "15:15",
            duration = 5,
            status = ScheduleStatus.PLANNED,
            notes = null,
            isCompleted = false,
            timeOfDay = TimeOfDay.AFTERNOON,
            habitColor = "#8B5CF6"
        ),
        
        // Night
        ScheduleResponseDto(
            id = "6",
            habitName = "Journal adventures",
            habitIcon = "✍️",
            scheduledTime = "19:30",
            duration = 20,
            status = ScheduleStatus.PLANNED,
            notes = null,
            isCompleted = false,
            timeOfDay = TimeOfDay.NIGHT,
            habitColor = "#F59E0B"
        ),
        ScheduleResponseDto(
            id = "7",
            habitName = "Walk the plank",
            habitIcon = "🏴‍☠️",
            scheduledTime = "18:15",
            duration = 30,
            status = ScheduleStatus.PLANNED,
            notes = null,
            isCompleted = false,
            timeOfDay = TimeOfDay.NIGHT,
            habitColor = "#8B5CF6"
        )
    )
}