package com.example.exerciseappapi

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.exerciseappapi.databinding.ItemExerciseBinding

class ExerciseAdapter(
    private val exercises: List<Exercise>,
    private val onItemClick: (Exercise) -> Unit
) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val binding = ItemExerciseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExerciseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val exercise = exercises[position]
        holder.bind(exercise)
        holder.itemView.setOnClickListener { onItemClick(exercise) }
    }

    override fun getItemCount(): Int = exercises.size

    inner class ExerciseViewHolder(private val binding: ItemExerciseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(exercise: Exercise) {
            Glide.with(binding.root.context).load(exercise.gifUrl).into(binding.exerciseGif)
            binding.exerciseName.text = exercise.name.capitalizeWords()
            "Target: ${exercise.target.capitalizeWords()}".also { binding.exerciseTarget.text = it }
            "Equipment: ${exercise.equipment.capitalizeWords()}".also { binding.exerciseEquipment.text = it }
        }
    }
}
