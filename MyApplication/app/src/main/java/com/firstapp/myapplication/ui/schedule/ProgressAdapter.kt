package com.firstapp.myapplication.ui.schedule

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.firstapp.myapplication.databinding.ItemProgressBinding
import com.firstapp.myapplication.network.dto.ProgressResponseDto
import java.text.SimpleDateFormat
import java.util.*

class ProgressAdapter : ListAdapter<ProgressResponseDto, ProgressAdapter.ProgressViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<ProgressResponseDto>() {
            override fun areItemsTheSame(oldItem: ProgressResponseDto, newItem: ProgressResponseDto): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ProgressResponseDto, newItem: ProgressResponseDto): Boolean {
                return oldItem.date == newItem.date &&
                        oldItem.loggedTime == newItem.loggedTime &&
                        oldItem.isCompleted == newItem.isCompleted &&
                        oldItem.notes == newItem.notes
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgressViewHolder {
        val binding = ItemProgressBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProgressViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProgressViewHolder, position: Int) {
        val progress = getItem(position)
        holder.bind(progress)
    }

    class ProgressViewHolder(
        private val binding: ItemProgressBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(progress: ProgressResponseDto) {
            binding.apply {
                // Format date
                val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                val date = dateFormat.format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(progress.date) ?: Date())
                progressDateText.text = date

                // Display logged time
                if (progress.loggedTime != null) {
                    loggedTimeText.text = "${progress.loggedTime} min"
                    loggedTimeText.visibility = android.view.View.VISIBLE
                } else {
                    loggedTimeText.visibility = android.view.View.GONE
                }

                // Display completion status
                completionStatusText.text = if (progress.isCompleted) "✓ Completed" else "○ Incomplete"
                completionStatusText.setTextColor(
                    if (progress.isCompleted) android.graphics.Color.GREEN else android.graphics.Color.GRAY
                )

                // Display notes
                if (progress.notes != null) {
                    progressNotesText.text = progress.notes
                    progressNotesText.visibility = android.view.View.VISIBLE
                } else {
                    progressNotesText.visibility = android.view.View.GONE
                }
            }
        }
    }
}

