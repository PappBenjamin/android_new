package com.firstapp.myapplication.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.firstapp.myapplication.R
import com.firstapp.myapplication.auth.TokenManager
import com.firstapp.myapplication.auth.LoginActivity
import com.firstapp.myapplication.databinding.FragmentProfileBinding
import com.firstapp.myapplication.repository.ProfileRepository
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var tokenManager: TokenManager
    private lateinit var profileRepository: ProfileRepository
    private lateinit var habitAdapter: HabitAdapter

    private var currentUserId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokenManager = TokenManager(requireContext())
        profileRepository = ProfileRepository(tokenManager)

        setupRecyclerView()
        setupClickListeners()
        loadUserProfile()
        loadUserHabits()
    }

    private fun setupRecyclerView() {
        habitAdapter = HabitAdapter { habit ->
            // Navigate to Add Habit screen with pre-filled habit
            showToast("Viewing habit: ${habit.name}")
            // TODO: Navigate to HabitDetailsFragment if created
        }

        binding.habitsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = habitAdapter
        }
    }

    private fun setupClickListeners() {
        binding.addHabitBtn.setOnClickListener {
            navigateToAddHabit()
        }

        binding.editProfileBtn.setOnClickListener {
            navigateToEditProfile()
        }

        binding.logoutBtn.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun loadUserProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = profileRepository.getCurrentProfile()
                result.onSuccess { profile ->
                    currentUserId = profile.id
                    binding.nameText.text = profile.username
                    binding.emailText.text = profile.email

                    // Load profile image if available
                    if (!profile.profileImageUrl.isNullOrEmpty()) {
                        Glide.with(requireContext())
                            .load(profile.profileImageUrl)
                            .centerCrop()
                            .into(binding.profileImage)
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

    private fun loadUserHabits() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = profileRepository.getHabits()
                result.onSuccess { habits ->
                    if (habits.isEmpty()) {
                        showToast("No habits yet")
                    } else {
                        habitAdapter.updateHabits(habits)
                    }
                }
                result.onFailure { error ->
                    showToast("Failed to load habits: ${error.message}")
                }
            } catch (e: Exception) {
                showToast("Error loading habits: ${e.message}")
            }
        }
    }

    private fun navigateToAddHabit() {
        try {
            findNavController().navigate(R.id.addHabitFragment)
        } catch (e: Exception) {
            showToast("Navigation to Add Habit failed: ${e.message}")
        }
    }

    private fun navigateToEditProfile() {
        try {
            findNavController().navigate(R.id.editProfileFragment)
        } catch (e: Exception) {
            showToast("Navigation to edit profile failed: ${e.message}")
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                performLogout()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun performLogout() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = profileRepository.logout()
                result.onSuccess {
                    showToast("Logged out successfully")
                    navigateToLogin()
                }
                result.onFailure { error ->
                    showToast("Logout failed: ${error.message}")
                    navigateToLogin()
                }
            } catch (e: Exception) {
                showToast("Error during logout: ${e.message}")
                navigateToLogin()
            }
        }
    }

    private fun navigateToLogin() {
        try {
            val intent = android.content.Intent(requireContext(), LoginActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        } catch (e: Exception) {
            showToast("Navigation to login failed: ${e.message}")
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
