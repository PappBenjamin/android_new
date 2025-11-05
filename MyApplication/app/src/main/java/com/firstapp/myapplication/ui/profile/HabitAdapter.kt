package com.firstapp.myapplication.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firstapp.myapplication.databinding.ItemHabitBinding
import com.firstapp.myapplication.network.dto.HabitResponseDto

class HabitAdapter(
    private val habits: MutableList<HabitResponseDto> = mutableListOf(),
    private val onHabitClick: (HabitResponseDto) -> Unit = {}
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    inner class HabitViewHolder(private val binding: ItemHabitBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(habit: HabitResponseDto) {
            binding.apply {
                habitName.text = habit.name
                habitDescription.text = habit.description ?: "No description"
                habitGoal.text = habit.goal ?: "No goal set"
                habitCategory.text = habit.category?.name ?: "Uncategorized"

                root.setOnClickListener {
                    onHabitClick(habit)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val binding = ItemHabitBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HabitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(habits[position])
    }

    override fun getItemCount(): Int = habits.size

    fun updateHabits(newHabits: List<HabitResponseDto>) {
        habits.clear()
        habits.addAll(newHabits)
        notifyDataSetChanged()
    }

    fun addHabit(habit: HabitResponseDto) {
        habits.add(habit)
        notifyItemInserted(habits.size - 1)
    }
}
