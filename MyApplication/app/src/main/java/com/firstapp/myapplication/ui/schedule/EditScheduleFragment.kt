package com.firstapp.myapplication.ui.schedule

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.firstapp.myapplication.auth.TokenManager
import com.firstapp.myapplication.databinding.FragmentEditScheduleBinding
import com.firstapp.myapplication.repository.ScheduleRepository
import kotlinx.coroutines.launch

class EditScheduleFragment : Fragment() {
    private var _binding: FragmentEditScheduleBinding? = null
    private val binding get() = _binding!!

    private lateinit var tokenManager: TokenManager
    private lateinit var scheduleRepository: ScheduleRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokenManager = TokenManager(requireContext())
        scheduleRepository = ScheduleRepository(tokenManager)

        setupClickListeners()
        loadScheduleData()
    }

    private fun setupClickListeners() {
        binding.saveBtn.setOnClickListener {
            updateSchedule()
        }

        binding.cancelBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun loadScheduleData() {
        val scheduleId = arguments?.getInt("scheduleId") ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = scheduleRepository.getScheduleById(scheduleId)
                result.onSuccess { schedule ->
                    displayScheduleData(schedule)
                }
                result.onFailure { error ->
                    showToast("Failed to load schedule: ${error.message}")
                }
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun displayScheduleData(schedule: com.firstapp.myapplication.network.dto.ScheduleResponseDto) {
        binding.apply {
            startTimeInput.setText(schedule.startTime)
            endTimeInput.setText(schedule.endTime ?: "")
            durationInput.setText(schedule.durationMinutes?.toString() ?: "")
            statusSpinner.setSelection(
                listOf("Planned", "Completed", "Skipped").indexOf(schedule.status)
            )
            notesInput.setText(schedule.notes ?: "")
        }
    }

    private fun updateSchedule() {
        val scheduleId = arguments?.getInt("scheduleId") ?: return
        val startTime = binding.startTimeInput.text.toString().trim()
        val endTime = binding.endTimeInput.text.toString().trim()
        val duration = binding.durationInput.text.toString().trim()
        val status = binding.statusSpinner.selectedItem.toString()
        val notes = binding.notesInput.text.toString().trim()

        if (startTime.isEmpty()) {
            showToast("Please enter start time")
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = scheduleRepository.updateSchedule(
                    scheduleId = scheduleId,
                    startTime = startTime,
                    endTime = endTime.ifEmpty { null },
                    durationMinutes = duration.toIntOrNull(),
                    status = status,
                    notes = notes.ifEmpty { null }
                )

                result.onSuccess {
                    showToast("Schedule updated successfully")
                    findNavController().popBackStack()
                }

                result.onFailure { error ->
                    showToast("Failed to update schedule: ${error.message}")
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
