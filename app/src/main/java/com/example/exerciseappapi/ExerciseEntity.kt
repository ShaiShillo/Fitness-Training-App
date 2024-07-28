package com.example.exerciseappapi

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val bodyPart: String,
    val equipment: String,
    val target: String,
    val gifUrl: String,
    @TypeConverters(Converters::class)
    val secondaryMuscles: List<String>,
    @TypeConverters(Converters::class)
    val instructions: List<String>,
    val createdByUser: Boolean  // Add this field
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
                instructions = exercise.instructions,
                createdByUser = exercise.createdByUser  // Add this field
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
            instructions = instructions,
            createdByUser = createdByUser  // Add this field
        )
    }
}
