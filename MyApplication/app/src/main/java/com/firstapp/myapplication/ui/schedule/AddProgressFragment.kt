package com.firstapp.myapplication.ui.schedule

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.firstapp.myapplication.databinding.FragmentAddProgressBinding

class AddProgressFragment : Fragment() {
    private var _binding: FragmentAddProgressBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AddProgressViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this, AddProgressViewModelFactory(requireContext()))
            .get(AddProgressViewModel::class.java)

        setupUI()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupUI() {
        // Date input will be set by ViewModel
    }

    private fun setupClickListeners() {
        binding.saveBtn.setOnClickListener {
            val scheduleId = arguments?.getInt("scheduleId") ?: return@setOnClickListener
            val date = binding.dateInput.text.toString()
            viewModel.addProgress(scheduleId, date)
        }

        binding.cancelBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        // Observe input changes
        binding.loggedTimeInput.setOnFocusChangeListener { _, _ ->
            viewModel.setLoggedTime(binding.loggedTimeInput.text.toString())
        }

        binding.notesInput.setOnFocusChangeListener { _, _ ->
            viewModel.setNotes(binding.notesInput.text.toString())
        }

        binding.completedCheckbox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setIsCompleted(isChecked)
        }
    }

    private fun observeViewModel() {
        // Observe today's date
        viewModel.todayDate.observe(viewLifecycleOwner) { todayDate ->
            binding.dateInput.setText(todayDate)
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
