package com.firstapp.myapplication.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenManager(context: Context) {
    
    companion object {
        private const val PREFS_FILE_NAME = "auth_prefs"
        private const val ACCESS_TOKEN_KEY = "access_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
        private const val USER_ID_KEY = "user_id"
        private const val USERNAME_KEY = "username"
        private const val EMAIL_KEY = "email"
    }
    
    // Create or retrieve the MasterKey for encryption (this is the encryption key used by EncryptedSharedPreferences)
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    // encrypts SharedPreferences using the MasterKey
    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // Save access and refresh tokens securely
    fun saveTokens(accessToken: String, refreshToken: String) {
        sharedPreferences.edit()
            .putString(ACCESS_TOKEN_KEY, accessToken)
            .putString(REFRESH_TOKEN_KEY, refreshToken)
            .apply()
    }

    fun getAccessToken(): String? {
        return sharedPreferences.getString(ACCESS_TOKEN_KEY, null)
    }
    
    fun getRefreshToken(): String? {
        return sharedPreferences.getString(REFRESH_TOKEN_KEY, null)
    }
    
    fun saveUserInfo(userId: String, username: String, email: String) {
        sharedPreferences.edit()
            .putString(USER_ID_KEY, userId)
            .putString(USERNAME_KEY, username)
            .putString(EMAIL_KEY, email)
            .apply()
    }
    
    fun getUserId(): String? {
        return sharedPreferences.getString(USER_ID_KEY, null)
    }
    
    fun getUsername(): String? {
        return sharedPreferences.getString(USERNAME_KEY, null)
    }
    
    fun getEmail(): String? {
        return sharedPreferences.getString(EMAIL_KEY, null)
    }
    
    fun clearTokens() {
        sharedPreferences.edit()
            .remove(ACCESS_TOKEN_KEY)
            .remove(REFRESH_TOKEN_KEY)
            .remove(USER_ID_KEY)
            .remove(USERNAME_KEY)
            .remove(EMAIL_KEY)
            .apply()
    }
    
    fun isLoggedIn(): Boolean {
        return getAccessToken() != null && getRefreshToken() != null
    }
}