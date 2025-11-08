package com.firstapp.myapplication

import android.app.Application
import com.firstapp.myapplication.auth.TokenManager
import com.firstapp.myapplication.network.ApiClient

class MyApplication : Application() {
    
    lateinit var tokenManager: TokenManager
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize TokenManager
        tokenManager = TokenManager(this)
        
        // Initialize API Client
        ApiClient.initialize(tokenManager)
    }
}