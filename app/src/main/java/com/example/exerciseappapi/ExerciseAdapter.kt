package com.example.exerciseappapi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ExerciseAdapter(private val exercises: List<Exercise>, private val onClick: (Exercise) -> Unit) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_exercise, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        holder.bind(exercises[position])
    }

    override fun getItemCount() = exercises.size

    inner class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val exerciseName: TextView = itemView.findViewById(R.id.exerciseName)
        private val exerciseImage: ImageView = itemView.findViewById(R.id.exerciseImage)

        fun bind(exercise: Exercise) {
            exerciseName.text = exercise.name
            Glide.with(itemView.context).load(exercise.gifUrl).into(exerciseImage)
            itemView.setOnClickListener { onClick(exercise) }
        }
    }
}
