package com.firstapp.myapplication.ui.schedule

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.firstapp.myapplication.databinding.FragmentAddScheduleBinding

class AddScheduleFragment : Fragment() {
    private var _binding: FragmentAddScheduleBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AddScheduleViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this, AddScheduleViewModelFactory(requireContext()))
            .get(AddScheduleViewModel::class.java)

        setupUI()
        setupClickListeners()
        observeViewModel()
        viewModel.loadHabits()
    }

    private fun setupUI() {
        // Setup repeat pattern spinner
        val repeatPatterns = listOf("None", "Daily", "Weekdays", "Weekends")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, repeatPatterns)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.repeatPatternSpinner.adapter = adapter

        binding.repeatPatternSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val pattern = when (position) {
                    0 -> "none"
                    1 -> "daily"
                    2 -> "weekdays"
                    3 -> "weekends"
                    else -> "none"
                }
                viewModel.setRepeatPattern(pattern)

                // Show/hide relevant fields based on pattern
                if (pattern == "none") {
                    binding.dateInput.visibility = View.VISIBLE
                    binding.dateLabel.visibility = View.VISIBLE
                } else {
                    binding.dateInput.visibility = View.GONE
                    binding.dateLabel.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Setup time input listeners for duration calculation
        binding.startTimeInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) calculateDuration()
        }

        binding.endTimeInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) calculateDuration()
        }
    }

    private fun calculateDuration() {
        val startTime = binding.startTimeInput.text.toString().trim()
        val endTime = binding.endTimeInput.text.toString().trim()
        viewModel.calculateDuration(startTime, endTime)
    }

    private fun setupClickListeners() {
        binding.createBtn.setOnClickListener {
            val startTime = binding.startTimeInput.text.toString()
            val endTime = binding.endTimeInput.text.toString()
            val date = binding.dateInput.text.toString()
            val notes = binding.notesInput.text.toString()
            viewModel.createSchedule(startTime, endTime, date, notes)
        }

        binding.cancelBtn.setOnClickListener {
            try {
                findNavController().popBackStack()
            } catch (_: Exception) {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun observeViewModel() {
        // Observe today's date
        viewModel.todayDate.observe(viewLifecycleOwner) { todayDate ->
            binding.dateInput.setText(todayDate)
        }

        // Observe habits
        viewModel.habits.observe(viewLifecycleOwner) { habits ->
            setupHabitSpinner(habits)
        }

        // Observe calculated duration
        viewModel.calculatedDuration.observe(viewLifecycleOwner) { duration ->
            if (duration != null && duration > 0) {
                binding.durationInput.setText(duration.toString())
            }
        }

        // Observe toast messages
        viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                showToast(message)
                viewModel.clearToastMessage()
            }
        }

        // Observe navigation back
        viewModel.navigateBack.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                findNavController().popBackStack()
                viewModel.clearNavigateBack()
            }
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.createBtn.isEnabled = !isLoading
            binding.cancelBtn.isEnabled = !isLoading
        }
    }

    private fun setupHabitSpinner(habits: List<com.firstapp.myapplication.network.dto.HabitResponseDto>) {
        val habitNames = habits.map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, habitNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.habitSpinner.adapter = adapter

        binding.habitSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position >= 0 && position < habits.size) {
                    viewModel.setSelectedHabit(habits[position].id)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                viewModel.setSelectedHabit(0)
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
