package com.firstapp.myapplication.ui.habit

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
import com.firstapp.myapplication.databinding.FragmentAddHabitBinding

class AddHabitFragment : Fragment() {
    private var _binding: FragmentAddHabitBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AddHabitViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddHabitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this, AddHabitViewModelFactory(requireContext()))
            .get(AddHabitViewModel::class.java)

        setupClickListeners()
        observeViewModel()
        viewModel.loadHabitCategories()
    }

    private fun setupClickListeners() {
        binding.createBtn.setOnClickListener {
            val name = binding.habitNameInput.text.toString()
            val description = binding.habitDescriptionInput.text.toString()
            val goal = binding.habitGoalInput.text.toString()
            viewModel.createHabit(name, description, goal)
        }

        binding.cancelBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeViewModel() {
        // Observe categories
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            setupCategorySpinner(categories)
        }

        // Observe selected category
        viewModel.selectedCategoryId.observe(viewLifecycleOwner) { categoryId ->
            // Category selected, ready to create
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

    private fun setupCategorySpinner(categories: List<com.firstapp.myapplication.network.dto.HabitCategoryDto>) {
        val categoryNames = categories.map { it.name }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categoryNames
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.categorySpinner.adapter = adapter

        binding.categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position >= 0 && position < categories.size) {
                    viewModel.setSelectedCategory(categories[position].id)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                viewModel.setSelectedCategory(0)
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
