package com.example.exerciseappapi

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.exerciseappapi.databinding.ItemExerciseBinding

class ExerciseAdapter(
    private val exercises: MutableList<Exercise>,
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

    fun getExerciseAt(position: Int): Exercise = exercises[position]

    fun removeExerciseAt(position: Int) {
        exercises.removeAt(position)
        notifyItemRemoved(position)
    }

    inner class ExerciseViewHolder(private val binding: ItemExerciseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(exercise: Exercise) {
            binding.loadingProgressBar.visibility = View.VISIBLE
            binding.exerciseGif.setImageDrawable(null)  // Reset the ImageView to prevent image flickering
            Glide.with(binding.root.context)
                .load(exercise.gifUrl)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.loadingProgressBar.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.loadingProgressBar.visibility = View.GONE
                        return false
                    }
                })
                .into(binding.exerciseGif)

            binding.exerciseName.text = exercise.name.capitalizeWords()
            "Target: ${exercise.target.capitalizeWords()}".also { binding.exerciseTarget.text = it }
            "Equipment: ${exercise.equipment.capitalizeWords()}".also { binding.exerciseEquipment.text = it }
        }
    }
}
