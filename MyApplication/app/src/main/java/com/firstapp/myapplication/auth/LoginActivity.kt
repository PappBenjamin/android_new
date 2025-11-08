package com.firstapp.myapplication.auth

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import com.firstapp.myapplication.main.MainActivity
import com.firstapp.myapplication.MyApplication
import com.firstapp.myapplication.databinding.ActivityLoginBinding
import com.firstapp.myapplication.repository.AuthRepository
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var authRepository: AuthRepository
    private lateinit var googleSignInManager: GoogleSignInManager

    companion object {
        private const val TAG = "LoginActivity"
    }

    // Google Sign-In activity result launcher
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "Google Sign-In result code: ${result.resultCode}")
        lifecycleScope.launch {
            handleGoogleSignInResult(result.data)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Link the layout using ViewBinding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize repository and Google Sign-In
        val app = application as MyApplication
        authRepository = AuthRepository(app.tokenManager)
        googleSignInManager = GoogleSignInManager(this)

        setupUI()
    }

    private fun setupUI() {
        // Login button click (the actual login button, not the tab)
        binding.btnLoginAction.setOnClickListener {
            handleLogin()
        }

        // Register tab click
        binding.btnRegister.setOnClickListener {
            // Navigate to register activity
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Google login button
        binding.btnGoogleLogin.setOnClickListener {
            handleGoogleSignIn()
        }
    }

    private fun handleLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (validateInput(email, password)) {
            lifecycleScope.launch {
                performLogin(email, password)
            }
        }
    }

    private fun handleGoogleSignIn() {
        Log.d(TAG, "Google Sign-In button clicked")
        setLoadingState(true)
        val signInIntent = googleSignInManager.getSignInIntent()
        googleSignInLauncher.launch(signInIntent)
    }

    private suspend fun handleGoogleSignInResult(data: Intent?) {
        Log.d(TAG, "handleGoogleSignInResult called with data: $data")
        try {
            val result = googleSignInManager.handleSignInResult(data)
            Log.d(TAG, "Google Sign-In result: $result")

            when (result) {
                is GoogleSignInResult.Success -> {
                    Log.d(TAG, "Google Sign-In success: idToken=${result.idToken != null}, email=${result.email}")
                    if (result.idToken != null && result.email != null) {
                        // Send OAuth token to backend for verification
                        performGoogleLogin(result.idToken, result.email, result.name ?: "")
                    } else {
                        Log.e(TAG, "Missing idToken or email")
                        Toast.makeText(this@LoginActivity, "Failed to get user info", Toast.LENGTH_LONG).show()
                        setLoadingState(false)
                    }
                }
                is GoogleSignInResult.Error -> {
                    Log.e(TAG, "Google Sign-In error: ${result.message}")
                    Toast.makeText(this@LoginActivity, result.message, Toast.LENGTH_LONG).show()
                    setLoadingState(false)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in handleGoogleSignInResult", e)
            Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            setLoadingState(false)
        }
    }

    private suspend fun performGoogleLogin(idToken: String, email: String, name: String) {
        Log.d(TAG, "performGoogleLogin called with email=$email")
        try {
            Log.d(TAG, "Calling authRepository.signInWithGoogle")
            val result = authRepository.signInWithGoogle(idToken, email, name)
            Log.d(TAG, "signInWithGoogle result: isSuccess=${result.isSuccess}")

            if (result.isSuccess) {
                Log.d(TAG, "Google login successful, navigating to MainActivity")
                // Login successful, navigate to main activity
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                Log.d(TAG, "startActivity called, finishing LoginActivity")
                finish()
            } else {
                // Login failed
                val error = result.exceptionOrNull()?.message ?: "Google login failed"
                Log.e(TAG, "Google login failed: $error")
                Toast.makeText(this@LoginActivity, error, Toast.LENGTH_LONG).show()
                setLoadingState(false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in performGoogleLogin", e)
            Toast.makeText(this@LoginActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            setLoadingState(false)
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true

        // Reset ALL error states first
        resetFieldErrors()

        // Email validation
        if (email.isEmpty()) {
            setFieldError(binding.tilEmail, "Email cím kötelező")
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            setFieldError(binding.tilEmail, "Érvényes email címet adj meg")
            isValid = false
        }

        // Password validation
        if (password.isEmpty()) {
            setFieldError(binding.tilPassword, "Jelszó kötelező")
            isValid = false
        } else if (password.length < 6) {
            setFieldError(binding.tilPassword, "Legalább 6 karakter szükséges")
            isValid = false
        }

        return isValid
    }

    private fun resetFieldErrors() {
        // Reset all fields to normal state
        resetSingleField(binding.tilEmail)
        resetSingleField(binding.tilPassword)
    }

    private fun resetSingleField(textInputLayout: TextInputLayout) {
        textInputLayout.error = null
        textInputLayout.boxBackgroundColor = "#2D3748".toColorInt()
        textInputLayout.setBoxStrokeColorStateList(
            ColorStateList.valueOf(Color.TRANSPARENT)
        )
    }

    private fun setFieldError(textInputLayout: TextInputLayout, errorMessage: String) {
        textInputLayout.error = errorMessage
        // Set red background for error state
        textInputLayout.boxBackgroundColor = "#2D1B1F".toColorInt()
        // Set red border
        textInputLayout.setBoxStrokeColorStateList(
            ColorStateList.valueOf("#EF4444".toColorInt())
        )
    }

    private suspend fun performLogin(email: String, password: String) {
        // Show loading
        setLoadingState(true)

        try {
            val result = authRepository.signIn(email, password)

            if (result.isSuccess) {
                // Login successful, navigate to main activity
                Log.d(TAG, "Login successful, navigating to MainActivity")
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                // Login failed - show detailed error message
                val error = result.exceptionOrNull()?.message ?: "Login failed"
                Log.e(TAG, "Login failed: $error")
                Toast.makeText(this@LoginActivity, error, Toast.LENGTH_LONG).show()
                setLoadingState(false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during login", e)
            Toast.makeText(this@LoginActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            setLoadingState(false)
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLoginAction.isEnabled = !isLoading
        binding.btnRegister.isEnabled = !isLoading
        binding.btnGoogleLogin.isEnabled = !isLoading
    }
}