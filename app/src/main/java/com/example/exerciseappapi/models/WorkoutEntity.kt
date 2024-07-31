package com.example.exerciseappapi.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.exerciseappapi.utils.Converters
import java.util.Date

@Entity(tableName = "workouts")
@TypeConverters(Converters::class)
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val workoutName: String,
    val creationDate: Date,
    val exercises: List<Exercise>
)

fun WorkoutEntity.toWorkout(): Workout {
    return Workout(
        id = this.id,
        workoutName = this.workoutName,
        creationDate = this.creationDate,
        exercises = this.exercises.map { it }
    )
}

fun Workout.toWorkoutEntity(): WorkoutEntity {
    return WorkoutEntity(
        id = this.id,
        workoutName = this.workoutName,
        creationDate = this.creationDate,
        exercises = this.exercises.map { it }
    )
}