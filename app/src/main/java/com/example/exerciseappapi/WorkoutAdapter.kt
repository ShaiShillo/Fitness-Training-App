package com.example.exerciseappapi

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.exerciseappapi.databinding.ItemWorkoutBinding

class WorkoutAdapter(
    private val workouts: MutableList<WorkoutEntity>,
    private val onItemClick: (WorkoutEntity) -> Unit,
    private val onEditClick: (WorkoutEntity) -> Unit,
    private val onDeleteClick: (WorkoutEntity, Int) -> Unit
) : RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val binding = ItemWorkoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WorkoutViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        holder.bind(workouts[position], position)
    }

    override fun getItemCount() = workouts.size

    fun getWorkoutAt(position: Int): WorkoutEntity = workouts[position]

    fun setWorkouts(newWorkouts: List<WorkoutEntity>) {
        workouts.clear()
        workouts.addAll(newWorkouts)
        notifyDataSetChanged()
    }

    inner class WorkoutViewHolder(private val binding: ItemWorkoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(workout: WorkoutEntity, position: Int) {
            binding.workout = workout.toWorkout()
            binding.executePendingBindings()

            binding.root.setOnClickListener {
                onItemClick(workout)
            }

            binding.editButton.setOnClickListener {
                onEditClick(workout)
            }

            binding.deleteButton.setOnClickListener {
                onDeleteClick(workout, position)
            }
        }
    }
}
