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
import com.firstapp.myapplication.databinding.FragmentAddProgressBinding
import com.firstapp.myapplication.repository.ScheduleRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddProgressFragment : Fragment() {
    private var _binding: FragmentAddProgressBinding? = null
    private val binding get() = _binding!!

    private lateinit var tokenManager: TokenManager
    private lateinit var scheduleRepository: ScheduleRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokenManager = TokenManager(requireContext())
        scheduleRepository = ScheduleRepository(tokenManager)

        setupUI()
        setupClickListeners()
    }

    private fun setupUI() {
        // Set today's date as default
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        binding.dateInput.setText(todayDate)
    }

    private fun setupClickListeners() {
        binding.saveBtn.setOnClickListener {
            addProgress()
        }

        binding.cancelBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun addProgress() {
        val scheduleId = arguments?.getInt("scheduleId") ?: return
        val date = binding.dateInput.text.toString().trim()
        val loggedTimeStr = binding.loggedTimeInput.text.toString().trim()
        val notes = binding.notesInput.text.toString().trim()
        val isCompleted = binding.completedCheckbox.isChecked

        if (date.isEmpty()) {
            showToast("Please enter a date")
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = scheduleRepository.createProgress(
                    scheduleId = scheduleId,
                    date = date,
                    loggedTime = loggedTimeStr.toIntOrNull(),
                    notes = notes.ifEmpty { null },
                    isCompleted = isCompleted
                )

                result.onSuccess {
                    showToast("Progress added successfully")
                    findNavController().popBackStack()
                }

                result.onFailure { error ->
                    showToast("Failed to add progress: ${error.message}")
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
