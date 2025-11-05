package com.firstapp.myapplication.network.api

import com.firstapp.myapplication.network.dto.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {
    
    @POST("auth/local/signin")
    suspend fun signIn(@Body request: SignInRequest): Response<AuthResponseDto>
    
    @POST("auth/local/signup")
    suspend fun signUp(@Body request: SignUpRequest): Response<AuthResponseDto>
    
    @POST("auth/local/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<TokenResponse>
    
    @POST("auth/local/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<Unit>
    
    @POST("auth/reset-password-via-email")
    suspend fun resetPassword(@Body email: Map<String, String>): Response<Unit>

    @POST("auth/google/signin")
    suspend fun signInWithGoogle(@Body request: GoogleSignInRequest): Response<AuthResponseDto>
}