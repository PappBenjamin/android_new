package com.firstapp.myapplication.ui.habit

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
import com.firstapp.myapplication.databinding.FragmentAddHabitBinding
import com.firstapp.myapplication.network.dto.HabitCategoryDto
import com.firstapp.myapplication.repository.ProfileRepository
import kotlinx.coroutines.launch

class AddHabitFragment : Fragment() {
    private var _binding: FragmentAddHabitBinding? = null
    private val binding get() = _binding!!
    private lateinit var tokenManager: TokenManager
    private lateinit var profileRepository: ProfileRepository
    private var selectedCategoryId: Int? = null
    private var categories: List<HabitCategoryDto> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddHabitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokenManager = TokenManager(requireContext())
        profileRepository = ProfileRepository(tokenManager)

        setupClickListeners()
        loadHabitCategories()
    }

    private fun setupClickListeners() {
        binding.createBtn.setOnClickListener {
            createHabit()
        }

        binding.cancelBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun loadHabitCategories() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = profileRepository.getHabitCategories()
                result.onSuccess { categoryList ->
                    categories = categoryList
                    setupCategorySpinner(categoryList)
                }
                result.onFailure { error ->
                    showToast("Failed to load categories: ${error.message}")
                }
            } catch (e: Exception) {
                showToast("Error loading categories: ${e.message}")
            }
        }
    }

    private fun setupCategorySpinner(categoryList: List<HabitCategoryDto>) {
        val categoryNames = categoryList.map { it.name }

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
                    selectedCategoryId = categories[position].id
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedCategoryId = null
            }
        }
    }

    private fun createHabit() {
        val name = binding.habitNameInput.text.toString().trim()
        val description = binding.habitDescriptionInput.text.toString().trim()
        val goal = binding.habitGoalInput.text.toString().trim()

        // Validation
        if (name.isEmpty()) {
            showToast("Please enter a habit name")
            return
        }

        if (selectedCategoryId == null) {
            showToast("Please select a category")
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = profileRepository.createHabit(
                    name = name,
                    description = description.ifEmpty { null },
                    goal = goal.ifEmpty { null },
                    categoryId = selectedCategoryId!!
                )

                result.onSuccess { habit ->
                    showToast("Habit '${habit.name}' created successfully!")
                    findNavController().popBackStack()
                }

                result.onFailure { error ->
                    showToast("Failed to create habit: ${error.message}")
                }
            } catch (e: Exception) {
                showToast("Error creating habit: ${e.message}")
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
