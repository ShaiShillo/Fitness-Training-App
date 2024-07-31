package com.example.exerciseappapi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.exerciseappapi.databinding.ItemWorkoutBinding

class WorkoutAdapter(
    private val workouts: MutableList<WorkoutEntity>,
    private val onItemClick: (WorkoutEntity) -> Unit,
    private val onEditClick: (WorkoutEntity) -> Unit,
    private val onDeleteClick: (WorkoutEntity, Int) -> Unit,
    private val showEditButton: Boolean,
    private val showDeleteButton: Boolean
) : RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val binding = ItemWorkoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WorkoutViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        holder.bind(workouts[position])
    }

    override fun getItemCount(): Int = workouts.size

    fun setWorkouts(newWorkouts: List<WorkoutEntity>) {
        workouts.clear()
        workouts.addAll(newWorkouts)
        notifyDataSetChanged()
    }

    fun getWorkoutAt(position: Int): WorkoutEntity = workouts[position]

    inner class WorkoutViewHolder(private val binding: ItemWorkoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(workout: WorkoutEntity) {
            binding.workout = workout.toWorkout()
            binding.executePendingBindings()

            binding.root.setOnClickListener {
                onItemClick(workout)
            }

            binding.editButton.visibility = if (showEditButton) View.VISIBLE else View.GONE
            binding.deleteButton.visibility = if (showDeleteButton) View.VISIBLE else View.GONE

            binding.deleteButton.setOnClickListener {
                onDeleteClick(workout, adapterPosition)
            }

            binding.editButton.setOnClickListener {
                onEditClick(workout)
            }
        }
    }
}
