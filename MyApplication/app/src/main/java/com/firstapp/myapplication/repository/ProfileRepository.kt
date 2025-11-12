package com.firstapp.myapplication.repository

import com.firstapp.myapplication.auth.TokenManager
import com.firstapp.myapplication.network.ApiClient
import com.firstapp.myapplication.network.api.CreateHabitRequest
import com.firstapp.myapplication.network.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProfileRepository(private val tokenManager: TokenManager) {

    // Result is a type that either holds a success value or an exception
    suspend fun getCurrentProfile(): Result<ProfileResponseDto> {
        return withContext(Dispatchers.IO)
        {
            try
            {
                val response = ApiClient.profileService.getCurrentProfile()

                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                }
                else
                {
                    Result.failure(Exception("Failed to fetch profile: ${response.code()}"))
                }
            }
            catch (e: Exception)
            {
                Result.failure(e)
            }
        }
    }

    suspend fun logout(): Result<Unit> {

        return withContext(Dispatchers.IO)
        {
            try
            {
                val token = tokenManager.getAccessToken() ?: return@withContext Result.failure(Exception("No access token"))
                val response = ApiClient.authService.logout("Bearer $token")

                if (response.isSuccessful)
                {
                    // Clear tokens after successful logout
                    tokenManager.clearTokens()
                    Result.success(Unit)
                }
                else
                {
                    Result.failure(Exception("Logout failed: ${response.code()}"))
                }
            }
            catch (e: Exception)
            {
                // Clear tokens even if request fails
                tokenManager.clearTokens()
                Result.failure(e)
            }
        }
    }

    suspend fun getHabits(): Result<List<HabitResponseDto>> {

        return withContext(Dispatchers.IO)
        {
            try
            {
                val response = ApiClient.profileService.getHabits()
                if (response.isSuccessful && response.body() != null)
                {
                    Result.success(response.body()!!)
                }
                else
                {
                    Result.failure(Exception("Failed to fetch habits: ${response.code()}"))
                }
            }
            catch (e: Exception)
            {
                Result.failure(e)
            }
        }
    }

    suspend fun getHabitCategories(): Result<List<HabitCategoryDto>> {

        return withContext(Dispatchers.IO)
        {
            try
            {
                val response = ApiClient.profileService.getHabitCategories()
                if (response.isSuccessful && response.body() != null)
                {
                    Result.success(response.body()!!)
                }
                else
                {
                    Result.failure(Exception("Failed to fetch categories: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun createHabit(name: String, description: String?, goal: String?, categoryId: Int): Result<HabitResponseDto> {

        return withContext(Dispatchers.IO)
        {
            try
            {
                val request = CreateHabitRequest(
                    name = name,
                    description = description,
                    goal = goal,
                    categoryId = categoryId
                )

                val response = ApiClient.profileService.createHabit(request)

                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                }
                else
                {
                    Result.failure(Exception("Failed to create habit: ${response.code()}"))
                }
            }
            catch (e: Exception)
            {
                Result.failure(e)
            }
        }
    }

    suspend fun updateProfile(username: String): Result<ProfileResponseDto> {

        return withContext(Dispatchers.IO)
        {
            try
            {
                val request = UpdateProfileRequest(username = username)
                val response = ApiClient.profileService.updateProfile(request)

                if (response.isSuccessful && response.body() != null)
                {
                    Result.success(response.body()!!)
                }
                else
                {
                    Result.failure(Exception("Failed to update profile: ${response.code()}"))
                }
            }
            catch (e: Exception)
            {
                Result.failure(e)
            }
        }
    }


    // okhttp3.MultipartBody.Part used for file upload
    suspend fun uploadProfileImage(file: okhttp3.MultipartBody.Part): Result<ProfileResponseDto> {
        return withContext(Dispatchers.IO)
        {
            try
            {
                val response = ApiClient.profileService.uploadProfileImage(file)

                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                }
                else
                {
                    Result.failure(Exception("Failed to upload profile image: ${response.code()}"))
                }
            }
            catch (e: Exception)
            {
                Result.failure(e)
            }
        }
    }

}
