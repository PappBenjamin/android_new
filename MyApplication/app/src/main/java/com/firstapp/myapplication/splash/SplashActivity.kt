package com.firstapp.myapplication.splash

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.firstapp.myapplication.MyApplication
import com.firstapp.myapplication.R
import com.firstapp.myapplication.auth.LoginActivity
import com.firstapp.myapplication.main.MainActivity
import com.firstapp.myapplication.auth.TokenManager
import com.firstapp.myapplication.repository.AuthRepository
import com.firstapp.myapplication.databinding.ActivitySplashBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    companion object {
        const val TAG = "SplashActivity"
        const val SPLASH_DURATION = 2000L // 2 seconds
    }

    private lateinit var binding: ActivitySplashBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize dependencies from Application class
        val app = application as MyApplication
        tokenManager = app.tokenManager
        authRepository = AuthRepository(tokenManager)

        // Start splash flow
        startSplashFlow()
    }

    private fun startSplashFlow() {
        lifecycleScope.launch {
            // Show splash screen for minimum duration
            delay(SPLASH_DURATION)

            // navigate to login
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this@SplashActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: SplashActivity destroyed.")
    }
}