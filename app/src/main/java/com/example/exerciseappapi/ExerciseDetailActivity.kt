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

        @Suppress("DEPRECATION")
        val exercise: Exercise? = intent.getParcelableExtra("exercise")

        exercise?.let {
            val exerciseName: TextView = findViewById(R.id.exerciseName)

            val exerciseImage: ImageView = findViewById(R.id.exerciseImage)
            val exerciseCategory: TextView = findViewById(R.id.exerciseCategory)
            val exerciseEquipment: TextView = findViewById(R.id.exerciseEquipment)
            val exerciseTargetPrimaryMuscles: TextView = findViewById(R.id.exerciseTargetPrimaryMuscles)
            val exerciseSecondaryMuscles: TextView = findViewById(R.id.exerciseSecondaryMuscles)
            val exerciseInstructions: TextView = findViewById(R.id.exerciseInstructions)

            exerciseName.text = it.name.capitalizeWords()
            Glide.with(this).load(it.gifUrl).into(exerciseImage)
            exerciseCategory.text = it.bodyPart.capitalizeWords()
            exerciseEquipment.text = it.equipment.capitalizeWords()
            exerciseTargetPrimaryMuscles.text = it.target.capitalizeWords() // Assuming primary muscle is the target muscle
            exerciseSecondaryMuscles.text = it.secondaryMuscles.joinToString(", ").capitalizeWords()
            exerciseInstructions.text = it.instructions.joinToString("\n").capitalizeWords()
        }
    }
}
