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
import com.firstapp.myapplication.databinding.ItemScheduleHeaderBinding
import com.firstapp.myapplication.network.models.ScheduleResponseDto
import com.firstapp.myapplication.network.models.ScheduleStatus

class ScheduleAdapter(
    private val onItemClick: (ScheduleResponseDto) -> Unit
) : ListAdapter<Any, RecyclerView.ViewHolder>(DiffCallback) {
    
    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_SCHEDULE = 1
        
        private val DiffCallback = object : DiffUtil.ItemCallback<Any>() {
            override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                return when {
                    oldItem is String && newItem is String -> oldItem == newItem
                    oldItem is ScheduleResponseDto && newItem is ScheduleResponseDto -> 
                        oldItem.id == newItem.id
                    else -> false
                }
            }
            
            override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
                return when {
                    oldItem is String && newItem is String -> oldItem == newItem
                    oldItem is ScheduleResponseDto && newItem is ScheduleResponseDto -> {
                        oldItem.habitName == newItem.habitName &&
                        oldItem.scheduledTime == newItem.scheduledTime &&
                        oldItem.status == newItem.status &&
                        oldItem.isCompleted == newItem.isCompleted &&
                        oldItem.habitIcon == newItem.habitIcon &&
                        oldItem.habitColor == newItem.habitColor
                    }
                    else -> false
                }
            }
        }
    }
    
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is String -> TYPE_HEADER
            is ScheduleResponseDto -> TYPE_SCHEDULE
            else -> throw IllegalArgumentException("Unknown item type")
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val binding = ItemScheduleHeaderBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                HeaderViewHolder(binding)
            }
            TYPE_SCHEDULE -> {
                val binding = ItemScheduleBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                ScheduleViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                val header = getItem(position) as String
                holder.bind(header)
            }
            is ScheduleViewHolder -> {
                val schedule = getItem(position) as ScheduleResponseDto
                holder.bind(schedule, onItemClick)
            }
        }
    }
    
    class HeaderViewHolder(
        private val binding: ItemScheduleHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(header: String) {
            binding.tvTimeOfDay.text = header
        }
    }
    
    class ScheduleViewHolder(
        private val binding: ItemScheduleBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(schedule: ScheduleResponseDto, onItemClick: (ScheduleResponseDto) -> Unit) {
            binding.apply {
                // Set habit icon and name
                tvHabitIcon.text = schedule.habitIcon ?: "📋"
                tvHabitName.text = schedule.habitName
                tvScheduledTime.text = schedule.scheduledTime
                
                // Set status indicator
                when (schedule.status) {
                    ScheduleStatus.COMPLETED -> {
                        ivStatusIndicator.setImageResource(R.drawable.ic_check_completed)
                        ivStatusIndicator.setColorFilter(
                            ContextCompat.getColor(root.context, android.R.color.holo_green_dark)
                        )
                    }
                    ScheduleStatus.PLANNED -> {
                        ivStatusIndicator.setImageResource(R.drawable.ic_circle_outline)
                        ivStatusIndicator.setColorFilter(
                            ContextCompat.getColor(root.context, R.color.gray_400)
                        )
                    }
                    ScheduleStatus.SKIPPED -> {
                        ivStatusIndicator.setImageResource(R.drawable.ic_close)
                        ivStatusIndicator.setColorFilter(
                            ContextCompat.getColor(root.context, android.R.color.holo_red_dark)
                        )
                    }
                }
                
                // Set habit color accent
                schedule.habitColor?.let { colorString ->
                    try {
                        val color = Color.parseColor(colorString)
                        viewColorAccent.setBackgroundColor(color)
                    } catch (e: IllegalArgumentException) {
                        // Fallback to default color
                        viewColorAccent.setBackgroundColor(
                            ContextCompat.getColor(root.context, R.color.purple_500)
                        )
                    }
                }
                
                // Set click listener
                root.setOnClickListener { onItemClick(schedule) }
            }
        }
    }
}