package com.firstapp.myapplication.ui.profile

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.firstapp.myapplication.auth.TokenManager
import com.firstapp.myapplication.databinding.FragmentEditProfileBinding
import com.firstapp.myapplication.repository.ProfileRepository
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class EditProfileFragment : Fragment() {
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var tokenManager: TokenManager
    private lateinit var profileRepository: ProfileRepository
    private lateinit var sharedViewModel: SharedProfileViewModel
    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            binding.profileImageView.setImageURI(uri)
            showToast("Image selected. Click 'Save Changes' to upload.")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokenManager = TokenManager(requireContext())
        profileRepository = ProfileRepository(tokenManager)
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedProfileViewModel::class.java)

        setupClickListeners()
        loadCurrentProfile()
    }

    private fun setupClickListeners() {
        binding.changeImageBtn.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.saveBtn.setOnClickListener {
            saveProfile()
        }

        binding.cancelBtn.setOnClickListener {
            try {
                findNavController().popBackStack()
            } catch (_: Exception) {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun loadCurrentProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = profileRepository.getCurrentProfile()
                result.onSuccess { profile ->
                    binding.usernameInput.setText(profile.username)
                    binding.emailText.text = profile.email

                    // Load profile image
                    if (!profile.profileImageUrl.isNullOrEmpty()) {
                        Glide.with(requireContext())
                            .load(profile.profileImageUrl)
                            .centerCrop()
                            .into(binding.profileImageView)
                    }
                }
                result.onFailure { error ->
                    showToast("Failed to load profile: ${error.message}")
                }
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun saveProfile() {
        val newUsername = binding.usernameInput.text.toString().trim()

        if (newUsername.isEmpty()) {
            showToast("Please enter a username")
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // First, update the username
                val updateResult = profileRepository.updateProfile(newUsername)
                updateResult.onSuccess {
                    showToast("Username updated successfully")

                    // Then upload image if selected
                    if (selectedImageUri != null) {
                        uploadProfileImage()
                    } else {
                        findNavController().popBackStack()
                    }
                }
                updateResult.onFailure { error ->
                    showToast("Failed to update profile: ${error.message}")
                }
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun uploadProfileImage() {
        if (selectedImageUri == null) {
            showToast("No image selected")
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val inputStream = requireContext().contentResolver.openInputStream(selectedImageUri!!)
                val tempFile = File.createTempFile("profile", ".jpg", requireContext().cacheDir)
                tempFile.outputStream().use { fileOut ->
                    inputStream?.copyTo(fileOut)
                }

                val requestFile = tempFile.asRequestBody("image/jpeg".toMediaType())
                val body = MultipartBody.Part.createFormData("profileImage", tempFile.name, requestFile)

                val result = profileRepository.uploadProfileImage(body)
                result.onSuccess {
                    showToast("Profile image uploaded successfully")
                    tempFile.delete()

                    // Notify ProfileFragment using ViewModel with cache-buster timestamp
                    val cacheBuster = System.currentTimeMillis()
                    sharedViewModel.notifyImageUpdated(cacheBuster)

                    findNavController().popBackStack()
                }
                result.onFailure { error ->
                    showToast("Failed to upload image: ${error.message}")
                    tempFile.delete()
                }
            } catch (e: Exception) {
                showToast("Error uploading image: ${e.message}")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
