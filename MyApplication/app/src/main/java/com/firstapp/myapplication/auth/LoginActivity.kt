package com.firstapp.myapplication.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.firstapp.myapplication.MainActivity
import com.firstapp.myapplication.MyApplication
import com.firstapp.myapplication.databinding.ActivityLoginBinding
import com.firstapp.myapplication.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLoginBinding
    private lateinit var authRepository: AuthRepository
    private lateinit var googleSignInManager: GoogleSignInManager

    companion object {
        private const val GOOGLE_SIGN_IN_REQUEST_CODE = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        
        // Forgot password link
        binding.tvForgotPassword.setOnClickListener {
            // Navigate to password reset activity
            startActivity(Intent(this, PasswordResetActivity::class.java))
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
        setLoadingState(true)
        val signInIntent = googleSignInManager.getSignInIntent()
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GOOGLE_SIGN_IN_REQUEST_CODE) {
            lifecycleScope.launch {
                handleGoogleSignInResult(data)
            }
        }
    }

    private suspend fun handleGoogleSignInResult(data: Intent?) {
        try {
            val result = googleSignInManager.handleSignInResult(data)

            when (result) {
                is GoogleSignInResult.Success -> {
                    if (result.idToken != null && result.email != null) {
                        // Send OAuth token to backend for verification
                        performGoogleLogin(result.idToken, result.email, result.name ?: "")
                    } else {
                        Toast.makeText(this@LoginActivity, "Failed to get user info", Toast.LENGTH_LONG).show()
                        setLoadingState(false)
                    }
                }
                is GoogleSignInResult.Error -> {
                    Toast.makeText(this@LoginActivity, result.message, Toast.LENGTH_LONG).show()
                    setLoadingState(false)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            setLoadingState(false)
        }
    }

    private suspend fun performGoogleLogin(idToken: String, email: String, name: String) {
        try {
            // Here you would typically send the idToken to your backend
            // The backend should verify the token with Google and create/update user session
            val result = authRepository.signInWithGoogle(idToken, email, name)

            if (result.isSuccess) {
                // Login successful, navigate to main activity
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                // Login failed
                val error = result.exceptionOrNull()?.message ?: "Google login failed"
                Toast.makeText(this@LoginActivity, error, Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this@LoginActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
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
    
    private fun resetSingleField(textInputLayout: com.google.android.material.textfield.TextInputLayout) {
        textInputLayout.error = null
        textInputLayout.boxBackgroundColor = android.graphics.Color.parseColor("#2D3748")
        textInputLayout.setBoxStrokeColorStateList(
            android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT)
        )
    }
    
    private fun setFieldError(textInputLayout: com.google.android.material.textfield.TextInputLayout, errorMessage: String) {
        textInputLayout.error = errorMessage
        // Set red background for error state
        textInputLayout.boxBackgroundColor = android.graphics.Color.parseColor("#2D1B1F")
        // Set red border
        textInputLayout.setBoxStrokeColorStateList(
            android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#EF4444"))
        )
    }
    
    private suspend fun performLogin(email: String, password: String) {
        // Show loading
        setLoadingState(true)
        
        try {
            val result = authRepository.signIn(email, password)

            if (result.isSuccess) {
                // Login successful, navigate to main activity
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                // Login failed
                val error = result.exceptionOrNull()?.message ?: "Login failed"
                Toast.makeText(this@LoginActivity, error, Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this@LoginActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
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