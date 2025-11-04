package com.firstapp.myapplication.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.firstapp.myapplication.databinding.FragmentHomeBinding
import com.firstapp.myapplication.network.models.SampleScheduleData
import com.firstapp.myapplication.network.models.ScheduleResponseDto
import com.firstapp.myapplication.network.models.TimeOfDay
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {
    
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var scheduleAdapter: ScheduleAdapter
    private val schedules = mutableListOf<Any>() // Mixed list of headers and schedules

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        loadScheduleData()
    }
    
    private fun setupUI() {
        // Set current date
        val currentDate = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
        binding.tvCurrentDate.text = currentDate
        
        // Setup RecyclerView
        scheduleAdapter = ScheduleAdapter { schedule ->
            // Handle schedule item click
            onScheduleItemClick(schedule)
        }
        
        binding.rvSchedules.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = scheduleAdapter
        }
        
        // Add Schedule FAB click
        binding.fabAddSchedule.setOnClickListener {
            // Navigate to Add Schedule screen (will implement later)
            // For now, show a toast
            android.widget.Toast.makeText(requireContext(), "Add Schedule - Coming soon!", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun loadScheduleData() {
        // Instead of loading sample data, show empty time periods for today
        schedules.clear()
        
        // Always show the three main time periods (empty initially)
        schedules.add("☀️ Morning")
        // No schedules added here - user will add them via the FAB
        
        schedules.add("🌤️ Afternoon") 
        // No schedules added here - user will add them via the FAB
        
        schedules.add("🌙 Night")
        // No schedules added here - user will add them via the FAB
        
        scheduleAdapter.submitList(schedules.toList())
    }
    
    private fun onScheduleItemClick(schedule: ScheduleResponseDto) {
        // Navigate to Schedule Details screen (will implement later)
        android.widget.Toast.makeText(
            requireContext(), 
            "Schedule Details: ${schedule.habitName} - Coming soon!", 
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}