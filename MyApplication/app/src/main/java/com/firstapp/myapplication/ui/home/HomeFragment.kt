package com.firstapp.myapplication.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.firstapp.myapplication.R
import com.firstapp.myapplication.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: HomeViewModel
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
        
        viewModel = ViewModelProvider(this, HomeViewModelFactory(requireContext()))
            .get(HomeViewModel::class.java)

        setupUI()
        observeViewModel()
        viewModel.loadScheduleData()
    }
    
    private fun setupUI() {
        scheduleAdapter = ScheduleAdapter { scheduleId ->
            viewModel.onScheduleItemClick(scheduleId)
        }
        
        binding.rvSchedules.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = scheduleAdapter
        }
        
        binding.fabAddSchedule.setOnClickListener {
            viewModel.navigateToAddSchedule()
        }

        // Add navigation buttons for day navigation
        binding.btnPreviousDay.setOnClickListener {
            viewModel.previousDay()
        }

        binding.btnNextDay.setOnClickListener {
            viewModel.nextDay()
        }
    }

    private fun observeViewModel() {
        // Observe current date
        viewModel.currentDate.observe(viewLifecycleOwner) { currentDate ->
            binding.tvCurrentDate.text = currentDate
        }

        // Observe schedules
        viewModel.schedules.observe(viewLifecycleOwner) { schedules ->
            scheduleAdapter.submitList(schedules)
        }

        // Observe toast messages
        viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                showToast(message)
                viewModel.clearToastMessage()
            }
        }

        // Observe navigation to Add Schedule
        viewModel.navigateToAddSchedule.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                try {
                    findNavController().navigate(R.id.addScheduleFragment)
                } catch (e: Exception) {
                    showToast("Navigation to Add Schedule failed: ${e.message}")
                }
                viewModel.clearNavigationFlags()
            }
        }

        // Observe navigation to Schedule Details
        viewModel.navigateToScheduleDetails.observe(viewLifecycleOwner) { scheduleId ->
            if (scheduleId != null) {
                val bundle = Bundle().apply {
                    putInt("scheduleId", scheduleId)
                }
                try {
                    findNavController().navigate(R.id.scheduleDetailsFragment, bundle)
                } catch (e: Exception) {
                    showToast("Navigation to schedule details failed: ${e.message}")
                }
                viewModel.clearNavigationFlags()
            }
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.fabAddSchedule.isEnabled = !isLoading
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