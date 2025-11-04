package com.firstapp.myapplication.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.firstapp.myapplication.MyApplication
import com.firstapp.myapplication.databinding.ActivityPasswordResetBinding
import com.firstapp.myapplication.repository.AuthRepository
import kotlinx.coroutines.launch

class PasswordResetActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityPasswordResetBinding
    private lateinit var authRepository: AuthRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordResetBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize repository
        val app = application as MyApplication
        authRepository = AuthRepository(app.tokenManager)
        
        setupUI()
    }
    
    private fun setupUI() {
        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }
        
        // Send reset email button
        binding.btnSendReset.setOnClickListener {
            handlePasswordReset()
        }
        
        // Login tab click (navigate back to login)
        binding.btnLogin.setOnClickListener {
            finish()
        }
        
        // Register tab click
        binding.btnRegister.setOnClickListener {
            // Navigate to register activity
            finish()
            startActivity(android.content.Intent(this, RegisterActivity::class.java))
        }
    }
    
    private fun handlePasswordReset() {
        val email = binding.etEmail.text.toString().trim()
        
        if (validateEmail(email)) {
            sendPasswordResetEmail(email)
        }
    }
    
    private fun validateEmail(email: String): Boolean {
        // Reset error state first
        resetFieldError()
        
        return when {
            email.isEmpty() -> {
                setFieldError("Email cím kötelező")
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                setFieldError("Érvényes email címet adj meg")
                false
            }
            else -> true
        }
    }
    
    private fun resetFieldError() {
        binding.tilEmail.error = null
        binding.tilEmail.boxBackgroundColor = android.graphics.Color.parseColor("#1E293B")
        binding.tilEmail.setBoxStrokeColorStateList(
            android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT)
        )
    }
    
    private fun setFieldError(errorMessage: String) {
        binding.tilEmail.error = errorMessage
        // Set red background for error state
        binding.tilEmail.boxBackgroundColor = android.graphics.Color.parseColor("#2D1B1F")
        // Set red border
        binding.tilEmail.setBoxStrokeColorStateList(
            android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#EF4444"))
        )
    }
    
    private fun sendPasswordResetEmail(email: String) {
        // Show loading
        setLoadingState(true)
        
        lifecycleScope.launch {
            try {
                val result = authRepository.resetPassword(email)
                
                if (result.isSuccess) {
                    // Show success message
                    showSuccessState()
                    Toast.makeText(
                        this@PasswordResetActivity,
                        "Password reset email sent! Please check your inbox.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    // Show error message
                    val error = result.exceptionOrNull()?.message ?: "Failed to send reset email"
                    Toast.makeText(this@PasswordResetActivity, error, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@PasswordResetActivity,
                    "Network error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                setLoadingState(false)
            }
        }
    }
    
    private fun setLoadingState(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSendReset.isEnabled = !isLoading
        binding.etEmail.isEnabled = !isLoading
    }
    
    private fun showSuccessState() {
        // Show success icon and message
        binding.ivSuccessIcon.visibility = View.VISIBLE
        binding.tvSuccessMessage.visibility = View.VISIBLE
        binding.tvSuccessDescription.visibility = View.VISIBLE
        
        // Hide the form
        binding.cardEmailForm.visibility = View.GONE
    }
}