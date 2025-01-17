package com.example.exerciseappapi.ui.adapters

import android.graphics.drawable.Drawable
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.util.set
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.exerciseappapi.models.Exercise
import com.example.exerciseappapi.databinding.ItemExerciseBinding
import com.example.exerciseappapi.ui.fragments.capitalizeWords

class ExerciseAdapter(
    private val exercises: MutableList<Exercise>,
    private val onItemClick: (Exercise) -> Unit,
    private val onEditClick: (Exercise) -> Unit,
    private val isSelectingExercises: Boolean = false,
    private val showEditButton: Boolean = true,
    private val showCheckboxes: Boolean = true,
    private val onExerciseSelected: (Exercise, Boolean) -> Unit = { _, _ -> }
) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {
    private val selectedItems = SparseBooleanArray()


    fun addExerciseToTop(exercise: Exercise) {
        exercises.add(0, exercise)
        notifyItemInserted(0)
    }
    fun setExercises(newExercises: List<Exercise>) {
        exercises.clear()
        exercises.addAll(newExercises)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val binding = ItemExerciseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExerciseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val exercise = exercises[position]
        holder.bind(exercise, selectedItems[position, false])
        holder.itemView.setOnClickListener { onItemClick(exercise) }
        holder.binding.editButton.setOnClickListener { onEditClick(exercise) }
    }

    override fun getItemCount(): Int = exercises.size

    fun getExerciseAt(position: Int): Exercise = exercises[position]

    fun removeExerciseAt(position: Int) {
        exercises.removeAt(position)
        notifyItemRemoved(position)
    }

    inner class ExerciseViewHolder(val binding: ItemExerciseBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(exercise: Exercise, isSelected: Boolean) {
            if (isSelectingExercises && showCheckboxes) {
                binding.checkbox.visibility = View.VISIBLE
                binding.editButton.visibility = View.GONE
                binding.checkbox.setOnCheckedChangeListener(null) // Remove previous listener
                binding.checkbox.isChecked = isSelected
                binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
                    selectedItems[adapterPosition] = isChecked
                    onExerciseSelected(exercise, isChecked)
                }
            } else {
                binding.checkbox.visibility = View.GONE
                binding.editButton.visibility = if (exercise.createdByUser && showEditButton) View.VISIBLE else View.GONE
            }
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
