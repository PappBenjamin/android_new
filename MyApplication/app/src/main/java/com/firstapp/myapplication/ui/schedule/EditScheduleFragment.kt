package com.firstapp.myapplication.ui.schedule

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import android.text.Editable
import android.text.TextWatcher
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.firstapp.myapplication.auth.TokenManager
import com.firstapp.myapplication.databinding.FragmentEditScheduleBinding
import com.firstapp.myapplication.repository.ScheduleRepository
import java.time.LocalTime
import java.time.Duration
import kotlinx.coroutines.launch

class EditScheduleFragment : Fragment() {
    private var _binding: FragmentEditScheduleBinding? = null
    private val binding get() = _binding!!

    private lateinit var tokenManager: TokenManager
    private lateinit var scheduleRepository: ScheduleRepository
    private var currentSchedule: com.firstapp.myapplication.network.dto.ScheduleResponseDto? = null

    // setup view
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Initialize components and load existing schedule data
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokenManager = TokenManager(requireContext())
        scheduleRepository = ScheduleRepository(tokenManager)

        setupSpinner()
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

        binding.startTimeInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) showCalculatedDuration()
        }

        binding.endTimeInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) showCalculatedDuration()
        }

        // Setup time input formatting (HHMM to HH:mm)
        binding.startTimeInput.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true

                val text = s.toString().replace(":", "").take(4)
                if (text.length == 4) {
                    val formatted = "${text.substring(0, 2)}:${text.substring(2, 4)}"
                    binding.startTimeInput.setText(formatted)
                    binding.startTimeInput.setSelection(formatted.length)
                }

                isFormatting = false
            }
        })

        binding.endTimeInput.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true

                val text = s.toString().replace(":", "").take(4)
                if (text.length == 4) {
                    val formatted = "${text.substring(0, 2)}:${text.substring(2, 4)}"
                    binding.endTimeInput.setText(formatted)
                    binding.endTimeInput.setSelection(formatted.length)
                }

                isFormatting = false
            }
        })
    }

    private fun setupSpinner() {
        val statusOptions = arrayOf("Planned", "Completed", "Skipped")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, statusOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.statusSpinner.adapter = adapter
    }

    private fun loadScheduleData() {
        val scheduleId = arguments?.getInt("scheduleId") ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = scheduleRepository.getScheduleById(scheduleId)
                result.onSuccess { schedule ->
                    currentSchedule = schedule // Store the current schedule
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
            startTimeInput.setText(parseTimeFromIso(schedule.startTime) ?: "")
            endTimeInput.setText(parseTimeFromIso(schedule.endTime) ?: "")
            statusSpinner.setSelection(
                listOf("Planned", "Completed", "Skipped").indexOf(schedule.status)
            )
            notesInput.setText(schedule.notes ?: "")
            participantsInput.setText(schedule.participantIds?.joinToString(", ") ?: "")
        }
    }

    private fun updateSchedule() {
        val scheduleId = arguments?.getInt("scheduleId") ?: return
        val startTime = binding.startTimeInput.text.toString().trim()
        val endTime = binding.endTimeInput.text.toString().trim()
        val status = binding.statusSpinner.selectedItem.toString()
        val notes = binding.notesInput.text.toString().trim()
        val participants = binding.participantsInput.text.toString().trim()
        val participantIds = if (participants.isNotEmpty()) participants.split(",").mapNotNull { it.trim().toIntOrNull() } else null
        val durationMinutes = calculateDuration(startTime, endTime)
        val date = currentSchedule?.date
        val datePart = date?.split("T")?.get(0) ?: ""

        if (startTime.isEmpty() || endTime.isEmpty()) {
            showToast("Please enter start time and end time")
            return
        }

        if (endTime.isNotEmpty() && durationMinutes == null) {
            showToast("Invalid time format. Please use HH:mm")
            return
        }

//        showToast("Calculated duration: $durationMinutes minutes")

        val startIso = if (startTime.isNotEmpty() && datePart.isNotEmpty()) "${datePart}T${startTime}:00.000Z" else startTime
        val endIso = if (endTime.isNotEmpty() && datePart.isNotEmpty()) "${datePart}T${endTime}:00.000Z" else endTime.ifEmpty { null }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = scheduleRepository.updateSchedule(
                    scheduleId = scheduleId,
                    startTime = startIso,
                    endTime = endIso,
                    durationMinutes = durationMinutes,
                    status = status,
                    date = date,
                    notes = notes.ifEmpty { null },
                    participantIds = participantIds
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

    private fun calculateDuration(start: String, end: String): Int? {
        if (end.isEmpty()) return null
        return try {
            val startTime = LocalTime.parse(start)
            val endTime = LocalTime.parse(end)
            Duration.between(startTime, endTime).toMinutes().toInt()
        } catch (e: Exception) {
            null
        }
    }

    private fun showCalculatedDuration() {
        val startTime = binding.startTimeInput.text.toString().trim()
        val endTime = binding.endTimeInput.text.toString().trim()

        if (startTime.isEmpty() || endTime.isEmpty()) {
            return
        }

        val duration = calculateDuration(startTime, endTime)

        if (duration != null) {
            showToast("Current duration: $duration minutes")
        }
        else
        {
            showToast("Invalid time format")
        }
    }

    private fun parseTimeFromIso(iso: String?): String? {
        if (iso.isNullOrEmpty()) return null
        return try {
            val timePart = iso.split("T")[1].split(":")
            "${timePart[0]}:${timePart[1]}"
        } catch (e: Exception) {
            null
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
