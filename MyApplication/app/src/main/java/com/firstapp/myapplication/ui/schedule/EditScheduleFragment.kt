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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.firstapp.myapplication.databinding.FragmentEditScheduleBinding

class EditScheduleFragment : Fragment() {
    private var _binding: FragmentEditScheduleBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: EditScheduleViewModel

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

        viewModel = ViewModelProvider(this, EditScheduleViewModelFactory(requireContext()))
            .get(EditScheduleViewModel::class.java)

        setupSpinner()
        setupClickListeners()
        observeViewModel()

        val scheduleId = arguments?.getInt("scheduleId") ?: return
        viewModel.loadScheduleData(scheduleId)
    }

    private fun setupClickListeners() {
        binding.saveBtn.setOnClickListener {
            val scheduleId = arguments?.getInt("scheduleId") ?: return@setOnClickListener
            viewModel.updateSchedule(scheduleId)
        }

        binding.cancelBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.startTimeInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val startTime = binding.startTimeInput.text.toString()
                val endTime = binding.endTimeInput.text.toString()
                viewModel.calculateDuration(startTime, endTime)
            }
        }

        binding.endTimeInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val startTime = binding.startTimeInput.text.toString()
                val endTime = binding.endTimeInput.text.toString()
                viewModel.calculateDuration(startTime, endTime)
            }
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

    private fun observeViewModel() {
        // Observe schedule details
        viewModel.currentSchedule.observe(viewLifecycleOwner) { schedule ->
            if (schedule != null) {
                binding.startTimeInput.setText(schedule.startTime.substring(11, 16))
                binding.endTimeInput.setText(schedule.endTime?.substring(11, 16) ?: "")
                binding.statusSpinner.setSelection(
                    listOf("Planned", "Completed", "Skipped").indexOf(schedule.status)
                )
                binding.notesInput.setText(schedule.notes ?: "")
                binding.participantsInput.setText(schedule.participantIds?.joinToString(", ") ?: "")
            }
        }

        // Observe start time
        viewModel.startTime.observe(viewLifecycleOwner) { startTime ->
            if (binding.startTimeInput.text.toString() != startTime) {
                binding.startTimeInput.setText(startTime)
            }
        }

        // Observe end time
        viewModel.endTime.observe(viewLifecycleOwner) { endTime ->
            if (binding.endTimeInput.text.toString() != endTime) {
                binding.endTimeInput.setText(endTime)
            }
        }

        // Observe status
        viewModel.status.observe(viewLifecycleOwner) { status ->
            binding.statusSpinner.setSelection(
                listOf("Planned", "Completed", "Skipped").indexOf(status)
            )
        }

        // Observe notes
        viewModel.notes.observe(viewLifecycleOwner) { notes ->
            if (binding.notesInput.text.toString() != notes) {
                binding.notesInput.setText(notes)
            }
        }

        // Observe participants
        viewModel.participants.observe(viewLifecycleOwner) { participants ->
            if (binding.participantsInput.text.toString() != participants) {
                binding.participantsInput.setText(participants)
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
            binding.saveBtn.isEnabled = !isLoading
            binding.cancelBtn.isEnabled = !isLoading
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
