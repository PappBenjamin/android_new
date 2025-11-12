package com.firstapp.myapplication.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.firstapp.myapplication.R
import com.firstapp.myapplication.auth.LoginActivity
import com.firstapp.myapplication.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProfileViewModel
    private lateinit var habitAdapter: HabitAdapter
    private lateinit var sharedViewModel: SharedProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this, ProfileViewModelFactory(requireContext()))
            .get(ProfileViewModel::class.java)
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedProfileViewModel::class.java)

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()

        // Observe ViewModel for profile image updates
        sharedViewModel.profileImageUpdated.observe(viewLifecycleOwner) { cacheBuster ->
            if (cacheBuster != null && cacheBuster > 0) {
                clearGlideCaches()
                viewModel.refreshProfileImage(cacheBuster)
            }
        }

        viewModel.loadUserProfile()
        viewModel.loadUserHabits()
    }

    private fun setupRecyclerView() {
        habitAdapter = HabitAdapter { habit ->
            showToast("Viewing habit: ${habit.name}")
        }

        binding.habitsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = habitAdapter
        }
    }

    private fun setupClickListeners() {
        binding.addHabitBtn.setOnClickListener {
            viewModel.navigateToAddHabit()
        }

        binding.editProfileBtn.setOnClickListener {
            viewModel.navigateToEditProfile()
        }

        binding.logoutBtn.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun observeViewModel() {
        // Observe username
        viewModel.username.observe(viewLifecycleOwner) { username ->
            binding.nameText.text = username
        }

        // Observe email
        viewModel.email.observe(viewLifecycleOwner) { email ->
            binding.emailText.text = email
        }

        // Observe profile image URL
        viewModel.profileImageUrl.observe(viewLifecycleOwner) { imageUrl ->
            if (!imageUrl.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(imageUrl)
                    .centerCrop()
                    .into(binding.profileImage)
            }
        }

        // Observe habits
        viewModel.habits.observe(viewLifecycleOwner) { habits ->
            habitAdapter.updateHabits(habits)
        }

        // Observe image update cache buster
        viewModel.imageUpdateCacheBuster.observe(viewLifecycleOwner) { cacheBuster ->
            if (cacheBuster != null && cacheBuster > 0) {
                Glide.with(requireContext())
                    .load(viewModel.profileImageUrl.value)
                    .centerCrop()
                    .signature(ObjectKey(cacheBuster))
                    .into(binding.profileImage)
            }
        }

        // Observe toast messages
        viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                showToast(message)
                viewModel.clearToastMessage()
            }
        }

        // Observe navigation to Add Habit
        viewModel.navigateToAddHabit.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                try {
                    findNavController().navigate(R.id.addHabitFragment)
                } catch (e: Exception) {
                    showToast("Navigation to Add Habit failed: ${e.message}")
                }
                viewModel.clearNavigationFlags()
            }
        }

        // Observe navigation to Edit Profile
        viewModel.navigateToEditProfile.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                try {
                    findNavController().navigate(R.id.editProfileFragment)
                } catch (e: Exception) {
                    showToast("Navigation to edit profile failed: ${e.message}")
                }
                viewModel.clearNavigationFlags()
            }
        }

        // Observe navigation to Login
        viewModel.navigateToLogin.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                navigateToLogin()
                viewModel.clearNavigationFlags()
            }
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.addHabitBtn.isEnabled = !isLoading
            binding.editProfileBtn.isEnabled = !isLoading
            binding.logoutBtn.isEnabled = !isLoading
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.performLogout()
            }
            .setNegativeButton("No", null)
            .show()
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

    private fun clearGlideCaches() {
        try {
            Glide.get(requireContext()).clearMemory()
            Thread {
                try {
                    Glide.get(requireContext()).clearDiskCache()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
