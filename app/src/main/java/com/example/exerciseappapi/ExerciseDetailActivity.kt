package com.example.exerciseappapi

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class ExerciseDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_detail)

        val exercise: Exercise? = intent.getParcelableExtra("exercise")
        exercise?.let {
            val exerciseNameTextView: TextView = findViewById(R.id.exerciseNameTextView)
            val exerciseImageView: ImageView = findViewById(R.id.exerciseImageView)
            val exerciseCategoryTextView: TextView = findViewById(R.id.exerciseCategoryTextView)
            val exerciseEquipmentTextView: TextView = findViewById(R.id.exerciseEquipmentTextView)
            val exerciseForceTextView: TextView = findViewById(R.id.exerciseForceTextView)
            val exerciseLevelTextView: TextView = findViewById(R.id.exerciseLevelTextView)
            val exercisePrimaryMusclesTextView: TextView = findViewById(R.id.exercisePrimaryMusclesTextView)
            val exerciseSecondaryMusclesTextView: TextView = findViewById(R.id.exerciseSecondaryMusclesTextView)
            val exerciseInstructionsTextView: TextView = findViewById(R.id.exerciseInstructionsTextView)

            exerciseNameTextView.text = it.name
            Glide.with(this).load(it.gifUrl).into(exerciseImageView)
            exerciseCategoryTextView.text = "Category: ${it.bodyPart}"
            exerciseEquipmentTextView.text = "Equipment: ${it.equipment}"
            exerciseForceTextView.text = "Target: ${it.target}"
            exerciseLevelTextView.text = "Level: ${it.secondaryMuscles.joinToString(", ")}"
            exercisePrimaryMusclesTextView.text = "Primary Muscles: ${it.secondaryMuscles.joinToString(", ")}"
            exerciseSecondaryMusclesTextView.text = "Secondary Muscles: ${it.secondaryMuscles.joinToString(", ")}"
            exerciseInstructionsTextView.text = "Instructions: ${it.instructions.joinToString("\n")}"
        }
    }
}
