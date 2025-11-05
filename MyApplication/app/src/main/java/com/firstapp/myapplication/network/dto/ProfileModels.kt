package com.firstapp.myapplication.network.dto

import com.google.gson.annotations.SerializedName

// Profile Response
data class ProfileResponseDto(
    val id: Int,
    val email: String,
    val username: String,
    val description: String?,
    @SerializedName("profileImageUrl")
    val profileImageUrl: String?,
    @SerializedName("coverImageUrl")
    val coverImageUrl: String?,
    val fcmToken: String,
    val preferences: String?,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

// Habit Models
data class HabitResponseDto(
    val id: Int,
    val name: String,
    val description: String?,
    val goal: String?,
    val categoryId: Int,
    val category: HabitCategoryDto?,
    val userId: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

data class HabitCategoryDto(
    val id: Int,
    val name: String,
    val iconUrl: String?
)

// Update Profile Request
data class UpdateProfileRequest(
    val username: String
)

