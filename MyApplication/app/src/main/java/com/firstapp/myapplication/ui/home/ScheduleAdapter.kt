package com.firstapp.myapplication.ui.home

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.firstapp.myapplication.R
import com.firstapp.myapplication.databinding.ItemScheduleBinding
import com.firstapp.myapplication.network.dto.ScheduleResponseDto

class ScheduleAdapter(
    private val onItemClick: (Int) -> Unit
) : ListAdapter<ScheduleResponseDto, ScheduleAdapter.ScheduleViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<ScheduleResponseDto>() {
            override fun areItemsTheSame(oldItem: ScheduleResponseDto, newItem: ScheduleResponseDto): Boolean {
                return oldItem.id == newItem.id
            }
            
            override fun areContentsTheSame(oldItem: ScheduleResponseDto, newItem: ScheduleResponseDto): Boolean {
                return oldItem.habit?.name == newItem.habit?.name &&
                        oldItem.startTime == newItem.startTime &&
                        oldItem.status == newItem.status &&
                        oldItem.durationMinutes == newItem.durationMinutes
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val binding = ItemScheduleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ScheduleViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val schedule = getItem(position)
        holder.bind(schedule, onItemClick)
    }

    class ScheduleViewHolder(
        private val binding: ItemScheduleBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(schedule: ScheduleResponseDto, onItemClick: (Int) -> Unit) {
            binding.apply {
                // Set habit name from habit object
                tvHabitName.text = schedule.habit?.name ?: "Habit"
                tvScheduledTime.text = schedule.startTime


                // Set status indicator
                when (schedule.status) {
                    "Completed" -> {
                        ivStatusIndicator.setImageResource(R.drawable.ic_check_completed)
                        ivStatusIndicator.setColorFilter(
                            ContextCompat.getColor(root.context, android.R.color.holo_green_dark)
                        )
                    }
                    "Planned" -> {
                        ivStatusIndicator.setImageResource(R.drawable.ic_circle_outline)
                        ivStatusIndicator.setColorFilter(
                            ContextCompat.getColor(root.context, R.color.gray_400)
                        )
                    }
                    "Skipped" -> {
                        ivStatusIndicator.setImageResource(R.drawable.ic_close)
                        ivStatusIndicator.setColorFilter(
                            ContextCompat.getColor(root.context, android.R.color.holo_red_dark)
                        )
                    }
                }
                
                // Set click listener
                root.setOnClickListener { onItemClick(schedule.id) }
            }
        }
    }
}