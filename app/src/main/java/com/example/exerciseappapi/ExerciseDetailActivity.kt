package com.example.exerciseappapi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.exerciseappapi.databinding.ActivityExerciseDetailBinding
import java.util.Locale

class ExerciseDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityExerciseDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_exercise_detail)

        val exercise: Exercise? = intent.getParcelableExtra("exercise")
        exercise?.let {
            binding.exercise = it
            binding.executePendingBindings()
        }
    }
}

// Extension function to capitalize words
fun String.capitalizeWords(): String = split(" ").joinToString(" ") { it.replaceFirstChar {
    if (it.isLowerCase()) it.titlecase(
        Locale.getDefault()
    ) else it.toString()
} }
