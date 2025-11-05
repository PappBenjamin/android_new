package com.firstapp.myapplication.network.dto

data class AuthResponseDto(
    val message: String,
    val user: User,
    val tokens: Tokens
)

data class Tokens(
    val accessToken: String,
    val refreshToken: String
)

data class User(
    val id: Int,
    val email: String,
    val auth_provider: String,
    val profile: Profile
)

data class Profile(
    val id: Int,
    val email: String,
    val username: String,
    val description: String?,
    val profileImageUrl: String?,
    val coverImageUrl: String?,
    val fcmToken: String,
    val preferences: String?,
    val created_at: String,
    val updated_at: String
)

data class SignInRequest(
    val email: String,
    val password: String
)

data class SignUpRequest(
    val username: String,
    val email: String,
    val password: String
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class GoogleSignInRequest(
    val idToken: String,
    val email: String,
    val name: String
)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String
)