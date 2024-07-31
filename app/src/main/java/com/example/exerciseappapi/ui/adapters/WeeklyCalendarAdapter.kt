package com.example.exerciseappapi.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.exerciseappapi.databinding.ItemDayBinding
import java.text.SimpleDateFormat
import java.util.*

class WeeklyCalendarAdapter(
    private var days: List<Date>,
    private val onDayClick: (Date) -> Unit
) : RecyclerView.Adapter<WeeklyCalendarAdapter.DayViewHolder>() {

    private var selectedPosition = -1

    class DayViewHolder(private val binding: ItemDayBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(day: Date, isSelected: Boolean, onDayClick: (Date) -> Unit) {
            val locale = Locale.getDefault() // Get current default locale
            val dayFormat = SimpleDateFormat("d", locale)
            val dayOfWeekFormat = SimpleDateFormat("E", locale) // Use default locale

            binding.textViewDay.text = dayFormat.format(day)
            binding.textViewDayOfWeek.text = dayOfWeekFormat.format(day) // Use full day name
            binding.root.isSelected = isSelected
            binding.root.setOnClickListener { onDayClick(day) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val binding = ItemDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(days[position], position == selectedPosition) { date ->
            val oldPosition = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(oldPosition)
            notifyItemChanged(selectedPosition)
            onDayClick(date)
        }
    }

    override fun getItemCount() = days.size

    fun updateDays(newDays: List<Date>) {
        days = newDays
        notifyDataSetChanged()
    }

    fun setSelectedDate(date: Date) {
        val newPosition = days.indexOfFirst {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it) ==
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
        }
        if (newPosition != -1 && newPosition != selectedPosition) {
            val oldPosition = selectedPosition
            selectedPosition = newPosition
            notifyItemChanged(oldPosition)
            notifyItemChanged(selectedPosition)
        }
    }
}
