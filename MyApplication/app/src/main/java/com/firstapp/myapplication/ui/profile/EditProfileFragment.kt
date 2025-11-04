package com.firstapp.myapplication.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.firstapp.myapplication.auth.TokenManager
import com.firstapp.myapplication.databinding.FragmentEditProfileBinding
import com.firstapp.myapplication.repository.ProfileRepository
import kotlinx.coroutines.launch

class EditProfileFragment : Fragment() {
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var tokenManager: TokenManager
    private lateinit var profileRepository: ProfileRepository

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

        setupClickListeners()
        loadCurrentProfile()
    }

    private fun setupClickListeners() {
        binding.saveBtn.setOnClickListener {
            updateProfile()
        }

        binding.cancelBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun loadCurrentProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = profileRepository.getCurrentProfile()
                result.onSuccess { profile ->
                    binding.usernameInput.setText(profile.username)
                    binding.emailText.text = profile.email
                }
                result.onFailure { error ->
                    showToast("Failed to load profile: ${error.message}")
                }
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun updateProfile() {
        val newUsername = binding.usernameInput.text.toString().trim()

        if (newUsername.isEmpty()) {
            showToast("Please enter a username")
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = profileRepository.updateProfile(newUsername)
                result.onSuccess {
                    showToast("Profile updated successfully")
                    findNavController().popBackStack()
                }
                result.onFailure { error ->
                    showToast("Failed to update profile: ${error.message}")
                }
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
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

