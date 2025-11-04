package com.firstapp.myapplication.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.firstapp.myapplication.R
import com.firstapp.myapplication.auth.TokenManager
import com.firstapp.myapplication.databinding.FragmentHomeBinding
import com.firstapp.myapplication.repository.ScheduleRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {
    
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var tokenManager: TokenManager
    private lateinit var scheduleRepository: ScheduleRepository
    private lateinit var scheduleAdapter: ScheduleAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        tokenManager = TokenManager(requireContext())
        scheduleRepository = ScheduleRepository(tokenManager)

        setupUI()
        loadScheduleData()
    }
    
    private fun setupUI() {
        // Set current date
        val currentDate = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
        binding.tvCurrentDate.text = currentDate
        
        // Setup RecyclerView
        scheduleAdapter = ScheduleAdapter { scheduleId ->
            onScheduleItemClick(scheduleId)
        }
        
        binding.rvSchedules.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = scheduleAdapter
        }
        
        // Add Schedule FAB click - Navigate to AddScheduleFragment
        binding.fabAddSchedule.setOnClickListener {
            try {
                findNavController().navigate(R.id.addScheduleFragment)
            } catch (e: Exception) {
                showToast("Navigation to Add Schedule failed: ${e.message}")
            }
        }
    }
    
    private fun loadScheduleData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = scheduleRepository.getSchedulesByDay()
                result.onSuccess { schedules ->
                    if (schedules.isEmpty()) {
                        showToast("No schedules for today")
                        scheduleAdapter.submitList(emptyList())
                    } else {
                        scheduleAdapter.submitList(schedules)
                    }
                }
                result.onFailure { error ->
                    showToast("Failed to load schedules: ${error.message}")
                }
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun onScheduleItemClick(scheduleId: Int) {
        showToast("Schedule Details - Coming soon (ID: $scheduleId)")
    }
    
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}