package com.example.exerciseappapi

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val bodyPart: String,
    val equipment: String,
    val target: String,
    var gifUrl: String,
    val secondaryMuscles: List<String>,
    val instructions: List<String>
) {
    fun toExercise(): Exercise {
        return Exercise(id, name, bodyPart, equipment, target, gifUrl, secondaryMuscles, instructions)
    }

    companion object {
        fun fromExercise(exercise: Exercise): ExerciseEntity {
            return ExerciseEntity(
                exercise.id, exercise.name, exercise.bodyPart, exercise.equipment,
                exercise.target, exercise.gifUrl, exercise.secondaryMuscles, exercise.instructions
            )
        }
    }
}
