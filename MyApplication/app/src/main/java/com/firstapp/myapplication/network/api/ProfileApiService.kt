package com.firstapp.myapplication.network.api

import com.firstapp.myapplication.network.models.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ProfileApiService {

    @GET("profile")
    suspend fun getCurrentProfile(): Response<ProfileResponseDto>

    @PATCH("profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<ProfileResponseDto>

    @Multipart
    @POST("profile/upload-profile-image")
    suspend fun uploadProfileImage(@Part image: MultipartBody.Part): Response<ProfileResponseDto>

    @GET("habit")
    suspend fun getHabits(): Response<List<HabitResponseDto>>

    @GET("habit/user/{userId}")
    suspend fun getUserHabits(@Path("userId") userId: Int): Response<List<HabitResponseDto>>

    @POST("habit")
    suspend fun createHabit(@Body request: CreateHabitRequest): Response<HabitResponseDto>

    @GET("habit/categories")
    suspend fun getHabitCategories(): Response<List<HabitCategoryDto>>
}

data class CreateHabitRequest(
    val name: String,
    val description: String?,
    val goal: String?,
    val categoryId: Int
)
