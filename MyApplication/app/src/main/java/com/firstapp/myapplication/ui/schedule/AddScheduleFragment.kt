package com.firstapp.myapplication.ui.schedule

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.firstapp.myapplication.auth.TokenManager
import com.firstapp.myapplication.databinding.FragmentAddScheduleBinding
import com.firstapp.myapplication.network.dto.HabitResponseDto
import com.firstapp.myapplication.repository.ProfileRepository
import com.firstapp.myapplication.repository.ScheduleRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddScheduleFragment : Fragment() {
    private var _binding: FragmentAddScheduleBinding? = null
    private val binding get() = _binding!!

    private lateinit var tokenManager: TokenManager
    private lateinit var profileRepository: ProfileRepository
    private lateinit var scheduleRepository: ScheduleRepository

    private var habits: List<HabitResponseDto> = emptyList()
    private var selectedHabitId: Int? = null
    private var repeatPattern: String = "none"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokenManager = TokenManager(requireContext())
        profileRepository = ProfileRepository(tokenManager)
        scheduleRepository = ScheduleRepository(tokenManager)

        setupUI()
        setupClickListeners()
        loadHabits()
    }

    private fun setupUI() {
        // Set today's date as default
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        binding.dateInput.setText(todayDate)

        // Setup duration calculation on time input changes
        binding.startTimeInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) calculateDuration()
        }

        binding.endTimeInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) calculateDuration()
        }

        // Setup repeat pattern spinner
        val repeatPatterns = listOf("None", "Daily", "Weekdays", "Weekends")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, repeatPatterns)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.repeatPatternSpinner.adapter = adapter

        binding.repeatPatternSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                repeatPattern = when (position) {
                    0 -> "none"
                    1 -> "daily"
                    2 -> "weekdays"
                    3 -> "weekends"
                    else -> "none"
                }

                // Show/hide relevant fields based on pattern
                if (repeatPattern == "none") {
                    binding.dateInput.visibility = View.VISIBLE
                    binding.dateLabel.visibility = View.VISIBLE
                } else {
                    binding.dateInput.visibility = View.GONE
                    binding.dateLabel.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun calculateDuration() {
        val startTime = binding.startTimeInput.text.toString().trim()
        val endTime = binding.endTimeInput.text.toString().trim()

        if (startTime.isEmpty() || endTime.isEmpty()) {
            return
        }

        try {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val startDate = timeFormat.parse(startTime)
            val endDate = timeFormat.parse(endTime)

            if (startDate != null && endDate != null) {
                var durationMs = endDate.time - startDate.time

                // Handle case where end time is next day
                if (durationMs < 0) {
                    durationMs += 24 * 60 * 60 * 1000
                }

                val durationMinutes = (durationMs / (1000 * 60)).toInt()

                if (durationMinutes > 0) {
                    binding.durationInput.setText(durationMinutes.toString())
                }
            }
        } catch (_: Exception) {
            // If parsing fails, don't update duration
        }
    }

    private fun setupClickListeners() {

        binding.createBtn.setOnClickListener {
            createSchedule()
        }

        binding.cancelBtn.setOnClickListener {
            try {
                findNavController().popBackStack()
            } catch (_: Exception) {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun loadHabits() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = profileRepository.getHabits()
                result.onSuccess { habitList ->
                    habits = habitList
                    setupHabitSpinner(habitList)
                }
                result.onFailure { error ->
                    showToast("Failed to load habits: ${error.message}")
                }
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun setupHabitSpinner(habitList: List<HabitResponseDto>) {
        val habitNames = habitList.map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, habitNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.habitSpinner.adapter = adapter

        binding.habitSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position >= 0 && position < habits.size) {
                    selectedHabitId = habits[position].id
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedHabitId = null
            }
        }
    }

    private fun createSchedule() {
        val startTime = binding.startTimeInput.text.toString().trim()
        val endTime = binding.endTimeInput.text.toString().trim()
        val date = binding.dateInput.text.toString().trim()
        val notes = binding.notesInput.text.toString().trim()

        if (selectedHabitId == null) {
            showToast("Please select a habit")
            return
        }

        if (startTime.isEmpty()) {
            showToast("Please enter start time")
            return
        }

        if (repeatPattern == "none" && date.isEmpty()) {
            showToast("Please enter a date")
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val endTimeOrNull = endTime.ifEmpty { null }
                val notesOrNull = notes.ifEmpty { null }

                if (repeatPattern == "none") {
                    // Create custom schedule
                    val result = scheduleRepository.createCustomSchedule(
                        habitId = selectedHabitId!!,
                        date = date,
                        startTime = startTime,
                        endTime = endTimeOrNull,
                        notes = notesOrNull
                    )

                    result.onSuccess {
                        showToast("Schedule created successfully")
                        findNavController().popBackStack()
                    }

                    result.onFailure { error ->
                        showToast("Failed to create schedule: ${error.message}")
                    }
                } else {
                    // Create recurring schedule
                    val repeatPatternValue = when (repeatPattern) {
                        "daily" -> "daily"
                        "weekdays" -> "weekdays"
                        "weekends" -> "weekends"
                        else -> "none"
                    }
                    val result = scheduleRepository.createRecurringSchedule(
                        habitId = selectedHabitId!!,
                        startTime = startTime,
                        repeatPattern = repeatPatternValue,
                        endTime = endTimeOrNull,
                        notes = notesOrNull
                    )

                    result.onSuccess {
                        showToast("Recurring schedule created successfully")
                        findNavController().popBackStack()
                    }

                    result.onFailure { error ->
                        showToast("Failed to create recurring schedule: ${error.message}")
                    }
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
