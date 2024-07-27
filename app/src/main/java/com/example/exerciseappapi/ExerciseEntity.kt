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
    val gifUrl: String,
    val secondaryMuscles: List<String>,
    val instructions: List<String>
) {
    companion object {
        fun fromExercise(exercise: Exercise): ExerciseEntity {
            return ExerciseEntity(
                id = exercise.id,
                name = exercise.name,
                bodyPart = exercise.bodyPart,
                equipment = exercise.equipment,
                target = exercise.target,
                gifUrl = exercise.gifUrl,
                secondaryMuscles = exercise.secondaryMuscles,
                instructions = exercise.instructions
            )
        }
    }

    fun toExercise(): Exercise {
        return Exercise(
            id = id,
            name = name,
            bodyPart = bodyPart,
            equipment = equipment,
            target = target,
            gifUrl = gifUrl,
            secondaryMuscles = secondaryMuscles,
            instructions = instructions
        )
    }
}
