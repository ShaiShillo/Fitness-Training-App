package com.example.exerciseappapi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ExerciseAdapter(
    private val exercises: List<Exercise>,
    private val onItemClick: (Exercise) -> Unit
) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_exercise, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val exercise = exercises[position]
        holder.bind(exercise)
        holder.itemView.setOnClickListener { onItemClick(exercise) }
    }

    override fun getItemCount(): Int = exercises.size

    inner class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val exerciseGif: ImageView = itemView.findViewById(R.id.exerciseGif)
        private val exerciseName: TextView = itemView.findViewById(R.id.exerciseName)
        private val exerciseTarget: TextView = itemView.findViewById(R.id.exerciseTarget)
        private val exerciseEquipment: TextView = itemView.findViewById(R.id.exerciseEquipment)

        fun bind(exercise: Exercise) {
            Glide.with(itemView.context).load(exercise.gifUrl).into(exerciseGif)
            exerciseName.text = "${exercise.name.capitalizeWords()}"
            exerciseTarget.text = "Target: ${exercise.target.capitalizeWords()}"
            exerciseEquipment.text = "Equipment: ${exercise.equipment.capitalizeWords()}"
        }
    }
}


