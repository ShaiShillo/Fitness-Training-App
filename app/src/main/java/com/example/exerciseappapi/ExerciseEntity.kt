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
    val gifUrl: String
)

fun ExerciseEntity.toExercise(): Exercise {
    return Exercise(
        id = this.id,
        name = this.name,
        bodyPart = this.bodyPart,
        equipment = this.equipment,
        target = this.target,
        gifUrl = this.gifUrl,
        secondaryMuscles = emptyList(),
        instructions = emptyList()
    )
}

fun Exercise.toExerciseEntity(): ExerciseEntity {
    return ExerciseEntity(
        id = this.id,
        name = this.name,
        bodyPart = this.bodyPart,
        equipment = this.equipment,
        target = this.target,
        gifUrl = this.gifUrl
    )
}
