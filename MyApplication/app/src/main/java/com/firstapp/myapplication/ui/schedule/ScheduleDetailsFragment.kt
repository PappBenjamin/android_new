package com.firstapp.myapplication.ui.schedule

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.firstapp.myapplication.auth.TokenManager
import com.firstapp.myapplication.databinding.FragmentScheduleDetailsBinding
import com.firstapp.myapplication.repository.ScheduleRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ScheduleDetailsFragment : Fragment() {
    private var _binding: FragmentScheduleDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var tokenManager: TokenManager
    private lateinit var scheduleRepository: ScheduleRepository
    private lateinit var progressAdapter: ProgressAdapter
    private var scheduleId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokenManager = TokenManager(requireContext())
        scheduleRepository = ScheduleRepository(tokenManager)
        scheduleId = arguments?.getInt("scheduleId") ?: 0

        setupRecyclerView()
        setupClickListeners()
        loadScheduleDetails()
    }

    private fun setupRecyclerView() {
        progressAdapter = ProgressAdapter()
        binding.progressRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = progressAdapter
        }
    }

    private fun setupClickListeners() {
        binding.editBtn.setOnClickListener {
            editSchedule()
        }

        binding.addProgressBtn.setOnClickListener {
            navigateToAddProgress()
        }

        binding.deleteBtn.setOnClickListener {
            showDeleteConfirmation()
        }

    }

    private fun editSchedule() {
        val bundle = android.os.Bundle().apply {
            putInt("scheduleId", scheduleId)
        }
        try {
            findNavController().navigate(com.firstapp.myapplication.R.id.editScheduleFragment, bundle)
        } catch (e: Exception) {
            showToast("Navigation to edit schedule failed: ${e.message}")
        }
    }

    private fun navigateToAddProgress() {
        val bundle = android.os.Bundle().apply {
            putInt("scheduleId", scheduleId)
        }
        try {
            findNavController().navigate(com.firstapp.myapplication.R.id.addProgressFragment, bundle)
        } catch (e: Exception) {
            showToast("Add Progress - Coming soon")
        }
    }

    private fun loadScheduleDetails() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = scheduleRepository.getScheduleById(scheduleId)
                result.onSuccess { schedule ->
                    displayScheduleDetails(schedule)
                }
                result.onFailure { error ->
                    showToast("Failed to load schedule: ${error.message}")
                }
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun displayScheduleDetails(schedule: com.firstapp.myapplication.network.models.ScheduleResponseDto) {
        binding.apply {
            habitNameText.text = schedule.habit?.name ?: "Habit"
            habitDescriptionText.text = schedule.habit?.description ?: "No description"
            habitGoalText.text = schedule.habit?.goal ?: "No goal"

            try {
                val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(schedule.date) ?: Date()
                scheduleDateText.text = dateFormat.format(date)
            } catch (e: Exception) {
                scheduleDateText.text = schedule.date
            }

            scheduleTimeText.text = schedule.startTime
            durationText.text = "${schedule.durationMinutes ?: 0} minutes"
            statusText.text = schedule.status
            notesText.text = schedule.notes ?: "No notes"

            // Set status color
            when (schedule.status) {
                "Completed" -> statusText.setTextColor(android.graphics.Color.GREEN)
                "Planned" -> statusText.setTextColor(android.graphics.Color.YELLOW)
                "Skipped" -> statusText.setTextColor(android.graphics.Color.RED)
            }

            // Display progress items
            schedule.progress?.let { progressAdapter.submitList(it) }
        }
    }

    private fun showDeleteConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Schedule")
            .setMessage("Are you sure you want to delete this schedule?")
            .setPositiveButton("Yes") { _, _ -> deleteSchedule() }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteSchedule() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = scheduleRepository.deleteSchedule(scheduleId)
                result.onSuccess {
                    showToast("Schedule deleted successfully")
                    findNavController().popBackStack()
                }
                result.onFailure { error ->
                    showToast("Failed to delete schedule: ${error.message}")
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
