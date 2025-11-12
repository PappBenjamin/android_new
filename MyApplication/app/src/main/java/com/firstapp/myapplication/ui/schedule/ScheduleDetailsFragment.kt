package com.firstapp.myapplication.ui.schedule

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
import com.firstapp.myapplication.R
import com.firstapp.myapplication.databinding.FragmentScheduleDetailsBinding

class ScheduleDetailsFragment : Fragment() {
    private var _binding: FragmentScheduleDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ScheduleDetailsViewModel
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

        viewModel = ViewModelProvider(this, ScheduleDetailsViewModelFactory(requireContext()))
            .get(ScheduleDetailsViewModel::class.java)
        scheduleId = arguments?.getInt("scheduleId") ?: 0

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        viewModel.loadScheduleDetails(scheduleId)
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
            viewModel.navigateToEditSchedule()
        }

        binding.addProgressBtn.setOnClickListener {
            viewModel.navigateToAddProgress()
        }

        binding.deleteBtn.setOnClickListener {
            showDeleteConfirmation()
        }

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        // Status change on click
        binding.statusText.setOnClickListener {
            showStatusChangeDialog()
        }
    }

    private fun observeViewModel() {
        // Observe schedule details
        viewModel.scheduleDetails.observe(viewLifecycleOwner) { schedule ->
            if (schedule != null) {
                displayScheduleDetails(schedule)
            }
        }

        // Observe toast messages
        viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                showToast(message)
                viewModel.clearToastMessage()
            }
        }

        // Observe navigation to Edit Schedule
        viewModel.navigateToEditSchedule.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                val bundle = Bundle().apply {
                    putInt("scheduleId", scheduleId)
                }
                try {
                    findNavController().navigate(R.id.editScheduleFragment, bundle)
                } catch (e: Exception) {
                    showToast("Navigation to edit schedule failed: ${e.message}")
                }
                viewModel.clearNavigationFlags()
            }
        }

        // Observe navigation to Add Progress
        viewModel.navigateToAddProgress.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                val bundle = Bundle().apply {
                    putInt("scheduleId", scheduleId)
                }
                try {
                    findNavController().navigate(R.id.addProgressFragment, bundle)
                } catch (e: Exception) {
                    showToast("Navigation to add progress failed: ${e.message}")
                }
                viewModel.clearNavigationFlags()
            }
        }

        // Observe navigation back
        viewModel.navigateBack.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                findNavController().popBackStack()
                viewModel.clearNavigationFlags()
            }
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.editBtn.isEnabled = !isLoading
            binding.addProgressBtn.isEnabled = !isLoading
            binding.deleteBtn.isEnabled = !isLoading
        }
    }

    private fun displayScheduleDetails(schedule: com.firstapp.myapplication.network.dto.ScheduleResponseDto) {
        binding.apply {
            habitNameText.text = schedule.habit?.name ?: "Habit"
            habitDescriptionText.text = schedule.habit?.description ?: "No description"
            habitGoalText.text = schedule.habit?.goal ?: "No goal"

            scheduleDateText.text = viewModel.formatDate(schedule.date)
            scheduleTimeText.text = schedule.startTime
            durationText.text = "${schedule.durationMinutes ?: 0} minutes"
            statusText.text = schedule.status
            notesText.text = schedule.notes ?: "No notes"

            // Set status color
            statusText.setTextColor(viewModel.getStatusColor(schedule.status))

            // Display progress items
            schedule.progress?.let { progressAdapter.submitList(it) }
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Schedule")
            .setMessage("Are you sure you want to delete this schedule?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.deleteSchedule(scheduleId)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun showStatusChangeDialog() {
        val statuses = arrayOf("Planned", "Completed", "Skipped")
        val currentStatus = binding.statusText.text.toString()
        val currentIndex = statuses.indexOf(currentStatus)

        AlertDialog.Builder(requireContext())
            .setTitle("Change Status")
            .setSingleChoiceItems(statuses, currentIndex) { dialog, which ->
                val newStatus = statuses[which]
                viewModel.updateScheduleStatus(scheduleId, newStatus)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
