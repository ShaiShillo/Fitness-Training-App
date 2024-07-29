package com.example.exerciseappapi

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.exerciseappapi.databinding.ItemWorkoutBinding

class WorkoutAdapter(
    private val workouts: MutableList<Workout>,
    private val onEditClick: (Workout) -> Unit,
    private val onDeleteClick: (Workout, Int) -> Unit
) : RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val binding = ItemWorkoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WorkoutViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        holder.bind(workouts[position], position)
    }

    override fun getItemCount() = workouts.size

    fun getWorkoutAt(position: Int): Workout = workouts[position]

    fun setWorkouts(newWorkouts: List<Workout>) {
        workouts.clear()
        workouts.addAll(newWorkouts)
        notifyDataSetChanged()
    }

    inner class WorkoutViewHolder(private val binding: ItemWorkoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(workout: Workout, position: Int) {
            binding.workout = workout
            binding.executePendingBindings()

            binding.editButton.setOnClickListener {
                onEditClick(workout)
            }

            binding.deleteButton.setOnClickListener {
                onDeleteClick(workout, position)
            }
        }
    }
}
