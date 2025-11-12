package com.firstapp.myapplication.ui.profile

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.firstapp.myapplication.auth.TokenManager
import com.firstapp.myapplication.network.dto.HabitResponseDto
import com.firstapp.myapplication.repository.ProfileRepository
import kotlinx.coroutines.launch

class ProfileViewModel(context: Context) : ViewModel() {
    private val tokenManager = TokenManager(context)
    private val profileRepository = ProfileRepository(tokenManager)

    // UI State
    private val _username = MutableLiveData<String?>()
    val username: LiveData<String?> = _username

    private val _email = MutableLiveData<String?>()
    val email: LiveData<String?> = _email

    private val _profileImageUrl = MutableLiveData<String?>()
    val profileImageUrl: LiveData<String?> = _profileImageUrl

    private val _habits = MutableLiveData<List<HabitResponseDto>>()
    val habits: LiveData<List<HabitResponseDto>> = _habits

    private val _userId = MutableLiveData<Int?>()
    val userId: LiveData<Int?> = _userId

    // Loading/Error state
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage

    private val _navigateToAddHabit = MutableLiveData<Boolean>()
    val navigateToAddHabit: LiveData<Boolean> = _navigateToAddHabit

    private val _navigateToEditProfile = MutableLiveData<Boolean>()
    val navigateToEditProfile: LiveData<Boolean> = _navigateToEditProfile

    private val _navigateToLogin = MutableLiveData<Boolean>()
    val navigateToLogin: LiveData<Boolean> = _navigateToLogin

    private val _imageUpdateCacheBuster = MutableLiveData<Long>()
    val imageUpdateCacheBuster: LiveData<Long> = _imageUpdateCacheBuster

    fun loadUserProfile() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = profileRepository.getCurrentProfile()
                result.onSuccess { profile ->
                    _userId.value = profile.id
                    _username.value = profile.username
                    _email.value = profile.email
                    _profileImageUrl.value = profile.profileImageUrl
                    _isLoading.value = false
                }
                result.onFailure { error ->
                    _toastMessage.value = "Failed to load profile: ${error.message}"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _toastMessage.value = "Error: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun loadUserHabits() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = profileRepository.getHabits()
                result.onSuccess { habitList ->
                    _habits.value = habitList
                    if (habitList.isEmpty()) {
                        _toastMessage.value = "No habits yet"
                    }
                    _isLoading.value = false
                }
                result.onFailure { error ->
                    _toastMessage.value = "Failed to load habits: ${error.message}"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _toastMessage.value = "Error loading habits: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun refreshProfileImage(cacheBuster: Long) {
        _imageUpdateCacheBuster.value = cacheBuster
        viewModelScope.launch {
            try {
                val result = profileRepository.getCurrentProfile()
                result.onSuccess { profile ->
                    _profileImageUrl.value = profile.profileImageUrl
                    _toastMessage.value = "Profile picture updated!"
                }
                result.onFailure { error ->
                    _toastMessage.value = "Failed to refresh profile: ${error.message}"
                }
            } catch (e: Exception) {
                _toastMessage.value = "Error refreshing profile: ${e.message}"
            }
        }
    }

    fun performLogout() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = profileRepository.logout()
                result.onSuccess {
                    _toastMessage.value = "Logged out successfully"
                    _navigateToLogin.value = true
                    _isLoading.value = false
                }
                result.onFailure { error ->
                    _toastMessage.value = "Logout failed: ${error.message}"
                    _navigateToLogin.value = true
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _toastMessage.value = "Error during logout: ${e.message}"
                _navigateToLogin.value = true
                _isLoading.value = false
            }
        }
    }

    fun navigateToAddHabit() {
        _navigateToAddHabit.value = true
    }

    fun navigateToEditProfile() {
        _navigateToEditProfile.value = true
    }

    fun clearNavigationFlags() {
        _navigateToAddHabit.value = false
        _navigateToEditProfile.value = false
        _navigateToLogin.value = false
    }

    fun clearToastMessage() {
        _toastMessage.value = null
    }
}
