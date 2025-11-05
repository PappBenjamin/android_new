package com.firstapp.myapplication.repository

import android.util.Log
import com.firstapp.myapplication.auth.TokenManager
import com.firstapp.myapplication.network.ApiClient
import com.firstapp.myapplication.network.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class AuthRepository(private val tokenManager: TokenManager) {
    
    companion object {
        private const val TAG = "AuthRepository"
    }

    private fun extractErrorMessage(errorBody: String?): String {
        return try {
            if (errorBody != null) {
                val json = JSONObject(errorBody)
                json.optString("message", "An error occurred")
            }
            else
            {
                "An error occurred"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing error response", e)
            "An error occurred"
        }
    }

    suspend fun checkAuthenticationStatus(): Boolean {
        return withContext(Dispatchers.IO) {
            if (!tokenManager.isLoggedIn()) {
                return@withContext false
            }
            
            // Try to refresh token to verify it's still valid
            val refreshToken = tokenManager.getRefreshToken()
            if (refreshToken != null) {
                try {
                    val response = ApiClient.authService.refreshToken(
                        RefreshTokenRequest(refreshToken)
                    )
                    
                    if (response.isSuccessful) {
                        val tokenResponse = response.body()
                        if (tokenResponse != null) {
                            tokenManager.saveTokens(
                                tokenResponse.accessToken,
                                tokenResponse.refreshToken
                            )
                            return@withContext true
                        }
                    }
                } catch (e: Exception) {
                    // Token refresh failed, clear tokens
                    tokenManager.clearTokens()
                    return@withContext false
                }
            }
            
            // If we get here, authentication failed
            tokenManager.clearTokens()
            false
        }
    }
    
    suspend fun signIn(email: String, password: String): Result<AuthResponseDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.authService.signIn(
                    SignInRequest(email, password)
                )
                
                if (response.isSuccessful)
                {
                    val authResponse = response.body()
                    if (authResponse != null)
                    {
                        // Save tokens and user info
                        tokenManager.saveTokens(
                            authResponse.tokens.accessToken,
                            authResponse.tokens.refreshToken
                        )
                        tokenManager.saveUserInfo(
                            authResponse.user.id.toString(),
                            authResponse.user.profile.username,
                            authResponse.user.email
                        )
                        Result.success(authResponse)
                    }
                    else
                    {
                        Result.failure(Exception("Empty response body"))
                    }
                }
                else
                {
                    val errorMessage = extractErrorMessage(response.errorBody()?.string())
                    Log.e(TAG, "Sign in failed: HTTP ${response.code()} - $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            }
            catch (e: Exception)
            {
                Log.e(TAG, "Exception during sign in", e)
                Result.failure(e)
            }
        }
    }
    
    suspend fun signUp(username: String, email: String, password: String): Result<AuthResponseDto> {
        return withContext(Dispatchers.IO) {
            try {
                val request = SignUpRequest(username, email, password)
                val response = ApiClient.authService.signUp(request)
                
                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse != null) {
                        // Save tokens and user info
                        tokenManager.saveTokens(
                            authResponse.tokens.accessToken,
                            authResponse.tokens.refreshToken
                        )
                        tokenManager.saveUserInfo(
                            authResponse.user.id.toString(),
                            authResponse.user.profile.username,
                            authResponse.user.email
                        )
                        Result.success(authResponse)
                    } else {
                        Result.failure(Exception("Empty response body"))
                    }
                } else {
                    val errorMessage = extractErrorMessage(response.errorBody()?.string())
                    Log.e(TAG, "Sign up failed: HTTP ${response.code()} - $errorMessage")

                    // Handle specific error cases
                    val userFriendlyMessage = when {
                        errorMessage.contains("duplicate", ignoreCase = true) ->
                            "This email is already registered. Please use a different email or try logging in."
                        else -> errorMessage
                    }

                    Result.failure(Exception(userFriendlyMessage))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during sign up", e)
                Result.failure(e)
            }
        }
    }
    
    suspend fun logout(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val accessToken = tokenManager.getAccessToken()
                if (accessToken != null) {
                    ApiClient.authService.logout("Bearer $accessToken")
                }
                tokenManager.clearTokens()
                Result.success(Unit)
            } catch (e: Exception) {
                // Clear tokens even if logout API call fails
                tokenManager.clearTokens()
                Result.success(Unit)
            }
        }
    }
    
    suspend fun resetPassword(email: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.authService.resetPassword(
                    mapOf("email" to email)
                )
                
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Password reset failed: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun signInWithGoogle(idToken: String, email: String, name: String): Result<AuthResponseDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.authService.signInWithGoogle(
                    GoogleSignInRequest(idToken, email, name)
                )

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse != null) {
                        // Save tokens and user info
                        tokenManager.saveTokens(
                            authResponse.tokens.accessToken,
                            authResponse.tokens.refreshToken
                        )
                        tokenManager.saveUserInfo(
                            authResponse.user.id.toString(),
                            authResponse.user.profile.username,
                            authResponse.user.email
                        )
                        Result.success(authResponse)
                    } else {
                        Result.failure(Exception("Empty response body"))
                    }
                } else {
                    val errorMessage = extractErrorMessage(response.errorBody()?.string())
                    Log.e(TAG, "Google sign in failed: HTTP ${response.code()} - $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during Google sign in", e)
                Result.failure(e)
            }
        }
    }
}