package com.firstapp.myapplication.auth

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.firstapp.myapplication.main.MainActivity
import com.firstapp.myapplication.MyApplication
import com.firstapp.myapplication.databinding.ActivityRegisterBinding
import com.firstapp.myapplication.repository.AuthRepository
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var authRepository: AuthRepository
    private lateinit var googleSignInManager: GoogleSignInManager
    private var selectedImageUri: Uri? = null

    companion object {
        private const val GOOGLE_SIGN_IN_REQUEST_CODE = 9001
    }

    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            Glide.with(this)
                .load(it)
                .circleCrop()
                .into(binding.ivProfileImage)
        }
    }

    // Permission launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            openImagePicker()
        } else {
            Toast.makeText(this, "Permission denied to access photos", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize repository and Google Sign-In
        val app = application as MyApplication
        authRepository = AuthRepository(app.tokenManager)
        googleSignInManager = GoogleSignInManager(this)

        setupUI()
    }

    private fun setupUI() {
        // Register button click
        binding.btnRegisterAction.setOnClickListener {
            handleRegister()
        }

        // Login tab click
        binding.btnLogin.setOnClickListener {
            // Navigate back to login activity
            finish()
        }

        // Google register button
        binding.btnGoogleRegister.setOnClickListener {
            handleGoogleSignIn()
        }

        // Profile image click listeners
        binding.ivProfileImage.setOnClickListener {
            checkPermissionAndPickImage()
        }

        binding.tvAddPhoto.setOnClickListener {
            checkPermissionAndPickImage()
        }
    }

    private fun handleRegister() {
        val username = binding.etUsername.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        if (validateInput(username, email, password, confirmPassword)) {
            performRegister(username, email, password)
        }
    }

    private fun validateInput(username: String, email: String, password: String, confirmPassword: String): Boolean {
        var isValid = true

        // Reset ALL error states first
        resetFieldErrors()

        // Username validation
        if (username.isEmpty()) {
            setFieldError(binding.tilUsername, "Felhasználónév kötelező")
            isValid = false
        } else if (username.length < 3) {
            setFieldError(binding.tilUsername, "Legalább 3 karakter szükséges")
            isValid = false
        }

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

        // Confirm password validation
        if (confirmPassword.isEmpty()) {
            setFieldError(binding.tilConfirmPassword, "Jelszó megerősítés kötelező")
            isValid = false
        } else if (password != confirmPassword) {
            setFieldError(binding.tilPassword, "A jelszavak nem egyeznek")
            setFieldError(binding.tilConfirmPassword, "A jelszavak nem egyeznek")
            isValid = false
        }

        return isValid
    }

    private fun resetFieldErrors() {
        // Reset all fields to normal state
        resetSingleField(binding.tilUsername)
        resetSingleField(binding.tilEmail)
        resetSingleField(binding.tilPassword)
        resetSingleField(binding.tilConfirmPassword)
    }

    private fun resetSingleField(textInputLayout: TextInputLayout) {
        textInputLayout.error = null
        textInputLayout.boxBackgroundColor = Color.parseColor("#2D3748")
        textInputLayout.setBoxStrokeColorStateList(
            ColorStateList.valueOf(Color.TRANSPARENT)
        )
    }

    private fun setFieldError(textInputLayout: TextInputLayout, errorMessage: String) {
        textInputLayout.error = errorMessage
        // Set red background for error state
        textInputLayout.boxBackgroundColor = Color.parseColor("#2D1B1F")
        // Set red border
        textInputLayout.setBoxStrokeColorStateList(
            ColorStateList.valueOf(Color.parseColor("#EF4444"))
        )
    }

    private fun performRegister(username: String, email: String, password: String) {
        // Show loading
        setLoadingState(true)

        lifecycleScope.launch {
            try {
                val result = authRepository.signUp(username, email, password)

                if (result.isSuccess) {
                    // Registration successful, go back to login page
                    Toast.makeText(this@RegisterActivity, "Registration successful! Please log in.", Toast.LENGTH_LONG).show()

                    // Navigate back to LoginActivity
                    finish() // This will go back to LoginActivity
                } else {
                    // Registration failed
                    val error = result.exceptionOrNull()?.message ?: "Registration failed"
                    Toast.makeText(this@RegisterActivity, error, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                setLoadingState(false)
            }
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnRegisterAction.isEnabled = !isLoading
        binding.btnLogin.isEnabled = !isLoading
        binding.btnGoogleRegister.isEnabled = !isLoading
    }

    private fun checkPermissionAndPickImage() {
        when {
            // For Android 13+ (API 33+), use READ_MEDIA_IMAGES
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                when {
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        openImagePicker()
                    }
                    else -> {
                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    }
                }
            }
            // For older Android versions, use READ_EXTERNAL_STORAGE
            else -> {
                when {
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        openImagePicker()
                    }
                    else -> {
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }
            }
        }
    }

    private fun openImagePicker() {
        imagePickerLauncher.launch("image/*")
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
                        // Send OAuth token to backend
                        performGoogleRegister(result.idToken, result.email, result.name ?: "")
                    } else {
                        Toast.makeText(this@RegisterActivity, "Failed to get user info", Toast.LENGTH_LONG).show()
                        setLoadingState(false)
                    }
                }
                is GoogleSignInResult.Error -> {
                    Toast.makeText(this@RegisterActivity, result.message, Toast.LENGTH_LONG).show()
                    setLoadingState(false)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this@RegisterActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            setLoadingState(false)
        }
    }

    private suspend fun performGoogleRegister(idToken: String, email: String, name: String) {
        try {
            // Send OAuth credentials to backend
            val result = authRepository.signInWithGoogle(idToken, email, name)

            if (result.isSuccess) {
                // Registration successful, navigate to main activity
                val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                val error = result.exceptionOrNull()?.message ?: "Google registration failed"
                Toast.makeText(this@RegisterActivity, error, Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this@RegisterActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            setLoadingState(false)
        }
    }
}