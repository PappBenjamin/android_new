package com.firstapp.myapplication.network.api

import com.firstapp.myapplication.network.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ScheduleApiService {

    // Get schedules by day
    @GET("schedule/day")
    suspend fun getSchedulesByDay(
        @Query("date") date: String? = null
    ): Response<List<ScheduleResponseDto>>

    // Get schedule by ID
    @GET("schedule/{id}")
    suspend fun getScheduleById(
        @Path("id") scheduleId: Int
    ): Response<ScheduleResponseDto>

    // Create custom schedule
    @POST("schedule/custom")
    suspend fun createCustomSchedule(
        @Body request: CreateCustomScheduleRequest
    ): Response<ScheduleResponseDto>

    // Create recurring schedule
    @POST("schedule/recurring")
    suspend fun createRecurringSchedule(
        @Body request: CreateRecurringScheduleRequest
    ): Response<List<ScheduleResponseDto>>

    // Create weekday recurring schedule
    @POST("schedule/recurring/weekdays")
    suspend fun createWeekdaySchedule(
        @Body request: CreateWeekdayScheduleRequest
    ): Response<List<ScheduleResponseDto>>

    // Update schedule
    @PATCH("schedule/{id}")
    suspend fun updateSchedule(
        @Path("id") scheduleId: Int,
        @Body request: UpdateScheduleRequest
    ): Response<ScheduleResponseDto>

    // Delete schedule
    @DELETE("schedule/{id}")
    suspend fun deleteSchedule(
        @Path("id") scheduleId: Int
    ): Response<Unit>

    // Create progress
    @POST("progress")
    suspend fun createProgress(
        @Body request: CreateProgressRequest
    ): Response<ProgressResponseDto>
}

