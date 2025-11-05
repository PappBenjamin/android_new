package com.firstapp.myapplication.splash

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.firstapp.myapplication.MyApplication
import com.firstapp.myapplication.R
import com.firstapp.myapplication.auth.LoginActivity
import com.firstapp.myapplication.main.MainActivity
import com.firstapp.myapplication.auth.TokenManager
import com.firstapp.myapplication.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    companion object {
        const val TAG = "SplashActivity"
        const val SPLASH_DURATION = 2000L // 2 seconds
    }

    private lateinit var tokenManager: TokenManager
    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Initialize dependencies from Application class
        val app = application as MyApplication
        tokenManager = app.tokenManager
        authRepository = AuthRepository(tokenManager)

        // Start animations
        startAnimations()

        // Start splash flow
        startSplashFlow()
    }

    private fun startAnimations() {
        val logo = findViewById<ImageView>(R.id.imageViewLogo)
        val appName = findViewById<TextView>(R.id.textViewAppName)
        val description = findViewById<TextView>(R.id.textViewDescription)

        val animation = AnimationUtils.loadAnimation(this, R.anim.splash_fade_in)

        logo.startAnimation(animation)
        appName.startAnimation(animation)
        description.startAnimation(animation)
    }

    private fun startSplashFlow() {
        lifecycleScope.launch {
            // Show splash screen for minimum duration
            delay(SPLASH_DURATION)

            // Check authentication status
            checkAuthenticationAndNavigate()
        }
    }

    private suspend fun checkAuthenticationAndNavigate() {
        try {
            Log.d(TAG, "Checking authentication status...")
            val isAuthenticated = authRepository.checkAuthenticationStatus()

            if (isAuthenticated) {
                Log.d(TAG, "User is authenticated, navigating to MainActivity")
                navigateToMain()
            } else {
                Log.d(TAG, "User is not authenticated, navigating to LoginActivity")
                navigateToLogin()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during authentication check", e)
            // On error, navigate to login screen
            navigateToLogin()
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this@SplashActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
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